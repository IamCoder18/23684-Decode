package org.firstinspires.ftc.teamcode.OpModes.Auto;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Blue Alliance Audience-side autonomous mode.
 * Extends the abstract AudienceAuto with blue-specific poses and positions.
 */
@Autonomous(name = "Blue Audience Auto", group = "Competition")
public class BlueAudienceAuto extends AudienceAuto {

	private static final double BLUE_SHOOTING_X = 57;
	private static final double BLUE_SHOOTING_Y = -23;
	private static final double BLUE_GOAL_X = -60;
	private static final double BLUE_GOAL_Y = -60;
	private static final Pose2d BLUE_START_POSE = new Pose2d(60, -9, Math.toRadians(0));
	private static final Pose2d BLUE_INITIAL_TRAJECTORY_START = new Pose2d(54, -9, Math.toRadians(0));
	private static final Vector2d BLUE_COLLECTION_POSITION = new Vector2d(35, -23);
	private static final double BLUE_COLLECTION_HEADING = 270;

	@Override
	protected double getShootingX() {
		return BLUE_SHOOTING_X;
	}

	@Override
	protected double getShootingY() {
		return BLUE_SHOOTING_Y;
	}

	@Override
	protected double getGoalX() {
		return BLUE_GOAL_X;
	}

	@Override
	protected double getGoalY() {
		return BLUE_GOAL_Y;
	}

	@Override
	protected Pose2d getStartPose() {
		return BLUE_START_POSE;
	}

	@Override
	protected Pose2d getInitialTrajectoryStartPose() {
		return BLUE_INITIAL_TRAJECTORY_START;
	}

	@Override
	protected Vector2d getCollectionPosition() {
		return BLUE_COLLECTION_POSITION;
	}

	@Override
	protected double getCollectionHeading() {
		return BLUE_COLLECTION_HEADING;
	}
}
