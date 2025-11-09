package org.firstinspires.ftc.teamcode.Subsystems;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class ColorDetector {

	// HSV Thresholds
	public static double GREEN_HUE_MIN = 100, GREEN_HUE_MAX = 140;
	public static double PURPLE_HUE_MIN = 250, PURPLE_HUE_MAX = 290;
	public static double MIN_SATURATION = 0.4;

	private static ColorDetector instance = null;
	// Public fields to store the last read values
	public int avgRed, avgGreen, avgBlue;
	public float[] avgHSV = new float[3];
	public boolean isGreen, isPurple;
	private com.qualcomm.robotcore.hardware.ColorSensor colourLeft;
	private com.qualcomm.robotcore.hardware.ColorSensor colourRight;

	private ColorDetector() {
	}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new ColorDetector();
			instance.colourLeft = hardwareMap.get(com.qualcomm.robotcore.hardware.ColorSensor.class, "colourLeft");
			instance.colourRight = hardwareMap.get(com.qualcomm.robotcore.hardware.ColorSensor.class, "colourRight");
		}
	}

	public static ColorDetector getInstance() {
		if (instance == null) {
			throw new IllegalStateException("ColorSensor not initialized. Call initialize(hardwareMap) first.");
		}
		return instance;
	}

	public static void shutdown() {
		// No cleanup needed currently
	}

	private void updateValues() {
		int leftRed = colourLeft.red();
		int leftGreen = colourLeft.green();
		int leftBlue = colourLeft.blue();

		int rightRed = colourRight.red();
		int rightGreen = colourRight.green();
		int rightBlue = colourRight.blue();

		avgRed = (leftRed + rightRed) / 2;
		avgGreen = (leftGreen + rightGreen) / 2;
		avgBlue = (leftBlue + rightBlue) / 2;

		Color.RGBToHSV(avgRed, avgGreen, avgBlue, avgHSV);

		isGreen = (avgHSV[1] > MIN_SATURATION && avgHSV[0] > GREEN_HUE_MIN && avgHSV[0] < GREEN_HUE_MAX);
		isPurple = (avgHSV[1] > MIN_SATURATION && avgHSV[0] > PURPLE_HUE_MIN && avgHSV[0] < PURPLE_HUE_MAX);
	}

	public Action update() {
		return new UpdateAction();
	}

	private class UpdateAction implements Action {
		@Override
		public boolean run(@NonNull TelemetryPacket packet) {
			updateValues();
			packet.put("Average Red", avgRed);
			packet.put("Average Green", avgGreen);
			packet.put("Average Blue", avgBlue);
			packet.put("Average Hue", avgHSV[0]);
			packet.put("Average Saturation", avgHSV[1]);
			packet.put("Average Value", avgHSV[2]);
			packet.put("Is Green", isGreen);
			packet.put("Is Purple", isPurple);
			return true; // Action finishes instantly
		}
	}
}
