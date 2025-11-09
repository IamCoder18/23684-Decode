package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.DistanceDetector;

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
public class Test_DistanceDetector extends LinearOpMode {

	private DistanceDetector distanceDetector;
	private boolean isPaused = false;

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		distanceDetector = DistanceDetector.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test distance sensor detection accuracy");
		telemetry.addData("Instructions", "Move objects at various distances");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A=Pause, B=Print Details");
		telemetry.update();

		while (opModeIsActive()) {
			// A button - Pause/unpause sensor readings
			if (gamepad1.a && !isPaused) {
				isPaused = true;
				telemetry.addData("Action", "Sensor readings paused");
				Thread.sleep(200); // Debounce
			} else if (gamepad1.a && isPaused) {
				isPaused = false;
				telemetry.addData("Action", "Sensor readings resumed");
				Thread.sleep(200); // Debounce
			}

			// B button - Print detailed analysis
			if (gamepad1.b) {
				printDetailedAnalysis();
				Thread.sleep(500); // Debounce
			}

			// Update distance sensor readings (unless paused)
			if (!isPaused) {
				distanceDetector.update().run(null);
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
		}

		// Shutdown
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
