package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Shooter {

	// Motor power constants
	public static double RUN_POWER = 1.0;
	public static double STOP_POWER = 0.0;
	// Offsets to resolve minor speed differences between motors
	public static double UPPER_OFFSET = 0.0;
	public static double LOWER_OFFSET = 0.0;
	private static Shooter instance = null;
	private DcMotor upperShooter;
	private DcMotor lowerShooter;

	private Shooter() {
	}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Shooter();
			instance.upperShooter = hardwareMap.get(DcMotor.class, "upperShooter");
			instance.lowerShooter = hardwareMap.get(DcMotor.class, "lowerShooter");
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

	public Action run() {
		return packet -> {
			upperShooter.setPower(RUN_POWER + UPPER_OFFSET);
			lowerShooter.setPower(RUN_POWER + LOWER_OFFSET);
			return true;
		};
	}

	public Action stop() {
		return packet -> {
			upperShooter.setPower(STOP_POWER);
			lowerShooter.setPower(STOP_POWER);
			return true;
		};
	}
}
