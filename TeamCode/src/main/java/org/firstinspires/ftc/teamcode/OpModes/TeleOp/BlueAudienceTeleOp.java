package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * TeleOp OpMode for Blue Audience starting position
 */
@TeleOp(name = "Blue Audience TeleOp", group = "Drivers")
public class BlueAudienceTeleOp extends MainTeleOp {

	@Override
	protected Pose2d getStartingPose() {
		// Blue audience starting position
		return new Pose2d(35, -23, Math.toRadians(270));
	}

	protected boolean TeamColourRed(){
		return false;
	}
}
