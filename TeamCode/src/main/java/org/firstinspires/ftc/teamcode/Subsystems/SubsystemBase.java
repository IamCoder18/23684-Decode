package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Base class for all subsystems to reduce code duplication and standardize behavior.
 * Provides common functionality for initialization, singleton pattern, and shutdown.
 */
public abstract class SubsystemBase {
    private static final String NOT_INITIALIZED_ERROR = "Subsystem not initialized. Call initialize(hardwareMap) first.";

    /**
     * Initialize the subsystem with hardware map.
     * Should be called once during robot initialization.
     *
     * @param hardwareMap The hardware map to use for device access
     */
    public abstract void initialize(HardwareMap hardwareMap);

    /**
     * Get the singleton instance of this subsystem.
     * Throws SubsystemNotInitializedException if not initialized.
     *
     * @return The subsystem instance
     * @throws SubsystemNotInitializedException if subsystem not initialized
     */
    public abstract Object getInstance() throws SubsystemNotInitializedException;

    /**
     * Clean up resources and shutdown the subsystem.
     * Called when OpMode is stopped.
     */
    public abstract void shutdown();

    /**
     * Standardized exception for uninitialized subsystem access.
     */
    public static class SubsystemNotInitializedException extends IllegalStateException {
        public SubsystemNotInitializedException(String message) {
            super(message);
        }
    }

    /**
     * Validate that the subsystem has been initialized.
     * Throws SubsystemNotInitializedException if not initialized.
     *
     * @param initialized Flag indicating initialization state
     * @throws SubsystemNotInitializedException if not initialized
     */
    protected void validateInitialized(boolean initialized) throws SubsystemNotInitializedException {
        if (!initialized) {
            throw new SubsystemNotInitializedException(NOT_INITIALIZED_ERROR);
        }
    }
}