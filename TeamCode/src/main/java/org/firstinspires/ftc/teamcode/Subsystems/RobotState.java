package org.firstinspires.ftc.teamcode.Subsystems;

import com.acmerobotics.roadrunner.Pose2d;

/**
 * Singleton class to persist state between OpModes for the hybrid encoder system.
 */
public class RobotState {
    private static RobotState instance = null;

    // Fields for absolute-quadrature offset calibration
    public double absoluteOffset = 0.0;
    public double averageQuality = 0.0;
    public boolean hasValidData = false;

    // Fields for persisting robot position from auto to teleop
    public Pose2d lastAutoPose = null;
    public boolean hasValidAutoPose = false;

    private RobotState() {
        // Private constructor for singleton
    }

    /**
     * Thread-safe singleton access.
     */
    public static synchronized RobotState getInstance() {
        if (instance == null) {
            instance = new RobotState();
        }
        return instance;
    }

    /**
     * Clears flags and resets data.
     */
    public void reset() {
        absoluteOffset = 0.0;
        averageQuality = 0.0;
        hasValidData = false;
    }

    /**
     * Save the final pose from autonomous to use as starting pose in teleop.
     */
    public void saveAutoPose(Pose2d pose) {
        lastAutoPose = new Pose2d(pose.position.x, pose.position.y, pose.heading.toDouble());
        hasValidAutoPose = true;
    }

    /**
     * Retrieve the saved auto pose for use in teleop.
     */
    public Pose2d getAutoPose() {
        return hasValidAutoPose ? lastAutoPose : null;
    }
}