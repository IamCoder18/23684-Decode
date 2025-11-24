package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

@Config
@TeleOp
public class Test_Spindexer extends OpMode {
	public static double manualPower = 0.0;

	private Spindexer spindexer;
	private ActionScheduler scheduler;

	@Override
	public void init() {
		Spindexer.initialize(hardwareMap);
		spindexer = Spindexer.getInstance();
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Manual control with gamepad
		double leftStickY = -gamepad1.left_stick_y;
		double manualControlPower = leftStickY;

		// Apply manual control
//		scheduler.schedule(spindexer.setDirectPower(manualControlPower));
		scheduler.update();

		// Update spindexer internal state
		spindexer.updateRawPosition();

		// Telemetry
		telemetry.addData("Manual Control Power", manualControlPower);
		telemetry.addData("Current Position (ticks)", spindexer.getCurrentPositionTicks());
		telemetry.addData("Current Position (raw servo)", spindexer.currentPosition);
		telemetry.addData("Current Position (revolutions)", spindexer.per);
		telemetry.addData("Current Position (percent)", spindexer.cent);
		telemetry.addData("Current Position (normalized %)", spindexer.fin);
		telemetry.addData("Target Position", spindexer.targetPosition);
		telemetry.addData("Controller Power Output", spindexer.power);
		telemetry.update();
	}

	@Override
	public void stop() {
		Spindexer.shutdown();
	}
}
