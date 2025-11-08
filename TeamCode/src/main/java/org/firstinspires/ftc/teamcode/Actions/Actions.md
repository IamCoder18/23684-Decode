# Actions Documentation

Actions are Roadrunner-compatible objects that represent single behaviors. They implement the `Action` interface and can be sequenced, parallelized, and controlled through Roadrunner's action scheduler.

## What is an Action?

An `Action` has a single method:
```java
boolean run(TelemetryPacket packet)
```

- Returns `true` when the action is complete
- Returns `false` while the action is still running
- Called repeatedly by Roadrunner's scheduler
- Receives a `TelemetryPacket` for logging telemetry

## Subsystem Actions

### ColorDetector

**`Action update()`**
- Updates color sensor readings and outputs telemetry
- Finishes instantly (returns true)
- Updates: `isGreen`, `isPurple`, `avgRed`, `avgGreen`, `avgBlue`, `avgHSV`

### Transfer

**`Action transferForward()`**
- Spins transfer servos forward (power = FORWARD_POWER)
- Finishes instantly

**`Action transferBackward()`**
- Spins transfer servos backward (power = BACKWARD_POWER)
- Finishes instantly

**`Action transferStop()`**
- Stops transfer servos
- Finishes instantly

**`Action intakeDoorForward()`**
- Opens intake door servos
- Finishes instantly

**`Action intakeDoorBackward()`**
- Closes intake door servos
- Finishes instantly

**`Action intakeDoorStop()`**
- Stops intake door servos
- Finishes instantly

### Shooter

**`Action run()`**
- Runs both shooter motors at RUN_POWER + offsets
- Finishes instantly
- Does NOT handle shooting sequencing - shooter must spin before balls can be fired

**`Action stop()`**
- Stops both shooter motors
- Finishes instantly

### Intake

**`Action in()`**
- Spins intake motor inward at IN_POWER
- Finishes instantly

**`Action out()`**
- Spins intake motor outward at OUT_POWER
- Finishes instantly

**`Action stop()`**
- Stops intake motor
- Finishes instantly

### Spindexer

**`Action zero()`**
- Calibrates the spindexer to zero position using touch sensor
- **BLOCKING** - returns true when zeroed
- **MUST be called manually on the first OpMode run before any position control actions**
- State machine: START → MOVE_OFF_SENSOR → FAST_TOWARDS_SENSOR → BACK_OFF → SLOW_TOWARDS_SENSOR → DONE
- Example: `Actions.runBlocking(Spindexer.getInstance().zero());`

**`Action toPosition(double revolutions)`**
- Moves to specified position (in revolutions from zero)
- **BLOCKING** - returns true when position reached (within 50 ticks)
- Runs PID controller in background
- Example: `toPosition(0.5)` moves to 180°

**`Action intakeBall()`**
- Full intake sequence:
  1. Run intake motor
  2. Rotate to next free slot
  3. Open intake door
  4. Wait for ball (detects color)
  5. Wait 0.2 seconds
  6. Stop intake and door
- **BLOCKING** - returns true when complete
- Uses System.nanoTime() for precision timing
- Automatically detects GREEN or PURPLE balls and stores color in spindexer

## Action Sequencing

Use Roadrunner's `Actions` class to sequence actions:

```java
import com.acmerobotics.roadrunner.Actions;

// Sequential execution
Actions.runBlocking(
    new SequentialAction(
        spindexer.zero(),
        spindexer.intakeBall(),
        spindexer.toPosition(0.25),
        shooter.run()
    )
);

// Parallel execution
Actions.runBlocking(
    new ParallelAction(
        transfer.transferForward(),
        spindexer.toPosition(0.5)
    )
);

// Deadlines (run action until another completes)
Actions.runBlocking(
    new DeadlineAction(
        spindexer.moveToPosition(0.5),  // Background task
        new WaitAction(2.0)  // Timeout
    )
);
```

## Important Notes

### SubsystemUpdater.update() Must Be Called Every Loop

```java
@Override
public void loop() {
    SubsystemUpdater.update();  // Critical for PID control and other periodic tasks
    
    // Your code here
}
```

Without this, the spindexer cannot hold its position. The `update()` method runs the PID controller.

### Action Composition

Create custom composite actions by implementing the Action interface:

```java
public class CustomSequence implements Action {
    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        // Your state machine logic here
        // Return true when done, false while running
        return false;
    }
}
```

### Telemetry

All actions can output telemetry via the `TelemetryPacket`:

```java
packet.put("Key", value);
packet.put("Position", currentPosition);
packet.put("Error", error);
```

This telemetry is visible in the FTC Dashboard.

## Roadrunner Integration

See `Subsystems.md` for initialization and basic usage examples.

The subsystem actions are designed to work seamlessly with Roadrunner's action scheduler, allowing you to create complex autonomous routines with clean, readable code.
