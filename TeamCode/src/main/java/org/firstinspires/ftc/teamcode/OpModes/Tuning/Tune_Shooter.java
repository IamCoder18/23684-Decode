package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

@Config
@TeleOp
public class Tune_Shooter extends OpMode {
	public static double targetU = 0, targetL = 0;
	public static double UPPER_P = 0.05, UPPER_I = 0, UPPER_D = 0.04, UPPER_F = 0;
	public static double LOWER_P = 0.05, LOWER_I = 0, LOWER_D = 0, LOWER_F = 0;
	PIDFController UController;
	PIDFController LController;
	DcMotorEx upperShooter;
	DcMotorEx lowerShooter;
	FtcDashboard dashboard;
	double upperRPM = 0;
	double lowerRPM = 0;
	double TICKS_PER_REV = 28;
	
	long lastTargetChangeTime = 0;
	int[] targetSequence = {0, 500, 0, 1000, 0, 2000, 0, 2500};
	int targetIndex = 0;
	long CHANGE_INTERVAL_NANOS = 3_000_000_000L; // 3 seconds in nanoseconds

	@Override
	public void init() {
		upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
		lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

		UController = new PIDFController(UPPER_P, UPPER_I, UPPER_D, UPPER_F);
		LController = new PIDFController(LOWER_P, LOWER_I, LOWER_D, LOWER_F);

		UController.setOutputLimits(0, 1);
		LController.setOutputLimits(0, 1);

		dashboard = FtcDashboard.getInstance();
		lastTargetChangeTime = System.nanoTime();
	}

	@Override
	public void loop() {
		// Update target every 5 seconds (non-blocking)
		long currentTime = System.nanoTime();
		if (currentTime - lastTargetChangeTime >= CHANGE_INTERVAL_NANOS) {
			targetU = targetL = targetSequence[targetIndex];
			targetIndex = (targetIndex + 1) % targetSequence.length;
			lastTargetChangeTime = currentTime;
		}
		
		UController.setPID(UPPER_P, UPPER_I, UPPER_D, UPPER_F);
		LController.setPID(LOWER_P, LOWER_I, LOWER_D, LOWER_F);

		double upperVelocity = upperShooter.getVelocity();
		double lowerVelocity = lowerShooter.getVelocity();
		upperRPM = (upperVelocity / TICKS_PER_REV) * 60;
		lowerRPM = (lowerVelocity / TICKS_PER_REV) * 60;

		double upperPower = UController.getOutput(upperRPM, targetU);
		double lowerPower = LController.getOutput(lowerRPM, targetL);

		upperShooter.setPower(upperPower);
		lowerShooter.setPower(lowerPower);

		dashboard.getTelemetry().addData("Current Upper RPM", upperRPM);
		dashboard.getTelemetry().addData("Current Lower RPM", lowerRPM);
		dashboard.getTelemetry().addData("Target Upper RPM", targetU);
		dashboard.getTelemetry().addData("Target Lower RPM", targetL);
		dashboard.getTelemetry().update();
	}
}
