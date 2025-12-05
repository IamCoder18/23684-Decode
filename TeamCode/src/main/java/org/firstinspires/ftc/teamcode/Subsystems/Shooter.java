package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Shooter {

	// Motor power constants
	public static double RUN_POWER = 1.0;
	public static double MIN_POWER = 0.9;
	public static double STOP_POWER = 0.0;
	// Offsets to resolve minor speed differences between motors
	public static double UPPER_OFFSET = 0.0;
	public static double LOWER_OFFSET = 0.0;

	// Shooter motor specifications
	public static double TICKS_PER_REVOLUTION = 28.0;

	private static Shooter instance = null;
	private static final double OSCILLATION_PERIOD = 500.0; // milliseconds for full cycle
	// Average RPM of the shooter motors
	public double averageRPM = 0.0;
	// Individual RPM values for each shooter motor
	public double upperRPM = 0.0;
	public double lowerRPM = 0.0;
	private DcMotorEx upperShooter;
	private DcMotorEx lowerShooter;
	// Oscillation timing
	private long oscillationStartTime = 0;

	private Shooter() {
	}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Shooter();
			instance.upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
			instance.lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");
		}
	}

	public static Shooter getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Shooter not initialized. Call initialize(hardwareMap) first.");
		}
		return instance;
	}

	public static void shutdown() {
		// No cleanup needed currently
	}

	/**
	 * Updates the average RPM of the shooter motors.
	 * Call this from your main loop to continuously update the RPM readings.
	 */
	public void updateRPM() {
		double upperVelocity = upperShooter.getVelocity(); // ticks per second
		double lowerVelocity = lowerShooter.getVelocity(); // ticks per second

		// Convert ticks per second to RPM: (ticks_per_second / ticks_per_revolution) * 60
		upperRPM = (upperVelocity / TICKS_PER_REVOLUTION) * 60.0;
		lowerRPM = (lowerVelocity / TICKS_PER_REVOLUTION) * 60.0;

		// Calculate average RPM
		averageRPM = (upperRPM + lowerRPM) / 2.0;
	}

	/**
	 * Returns an InstantAction that runs both shooter motors, oscillating between MIN_POWER and RUN_POWER.
	 * This action completes immediately after setting motor power.
	 * Power values include offset adjustments to balance motor speed.
	 *
	 * @return Action that starts both shooter motors with oscillation
	 */
	public Action run() {
		return new InstantAction(() -> {
			if (oscillationStartTime == 0) {
				oscillationStartTime = System.currentTimeMillis();
			}

			// Calculate elapsed time and position in oscillation cycle
			long elapsedTime = System.currentTimeMillis() - oscillationStartTime;
			double cyclePosition = (elapsedTime % (long) OSCILLATION_PERIOD) / OSCILLATION_PERIOD;

			// Oscillate between MIN_POWER and RUN_POWER using sine wave
			double oscillatingPower = MIN_POWER + (RUN_POWER - MIN_POWER) * 0.5 * (1 + Math.sin(2 * Math.PI * cyclePosition - Math.PI / 2));

			upperShooter.setPower(oscillatingPower + UPPER_OFFSET);
			lowerShooter.setPower(oscillatingPower + LOWER_OFFSET);
		});
	}

	/**
	 * Returns an InstantAction that stops both shooter motors.
	 * This action completes immediately after setting power to zero.
	 * Also resets the oscillation timer.
	 *
	 * @return Action that stops both shooter motors
	 */
	public Action stop() {
		return new InstantAction(() -> {
			oscillationStartTime = 0;
			upperShooter.setPower(STOP_POWER);
			lowerShooter.setPower(STOP_POWER);
		});
	}
}
