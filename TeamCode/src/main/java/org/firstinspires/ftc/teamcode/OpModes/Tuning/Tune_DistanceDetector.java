package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.DistanceDetector;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Tuning OpMode for the DistanceDetector Subsystem
 * 
 * Purpose: Calibrate object detection threshold for optimal accuracy
 * 
 * FTC Dashboard Constants:
 * - OBJECT_THRESHOLD_CM: [0.0, 50.0] (default: 2.0)
 * 
 * Controls:
 * - DPAD UP/DOWN: Adjust OBJECT_THRESHOLD_CM (±0.1)
 * - LB/RB: Fine adjust OBJECT_THRESHOLD_CM (±0.01)
 * - X: Reset to default (2.0 cm)
 * 
 * Expected Behavior:
 * - Real-time distance sensor readings
 * - Object detection status feedback
 * - Threshold comparison display
 * - Visual confirmation of detection changes
 * 
 * Notes:
 * - Place test objects at various distances from sensor
 * - Values update in real-time through FTC Dashboard
 * - No timeout - operator can tune as long as needed
 * - Focus on finding optimal detection distance for your use case
 */
@TeleOp(name = "Tune_DistanceDetector", group = "Tuning")
public class Tune_DistanceDetector extends OpMode {

	private DistanceDetector distanceDetector;
	private ActionScheduler scheduler;
	
	// Store default values for reset functionality
	private static final double DEFAULT_THRESHOLD = 2.0;
	private static final double MIN_THRESHOLD = 0.5;
	private static final double MAX_THRESHOLD = 50.0;
	
	private static final double THRESHOLD_STEP = 0.1;
	private static final double THRESHOLD_FINE_STEP = 0.01;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		distanceDetector = DistanceDetector.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Calibrate distance detection threshold");
		telemetry.addData("Note", "Place test objects at various distances");
		telemetry.update();
	}

	@Override
	public void start() {
		telemetry.addData("Status", "Running");
		telemetry.addData("Instructions", "Use gamepad to adjust threshold");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Update distance sensor readings
		scheduler.schedule(distanceDetector.update());

		// === THRESHOLD ADJUSTMENT ===

		// X button - Reset all thresholds to default
		if (gamepad1.x) {
			DistanceDetector.OBJECT_THRESHOLD_CM = DEFAULT_THRESHOLD;
			telemetry.addData("Action", "Reset to default");
		}

		// DPAD UP - Increase OBJECT_THRESHOLD_CM
		if (gamepad1.dpad_up) {
			DistanceDetector.OBJECT_THRESHOLD_CM = Math.min(DistanceDetector.OBJECT_THRESHOLD_CM + THRESHOLD_STEP, MAX_THRESHOLD);
			telemetry.addData("OBJECT_THRESHOLD_CM", "Adjusted to " + String.format("%.2f", DistanceDetector.OBJECT_THRESHOLD_CM));
		}

		// DPAD DOWN - Decrease OBJECT_THRESHOLD_CM
		if (gamepad1.dpad_down) {
			DistanceDetector.OBJECT_THRESHOLD_CM = Math.max(DistanceDetector.OBJECT_THRESHOLD_CM - THRESHOLD_STEP, MIN_THRESHOLD);
			telemetry.addData("OBJECT_THRESHOLD_CM", "Adjusted to " + String.format("%.2f", DistanceDetector.OBJECT_THRESHOLD_CM));
		}

		// LB - Fine decrease OBJECT_THRESHOLD_CM
		if (gamepad1.left_bumper) {
			DistanceDetector.OBJECT_THRESHOLD_CM = Math.max(DistanceDetector.OBJECT_THRESHOLD_CM - THRESHOLD_FINE_STEP, MIN_THRESHOLD);
			telemetry.addData("OBJECT_THRESHOLD_CM", "Fine adjusted to " + String.format("%.2f", DistanceDetector.OBJECT_THRESHOLD_CM));
		}

		// RB - Fine increase OBJECT_THRESHOLD_CM
		if (gamepad1.right_bumper) {
			DistanceDetector.OBJECT_THRESHOLD_CM = Math.min(DistanceDetector.OBJECT_THRESHOLD_CM + THRESHOLD_FINE_STEP, MAX_THRESHOLD);
			telemetry.addData("OBJECT_THRESHOLD_CM", "Fine adjusted to " + String.format("%.2f", DistanceDetector.OBJECT_THRESHOLD_CM));
		}

		// === UPDATE SCHEDULER ===
		scheduler.update();

		// === DISPLAY TELEMETRY ===

		// Current sensor readings
		telemetry.addData("", "=== RAW DISTANCE VALUES ===");
		telemetry.addData("Distance (cm)", String.format("%.2f", distanceDetector.rawDistance));

		// Detection status
		telemetry.addData("", "=== DETECTION STATUS ===");
		String detectedObject;
		if (distanceDetector.isObject) {
			detectedObject = "OBJECT ✓";
		} else {
			detectedObject = "NO OBJECT";
		}
		telemetry.addData("Detected", detectedObject);
		telemetry.addData("Is Object", distanceDetector.isObject);

		// Current threshold
		telemetry.addData("", "=== DETECTION THRESHOLD ===");
		telemetry.addData("OBJECT_THRESHOLD_CM", String.format("%.2f (DPAD ↑↓)", DistanceDetector.OBJECT_THRESHOLD_CM));

		// Controls reminder
		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("X: Reset to default", "");

		// Distance vs Threshold comparison
		double difference = distanceDetector.rawDistance - DistanceDetector.OBJECT_THRESHOLD_CM;
		String comparisonStatus = (difference <= 0) ? "✓ OBJECT DETECTED" : "✗ OUTSIDE THRESHOLD";
		telemetry.addData("Comparison", "Distance (" + String.format("%.2f", distanceDetector.rawDistance) + ") vs Threshold (" + String.format("%.2f", DistanceDetector.OBJECT_THRESHOLD_CM) + ")");
		telemetry.addData("Status", comparisonStatus);

		telemetry.update();
	}

	@Override
	public void stop() {
		// Shutdown
		HardwareShutdown.shutdown();
	}
}
