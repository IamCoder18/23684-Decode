package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.Roadrunner.MecanumDrive;
import org.firstinspires.ftc.teamcode.Subsystems.StupidIntake;
import org.firstinspires.ftc.teamcode.Subsystems.StupidShooter;
import org.firstinspires.ftc.teamcode.Subsystems.StupidTransfer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Subsystems.stupidSpindexer;


@Autonomous
public class spindexerTest extends OpMode {

    stupidSpindexer spindexer;
    StupidShooter shooter;
    Pose2d beginPose;

    Pose2d NextPose;

    StupidTransfer transfer;

    StupidIntake intake;

    CRServo transferRight;
    CRServo transferLeft;

    public double topRpM = 2000;




    TelemetryPacket telemetryPacket;
    MecanumDrive drive;

    public static double shootingX = 57, shootingY = -23; // this is the position used shooting
    public static double  Goalx = -60, Goaly = -60; // this is the position of the goal

    public static double AngleofShot(double x, double y){
        double diffx = Goalx - x;
        double diffy = Goaly - y;

        double angle = Math.atan2(-diffy, -diffx);
        return angle;
    }

    TrajectoryActionBuilder tab1;

    TrajectoryActionBuilder tab2;


    @Override
    public void init() {
        shooter = new StupidShooter(hardwareMap);
        spindexer = new stupidSpindexer(hardwareMap);
        transfer = new StupidTransfer(hardwareMap);
        intake = new StupidIntake(hardwareMap);
        beginPose = new Pose2d(60, -9,Math.toRadians(0));
        drive = new MecanumDrive(hardwareMap, beginPose);


       tab1 = drive.actionBuilder(new Pose2d(60, -9,Math.toRadians(0)))
                .strafeToLinearHeading(new Vector2d(shootingX, shootingY), AngleofShot(shootingX,shootingY));

         tab2 = drive.actionBuilder(new Pose2d(shootingX, shootingY,AngleofShot(shootingX,shootingY)))
                .strafeToLinearHeading(new Vector2d(35,-23), Math.toRadians(270));

        transferRight = hardwareMap.get(CRServo.class, "transferRight");
        transferLeft = hardwareMap.get(CRServo.class, "transferLeft");

        transferRight.setDirection(DcMotorSimple.Direction.REVERSE);


    }

    public void start() {
        Actions.runBlocking(
                new SequentialAction(
                     new ParallelAction(
                             tab1.build(),
                             shooter.WindUp()
                             ),
                        new ParallelAction(
                                spindexer.MindlessSpindexer(),
                                intake.intakeDoorIn()
                        ),
                 // ball 1
                shooter.WaitForSpike(),
                        shooter.WindUp(),
                new ParallelAction(
                        spindexer.MindlessSpindexer(),
                        intake.intakeDoorIn()
                ),
                        // ball 2
                shooter.WaitForSpike(),
                        new ParallelAction(
                                shooter.WindUp()
                        ),
                new ParallelAction(
                  spindexer.MindlessSpindexer(),
                   intake.intakeDoorIn()
                ),
                        // ball 3
                shooter.WaitForSpike(),
                tab2.build()

        )
                //spindexer.Zero(),
                //spindexer.NextSlot()
        );
    }


    @Override
    public void loop() {
        shooter.updateRPM();

        if (shooter.averageRPM >= topRpM){
            transferRight.setPower(1);
            transferLeft.setPower(1);
        } else {
            transferRight.setPower(-1);
            transferLeft.setPower(-1);
        }


    }
}
