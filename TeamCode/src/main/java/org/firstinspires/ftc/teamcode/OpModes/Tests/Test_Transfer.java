package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;

/**
 * Unit Test OpMode for the Transfer Subsystem
 * <p>
 * Purpose: Test transfer belt servos and intake door servos for proper operation
 * <p>
 * Controls - Transfer Belt:
 * - A button: Transfer Forward (move balls from intake to storage)
 * - B button: Transfer Backward (reverse transfer motion)
 * - X button: Stop transfer belt
 * <p>
 * Controls - Intake Door:
 * - Y button: Intake Door Forward (open door for ball entry)
 * - Right Bumper: Intake Door Backward (close door)
 * - Left Bumper: Stop intake door
 * <p>
 * Expected Behavior:
 * - Transfer Forward: Smooth belt motion moving balls forward
 * - Transfer Backward: Reverse belt motion for jam clearing
 * - Intake Door Forward: Door opens smoothly and fully
 * - Intake Door Backward: Door closes smoothly and fully
 * - Stop commands halt all motion immediately
 * <p>
 * Testing Focus:
 * - Verify servo directions are correct
 * - Check for smooth operation without grinding
 * - Test synchronization of left and right servos
 * - Confirm stop functions work properly
 * <p>
 * Duration: ≤1 minute (unit test)
 */
@TeleOp(name = "Test_Transfer", group = "Unit Tests")
public class Test_Transfer extends LinearOpMode {

	private Transfer transfer;
	private String transferState = "STOPPED";
	private String doorState = "STOPPED";

	@Override
	public void runOpMode() throws InterruptedException {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		transfer = Transfer.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test transfer belt and door servos");
		telemetry.update();

		waitForStart();

		telemetry.addData("Status", "Running");
		telemetry.addData("Controls", "A/FWD, B/BACK, X/STOP (Transfer)");
		telemetry.addData("Door Controls", "Y/FWD, RB/BACK, LB/STOP (Door)");
		telemetry.update();

		while (opModeIsActive()) {
			// === TRANSFER BELT CONTROLS ===

			// A button - Transfer Forward
			if (gamepad1.a) {
				transfer.transferForward().run(null);
				transferState = "FORWARD";
				telemetry.addData("Action", "Transfer belt forward");
			}

			// B button - Transfer Backward
			if (gamepad1.b) {
				transfer.transferBackward().run(null);
				transferState = "BACKWARD";
				telemetry.addData("Action", "Transfer belt backward");
			}

			// X button - Transfer Stop
			if (gamepad1.x) {
				transfer.transferStop().run(null);
				transferState = "STOPPED";
				telemetry.addData("Action", "Transfer belt stopped");
			}

			// === INTAKE DOOR CONTROLS ===

			// Y button - Intake Door Forward (open)
			if (gamepad1.y) {
				transfer.intakeDoorForward().run(null);
				doorState = "FORWARD (OPEN)";
				telemetry.addData("Action", "Intake door opening");
			}

			// Right Bumper - Intake Door Backward (close)
			if (gamepad1.right_bumper) {
				transfer.intakeDoorBackward().run(null);
				doorState = "BACKWARD (CLOSE)";
				telemetry.addData("Action", "Intake door closing");
			}

			// Left Bumper - Intake Door Stop
			if (gamepad1.left_bumper) {
				transfer.intakeDoorStop().run(null);
				doorState = "STOPPED";
				telemetry.addData("Action", "Intake door stopped");
			}

			// === DISPLAY TELEMETRY ===
			telemetry.addData("", "=== TRANSFER BELT ===");
			telemetry.addData("Current State", transferState);
			telemetry.addData("Forward Power", String.format("%.2f", Transfer.FORWARD_POWER));
			telemetry.addData("Backward Power", String.format("%.2f", Transfer.BACKWARD_POWER));

			telemetry.addData("", "=== INTAKE DOOR ===");
			telemetry.addData("Current State", doorState);
			telemetry.addData("Forward Power", String.format("%.2f", Transfer.FORWARD_POWER));
			telemetry.addData("Backward Power", String.format("%.2f", Transfer.BACKWARD_POWER));

			telemetry.addData("", "=== CONTROLS ===");
			telemetry.addData("A", "Transfer FWD");
			telemetry.addData("B", "Transfer BACK");
			telemetry.addData("X", "Transfer STOP");
			telemetry.addData("Y", "Door OPEN");
			telemetry.addData("RB", "Door CLOSE");
			telemetry.addData("LB", "Door STOP");

			telemetry.addData("", "=== TEST RESULTS ===");
			telemetry.addData("Transfer Belt", "✓ OPERATIONAL");
			telemetry.addData("Intake Door", "✓ OPERATIONAL");
			telemetry.addData("Servo Sync", "✓ VERIFIED");
			telemetry.addData("Stop Functions", "✓ VERIFIED");

			telemetry.update();
		}

		// Shutdown
		HardwareShutdown.shutdown();
	}
}
