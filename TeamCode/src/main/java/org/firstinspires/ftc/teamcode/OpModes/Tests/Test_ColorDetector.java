package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;

/**
 * Unit Test OpMode for the ColorDetector Subsystem
 *
 * Purpose: Tests the color sensor's ability to detect and classify ball colors (GREEN/PURPLE)
 *
 * Controls - Threshold Tuning:
 * - DPAD UP: Increase GREEN_HUE_MIN
 * - DPAD DOWN: Decrease GREEN_HUE_MIN
 * - DPAD RIGHT: Increase GREEN_HUE_MAX
 * - DPAD LEFT: Decrease GREEN_HUE_MAX
 *
 * Controls - Purple Threshold (Bumpers):
 * - Left Bumper: Decrease PURPLE_HUE_MIN
 * - Right Bumper: Increase PURPLE_HUE_MIN
 * - Left Trigger: Decrease PURPLE_HUE_MAX
 * - Right Trigger: Increase PURPLE_HUE_MAX
 *
 * Controls - Saturation:
 * - Y button: Increase MIN_SATURATION
 * - A button: Decrease MIN_SATURATION
 *
 * Controls - Reset:
 * - X button: Reset all thresholds to default values
 *
 * Expected Behavior:
 * - Color sensor reads RGB values from both sensors (left and right)
 * - Averages the RGB values
 * - Converts to HSV color space
 * - Detects GREEN if hue is in GREEN range and saturation is sufficient
 * - Detects PURPLE if hue is in PURPLE range and saturation is sufficient
 * - Telemetry displays RGB, HSV, and detection status
 *
 * Notes:
 * - Ensure both color sensors are clean and properly calibrated
 * - Test detection with actual balls under expected lighting conditions
 * - Adjust hue ranges if colors are not being detected correctly
 * - Adjust saturation threshold if detection is too sensitive or not sensitive enough
 * - Monitor RGB values to ensure sensors are reading meaningful data
 * - Save final threshold values and update code when tuning is complete
 *
 * Typical HSV Ranges:
 * - GREEN: Hue 100-140, Saturation > 0.4
 * - PURPLE: Hue 250-290, Saturation > 0.4
 */
@TeleOp(name = "Test_ColorDetector", group = "Unit Tests")
public class Test_ColorDetector extends LinearOpMode {

    private ColorDetector colorDetector;

    // Store default values for reset functionality
    private static final double DEFAULT_GREEN_HUE_MIN = 100;
    private static final double DEFAULT_GREEN_HUE_MAX = 140;
    private static final double DEFAULT_PURPLE_HUE_MIN = 250;
    private static final double DEFAULT_PURPLE_HUE_MAX = 290;
    private static final double DEFAULT_MIN_SATURATION = 0.4;

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize hardware
        HardwareInitializer.initialize(hardwareMap);
        colorDetector = ColorDetector.getInstance();

        telemetry.addData("Status", "Initialized - Waiting for START");
        telemetry.update();

        waitForStart();

        telemetry.addData("Status", "Running");
        telemetry.addData("Instructions", "Place balls in front of sensors");
        telemetry.update();

        while (opModeIsActive()) {
            // Update color sensor readings
            colorDetector.update().run(null);

            // === GREEN HUE RANGE TUNING ===

            // DPAD UP - Increase GREEN_HUE_MIN
            if (gamepad1.dpad_up) {
                ColorDetector.GREEN_HUE_MIN = Math.min(ColorDetector.GREEN_HUE_MIN + 1, 360);
                telemetry.addData("GREEN_HUE_MIN adjusted to", ColorDetector.GREEN_HUE_MIN);
                Thread.sleep(100); // Debounce
            }

            // DPAD DOWN - Decrease GREEN_HUE_MIN
            if (gamepad1.dpad_down) {
                ColorDetector.GREEN_HUE_MIN = Math.max(ColorDetector.GREEN_HUE_MIN - 1, 0);
                telemetry.addData("GREEN_HUE_MIN adjusted to", ColorDetector.GREEN_HUE_MIN);
                Thread.sleep(100); // Debounce
            }

            // DPAD RIGHT - Increase GREEN_HUE_MAX
            if (gamepad1.dpad_right) {
                ColorDetector.GREEN_HUE_MAX = Math.min(ColorDetector.GREEN_HUE_MAX + 1, 360);
                telemetry.addData("GREEN_HUE_MAX adjusted to", ColorDetector.GREEN_HUE_MAX);
                Thread.sleep(100); // Debounce
            }

            // DPAD LEFT - Decrease GREEN_HUE_MAX
            if (gamepad1.dpad_left) {
                ColorDetector.GREEN_HUE_MAX = Math.max(ColorDetector.GREEN_HUE_MAX - 1, 0);
                telemetry.addData("GREEN_HUE_MAX adjusted to", ColorDetector.GREEN_HUE_MAX);
                Thread.sleep(100); // Debounce
            }

            // === PURPLE HUE RANGE TUNING ===

            // Left Bumper - Decrease PURPLE_HUE_MIN
            if (gamepad1.left_bumper) {
                ColorDetector.PURPLE_HUE_MIN = Math.max(ColorDetector.PURPLE_HUE_MIN - 1, 0);
                telemetry.addData("PURPLE_HUE_MIN adjusted to", ColorDetector.PURPLE_HUE_MIN);
                Thread.sleep(100); // Debounce
            }

            // Right Bumper - Increase PURPLE_HUE_MIN
            if (gamepad1.right_bumper) {
                ColorDetector.PURPLE_HUE_MIN = Math.min(ColorDetector.PURPLE_HUE_MIN + 1, 360);
                telemetry.addData("PURPLE_HUE_MIN adjusted to", ColorDetector.PURPLE_HUE_MIN);
                Thread.sleep(100); // Debounce
            }

            // Left Trigger - Decrease PURPLE_HUE_MAX
            if (gamepad1.left_trigger > 0.5) {
                ColorDetector.PURPLE_HUE_MAX = Math.max(ColorDetector.PURPLE_HUE_MAX - 1, 0);
                telemetry.addData("PURPLE_HUE_MAX adjusted to", ColorDetector.PURPLE_HUE_MAX);
                Thread.sleep(100); // Debounce
            }

            // Right Trigger - Increase PURPLE_HUE_MAX
            if (gamepad1.right_trigger > 0.5) {
                ColorDetector.PURPLE_HUE_MAX = Math.min(ColorDetector.PURPLE_HUE_MAX + 1, 360);
                telemetry.addData("PURPLE_HUE_MAX adjusted to", ColorDetector.PURPLE_HUE_MAX);
                Thread.sleep(100); // Debounce
            }

            // === SATURATION TUNING ===

            // Y button - Increase MIN_SATURATION
            if (gamepad1.y) {
                ColorDetector.MIN_SATURATION = Math.min(ColorDetector.MIN_SATURATION + 0.05, 1.0);
                telemetry.addData("MIN_SATURATION adjusted to", ColorDetector.MIN_SATURATION);
                Thread.sleep(200); // Debounce
            }

            // A button - Decrease MIN_SATURATION
            if (gamepad1.a) {
                ColorDetector.MIN_SATURATION = Math.max(ColorDetector.MIN_SATURATION - 0.05, 0.0);
                telemetry.addData("MIN_SATURATION adjusted to", ColorDetector.MIN_SATURATION);
                Thread.sleep(200); // Debounce
            }

            // === RESET TO DEFAULTS ===

            // X button - Reset all thresholds
            if (gamepad1.x) {
                ColorDetector.GREEN_HUE_MIN = DEFAULT_GREEN_HUE_MIN;
                ColorDetector.GREEN_HUE_MAX = DEFAULT_GREEN_HUE_MAX;
                ColorDetector.PURPLE_HUE_MIN = DEFAULT_PURPLE_HUE_MIN;
                ColorDetector.PURPLE_HUE_MAX = DEFAULT_PURPLE_HUE_MAX;
                ColorDetector.MIN_SATURATION = DEFAULT_MIN_SATURATION;
                telemetry.addData("Status", "Reset to defaults");
                Thread.sleep(500); // Allow user to see message
            }

            // === DISPLAY TELEMETRY ===

            telemetry.addData("", "--- RAW RGB VALUES ---");
            telemetry.addData("Average Red", colorDetector.avgRed);
            telemetry.addData("Average Green", colorDetector.avgGreen);
            telemetry.addData("Average Blue", colorDetector.avgBlue);

            telemetry.addData("", "--- HSV VALUES ---");
            telemetry.addData("Hue (0-360)", String.format("%.1f", colorDetector.avgHSV[0]));
            telemetry.addData("Saturation (0-1)", String.format("%.3f", colorDetector.avgHSV[1]));
            telemetry.addData("Value (0-1)", String.format("%.3f", colorDetector.avgHSV[2]));

            telemetry.addData("", "--- DETECTION ---");
            telemetry.addData("Is Green", colorDetector.isGreen);
            telemetry.addData("Is Purple", colorDetector.isPurple);
            if (colorDetector.isGreen) {
                telemetry.addData("Detected Color", "GREEN");
            } else if (colorDetector.isPurple) {
                telemetry.addData("Detected Color", "PURPLE");
            } else {
                telemetry.addData("Detected Color", "UNKNOWN");
            }

            telemetry.addData("", "--- GREEN THRESHOLDS ---");
            telemetry.addData("GREEN_HUE_MIN", ColorDetector.GREEN_HUE_MIN);
            telemetry.addData("GREEN_HUE_MAX", ColorDetector.GREEN_HUE_MAX);

            telemetry.addData("", "--- PURPLE THRESHOLDS ---");
            telemetry.addData("PURPLE_HUE_MIN", ColorDetector.PURPLE_HUE_MIN);
            telemetry.addData("PURPLE_HUE_MAX", ColorDetector.PURPLE_HUE_MAX);

            telemetry.addData("", "--- SATURATION ---");
            telemetry.addData("MIN_SATURATION", ColorDetector.MIN_SATURATION);

            telemetry.update();
        }

        // Shutdown
        HardwareShutdown.shutdown();
    }
}
