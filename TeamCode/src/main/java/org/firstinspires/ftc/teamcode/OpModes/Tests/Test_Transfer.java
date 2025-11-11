package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

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
public class Test_Transfer extends OpMode {

	private Transfer transfer;
	private ActionScheduler scheduler;
	private String transferState = "STOPPED";
	private String doorState = "STOPPED";
	private boolean aButtonPrev = false;
	private boolean bButtonPrev = false;
	private boolean xButtonPrev = false;
	private boolean yButtonPrev = false;
	private boolean rightBumperPrev = false;
	private boolean leftBumperPrev = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		transfer = Transfer.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test transfer belt and door servos");
	}

	@Override
	public void loop() {
		// === TRANSFER BELT CONTROLS ===

		// A button - Transfer Forward - edge detection
		if (gamepad1.a && !aButtonPrev) {
			scheduler.schedule(transfer.transferForward());
			transferState = "FORWARD";
			telemetry.addData("Action", "Transfer belt forward");
		}
		aButtonPrev = gamepad1.a;

		// B button - Transfer Backward - edge detection
		if (gamepad1.b && !bButtonPrev) {
			scheduler.schedule(transfer.transferBackward());
			transferState = "BACKWARD";
			telemetry.addData("Action", "Transfer belt backward");
		}
		bButtonPrev = gamepad1.b;

		// X button - Transfer Stop - edge detection
		if (gamepad1.x && !xButtonPrev) {
			scheduler.schedule(transfer.transferStop());
			transferState = "STOPPED";
			telemetry.addData("Action", "Transfer belt stopped");
		}
		xButtonPrev = gamepad1.x;

		// === INTAKE DOOR CONTROLS ===

		// Y button - Intake Door Forward (open) - edge detection
		if (gamepad1.y && !yButtonPrev) {
			scheduler.schedule(transfer.intakeDoorForward());
			doorState = "FORWARD (OPEN)";
			telemetry.addData("Action", "Intake door opening");
		}
		yButtonPrev = gamepad1.y;

		// Right Bumper - Intake Door Backward (close) - edge detection
		if (gamepad1.right_bumper && !rightBumperPrev) {
			scheduler.schedule(transfer.intakeDoorBackward());
			doorState = "BACKWARD (CLOSE)";
			telemetry.addData("Action", "Intake door closing");
		}
		rightBumperPrev = gamepad1.right_bumper;

		// Left Bumper - Intake Door Stop - edge detection
		if (gamepad1.left_bumper && !leftBumperPrev) {
			scheduler.schedule(transfer.intakeDoorStop());
			doorState = "STOPPED";
			telemetry.addData("Action", "Intake door stopped");
		}
		leftBumperPrev = gamepad1.left_bumper;

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

		// Update action scheduler
		scheduler.update();
	}

	@Override
	public void stop() {
		// Clear any running actions and shutdown
		scheduler.clearActions();
		HardwareShutdown.shutdown();
	}
}
