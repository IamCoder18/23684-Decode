package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;

/**
 * TeleOp OpMode for Blue Goal starting position
 */
@TeleOp(name = "Blue Goal TeleOp", group = "Drivers")
public class BlueGoalTeleOp extends MainTeleOp {
	
	@Override
	protected Pose2d getStartingPose() {
		// Blue goal starting position
		return new Pose2d(-8, 62, -Math.PI / 2);
	}
}
