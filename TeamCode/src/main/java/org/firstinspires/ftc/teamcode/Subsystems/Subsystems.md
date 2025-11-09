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

Reads RGB color values from dual color sensors and detects green/purple balls.

**Access:** `ColorDetector.getInstance()`

**Methods:**

- `Action update()` - Updates color readings and outputs telemetry (finishes instantly)

**Public Fields:**

- `boolean isGreen` - True if current color is detected as green
- `boolean isPurple` - True if current color is detected as purple
- `int avgRed, avgGreen, avgBlue` - Average RGB values from both sensors
- `float[] avgHSV` - HSV conversion [Hue, Saturation, Value]

**Tunable Constants (FTC Dashboard):**

- `GREEN_HUE_MIN`, `GREEN_HUE_MAX` - Green hue range
- `PURPLE_HUE_MIN`, `PURPLE_HUE_MAX` - Purple hue range
- `MIN_SATURATION` - Minimum saturation threshold for detection

---

### Transfer

Controls transfer servos (moves balls) and intake door servos.

**Access:** `Transfer.getInstance()`

**Methods:**

- `Action transferForward()` - Spin transfer servos forward (finishes instantly)
- `Action transferBackward()` - Spin transfer servos backward (finishes instantly)
- `Action transferStop()` - Stop transfer servos (finishes instantly)
- `Action intakeDoorForward()` - Open intake door (finishes instantly)
- `Action intakeDoorBackward()` - Close intake door (finishes instantly)
- `Action intakeDoorStop()` - Stop intake door (finishes instantly)

**Tunable Constants (FTC Dashboard):**

- `FORWARD_POWER` - Transfer forward power level
- `BACKWARD_POWER` - Transfer backward power level
- `DOOR_FORWARD_POWER` - Intake door open power level
- `DOOR_BACKWARD_POWER` - Intake door close power level

---

### Intake

Controls the intake motor for ball collection.

**Access:** `Intake.getInstance()`

**Methods:**

- `Action in()` - Spin intake motor inward (finishes instantly)
- `Action out()` - Spin intake motor outward (finishes instantly)
- `Action stop()` - Stop intake motor (finishes instantly)

**Tunable Constants (FTC Dashboard):**

- `IN_POWER` - Intake inward power level
- `OUT_POWER` - Intake outward power level

---

### Shooter

Controls upper and lower shooter motors for shooting balls.

**Access:** `Shooter.getInstance()`

**Methods:**

- `Action run()` - Start both shooter motors at full power (finishes instantly)
- `Action stop()` - Stop both shooter motors (finishes instantly)

**Tunable Constants (FTC Dashboard):**

- `RUN_POWER` - Shooter motor power level
- `UPPER_OFFSET` - Speed compensation for upper motor
- `LOWER_OFFSET` - Speed compensation for lower motor

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

## Architecture

Subsystems are separated from Actions:

- **Subsystems** (this folder) - Hardware control and state management
- **Actions** (separate folder) - Complex action sequences and behaviors

This separation allows subsystems to be used independently or combined through Actions.
