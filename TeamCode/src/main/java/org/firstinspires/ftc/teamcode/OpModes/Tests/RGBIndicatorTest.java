package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.RGBIndicator;

/**
 * Test OpMode for RGBIndicator servo control.
 * <p>
 * Gamepad controls:
 * - Left Stick Y: Increase servo position when up, decrease when down
 * - A Button: Set to minimum position (0.0)
 * - Y Button: Set to maximum position (1.0)
 * - X Button: Set to middle position (0.5)
 * </p>
 */
@TeleOp(name = "RGB Indicator Test", group = "Test")
public class RGBIndicatorTest extends OpMode {

	private final double INCREMENT = 0.01;
	private final double MIN_POSITION = 0.0;
	private final double MAX_POSITION = 1.0;
	private RGBIndicator rgbIndicator;
	private double servoPosition = 0.5;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		rgbIndicator = RGBIndicator.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Handle joystick input for servo position
		handleJoystickInput();

		// Handle button inputs
		handleButtonInput();

		// Display telemetry
		displayTelemetry();

		telemetry.update();
	}

	@Override
	public void stop() {
		HardwareShutdown.shutdown();
	}

	/**
	 * Handle joystick input to control servo position
	 */
	private void handleJoystickInput() {
		double joystickY = gamepad1.left_stick_y;

		// Apply dead zone
		if (Math.abs(joystickY) < 0.05) {
			return;
		}

		// Joystick up (positive Y) increases position
		// Joystick down (negative Y) decreases position
		// Note: Joystick Y is inverted on most gamepads (-1 is up, +1 is down)
		servoPosition -= joystickY * INCREMENT;

		// Clamp position between MIN and MAX
		servoPosition = Math.max(MIN_POSITION, Math.min(MAX_POSITION, servoPosition));

		// Set the servo position by converting to hex color
		double hueValue = servoPosition * 360.0; // Convert to 0-360 hue range for visualization
		rgbIndicator.setColor(String.format("%02X%02X%02X",
				(int) (255 * servoPosition), 0, (int) (255 * (1 - servoPosition))));
	}

	/**
	 * Handle button inputs for preset positions
	 */
	private void handleButtonInput() {
		// A Button: Minimum position
		if (gamepad1.a) {
			servoPosition = MIN_POSITION;
			rgbIndicator.setColor("000000"); // Black
		}

		// X Button: Middle position
		if (gamepad1.x) {
			servoPosition = 0.5;
			rgbIndicator.setColor("00FF00"); // Green
		}

		// Y Button: Maximum position
		if (gamepad1.y) {
			servoPosition = MAX_POSITION;
			rgbIndicator.setColor("FFFFFF"); // White
		}

		// B Button: Red
		if (gamepad1.b) {
			servoPosition = 0.277; // Red position from RGBIndicator
			rgbIndicator.setColor("FF0000"); // Red
		}
	}

	/**
	 * Display telemetry information
	 */
	private void displayTelemetry() {
		telemetry.addData("Status", "RGB Indicator Test");
		telemetry.addData("Current Servo Position", String.format("%.3f", servoPosition));
		telemetry.addData("Position Percentage", String.format("%.1f%%", servoPosition * 100));
		telemetry.addData("", "");
		telemetry.addData("Controls:", "");
		telemetry.addData("Left Stick Y", "Adjust servo position (up/down)");
		telemetry.addData("A Button", "Minimum (0.0) - Black");
		telemetry.addData("X Button", "Middle (0.5) - Green");
		telemetry.addData("Y Button", "Maximum (1.0) - White");
		telemetry.addData("B Button", "Red (0.277)");
		telemetry.addData("Increment Size", String.format("%.3f", INCREMENT));
	}
}
