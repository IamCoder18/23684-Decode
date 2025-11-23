package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.Actions.IntakeBall;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

@Config
public class Spindexer {
	// 8192 ticks per 360 degrees for the through-bore encoder
	public static double TICKS_PER_REV = 8192.0;

	// Magnetic limit switch triggers 11.011 degrees after the desired zero point
	// Offset in ticks: -11.011 * (8192 / 360) ≈ -250.7 ticks
	public static double zeroOffset = 0 * (8192.0 / 360.0);

	// PID coefficients for position control. Tune these via the FTC Dashboard.
	public static double P = 0.01, I = 0, D = 0, F = 0;

	private static Spindexer instance = null;

	private CRServo spindexer;
	private DcMotorEx spindexerEncoder;
	private TouchSensor spindexerZero;

	private PIDFController controller;
	private double targetPosition = 0;
	private boolean isZeroed = false;

	public double degreeToTicks = 360 / 8192.0;


	// This boolean is used by the ZeroAction to prevent the update() method's PID
	// from interfering with the direct power calls during the zeroing sequence.
	private final boolean isZeroing = false;

	// Stores the encoder position when the magnetic limit switch triggers
	private final double calibrationPosition = 0;

	// Stores the true zero position, accounting for the sensor offset
	private final double actualZeroPosition = 0;

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
			instance.spindexer = hardwareMap.get(CRServo.class, "spindexerLeft");
			instance.spindexerEncoder = hardwareMap.get(DcMotorEx.class, "rearRight"); // Encoder plugged into a motor port
			instance.spindexerZero = hardwareMap.get(TouchSensor.class, "spindexerZero");

			// Configure the encoder motor port to just read encoder values
			instance.spindexerEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

			instance.controller = new PIDFController(P, I, D, F);
			instance.controller.setOutputLimits(-1, 1);
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
	 * Gets the current position of the spindexer, adjusted for the zero offset.
	 * This accounts for both the sensor trigger point and the actual zero calibration.
	 */
	private double getAdjustedPosition() {
		return spindexerEncoder.getCurrentPosition() - actualZeroPosition;
	}

	/**
	 * IMPORTANT: This method must be called from the main loop of your OpMode for the PID
	 * controller to run and for the spindexer to hold its position.
	 */
	public void update() {
		if (!isZeroed || isZeroing) {
			// Do not run PID controller if the spindexer is not zeroed or is currently zeroing.
			return;
		}

		controller.setSetpoint(targetPosition);
		double currentPosition = getAdjustedPosition() * degreeToTicks;
		double power = controller.getOutput(currentPosition,targetPosition);
		spindexer.setPower(power);
	}

	public Action zero() {
		return new ZeroAction();
	}

	private Action setTargetRevolutions(double revolutions) {
		return new InstantAction(() -> {
			if (isZeroed) {
				targetPosition = revolutions * TICKS_PER_REV;
			}
		});
	}

	public Action toPosition(double revolutions) {
		return packet -> {
			// If not zeroed, keep running (return true) to wait until zeroed
			isZeroed = true;
			if (!isZeroed) return true;


			// Set the target for the PID controller running in the background
			//targetPosition = revolutions * TICKS_PER_REV;

			targetPosition = revolutions;

			// This action is considered "done" when the error is small.
			// This allows it to be a "blocking" call in a sequence.
			// Returns true while moving (error >= threshold), false when within tolerance
			double error = targetPosition - getAdjustedPosition();
			packet.put("Spindexer Error", error);
			return Math.abs(error) >= 50; // Returns true while moving, false when within tolerance
		};
	}

	public Action intakeBall() {
		return new IntakeBall();
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

	public double getCurrentPositionTicks() {
		return getAdjustedPosition();
	}

	public void setTargetPosition(double revolutions) {
		if (isZeroed) {
			targetPosition = revolutions * TICKS_PER_REV;
		}
	}

	/**
	 * Force set the spindexer power to a value between -1 and 1.
	 * Clamps the power to the valid range.
	 */
	public InstantAction setDirectPower(double power) {
		return new InstantAction(() -> {
			// Clamp power between -1 and 1
			double clampedPower = Math.max(-1.0, Math.min(1.0, power));
			spindexer.setPower(clampedPower);
		});
	}

	private class ZeroAction implements Action {
//		private long startTime = System.currentTimeMillis();

		@Override
		public boolean run(@NonNull TelemetryPacket packet) {
//			double currentEncoderPos = spindexerEncoder.getCurrentPosition();
//			boolean sensorPressed = spindexerZero.isPressed();
//			long elapsedTime = System.currentTimeMillis() - startTime;
//
//			packet.put("ZEROING: Encoder Position", currentEncoderPos);
//			packet.put("ZEROING: Sensor Pressed", sensorPressed);
//			packet.put("ZEROING: Elapsed time (ms)", elapsedTime);
//
//			isZeroing = true;
//			spindexer.setPower(0.1);
//
//			if (sensorPressed) {
//				spindexer.setPower(0);
//				// Record the encoder position when the sensor triggers
//				calibrationPosition = spindexerEncoder.getCurrentPosition();
//				// Calculate true zero position accounting for the sensor offset
//				actualZeroPosition = calibrationPosition + zeroOffset;
//				targetPosition = 0;
//				packet.put("ZEROING: COMPLETE - Calibration position", calibrationPosition);
//				packet.put("ZEROING: COMPLETE - Actual zero position", actualZeroPosition);
//				packet.put("ZEROING: COMPLETE - Zero offset applied", zeroOffset);
//				isZeroed = true;
//				isZeroing = false;
//				return true; // Action is complete
//			}
//
//			return false; // Action is still running
			return true;
		}
	}
}
