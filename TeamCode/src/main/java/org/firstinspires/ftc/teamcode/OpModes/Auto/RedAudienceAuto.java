package org.firstinspires.ftc.teamcode.OpModes.Auto;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Red Alliance Audience-side autonomous mode.
 * Extends the abstract AudienceAuto with red-specific poses and positions.
 * Uses pose mirroring across the Y-axis for field-relative coordinates.
 */
@Autonomous(name = "Red Audience Auto", group = "Competition")
public class RedAudienceAuto extends AudienceAuto {

	// Red positions are mirror images of blue across the Y-axis
	private static final double RED_SHOOTING_X = 54.75;
	private static final double RED_SHOOTING_Y = 10.25;
	private static final double RED_GOAL_X = -72;
	private static final double RED_GOAL_Y = 72;
	private static final Pose2d RED_START_POSE = new Pose2d(60, 9, Math.toRadians(0));
	private static final Pose2d RED_INITIAL_TRAJECTORY_START = new Pose2d(55, 9, Math.toRadians(0));
	private static final Vector2d RED_COLLECTION_POSITION = new Vector2d(35, 23);
	private static final double RED_COLLECTION_HEADING = 90;
	private static final double ShootHeading = 35;

	@Override
	protected double getShootingX() {
		return RED_SHOOTING_X;
	}

	@Override
	protected double getShootingY() {
		return RED_SHOOTING_Y;
	}

	@Override
	protected double getGoalX() {
		return RED_GOAL_X;
	}

	@Override
	protected double getGoalY() {
		return RED_GOAL_Y;
	}

	@Override
	protected Pose2d getStartPose() {
		return RED_START_POSE;
	}

	@Override
	protected Pose2d getInitialTrajectoryStartPose() {
		return RED_INITIAL_TRAJECTORY_START;
	}

	@Override
	protected Vector2d getCollectionPosition() {
		return RED_COLLECTION_POSITION;
	}

	@Override
	protected double getCollectionHeading() {
		return RED_COLLECTION_HEADING;
	}

	@Override
	protected double GetShootingHeading(){return ShootHeading;}
}
