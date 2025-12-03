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
        // Calculate the remainder when current position is divided by 120
        int remainder = currentPosition % 120;

        // If remainder is 0, current position is already a shoot position
        // so next shoot position is currentPosition + 120
        if (remainder == 0) {
            return currentPosition + 120;
        }

        // Otherwise, next shoot position is currentPosition + (120 - remainder)
        return currentPosition + (120 - remainder);
    }

    /**
     * Calculates the next position for the Spindexer to intake a ball.
     * The next intake position is the smallest x > currentPosition where x ≡ 20 (mod 120).
     *
     * @param currentPosition the current position of the Spindexer
     * @return the next position to intake a ball
     */
    public static int getNextIntakePosition(int currentPosition) {
        // Calculate how far we are from the next intake position
        int remainder = (currentPosition - 20) % 120;

        // If remainder is 0, current position is already an intake position
        // so next intake position is currentPosition + 120
        if (remainder == 0) {
            return currentPosition + 120;
        }

        // If remainder is negative, we need to adjust it
        if (remainder < 0) {
            remainder += 120;
        }

        // Next intake position is currentPosition + (120 - remainder)
        return currentPosition + (120 - remainder);
    }

    /**
     * Alternative implementation of getNextIntakePosition that might be clearer
     * This version finds the next position where (position % 120) == 20
     *
     * @param currentPosition the current position of the Spindexer
     * @return the next position to intake a ball
     */
    public static int getNextIntakePositionAlternative(int currentPosition) {
        int targetRemainder = 20;
        int remainder = currentPosition % 120;

        // If we're already at an intake position
        if (remainder == targetRemainder) {
            return currentPosition + 120;
        }
        // If we've passed the intake position in this cycle
        else if (remainder > targetRemainder) {
            return currentPosition + (120 - remainder + targetRemainder);
        }
        // If we haven't reached the intake position yet in this cycle
        else {
            return currentPosition + (targetRemainder - remainder);
        }
    }
}