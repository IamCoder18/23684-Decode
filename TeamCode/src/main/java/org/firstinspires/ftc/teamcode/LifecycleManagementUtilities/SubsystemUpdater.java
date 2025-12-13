package org.firstinspires.ftc.teamcode.LifecycleManagementUtilities;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.DistanceDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.TouchDetector;

/**
 * Handles periodic updates for subsystems that require continuous processing.
 * Call this once per loop cycle in your OpMode.
 */
public class SubsystemUpdater {
	// Telemetry throttling to reduce loop time
	private static int telemetryCounter = 0;
	public static int TELEMETRY_INTERVAL = 5; // Only send every 5th loop

	/**
	 * Updates all subsystems that require periodic updates.
	 * Must be called every loop cycle for proper PID control and sensor readings.
	 */
	public static void update() {
		// Critical: Update shooter RPM calculations every loop
		Shooter.getInstance().updateRPM(System.nanoTime());

		// Throttled telemetry updates to reduce loop overhead
		if (telemetryCounter++ % TELEMETRY_INTERVAL == 0) {
			TelemetryPacket telemetryPacket = new TelemetryPacket();
			ColorDetector.getInstance().update().run(telemetryPacket);
			DistanceDetector.getInstance().update().run(telemetryPacket);
			FtcDashboard.getInstance().sendTelemetryPacket(telemetryPacket);
		}
	}
}
