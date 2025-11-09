# Actions Documentation

Actions are Roadrunner-compatible objects that implement complex behaviors by composing subsystem methods. All actions implement `Action` interface and are scheduled through Roadrunner's action system.

## What is an Action?

```java
public interface Action {
    boolean run(TelemetryPacket packet);
}
```

- Returns `true` when action is complete
- Returns `false` while still running
- Called repeatedly by Roadrunner's scheduler
- Receives `TelemetryPacket` for logging

## BallColor Enum

Tracks detected ball colors in the spindexer:

```java
public enum BallColor {
    GREEN,      // Green ball detected
    PURPLE,     // Purple ball detected
    UNKNOWN     // No ball or color not detected
}
```

## Complex Actions

### IntakeBall

Full intake sequence for collecting a ball into the next available slot.

**Usage:**
```java
Actions.runBlocking(Spindexer.getInstance().intakeBall());
```

**Behavior:**

1. Run intake motor forward
2. Rotate spindexer to next free slot
3. Open intake door
4. Wait for ball (color detection)
5. Wait 0.2 seconds for ball to settle
6. Stop intake and door

**Features:**

- Automatically detects GREEN or PURPLE balls
- Stores detected color in spindexer
- Finds next available empty slot
- Handles case where all slots are full (stops gracefully)

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

Aligns a ball with the shooter and returns when aligned.

**Usage:**
```java
// Shoot any available ball
Actions.runBlocking(new ShootBall());

// Shoot specific color (GREEN or PURPLE)
Actions.runBlocking(new ShootBall(BallColor.GREEN));
```

**Behavior:**

1. Find best slot to shoot (color preference or closest)
2. Set spindexer target position to align with shooter
3. Return true when position reached (within 50 ticks)

**Target Slot Selection Priority:**

1. If color requested: find that color within 5° tolerance
2. Find slot within tolerance of current position (no movement needed)
3. Find closest slot

**Features:**

- Shooter alignment offset handled automatically (131.011°)
- Accounts for shortest angular path to target
- Supports color preference for selective shooting

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

### ColorDetector

**`Action update()`**

- Updates RGB and HSV readings from both sensors
- Detects green/purple balls
- Returns immediately (true)
- Outputs telemetry: RGB values, HSV, detection flags

### Transfer

**`Action transferForward()`** - Move balls forward (returns immediately)

**`Action transferBackward()`** - Move balls backward (returns immediately)

**`Action transferStop()`** - Stop transfer (returns immediately)

**`Action intakeDoorForward()`** - Open intake door (returns immediately)

**`Action intakeDoorBackward()`** - Close intake door (returns immediately)

**`Action intakeDoorStop()`** - Stop intake door (returns immediately)

### Intake

**`Action in()`** - Spin intake inward (returns immediately)

**`Action out()`** - Spin intake outward (returns immediately)

**`Action stop()`** - Stop intake motor (returns immediately)

### Shooter

**`Action run()`** - Start shooters at full power (returns immediately)

**`Action stop()`** - Stop shooters (returns immediately)

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
