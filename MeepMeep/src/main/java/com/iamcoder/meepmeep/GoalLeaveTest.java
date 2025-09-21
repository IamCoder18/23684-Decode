package com.iamcoder.meepmeep;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class GoalLeaveTest {
	public static void main(String[] args) {
		MeepMeep meepMeep = new MeepMeep(700);

		RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
				// Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
				.setConstraints(50, 50, Math.PI, Math.PI, 15.3888)
				.build();

		myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(-41.25, 52.5, Math.toRadians(270)))
				.strafeTo(new Vector2d(-30, 52))
				.build());

		meepMeep.setBackground(MeepMeep.Background.FIELD_DECODE_JUICE_DARK)
				.setDarkMode(true)
				.setBackgroundAlpha(0.95f)
				.addEntity(myBot)
				.start();
	}
}