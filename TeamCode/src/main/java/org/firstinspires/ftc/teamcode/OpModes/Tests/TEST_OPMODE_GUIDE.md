# Test OpMode Guide

This document provides an overview of all test OpModes created to validate the robot's subsystems
and integrated functionality.

## Quick Reference

| OpMode              | Purpose                          | Key Feature                         |
|---------------------|----------------------------------|-------------------------------------|
| Test_Intake         | Test intake motor                | Simple forward/reverse/stop control |
| Test_Shooter        | Test both shooter motors         | Dual motor sync with offset tuning  |
| Test_Transfer       | Test transfer belt & intake door | Independent servo control           |
| Test_Spindexer      | Test position control & storage  | PID tuning, color storage           |
| Test_ColorDetector  | Test color sensor                | Hue/saturation threshold tuning     |
| Test_IntegrationAll | **Complete workflow test**       | **All systems working together**    |

---

## Unit Test OpModes

### Test_Intake

**File:** `Test_Intake.java`

Tests the intake motor in isolation.

**Controls:**

- `A`: Start intake motor IN
- `B`: Start intake motor OUT
- `X`: Stop intake motor
- `DPAD UP/DOWN`: Adjust IN_POWER (0.0 - 1.0)
- `DPAD LEFT/RIGHT`: Adjust OUT_POWER (-1.0 to 0.0)

**What to Check:**

- [ ] Motor spins forward when A is pressed
- [ ] Motor spins backward when B is pressed
- [ ] Motor stops immediately when X is pressed
- [ ] Power adjustments work smoothly
- [ ] No grinding or unusual noises

**Common Issues:**

- Motor doesn't spin: Check motor wiring and HardwareMap name "intake"
- Power adjustment doesn't work: Ensure values stay within -1.0 to 1.0 range
- Motor direction reversed: Swap motor wires or negate power in code

---

### Test_Shooter

**File:** `Test_Shooter.java`

Tests both shooter motors for synchronized operation.

**Controls:**

- `A`: Start both shooter motors
- `B`: Stop both shooter motors
- `DPAD UP/DOWN`: Adjust RUN_POWER
- `DPAD LEFT/RIGHT`: Adjust UPPER_OFFSET
- `LB/RB`: Adjust LOWER_OFFSET

**What to Check:**

- [ ] Both motors spin at the same speed
- [ ] Motors stop together when B is pressed
- [ ] Offset adjustments compensate for speed differences
- [ ] No vibration or excessive noise
- [ ] Telemetry shows correct power values

**Tuning Tips:**

1. Run motors at full power and observe speed difference
2. If upper motor is slower, increase UPPER_OFFSET
3. If lower motor is slower, increase LOWER_OFFSET
4. Test multiple times to ensure consistency
5. Record final offset values for use in autonomous

**Common Issues:**

- One motor doesn't spin: Check wiring and HardwareMap names ("upperShooter", "lowerShooter")
- Inconsistent speeds: Adjust offsets more gradually (0.05 increments)
- Offset doesn't help: Motors may be mechanically limited; check gearing

---

### Test_Transfer

**File:** `Test_Transfer.java`

Tests the transfer belt servos and intake door servos independently.

**Controls - Transfer Servos:**

- `A`: Transfer Forward
- `B`: Transfer Backward
- `X`: Stop Transfer

**Controls - Intake Door Servos:**

- `Y`: Intake Door Forward (open)
- `RB`: Intake Door Backward (close)
- `LB`: Stop Intake Door

**DPAD Controls:**

- `DPAD UP`: Increase servo power
- `DPAD DOWN`: Decrease servo power

**What to Check:**

- [ ] Transfer belt moves smoothly in both directions
- [ ] Intake door opens fully without jamming
- [ ] Intake door closes completely
- [ ] Left and right servos move together
- [ ] No chattering or servo strain sounds
- [ ] Stop commands are immediate

**Common Issues:**

- Servos don't move: Check HardwareMap names and power supply (servos draw more power)
- Servos move in wrong direction: Swap positive/negative power
- Uneven left/right motion: Check if servos need calibration offset
- Servo stalls or makes noise: Reduce power or check for mechanical obstructions

---

### Test_Spindexer

**File:** `Test_Spindexer.java`

Tests the spindexer carousel position control with PID tuning.

**Controls - Motion:**

- `A`: Start Zero Sequence (REQUIRED first!)
- `B`: Move to Position 0 (0°)
- `X`: Move to Position 1 (120°)
- `Y`: Move to Position 2 (240°)

**Controls - Color Storage:**

- `DPAD UP`: Set current slot to GREEN
- `DPAD DOWN`: Set current slot to PURPLE
- `DPAD LEFT`: Set current slot to UNKNOWN
- `DPAD RIGHT`: Clear all slots

**Controls - PID Tuning:**

- `LB`: Decrease P (less sensitive)
- `RB`: Increase P (more sensitive)
- `LT`: Decrease D (less damping)
- `RT`: Increase D (more damping)

**What to Check:**

- [ ] Zero sequence completes and encoder reads 0
- [ ] Position commands move spindexer smoothly
- [ ] Spindexer stops at target position (within 50 ticks)
- [ ] No oscillation around target position
- [ ] Color storage works (can set and retrieve)
- [ ] Touch sensor triggers correctly

**PID Tuning Guide:**

1. Start with P=0.005, I=0, D=0
2. Increase P until spindexer reaches position but oscillates slightly
3. Increase D until oscillation is dampened but position still reached
4. Fine-tune P and D values for smooth, fast response
5. If position overshoots, increase D
6. If movement is too slow, increase P

**Typical Final Values:**

- P: 0.005 - 0.01 (adjust for responsiveness)
- I: 0 (typically not needed)
- D: 0 - 0.001 (adjust for stability)
- F: 0 (feedforward, not typically used)

**Common Issues:**

- Spindexer won't zero: Check touch sensor wiring and connectivity
- Position commands overshoot: Increase D coefficient
- Movement too slow: Increase P coefficient
- Encoder stuck at 0: Check encoder wiring (uses frontRight motor port)

---

### Test_ColorDetector

**File:** `Test_ColorDetector.java`

Tests color sensor calibration and detection accuracy.

**Controls - Green Hue Tuning:**

- `DPAD UP`: Increase GREEN_HUE_MIN
- `DPAD DOWN`: Decrease GREEN_HUE_MIN
- `DPAD RIGHT`: Increase GREEN_HUE_MAX
- `DPAD LEFT`: Decrease GREEN_HUE_MAX

**Controls - Purple Hue Tuning:**

- `LB`: Decrease PURPLE_HUE_MIN
- `RB`: Increase PURPLE_HUE_MIN
- `LT`: Decrease PURPLE_HUE_MAX
- `RT`: Increase PURPLE_HUE_MAX

**Controls - Saturation:**

- `Y`: Increase MIN_SATURATION
- `A`: Decrease MIN_SATURATION
- `X`: Reset to defaults

**What to Check:**

- [ ] Reads stable RGB values (not wildly fluctuating)
- [ ] Correctly identifies green balls
- [ ] Correctly identifies purple balls
- [ ] Doesn't falsely detect colors
- [ ] Works under expected lighting conditions
- [ ] Saturation threshold prevents false positives

**Calibration Steps:**

1. Place green ball in front of sensors
2. Adjust GREEN_HUE_MIN/MAX until isGreen = true
3. Remove green ball and verify isGreen = false
4. Repeat with purple ball
5. Test in actual match lighting conditions
6. Record final threshold values

**Typical Hue Ranges:**

- Green: 100-140 (adjust +/- 5 as needed)
- Purple: 250-290 (adjust +/- 5 as needed)
- Saturation: 0.4 minimum (increase if false positives)

**Common Issues:**

- No readings: Check sensor wiring and I2C connection
- Always detects wrong color: Check ball colors and lighting
- False positives: Increase MIN_SATURATION
- Won't detect color even when close: Decrease saturation threshold or expand hue range

---

## Integration Test OpMode

### Test_IntegrationAll

**File:** `Test_IntegrationAll.java`

Comprehensive test of all subsystems working together in a realistic workflow.

**Main Controls:**

- `A`: Zero Spindexer (REQUIRED - do this first!)
- `B`: Intake a Ball (full auto sequence)
- `X`: Shoot a Ball
- `Y`: Stop All Motors

**Manual Controls (when sequences not running):**

- `LB`: Intake motor ON
- `RB`: Intake motor REVERSE
- `DPAD UP`: Shooter ON
- `DPAD DOWN`: Shooter OFF
- `DPAD LEFT`: Transfer FORWARD
- `DPAD RIGHT`: Transfer BACKWARD

**Diagnostics:**

- `Left Stick Click`: Print detailed status
- `Right Stick Click`: Clear all stored ball colors

**Recommended Test Sequence:**

```
1. Start OpMode
2. Press A to ZERO spindexer
   ↓ Wait for "Zero Sequence Complete"
3. Press B to INTAKE first ball
   ↓ Observe: Motor runs, door opens, color detected
4. Press B again to INTAKE second ball
   ↓ Observe: Spindexer rotates 120°, new ball intaked
5. Press B again to INTAKE third ball
   ↓ Observe: All three slots filled
6. Press X to SHOOT a ball
   ↓ Observe: Shooter spins, ball expelled
7. Check telemetry for remaining balls
8. Repeat as needed
```

**What to Verify:**

- [ ] Zero sequence completes without errors
- [ ] Spindexer rotates exactly 120° between intakes
- [ ] Color detection works consistently
- [ ] All three slots can be filled
- [ ] Shooter spins up and fires ball
- [ ] Telemetry accurately tracks balls
- [ ] No subsystem interferes with another
- [ ] Smooth transitions between states

**Expected Timing:**

- Zero Sequence: 2-3 seconds
- Intake Cycle: 1-2 seconds per ball
- Shoot Cycle: 1-2 seconds
- Color Detection: <100ms

**Advanced Features Tested:**

1. **State Management**: Prevents conflicting actions (e.g., can't intake while shooting)
2. **Synchronization**: Multiple motors/servos work in coordinated sequences
3. **Sensor Integration**: Color detection feeds into ball tracking
4. **Position Tracking**: Encoder position converted to slot index
5. **Error Prevention**: Blocks invalid operations (e.g., can't shoot if not zeroed)

---

## Testing Checklist

### Pre-Testing

- [ ] All hardware is properly connected
- [ ] Battery is fully charged
- [ ] HardwareMap configuration matches subsystem hardware names
- [ ] No visible wiring damage or loose connections

### Unit Testing (run in this order)

1. [ ] Test_Intake
2. [ ] Test_Shooter
3. [ ] Test_Transfer
4. [ ] Test_ColorDetector
5. [ ] Test_Spindexer (last, as it may affect others)

### Integration Testing

1. [ ] Test_IntegrationAll - Basic workflow
2. [ ] Multiple intake and shoot cycles
3. [ ] All subsystems under load simultaneously
4. [ ] Emergency stop (Y button) from any state

### Performance Validation

- [ ] No lag between button press and motor response
- [ ] Smooth motion without jerking
- [ ] Encoder values are stable and accurate
- [ ] Color detection is consistent
- [ ] PID controller reaches position quickly
- [ ] No motor stalls or thermal shutdown

---

## Troubleshooting Guide

### Issue: OpMode won't start

**Solution:**

- Check HardwareMap configuration in FTC Control Hub
- Verify all hardware names match subsystem code
- Check for wiring issues (especially power/ground)

### Issue: Telemetry not updating

**Solution:**

- Check USB connection to Control Hub
- Restart the Robot Controller app
- Verify OpMode is actually running (not paused)

### Issue: Motor spins wrong direction

**Solution:**

- Check motor wiring polarity
- Or negate the power value in the subsystem

### Issue: Servo doesn't respond

**Solution:**

- Check servo power supply (may need external power)
- Verify servo wiring (signal pin, power, ground)
- Check HardwareMap name and port

### Issue: Spindexer won't zero

**Solution:**

- Verify touch sensor is connected
- Check that spindexer can physically reach the sensor
- Ensure encoder motor port is correct (frontRight)

### Issue: Color detection unreliable

**Solution:**

- Clean color sensor lens
- Check lighting conditions match expected environment
- Recalibrate hue/saturation thresholds
- Verify color sensor power/data lines

### Issue: PID controller overshoots target

**Solution:**

- Increase D coefficient for damping
- Decrease P coefficient for less aggressiveness
- Check encoder is reading correctly

---

## Reference Information

### Subsystem Hardware Ports

```
Intake Motor:          "intake"
Upper Shooter Motor:   "upperShooter"
Lower Shooter Motor:   "lowerShooter"
Transfer Left Servo:   "transferLeft"
Transfer Right Servo:  "transferRight"
Intake Door Left:      "intakeDoorLeft"
Intake Door Right:     "intakeDoorRight"
Spindexer Servo:       "spindexer"
Spindexer Encoder:     "frontRight" (motor port reading encoder)
Spindexer Touch Sensor: "spindexerZero"
Color Sensor Left:     "colourLeft"
Color Sensor Right:    "colourRight"
```

### Key Constants

```
Spindexer.TICKS_PER_REV = 8192 (encoder resolution)
Slot spacing = 120 degrees = 1/3 revolution
Position tolerance = 50 ticks ≈ 1.75 degrees
Slot width ≈ 5 degrees
```

### Ball Colors

```
BallColor.GREEN:   Hue 100-140
BallColor.PURPLE:  Hue 250-290
BallColor.UNKNOWN: Not detected or not checked
```

---

## Notes for Future Development

1. **Autonomous Integration**: Use these test OpModes as basis for autonomous routines
2. **Performance Tuning**: Record and save optimal PID values and offsets
3. **Sensor Calibration**: Save color thresholds after final tuning
4. **Error Logging**: Add more detailed error detection and reporting
5. **Load Testing**: Test all systems running simultaneously for thermal management

---

*Last Updated: 2025*
*Created as comprehensive testing suite for FTC robot subsystems*
