package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Shooter {
	// --- Motor Power Constants ---
	/**
	 * The power level for the "on" state of the bang-bang controller. This is the power
	 * used when the shooter's RPM is below the target range.
	 */
	public static double BANG_BANG_HIGH_POWER = 0.85;
	/**
	 * The power level for the "off" or "idle" state of the bang-bang controller. This is the
	 * minimum power applied when the RPM is above the target range to keep it spinning.
	 */
	public static double BANG_BANG_LOW_POWER = 0.75;
	/**
	 * The power level to completely stop the motors.
	 */
	public static double STOP_POWER = 0.0;

	// --- RPM & Control Constants ---
	/**
	 * The tolerance for the bang-bang controller. The shooter is considered "at target speed"
	 * if the RPM is within `targetRPM +/- RPM_TOLERANCE`. A value of 100 is a good starting point.
	 */
	public static double RPM_TOLERANCE = 100.0;
	/**
	 * The number of encoder ticks per single revolution of the shooter motor's output shaft.
	 */
	public static double TICKS_PER_REVOLUTION = 28.0;
	/**
	 * The RPM required for shooting artifacts from the audience-side shooting zone.
	 */
	public static double AUDIENCE_RPM = 2400.0;

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

	private Shooter() {}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Shooter();
			instance.upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
			instance.lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");

			// Set zero power behavior. BRAKE helps motors stop faster and resist movement.
			instance.upperShooter.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
			instance.lowerShooter.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
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
	 * Returns an Action that runs the shooter motors to maintain a target RPM using a bang-bang controller.
	 * This action must be run continuously and will maintain the target speed until cancelled.
	 *
	 * @param targetRPM The desired revolutions per minute for the shooter.
	 * @return Action that runs the shooter with a bang-bang controller.
	 */
	public Action run(double targetRPM) {
		return new Action() {
			private boolean highPowerActive = true; // Start by powering up to reach the target.

			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
				// First, get the latest RPM reading. This is the "feedback" part of the loop.
				updateRPM();

				// --- Bang-Bang Controller Logic ---
				// If RPM is too low, activate high power mode.
				if (averageRPM < targetRPM - RPM_TOLERANCE) {
					highPowerActive = true;
				}
				// If RPM is too high, switch to low power (idle) mode.
				else if (averageRPM > targetRPM + RPM_TOLERANCE) {
					highPowerActive = false;
				}
				// If inside the tolerance band, the state (highPowerActive) does not change.
				// This prevents the motor from rapidly switching on/off (chattering).

				// Set power based on the controller's state.
				// The power will never be negative as both constants are positive.
				double power = highPowerActive ? BANG_BANG_HIGH_POWER : BANG_BANG_LOW_POWER;

				upperShooter.setPower(power + UPPER_OFFSET);
				lowerShooter.setPower(power + LOWER_OFFSET);

				// Optional: Add telemetry for debugging via FTC Dashboard
				packet.put("Shooter Target RPM", targetRPM);
				packet.put("Shooter Average RPM", averageRPM);
				packet.put("Shooter Power", power);
				packet.put("Shooter At Target", isAtTargetRPM(targetRPM));

				return false;
			}
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
