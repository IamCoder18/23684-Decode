package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

@Config
@TeleOp
public class Test_Spindexer extends OpMode {
	public static double manualPower = 0.0;

	private Spindexer spindexer;
	private ActionScheduler scheduler;
	private Telemetry dashboardTelemetry;

	@Override
	public void init() {
		Spindexer.initialize(hardwareMap);
		spindexer = Spindexer.getInstance();
		scheduler = ActionScheduler.getInstance();

		dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void loop() {
		// Manual control with gamepad
		double manualControlPower = -gamepad1.left_stick_y;

		// Apply manual control
//		scheduler.schedule(spindexer.setDirectPower(manualControlPower));
		scheduler.update();

		// Telemetry
		telemetry.addData("Current Position (ticks)", spindexer.getPosition());
		telemetry.update();
		dashboardTelemetry.addData("Current Position (ticks)", spindexer.getPosition());
		dashboardTelemetry.update();
	}

	@Override
	public void stop() {
		Spindexer.shutdown();
	}
}
