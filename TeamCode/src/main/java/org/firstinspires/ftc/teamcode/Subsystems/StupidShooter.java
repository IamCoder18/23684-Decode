package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class StupidShooter {
	public static double TICKS_PER_REV = 28;
	public double averageRPM = 0;
	DcMotorEx upperShooter;
	DcMotorEx lowerShooter;
	double upperRPM;
	double lowerRPM;
	double needForSpeed = 2400;

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
	public static double BANG_BANG_LOW_POWER = 0.7;
	/**
	 * The power level to completely stop the motors.
	 */
	public static double STOP_POWER = 0.0;

	// --- RPM & Control Constants ---
	/**
	 * The tolerance for the bang-bang controller. The shooter is considered "at target speed"
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
	public static double AUDIENCE_RPM = 2400.0;

	// --- Motor Offsets ---
	// Minor power adjustments to balance any speed differences between the two motors.
	public static double UPPER_OFFSET = 0.0;
	public static double LOWER_OFFSET = 0.0;

	public StupidShooter(HardwareMap hardwareMap) {
		upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
		lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");
	}

	public void updateRPM() {
		double upperVelocity = upperShooter.getVelocity();
		double lowerVelocity = lowerShooter.getVelocity();

		upperRPM = (upperVelocity / TICKS_PER_REV) * 60;
		lowerRPM = (lowerVelocity / TICKS_PER_REV) * 60;

		averageRPM = (upperRPM + lowerRPM) / 2;
	}

	public Action WindUp() {
		return new WindUp();
	}

	public Action Stop() {
		return new Stop();
	}

	public Action WaitForSpike() {
		return new WaitForSpike();
	}

	public class WindUp implements Action {
		boolean initialized = false;

		@Override
		public boolean run(@NonNull TelemetryPacket telemetryPacket) {
			updateRPM();

			boolean upperHighPowerActive = true; // Start by powering up to reach the target.
			boolean lowerHighPowerActive = true; // Start by powering up to reach the target.

			// First, get the latest RPM reading. This is the "feedback" part of the loop.
			updateRPM();

			// --- Upper Motor Bang-Bang Controller Logic ---
			// If RPM is too low, activate high power mode.
			if (upperRPM < needForSpeed - RPM_TOLERANCE) {
				upperHighPowerActive = true;
			}
			// If RPM is too high, switch to low power (idle) mode.
			else if (upperRPM > needForSpeed + RPM_TOLERANCE) {
				upperHighPowerActive = false;
			}
			// If inside the tolerance band, the state does not change.
			// This prevents the motor from rapidly switching on/off (chattering).

			// --- Lower Motor Bang-Bang Controller Logic ---
			// If RPM is too low, activate high power mode.
			if (lowerRPM < needForSpeed - RPM_TOLERANCE) {
				lowerHighPowerActive = true;
			}
			// If RPM is too high, switch to low power (idle) mode.
			else if (lowerRPM > needForSpeed + RPM_TOLERANCE) {
				lowerHighPowerActive = false;
			}
			// If inside the tolerance band, the state does not change.

			// Set power for each motor based on its controller's state.
			double upperPower = upperHighPowerActive ? BANG_BANG_HIGH_POWER : BANG_BANG_LOW_POWER;
			double lowerPower = lowerHighPowerActive ? BANG_BANG_HIGH_POWER : BANG_BANG_LOW_POWER;

			upperShooter.setPower(upperPower + UPPER_OFFSET);
			lowerShooter.setPower(lowerPower + LOWER_OFFSET);

			return averageRPM < needForSpeed;
		}
	}

	public class WaitForSpike implements Action {
		@Override
		public boolean run(@NonNull TelemetryPacket telemetryPacket) {
			updateRPM();
			return averageRPM > needForSpeed;
		}
	}

	public class Stop implements Action {
		@Override
		public boolean run(@NonNull TelemetryPacket telemetryPacket) {
			upperShooter.setPower(0);
			lowerShooter.setPower(0);
			return false;
		}
	}
}
