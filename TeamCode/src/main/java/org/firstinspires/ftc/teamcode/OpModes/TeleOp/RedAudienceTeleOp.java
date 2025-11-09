package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;

/**
 * TeleOp OpMode for Red Audience starting position
 */
@TeleOp(name = "Red Audience TeleOp", group = "Drivers")
public class RedAudienceTeleOp extends MainTeleOp {
	
	@Override
	protected Pose2d getStartingPose() {
		// Red audience starting position
		return new Pose2d(-38, -62, Math.PI / 2);
	}
}
