package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.teamcode.Actions.BallColor;
import org.firstinspires.ftc.teamcode.Actions.IntakeBall;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

@Config
public class Spindexer {
	// 8192 ticks per 360 degrees for the through-bore encoder
	public static double TICKS_PER_REV = 8192.0;

	// Magnetic limit switch triggers 11.011 degrees after the desired zero point
	// Offset in ticks: -11.011 * (8192 / 360) ≈ -250.7 ticks
	public static double zeroOffset = -11.011 * (8192.0 / 360.0);

	// PID coefficients for position control. Tune these via the FTC Dashboard.
	public static double P = 0.005, I = 0, D = 0, F = 0;

	private static Spindexer instance = null;

	private CRServo spindexer;
	private DcMotorEx spindexerEncoder;
	private TouchSensor spindexerZero;

	private PIDFController controller;
	private double targetPosition = 0;
	private boolean isZeroed = false;

	// This boolean is used by the ZeroAction to prevent the update() method's PID
	// from interfering with the direct power calls during the zeroing sequence.
	private boolean isZeroing = false;

	// Stores the encoder position when the magnetic limit switch triggers
	private double calibrationPosition = 0;

	// Stores the true zero position, accounting for the sensor offset
	private double actualZeroPosition = 0;

	// Store detected ball colors for each slot (0, 1, 2)
	private BallColor[] ballColors = new BallColor[3];

	private Spindexer() {
		// Initialize all slots as UNKNOWN
		for (int i = 0; i < 3; i++) {
			ballColors[i] = BallColor.UNKNOWN;
		}
	}

	public static void initialize(HardwareMap hardwareMap) {
		if (instance == null) {
			instance = new Spindexer();
			instance.spindexer = hardwareMap.get(CRServo.class, "spindexer");
			instance.spindexerEncoder = hardwareMap.get(DcMotorEx.class, "frontRight"); // Encoder plugged into a motor port
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
		double currentPosition = getAdjustedPosition();
		double power = controller.getOutput(currentPosition);
		spindexer.setPower(power);
	}

	public Action zero() {
		return new ZeroAction();
	}

	private Action setTargetRevolutions(double revolutions) {
		return packet -> {
			if (isZeroed) {
				targetPosition = revolutions * TICKS_PER_REV;
			}
			return true; // This action finishes instantly
		};
	}

	public Action toPosition(double revolutions) {
		return packet -> {
			if (!isZeroed) return true; // Can't go to position if not zeroed

			// Set the target for the PID controller running in the background
			targetPosition = revolutions * TICKS_PER_REV;

			// This action is considered "done" when the error is small.
			// This allows it to be a "blocking" call in a sequence.
			double error = targetPosition - getAdjustedPosition();
			packet.put("Spindexer Error", error);
			return Math.abs(error) < 50; // 50 ticks tolerance
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

	private enum ZeroState {
		START,
		MOVE_OFF_SENSOR,
		FAST_TOWARDS_SENSOR,
		BACK_OFF,
		SLOW_TOWARDS_SENSOR,
		DONE
	}

	private class ZeroAction implements Action {
		private ZeroState currentState = ZeroState.START;
		private double backOffPosition;

		@Override
		public boolean run(@NonNull TelemetryPacket packet) {
			packet.put("Zeroing State", currentState.toString());

			switch (currentState) {
				case START:
					isZeroing = true;
					if (spindexerZero.isPressed()) {
						spindexer.setPower(-0.5);
						currentState = ZeroState.MOVE_OFF_SENSOR;
					} else {
						spindexer.setPower(1.0);
						currentState = ZeroState.FAST_TOWARDS_SENSOR;
					}
					break;

				case MOVE_OFF_SENSOR:
					if (!spindexerZero.isPressed()) {
						spindexer.setPower(1.0);
						currentState = ZeroState.FAST_TOWARDS_SENSOR;
					}
					break;

				case FAST_TOWARDS_SENSOR:
					if (spindexerZero.isPressed()) {
						spindexer.setPower(0);
						backOffPosition = spindexerEncoder.getCurrentPosition() - 200;
						spindexer.setPower(-0.5);
						currentState = ZeroState.BACK_OFF;
					}
					break;

				case BACK_OFF:
					if (spindexerEncoder.getCurrentPosition() <= backOffPosition) {
						spindexer.setPower(0.2);
						currentState = ZeroState.SLOW_TOWARDS_SENSOR;
					}
					break;

				case SLOW_TOWARDS_SENSOR:
					if (spindexerZero.isPressed()) {
						spindexer.setPower(0);
						// Record the encoder position when the sensor triggers
						calibrationPosition = spindexerEncoder.getCurrentPosition();
						// Calculate true zero position accounting for the sensor offset
						actualZeroPosition = calibrationPosition + zeroOffset;
						targetPosition = 0;
						isZeroed = true;
						isZeroing = false;
						currentState = ZeroState.DONE;
					}
					break;

				case DONE:
					return true; // Action is complete
			}
			return false; // Action is still running
		}
	}
}
