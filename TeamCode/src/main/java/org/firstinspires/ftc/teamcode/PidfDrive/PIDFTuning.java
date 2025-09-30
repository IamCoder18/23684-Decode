package org.firstinspires.ftc.teamcode.PidfDrive;

import static org.firstinspires.ftc.teamcode.MecanumDrive.PARAMS;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;


import org.firstinspires.ftc.teamcode.Localizer;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TrigLocation;
import org.firstinspires.ftc.teamcode.TwoDeadWheelLocalizer;
@Autonomous
public class PIDFTuning extends OpMode {

    Localizer localizer;
    DrivePIDF drivepidf;
    MecanumDrive drive;
    TrigLocation trig;

    Pose2d beginPose = new Pose2d(new Vector2d(0,0),0);

    public static double target;
    public static String TuningMode = "X";

    public static double p = 0,i = 0 ,d = 0,f = 0;
    @Override
    public void init() {
        drive = new MecanumDrive(hardwareMap, beginPose);
        localizer = new TwoDeadWheelLocalizer(hardwareMap,drive.lazyImu.get(), PARAMS.inPerTick,beginPose);
        drivepidf = new DrivePIDF(hardwareMap,localizer);
        trig = new TrigLocation(drive,localizer,hardwareMap);

        drivepidf.setStartPos(beginPose.position.x,beginPose.position.y,beginPose.heading.toDouble());


    }

    @Override
    public void loop() {

        if (TuningMode == "X"){
            drivepidf.GoToPos(target,0,0);

            drivepidf.Xp = p;
            drivepidf.Xi = i;
            drivepidf.Xd = d;
            drivepidf.Xf = f;

        } else if (TuningMode == "Y") {
            drivepidf.GoToPos(0,target,0);

            drivepidf.Yp = p;
            drivepidf.Yi = i;
            drivepidf.Yd = d;
            drivepidf.Yf = f;
        }else if (TuningMode == "H") {
            drivepidf.GoToPos(0,0,0);

            drivepidf.Hp = p;
            drivepidf.Hi = i;
            drivepidf.Hd = d;
            drivepidf.Hf = f;
        }

    }
}
