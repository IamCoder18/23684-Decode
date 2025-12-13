package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.Map;
import java.util.TreeMap;

@Config
public class RGBIndicator {

	// --- Tuning Parameters ---
	// Increase if colors look washed out or white
	public static double PURITY_THRESHOLD = 0.25;

	// MAPPING: Wavelength (nm) -> Servo Position
	// Based on your goBILDA 3118-0808-0002 values (1050us-1950us range)
	private static final TreeMap<Double, Double> SPECTRAL_MAP = new TreeMap<>();
	static {
		SPECTRAL_MAP.put(650.0, 0.277); // Red
		SPECTRAL_MAP.put(605.0, 0.333); // Orange
		SPECTRAL_MAP.put(585.0, 0.388); // Yellow
		SPECTRAL_MAP.put(560.0, 0.444); // Sage
		SPECTRAL_MAP.put(530.0, 0.500); // Green
		SPECTRAL_MAP.put(500.0, 0.555); // Azure
		SPECTRAL_MAP.put(470.0, 0.611); // Blue
		SPECTRAL_MAP.put(440.0, 0.666); // Indigo
		SPECTRAL_MAP.put(400.0, 0.722); // Violet
	}

	private static RGBIndicator instance = null;
	private Servo rgbServo;

	private RGBIndicator() {}

	public static void initialize(HardwareMap hardwareMap) {
		instance = new RGBIndicator();
		// Safety check: only get the servo if it exists in config
		if (hardwareMap.servo.contains("rgbIndicator")) {
			instance.rgbServo = hardwareMap.get(Servo.class, "rgbIndicator");
		}
	}

	public static RGBIndicator getInstance() {
		if (instance == null) instance = new RGBIndicator();
		return instance;
	}

	public static void shutdown() {
		// No cleanup needed currently
	}

	public void setColor(String hexColor) {
		if (rgbServo == null) return;

		// 1. Calculate the Physics (Wavelength)
		WavelengthConverter.SpectralResult result = WavelengthConverter.getDominantWavelength(hexColor);

		double servoPos;

		// 2. Handle Achromatic (White/Gray)
		// If the color is too "pale" (low saturation), goBILDA usually treats this as White (1.0)
		if (result.purity < PURITY_THRESHOLD) {
			servoPos = 1.0;
		}
		// 3. Handle Purples (Non-Spectral)
		// Purples don't exist in the rainbow (wavelength), they are Red + Blue.
		// We map them to the end of the spectrum (Violet).
		else if (result.isPurple) {
			servoPos = 0.722;
		}
		// 4. Interpolate Spectral Colors
		else {
			servoPos = interpolateServoPosition(result.wavelength);
		}

		rgbServo.setPosition(servoPos);
	}

	/**
	 * smoothly finds the servo position between two known colors.
	 */
	private double interpolateServoPosition(double wavelength) {
		// Clamp to physical limits of the goBILDA hue range
		if (wavelength >= 650) return 0.277; // Red limit
		if (wavelength <= 400) return 0.722; // Violet limit

		// Find the two closest defined points in our map
		Map.Entry<Double, Double> floor = SPECTRAL_MAP.floorEntry(wavelength);
		Map.Entry<Double, Double> ceil = SPECTRAL_MAP.ceilingEntry(wavelength);

		if (floor == null) {
			assert ceil != null;
			return ceil.getValue();
		}
		if (ceil == null) return floor.getValue();
		if (floor.getKey().equals(ceil.getKey())) return floor.getValue();

		// MATH: Linear Interpolation
		// "If we are 40% of the way from Yellow to Green in wavelength,
		//  move the servo 40% of the way from 0.388 to 0.500."
		double range = ceil.getKey() - floor.getKey();
		double fraction = (wavelength - floor.getKey()) / range;

		return floor.getValue() + fraction * (ceil.getValue() - floor.getValue());
	}

	public void setDirectPosition(double position) {
		if (rgbServo != null) rgbServo.setPosition(position);
	}

	public Action setDirectPositionAction(double position) {
		return new InstantAction(() -> setDirectPosition(position));
	}

	public Action setColorAction(String hexColor) {
		return new InstantAction(() -> setColor(hexColor));
	}

	// --- Helper Logic ---
	private static class WavelengthConverter {

		public static class SpectralResult {
			public double wavelength;
			public boolean isPurple;
			public double purity;
		}

		// CIE 1931 Spectral Locus (The curve of the rainbow)
		private static final TreeMap<Integer, double[]> SPECTRAL_LOCUS = new TreeMap<>();
		static {
			SPECTRAL_LOCUS.put(380, new double[]{0.1741, 0.0050});
			SPECTRAL_LOCUS.put(440, new double[]{0.1644, 0.0093});
			SPECTRAL_LOCUS.put(460, new double[]{0.1440, 0.0297});
			SPECTRAL_LOCUS.put(490, new double[]{0.0454, 0.2950}); // Cyan bend
			SPECTRAL_LOCUS.put(520, new double[]{0.0743, 0.8338}); // Green peak
			SPECTRAL_LOCUS.put(560, new double[]{0.3731, 0.6245});
			SPECTRAL_LOCUS.put(580, new double[]{0.5125, 0.4866}); // Yellow bend
			SPECTRAL_LOCUS.put(600, new double[]{0.6270, 0.3725});
			SPECTRAL_LOCUS.put(620, new double[]{0.6915, 0.3083});
			SPECTRAL_LOCUS.put(700, new double[]{0.7347, 0.2653});
		}

		private static final double WX = 0.3127;
		private static final double WY = 0.3290;

		public static SpectralResult getDominantWavelength(String hexColor) {
			if (hexColor.startsWith("#")) hexColor = hexColor.substring(1);

			// Fast Parse (Optimized for Loop Time)
			long color;
			try {
				color = Long.parseLong(hexColor, 16);
			} catch (NumberFormatException e) {
				return new SpectralResult(); // Return default on error
			}

			// Fast Gamma Correction (Approximation)
			double r = Math.pow(((color >> 16) & 0xFF) / 255.0, 2.2);
			double g = Math.pow(((color >> 8) & 0xFF) / 255.0, 2.2);
			double b = Math.pow((color & 0xFF) / 255.0, 2.2);

			// RGB to XYZ
			double X = 0.4124 * r + 0.3576 * g + 0.1805 * b;
			double Y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
			double Z = 0.0193 * r + 0.1192 * g + 0.9505 * b;

			double sum = X + Y + Z;
			SpectralResult res = new SpectralResult();

			if (sum == 0) return res;

			double x = X / sum;
			double y = Y / sum;

			// Approximate Saturation (Distance from White Point)
			res.purity = Math.hypot(x - WX, y - WY) / 0.22;

			double angleColor = Math.atan2(y - WY, x - WX);

			// Purple Detection
			double deg = Math.toDegrees(angleColor);
			if (deg < -20 && deg > -160) {
				res.isPurple = true;
				return res;
			}

			// Find closest spectral points
			double closestDiff = Double.MAX_VALUE;
			double secondDiff = Double.MAX_VALUE;
			int wl1 = 0, wl2 = 0;

			for (Map.Entry<Integer, double[]> entry : SPECTRAL_LOCUS.entrySet()) {
				double[] p = entry.getValue();
				double angleSpec = Math.atan2(p[1] - WY, p[0] - WX);
				double diff = Math.abs(angleSpec - angleColor);
				if (diff > Math.PI) diff = 2 * Math.PI - diff;

				if (diff < closestDiff) {
					secondDiff = closestDiff;
					wl2 = wl1;
					closestDiff = diff;
					wl1 = entry.getKey();
				} else if (diff < secondDiff) {
					secondDiff = diff;
					wl2 = entry.getKey();
				}
			}

			// Weighted Average to find exact wavelength between the two closest points
			double total = closestDiff + secondDiff;
			if (total == 0) res.wavelength = wl1;
			else res.wavelength = (wl1 * secondDiff + wl2 * closestDiff) / total;

			return res;
		}
	}
}
