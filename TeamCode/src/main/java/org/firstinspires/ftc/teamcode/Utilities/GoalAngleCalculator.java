package org.firstinspires.ftc.teamcode.Utilities;

/**
 * Utility class for autonomous navigation calculations.
 * Provides helper methods for trajectory and angle computations.
 */
public class GoalAngleCalculator {

	// Goal position constants (opponent goal location)
	private static final double GOAL_X = -60;
	private static final double GOAL_Y = -60;

	/**
	 * Calculates the angle required to shoot at the goal from a given position.
	 *
	 * <p>All coordinates and directions use the FTC Field Coordinate System, which is the
	 * same system used by Road Runner. You can find more information on this standard at the
	 * <a href="https://ftc-docs.firstinspires.org/en/latest/game_specific_resources/field_coordinate_system/field-coordinate-system.html">
	 * official FTC documentation</a>.
	 *
	 * @param x the x-coordinate of the shooting position in the FTC Field Coordinate System
	 * @param y the y-coordinate of the shooting position in the FTC Field Coordinate System
	 * @return the required robot heading (yaw) in radians to aim at the goal. This does not
	 *         represent the shooter's pitch (vertical angle).
	 */
	public static double calculateAngle(double x, double y) {
		double deltaX = GOAL_X - x;
		double deltaY = GOAL_Y - y;

		// TODO: Find if the negative is needed here, reading https://www.perplexity.ai/search/other-than-getting-the-positio-c5otZ_oTQx.URffXUJxk6A#2 for reference
		return Math.atan2(-deltaY, -deltaX);
	}
}
