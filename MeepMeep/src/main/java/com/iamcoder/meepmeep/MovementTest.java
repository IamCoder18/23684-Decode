package com.iamcoder.meepmeep;

import com.acmerobotics.roadrunner.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MovementTest {
	public static void main(String[] args) {
		MeepMeep meepMeep = new MeepMeep(700);

		RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
				// Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
				.setConstraints(50, 50, Math.PI, Math.PI, 15.3888)
				.build();

		myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(66, 15.5, Math.toRadians(180)))
				.lineToX(60)
				.build());

		meepMeep.setBackground(MeepMeep.Background.FIELD_DECODE_JUICE_LIGHT)
				.setDarkMode(true)
				.setBackgroundAlpha(0.95f)
				.addEntity(myBot)
				.start();
	}
}