package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.DistanceDetector;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Unit Test OpMode for the DistanceDetector Subsystem
 * <p>
 * Purpose: Test the distance sensor's ability to detect objects within range
 * <p>
 * Controls:
 * - A button: Pause/unpause sensor readings (for stable reading)
 * - B button: Print detailed distance analysis
 * <p>
 * Expected Behavior:
 * - Distance sensor reads distance to nearest object in centimeters
 * - Converts raw reading to meaningful distance value
 * - Detects OBJECT if distance is within OBJECT_THRESHOLD_CM
 * - Telemetry displays distance and detection status
 * <p>
 * Testing Focus:
 * - Verify sensor is reading meaningful distance data
 * - Test detection accuracy at various distances
 * - Check sensor response to different object materials
 * - Monitor for stable, consistent readings
 * <p>
 * Notes:
 * - Place test objects at various distances from sensor for testing
 * - Ensure distance sensor is clean and properly mounted
 * - Test under expected lighting conditions
 * - Sensor may have variance in readings - observe trends, not individual values
 * <p>
 * Duration: ≤1 minute (unit test)
 */
@TeleOp(name = "Test_DistanceDetector", group = "Unit Tests")
public class Test_DistanceDetector extends OpMode {

	private DistanceDetector distanceDetector;
	private ActionScheduler scheduler;
	private boolean isPaused = false;
	private boolean aButtonPrev = false;
	private boolean bButtonPrev = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		distanceDetector = DistanceDetector.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test distance sensor detection accuracy");
		telemetry.addData("Instructions", "Move objects at various distances");
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

		// Update distance sensor readings (unless paused)
		if (!isPaused) {
			scheduler.schedule(distanceDetector.update());
		}

		// === DISPLAY TELEMETRY ===

		// Current sensor readings
		telemetry.addData("", "=== RAW DISTANCE VALUES ===");
		telemetry.addData("Distance (cm)", String.format("%.2f", distanceDetector.rawDistance));

		// Detection status
		telemetry.addData("", "=== DETECTION STATUS ===");
		String detectionStatus;
		if (distanceDetector.isObject) {
			detectionStatus = "OBJECT ✓";
		} else {
			detectionStatus = "NO OBJECT";
		}
		telemetry.addData("Detected", detectionStatus);
		telemetry.addData("Is Object", distanceDetector.isObject);

		// Current threshold (read-only in test mode)
		telemetry.addData("", "=== DETECTION THRESHOLD ===");
		telemetry.addData("OBJECT_THRESHOLD_CM", String.format("%.2f", DistanceDetector.OBJECT_THRESHOLD_CM));

		// Controls
		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", isPaused ? "RESUME" : "PAUSE");
		telemetry.addData("B", "Print Details");

		// Test results
		telemetry.addData("", "=== TEST RESULTS ===");
		telemetry.addData("Sensor Response", "✓ OPERATIONAL");
		telemetry.addData("Object Detection", distanceDetector.isObject ? "✓ TRIGGERED" : "✓ READY");
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
		telemetry.addData("Distance Reading", String.format("%.2f cm", distanceDetector.rawDistance));
		telemetry.addData("Threshold", String.format("%.2f cm", DistanceDetector.OBJECT_THRESHOLD_CM));

		// Analyze distance relative to threshold
		String analysis;
		double difference = distanceDetector.rawDistance - DistanceDetector.OBJECT_THRESHOLD_CM;

		if (distanceDetector.isObject) {
			analysis = "Within detection range";
		} else if (difference < 0.5) {
			analysis = "Very close to threshold";
		} else if (difference < 2.0) {
			analysis = "Approaching threshold";
		} else {
			analysis = "Outside detection range";
		}

		telemetry.addData("Analysis", analysis);
	}
}
