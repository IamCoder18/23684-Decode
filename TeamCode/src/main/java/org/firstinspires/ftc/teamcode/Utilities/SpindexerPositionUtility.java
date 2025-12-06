package org.firstinspires.ftc.teamcode.Utilities;

/**
 * SpindexerPositionUtility provides utility methods for calculating
 * the next positions for the Spindexer to shoot or intake balls.
 * The utility uses modular arithmetic to determine positions:
 * - Shoot positions: x ≡ 0 (mod 120)
 * - Intake positions: x ≡ 20 (mod 120)
 */
public class SpindexerPositionUtility {

    /**
     * Calculates the next position for the Spindexer to shoot a ball.
     * The next shoot position is the smallest x > currentPosition where x ≡ 0 (mod 120).
     *
     * @param currentPosition the current position of the Spindexer
     * @return the next position to shoot a ball
     */
    public static int getNextShootPosition(int currentPosition) {
        int targetRemainder = 20;
        int remainder = currentPosition % 120;

        // If we're already at a shoot position (with tolerance of ±10 degrees)
        if (remainder >= targetRemainder - 10 && remainder <= targetRemainder + 10) {
            return currentPosition + 120;
        }
        // If we've passed the shoot position in this cycle
        else if (remainder > targetRemainder) {
            return currentPosition + (120 - remainder + targetRemainder);
        }

        // If we haven't reached the shoot position yet in this cycle
        else {
            return currentPosition + (targetRemainder - remainder);
        }
    }

    /**
     * Calculates the next position for the Spindexer to intake a ball.
     * This finds the next position where (position % 120) == 20
     *
     * @param currentPosition the current position of the Spindexer
     * @return the next position to intake a ball
     */
    public static int getNextIntakePosition(int currentPosition) {
        // Calculate the remainder when current position is divided by 120
        int remainder = currentPosition % 120;

        // If remainder is close to 0, current position is already an intake position
        // with tolerance of ±10 degrees, so next intake position is currentPosition + 120
        if (remainder < 10 || remainder > 110) {
        return currentPosition + 120;
        }

        // Otherwise, next intake position is currentPosition + (120 - remainder)
        return currentPosition + (120 - remainder);
    }
}
