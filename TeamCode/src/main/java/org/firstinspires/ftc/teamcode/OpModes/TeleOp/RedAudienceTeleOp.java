package org.firstinspires.ftc.teamcode.OpModes.TeleOp;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Utilities.Team;

/**
 * TeleOp OpMode for Red Audience starting position
 */
@TeleOp(name = "Red Audience TeleOp", group = "Drivers")
public class RedAudienceTeleOp extends MainTeleOp {

	@Override
	protected Pose2d getStartingPose() {
		// Red audience starting position
		return new Pose2d(66, 15.5, Math.toRadians(180));
	}

	@Override
	protected Team getTeam(){
		return Team.RED;
	}
}
