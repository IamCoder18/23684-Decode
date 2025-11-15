# HOW TO TEST - Robot Subsystem Testing Guide

## Overview

This guide explains how to test robot subsystems using the dedicated Test OpModes. Testing is for *
*validation** - verifying that subsystems work correctly through hands-on interaction. Use testing
to diagnose issues, verify functionality, and ensure systems are ready for competition.

## Testing Philosophy

**Tests are not for validation or automation.**  
Test OpModes allow a human operator to experiment and visually inspect subsystems:

- Interactive testing with immediate visual feedback
- No automatic pass/fail - operator judgment guides results
- Quick diagnostics during competition setup
- Hands-on exploration of subsystem behavior

## Available Test OpModes

### Unit Tests (≤1 minute each)

#### 1. Test_Intake

**Purpose:** Test intake motor forward, reverse, and stop operations
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_Intake.java`

**Setup:**

- Ensure ball intake area is clear
- Have test balls available if needed

**Controls:**

- **A button:** Start intake motor IN (forward)
- **B button:** Start intake motor OUT (reverse)
- **X button:** Stop intake motor

**What to Test:**

1. Press A - verify motor spins forward smoothly
2. Press B - verify motor spins in reverse
3. Press X - verify motor stops immediately
4. Listen for unusual noises or vibrations
5. Check that motor responds to all commands

**Expected Results:**

- ✓ Motor responds to all buttons
- ✓ Direction changes are correct
- ✓ Stop function works immediately
- ✓ No grinding, stalling, or unusual sounds

---

#### 2. Test_Shooter

**Purpose:** Test both shooter motors for consistent operation
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_Shooter.java`

**Setup:**

- Ensure shooter area is clear
- Remove any balls from shooter vicinity

**Controls:**

- **A button:** Start both shooter motors
- **B button:** Stop both shooter motors

**What to Test:**

1. Press A - verify both motors start together
2. Listen for synchronization - both motors should sound similar
3. Check for vibration or wobble
4. Press B - verify both motors stop immediately
5. Verify motor powers are displayed in telemetry

**Expected Results:**

- ✓ Both motors start simultaneously
- ✓ No excessive vibration or noise
- ✓ Motor powers are synchronized (<5% difference)
- ✓ Stop function works for both motors

---

#### 3. Test_Transfer

**Purpose:** Test transfer belt and intake door servos
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_Transfer.java`

**Setup:**

- Ensure transfer path is clear
- Check that servo linkages move freely

**Controls - Transfer Belt:**

- **A button:** Transfer Forward (move balls intake→storage)
- **B button:** Transfer Backward (reverse motion)
- **X button:** Stop transfer belt

**Controls - Intake Door:**

- **Y button:** Intake Door Forward (open door)
- **Right Bumper:** Intake Door Backward (close door)
- **Left Bumper:** Stop intake door

**What to Test:**

1. Test transfer belt in both directions
2. Verify door opens and closes smoothly
3. Check servo synchronization
4. Ensure all stop commands work immediately
5. Listen for servo noise or binding

**Expected Results:**

- ✓ Transfer belt moves smoothly in both directions
- ✓ Door opens and closes fully
- ✓ No grinding or binding in servos
- ✓ All stop commands work immediately

---

#### 4. Test_Spindexer

**Purpose:** Test spindexer zeroing and position control
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_Spindexer.java`

**Setup:**

- **REQUIRED:** Spindexer must be zeroed first (press A)
- Clear any balls from spindexer slots
- Ensure touch sensor is accessible

**Controls:**

- **A button:** Start Zero Sequence (REQUIRED FIRST)
- **B button:** Move to Position 0 (0°)
- **X button:** Move to Position 1 (120°)
- **Y button:** Move to Position 2 (240°)

**What to Test:**

1. **ALWAYS start with zero sequence** (press A)
2. Wait for "Zero Sequence Complete" message
3. Test position movements (B, X, Y buttons)
4. Verify smooth, accurate positioning
5. Check that position error is <50 ticks

**Expected Results:**

- ✓ Zero sequence completes successfully
- ✓ Spindexer moves to each position smoothly
- ✓ Position accuracy within tolerance
- ✓ No oscillation or overshoot

**⚠ IMPORTANT:**

- MUST zero spindexer before testing positions
- If zero sequence fails, check touch sensor

---

#### 5. Test_ColorDetector

**Purpose:** Test color sensor detection accuracy
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_ColorDetector.java`

**Setup:**

- Clean both color sensors
- Place test balls in front of sensors
- Ensure good lighting conditions

**Controls:**

- **A button:** Pause/unpause sensor readings
- **B button:** Print detailed sensor analysis

**What to Test:**

1. Observe real-time RGB and HSV values
2. Test with green ball - should detect as GREEN
3. Test with purple ball - should detect as PURPLE
4. Test with no ball - should show UNKNOWN
5. Press B for detailed analysis when needed

**Expected Results:**

- ✓ Sensors read meaningful RGB values
- ✓ Green balls detected as GREEN
- ✓ Purple balls detected as PURPLE
- ✓ No ball shows UNKNOWN
- ✓ Readings are stable (not flickering)

---

#### 6. Test_DistanceDetector

**Purpose:** Test distance sensor detection accuracy
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_DistanceDetector.java`

**Setup:**

- Ensure distance sensor is clean and unobstructed
- Have objects available at various distances for testing
- Test under expected lighting conditions

**Controls:**

- **A button:** Pause/unpause sensor readings
- **B button:** Print detailed distance analysis

**What to Test:**

1. Observe real-time distance readings in centimeters
2. Move objects at various distances to verify range
3. Test near threshold (2 cm) - should trigger isObject
4. Test far from threshold (>5 cm) - should not trigger
5. Press B for detailed analysis when needed

**Expected Results:**

- ✓ Sensor reads meaningful distance values
- ✓ Objects within 2 cm detected as OBJECT
- ✓ Objects beyond threshold show NO OBJECT
- ✓ Readings are stable (not flickering)
- ✓ Detection accuracy at expected distances

---

#### 7. Test_TouchDetector

**Purpose:** Test touch sensor contact detection
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_TouchDetector.java`

**Setup:**

- Ensure both touch sensors are properly mounted
- Verify physical access to both sensors
- Have test objects available to press sensors

**Controls:**

- **A button:** Pause/unpause sensor readings
- **B button:** Print detailed sensor analysis

**What to Test:**

1. Observe individual left and right sensor states
2. Press left sensor - should show "PRESSED ✓"
3. Press right sensor - should show "PRESSED ✓"
4. Press both sensors together - should show both active
5. Release and watch states return to "RELEASED"
6. Press B for detailed analysis of sensor combinations

**Expected Results:**

- ✓ Sensors read meaningful boolean states
- ✓ Left sensor detects independent contact
- ✓ Right sensor detects independent contact
- ✓ Combined detected field is true when either is pressed
- ✓ Response is immediate to contact/release
- ✓ Readings are stable (no flickering)

---

### Action Tests (≤1 minute each)

#### 8. Test_IntakeBall

**Purpose:** Test complete IntakeBall action sequence
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_IntakeBall.java`

**Setup:**

- **REQUIRED:** Spindexer must be zeroed first
- Place ball in intake position
- Ensure color detector is calibrated

**Controls:**

- **A button:** Start IntakeBall action
- **B button:** Stop all actions
- **X button:** Print detailed status

**What to Test:**

1. Ensure spindexer is zeroed (use Test_Spindexer first)
2. Press A to run complete intake sequence
3. Watch action progress in telemetry
4. Verify ball color is detected and stored
5. Check that action completes successfully

**Expected Results:**

- ✓ Action sequence runs without errors
- ✓ Spindexer moves to correct position
- ✓ Ball color is detected and stored
- ✓ Action completes within reasonable time

---

#### 9. Test_ShootBall

**Purpose:** Test ShootBall action sequence
**Duration:** ≤1 minute
**Location:** `OpModes/Tests/Test_ShootBall.java`

**Setup:**

- Load balls in spindexer slots first
- Spindexer should be positioned properly
- Ensure shooter motors are ready

**Controls:**

- **A button:** Align spindexer (no shooting)
- **B button:** Full sequence (align + shoot simulation)
- **X button:** Stop all actions
- **Y button:** Print detailed status

**What to Test:**

1. Press A to test alignment only
2. Watch spindexer move to target position
3. Verify alignment accuracy in telemetry
4. Check ball selection logic

**Expected Results:**

- ✓ Spindexer aligns to target position
- ✓ Alignment error <5 degrees
- ✓ Correct ball selection logic
- ✓ Smooth movement without oscillation

---

### Comprehensive Test (≤2 minutes)

#### 10. ComprehensiveTest

**Purpose:** Automated pre-competition system verification
**Duration:** ≤2 minutes
**Location:** `OpModes/Tests/ComprehensiveTest.java`

**Setup:**

- **REQUIRED:** Spindexer must be zeroed
- Ensure robot is in safe testing position
- Have test balls available for action tests

**Controls:**

- **A button:** START (single button starts all tests)
- **Y button:** EMERGENCY STOP (available anytime)

**Test Sequence:**

1. **Phase 1:** Subsystem checks (3s)
    - Intake: Spin IN/OUT
    - Shooter: Spin up
    - Transfer: Move belt and door

2. **Phase 2:** Spindexer validation (2.5s)
    - Zero sequence
    - Position movements

3. **Phase 3:** Color detection (1.5s)
    - Baseline reading
    - Color detection test

4. **Phase 4:** Action sequence (4.5s)
    - IntakeBall action
    - ShootBall action

5. **Phase 5:** Summary (0.5s)
    - PASS/FAIL report

**What to Watch:**

- All phases complete successfully
- No emergency stops required
- All tests show ✓ PASS status
- Overall status shows "READY FOR COMPETITION"

**Expected Results:**

- ✓ All 6 component tests pass
- ✓ No errors or timeouts
- ✓ Overall status: "✓ READY FOR COMPETITION"

---

## Testing Best Practices

### Before Testing

1. **Safety First:** Clear testing area of people and obstacles
2. **Hardware Check:** Verify all connections are secure
3. **Spindexer Setup:** Zero spindexer before any position testing
4. **Sensor Calibration:** Ensure color detector is calibrated
5. **Documentation:** Note any issues for later investigation

### During Testing

1. **One Test at a Time:** Focus on one subsystem per test
2. **Listen Carefully:** Unusual noises indicate problems
3. **Watch Telemetry:** Real-time feedback shows system status
4. **Document Results:** Note pass/fail for each component
5. **Emergency Stop:** Use Y button if anything goes wrong

### After Testing

1. **Stop All Motors:** Ensure everything is stopped
2. **Review Results:** Check which tests passed/failed
3. **Plan Next Steps:** Address any failed tests
4. **Update Status:** Document robot readiness

### Competition Day Testing

1. **Run ComprehensiveTest first** (≤2 minutes)
2. **Address any failures** before competition
3. **Run individual tests** if issues found
4. **Final verification** before match

---

## Interpreting Test Results

### Pass Indicators

- ✓ **OPERATIONAL** - Subsystem responds correctly
- ✓ **VERIFIED** - Function works as expected
- ✓ **FUNCTIONAL** - All features working
- ✓ **READY** - System ready for use

### Warning Indicators

- ⚠ **CLOSE** - Near tolerance limits, monitor closely
- ⚠ **OFF** - Slightly out of spec, may need attention
- ⚠ **NO MATCH** - Color not detected, check sensors
- ⚠ **RUNNING** - Test in progress

### Failure Indicators

- ✗ **ERROR** - System not responding correctly
- ✗ **INVALID** - Configuration or range error
- ✗ **FAILED** - Test did not complete successfully
- **No response** - Hardware or connection issue

### Telemetry Reading

- **Position values** should be smooth and consistent
- **Power values** should match expected ranges
- **Error values** should be within tolerance
- **Status messages** indicate current operation

---

## Troubleshooting Failed Tests

### Common Issues and Solutions

#### Test_Intake Failures

- **Motor doesn't respond:** Check motor connections
- **Wrong direction:** Verify motor wiring
- **Noisy operation:** Check for mechanical binding

#### Test_Shooter Failures

- **Motors out of sync:** Use Tune_Shooter to adjust offsets
- **Excessive vibration:** Check motor mounting
- **One motor not working:** Check individual motor connections

#### Test_Transfer Failures

- **Servos not moving:** Check servo connections and power
- **Jerky movement:** Lubricate mechanical linkages
- **Wrong direction:** May need servo direction adjustment

#### Test_Spindexer Failures

- **Won't zero:** Check touch sensor connection
- **Position errors:** Use Tune_Spindexer to adjust PID
- **Oscillation:** Reduce P coefficient, increase D

#### Test_ColorDetector Failures

- **No detection:** Check sensor connections and lighting
- **Wrong colors:** Use Tune_ColorDetector to adjust thresholds
- **Unstable readings:** Improve lighting conditions

#### Test_DistanceDetector Failures

- **No distance reading:** Check sensor connection and power
- **Inaccurate measurements:** Use Tune_DistanceDetector to adjust threshold
- **Unstable readings:** Ensure sensor is clean and unobstructed
- **Object not detected:** Verify OBJECT_THRESHOLD_CM is appropriate for your use case

#### Test_TouchDetector Failures

- **No response:** Check sensor connections and power
- **One sensor not working:** Verify individual sensor connection
- **Detected always true:** Check for stuck or pressed sensor
- **Detected always false:** Verify both sensors are properly wired
- **Unstable readings:** Check for loose connections or corrosion

#### Action Test Failures

- **Actions don't start:** Check spindexer is zeroed
- **Sequence stops early:** Check for mechanical jams
- **Color not detected:** Verify color detector calibration

### Emergency Procedures

1. **Press Y** in any test to emergency stop
2. **Power cycle** robot if systems are unresponsive
3. **Check connections** if multiple tests fail
4. **Document issues** for later repair

---

## Pre-Competition Checklist

### Required Tests

- [ ] **ComprehensiveTest** - All systems (≤2 min)
- [ ] **Test_Spindexer** - Zero sequence (if ComprehensiveTest fails)
- [ ] **Test_ColorDetector** - Color detection
- [ ] **Test_Intake** - Intake functionality
- [ ] **Test_Shooter** - Shooter operation

### Before Each Match

1. Run ComprehensiveTest
2. Address any failures
3. Verify spindexer zero
4. Check color detection
5. Test critical functions

### Documentation

- Record any failed tests
- Note hardware issues
- Document tuned values used
- Update team on robot status

---

**Created:** 2025-11-09  
**Last Updated:** 2025-11-09  
**Version:** 1.0