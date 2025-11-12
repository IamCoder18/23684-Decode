package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.StupidShooter;
import org.firstinspires.ftc.teamcode.Subsystems.StupidTransfer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Subsystems.stupidSpindexer;


@Autonomous
public class spindexerTest extends OpMode {

    stupidSpindexer spindexer;
    StupidShooter shooter;
    Pose2d beginPose;

    StupidTransfer transfer;


    TelemetryPacket telemetryPacket;
    MecanumDrive drive;

    @Override
    public void init() {
        shooter = new StupidShooter(hardwareMap);
        spindexer = new stupidSpindexer(hardwareMap);
        transfer = new StupidTransfer(hardwareMap);
        beginPose = new Pose2d(0, 0, Math.toRadians(90));
        drive = new MecanumDrive(hardwareMap, beginPose);


    }

    public void start() {
        Actions.runBlocking(
                new SequentialAction(
                     new ParallelAction(
                             drive.actionBuilder(beginPose)
                                     .strafeToConstantHeading(new Vector2d(0, 24))
                                     .build(),
                             shooter.WindUp()
                             ),
                transfer.tranferOut(),
                spindexer.NextSlot(), //ball 1
                shooter.WindUp(),
                transfer.tranferOut(),
                spindexer.NextSlot(), //ball 2
                shooter.WindUp(),
                transfer.tranferOut(),
                spindexer.NextSlot() //ball 3

                //spindexer.Zero(),
                //spindexer.NextSlot()
                )
        );
    }


    @Override
    public void loop() {


    }
}
