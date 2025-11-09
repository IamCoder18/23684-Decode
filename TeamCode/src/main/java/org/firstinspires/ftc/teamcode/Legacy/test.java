package org.firstinspires.ftc.teamcode.Legacy;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;


@Config
@TeleOp
public class test extends OpMode {

	public static double target = 0.7;
	public double timmythetime = 0;
	DcMotor Decodetest;
	DcMotor motor;
	DcMotor intake;
	CRServo spindexer;
	CRServo intakeDoorLeft;
	CRServo transferLeft;
	;
	CRServo transferRight;
	CRServo intakeDoorRight;
	String string = "IM A STRING";

	MultipleTelemetry multipleTelemetry;

	long lastTimeNanos;
	int lastPos;
	double CPR = 1440.0;


	int liftTarget = 0;


	@Override
	public void init() {
		timmythetime += 0.5;

		Decodetest = hardwareMap.get(DcMotor.class, "upperShooter");
		motor = hardwareMap.get(DcMotor.class, "lowerShooter");
		intake = hardwareMap.get(DcMotor.class, "intake");

		spindexer = hardwareMap.get(CRServo.class, "spindexer");
		intakeDoorLeft = hardwareMap.get(CRServo.class, "intakeDoorLeft");
		intakeDoorRight = hardwareMap.get(CRServo.class, "intakeDoorRight");

		transferLeft = hardwareMap.get(CRServo.class, "transferLeft");
		transferRight = hardwareMap.get(CRServo.class, "transferRight");


		intakeDoorLeft.setDirection(DcMotorSimple.Direction.REVERSE);
		transferLeft.setDirection(DcMotorSimple.Direction.REVERSE);

		Decodetest.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
		motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

		multipleTelemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

		lastTimeNanos = System.nanoTime();
		lastPos = motor.getCurrentPosition();


	}

	@Override
	public void loop() {


		long nowNanos = System.nanoTime();
		int nowPos = (motor.getCurrentPosition() + Decodetest.getCurrentPosition()) / 2;
		double dt = (nowNanos - lastTimeNanos) / 1e9; // seconds
		int dCounts = nowPos - lastPos; // signed delta (handles direction)

		// compute RPM
		double revs = dCounts / CPR;
		double rpm = (revs / dt) * 60.0;

		// optionally convert to rad/s
		double radPerSec = (revs * 2.0 * Math.PI) / dt;

		// update for next iteration
		lastTimeNanos = nowNanos;
		lastPos = nowPos;

		if (target != 0) {
			transferLeft.setPower(-1);
			transferRight.setPower(-1);

			Decodetest.setPower((0.1 * Math.sin(timmythetime * 50) + target));
			motor.setPower((0.1 * Math.sin(timmythetime * 50) + target));
		} else {
			Decodetest.setPower(0);
			motor.setPower(0);
		}
		target = gamepad1.left_stick_y;

		if (gamepad1.left_trigger > 0) {
			intake.setPower((0.1 * Math.sin(timmythetime * 50) + -1));

			spindexer.setPower(-1);


			intakeDoorLeft.setPower(1);
			intakeDoorRight.setPower(1);


		} else {
			intake.setPower(0);
			intakeDoorLeft.setPower(0);
			intakeDoorRight.setPower(0);
			spindexer.setPower(0);
			transferLeft.setPower(0);
			transferRight.setPower(0);
		}

		if (gamepad1.a) {
			spindexer.setPower(1);
		} else {
			spindexer.setPower(0);
		}

		if (gamepad1.b) {
			spindexer.setPower(-1);
		} else {
			spindexer.setPower(0);
		}


		multipleTelemetry.addData(" speed", rpm);
		////        telemetry.put("Wrist", wrist.getPosition());
		multipleTelemetry.update();

	}
}
