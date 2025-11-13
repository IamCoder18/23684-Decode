package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Shooter {

	// Motor power constants
	public static double RUN_POWER = 0.82;
	public static double STOP_POWER = 0.0;
	// Offsets to resolve minor speed differences between motors
	public static double UPPER_OFFSET = 0.0;
	public static double LOWER_OFFSET = 0.0;
	
	// Shooter motor specifications
	public static double TICKS_PER_REVOLUTION = 28.0;
	
	private static Shooter instance = null;
	private DcMotorEx upperShooter;
	private DcMotorEx lowerShooter;
	
	// Average RPM of the shooter motors
	public double averageRPM = 0.0;

	public double timmythetime;

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
		double upperRPM = (upperVelocity / TICKS_PER_REVOLUTION) * 60.0;
		double lowerRPM = (lowerVelocity / TICKS_PER_REVOLUTION) * 60.0;
		
		// Calculate average RPM
		averageRPM = (upperRPM + lowerRPM) / 2.0;
	}

	/**
	 * Returns an InstantAction that runs both shooter motors at full power dc pulsing owo.
	 * This action completes immediately after setting motor power.
	 * Power values include offset adjustments to balance motor speed.
	 *
	 * @return Action that starts both shooter motors
	 */
	public Action run() {
		return new InstantAction(() -> {
			upperShooter.setPower(RUN_POWER + UPPER_OFFSET);
			lowerShooter.setPower(RUN_POWER + LOWER_OFFSET);
		});
	}

	/**
	 * Returns an InstantAction that stops both shooter motors.
	 * This action completes immediately after setting power to zero.
	 *
	 * @return Action that stops both shooter motors
	 */
	public Action stop() {
		return new InstantAction(() -> {
			upperShooter.setPower(STOP_POWER);
			lowerShooter.setPower(STOP_POWER);
		});
	}
}
