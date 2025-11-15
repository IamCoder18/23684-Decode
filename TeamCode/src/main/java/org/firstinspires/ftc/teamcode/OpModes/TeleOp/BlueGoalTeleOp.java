package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * TeleOp OpMode for Blue Goal starting position
 */
@TeleOp(name = "Blue Goal TeleOp", group = "Drivers")
public class BlueGoalTeleOp extends MainTeleOp {

	@Override
	protected Pose2d getStartingPose() {
		// Blue goal starting position
		return new Pose2d(-41.25, -52.5, Math.toRadians(90));
	}
}
