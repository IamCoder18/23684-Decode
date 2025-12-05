package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Roadrunner.Localizer;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Roadrunner.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;


@Config
@TeleOp
public class drivetest extends OpMode {

	public static double targetX = 0, targetY = 0, targetH = 0;
	public static X xpid = new X();
	public static Y whypid = new Y();
	public static H hpid = new H();
	MecanumDrive drive;
	Pose2d pose;
	Pose2d target;
	Localizer localizer;
	GoBildaPinpointDriver pinpoint;
	GoBildaPinpointDriver.EncoderDirection ything, xthing;
	PIDFController xController;
	PIDFController yController;
	PIDFController hController;

	public double normalizeAngle(double angle) {
		while (angle > Math.PI) angle -= 2 * Math.PI;
		while (angle < -Math.PI) angle += 2 * Math.PI;
		return angle;
	}

	@Override
	public void init() {
		pose = new Pose2d(0, 0, 0);
		drive = new MecanumDrive(hardwareMap, pose);
		target = new Pose2d(targetX, targetY, targetH);

		localizer = new PinpointLocalizer(hardwareMap, MecanumDrive.PARAMS.inPerTick, pose);

		xController = new PIDFController(X.Xp, X.Xi, X.Xd, X.Xf);
		yController = new PIDFController(Y.Yp, Y.Yi, Y.Yd, Y.Yf);
		hController = new PIDFController(H.Hp, H.Hi, H.Hd, H.Hf);


		ything = GoBildaPinpointDriver.EncoderDirection.REVERSED;
		xthing = GoBildaPinpointDriver.EncoderDirection.REVERSED;

		pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
		pinpoint.setOffsets(7, -60.10603, DistanceUnit.MM);
		pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
		pinpoint.setEncoderDirections(ything, xthing);
		pinpoint.resetPosAndIMU();

	}

	@Override
	public void loop() {

//        telemetry.addLine("=========================Target=========================");
//        telemetry.addData("target",target);
//        telemetry.addLine("=====================Pows=========================");

		xController.setPID(X.Xp, X.Xi, X.Xd, X.Xf);
		yController.setPID(Y.Yp, Y.Yi, Y.Yd, Y.Yf);
		hController.setPID(H.Hp, H.Hi, H.Hd, H.Hf);


		pinpoint.update();

		pose = new Pose2d(-pinpoint.getPosY(DistanceUnit.INCH), pinpoint.getPosX(DistanceUnit.INCH), pinpoint.getHeading(AngleUnit.RADIANS));
		target = new Pose2d(targetX, targetY, Math.toRadians(targetH));


		double forwardPower = yController.getOutput(pose.position.y, target.position.y); // Left stick Y (inverted)
		double turnPower = hController.getOutput(normalizeAngle(pose.heading.toDouble()), normalizeAngle(target.heading.toDouble()));     // Right stick X
		double strafePower = xController.getOutput(pose.position.x, target.position.x);    // Left stick X


		// Apply deadzone
		forwardPower = Math.abs(forwardPower) > 0.05 ? forwardPower : 0;
		strafePower = Math.abs(strafePower) > 0.05 ? strafePower : 0;
		turnPower = Math.abs(turnPower) > 0.05 ? turnPower : 0;

		// Create velocity command
		PoseVelocity2d velocity = new PoseVelocity2d(
				new Vector2d(forwardPower, strafePower),
				turnPower
		);
		drive.setDrivePowers(velocity);

		telemetry.addData("pose", pose.position);
		telemetry.addData("pose head", Math.toDegrees(pose.heading.toDouble()));
		telemetry.addLine("=====================Pows=========================");
		telemetry.addData("powery", forwardPower);
		telemetry.addData("powerx", strafePower);
		telemetry.addData("powerh", turnPower);
		telemetry.update();


	}

	public static class X {
		public static double Xp = 0, Xi = 0, Xd = 0, Xf = 0;

	}

	public static class Y {
		public static double Yp = 0, Yi = 0, Yd = 0, Yf = 0;

	}

	public static class H {
		public static double Hp = 0, Hi = 0, Hd = 0, Hf = 0;
	}
}
