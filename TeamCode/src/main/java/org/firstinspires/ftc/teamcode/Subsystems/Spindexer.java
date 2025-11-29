package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.Actions.IntakeBall;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

@Config
public class Spindexer {
	// 360 ticks per 360 degrees for the through-bore encoder
	public static double TICKS_PER_REV = 360.0;

	// Magnetic limit switch triggers 11.011 degrees after the desired zero point
	// Offset in ticks: -11.011 * (8192 / 360) ≈ -250.7 ticks
	public static double zeroOffset = 0;

	// PID coefficients for position control. Tuned 2025-11-22.
	public static double P = 0.02, I = 0.0, D = 0.0, F = 0.0;

	private static Spindexer instance = null;

	private CRServo spindexerLeft;
	private CRServo spindexerRight;
	private AnalogInput spindexerEncoder;

	private PIDFController controller;
	public double targetPosition = 0;
	public double power = 0;

	public double per = 0;
	public double cent = 0;
	public double fin = 0;

	public double currentPosition;

	// EMA filter state variables
	private double filteredValue = 0.0;
	private boolean isInitialized = false;

	// EMA tuning parameters
	public static double ALPHA_RESPONSIVE = 0.7;
	public static double ALPHA_SMOOTH = 0.05;
	public static double MOTION_THRESHOLD = 0.1;

	// Store detected ball colors for each slot (0, 1, 2)
	private final BallColor[] ballColors = new BallColor[3];

	private Spindexer() {
		// Initialize all slots as EMPTY
		for (int i = 0; i < 3; i++) {
			ballColors[i] = BallColor.EMPTY;
		}
	}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Spindexer();
			instance.spindexerLeft = hardwareMap.get(CRServo.class, "spindexerLeft");
			instance.spindexerRight = hardwareMap.get(CRServo.class,"spindexerRight");
			instance.spindexerRight.setDirection(DcMotorSimple.Direction.REVERSE);
			instance.spindexerEncoder = hardwareMap.get(AnalogInput.class, "spindexerEncoder");

			instance.controller = new PIDFController(P, I, D, F);
		}
	}


	public static Spindexer getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Spindexer not initialized. Call initialize(hardwareMap, elapsedTime) first.");
		}
		return instance;
	}

	public static void shutdown() {
		// No cleanup needed currently
	}

	/**
	 * Gets the filtered position using Dynamic Exponential Moving Average (EMA).
	 * This provides a smoothed position value that adapts to motion.
	 */
	public double getPosition() {
		double rawInput = ((spindexerEncoder.getVoltage() / 3.3) * 360.0);
		if (!isInitialized) {
			filteredValue = rawInput;
			isInitialized = true;
			return filteredValue;
		}
		double currentAlpha = (Math.abs((spindexerLeft.getPower() + spindexerRight.getPower()) / 2.0) > MOTION_THRESHOLD) ? ALPHA_RESPONSIVE : ALPHA_SMOOTH;
		filteredValue = (rawInput * currentAlpha) + (filteredValue * (1 - currentAlpha));
		return filteredValue;
	}

	/**
	 * IMPORTANT: This method must be called from the main loop of your OpMode for the PID
	 * controller to run and for the spindexer to hold its position.
	 */
	public void update() {
		per = getPosition();
		cent = per * 100;
		if ( cent > 100){
			fin = cent - 100;
		} else{
			fin = cent;
		}

		controller.setPID(P, I, D, F);
		power = controller.getOutput(fin, targetPosition);

		if (power <= 0.3 && power >= 0.2){
			spindexerLeft.setPower(power);
			spindexerRight.setPower(power);
		} else if (power >= 0.3 && power >= 0.2){
			spindexerLeft.setPower(Math.abs(power));
			spindexerRight.setPower(Math.abs(power));
		} else if (power <= 0.05) {
			spindexerLeft.setPower(0);
			spindexerRight.setPower(0);
		}

		currentPosition = per;
	}

	private Action setTarget(double angle) {
		return new InstantAction(() -> {
			targetPosition = angle * TICKS_PER_REV;
		});
	}

	public Action toPosition(double revolutions) {
		return packet -> {
			targetPosition = revolutions * TICKS_PER_REV;

			// This action is considered "done" when the error is small.
			// This allows it to be a "blocking" call in a sequence.
			// Returns true while moving (error >= threshold), false when within tolerance
			double error = targetPosition - getPosition();
			packet.put("Spindexer Error", error);
			return Math.abs(error) >= 50; // Returns true while moving, false when within tolerance
		};
	}

	public BallColor getBallColor(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < 3) {
			return ballColors[slotIndex];
		}
		return BallColor.UNKNOWN;
	}

	public void setBallColor(int slotIndex, BallColor color) {
		if (slotIndex >= 0 && slotIndex < 3) {
			ballColors[slotIndex] = color;
		}
	}

	public void setTargetPosition(double revolutions) {
		targetPosition = revolutions; //* TICKS_PER_REV;
	}

	/**
	 * Force set the spindexer power to a value between -1 and 1.
	 * Clamps the power to the valid range.
	 */
	public InstantAction setDirectPower(double power) {
		return new InstantAction(() -> {
			// Clamp power between -1 and 1
			double clampedPower = Math.max(-1.0, Math.min(1.0, power));
			spindexerLeft.setPower(clampedPower);
			spindexerRight.setPower(clampedPower);
		});
	}
}
