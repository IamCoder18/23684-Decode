package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;

/**
 * Drive test OpMode for testing Mecanum drive with PID control.
 * Uses GoBilda Pinpoint for odometry.
 */
@Disabled
@Config
@TeleOp
public class Test_Drive extends OpMode {
	DcMotor frontRight;
	DcMotor rearRight;
	DcMotor frontLeft;
	DcMotor rearLeft;

	// Tuning parameters
	public static double targetX = 0;
	public static double targetY = 0;
	public static double targetH = 0;

	// Drive system components
	private Pose2d pose;
	private Pose2d target;
	private GoBildaPinpointDriver pinpoint;
	private PIDFController xPidController;
	private PIDFController yPidController;
	private PIDFController headingPidController;

	private static final double DEADZONE_THRESHOLD = 0.05;

	/**
	 * Normalize an angle to the range [-pi, pi].
	 *
	 * @param angle the angle in radians
	 * @return the normalized angle
	 */
	private double normalizeAngle(double angle) {
		while (angle > Math.PI) {
			angle -= 2 * Math.PI;
		}
		while (angle < -Math.PI) {
			angle += 2 * Math.PI;
		}
		return angle;
	}

	@Override
	public void init() {
		pose = new Pose2d(0, 0, 0);
		target = new Pose2d(targetX, targetY, targetH);

		xPidController = new PIDFController(XController.kP, XController.kI, XController.kD, XController.kF);
		yPidController = new PIDFController(YController.kP, YController.kI, YController.kD, YController.kF);
		headingPidController = new PIDFController(HController.kP, HController.kI, HController.kD, HController.kF);

		GoBildaPinpointDriver.EncoderDirection yEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;
		GoBildaPinpointDriver.EncoderDirection xEncoderDirection = GoBildaPinpointDriver.EncoderDirection.REVERSED;

		pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
		pinpoint.setOffsets(-177.8, -63.5, DistanceUnit.MM);
		pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
		pinpoint.setEncoderDirections(xEncoderDirection, yEncoderDirection);
		pinpoint.resetPosAndIMU();

		frontRight = hardwareMap.get(DcMotor.class, "frontRight");
		rearRight = hardwareMap.get(DcMotor.class, "rearRight");
		frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
		rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");

	}

	@Override
	public void loop() {
		// Update PID gains from config
		xPidController.setPID(XController.kP, XController.kI, XController.kD, XController.kF);
		yPidController.setPID(YController.kP, YController.kI, YController.kD, YController.kF);
		headingPidController.setPID(HController.kP, HController.kI, HController.kD, HController.kF);

		// Update odometry
		pinpoint.update();
		pose = new Pose2d(
				-pinpoint.getPosY(DistanceUnit.INCH),
				pinpoint.getPosX(DistanceUnit.INCH),
				pinpoint.getHeading(AngleUnit.RADIANS)
		);
		target = new Pose2d(targetX, targetY, Math.toRadians(targetH));

		// Calculate power commands
		double forwardPower = yPidController.getOutput(pose.position.y, target.position.y);
		double strafePower = xPidController.getOutput(pose.position.x, target.position.x);
		double turnPower = headingPidController.getOutput(
				normalizeAngle(pose.heading.toDouble()),
				normalizeAngle(target.heading.toDouble())
		);

		// Apply deadzone
		forwardPower = Math.abs(forwardPower) > DEADZONE_THRESHOLD ? forwardPower : 0;
		strafePower = Math.abs(strafePower) > DEADZONE_THRESHOLD ? strafePower : 0;
		turnPower = Math.abs(turnPower) > DEADZONE_THRESHOLD ? turnPower : 0;

		double y = forwardPower; // Remember, Y stick is reversed!
		double x = strafePower;
		double rx = turnPower;

		frontLeft.setPower(y + x + rx);
		rearLeft.setPower(y - x + rx);
		frontRight.setPower(y - x - rx);
		rearRight.setPower(y + x - rx);

		// Telemetry
		telemetry.addData("Position", pose.position);
		telemetry.addData("Heading (deg)", Math.toDegrees(pose.heading.toDouble()));
		telemetry.addLine();
		telemetry.addData("Forward Power", forwardPower);
		telemetry.addData("Strafe Power", strafePower);
		telemetry.addData("Turn Power", turnPower);
		telemetry.update();
	}

	/**
	 * X-axis (strafe) PID controller gains.
	 */
	public static class XController {
		public static double kP = 0;
		public static double kI = 0;
		public static double kD = 0;
		public static double kF = 0;
	}

	/**
	 * Y-axis (forward) PID controller gains.
	 */
	public static class YController {
		public static double kP = 0;
		public static double kI = 0;
		public static double kD = 0;
		public static double kF = 0;
	}

	/**
	 * Heading (rotation) PID controller gains.
	 */
	public static class HController {
		public static double kP = 0;
		public static double kI = 0;
		public static double kD = 0;
		public static double kF = 0;
	}
}
