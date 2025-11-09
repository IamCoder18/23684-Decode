package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;

/**
 * Tuning OpMode for the Spindexer Subsystem
 * 
 * Purpose: Optimize PID coefficients for smooth position control using real-time adjustment
 * 
 * FTC Dashboard Constants:
 * - P: [0.0, 0.1] (default: 0.005)
 * - I: [0.0, 0.01] (default: 0.0)
 * - D: [0.0, 0.001] (default: 0.0)
 * - F: [0.0, 0.1] (default: 0.0)
 * - zeroOffset: [-100, 100] (default: 0)
 * 
 * Controls:
 * - DPAD UP/DOWN: Adjust P (±0.0005)
 * - LB/RB: Adjust I (±0.0001)
 * - LT/RT: Adjust D (±0.00001)
 * - B: Move to position 0.25 (90°)
 * - X: Move to position 0.5 (180°)
 * - Y: Move to position 0.75 (270°)
 * - A: Zero sequence
 * - Right Stick: Fine-tune with bigger increments
 * 
 * Expected Behavior:
 * - Real-time PID coefficient adjustment
 * - Position control validation
 * - Smooth motion with proper damping
 * - Accurate zero point calibration
 * 
 * Notes:
 * - MUST zero spindexer before position commands
 * - Values update in real-time through FTC Dashboard
 * - Use right stick for larger adjustment increments
 * - Monitor error values for tuning feedback
 */
@TeleOp(name = "Tune_Spindexer", group = "Tuning")
public class Tune_Spindexer extends LinearOpMode {

	private Spindexer spindexer;
	private static final double P_STEP = 0.0005;
	private static final double I_STEP = 0.0001;
	private static final double D_STEP = 0.00001;
	private static final double FINE_MULTIPLIER = 5.0; // Right stick increases step size

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		spindexer = Spindexer.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Tune spindexer PID coefficients");
		telemetry.addData("Note", "Use FTC Dashboard to adjust values in real-time");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Instructions", "A=Zero, B/X/Y=Move, Fine tune with gamepad");
		telemetry.update();

		while (opModeIsActive()) {
			// Calculate step multipliers
			double stepMultiplier = gamepad1.right_stick_button ? FINE_MULTIPLIER : 1.0;
			
			// === ZEROING ===
			if (gamepad1.a) {
				spindexer.zero().run(null);
				telemetry.addData("Action", "Zero sequence started");
			}

			// === POSITION COMMANDS ===
			if (gamepad1.b) {
				spindexer.toPosition(0.25).run(null); // 90 degrees
				telemetry.addData("Action", "Moving to 90°");
			}
			if (gamepad1.x) {
				spindexer.toPosition(0.5).run(null); // 180 degrees
				telemetry.addData("Action", "Moving to 180°");
			}
			if (gamepad1.y) {
				spindexer.toPosition(0.75).run(null); // 270 degrees
				telemetry.addData("Action", "Moving to 270°");
			}

			// === PID COEFFICIENT ADJUSTMENTS ===
			
			// P coefficient (DPAD UP/DOWN)
			if (gamepad1.dpad_up) {
				Spindexer.P = Math.min(Spindexer.P + (P_STEP * stepMultiplier), 0.1);
				telemetry.addData("P Coefficient", "Adjusted to " + String.format("%.4f", Spindexer.P));
				Thread.sleep(100);
			}
			if (gamepad1.dpad_down) {
				Spindexer.P = Math.max(Spindexer.P - (P_STEP * stepMultiplier), 0.0);
				telemetry.addData("P Coefficient", "Adjusted to " + String.format("%.4f", Spindexer.P));
				Thread.sleep(100);
			}

			// I coefficient (LB/RB)
			if (gamepad1.left_bumper) {
				Spindexer.I = Math.max(Spindexer.I - (I_STEP * stepMultiplier), 0.0);
				telemetry.addData("I Coefficient", "Adjusted to " + String.format("%.4f", Spindexer.I));
				Thread.sleep(100);
			}
			if (gamepad1.right_bumper) {
				Spindexer.I = Math.min(Spindexer.I + (I_STEP * stepMultiplier), 0.01);
				telemetry.addData("I Coefficient", "Adjusted to " + String.format("%.4f", Spindexer.I));
				Thread.sleep(100);
			}

			// D coefficient (LT/RT)
			if (gamepad1.left_trigger > 0.5) {
				Spindexer.D = Math.max(Spindexer.D - (D_STEP * stepMultiplier), 0.0);
				telemetry.addData("D Coefficient", "Adjusted to " + String.format("%.4f", Spindexer.D));
				Thread.sleep(100);
			}
			if (gamepad1.right_trigger > 0.5) {
				Spindexer.D = Math.min(Spindexer.D + (D_STEP * stepMultiplier), 0.001);
				telemetry.addData("D Coefficient", "Adjusted to " + String.format("%.4f", Spindexer.D));
				Thread.sleep(100);
			}

			// === UPDATE PID CONTROLLER ===
			spindexer.update();

			// === TELEMETRY DISPLAY ===

			// Position information
			double currentTicks = spindexer.getCurrentPositionTicks();
			double currentRevolutions = currentTicks / Spindexer.TICKS_PER_REV;
			double currentDegrees = currentRevolutions * 360.0;
			int currentSlot = (int) ((currentDegrees / 120) % 3);

			telemetry.addData("", "=== POSITION CONTROL ===");
			telemetry.addData("Current Position", String.format("%.3f rev (%.1f°)", currentRevolutions, currentDegrees));
			telemetry.addData("Current Slot", currentSlot + " (0-2)");
			telemetry.addData("Target Position", "Use B/X/Y buttons");

			// PID coefficients display
			telemetry.addData("", "=== PID COEFFICIENTS ===");
			telemetry.addData("P", String.format("%.4f (DPAD ↑↓)", Spindexer.P));
			telemetry.addData("I", String.format("%.4f (LB/RB)", Spindexer.I));
			telemetry.addData("D", String.format("%.4f (LT/RT)", Spindexer.D));
			telemetry.addData("F", String.format("%.4f", Spindexer.F));

			// Control information
			telemetry.addData("", "=== CONTROLS ===");
			telemetry.addData("A: ZERO    B: 90°    X: 180°    Y: 270°", "");
			telemetry.addData("Fine tune: " + (gamepad1.right_stick_button ? "ENABLED" : "disabled"), "Right stick button");
			if (stepMultiplier > 1.0) {
				telemetry.addData("Step Multiplier", String.format("%.1fx", stepMultiplier));
			}

			// Performance feedback
			String stabilityStatus;
			if (Math.abs(currentTicks % (Spindexer.TICKS_PER_REV / 3)) < 50) {
				stabilityStatus = "✓ STABLE";
			} else {
				stabilityStatus = "Moving...";
			}
			telemetry.addData("Status", stabilityStatus);

			telemetry.update();
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}
}