package com.iamcoder.meepmeep;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class BlueGoal {
	public static void main(String[] args) {
		MeepMeep meepMeep = new MeepMeep(700);

		RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
				// Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
				.setConstraints(50, 50, Math.PI, Math.PI, 15.3888)
				.build();

		myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(-57.7, -44.5, Math.toRadians(54.046)))
					.strafeToSplineHeading(new Vector2d(-20, -14), Math.toRadians(45))
					.waitSeconds(3)
//					.strafeToSplineHeading(new Vector2d(-10, -30), Math.toRadians(270))
					.splineTo(new Vector2d(-10, -54), Math.toRadians(270))
					.strafeToLinearHeading(new Vector2d(-20, -14), Math.toRadians(45))
					.waitSeconds(3)
//					.strafeToSplineHeading(new Vector2d(14, -30), Math.toRadians(270))
					.splineTo(new Vector2d(14, -54), Math.toRadians(270))
					.strafeToLinearHeading(new Vector2d(-20, -14), Math.toRadians(45))
					.waitSeconds(3)
//					.strafeToSplineHeading(new Vector2d(38, -30), Math.toRadians(270))
					.splineTo(new Vector2d(38, -54), Math.toRadians(270))
					.strafeToLinearHeading(new Vector2d(-20, -14), Math.toRadians(45))
					.build());

		meepMeep.setBackground(MeepMeep.Background.FIELD_DECODE_JUICE_DARK)
				.setDarkMode(true)
				.setBackgroundAlpha(0.95f)
				.addEntity(myBot)
				.start();
	}
}