package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.teamcode.MecanumDrive.PARAMS;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class TrigLocation {


    MecanumDrive drive;

    Vector2d vector2d;

    Vector2d redGoal = new Vector2d(59,59);
    Vector2d bluGoal = new Vector2d(59,-59);

    public Localizer localizer;// = new TwoDeadWheelLocalizer(hardwareMap,drive.lazyImu.get(), PARAMS.inPerTick, currentPos);

    public Pose2d currentPos;
    public double redHypothenus = 0;
    public double redAjecentAngle = 0;

    public double bluHypothenus = 0;
    public double bluAjecentAngle = 0;





    public TrigLocation(MecanumDrive mecanumDrive,Localizer localizer, HardwareMap hardwareMap){
        this.drive = mecanumDrive;
        this.localizer = localizer;



    }



    public double TurnToRed() {
        currentPos = localizer.getPose();
        double diffX = redGoal.x - currentPos.position.x;
        double diffY = redGoal.y - currentPos.position.y;

        double c = (diffY * diffY) + (diffX * diffX);
        redHypothenus = Math.sqrt(c);

        redAjecentAngle = Math.atan2(diffY,diffX);

        return normalizeAngle(redAjecentAngle);

    }

    public double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public double TurnToBlue() {
        currentPos = localizer.getPose();
        double diffX = bluGoal.x - currentPos.position.x;
        double diffY = bluGoal.y - currentPos.position.y;

        double c = (diffY * diffY) + (diffX * diffX);
        bluHypothenus = Math.sqrt(c);

        bluAjecentAngle = diffY/diffX;

        return normalizeAngle(bluAjecentAngle);
    }
}

