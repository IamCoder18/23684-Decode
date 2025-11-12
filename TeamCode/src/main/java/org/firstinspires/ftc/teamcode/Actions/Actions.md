# Actions Documentation

Actions are Roadrunner-compatible objects that implement complex behaviors by composing subsystem methods. All actions implement `Action` interface and are scheduled through Roadrunner's action system.

## What is an Action?

```java
public interface Action {
    boolean run(TelemetryPacket packet);
}
```

- Returns `false` when action is complete
- Returns `true` while still running
- Called repeatedly by Roadrunner's scheduler
- Receives `TelemetryPacket` for logging

## BallColor Enum

Enumeration of ball colors that can be detected in the game. Used to track which balls are in each slot of the spindexer.

```java
public enum BallColor {
    GREEN,      // Green ball detected
    PURPLE,     // Purple/Blue ball detected
    UNKNOWN     // Unknown or no ball detected
}
```

**Javadoc:** All enum values are documented with their purpose.

## Complex Actions

### IntakeBall

Full intake sequence for collecting a ball into the next available slot. Performs a multi-state sequence with automatic slot detection and color identification.

**Usage:**
```java
Actions.runBlocking(Spindexer.getInstance().intakeBall());
```

**Behavior:**

1. RUN_INTAKE: Turn on the intake motor
2. MOVE_TO_NEXT_SLOT: Rotate spindexer to the target slot
3. RUN_INTAKE_DOOR: Open the transfer door
4. WAIT_FOR_BALL: Wait for color sensor to detect the ball
5. WAIT_0_2_SECONDS: Hold position while ball settles (0.2 seconds)
6. DONE: Stop intake and door motors

**Features:**

- Automatically detects GREEN or PURPLE balls
- Stores detected color in spindexer
- Finds next available empty slot automatically
- Handles case where all slots are full (stops gracefully)

**Javadoc:** See `IntakeBall` class for complete method documentation, including `findNextFreeSlot()` and helper methods.

**State Machine:**

```
RUN_INTAKE
    ↓
MOVE_TO_NEXT_SLOT (tolerance: 50 ticks)
    ↓
RUN_INTAKE_DOOR
    ↓
WAIT_FOR_BALL (color detection)
    ↓
WAIT_0_2_SECONDS (precise timing with System.nanoTime())
    ↓
DONE
```

---

### ShootBall

Action that aligns and shoots a ball from the spindexer. Determines which slot contains the target ball (or closest ball if no color preference), calculates the rotation needed, and returns true when aligned within tolerance.

**Usage:**
```java
// Shoot any available ball
Actions.runBlocking(new ShootBall());

// Shoot specific color (GREEN or PURPLE)
Actions.runBlocking(new ShootBall(BallColor.GREEN));
```

**Behavior:**

1. Determine which slot contains the target ball (or closest ball if no color preference)
2. Calculate the rotation needed to align the slot with the shooter
3. Command the spindexer to rotate to that position
4. Return true while the spindexer is still moving, false when aligned within tolerance

**Target Slot Selection Priority:**

1. If color requested: find that color within 5° tolerance
2. Find slot within tolerance of current position (no movement needed)
3. Find closest slot

**Features:**

- Shooter alignment offset handled automatically (131.011°)
- Accounts for shortest angular path to target
- Supports color preference for selective shooting
- Optional color filtering for targeted shooting

**Javadoc:** See `ShootBall` class for complete method documentation, including `findTargetSlot()` and helper methods.

**Constants:**

- `SLOT_TOLERANCE_DEGREES` - Position tolerance for slot detection
- `SHOOTER_ALIGNMENT_DEGREES` - Offset to align slot with shooter

---

## Action Composition

Use Roadrunner's action combinators to sequence and parallelize actions:

### Sequential Execution

```java
Actions.runBlocking(
    new SequentialAction(
        Spindexer.getInstance().zero(),           // Must zero first
        Spindexer.getInstance().intakeBall(),     // Intake first ball
        new ShootBall(),                          // Shoot it
        Spindexer.getInstance().intakeBall(),     // Intake second ball
        new ShootBall()                           // Shoot it
    )
);
```

### Parallel Execution

```java
// Run multiple actions simultaneously
Actions.runBlocking(
    new ParallelAction(
        Transfer.getInstance().transferForward(),
        Spindexer.getInstance().toPosition(0.5)
    )
);
```

### Deadlines

```java
// Run background action until timeout completes
Actions.runBlocking(
    new DeadlineAction(
        Spindexer.getInstance().toPosition(0.5),  // Background task
        new WaitAction(2.0)                       // Timeout in seconds
    )
);
```

---

## Subsystem Actions

All action methods are documented with JavaDoc comments. See the source files for complete documentation.

### ColorDetector

**`Action update()`**

- Updates RGB and HSV readings from both color sensors
- Averages values from dual sensors for robustness
- Detects green/purple balls using HSV thresholds
- Returns immediately (true)
- Outputs telemetry: RGB values, HSV, detection flags
- **Javadoc:** Complete documentation in `ColorDetector.java`

### Transfer

**`Action transferForward()`** - Move balls forward (returns immediately)
- **Javadoc:** Documented in source file

**`Action transferBackward()`** - Move balls backward (returns immediately)
- **Javadoc:** Documented in source file

**`Action transferStop()`** - Stop transfer (returns immediately)
- **Javadoc:** Documented in source file

**`Action intakeDoorForward()`** - Open intake door (returns immediately)
- **Javadoc:** Documented in source file

**`Action intakeDoorBackward()`** - Close intake door (returns immediately)
- **Javadoc:** Documented in source file

**`Action intakeDoorStop()`** - Stop intake door (returns immediately)
- **Javadoc:** Documented in source file

### Intake

**`Action in()`** - Spin intake motor inward (returns immediately)
- Sets intake motor to forward power
- **Javadoc:** Documented in source file

**`Action out()`** - Spin intake motor outward (returns immediately)
- Sets intake motor to reverse power
- **Javadoc:** Documented in source file

**`Action stop()`** - Stop intake motor (returns immediately)
- Sets intake motor to zero power
- **Javadoc:** Documented in source file

### Shooter

**`Action run()`** - Start both shooter motors at full power (returns immediately)
- Applies configured offset adjustments for speed compensation
- **Javadoc:** Documented in source file

**`Action stop()`** - Stop both shooter motors (returns immediately)
- **Javadoc:** Documented in source file

### Spindexer

**`Action zero()`** - Calibrate to zero position using touch sensor

- Returns true when calibrated
- Must be called once before any position control
- Example: `Actions.runBlocking(Spindexer.getInstance().zero());`

**`Action toPosition(double revolutions)`** - Move to specific rotation

- `0.0` = 0° (slot 0)
- `0.333...` = 120° (slot 1)
- `0.666...` = 240° (slot 2)
- Returns true when within 50 ticks of target

**`Action intakeBall()`** - Complex sequence (see IntakeBall section)

---

## Important Patterns

### Every Loop Update

```java
@Override
public void loop() {
    SubsystemUpdater.update();  // CRITICAL - runs PID and periodic tasks
    // Your OpMode code here
}
```

Without this, spindexer cannot hold position and other periodic updates fail.

### Blocking vs Non-Blocking

- **Blocking Actions** return `true` when complete, allowing sequential chaining
  - `zero()`, `intakeBall()`, `toPosition()`, `ShootBall`, `IntakeBall`
- **Non-Blocking Actions** return `true` immediately
  - Simple subsystem actions like `in()`, `out()`, `run()`, `stop()`

### Custom Action Implementation

```java
public class MyCustomAction implements Action {
    private State currentState = State.START;
    
    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        packet.put("State", currentState.toString());
        
        switch (currentState) {
            case START:
                // Do something
                currentState = State.RUNNING;
                return false;  // Still running
                
            case RUNNING:
                if (conditionMet()) {
                    currentState = State.DONE;
                }
                return false;  // Still running
                
            case DONE:
                return true;  // Complete
        }
        return false;
    }
    
    private enum State { START, RUNNING, DONE }
}
```

---

## Telemetry

All actions can output telemetry visible in FTC Dashboard:

```java
packet.put("Key", value);
packet.put("Position", 123.45);
packet.put("Error", -2.5);
packet.put("State", "RUNNING");
```

---

## Architecture Notes

- **Actions are separate from Subsystems** - actions compose subsystem methods
- **Stateful** - action implementations maintain state machine for behavior
- **Precise Timing** - `System.nanoTime()` used for sub-millisecond precision
- **PID Control** - spindexer position control runs in background via `SubsystemUpdater`
