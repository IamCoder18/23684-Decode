# Subsystems Documentation

All subsystems are implemented as singletons and must be initialized before use.

## Initialization and Shutdown

### In Your OpMode

```java
@TeleOp
public class MyOpMode extends OpMode {
    @Override
    public void init() {
        HardwareInitializer.initialize(hardwareMap);
    }
    
    @Override
    public void loop() {
        SubsystemUpdater.update();  // Must call update() every loop
        
        // Your code here
    }
    
    @Override
    public void stop() {
        HardwareShutdown.shutdown();
    }
}
```

## Subsystems

### ColorDetector

Reads RGB color values and converts to HSV for color detection (Green/Purple).

**Access:** `ColorDetector.getInstance()`

**Key Methods:**

- `Action update()` - Updates color readings and telemetry
- `isGreen` - Public boolean field
- `isPurple` - Public boolean field
- `avgRed, avgGreen, avgBlue` - RGB values
- `avgHSV` - HSV array [H, S, V]

**Configuration:**

- `GREEN_HUE_MIN`, `GREEN_HUE_MAX` - Green hue range
- `PURPLE_HUE_MIN`, `PURPLE_HUE_MAX` - Purple hue range
- `MIN_SATURATION` - Minimum saturation threshold

### Transfer

Controls the transfer and intake door servos.

**Access:** `Transfer.getInstance()`

**Key Methods:**

- `Action transferForward()` - Move balls forward
- `Action transferBackward()` - Move balls backward
- `Action transferStop()` - Stop transfer
- `Action intakeDoorForward()` - Open intake door
- `Action intakeDoorBackward()` - Close intake door
- `Action intakeDoorStop()` - Stop intake door

**Configuration:**

- `FORWARD_POWER` (default: 1.0)
- `BACKWARD_POWER` (default: -1.0)
- `STOP_POWER` (default: 0.0)

### Shooter

Controls the upper and lower shooter motors.

**Access:** `Shooter.getInstance()`

**Key Methods:**

- `Action run()` - Start both shooters at full power
- `Action stop()` - Stop both shooters

**Configuration:**

- `RUN_POWER` (default: 1.0)
- `STOP_POWER` (default: 0.0)
- `UPPER_OFFSET` (default: 0.0) - Compensation for speed differences
- `LOWER_OFFSET` (default: 0.0) - Compensation for speed differences

### Intake

Controls the intake motor.

**Access:** `Intake.getInstance()`

**Key Methods:**

- `Action in()` - Spin intake motor inward
- `Action out()` - Spin intake motor outward
- `Action stop()` - Stop intake motor

**Configuration:**

- `IN_POWER` (default: 1.0)
- `OUT_POWER` (default: -1.0)
- `STOP_POWER` (default: 0.0)

### Spindexer

Controls the spindexer (carousel) with 3 ball slots. Requires PID tuning and encoder zeroing.

**Access:** `Spindexer.getInstance()`

**Critical Setup:**

- Must call `SubsystemUpdater.update()` **every loop** - this runs the PID controller
- **IMPORTANT:** On the first run of any OpMode, manually schedule `Spindexer.getInstance().zero()`
  action to calibrate the encoder. This must be done before using any other position control
  actions.

**Key Methods:**

- `Action zero()` - Calibrate encoder to zero position (BLOCKING)
- `Action toPosition(double revolutions)` - Move to specific position
- `Action toZero()` - Return to zero position
- `Action toHalf()` - Move to half rotation
- `Action intakeBall()` - Full intake sequence (requires ElapsedTime)
- `void update()` - **MUST be called every loop** to run PID controller
- `void setTargetPosition(double revolutions)` - Set target for PID
- `double getCurrentPositionTicks()` - Read current position
- `BallColor getBallColor(int slotIndex)` - Get detected color at slot
- `void setBallColor(int slotIndex, BallColor color)` - Store detected color

**Configuration:**

- `TICKS_PER_REV` (8192.0) - Encoder ticks per revolution
- `P`, `I`, `D`, `F` - PID coefficients (tune via FTC Dashboard)

**Slot Layout:**

- Slot 0: 0° (centered)
- Slot 1: 120° (centered)
- Slot 2: 240° (centered)

**Example Usage:**

```java
// In init()
HardwareInitializer.initialize(hardwareMap);

// In loop()
SubsystemUpdater.update();  // CRITICAL: Update PID controller and other periodic tasks

// On first OpMode run (autonomous or teleop start)
Spindexer spindexer = Spindexer.getInstance();
Roadrunner.Actions.runBlocking(spindexer.zero());  // MUST zero before using

// After zeroing, in your action sequences
Roadrunner.Actions.runBlocking(spindexer.intakeBall());
Roadrunner.Actions.runBlocking(spindexer.toPosition(0.5));
Roadrunner.Actions.runBlocking(spindexer.toPosition(0));
```

## Notes

- All subsystems are **singletons** - use `getInstance()` to access them
- **SubsystemUpdater.update()** must be called every loop for periodic updates (PID control, etc.)
- Timing is handled internally using System.nanoTime() for precision
- Single-threaded operation is assumed - FTC does not support multi-threaded OpModes
- Call `HardwareInitializer.initialize()` in init() and `HardwareShutdown.shutdown()` in stop()
