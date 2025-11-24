package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Limelight;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

@Config
@TeleOp
public class Test_Shooter extends OpMode {
	public static int lowerTargetRPM = 2700;
	public static int upperTargetRPM = 2700;

	private Shooter shooter;
	private Spindexer spindexer;
	private Transfer transfer;
	private Intake intake;
	private ActionScheduler scheduler;
	private Limelight limelight;

	// Edge detection for triggers
	private boolean prevLeftTrigger = false;
	private boolean prevRightTrigger = false;
	private boolean prevShooterActive = false;

	@Override
	public void init() {
		Shooter.initialize(hardwareMap);
		Spindexer.initialize(hardwareMap);
		Transfer.initialize(hardwareMap);
		Intake.initialize(hardwareMap);
		Limelight.initialize(hardwareMap);

		shooter = Shooter.getInstance();
		spindexer = Spindexer.getInstance();
		transfer = Transfer.getInstance();
		intake = Intake.getInstance();
		scheduler = ActionScheduler.getInstance();
		limelight = Limelight.getInstance();

		scheduler.schedule(transfer.intakeDoorForward());
		scheduler.update();

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Right trigger controls shooter (run at target RPM or stop)
		boolean currentShooterActive = gamepad2.right_trigger > 0.5;
		
		if (currentShooterActive) {
			shooter.updateRPM(System.nanoTime());
			scheduler.schedule(shooter.run(lowerTargetRPM, upperTargetRPM));
			
			// Transfer forward only if at target RPM, otherwise backward
			if (shooter.isAtTargetRPM(lowerTargetRPM, upperTargetRPM)) {
				scheduler.schedule(transfer.transferForward());
			} else {
				scheduler.schedule(transfer.transferBackward());
			}
		} else {
			scheduler.schedule(shooter.run(0));
		}
		prevShooterActive = currentShooterActive;

		// Left joystick Y controls spindexer power
		double spindexerPower = gamepad2.left_stick_y;
		scheduler.schedule(spindexer.setDirectPower(spindexerPower));

		// Update scheduler
		scheduler.update();

		telemetry.addData("Lower Target RPM", lowerTargetRPM);
		telemetry.addData("Upper Target RPM", upperTargetRPM);
		telemetry.addData("Shooter Average RPM", shooter.averageRPM);
		telemetry.addData("At Target RPM", shooter.isAtTargetRPM(lowerTargetRPM, upperTargetRPM));
		telemetry.addData("Spindexer Power", spindexerPower);
		telemetry.addData("Distance to Blue Goal", limelight.getDistanceToTag(20));
		telemetry.addData("Distance to Red Goal", limelight.getDistanceToTag(24));
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
