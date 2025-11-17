package org.firstinspires.ftc.teamcode.Utilities;

/**
 * Enumeration of ball colors that can be detected in the game.
 * Used to track which balls are in each slot of the spindexer.
 */
public enum BallColor {
	/**
	 * Green ball color
	 */
	GREEN,
	/**
	 * Purple/Blue ball color
	 */
	PURPLE,
	/**
	 * Ball detected in slot but color not yet determined
	 */
	UNKNOWN,
	/**
	 * No ball in this slot
	 */
	EMPTY
}
