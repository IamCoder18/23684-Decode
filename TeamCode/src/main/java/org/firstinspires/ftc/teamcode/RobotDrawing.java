package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.bylazar.field.PanelsField;
import com.bylazar.field.FieldManager;
import com.bylazar.field.Style;

import java.util.LinkedList;

/**
 * Utility class to draw the robot's current position on the Panels field.
 */
public class RobotDrawing {

	private final FieldManager fieldManager;
	private final LinkedList<Pose2d> trail = new LinkedList<>();
	private static final int TRAIL_LENGTH = 20; // number of past positions to keep

	// Robot dimensions in inches
	private final double robotWidth;
	private final double robotLength;

	/**
	 * Constructor that allows robot size to be specified.
	 * @param robotWidth  width of the robot in inches
	 * @param robotLength length of the robot in inches
	 */
	public RobotDrawing(double robotWidth, double robotLength) {
		this.robotWidth = robotWidth;
		this.robotLength = robotLength;

		fieldManager = PanelsField.INSTANCE.getField();
		fieldManager.setOffsets(PanelsField.INSTANCE.getPresets().getROAD_RUNNER());

		// Default style for robot marker (light translucent red for dark mode)
		fieldManager.setStyle(new Style(
				"rgba(255,100,100,0.6)",   // fill color
				"rgba(255,50,50,1.0)",     // outline color
				2.0                        // outline width
		));
	}

	/**
	 * Draws the robot at the given pose.
	 * @param pose Current robot pose (x, y, heading in radians)
	 */
	public void drawRobot(Pose2d pose) {
		double x = pose.position.x;
		double y = pose.position.y;
		double heading = pose.heading.toDouble();

		// Add current pose to trail
		trail.add(new Pose2d(x, y, heading));
		if (trail.size() > TRAIL_LENGTH) {
			trail.removeFirst();
		}

		// Draw trail (fading effect)
		int i = 0;
		for (Pose2d pastPose : trail) {
			double alpha = (double) i / TRAIL_LENGTH; // fade older positions
			fieldManager.setStyle(new Style(
					"rgba(255,100,100," + (0.2 + 0.4 * (1 - alpha)) + ")", // fading fill
					"rgba(255,50,50," + (0.5 * (1 - alpha)) + ")",         // fading outline
					1.0
			));
			fieldManager.moveCursor(pastPose.position.x, pastPose.position.y);
			fieldManager.circle(2.0); // small marker for trail
			i++;
		}

		// Draw robot body as a rotated rectangle
		fieldManager.setStyle(new Style(
				"rgba(255,100,100,0.6)",   // main robot fill
				"rgba(255,50,50,1.0)",     // outline
				2.0
		));

		// Half dimensions
		double halfL = robotLength / 2.0;
		double halfW = robotWidth / 2.0;

		// Rectangle corners relative to center
		double[][] corners = {
				{ halfL,  halfW},  // front-right
				{ halfL, -halfW},  // front-left
				{-halfL, -halfW},  // back-left
				{-halfL,  halfW}   // back-right
		};

		// Rotate and translate corners
		double cosH = Math.cos(heading);
		double sinH = Math.sin(heading);
		double[][] worldCorners = new double[4][2];
		for (int j = 0; j < 4; j++) {
			double cx = corners[j][0];
			double cy = corners[j][1];
			double rx = cx * cosH - cy * sinH;
			double ry = cx * sinH + cy * cosH;
			worldCorners[j][0] = x + rx;
			worldCorners[j][1] = y + ry;
		}

		// Draw polygon edges
		for (int j = 0; j < 4; j++) {
			int k = (j + 1) % 4;
			fieldManager.moveCursor(worldCorners[j][0], worldCorners[j][1]);
			fieldManager.line(worldCorners[k][0], worldCorners[k][1]);
		}

		// Draw heading line (from center forward)
		double lineLength = 12.0;
		double x2 = x + lineLength * cosH;
		double y2 = y + lineLength * sinH;

		fieldManager.moveCursor(x, y);
		fieldManager.line(x2, y2);

		// Push update to canvas
		fieldManager.update();
	}
}
