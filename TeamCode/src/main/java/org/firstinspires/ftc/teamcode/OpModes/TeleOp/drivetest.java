package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Roadrunner.Localizer;
import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Roadrunner.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.Utilities.PIDFController;


@Config
@TeleOp
public class drivetest extends OpMode {

    MecanumDrive drive;

    Pose2d pose;
    Pose2d target;

    Localizer localizer;

    public static double targetX = 0, targetY = 0, targetH = 0;



    public static class  X{
        public static double Xp = 0, Xi = 0, Xd = 0 , Xf = 0;

    }
    public static X xpid = new X();

    public static class  Y{
        public static  double Yp = 0, Yi = 0, Yd = 0, Yf = 0;

    }
    public static Y whypid = new Y();
    public static class  H{
        public static double Hp = 0, Hi = 0, Hd = 0, Hf = 0;
    }
    public static H hpid = new H();



    PIDFController xController;
    PIDFController yController;
    PIDFController hController;


    @Override
    public void init() {
        pose = new Pose2d(0, 0, 0);
        drive = new MecanumDrive(hardwareMap, pose);
        target = new Pose2d(targetX, targetY, targetH);

        localizer = new PinpointLocalizer(hardwareMap, drive.PARAMS.inPerTick,pose);

        xController = new PIDFController(xpid.Xp, xpid.Xi, xpid.Xd, xpid.Xf);
        yController = new PIDFController(whypid.Yp, whypid.Yi, whypid.Yd, whypid.Yf);
        hController = new PIDFController(hpid.Hp, hpid.Hi, hpid.Hd, hpid.Hf);

        xController.setPID(xpid.Xp, xpid.Xi, xpid.Xd,xpid.Xf);
        yController.setPID(whypid.Yp, whypid.Yi, whypid.Yd, whypid.Yf);
        hController.setPID(hpid.Hp, hpid.Hi, hpid.Hd, hpid.Hf);

    }

    @Override
    public void loop() {

        pose = localizer.getPose();
        target = new Pose2d(targetX, targetY, Math.toRadians(targetH));

        double forwardPower = yController.getOutput(pose.position.y, target.position.y); // Left stick Y (inverted)
        double turnPower = hController.getOutput(pose.heading.toDouble(), target.heading.toDouble());;     // Right stick X
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

        telemetry.addData("pose",drive.localizer.getPose());
        telemetry.addData("target",target);

    }
}
