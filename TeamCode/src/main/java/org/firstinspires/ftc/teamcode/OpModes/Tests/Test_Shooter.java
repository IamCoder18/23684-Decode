package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

@Config
@TeleOp
public class Test_Shooter extends OpMode {
	public static int targetRPM = 2300;

	private Shooter shooter;
	private Spindexer spindexer;
	private Transfer transfer;
	private Intake intake;
	private ActionScheduler scheduler;

	// Edge detection for triggers
	private boolean prevLeftTrigger = false;
	private boolean prevRightTrigger = false;

	@Override
	public void init() {
		Shooter.initialize(hardwareMap);
		Spindexer.initialize(hardwareMap);
		Transfer.initialize(hardwareMap);
		Intake.initialize(hardwareMap);

		shooter = Shooter.getInstance();
		spindexer = Spindexer.getInstance();
		transfer = Transfer.getInstance();
		intake = Intake.getInstance();
		scheduler = ActionScheduler.getInstance();

		scheduler.schedule(transfer.intakeDoorForward());
		scheduler.update();

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Always run shooter at target RPM
		shooter.updateRPM(System.nanoTime());
		scheduler.schedule(shooter.run(targetRPM));

		// Transfer forward only if at target RPM, otherwise backward
		if (shooter.isAtTargetRPM(targetRPM)) {
			scheduler.schedule(transfer.transferForward());
		} else {
			scheduler.schedule(transfer.transferBackward());
		}

		// Gamepad 2 right trigger edge detection
		boolean currentRightTrigger = gamepad2.right_trigger > 0;
		if (currentRightTrigger && !prevRightTrigger) {
			// Rising edge: trigger just pressed
			scheduler.schedule(spindexer.setDirectPower(0.3));
		} else if (!currentRightTrigger && prevRightTrigger) {
			// Falling edge: trigger just released
			scheduler.schedule(spindexer.setDirectPower(0.0));
		}
		prevRightTrigger = currentRightTrigger;

		// Gamepad 2 left trigger edge detection
		boolean currentLeftTrigger = gamepad2.left_trigger > 0;
		if (currentLeftTrigger && !prevLeftTrigger) {
			// Rising edge: trigger just pressed
			scheduler.schedule(intake.in());
		} else if (!currentLeftTrigger && prevLeftTrigger) {
			// Falling edge: trigger just released
			scheduler.schedule(intake.stop());
		}
		prevLeftTrigger = currentLeftTrigger;

		// Update scheduler
		scheduler.update();

		telemetry.addData("Target RPM", targetRPM);
		telemetry.addData("Shooter Average RPM", shooter.averageRPM);
		telemetry.addData("At Target RPM", shooter.isAtTargetRPM(targetRPM));
		telemetry.update();
	}

	@Override
	public void stop() {
		Shooter.shutdown();
		Spindexer.shutdown();
		Transfer.shutdown();
		Intake.shutdown();
	}
}
