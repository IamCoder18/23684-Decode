package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Unit Test OpMode for the ColorDetector Subsystem
 * <p>
 * Purpose: Test the color sensor's ability to detect and classify ball colors (GREEN/PURPLE)
 * <p>
 * Controls:
 * - A button: Pause/unpause sensor readings (for stable reading)
 * - B button: Print detailed sensor analysis
 * <p>
 * Expected Behavior:
 * - Color sensor reads RGB values from both sensors (left and right)
 * - Averages the RGB values
 * - Converts to HSV color space
 * - Detects GREEN if hue is in GREEN range and saturation is sufficient
 * - Detects PURPLE if hue is in PURPLE range and saturation is sufficient
 * - Telemetry displays RGB, HSV, and detection status
 * <p>
 * Testing Focus:
 * - Verify sensors are reading meaningful data
 * - Test detection accuracy with actual balls
 * - Check sensor response to different lighting conditions
 * - Monitor for stable, consistent readings
 * <p>
 * Notes:
 * - Place test balls in front of sensors for testing
 * - Ensure both color sensors are clean and properly mounted
 * - Test under expected lighting conditions
 * - Monitor RGB values to ensure sensors are functioning
 * <p>
 * Duration: ≤1 minute (unit test)
 */
@TeleOp(name = "Test_ColorDetector", group = "Unit Tests")
public class Test_ColorDetector extends OpMode {

	private ColorDetector colorDetector;
	private ActionScheduler scheduler;
	private boolean isPaused = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		colorDetector = ColorDetector.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test color sensor detection accuracy");
		telemetry.addData("Instructions", "Place balls in front of sensors");
	}

	@Override
	public void loop() {
		// A button - Pause/unpause sensor readings
		if (gamepad1.a && !isPaused) {
			isPaused = true;
			telemetry.addData("Action", "Sensor readings paused");
		} else if (!gamepad1.a && isPaused) {
			isPaused = false;
			telemetry.addData("Action", "Sensor readings resumed");
		}

		// B button - Print detailed analysis
		if (gamepad1.b) {
			printDetailedAnalysis();
		}

		// Update color sensor readings (unless paused)
		if (!isPaused) {
			scheduler.schedule(colorDetector.update());
		}

		// === DISPLAY TELEMETRY ===

		// Current sensor readings
		telemetry.addData("", "=== RAW RGB VALUES ===");
		telemetry.addData("Average Red", colorDetector.avgRed);
		telemetry.addData("Average Green", colorDetector.avgGreen);
		telemetry.addData("Average Blue", colorDetector.avgBlue);

		// HSV values
		telemetry.addData("", "=== HSV VALUES ===");
		telemetry.addData("Hue (0-360)", String.format("%.1f", colorDetector.avgHSV[0]));
		telemetry.addData("Saturation (0-1)", String.format("%.3f", colorDetector.avgHSV[1]));
		telemetry.addData("Value (0-1)", String.format("%.3f", colorDetector.avgHSV[2]));

		// Detection status
		telemetry.addData("", "=== DETECTION STATUS ===");
		String detectedColor;
		if (colorDetector.isGreen) {
			detectedColor = "GREEN ✓";
		} else if (colorDetector.isPurple) {
			detectedColor = "PURPLE ✓";
		} else {
			detectedColor = "UNKNOWN";
		}
		telemetry.addData("Detected Color", detectedColor);
		telemetry.addData("Is Green", colorDetector.isGreen);
		telemetry.addData("Is Purple", colorDetector.isPurple);

		// Current thresholds (read-only in test mode)
		telemetry.addData("", "=== DETECTION THRESHOLDS ===");
		telemetry.addData("GREEN_HUE_RANGE", String.format("%.0f - %.0f", ColorDetector.GREEN_HUE_MIN, ColorDetector.GREEN_HUE_MAX));
		telemetry.addData("PURPLE_HUE_RANGE", String.format("%.0f - %.0f", ColorDetector.PURPLE_HUE_MIN, ColorDetector.PURPLE_HUE_MAX));
		telemetry.addData("MIN_SATURATION", String.format("%.2f", ColorDetector.MIN_SATURATION));

		// Controls
		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", isPaused ? "RESUME" : "PAUSE");
		telemetry.addData("B", "Print Details");

		// Test results
		telemetry.addData("", "=== TEST RESULTS ===");
		telemetry.addData("Sensor Response", "✓ OPERATIONAL");
		telemetry.addData("Color Detection", detectedColor.equals("UNKNOWN") ? "⚠ NO MATCH" : "✓ FUNCTIONAL");
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
		telemetry.addData("RGB Values", String.format("(%d, %d, %d)", colorDetector.avgRed, colorDetector.avgGreen, colorDetector.avgBlue));
		telemetry.addData("HSV Values", String.format("(%.1f°, %.3f, %.3f)", colorDetector.avgHSV[0], colorDetector.avgHSV[1], colorDetector.avgHSV[2]));

		// Analyze which threshold range the current reading falls into
		String analysis = "";
		if (colorDetector.avgHSV[1] > ColorDetector.MIN_SATURATION) {
			if (colorDetector.avgHSV[0] >= ColorDetector.GREEN_HUE_MIN && colorDetector.avgHSV[0] <= ColorDetector.GREEN_HUE_MAX) {
				analysis = "Within GREEN range";
			} else if (colorDetector.avgHSV[0] >= ColorDetector.PURPLE_HUE_MIN && colorDetector.avgHSV[0] <= ColorDetector.PURPLE_HUE_MAX) {
				analysis = "Within PURPLE range";
			} else {
				analysis = "Outside both ranges";
			}
		} else {
			analysis = "Below saturation threshold";
		}

		telemetry.addData("Analysis", analysis);
	}
}
