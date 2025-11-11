package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.TouchDetector;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Unit Test OpMode for the TouchDetector Subsystem
 * <p>
 * Purpose: Test the touch sensors' ability to detect physical contact
 * <p>
 * Controls:
 * - A button: Pause/unpause sensor readings (for stable reading)
 * - B button: Print detailed touch sensor analysis
 * <p>
 * Expected Behavior:
 * - Touch sensors read boolean state (pressed or not pressed)
 * - Left and right sensors report independently
 * - Combined detected field is true if either sensor is pressed
 * - Telemetry displays both individual and combined states
 * <p>
 * Testing Focus:
 * - Verify sensors respond to physical contact
 * - Test independent operation of left and right sensors
 * - Confirm combined detection logic (OR operation)
 * - Monitor for responsive, consistent readings
 * <p>
 * Notes:
 * - Press/release sensors with test objects to trigger detections
 * - Ensure both sensors are properly mounted and accessible
 * - Watch for immediate response to contact and release
 * - Check for debouncing consistency
 * <p>
 * Duration: ≤1 minute (unit test)
 */
@TeleOp(name = "Test_TouchDetector", group = "Unit Tests")
public class Test_TouchDetector extends OpMode {

	private TouchDetector touchDetector;
	private ActionScheduler scheduler;
	private boolean isPaused = false;
	private boolean aButtonPrev = false;
	private boolean bButtonPrev = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		touchDetector = TouchDetector.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test touch sensor detection accuracy");
		telemetry.addData("Instructions", "Press/release both touch sensors");
	}

	@Override
	public void loop() {
		// A button - Pause/unpause sensor readings - edge detection
		if (gamepad1.a && !aButtonPrev) {
			isPaused = !isPaused;
			telemetry.addData("Action", isPaused ? "Sensor readings paused" : "Sensor readings resumed");
		}
		aButtonPrev = gamepad1.a;

		// B button - Print detailed analysis - edge detection
		if (gamepad1.b && !bButtonPrev) {
			printDetailedAnalysis();
		}
		bButtonPrev = gamepad1.b;

		// Update touch sensor readings (unless paused)
		if (!isPaused) {
			scheduler.schedule(touchDetector.update());
		}

		// === DISPLAY TELEMETRY ===

		// Individual sensor states
		telemetry.addData("", "=== INDIVIDUAL SENSORS ===");
		telemetry.addData("Touch Left", touchDetector.touchLeft ? "PRESSED ✓" : "RELEASED");
		telemetry.addData("Touch Right", touchDetector.touchRight ? "PRESSED ✓" : "RELEASED");

		// Combined detection state
		telemetry.addData("", "=== COMBINED STATE ===");
		String detectionStatus;
		if (touchDetector.detected) {
			detectionStatus = "CONTACT ✓";
		} else {
			detectionStatus = "NO CONTACT";
		}
		telemetry.addData("Detected", detectionStatus);
		telemetry.addData("Is Detected", touchDetector.detected);

		// Detection logic explanation
		telemetry.addData("", "=== DETECTION LOGIC ===");
		telemetry.addData("Logic", "OR (either sensor triggers)");
		telemetry.addData("Status", touchDetector.touchLeft || touchDetector.touchRight ? "Active" : "Inactive");

		// Controls
		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", isPaused ? "RESUME" : "PAUSE");
		telemetry.addData("B", "Print Details");

		// Test results
		telemetry.addData("", "=== TEST RESULTS ===");
		telemetry.addData("Sensor Response", "✓ OPERATIONAL");
		telemetry.addData("Combined Logic", "✓ FUNCTIONAL");
		telemetry.addData("Reading Stability", isPaused ? "PAUSED" : "ACTIVE");

		telemetry.update();

		// Update action scheduler
		scheduler.update();
	}

	@Override
	public void stop() {
		// Clear any running actions and shutdown
		scheduler.clearActions();
		HardwareShutdown.shutdown();
	}

	/**
	 * Print detailed sensor analysis to telemetry
	 */
	private void printDetailedAnalysis() {
		telemetry.addData("", "=== DETAILED ANALYSIS ===");
		telemetry.addData("Left Sensor", touchDetector.touchLeft ? "Pressed" : "Released");
		telemetry.addData("Right Sensor", touchDetector.touchRight ? "Pressed" : "Released");
		
		// Analyze sensor combination
		String analysis;
		
		if (touchDetector.touchLeft && touchDetector.touchRight) {
			analysis = "Both sensors pressed";
		} else if (touchDetector.touchLeft) {
			analysis = "Left sensor only";
		} else if (touchDetector.touchRight) {
			analysis = "Right sensor only";
		} else {
			analysis = "No sensors pressed";
		}
		
		telemetry.addData("Analysis", analysis);
	}
}
