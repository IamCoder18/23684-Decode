package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

@Config
public class Shooter {
	// --- PIDF Controller Constants ---
	public static double UPPER_P = 0.05, UPPER_I = 0, UPPER_D = 0.04, UPPER_F = 0;
	public static double LOWER_P = 0.05, LOWER_I = 0, LOWER_D = 0, LOWER_F = 0;

	// --- Motor Power Constants ---
	/**
	 * The power level to completely stop the motors.
	 */
	public static double STOP_POWER = 0.0;

	// --- RPM & Control Constants ---
	/**
	 * The tolerance for the PIDF controller. The shooter is considered "at target speed"
	 * if the RPM is within `targetRPM +/- RPM_TOLERANCE`. A value of 100 is a good starting point.
	 */
	public static double RPM_TOLERANCE = 1.0;
	/**
	 * The number of encoder ticks per single revolution of the shooter motor's output shaft.
	 */
	public static double TICKS_PER_REVOLUTION = 28.0;
	/**
	 * The RPM required for shooting artifacts from the audience-side shooting zone.
	 */
	public static double AUDIENCE_RPM = 2300.0;

	// --- Motor Offsets ---
	// Minor power adjustments to balance any speed differences between the two motors.
	public static double UPPER_OFFSET = 0.0;
	public static double LOWER_OFFSET = 0.0;

	// --- Singleton Instance ---
	private static Shooter instance = null;

	// --- Motor & State Variables ---
	private DcMotorEx upperShooter;
	private DcMotorEx lowerShooter;
	public double averageRPM = 0.0;
	public double upperRPM = 0.0;
	public double lowerRPM = 0.0;

	// --- PIDF Controllers ---
	private PIDFController upperController;
	private PIDFController lowerController;

	private Shooter() {}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Shooter();
			instance.upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
			instance.lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

			// Set zero power behavior. BRAKE helps motors stop faster and resist movement.
			instance.upperShooter.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
			instance.lowerShooter.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

			// Initialize PIDF controllers
			instance.upperController = new PIDFController(UPPER_P, UPPER_I, UPPER_D, UPPER_F);
			instance.lowerController = new PIDFController(LOWER_P, LOWER_I, LOWER_D, LOWER_F);
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
			// Ensure motors are stopped when the op-mode ends.
			instance.upperShooter.setPower(STOP_POWER);
			instance.lowerShooter.setPower(STOP_POWER);
		}
	}

	/**
	 * Updates the RPM readings for both shooter motors based on their velocity.
	 * This should be called continuously in your main robot loop to provide feedback for the controller.
	 */
	public void updateRPM() {
		double upperVelocity = upperShooter.getVelocity(); // ticks per second
		double lowerVelocity = lowerShooter.getVelocity(); // ticks per second

		upperRPM = (upperVelocity / TICKS_PER_REVOLUTION) * 60.0;
		lowerRPM = (lowerVelocity / TICKS_PER_REVOLUTION) * 60.0;

		averageRPM = (upperRPM + lowerRPM) / 2.0;
	}

	/**
	 * Checks if the shooter's average RPM is within the tolerance band of a given target.
	 * Use this method to determine if it's safe to feed a note for shooting.
	 * @param targetRPM The RPM you are aiming for.
	 * @return true if the current averageRPM is within the tolerance, false otherwise.
	 */
	public boolean isAtTargetRPM(double targetRPM) {
		return Math.abs(averageRPM - targetRPM) <= RPM_TOLERANCE;
	}

	/**
	 * Returns an Action that runs the shooter motors to maintain a target RPM using separate PIDF controllers.
	 * Each motor is controlled independently based on its own RPM feedback.
	 * This action must be run continuously and will maintain the target speed until cancelled.
	 *
	 * @param targetRPM The desired revolutions per minute for the shooter.
	 * @return Action that runs the shooter with independent PIDF controllers.
	 */
	public Action run(double targetRPM) {
		return packet -> {
			// First, get the latest RPM reading. This is the "feedback" part of the loop.
			updateRPM();

			double upperPower, lowerPower;

			// If target RPM is under 100, stop the motors
			if (targetRPM < 100) {
				upperPower = STOP_POWER;
				lowerPower = STOP_POWER;
			} else {
				// Get power output from PIDF controllers
				upperPower = upperController.getOutput(upperRPM, targetRPM);
				lowerPower = lowerController.getOutput(lowerRPM, targetRPM);
			}

			upperShooter.setPower(upperPower + UPPER_OFFSET);
			lowerShooter.setPower(lowerPower + LOWER_OFFSET);

			// Optional: Add telemetry for debugging via FTC Dashboard
			packet.put("Shooter Target RPM", targetRPM);
			packet.put("Shooter Upper RPM", upperRPM);
			packet.put("Shooter Lower RPM", lowerRPM);
			packet.put("Shooter Average RPM", averageRPM);
			packet.put("Upper Motor Power", upperPower);
			packet.put("Lower Motor Power", lowerPower);
			packet.put("Shooter At Target", isAtTargetRPM(targetRPM));

			return false;
		};
	}

	/**
	 * Returns an InstantAction that immediately stops both shooter motors.
	 *
	 * @return Action that stops both shooter motors.
	 */
	public Action stop() {
		return new InstantAction(() -> {
			upperShooter.setPower(STOP_POWER);
			lowerShooter.setPower(STOP_POWER);
		});
	}
}
