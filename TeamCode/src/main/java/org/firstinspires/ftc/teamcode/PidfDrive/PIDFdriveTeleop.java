package org.firstinspires.ftc.teamcode.PidfDrive;

import static org.firstinspires.ftc.teamcode.MecanumDrive.PARAMS;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Localizer;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TrigLocation;
import org.firstinspires.ftc.teamcode.TwoDeadWheelLocalizer;

import java.util.Objects;


@TeleOp
public class PIDFdriveTeleop extends OpMode {

    Localizer localizer;
    DrivePIDF drivepidf;

    MecanumDrive drive;

    TrigLocation trig;

    double X,Y,H;

    double Dx, Dy,Dh;
    double aimTrigger, parkTrigger;

    public String allianceGoal = "RED GOAL";



    Pose2d beginPose = new Pose2d(new Vector2d(0,0),0);

    @Override
    public void init() {
        drive = new MecanumDrive(hardwareMap, beginPose);
        localizer = new TwoDeadWheelLocalizer(hardwareMap,drive.lazyImu.get(), PARAMS.inPerTick,beginPose);
        drivepidf = new DrivePIDF(hardwareMap,localizer);
        trig = new TrigLocation(drive,localizer,hardwareMap);

        X = gamepad1.left_stick_y;
        Y = gamepad1.left_stick_x;
        H = gamepad1.right_stick_x;
        aimTrigger = gamepad1.right_trigger;
        parkTrigger = gamepad1.right_trigger;
        drivepidf.setStartPos(beginPose.position.x,beginPose.position.y,beginPose.heading.toDouble());

        Dx = beginPose.position.x;
        Dy = beginPose.position.y;
        Dh = beginPose.heading.toDouble();

    }

    @Override
    public void loop() {


        if (parkTrigger <= 0.5) {
            if (X != 0) {
                Dx += X * 12.2;
            } //else {
//                Dx = pose.position.x;
//            }

            if (Y != 0) {
                Dy += Y * 12.2;
            } //else {
//                Dy = pose.position.y;
//            }

            if (aimTrigger != 0) {
                if (H != 0) {
                    Dh += trig.normalizeAngle(X * Math.toRadians(30));

                } //else {
//                   Dh = pose.heading.toDouble();
//                }
            } else {
                if (Objects.equals(allianceGoal, "RED GOAL")) {
                    Dh = trig.TurnToRed();
                }else if (Objects.equals(allianceGoal, "BLUEGOAL")){
                    Dh = trig.TurnToBlue();
                }
            }
        }else{
            Dy = 37;
            Dx = -33;
            Dh = Math.toRadians(90);
        }

        drivepidf.GoToPos(Dx,Dy,Dh);


        telemetry.addData("Status", "Running");
        telemetry.addData("X,Y,H", localizer.getPose());

    }
}
