# Test OpMode Refactoring Plan

**Status:** Planning Phase  
**Last Updated:** 2025-11-09  
**Scope:** Comprehensive separation of Testing and Tuning OpModes with improved Action calling patterns

---

## Overview

This document outlines the complete refactoring strategy for reorganizing test and tuning OpModes. The goal is to establish a clear separation of concerns:

- **Testing OpModes** (`OpModes/Tests/`): Used for hands-on exploration of each subsystem's behavior.
- **Tuning OpModes** (`OpModes/Tuning/`): Used for adjusting constants and control parameters (FTC Dashboard).
- **Comprehensive Test** (`OpModes/Tests/`): Run prior to competition for high-level system checks.

Key architectural improvement:  
**Action calling patterns:**
- TODO: CREATE UPDATED ACTION CALLING PATTERN

---

## Current State Analysis

### Existing Test OpModes

1. **Test_Intake** - Motor forward/reverse/stop
2. **Test_Shooter** - Dual motor sync with power offset
3. **Test_Transfer** - Servos for transfer belt and intake door
4. **Test_Spindexer** - PID position control and color storage
5. **Test_ColorDetector** - HSV threshold calibration
6. **Test_IntegrationAll** - Complete workflow (mixed concerns)

### Current Issues

1. **Blurred Lines**: Test OpModes contain both validation logic and tuning UI
2. **Long Duration**: Test_IntegrationAll unclear what's being tested vs. tuned
3. **Manual State Management**: Test_IntegrationAll manually runs `action.run(null)` in a loop
4. **Inconsistent Patterns**: No unified approach to action scheduling
5. **Action Timing**: Tests don't properly leverage blocking/non-blocking action patterns

---

## Architecture: Test vs. Tuning Separation

### Testing Philosophy

**Tests are not for validation or automation.**  
A test OpMode should:
- Allow a human operator to *experiment and visually inspect* subsystems
- Report only telemetry feedback
- Never provide automatic pass/fail, nor block execution
- Be fully driver controlled (no sequences run unless user initiates them)

### Tuning Philosophy

**Tuning is strictly for optimization.**  
A tuning OpMode should:
- Allow the operator to adjust parameters in real-time
- Be modal, focused on individual subsystem behaviour
- Avoid any automated validation or reporting


---

## Directory Structure

```
OpModes/
├── Tests/
│   ├── REFACTORING_PLAN.md          (this file)
│   ├── ComprehensiveTest.java       (pre-competition, ≤2 min)
│   ├── Test_Intake.java             (unit test, ≤1 min)
│   ├── Test_Shooter.java            (unit test, ≤1 min)
│   ├── Test_Transfer.java           (unit test, ≤1 min)
│   ├── Test_Spindexer.java          (unit test, ≤1 min)
│   ├── Test_ColorDetector.java      (unit test, ≤1 min)
│   ├── Test_IntakeBall.java         (action test, ≤1 min) [NEW]
│   └── Test_ShootBall.java          (action test, ≤1 min) [NEW]
│
└── Tuning/
    ├── Tune_Shooter.java            (optimize RUN_POWER, UPPER_OFFSET, LOWER_OFFSET)
    ├── Tune_Spindexer.java          (optimize P, I, D, F, zeroOffset)
    └── Tune_ColorDetector.java      (optimize hue/saturation thresholds)
```

---

## Test OpMode Specifications

All test OpModes follow this pattern:

- Purpose: Allow a human operator to interact with the subsystem and visually observe its response.
- Duration: As needed (no automated limit).
- Required Setup: None unless noted in individual specs.
- Driver Controls: Gamepad controls mapped to subsystem actions.
- Telemetry Output: Display key states, sensor data, actuator feedback.
- *No automated pass/fail reporting. Only visual feedback and operator judgment guide further action.*

---

## Comprehensive Test (Pre-Competition)

**OpMode:** `ComprehensiveTest.java`

**Purpose:** Single automated test to verify all robot systems before competition  
**Duration:** ≤2 minutes  
**Required Setup:** Spindexer must be zeroed (one-time setup)

**Test Workflow:**

```
Phase 1: Subsystem Checks (0.5s each, 3s total)
├── Intake: Spin IN/OUT
├── Shooter: Spin up
└── Transfer: Move belt and door

Phase 2: Spindexer Validation (2.5s)
├── Zero sequence (already done once)
├── Move to position 0
└── Move to position 1

Phase 3: Color Detection (1.5s)
├── Read baseline
└── Detect color in view

Phase 4: Action Sequence (4.5s) [requires ball at intake]
├── IntakeBall action (full sequence)
└── ShootBall action (alignment only)

Phase 5: Summary (0.5s)
└── Report PASS/FAIL for each component
```

**Key Characteristic:** Minimal user interaction

- Single button press to START
- Automatic execution of all tests
- Clear visual PASS/FAIL indicators
- Emergency stop (Y) available anytime

**Telemetry Output:**

```
=== COMPREHENSIVE PRE-COMPETITION TEST ===
Test Duration: 8.3 / 120.0 sec

[ ✓ PASS ] Intake Spin Test
[ ✓ PASS ] Shooter Spin Test
[ ✓ PASS ] Transfer Belt Test
[ ✓ PASS ] Spindexer Position Test
[ ✓ PASS ] Color Detector Test
[ ⏳ RUN  ] Action Sequence Test

Failed Tests: 0
Warnings: None

OVERALL STATUS: ✓ READY FOR COMPETITION
```

---

## Tuning OpMode Specifications

All tuning OpModes follow this pattern:

### Design Principles

1. **Single Concern**: Each tuning OpMode focuses on ONE subsystem
2. **FTC Dashboard Integration**: All constants marked with `@Config`
3. **Real-Time Feedback**: Show sensor readings and calculated values
4. **No Automation**: User has full manual control
5. **Indefinite Runtime**: No timeout; user stops when done
6. **Clear Ranges**: Show min/max for all adjustable values

### 1. Tune_Shooter

**Purpose:** Synchronize upper and lower motors via offsets  
**FTC Dashboard Constants:**

```
RUN_POWER: [-1.0, 1.0] (default: 1.0)
UPPER_OFFSET: [-1.0, 1.0] (default: 0.0)
LOWER_OFFSET: [-1.0, 1.0] (default: 0.0)
STOP_POWER: [-1.0, 1.0] (default: 0.0)
```

**UI:**

- DPAD UP/DOWN: Adjust RUN_POWER (±0.05)
- LB/RB: Adjust UPPER_OFFSET (±0.01)
- LT/RT: Adjust LOWER_OFFSET (±0.01)
- A: Run both
- B: Stop both
- Y: Reset to defaults

**Telemetry:**

```
=== SHOOTER TUNING ===
[Motor Speeds]
Upper Motor: 2400 RPM
Lower Motor: 2380 RPM
Difference: -0.83% ✓

[Adjust with Gamepad]
RUN_POWER:    1.0  (DPAD ↑↓)
UPPER_OFFSET: 0.0  (LB/RB)
LOWER_OFFSET: 0.0  (LT/RT)

[Control]
A: RUN    B: STOP    Y: RESET
```

---

### 2. Tune_Spindexer

**Purpose:** Optimize PID coefficients for smooth position control  
**FTC Dashboard Constants:**

```
P: [0.0, 0.1] (default: 0.005)
I: [0.0, 0.01] (default: 0.0)
D: [0.0, 0.001] (default: 0.0)
F: [0.0, 0.1] (default: 0.0)
zeroOffset: [-100, 100] (default: 0)
```

**UI:**

- DPAD UP/DOWN: Adjust P (±0.0005)
- LB/RB: Adjust I (±0.0001)
- LT/RT: Adjust D (±0.00001)
- B: Move to position 0.25
- X: Move to position 0.5
- Y: Move to position 0.75
- A: Zero sequence
- Right Stick: Fine-tune with bigger increments

**Telemetry:**

```
=== SPINDEXER PID TUNING ===
[PID Coefficients]
P: 0.005 (DPAD ↑↓)
I: 0.0   (LB/RB)
D: 0.001 (LT/RT)
F: 0.0

[Position Control]
Current:  0.250 rev (90°)
Target:   0.500 rev (180°)
Error:    12.3 ticks

[Control]
B/X/Y: Move to position
A: ZERO (required first)
```

---

### 5. Tune_ColorDetector

**Purpose:** Calibrate hue and saturation thresholds for accurate detection  
**FTC Dashboard Constants:**

```
GREEN_HUE_MIN: [0, 360] (default: 100)
GREEN_HUE_MAX: [0, 360] (default: 140)
PURPLE_HUE_MIN: [0, 360] (default: 250)
PURPLE_HUE_MAX: [0, 360] (default: 290)
MIN_SATURATION: [0.0, 1.0] (default: 0.4)
```

**UI:**

- DPAD UP/DOWN: Adjust GREEN_HUE_MIN (±1)
- DPAD RIGHT/LEFT: Adjust GREEN_HUE_MAX (±1)
- LB/RB: Adjust PURPLE_HUE_MIN (±1)
- LT/RT: Adjust PURPLE_HUE_MAX (±1)
- A/Y: Adjust MIN_SATURATION (±0.05)
- X: Reset to defaults

**Telemetry:**

```
=== COLOR DETECTOR TUNING ===
[Thresholds]
GREEN HUE:   100-140 (DPAD ↑↓ / ←→)
PURPLE HUE:  250-290 (LB/RB / LT/RT)
MIN SAT:     0.4 (A/Y)

[Current Reading]
RGB: (120, 200, 80)
HSV: (115.2°, 0.6, 0.78)
Detected: GREEN ✓

[Status]
Stable readings: ✓
Color changing: No
```

---

**Creating HOW_TO_TEST.md and HOW_TO_TUNE.md**

To support maintainability and onboarding, create two comprehensive guides:

**1. HOW_TO_TEST.md**
- **Purpose:** Document detailed step-by-step procedures for running each Test OpMode and interpreting results.
- **Required Sections:**
    - Overview of available Test OpModes and their intended purpose.
    - Setup instructions for each test (robot position, required hardware state).
    - Step-by-step guide for running each test, including control mappings and expected operator actions.
    - Explanation of telemetry and PASS/FAIL feedback—how to interpret output and what to check.
    - Troubleshooting common failure modes per OpMode.
    - Checklist before competition day to ensure coverage.
    - Emergency stop instructions and warnings.

**2. HOW_TO_TUNE.md**
- **Purpose:** Document procedures and tips for tuning robot subsystems using Tuning OpModes.
- **Required Sections:**
    - Overview of each tunable subsystem and its associated OpMode.
    - Setup for tuning (connection to FTC Dashboard, required sensors).
    - Step-by-step tuning flow for each subsystem (parameters to change, UI controls).
    - Real-time data interpretation (how to read graphs, important telemetry to track).
    - Saving tuned values and rolling back changes.
    - Recommended ranges for parameters and reset process.
    - Troubleshooting tuning issues (instability, sensor misreads).

---

## Action Calling Patterns (Modernization)

### Current Issue

The codebase has mixed patterns:

1. **Test_IntegrationAll** manually calls `action.run(null)` in a loop
2. No unified approach to action queueing in interactive OpModes
3. Doesn't leverage Roadrunner's action scheduler pattern

### Modernized Pattern: ActionScheduler Singleton

Created **`Utilities/ActionScheduler.java`** - A singleton utility that manages the scheduling and execution of Actions without blocking the main loop.

**Key Features:**
- **Singleton pattern**: Use `ActionScheduler.getInstance()` to access
- **`schedule(Action action)`**: Queue a new action
- **`update()`**: Run all active actions and remove completed ones (call once per loop)
- **`update(TelemetryPacket packet)`**: Update actions with an existing telemetry packet
- **Utility methods**: `hasRunningActions()`, `getRunningActionCount()`, `clearActions()`, `sendTelemetry()`

**Usage in Test OpModes:**

```java
// In init()
ActionScheduler scheduler = ActionScheduler.getInstance();

// In loop()
if (gamepad1.a) {
    scheduler.schedule(new SequentialAction(
        new SleepAction(0.5),
        new InstantAction(() -> servo.setPosition(0.5))
    ));
}

// Update all scheduled actions
scheduler.update();
```

This replaces manual `action.run()` loops and provides consistent telemetry integration across all OpModes.

---

## Implementation Phases

### Phase 1: Planning & Documentation

- [x] Document refactoring strategy
- [x] Define test specifications
- [x] Define tuning specifications

### Phase 2: Create Tuning OpModes

- [ ] Tune_Shooter.java
- [ ] Tune_Spindexer.java
- [ ] Tune_ColorDetector.java

### Phase 3: Refactor Test OpModes

- [ ] Test_Intake.java (refactor as atomic test)
- [ ] Test_Shooter.java (refactor as atomic test)
- [ ] Test_Transfer.java (refactor as atomic test)
- [ ] Test_Spindexer.java (refactor as atomic test)
- [ ] Test_ColorDetector.java (refactor as atomic test)
- [ ] Test_IntakeBall.java (new action test)
- [ ] Test_ShootBall.java (new action test)

### Phase 4: Create Comprehensive Test

- [ ] ComprehensiveTest.java (all systems in sequence)
- [ ] Implement action queue pattern
- [ ] Add clear PASS/FAIL reporting

### Phase 5: Documentation & Migration

- [ ] Create HOW_TO_TUNE.md and HOW_TO_TEST.md
- [ ] Remove old Test_IntegrationAll
- [ ] Add safety checks/validations

---

## File Changes Summary

### New Tuning OpModes (Total: 3)

1. `OpModes/Tuning/Tune_Shooter.java`
2. `OpModes/Tuning/Tune_Spindexer.java`
3. `OpModes/Tuning/Tune_ColorDetector.java`

### Modified Files (Total: 6)

1. `OpModes/Tests/Test_Intake.java` (simplify, remove tuning)
2. `OpModes/Tests/Test_Shooter.java` (simplify, remove tuning)
3. `OpModes/Tests/Test_Transfer.java` (simplify, remove tuning)
4. `OpModes/Tests/Test_Spindexer.java` (simplify, remove color storage testing)
5. `OpModes/Tests/Test_ColorDetector.java` (simplify, remove tuning)

### New Files (Total: 3)

1. `OpModes/Tests/Test_IntakeBall.java` (new action test)
2. `OpModes/Tests/Test_ShootBall.java` (new action test)
3. `OpModes/Tests/ComprehensiveTest.java` (new pre-competition test)
4. `OpModes/Tuning/HOW_TO_TUNE.md` (new documentation)
5. `OpModes/Tuning/HOW_TO_TEST.md` (new documentation)

### Deleted Files (Total: 1)

1. `OpModes/Tests/Test_IntegrationAll.java` (superseded by ComprehensiveTest)
2. `OpModes/Tests/TEST_OPMODE_GUIDE.md` (superseded by HOW_TO_TUNE.md and HOW_TO_TEST.md)

---

## Testing Validation Checklist

After implementing refactored OpModes, verify:

### Test OpModes

- [ ] Test_Intake completes in ≤1 minute
- [ ] Test_Shooter completes in ≤1 minute
- [ ] Test_Transfer completes in ≤1 minute
- [ ] Test_Spindexer completes in ≤1 minute
- [ ] Test_ColorDetector completes in ≤1 minute
- [ ] Test_ShootBall completes in ≤1 minute
- [ ] Each test has clear PASS/FAIL indication
- [ ] Each test can run independently

### Comprehensive Test

- [ ] Completes in ≤2 minutes
- [ ] All subsystems validated
- [ ] Clear overall PASS/FAIL
- [ ] Shows which components failed (if any)

### Tuning OpModes

- [ ] Each focuses on single subsystem
- [ ] FTC Dashboard constants update in real-time
- [ ] Values persist across app restart
- [ ] No automatic timeout
- [ ] User can achieve precision tuning

### Action Patterns

- [ ] Actions queue properly in new OpModes
- [ ] Telemetry packets flow correctly
- [ ] No null pointer exceptions from null packets

---

## Migration Strategy

### Step 1: Create Tuning OpModes (low risk)

- No existing code changes
- Can be developed in parallel
- Team can start tuning immediately after deployment

### Step 2: Refactor Unit Tests

- One at a time, in isolation order
- Intake → Shooter → Transfer → Spindexer → ColorDetector
- Each refactored test is independent

### Step 3: Create new tests (IntakeBall, ShootBall, Comprehensive)

- Build on refactored unit tests
- Thoroughly test before deployment

### Step 4: Remove old Test_IntegrationAll

- Only after ComprehensiveTest is approved
- Archive old file in version control

---

## Future Enhancements

### Possible additions (not in scope)

1. **Performance Metrics Dashboard**: Track component timing across test runs
2. **Logging System**: Save test results with timestamps
3. **A/B Comparison**: Compare before/after tuning values
4. **Autonomous Integration**: Export test results to autonomous routines
5. **Automated Regression Testing**: Run all tests via script before competitions

---

## Rationale & Philosophy

### Why separate testing from tuning?

1. **Test OpModes** answer: "Does this work?" (interactive: user)
2. **Tuning OpModes** answer: "How can we make this better?" (continuous: values)
3. Mixing creates confusion and wastes time
4. Competition day uses tests to verify everything works
5. Practice days use tuning to optimize performance

### Why strict time limits on tests?

1. ≤1 minute per unit test = quick diagnostics during competition
2. ≤2 minutes comprehensive = can run in break between matches
3. Tests should be fast enough to repeat if first attempt fails
4. Faster feedback loop for debugging during competition

### Why use action queue pattern?

1. Roadrunner provides this pattern for actions
2. More maintainable than manual state machines
3. Consistent with autonomous OpMode patterns
4. Easier to reason about timing and sequencing
5. Better telemetry integration

### Why keep Tuning OpModes separate?

1. Tuning UI is inherently modal (adjust values, see results)
2. Tests don't need to show every constant
3. Prevents accidental tuning during competition
4. Tuning takes longer (no time constraints)
5. Different use case = different OpMode design

---

**Created:** 2025-11-09  
**Next Review:** After Phase 1 approval
