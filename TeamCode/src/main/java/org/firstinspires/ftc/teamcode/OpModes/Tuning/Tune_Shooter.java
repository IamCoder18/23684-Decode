package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.Utilities.PIDFController;
import org.firstinspires.ftc.teamcode.Utilities.FeedForwardController;

@Config
@TeleOp(name = "Tuner - Shooter", group = "Tuning")
public class Tune_Shooter extends OpMode {
	// --- Targets & Sequencing ---
	public static double targetU = 0, targetL = 0;
	private long lastTargetChangeTime = 0;
	private final int[] targetSequence = {0, 500, 1000, 1500, 2000, 2500, 0};
	private int targetIndex = 0;

	// --- PID Gains ---
	public static double UPPER_P = 0.05, UPPER_I = 0, UPPER_D = 0.04;
	public static double LOWER_P = 0.05, LOWER_I = 0, LOWER_D = 0;

	// --- Advanced Feedforward Gains ---
	public static double UPPER_KS = 0, UPPER_KV = 0;
	public static double LOWER_KS = 0, LOWER_KV = 0;

	// --- Controllers ---
	private PIDFController UpperController;
	private PIDFController LowerController;
	private FeedForwardController UpperFeedforward;
	private FeedForwardController LowerFeedforward;

	// --- Hardware & Dashboard ---
	private DcMotorEx upperShooter;
	private DcMotorEx lowerShooter;
	private FtcDashboard dashboard;

	// --- State for Calculations ---
	private final double TICKS_PER_REV = 28.0;
	private double upperRPM = 0, lowerRPM = 0;
	private double lastUpperRPM = 0, lastLowerRPM = 0;
	private long lastLoopTimeNanos = 0;

	@Override
	public void init() {
		upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
		lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

		// Initialize PID controllers (ensure internal F-gain is 0 if it exists)
		UpperController = new PIDFController(UPPER_P, UPPER_I, UPPER_D);
		LowerController = new PIDFController(LOWER_P, LOWER_I, LOWER_D);

		// Initialize our new advanced Feedforward controllers
		UpperFeedforward = new FeedForwardController(UPPER_KS, UPPER_KV, 0);
		LowerFeedforward = new FeedForwardController(LOWER_KS, LOWER_KV, 0);

		// Set output limits for the PID part only
		UpperController.setOutputLimits(0, 1);
		LowerController.setOutputLimits(0, 1);

		dashboard = FtcDashboard.getInstance();
		lastTargetChangeTime = System.nanoTime();
		lastLoopTimeNanos = System.nanoTime();
	}

	@Override
	public void loop() {
		// --- Automatic Target Sequencing for Tuning ---
		long currentTimeNanos = System.nanoTime();
		// 3 seconds
		long CHANGE_INTERVAL_NANOS = 3_000_000_000L;
		if (currentTimeNanos - lastTargetChangeTime >= CHANGE_INTERVAL_NANOS) {
			targetU = targetL = targetSequence[targetIndex];
			targetIndex = (targetIndex + 1) % targetSequence.length;
			lastTargetChangeTime = currentTimeNanos;
		}

		// --- Update Gains from Dashboard ---
		UpperController.setPID(UPPER_P, UPPER_I, UPPER_D);
		LowerController.setPID(LOWER_P, LOWER_I, LOWER_D);
		UpperFeedforward.setGains(UPPER_KS, UPPER_KV, 0);
		LowerFeedforward.setGains(LOWER_KS, LOWER_KV, 0);

		// --- Calculations ---
		// 1. Get current velocity in RPM
		upperRPM = (upperShooter.getVelocity() / TICKS_PER_REV) * 60;
		lowerRPM = (lowerShooter.getVelocity() / TICKS_PER_REV) * 60;

		// 2. Calculate acceleration
		double deltaTimeSeconds = (currentTimeNanos - lastLoopTimeNanos) / 1_000_000_000.0;
		double upperAccelRpmPerSec = (deltaTimeSeconds > 0) ? (upperRPM - lastUpperRPM) / deltaTimeSeconds : 0;
		double lowerAccelRpmPerSec = (deltaTimeSeconds > 0) ? (lowerRPM - lastLowerRPM) / deltaTimeSeconds : 0;

		// --- Power Calculation: Feedforward + PID ---
		// 3. Get the predictive power from the feedforward model
		double upperFeedforwardPower = UpperFeedforward.calculate(targetU, 0);
		double lowerFeedforwardPower = LowerFeedforward.calculate(targetL, 0);

		// 4. Get the reactive correction from the PID controller
		double upperPidPower = UpperController.getOutput(upperRPM, targetU);
		double lowerPidPower = LowerController.getOutput(lowerRPM, targetL);

		// 5. Combine for the final motor power
		double upperTotalPower = upperFeedforwardPower + upperPidPower;
		double lowerTotalPower = lowerFeedforwardPower + lowerPidPower;

		// --- Set Motor Power ---
		upperShooter.setPower(upperTotalPower);
		lowerShooter.setPower(lowerTotalPower);

		// --- Update State for Next Loop ---
		lastUpperRPM = upperRPM;
		lastLowerRPM = lowerRPM;
		lastLoopTimeNanos = currentTimeNanos;

		// --- Telemetry ---
		dashboard.getTelemetry().addData("Target RPM", targetU);
		dashboard.getTelemetry().addData("Upper RPM", upperRPM);
		dashboard.getTelemetry().addData("Lower RPM", lowerRPM);
		dashboard.getTelemetry().addData("Upper Accel (RPM/s)", upperAccelRpmPerSec);
		dashboard.getTelemetry().addData("FF Power", upperFeedforwardPower);
		dashboard.getTelemetry().addData("PID Power", upperPidPower);
		dashboard.getTelemetry().addData("Total Power", upperTotalPower);
		dashboard.getTelemetry().update();
	}
}
