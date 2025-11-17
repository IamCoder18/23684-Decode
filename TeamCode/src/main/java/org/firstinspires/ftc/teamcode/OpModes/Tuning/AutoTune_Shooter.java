package org.firstinspires.ftc.teamcode.OpModes.Tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.ArrayList;
import java.util.List;

@Config
@TeleOp(name = "Auto Tuner - Shooter", group = "Tuning")
public class AutoTune_Shooter extends OpMode {

	// --- Configuration ---
	public static double RAMP_DURATION_SECONDS = 20.0;
	public static double RAMP_MAX_POWER = 0.8;
	public static double STEP_TARGET_RPM = 2000;
	public static double STEP_DURATION_SECONDS = 15.0;
	public static int MIN_SAMPLES = 100;
	public static double VELOCITY_THRESHOLD_RPM = 50;

	private int overshootAttemptsUpper = 0;
	private int overshootAttemptsLower = 0;
	private final int MAX_OVERSHOOT_ATTEMPTS = 5;
	private final double OVERSHOOT_TARGET = 10.0;
	private final double OVERSHOOT_GAIN_SCALE = 1.2;

	private enum TuningPhase {
		IDLE,
		RAMP_TEST_RUNNING,
		RAMP_TEST_ANALYZING,
		STEP_TEST_RUNNING,
		STEP_TEST_ANALYZING,
		COMPLETE
	}
	private TuningPhase currentPhase = TuningPhase.IDLE;

	private DcMotorEx upperShooter;
	private DcMotorEx lowerShooter;
	private FtcDashboard dashboard;
	private MultipleTelemetry telemetry;

	private final double TICKS_PER_REV = 28.0;

	private List<Double> rampPowerSamples = new ArrayList<>();
	private List<Double> rampVelocitySamplesUpper = new ArrayList<>();
	private List<Double> rampVelocitySamplesLower = new ArrayList<>();
	private long rampStartTime = 0;

	private List<Double> stepTimeSamples = new ArrayList<>();
	private List<Double> stepVelocitySamplesUpper = new ArrayList<>();
	private List<Double> stepVelocitySamplesLower = new ArrayList<>();
	private long stepStartTime = 0;

	// Upper analysis variables
	private double maxVelocityUpper = 0, steadyStateVelocityUpper = 0, overshootUpper = 0;
	private long riseTimeUpper = 0;
	private double calculatedKSUpper = 0, calculatedKVUpper = 0, calculatedKPUpper = 0, calculatedKIUpper = 0, calculatedKDUpper = 0;

	// Lower analysis variables
	private double maxVelocityLower = 0, steadyStateVelocityLower = 0, overshootLower = 0;
	private long riseTimeLower = 0;
	private double calculatedKSLower = 0, calculatedKVLower = 0, calculatedKPLower = 0, calculatedKILower = 0, calculatedKDLower = 0;

	private double currentRPMUpper = 0, currentRPMLower = 0, currentPower = 0;

	private List<String> persistentTelemetry = new ArrayList<>();

	@Override
	public void init() {
		upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
		lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

		dashboard = FtcDashboard.getInstance();
		telemetry = new MultipleTelemetry(super.telemetry, dashboard.getTelemetry());

		telemetry.addLine("=== AUTOMATED SHOOTER TUNER ===");
		telemetry.addLine();
		telemetry.addLine("Instructions:");
		telemetry.addLine("• Press A to start RAMP TEST (feedforward)");
		telemetry.addLine("• Press B to start STEP TEST (PID)");
		telemetry.addLine("• Press X to reset and restart");
		telemetry.addLine();
		telemetry.addLine("Ready to begin tuning...");
		telemetry.update();
	}

	@Override
	public void start() {
		startRampTest();
	}

	@Override
	public void loop() {
		currentRPMUpper = (upperShooter.getVelocity() / TICKS_PER_REV) * 60.0;
		currentRPMLower = (lowerShooter.getVelocity() / TICKS_PER_REV) * 60.0;

		switch (currentPhase) {
			case IDLE: handleIdle(); break;
			case RAMP_TEST_RUNNING: handleRampTest(); break;
			case RAMP_TEST_ANALYZING: handleRampAnalysis(); break;
			case STEP_TEST_RUNNING: handleStepTest(); break;
			case STEP_TEST_ANALYZING: handleStepAnalysis(); break;
			case COMPLETE: handleComplete(); break;
		}
		updateTelemetry();
	}

	private void handleIdle() {
		upperShooter.setPower(0);
		lowerShooter.setPower(0);

		if (gamepad1.a) startRampTest();
		else if (gamepad1.b && calculatedKVUpper > 0 && calculatedKVLower > 0) startStepTest();
		else if (gamepad1.x) resetTuning();
	}

	private void startRampTest() {
		currentPhase = TuningPhase.RAMP_TEST_RUNNING;
		rampStartTime = System.nanoTime();
		rampPowerSamples.clear();
		rampVelocitySamplesUpper.clear();
		rampVelocitySamplesLower.clear();
		telemetry.addLine("Starting RAMP TEST...");
		telemetry.update();
	}

	private void handleRampTest() {
		long currentTime = System.nanoTime();
		double elapsedSeconds = (currentTime - rampStartTime) / 1e9;

		if (elapsedSeconds < RAMP_DURATION_SECONDS) {
			currentPower = (elapsedSeconds / RAMP_DURATION_SECONDS) * RAMP_MAX_POWER;
			upperShooter.setPower(currentPower);
			lowerShooter.setPower(currentPower);

			if (currentRPMUpper > VELOCITY_THRESHOLD_RPM && currentRPMLower > VELOCITY_THRESHOLD_RPM) {
				rampPowerSamples.add(currentPower);
				rampVelocitySamplesUpper.add(currentRPMUpper);
				rampVelocitySamplesLower.add(currentRPMLower);
			}
		} else {
			upperShooter.setPower(0);
			lowerShooter.setPower(0);
			currentPhase = TuningPhase.RAMP_TEST_ANALYZING;
		}
	}

	private void handleRampAnalysis() {
		if (rampPowerSamples.size() < MIN_SAMPLES) {
			persistentTelemetry.clear();
			persistentTelemetry.add("ERROR: Not enough samples collected!");
			persistentTelemetry.add("Try increasing RAMP_DURATION_SECONDS");
			currentPhase = TuningPhase.IDLE;
			return;
		}

		// Upper regression
		LinearRegression regressionUpper = new LinearRegression(
				listToArray(rampPowerSamples), listToArray(rampVelocitySamplesUpper));
		calculatedKSUpper = regressionUpper.intercept();
		calculatedKVUpper = regressionUpper.slope();
		double kV_ffUpper = 1.0 / calculatedKVUpper;
		double kS_ffUpper = -calculatedKSUpper / calculatedKVUpper;

		// Lower regression
		LinearRegression regressionLower = new LinearRegression(
				listToArray(rampPowerSamples), listToArray(rampVelocitySamplesLower));
		calculatedKSLower = regressionLower.intercept();
		calculatedKVLower = regressionLower.slope();
		double kV_ffLower = 1.0 / calculatedKVLower;
		double kS_ffLower = -calculatedKSLower / calculatedKVLower;

		persistentTelemetry.clear();
		persistentTelemetry.add("=== RAMP TEST RESULTS ===");
		persistentTelemetry.add("Upper Shooter:");
		persistentTelemetry.add(String.format("  kS: %.6f", kS_ffUpper));
		persistentTelemetry.add(String.format("  kV: %.6f", kV_ffUpper));
		persistentTelemetry.add("");
		persistentTelemetry.add("Lower Shooter:");
		persistentTelemetry.add(String.format("  kS: %.6f", kS_ffLower));
		persistentTelemetry.add(String.format("  kV: %.6f", kV_ffLower));
		persistentTelemetry.add("");
		persistentTelemetry.add("Set these in your shooter subsystem!");
		persistentTelemetry.add("");
		persistentTelemetry.add("Press B for STEP TEST or X to restart");

		// Store new tunings for the step
		calculatedKSUpper = kS_ffUpper;
		calculatedKVUpper = kV_ffUpper;
		calculatedKSLower = kS_ffLower;
		calculatedKVLower = kV_ffLower;

		startStepTest();
	}

	private void startStepTest() {
		currentPhase = TuningPhase.STEP_TEST_RUNNING;
		stepStartTime = System.nanoTime();
		stepTimeSamples.clear();
		stepVelocitySamplesUpper.clear();
		stepVelocitySamplesLower.clear();
		maxVelocityUpper = 0; steadyStateVelocityUpper = 0; riseTimeUpper = 0;
		maxVelocityLower = 0; steadyStateVelocityLower = 0; riseTimeLower = 0;
		overshootUpper = 0; overshootLower = 0;
		telemetry.addLine("Starting STEP TEST...");
		telemetry.update();
	}

	private void handleStepTest() {
		long currentTime = System.nanoTime();
		double elapsedSeconds = (currentTime - stepStartTime) / 1e9;

		if (elapsedSeconds < STEP_DURATION_SECONDS) {
			double upperPower = calculatedKSUpper + calculatedKVUpper * STEP_TARGET_RPM;
			double lowerPower = calculatedKSLower + calculatedKVLower * STEP_TARGET_RPM;
			upperShooter.setPower(upperPower);
			lowerShooter.setPower(lowerPower);

			stepTimeSamples.add(elapsedSeconds);
			stepVelocitySamplesUpper.add(currentRPMUpper);
			stepVelocitySamplesLower.add(currentRPMLower);

			if (currentRPMUpper > maxVelocityUpper) maxVelocityUpper = currentRPMUpper;
			if (currentRPMLower > maxVelocityLower) maxVelocityLower = currentRPMLower;

			if (riseTimeUpper == 0 && currentRPMUpper >= 0.9 * STEP_TARGET_RPM)
				riseTimeUpper = currentTime - stepStartTime;
			if (riseTimeLower == 0 && currentRPMLower >= 0.9 * STEP_TARGET_RPM)
				riseTimeLower = currentTime - stepStartTime;

			if (elapsedSeconds > 0.8 * STEP_DURATION_SECONDS) {
				steadyStateVelocityUpper = (steadyStateVelocityUpper + currentRPMUpper) / 2.0;
				steadyStateVelocityLower = (steadyStateVelocityLower + currentRPMLower) / 2.0;
			}
		} else {
			upperShooter.setPower(0);
			lowerShooter.setPower(0);
			currentPhase = TuningPhase.STEP_TEST_ANALYZING;
		}
	}

	private void handleStepAnalysis() {
		if (stepTimeSamples.size() < MIN_SAMPLES) {
			persistentTelemetry.clear();
			persistentTelemetry.add("ERROR: Not enough samples collected!");
			currentPhase = TuningPhase.IDLE;
			return;
		}

		overshootUpper = ((maxVelocityUpper - steadyStateVelocityUpper) / steadyStateVelocityUpper) * 100.0;
		overshootLower = ((maxVelocityLower - steadyStateVelocityLower) / steadyStateVelocityLower) * 100.0;

		double steadyStateErrorUpper = STEP_TARGET_RPM - steadyStateVelocityUpper;
		double steadyStateErrorLower = STEP_TARGET_RPM - steadyStateVelocityLower;
		double steadyStateErrorPercentUpper = (steadyStateErrorUpper / STEP_TARGET_RPM) * 100.0;
		double steadyStateErrorPercentLower = (steadyStateErrorLower / STEP_TARGET_RPM) * 100.0;

		double T90Upper = riseTimeUpper / 1e9;
		double T90Lower = riseTimeLower / 1e9;

		if (overshootAttemptsUpper == 0) {
			calculatedKPUpper = 0.2 / T90Upper;
			calculatedKIUpper = 0;
			calculatedKDUpper = 0.05 / T90Upper;
		}
		if (overshootAttemptsLower == 0) {
			calculatedKPLower = 0.2 / T90Lower;
			calculatedKILower = 0;
			calculatedKDLower = 0.05 / T90Lower;
		}

		persistentTelemetry.clear();
		persistentTelemetry.add("╔════════════════════════════════════════╗");
		persistentTelemetry.add("║      UPPER & LOWER STEP TEST RESULTS  ║");
		persistentTelemetry.add("╚════════════════════════════════════════╝");
		persistentTelemetry.add("");
		persistentTelemetry.add("UPPER SHOOTER:");
		persistentTelemetry.add(String.format("  PID: P=%.4f I=%.4f D=%.4f", calculatedKPUpper, calculatedKIUpper, calculatedKDUpper));
		persistentTelemetry.add(String.format("  kS=%.6f, kV=%.6f", calculatedKSUpper, calculatedKVUpper));
		persistentTelemetry.add(String.format("  Overshoot: %.1f%%", overshootUpper));
		persistentTelemetry.add(String.format("  Steady-State Error: %.1f%%", steadyStateErrorPercentUpper));
		if (overshootUpper > OVERSHOOT_TARGET && overshootAttemptsUpper < MAX_OVERSHOOT_ATTEMPTS) {
			persistentTelemetry.add("  ⚠ Upper: High overshoot - retrying...");
			overshootAttemptsUpper++;
			calculatedKPUpper *= OVERSHOOT_GAIN_SCALE;
			calculatedKDUpper *= OVERSHOOT_GAIN_SCALE;
			startStepTest();
			return;
		}
		persistentTelemetry.add("");

		persistentTelemetry.add("LOWER SHOOTER:");
		persistentTelemetry.add(String.format("  PID: P=%.4f I=%.4f D=%.4f", calculatedKPLower, calculatedKILower, calculatedKDLower));
		persistentTelemetry.add(String.format("  kS=%.6f, kV=%.6f", calculatedKSLower, calculatedKVLower));
		persistentTelemetry.add(String.format("  Overshoot: %.1f%%", overshootLower));
		persistentTelemetry.add(String.format("  Steady-State Error: %.1f%%", steadyStateErrorPercentLower));
		if (overshootLower > OVERSHOOT_TARGET && overshootAttemptsLower < MAX_OVERSHOOT_ATTEMPTS) {
			persistentTelemetry.add("  ⚠ Lower: High overshoot - retrying...");
			overshootAttemptsLower++;
			calculatedKPLower *= OVERSHOOT_GAIN_SCALE;
			calculatedKDLower *= OVERSHOOT_GAIN_SCALE;
			startStepTest();
			return;
		}
		persistentTelemetry.add("");

		persistentTelemetry.add("=== SUGGESTED GAINS ===");
		persistentTelemetry.add(String.format("UPPER_KS = %.6f; UPPER_KV = %.6f;", calculatedKSUpper, calculatedKVUpper));
		persistentTelemetry.add(String.format("UPPER_P = %.4f; UPPER_I = %.4f; UPPER_D = %.4f;",
				calculatedKPUpper, calculatedKIUpper, calculatedKDUpper));
		persistentTelemetry.add(String.format("LOWER_KS = %.6f; LOWER_KV = %.6f;", calculatedKSLower, calculatedKVLower));
		persistentTelemetry.add(String.format("LOWER_P = %.4f; LOWER_I = %.4f; LOWER_D = %.4f;",
				calculatedKPLower, calculatedKILower, calculatedKDLower));
		persistentTelemetry.add("");
		persistentTelemetry.add("Press X to restart");

		currentPhase = TuningPhase.COMPLETE;
	}

	private void handleComplete() {
		upperShooter.setPower(0);
		lowerShooter.setPower(0);
		if (gamepad1.x) resetTuning();
	}

	private void resetTuning() {
		currentPhase = TuningPhase.IDLE;
		rampPowerSamples.clear();
		rampVelocitySamplesUpper.clear();
		rampVelocitySamplesLower.clear();
		stepTimeSamples.clear();
		stepVelocitySamplesUpper.clear();
		stepVelocitySamplesLower.clear();
		calculatedKSUpper = calculatedKVUpper = calculatedKPUpper = calculatedKIUpper = calculatedKDUpper = 0;
		calculatedKSLower = calculatedKVLower = calculatedKPLower = calculatedKILower = calculatedKDLower = 0;
		overshootAttemptsUpper = overshootAttemptsLower = 0;
		telemetry.clear();
		telemetry.addLine("Tuning reset. Ready to begin.");
		telemetry.update();
	}

	private void updateTelemetry() {
		telemetry.clear();
		for (String line : persistentTelemetry) {
			if (line.isEmpty()) telemetry.addLine();
			else telemetry.addLine(line);
		}
		telemetry.addLine();
		telemetry.addData("Current Phase", currentPhase);
		telemetry.addData("Upper RPM", String.format("%.0f", currentRPMUpper));
		telemetry.addData("Lower RPM", String.format("%.0f", currentRPMLower));
		telemetry.addData("Current Power", String.format("%.3f", currentPower));
		if (currentPhase == TuningPhase.RAMP_TEST_RUNNING) {
			telemetry.addData("Samples", rampPowerSamples.size());
		} else if (currentPhase == TuningPhase.STEP_TEST_RUNNING) {
			telemetry.addData("Samples", stepTimeSamples.size());
			long elapsed = System.nanoTime() - stepStartTime;
			telemetry.addData("Time Remaining",
					String.format("%.1f s", STEP_DURATION_SECONDS - (elapsed / 1e9)));
		}
		telemetry.update();
	}

	@Override
	public void stop() {
		upperShooter.setPower(0);
		lowerShooter.setPower(0);
	}

	private double[] listToArray(List<Double> list) {
		double[] array = new double[list.size()];
		for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
		return array;
	}

	private static class LinearRegression {
		private final double intercept;
		private final double slope;
		private final double r2;

		public LinearRegression(double[] x, double[] y) {
			if (x.length != y.length) throw new IllegalArgumentException("Array lengths must match");
			int n = x.length;
			double sumX = 0, sumY = 0;
			for (int i = 0; i < n; i++) { sumX += x[i]; sumY += y[i]; }
			double xBar = sumX / n;
			double yBar = sumY / n;

			double xxBar = 0, yyBar = 0, xyBar = 0;
			for (int i = 0; i < n; i++) {
				xxBar += (x[i] - xBar) * (x[i] - xBar);
				yyBar += (y[i] - yBar) * (y[i] - yBar);
				xyBar += (x[i] - xBar) * (y[i] - yBar);
			}
			slope = xyBar / xxBar;
			intercept = yBar - slope * xBar;

			double rss = 0;
			for (int i = 0; i < n; i++) {
				double fit = slope * x[i] + intercept;
				rss += (fit - y[i]) * (fit - y[i]);
			}
			r2 = 1.0 - (rss / yyBar);
		}
		public double intercept() { return intercept; }
		public double slope() { return slope; }
		public double R2() { return r2; }
	}
}
