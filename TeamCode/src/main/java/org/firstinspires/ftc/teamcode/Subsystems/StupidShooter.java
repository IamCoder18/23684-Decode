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
	double needForSpeed = 2800;

	public StupidShooter(HardwareMap hardwareMap) {
		upperShooter = hardwareMap.get(DcMotorEx.class, "upperShooter");
		lowerShooter = hardwareMap.get(DcMotorEx.class, "lowerShooter");
	}

	public void updateRPM() {
		double upperVelocity = upperShooter.getVelocity();
		double lowerVelocity = lowerShooter.getVelocity();

		double upperRPM = (upperVelocity / TICKS_PER_REV) * 60;
		double lowerRPM = (lowerVelocity / TICKS_PER_REV) * 60;

		averageRPM = (upperRPM + lowerRPM) / 2;
	}

	public Action WindUp() {
		return new WindUp();
	}

	public Action WaitForSpike() {
		return new WaitForSpike();
	}

	public class WindUp implements Action {
		boolean initialized = false;

		@Override
		public boolean run(@NonNull TelemetryPacket telemetryPacket) {
			updateRPM();

			double currentTime = System.nanoTime();

			if (!initialized) {
				upperShooter.setPower(0.1 * Math.sin(currentTime * 50) + 1);
				lowerShooter.setPower(0.1 * Math.sin(currentTime * 50) + 1);
				initialized = true;
			}
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
}
