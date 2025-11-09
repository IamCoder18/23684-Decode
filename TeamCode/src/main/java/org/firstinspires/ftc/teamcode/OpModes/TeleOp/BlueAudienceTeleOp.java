package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;

/**
 * TeleOp OpMode for Blue Audience starting position
 */
@TeleOp(name = "Blue Audience TeleOp", group = "Drivers")
public class BlueAudienceTeleOp extends MainTeleOp {
	
	@Override
	protected Pose2d getStartingPose() {
		// Blue audience starting position
		return new Pose2d(66, -15.5, Math.toRadians(180));
	}
}
