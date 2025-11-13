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
        beginPose = new Pose2d(-58.45, -44.57, Math.toRadians(54.046));
        drive = new MecanumDrive(hardwareMap, beginPose);


    }

    public void start() {
        Actions.runBlocking(
                new SequentialAction(
                     new ParallelAction(
                             drive.actionBuilder(beginPose)
                                     .strafeToConstantHeading(new Vector2d(-16, -16))
                                     .build(),
                             shooter.WindUp()
                             ),
                transfer.tranferOut(), // ball 1
                shooter.WaitForSpike(),
                 transfer.tranferIn(),
                spindexer.NextSlot(),
                shooter.WindUp(),
                transfer.tranferOut(), // ball 2
                shooter.WaitForSpike(),
                transfer.tranferIn(),
                spindexer.NextSlot(),
                shooter.WindUp(),
                transfer.tranferOut() // ball 3

                //spindexer.Zero(),
                //spindexer.NextSlot()
                )
        );
    }


    @Override
    public void loop() {


    }
}
