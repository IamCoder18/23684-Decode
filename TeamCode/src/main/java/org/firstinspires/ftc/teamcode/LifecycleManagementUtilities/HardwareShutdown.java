package org.firstinspires.ftc.teamcode.LifecycleManagementUtilities;

import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.RGBIndicator;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;

/**
 * Shuts down all subsystems cleanly.
 * Call this in your OpMode's stop() method.
 */
public class HardwareShutdown {
	public static void shutdown() {
		// Shut down in reverse order of initialization
		Spindexer.shutdown();
		RGBIndicator.shutdown();
		Intake.shutdown();
		Shooter.shutdown();
		Transfer.shutdown();
		ColorDetector.shutdown();
	}
}
