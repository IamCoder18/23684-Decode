package org.firstinspires.ftc.teamcode.LifecycleManagementUtilities;

import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;

/**
 * Handles periodic updates for subsystems that require it.
 * Call this once per loop cycle in your OpMode.
 */
public class SubsystemUpdater {
	/**
	 * Updates all subsystems that require periodic updates.
	 * Must be called every loop cycle for proper operation.
	 */
	public static void update() {
		// Update PID controller for spindexer position control
		Spindexer.getInstance().update();
	}
}
