package org.firstinspires.ftc.teamcode.RROpmodes;

import static org.firstinspires.ftc.teamcode.MecanumDrive.PARAMS;


import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.AccelConstraint;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.AngularVelConstraint;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.MinVelConstraint;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ProfileAccelConstraint;
import com.acmerobotics.roadrunner.Profiles;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.TranslationalVelConstraint;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.VelConstraint;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Localizer;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TrigLocation;
import org.firstinspires.ftc.teamcode.TwoDeadWheelLocalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;

@TeleOp
public class roadRUNNERv1 extends OpMode {
    Pose2d beginPose;

    Profiles profiles;

    MecanumDrive drive;
    Localizer localizer;
    TrigLocation trig;

    VelConstraint vel;
    public int timer = 0;
    private FtcDashboard dash = FtcDashboard.getInstance();
    private List<Action> runningActions = new ArrayList<>();
    double Dx, Dy,Dh;
    double X;
    double Y;
    double H;
    double aimTrigger;
    double parkTrigger;
    public String allianceGoal = "RED GOAL";
    // red park is 37, -33

    @Override
    public void init() {
        beginPose = new Pose2d(0, 0, Math.toRadians(180));
        drive = new MecanumDrive(hardwareMap, beginPose);
        localizer = new TwoDeadWheelLocalizer(hardwareMap,drive.lazyImu.get(), PARAMS.inPerTick,beginPose);
        trig = new TrigLocation(drive,localizer,hardwareMap);
        Dx = beginPose.position.x;
        Dy = beginPose.position.y;
        Dh = beginPose.heading.toDouble();

        X = gamepad1.left_stick_y;
        Y = gamepad1.left_stick_x;
        H = gamepad1.right_stick_x;
        aimTrigger = gamepad1.right_trigger;
        parkTrigger = gamepad1.right_trigger;

    }
    @Override
    public void loop() {
        telemetry.addData("Status", "Running");
        telemetry.addData("X,Y,H", localizer.getPose());
        telemetry.addLine(allianceGoal);
        telemetry.update();
        localizer.update();
        Pose2d pose = drive.localizer.getPose();





        timer += 1;


        TelemetryPacket packet = new TelemetryPacket();

        // updated based on gamepads

        // update running actions
        List<Action> newActions = new ArrayList<>();
        for (Action action : runningActions) {
            action.preview(packet.fieldOverlay());
            if (action.run(packet)) {
                newActions.add(action);
            }
        }
        runningActions = newActions;

        dash.sendTelemetryPacket(packet);




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

//        Actions.runBlocking(
//                drive.actionBuilder(beginPose)
//                        .strafeToLinearHeading(new Vector2d(Dx,Dy),Dh)
//                        .build());



        if (timer >= 100) {
            runningActions.add(new SequentialAction(
                    new InstantAction(() -> drive.actionBuilder(pose)
                            .strafeToLinearHeading(new Vector2d(Dx,Dy),Dh)
                            .build())
            ));
        }
    }
}

