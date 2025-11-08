package org.firstinspires.ftc.teamcode.LifecycleManagementUtilities;

import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;

/**
 * Shuts down all subsystems cleanly.
 * Call this in your OpMode's stop() method.
 */
public class HardwareShutdown {
    public static void shutdown() {
        // Shut down in reverse order of initialization
        Spindexer.shutdown();
        Intake.shutdown();
        Shooter.shutdown();
        Transfer.shutdown();
        ColorDetector.shutdown();
    }
}
