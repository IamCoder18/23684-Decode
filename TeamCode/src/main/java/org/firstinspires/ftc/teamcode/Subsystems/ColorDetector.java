package org.firstinspires.ftc.teamcode.Subsystems;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Singleton subsystem that manages dual color sensors for ball color detection.
 * 
 * Uses HSV color space to detect green and purple balls.
 * Configuration values can be tuned via FTC Dashboard.
 * 
 * To use:
 * 1. Call initialize(hardwareMap) once during robot initialization
 * 2. Call getInstance().update().run(packet) in the main loop
 * 3. Read isGreen and isPurple boolean fields for detection results
 */
@Config
public class ColorDetector {

	// HSV Thresholds (tunable via FTC Dashboard)
	/** Minimum hue value for green detection */
	public static double GREEN_HUE_MIN = 100;
	/** Maximum hue value for green detection */
	public static double GREEN_HUE_MAX = 140;
	/** Minimum hue value for purple detection */
	public static double PURPLE_HUE_MIN = 250;
	/** Maximum hue value for purple detection */
	public static double PURPLE_HUE_MAX = 290;
	/** Minimum saturation required to consider a color valid */
	public static double MIN_SATURATION = 0.4;

	private static ColorDetector instance = null;
	// Public fields to store the last read values
	/** Average red value from both sensors (0-255) */
	public int avgRed;
	/** Average green value from both sensors (0-255) */
	public int avgGreen;
	/** Average blue value from both sensors (0-255) */
	public int avgBlue;
	/** Average HSV values: [0]=hue (0-360), [1]=saturation (0-1), [2]=value (0-1) */
	public float[] avgHSV = new float[3];
	/** True if the last reading detected a green ball */
	public boolean isGreen;
	/** True if the last reading detected a purple ball */
	public boolean isPurple;
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

	/**
	 * Reads values from both color sensors and updates detection state.
	 * Averages RGB values from both sensors, converts to HSV, and checks against thresholds.
	 * Updates the public isGreen and isPurple fields.
	 */
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

	/**
	 * Returns an Action that updates color sensor readings.
	 * This action finishes immediately after one frame and should be called every loop.
	 * 
	 * @return Action that performs one sensor update
	 */
	public Action update() {
		return new UpdateAction();
	}

	/**
	 * Internal Action that performs color sensor updates.
	 * Updates sensor readings and logs telemetry data.
	 */
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
			return false; // Action finishes immediately after one frame
		}
	}
}
