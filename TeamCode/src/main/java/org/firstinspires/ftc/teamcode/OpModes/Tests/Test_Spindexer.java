package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.SubsystemUpdater;
import org.firstinspires.ftc.teamcode.Subsystems.RobotState;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

@Config
@TeleOp
public class Test_Spindexer extends OpMode {
	public static double manualPower = 0.0;

	private Spindexer spindexer;
	private ActionScheduler scheduler;
	private Telemetry dashboardTelemetry;
	// Button state tracking
	private boolean prevA = false;
	private boolean prevB = false;
	private boolean prevX = false;

	@Override
	public void init() {
		HardwareInitializer.initialize(hardwareMap); // Use centralized initializer
		spindexer = Spindexer.getInstance();
		scheduler = ActionScheduler.getInstance();

		dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

		spindexer.resetCalibrationAverage();

		telemetry.addData("Status", "Initialized");
		telemetry.update();
	}

	@Override
	public void init_loop() {
		spindexer.updateCalibrationAverage();
	}

	@Override
	public void start() {
		spindexer.finalizeAutoCalibration();
	}

	@Override
	public void loop() {
		SubsystemUpdater.update();
		scheduler.update();

		// Telemetry
		telemetry.addData("Current Position (degrees)", spindexer.getCalibratedPosition());
		RobotState state = RobotState.getInstance();
		telemetry.addData("Absolute Offset", String.format("%.2f°", state.absoluteOffset));
		telemetry.addData("Quadrature Position", String.format("%.2f", spindexer.getQuadraturePosition()));
		telemetry.addData("Average Quality", String.format("%.2f sec", state.averageQuality));
		telemetry.addData("Has Valid Data", state.hasValidData);
		telemetry.update();
		dashboardTelemetry.addData("Current Position (degrees)", spindexer.getCalibratedPosition());
		dashboardTelemetry.addData("Absolute Offset", state.absoluteOffset);
		dashboardTelemetry.addData("Average Quality", state.averageQuality);
		dashboardTelemetry.addData("Has Valid Data", state.hasValidData);
		dashboardTelemetry.update();
	}

	@Override
	public void stop() {
		HardwareShutdown.shutdown();
	}
}
