package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

@Config
public class RGBIndicator {

	// Color names for discrete color selection
	private static final String[] COLOR_NAMES = {
			"OFF", "RED", "ORANGE", "YELLOW", "SAGE", "GREEN",
			"AZURE", "BLUE", "INDIGO", "VIOLET", "WHITE"
	};
	// Color map with RGB values and corresponding servo positions
	private static final int[][] COLOR_MAP = {
			{0, 0, 0},           // Off (black) -> 0.0
			{255, 0, 0},         // Red -> 0.277
			{255, 127, 0},       // Orange -> 0.333
			{255, 255, 0},       // Yellow -> 0.388
			{191, 255, 0},       // Sage (yellow-green) -> 0.444
			{0, 255, 0},         // Green -> 0.500
			{0, 255, 255},       // Azure (cyan) -> 0.555
			{0, 0, 255},         // Blue -> 0.611
			{75, 0, 130},        // Indigo -> 0.666
			{148, 0, 211},       // Violet -> 0.722
			{255, 255, 255}      // White -> 1.0
	};
	private static final double[] SERVO_POSITIONS = {
			0.0,    // Off
			0.277,  // Red
			0.333,  // Orange
			0.388,  // Yellow
			0.444,  // Sage
			0.500,  // Green
			0.555,  // Azure
			0.611,  // Blue
			0.666,  // Indigo
			0.722,  // Violet
			1.0     // White
	};
	private static RGBIndicator instance = null;
	private Servo rgbServo;

	private RGBIndicator() {
	}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new RGBIndicator();
			instance.rgbServo = hardwareMap.get(Servo.class, "rgbIndicator");
		}
	}

	public static RGBIndicator getInstance() {
		if (instance == null) {
			throw new IllegalStateException("RGBIndicator not initialized. Call initialize(hardwareMap) first.");
		}
		return instance;
	}

	public static void shutdown() {
		// No cleanup needed currently
	}

	/**
	 * Calculates the Euclidean distance between two RGB values.
	 */
	private double rgbDistance(int[] a, int[] b) {
		double dr = a[0] - b[0];
		double dg = a[1] - b[1];
		double db = a[2] - b[2];
		return Math.sqrt(dr * dr + dg * dg + db * db);
	}

	/**
	 * Converts a HEX color string (e.g., "FF00FF" or "#FF00FF") to servo position.
	 * Maps the RGB value to the closest color in the color map and returns the corresponding servo position.
	 *
	 * @param hexColor HEX color string with or without '#' prefix
	 * @return Servo position (0.0 to 1.0) corresponding to the closest color
	 */
	public double hexToServoPosition(String hexColor) {
		// Remove '#' if present
		if (hexColor.startsWith("#")) {
			hexColor = hexColor.substring(1);
		}

		// Parse hex string to RGB
		int hex = Integer.parseInt(hexColor, 16);
		int r = (hex >> 16) & 0xFF;
		int g = (hex >> 8) & 0xFF;
		int b = hex & 0xFF;

		// Find closest color in map
		double minDistance = Double.MAX_VALUE;
		int closestIndex = 0;

		int[] currentRGB = {r, g, b};
		for (int i = 0; i < COLOR_MAP.length; i++) {
			double distance = rgbDistance(currentRGB, COLOR_MAP[i]);
			if (distance < minDistance) {
				minDistance = distance;
				closestIndex = i;
			}
		}

		return SERVO_POSITIONS[closestIndex];
	}

	/**
	 * Sets the RGB indicator color using a color name string.
	 * Valid color names: OFF, RED, ORANGE, YELLOW, SAGE, GREEN, AZURE, BLUE, INDIGO, VIOLET, WHITE
	 *
	 * @param colorName Color name string (case-insensitive)
	 */
	public void setColorByName(String colorName) {
		if (rgbServo == null) {
			return;
		}
		colorName = colorName.toUpperCase();

		// Find the matching color index
		int colorIndex = -1;
		for (int i = 0; i < COLOR_NAMES.length; i++) {
			if (COLOR_NAMES[i].equals(colorName)) {
				colorIndex = i;
				break;
			}
		}

		// If color name not found, default to OFF
		if (colorIndex == -1) {
			colorIndex = 0;
		}

		rgbServo.setPosition(SERVO_POSITIONS[colorIndex]);
	}

	/**
	 * Sets the RGB indicator color using a HEX color string.
	 *
	 * @param hexColor HEX color string (e.g., "FF00FF" or "#FF00FF")
	 */
	public void setColor(String hexColor) {
		if (rgbServo == null) {
			return;
		}
		double position = hexToServoPosition(hexColor);
		rgbServo.setPosition(position);
	}

	/**
	 * Returns an InstantAction that sets the RGB indicator color.
	 *
	 * @param hexColor HEX color string
	 * @return Action that sets the color
	 */
	public Action setColorAction(String hexColor) {
		return new InstantAction(() -> setColor(hexColor));
	}
}
