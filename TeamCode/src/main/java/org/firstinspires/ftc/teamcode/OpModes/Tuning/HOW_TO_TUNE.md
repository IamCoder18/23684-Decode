# HOW TO TUNE - Robot Subsystem Tuning Guide

## Overview

This guide explains how to tune robot subsystems using the dedicated Tuning OpModes. Tuning is for **optimization** - adjusting parameters to achieve the best performance. Use tuning when you have time to experiment and fine-tune values, not during competition.

## Available Tuning OpModes

### 1. Tune_Shooter
**Purpose:** Synchronize upper and lower motors for consistent shooting
**Location:** `OpModes/Tuning/Tune_Shooter.java`

#### Setup
1. Connect to FTC Dashboard (all constants visible)
2. Ensure both shooter motors are properly mounted
3. Have access to motor RPM monitoring if available

#### Tuning Process
1. **Start with RUN_POWER:**
   - Press A to start both motors
   - Use DPAD UP/DOWN to adjust base power (±0.05)
   - Target: Consistent ball trajectory

2. **Synchronize Motors:**
   - Use LB/RB to adjust UPPER_OFFSET (±0.01)
   - Use LT/RT to adjust LOWER_OFFSET (±0.01)
   - Watch RPM difference in telemetry
   - Goal: <2% difference between motors

3. **Fine-tune:**
   - Press Y to reset to defaults anytime
   - Small adjustments (0.001) for precision
   - Test with actual ball shooting

#### Key Constants (FTC Dashboard)
- `RUN_POWER: [-1.0, 1.0]` - Base motor power
- `UPPER_OFFSET: [-1.0, 1.0]` - Upper motor adjustment
- `LOWER_OFFSET: [-1.0, 1.0]` - Lower motor adjustment
- `STOP_POWER: [-1.0, 1.0]` - Motor stop power (usually 0.0)

---

### 2. Tune_Spindexer
**Purpose:** Optimize PID coefficients for smooth position control
**Location:** `OpModes/Tuning/Tune_Spindexer.java`

#### Setup
1. **MUST zero spindexer first** (press A)
2. Ensure touch sensor is functional
3. Clear any balls from spindexer

#### Tuning Process
1. **Start with Position Testing:**
   - Press B/X/Y to test different positions
   - Observe movement smoothness and overshoot

2. **Tune P Coefficient (Proportional):**
   - DPAD UP/DOWN to adjust P (±0.0005)
   - Increase if spindexer doesn't reach position
   - Decrease if it oscillates around target

3. **Tune D Coefficient (Derivative):**
   - LT/RT to adjust D (±0.00001)
   - Increase to reduce overshoot
   - Use Right Stick for larger adjustments

4. **Fine-tune I Coefficient if needed:**
   - LB/RB to adjust I (±0.0001)
   - Usually not needed for position control

#### Key Constants (FTC Dashboard)
- `P: [0.0, 0.1]` - Proportional gain (default: 0.005)
- `I: [0.0, 0.01]` - Integral gain (default: 0.0)
- `D: [0.0, 0.001]` - Derivative gain (default: 0.0)
- `F: [0.0, 0.1]` - Feed-forward gain (default: 0.0)
- `zeroOffset: [-100, 100]` - Zero position offset

#### PID Tuning Tips
- **Too much P:** Oscillation around target
- **Too little P:** Slow response, doesn't reach target
- **Too much D:** sluggish response
- **Too little D:** overshoot and oscillation

---

### 3. Tune_ColorDetector
**Purpose:** Calibrate HSV thresholds for accurate color detection
**Location:** `OpModes/Tuning/Tune_ColorDetector.java`

#### Setup
1. Clean both color sensors
2. Test under expected lighting conditions
3. Have green and purple balls available

#### Tuning Process
1. **Green Color Range:**
   - DPAD UP/DOWN: Adjust GREEN_HUE_MIN
   - DPAD LEFT/RIGHT: Adjust GREEN_HUE_MAX
   - Test with green ball - should detect consistently

2. **Purple Color Range:**
   - LB/RB: Adjust PURPLE_HUE_MIN
   - LT/RT: Adjust PURPLE_HUE_MAX
   - Test with purple ball - should detect consistently

3. **Saturation Threshold:**
   - A/Y: Adjust MIN_SATURATION
   - Lower if detection is too strict
   - Higher if false detections occur

4. **Validation:**
   - Test both colors in different lighting
   - Ensure no cross-detection
   - Press X to reset to defaults if needed

#### Key Constants (FTC Dashboard)
- `GREEN_HUE_MIN: [0, 360]` - Minimum green hue (default: 100)
- `GREEN_HUE_MAX: [0, 360]` - Maximum green hue (default: 140)
- `PURPLE_HUE_MIN: [0, 360]` - Minimum purple hue (default: 250)
- `PURPLE_HUE_MAX: [0, 360]` - Maximum purple hue (default: 290)
- `MIN_SATURATION: [0.0, 1.0]` - Minimum saturation (default: 0.4)

#### Color Detection Tips
- **Hue:** Color on color wheel (0-360°)
- **Saturation:** Color intensity (0.0-1.0)
- **Value:** Brightness (0.0-1.0)
- Test in actual competition lighting when possible

---

## General Tuning Guidelines

### When to Tune
- **Practice sessions** - when you have time to experiment
- **After hardware changes** - new motors, sensors, or mechanical modifications
- **Performance issues** - inconsistency, inaccuracy, or unexpected behavior
- **Before competitions** - final optimization of tuned values

### Safety Considerations
- **Emergency Stop:** Press Y in any tuning OpMode to stop all motors
- **Start Small:** Make small adjustments, test, then adjust more
- **Document Values:** Write down good values before making changes
- **Test Thoroughly:** Run multiple tests under different conditions

### Saving Tuned Values
1. **FTC Dashboard:** Values persist while app is open
2. **Code Updates:** Update subsystem constants when satisfied
3. **Backup:** Document good values in team notes
4. **Version Control:** Commit tuned values to repository

### Common Issues

#### Shooter Issues
- **Motors spin at different speeds:** Adjust UPPER_OFFSET/LOWER_OFFSET
- **Inconsistent ball trajectory:** Check RUN_POWER and motor mounting
- **Motors stalling:** Reduce power or check for mechanical binding

#### Spindexer Issues
- **Won't move to position:** Increase P coefficient
- **Oscillates around target:** Decrease P, increase D
- **Won't zero:** Check touch sensor connection and position
- **Slow response:** Increase P, decrease D

#### Color Detection Issues
- **No color detected:** Check sensor connections and lighting
- **Wrong color detected:** Adjust hue ranges, check saturation
- **Inconsistent detection:** Improve lighting, clean sensors
- **Both colors detected:** Adjust hue ranges to be mutually exclusive

### Recommended Tuning Order
1. **Tune_Shooter** - Ensure consistent motor speeds
2. **Tune_Spindexer** - Smooth position control
3. **Tune_ColorDetector** - Accurate color detection

### Performance Metrics
- **Shooter:** <2% speed difference between motors
- **Spindexer:** <50 tick position error, smooth motion
- **Color Detector:** >95% correct detection rate

---

## Troubleshooting Tuning Problems

### OpMode Won't Start
- Check hardware initialization
- Ensure robot is configured properly
- Verify all required sensors/motors are connected

### Values Don't Change
- Check FTC Dashboard connection
- Ensure OpMode is running (not paused)
- Verify constant names match subsystem

### Unexpected Behavior
- Reset to defaults (Y button in most OpModes)
- Check for mechanical issues
- Verify sensor calibration

### Getting Help
- Document symptoms and current values
- Check team documentation for known issues
- Test with simple OpModes to isolate problems

---

**Created:** 2025-11-09  
**Last Updated:** 2025-11-09  
**Version:** 1.0