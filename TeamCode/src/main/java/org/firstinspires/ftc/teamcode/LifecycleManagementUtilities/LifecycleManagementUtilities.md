# Lifecycle Management Utilities

Utilities for managing the initialization, update, and shutdown of subsystems throughout the OpMode
lifecycle.

## Overview

These utilities handle the orchestration of all subsystems and must be called at specific points in
your OpMode to ensure proper setup and teardown.

## HardwareInitializer

Initializes all subsystems in the correct dependency order.

**Usage:**

```java
@Override
public void init() {
    HardwareInitializer.initialize(hardwareMap);
}
```

**Initialization Order:**

1. ColorSensor
2. Transfer
3. Shooter
4. Intake
5. Spindexer (last, depends on others)

## SubsystemUpdater

Handles periodic updates for subsystems that require continuous processing.

**Usage:**

```java
@Override
public void loop() {
    SubsystemUpdater.update();  // Must call every loop iteration
    // Your code here
}
```

**Critical:** Must be called every loop cycle for proper PID control and other periodic tasks.

## HardwareShutdown

Cleanly shuts down all subsystems.

**Usage:**

```java
@Override
public void stop() {
    HardwareShutdown.shutdown();
}
```

**Shutdown Order (reverse of initialization):**

1. Spindexer
2. Intake
3. Shooter
4. Transfer
5. ColorSensor

## Complete OpMode Example

```java
@TeleOp
public class MyOpMode extends OpMode {
    @Override
    public void init() {
        HardwareInitializer.initialize(hardwareMap);
    }
    
    @Override
    public void loop() {
        SubsystemUpdater.update();  // Must call every loop
        
        // Your code here
    }
    
    @Override
    public void stop() {
        HardwareShutdown.shutdown();
    }
}
```

## Notes

- Always call `HardwareInitializer.initialize()` in `init()` before using any subsystems
- Always call `SubsystemUpdater.update()` in `loop()` - it's critical for PID control
- Always call `HardwareShutdown.shutdown()` in `stop()` for clean shutdown
- Timing is handled internally using System.nanoTime() for precision timing
