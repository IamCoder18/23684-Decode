package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * TeleOp OpMode for Red Goal starting position
 */
@TeleOp(name = "Red Goal TeleOp", group = "Drivers")
public class RedGoalTeleOp extends MainTeleOp {

	@Override
	protected Pose2d getStartingPose() {
		// Red goal starting position
		return new Pose2d(-41.25, 52.5, Math.toRadians(270));
	}
}
