# Subsystems Documentation

All subsystems are implemented as singletons and manage robot hardware. They are initialized through `HardwareInitializer` and updated through `SubsystemUpdater`.

## Lifecycle Management

### Initialization

```java
// In OpMode.init()
HardwareInitializer.initialize(hardwareMap);
```

### Updates

```java
// In OpMode.loop()
SubsystemUpdater.update();  // CRITICAL for periodic tasks (PID, etc.)
```

### Shutdown

```java
// In OpMode.stop()
HardwareShutdown.shutdown();
```

## Subsystems Overview

### ColorDetector

Singleton subsystem that manages dual color sensors for ball color detection. Uses HSV color space to detect green and purple balls with tunable thresholds.

**Access:** `ColorDetector.getInstance()`

**Methods:**

- `Action update()` - Updates color readings from both sensors and outputs telemetry (finishes instantly)
  - Reads RGB values from both sensors and averages them
  - Converts to HSV color space
  - Detects green/purple based on hue and saturation thresholds
  - All methods documented with JavaDoc

**Public Fields:**

- `boolean isGreen` - True if current color is detected as green
- `boolean isPurple` - True if current color is detected as purple
- `int avgRed, avgGreen, avgBlue` - Average RGB values from both sensors (0-255)
- `float[] avgHSV` - HSV conversion [Hue (0-360), Saturation (0-1), Value (0-1)]

**Tunable Constants (FTC Dashboard):**

- `GREEN_HUE_MIN`, `GREEN_HUE_MAX` - Green hue range (default: 100-140)
- `PURPLE_HUE_MIN`, `PURPLE_HUE_MAX` - Purple hue range (default: 250-290)
- `MIN_SATURATION` - Minimum saturation threshold for valid color detection (default: 0.4)

---

### Transfer

Controls transfer servos (moves balls) and intake door servos. All action methods are documented with JavaDoc.

**Access:** `Transfer.getInstance()`

**Methods:**

- `Action transferForward()` - Moves transfer servos forward (finishes instantly)
- `Action transferBackward()` - Moves transfer servos backward (finishes instantly)
- `Action transferStop()` - Stops transfer servos (finishes instantly)
- `Action intakeDoorForward()` - Opens intake door (finishes instantly)
- `Action intakeDoorBackward()` - Closes intake door (finishes instantly)
- `Action intakeDoorStop()` - Stops intake door (finishes instantly)

All methods return InstantActions that complete immediately after setting servo power.

**Tunable Constants (FTC Dashboard):**

- `FORWARD_POWER` - Transfer forward power level
- `BACKWARD_POWER` - Transfer backward power level
- `STOP_POWER` - Servo power when stopped (default: 0.0)

---

### Intake

Controls the intake motor for ball collection. All action methods are documented with JavaDoc.

**Access:** `Intake.getInstance()`

**Methods:**

- `Action in()` - Runs intake motor forward (finishes instantly)
- `Action out()` - Runs intake motor backward (finishes instantly)
- `Action stop()` - Stops intake motor (finishes instantly)

All methods return InstantActions that complete immediately after setting motor power.

**Tunable Constants (FTC Dashboard):**

- `IN_POWER` - Intake forward power level (default: 1.0)
- `OUT_POWER` - Intake backward power level (default: -1.0)
- `STOP_POWER` - Motor power when stopped (default: 0.0)

---

### Shooter

Controls upper and lower shooter motors for shooting balls. All action methods are documented with JavaDoc.

**Access:** `Shooter.getInstance()`

**Methods:**

- `Action run()` - Starts both shooter motors at full power with configured offsets (finishes instantly)
  - Applies UPPER_OFFSET and LOWER_OFFSET for speed compensation
- `Action stop()` - Stops both shooter motors (finishes instantly)

All methods return InstantActions that complete immediately after setting motor power.

**Tunable Constants (FTC Dashboard):**

- `RUN_POWER` - Shooter motor power level (default: 1.0)
- `UPPER_OFFSET` - Speed compensation for upper motor (default: 0.0)
- `LOWER_OFFSET` - Speed compensation for lower motor (default: 0.0)
- `STOP_POWER` - Motor power when stopped (default: 0.0)

---

### Spindexer

Controls the carousel/spindexer with 3 ball slots. Provides PID-based position control and ball color tracking.

**Access:** `Spindexer.getInstance()`

**Critical Setup:**

1. Must call `SubsystemUpdater.update()` every loop (runs PID controller)
2. First OpMode run: manually schedule `Spindexer.getInstance().zero()` to calibrate

**Methods:**

- `Action zero()` - Calibrate encoder to zero position using touch sensor (BLOCKING)
- `Action toPosition(double revolutions)` - Move to specific position (BLOCKING)
- `Action intakeBall()` - Full intake sequence (BLOCKING) - implemented in Actions folder
- `void setTargetPosition(double revolutions)` - Set target for PID controller
- `double getCurrentPositionTicks()` - Read current position in encoder ticks
- `BallColor getBallColor(int slotIndex)` - Get detected color at slot (0, 1, or 2)
- `void setBallColor(int slotIndex, BallColor color)` - Store detected color

**Slot Layout:**

- Slot 0: 0° (centered)
- Slot 1: 120° (centered)
- Slot 2: 240° (centered)

**Tunable Constants (FTC Dashboard):**

- `TICKS_PER_REV` - Encoder ticks per revolution (8192 for through-bore)
- `zeroOffset` - Adjustment for magnetic limit switch trigger offset
- `P`, `I`, `D`, `F` - PID coefficients for position control

**Example Usage:**

```java
// In init()
HardwareInitializer.initialize(hardwareMap);

// First time only - calibrate the spindexer
// (do this once at the beginning of your first OpMode)
Spindexer spindexer = Spindexer.getInstance();
Actions.runBlocking(spindexer.zero());

// In loop()
SubsystemUpdater.update();  // CRITICAL for PID

// Usage in action sequences
Actions.runBlocking(spindexer.intakeBall());
Actions.runBlocking(spindexer.toPosition(0.25));  // 90 degrees
Actions.runBlocking(spindexer.toPosition(0));     // Back to zero
```

---

## Important Notes

- **All subsystems are singletons** - use `getInstance()` to access them
- **SubsystemUpdater.update() must be called every loop** for periodic updates and PID control
- **Single-threaded operation** - FTC does not support multi-threaded OpModes
- **Actions return immediately unless specified as BLOCKING**
- **Timing** is handled internally using `System.nanoTime()` for precision
- **All public methods have JavaDoc documentation** - see source files for complete details
- **Configuration tuning** - all constants marked with `@Config` can be tuned via FTC Dashboard

## Architecture

Subsystems are separated from Actions:

- **Subsystems** (this folder) - Hardware control and state management
- **Actions** (separate folder) - Complex action sequences and behaviors

This separation allows subsystems to be used independently or combined through Actions.
