package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;
import org.firstinspires.ftc.teamcode.Utilities.FeedForwardController;

@Config
public class Shooter {
	// --- PID Controller Constants ---
	public static double UPPER_P = 0.0024, UPPER_I = 0, UPPER_D = 0;
	public static double LOWER_P = 0.002, LOWER_I = 0, LOWER_D = 0;

	// --- Feedforward Constants (Set these from your auto-tuner results) ---
	public static double UPPER_KS = 0.28, UPPER_KV = 0.000095;
	public static double LOWER_KS = 0.29, LOWER_KV = 0.00017;

	// --- Motor Power Constants ---
	public static double STOP_POWER = 0.0;

	// --- RPM & Control Constants ---
	public static double RPM_TOLERANCE = 100.0;
	public static double TICKS_PER_REVOLUTION = 28.0;
	public static double AUDIENCE_RPM = 2600.0;
	public static double AUDIENCE_SHOT_RPM = 2500.0;

	// --- Motor Offsets ---
	public static double UPPER_OFFSET = 0.0;
	public static double LOWER_OFFSET = 0.0;

	private static Shooter instance = null;

	private DcMotorEx upperShooter;
	private DcMotorEx lowerShooter;
	public double averageRPM = 0.0;
	public double upperRPM = 0.0;
	public double lowerRPM = 0.0;
	public double upperAcceleration = 0.0;
	public double lowerAcceleration = 0.0;
	public double averageAcceleration = 0.0;

	private long lastNanoTime = 0;
	private double lastUpperVelocity = 0.0;
	private double lastLowerVelocity = 0.0;

	private PIDFController upperController;
	private PIDFController lowerController;
	private FeedForwardController upperFF;
	private FeedForwardController lowerFF;

	private Shooter() {}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Shooter();
			instance.upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
			instance.lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

			instance.upperShooter.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
			instance.lowerShooter.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

			instance.upperController = new PIDFController(UPPER_P, UPPER_I, UPPER_D, 0);
			instance.lowerController = new PIDFController(LOWER_P, LOWER_I, LOWER_D, 0);

			// --- Init FeedForward controllers with tuned gains
			instance.upperFF = new FeedForwardController(UPPER_KS, UPPER_KV, 0);
			instance.lowerFF = new FeedForwardController(LOWER_KS, LOWER_KV, 0);
		}
	}

	public static Shooter getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Shooter not initialized. Call initialize(hardwareMap) first.");
		}
		return instance;
	}

	public static void shutdown() {
		if(instance != null) {
			instance.upperShooter.setPower(STOP_POWER);
			instance.lowerShooter.setPower(STOP_POWER);
		}
	}

	public void updateRPM(long nanoTime) {
		double upperVelocity = upperShooter.getVelocity();
		double lowerVelocity = lowerShooter.getVelocity();

		upperRPM = (upperVelocity / TICKS_PER_REVOLUTION) * 60.0;
		lowerRPM = (lowerVelocity / TICKS_PER_REVOLUTION) * 60.0;
		averageRPM = (upperRPM + lowerRPM) / 2.0;

		if (lastNanoTime != 0) {
			double timeElapsedSeconds = (nanoTime - lastNanoTime) / 1_000_000_000.0;
			if (timeElapsedSeconds > 0) {
				upperAcceleration = (upperVelocity - lastUpperVelocity) / timeElapsedSeconds;
				lowerAcceleration = (lowerVelocity - lastLowerVelocity) / timeElapsedSeconds;
				averageAcceleration = (upperAcceleration + lowerAcceleration) / 2.0;
			}
		}
		lastNanoTime = nanoTime;
		lastUpperVelocity = upperVelocity;
		lastLowerVelocity = lowerVelocity;
	}

	public boolean isAtTargetRPM(double targetRPM) {
		return !(targetRPM < 100) && Math.abs(averageRPM - targetRPM) <= RPM_TOLERANCE;
	}

	public boolean isAtTargetRPM(double lowerTargetRPM, double upperTargetRPM) {
		return (!(lowerTargetRPM < 100) && Math.abs(lowerRPM - lowerTargetRPM) <= RPM_TOLERANCE) && (!(upperTargetRPM < 100) && Math.abs(upperRPM - upperTargetRPM) <= RPM_TOLERANCE);
	}

	/**
	 * Returns an Action that runs the shooter motors to maintain a target RPM
	 * with sum of PID and Feedforward controllers.
	 */
	public Action run(double targetRPM) {
		return packet -> {
			updateRPM(System.nanoTime());

			double upperPower, lowerPower;
			double upperFFPower = upperFF.calculate(targetRPM, 0);
			double lowerFFPower = lowerFF.calculate(targetRPM, 0);

			if (targetRPM < 100) {
				upperPower = STOP_POWER;
				lowerPower = STOP_POWER;
			} else {
				double upperPidPower = upperController.getOutput(upperRPM, targetRPM);
				double lowerPidPower = lowerController.getOutput(lowerRPM, targetRPM);

				// Final output: sum of feedforward and PID corrections
				upperPower = upperFFPower + upperPidPower + UPPER_OFFSET;
				lowerPower = lowerFFPower + lowerPidPower + LOWER_OFFSET;
			}

			upperShooter.setPower(upperPower);
			lowerShooter.setPower(lowerPower);

			// Telemetry for dashboard analysis
			packet.put("Shooter Target RPM", targetRPM);
			packet.put("Shooter Upper RPM", upperRPM);
			packet.put("Shooter Lower RPM", lowerRPM);
			packet.put("Shooter Average RPM", averageRPM);
			packet.put("Upper Motor Power", upperPower);
			packet.put("Lower Motor Power", lowerPower);
			packet.put("Upper Feedforward", upperFFPower);
			packet.put("Upper PID", upperController.getOutput(upperRPM, targetRPM));
			packet.put("Lower Feedforward", lowerFFPower);
			packet.put("Lower PID", lowerController.getOutput(lowerRPM, targetRPM));
			packet.put("Shooter At Target", isAtTargetRPM(targetRPM));

			return false;
		};
	}

	/**
	 * Returns an Action that runs the shooter motors and waits until:
	 * 1. averageRPM is within RPM_TOLERANCE of target RPM
	 * 2. Then averageRPM drops below AUDIENCE_SHOT_RPM
	 * Returns true while waiting, false when complete.
	 */
	public Action runAndWait(double targetRPM) {
		return packet -> {
			updateRPM(System.nanoTime());

			double upperPower, lowerPower;
			double upperFFPower = upperFF.calculate(targetRPM, 0);
			double lowerFFPower = lowerFF.calculate(targetRPM, 0);

			if (targetRPM < 100) {
				upperPower = STOP_POWER;
				lowerPower = STOP_POWER;
			} else {
				double upperPidPower = upperController.getOutput(upperRPM, targetRPM);
				double lowerPidPower = lowerController.getOutput(lowerRPM, targetRPM);

				// Final output: sum of feedforward and PID corrections
				upperPower = upperFFPower + upperPidPower + UPPER_OFFSET;
				lowerPower = lowerFFPower + lowerPidPower + LOWER_OFFSET;
			}

			upperShooter.setPower(upperPower);
			lowerShooter.setPower(lowerPower);

			// Telemetry for dashboard analysis
			packet.put("Shooter Target RPM", targetRPM);
			packet.put("Shooter Upper RPM", upperRPM);
			packet.put("Shooter Lower RPM", lowerRPM);
			packet.put("Shooter Average RPM", averageRPM);
			packet.put("Upper Motor Power", upperPower);
			packet.put("Lower Motor Power", lowerPower);
			packet.put("Upper Feedforward", upperFFPower);
			packet.put("Upper PID", upperController.getOutput(upperRPM, targetRPM));
			packet.put("Lower Feedforward", lowerFFPower);
			packet.put("Lower PID", lowerController.getOutput(lowerRPM, targetRPM));
			packet.put("Shooter At Target", isAtTargetRPM(targetRPM));

			// Return true while waiting (at target RPM), false once RPM drops below AUDIENCE_SHOT_RPM
			if (isAtTargetRPM(targetRPM)) {
				return true;
			}
			return averageRPM >= AUDIENCE_SHOT_RPM;
		};
	}

	/**
	 * Returns an Action that runs the shooter motors with separate target RPMs for upper and lower
	 */
	public Action run(double upperTargetRPM, double lowerTargetRPM) {
		return packet -> {
			updateRPM(System.nanoTime());

			double upperPower, lowerPower;
			double upperFFPower = upperFF.calculate(upperTargetRPM, 0);
			double lowerFFPower = lowerFF.calculate(lowerTargetRPM, 0);

			if (upperTargetRPM < 100) {
				upperPower = STOP_POWER;
			} else {
				double upperPidPower = upperController.getOutput(upperRPM, upperTargetRPM);
				upperPower = upperFFPower + upperPidPower + UPPER_OFFSET;
			}

			if (lowerTargetRPM < 100) {
				lowerPower = STOP_POWER;
			} else {
				double lowerPidPower = lowerController.getOutput(lowerRPM, lowerTargetRPM);
				lowerPower = lowerFFPower + lowerPidPower + LOWER_OFFSET;
			}

			upperShooter.setPower(upperPower);
			lowerShooter.setPower(lowerPower);

			// Telemetry for dashboard analysis
			packet.put("Shooter Upper Target RPM", upperTargetRPM);
			packet.put("Shooter Lower Target RPM", lowerTargetRPM);
			packet.put("Shooter Upper RPM", upperRPM);
			packet.put("Shooter Lower RPM", lowerRPM);
			packet.put("Upper Motor Power", upperPower);
			packet.put("Lower Motor Power", lowerPower);
			packet.put("Upper Feedforward", upperFFPower);
			packet.put("Upper PID", upperController.getOutput(upperRPM, upperTargetRPM));
			packet.put("Lower Feedforward", lowerFFPower);
			packet.put("Lower PID", lowerController.getOutput(lowerRPM, lowerTargetRPM));

			return false;
		};
	}

	/**
	 * Returns an Action that runs the shooter motors with separate targets and waits until both are at target
	 */
	public Action runAndWait(double upperTargetRPM, double lowerTargetRPM) {
		return packet -> {
			updateRPM(System.nanoTime());

			double upperPower, lowerPower;
			double upperFFPower = upperFF.calculate(upperTargetRPM, 0);
			double lowerFFPower = lowerFF.calculate(lowerTargetRPM, 0);

			if (upperTargetRPM < 100) {
				upperPower = STOP_POWER;
			} else {
				double upperPidPower = upperController.getOutput(upperRPM, upperTargetRPM);
				upperPower = upperFFPower + upperPidPower + UPPER_OFFSET;
			}

			if (lowerTargetRPM < 100) {
				lowerPower = STOP_POWER;
			} else {
				double lowerPidPower = lowerController.getOutput(lowerRPM, lowerTargetRPM);
				lowerPower = lowerFFPower + lowerPidPower + LOWER_OFFSET;
			}

			upperShooter.setPower(upperPower);
			lowerShooter.setPower(lowerPower);

			// Telemetry for dashboard analysis
			packet.put("Shooter Upper Target RPM", upperTargetRPM);
			packet.put("Shooter Lower Target RPM", lowerTargetRPM);
			packet.put("Shooter Upper RPM", upperRPM);
			packet.put("Shooter Lower RPM", lowerRPM);
			packet.put("Upper Motor Power", upperPower);
			packet.put("Lower Motor Power", lowerPower);
			packet.put("Upper Feedforward", upperFFPower);
			packet.put("Upper PID", upperController.getOutput(upperRPM, upperTargetRPM));
			packet.put("Lower Feedforward", lowerFFPower);
			packet.put("Lower PID", lowerController.getOutput(lowerRPM, lowerTargetRPM));

			// Return true while waiting, false once both are below AUDIENCE_SHOT_RPM
			boolean upperAtTarget = !(upperTargetRPM < 100) && Math.abs(upperRPM - upperTargetRPM) <= RPM_TOLERANCE;
			boolean lowerAtTarget = !(lowerTargetRPM < 100) && Math.abs(lowerRPM - lowerTargetRPM) <= RPM_TOLERANCE;

			if (upperAtTarget && lowerAtTarget) {
				return true;
			}
			return upperRPM >= AUDIENCE_SHOT_RPM || lowerRPM >= AUDIENCE_SHOT_RPM;
		};
	}

	public Action stop() {
		return new InstantAction(() -> {
			upperShooter.setPower(STOP_POWER);
			lowerShooter.setPower(STOP_POWER);
		});
	}
}
