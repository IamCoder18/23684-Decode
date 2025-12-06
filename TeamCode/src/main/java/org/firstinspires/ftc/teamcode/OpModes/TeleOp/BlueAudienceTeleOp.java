package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Utilities.Team;

/**
 * TeleOp OpMode for Blue Audience starting position
 */
@TeleOp(name = "Blue Audience TeleOp", group = "Drivers")
public class BlueAudienceTeleOp extends MainTeleOp {

	@Override
	protected Pose2d getStartingPose() {
		// Blue audience starting position
		return new Pose2d(36, -32, Math.toRadians(270));
	}

	@Override
	protected Team getTeam(){
		return Team.BLUE;
	}
}
