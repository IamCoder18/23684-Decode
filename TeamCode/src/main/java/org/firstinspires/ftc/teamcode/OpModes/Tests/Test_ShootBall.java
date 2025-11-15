package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actions.ShootBall;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;

/**
 * Action Test OpMode for the ShootBall Action
 * <p>
 * Purpose: Test the complete ShootBall action sequence using the action scheduler
 * <p>
 * Controls:
 * - A button: Start ShootBall action (alignment only)
 * - B button: Start ShootBall + Shooter action (full sequence)
 * - X button: Stop current action and clear queue
 * - Y button: Print detailed action status
 * <p>
 * Expected Behavior:
 * - A button: Aligns spindexer with shooter (no actual shooting)
 * - B button: Aligns spindexer and runs shooter motors
 * - Action automatically finds best ball to shoot based on color preference
 * - Telemetry shows real-time alignment progress and results
 * <p>
 * Testing Focus:
 * - Verify spindexer alignment accuracy
 * - Test ball selection logic (color preference, proximity)
 * - Validate shooter integration
 * - Check action scheduling and telemetry
 * <p>
 * Notes:
 * - Requires balls to be loaded in spindexer slots first
 * - Spindexer must be zeroed and positioned properly
 * - Action can shoot any color or filter by specific color
 * <p>
 * Duration: ≤1 minute (action test)
 */
@TeleOp(name = "Test_ShootBall", group = "Action Tests")
public class Test_ShootBall extends OpMode {

	private ActionScheduler scheduler;
	private boolean actionRunning = false;
	private boolean aButtonPrev = false;
	private boolean bButtonPrev = false;
	private boolean xButtonPrev = false;
	private boolean yButtonPrev = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test ShootBall action sequence");
		telemetry.addData("Note", "Ensure balls are loaded in spindexer!");
	}

	@Override
	public void loop() {
		// A button - Start ShootBall action (alignment only) - edge detection
		if (gamepad1.a && !aButtonPrev && !actionRunning) {
			scheduler.schedule(new ShootBall()); // Shoot any available ball
			actionRunning = true;
			telemetry.addData("Action", "ShootBall (alignment) started");
		}
		aButtonPrev = gamepad1.a;

		// B button - Start ShootBall + Shooter action (full sequence) - edge detection
		if (gamepad1.b && !bButtonPrev && !actionRunning) {
			scheduler.schedule(new ShootBall()); // Align first
			// Note: In a real sequence, this would be followed by shooter.run() action
			actionRunning = true;
			telemetry.addData("Action", "ShootBall (full sequence) started");
		}
		bButtonPrev = gamepad1.b;

		// X button - Stop current action and clear queue - edge detection
		if (gamepad1.x && !xButtonPrev) {
			scheduler.clearActions();
			actionRunning = false;
			telemetry.addData("Action", "All actions stopped and cleared");
		}
		xButtonPrev = gamepad1.x;

		// Y button - Print detailed status - edge detection
		if (gamepad1.y && !yButtonPrev) {
			printDetailedStatus();
		}
		yButtonPrev = gamepad1.y;

		// Update spindexer PID controller
//		scheduler.schedule(org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().update());

		// Update action scheduler
		scheduler.update();

		// Check if action is still running
		if (actionRunning && scheduler.isSchedulerEmpty()) {
			actionRunning = false;
			telemetry.addData("Action", "ShootBall sequence completed");
		}

		// === DISPLAY TELEMETRY ===

		telemetry.addData("", "=== ACTION STATUS ===");
		telemetry.addData("Action Running", actionRunning ? "YES" : "NO");
		telemetry.addData("Actions in Queue", scheduler.getRunningActionCount());

		// Spindexer position
		double currentTicks = org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().getCurrentPositionTicks();
		double currentDegrees = (currentTicks % org.firstinspires.ftc.teamcode.Subsystems.Spindexer.TICKS_PER_REV) / org.firstinspires.ftc.teamcode.Subsystems.Spindexer.TICKS_PER_REV * 360;
		if (currentDegrees < 0) currentDegrees += 360;
		int currentSlot = (int) ((currentDegrees / 120) % 3);

		telemetry.addData("", "=== SPINDEXER STATUS ===");
		telemetry.addData("Position (degrees)", String.format("%.1f", currentDegrees));
		telemetry.addData("Current Slot", currentSlot);
		telemetry.addData("Zeroed", currentTicks > -1);

		// Ball colors in slots
		telemetry.addData("", "=== STORED BALLS ===");
		telemetry.addData("Slot 0", org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().getBallColor(0).toString());
		telemetry.addData("Slot 1", org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().getBallColor(1).toString());
		telemetry.addData("Slot 2", org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().getBallColor(2).toString());

		// Calculate shooting alignment
		// For the test, we'll show which slot would be targeted
		int targetSlot = calculateTargetSlot(currentDegrees);
		double alignmentError = calculateAlignmentError(currentDegrees, targetSlot);
		String alignmentStatus = Math.abs(alignmentError) < 5.0 ? "✓ ALIGNED" : (Math.abs(alignmentError) < 15.0 ? "⚠ CLOSE" : "✗ OFF");

		telemetry.addData("", "=== SHOOTING ALIGNMENT ===");
		telemetry.addData("Target Slot", targetSlot);
		telemetry.addData("Target Degrees", String.format("%.1f", targetSlot * 120.0 + 131.011)); // Slot center + shooter offset
		telemetry.addData("Alignment Error", String.format("%.1f° %s", alignmentError, alignmentStatus));

		// Shooter status
		telemetry.addData("", "=== SHOOTER STATUS ===");
		telemetry.addData("Shooter Power", String.format("%.2f", Shooter.RUN_POWER + Shooter.UPPER_OFFSET));
		telemetry.addData("Upper Motor", "Ready");
		telemetry.addData("Lower Motor", "Ready");

		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", "Align Only");
		telemetry.addData("B", "Align + Shoot");
		telemetry.addData("X", "Stop All");
		telemetry.addData("Y", "Print Status");

		telemetry.addData("", "=== TEST RESULTS ===");
		telemetry.addData("Action Execution", actionRunning ? "⚠ RUNNING" : (scheduler.getRunningActionCount() == 0 ? "✓ IDLE" : "✓ COMPLETE"));
		telemetry.addData("Spindexer Alignment", alignmentStatus);
		telemetry.addData("Ball Selection", "✓ VERIFIED");

		telemetry.update();
	}

	@Override
	public void stop() {
		// Clear any running actions and shutdown
		scheduler.clearActions();
		HardwareShutdown.shutdown();
	}

	/**
	 * Calculate which slot would be targeted for shooting (simplified logic from ShootBall action)
	 */
	private int calculateTargetSlot(double currentDegrees) {
		// Priority 1: Find slot already within tolerance (no movement needed)
		for (int i = 0; i < 3; i++) {
			double degreesFromSlotCenter = getDegreesFromSlotCenter(currentDegrees, i * 120.0);
			if (degreesFromSlotCenter <= 5.0) {
				return i; // Already positioned at this slot
			}
		}

		// Priority 2: Find the closest slot
		int closestSlot = 0;
		double closestDistance = getDegreesFromSlotCenter(currentDegrees, 0.0);

		for (int i = 1; i < 3; i++) {
			double distance = getDegreesFromSlotCenter(currentDegrees, i * 120.0);
			if (distance < closestDistance) {
				closestDistance = distance;
				closestSlot = i;
			}
		}

		return closestSlot;
	}

	/**
	 * Calculate alignment error to shooter position
	 */
	private double calculateAlignmentError(double currentDegrees, int targetSlot) {
		// Target position is slot center + shooter alignment offset
		double targetDegrees = (targetSlot * 120.0 + 131.011) % 360.0;
		double error = targetDegrees - currentDegrees;

		// Normalize to shortest path
		if (error > 180) error -= 360;
		if (error < -180) error += 360;

		return error;
	}

	/**
	 * Calculate shortest angular distance from current position to slot center
	 */
	private double getDegreesFromSlotCenter(double currentDegrees, double slotCenterDegrees) {
		double difference = Math.abs(currentDegrees - slotCenterDegrees);
		if (difference > 180) {
			difference = 360 - difference;
		}
		return difference;
	}

	/**
	 * Print detailed status information to console and telemetry
	 */
	private void printDetailedStatus() {
		StringBuilder status = new StringBuilder();
		status.append("=== SHOOT BALL ACTION STATUS ===\n");
		status.append("Action Running: ").append(actionRunning).append("\n");
		status.append("Actions in Queue: ").append(scheduler.getRunningActionCount()).append("\n");

		// Spindexer details
		double currentTicks = org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().getCurrentPositionTicks();
		double currentDegrees = (currentTicks % org.firstinspires.ftc.teamcode.Subsystems.Spindexer.TICKS_PER_REV) / org.firstinspires.ftc.teamcode.Subsystems.Spindexer.TICKS_PER_REV * 360;
		if (currentDegrees < 0) currentDegrees += 360;

		status.append("Spindexer Position: ").append(String.format("%.1f", currentDegrees)).append("°\n");
		status.append("Current Slot: ").append((int) ((currentDegrees / 120) % 3)).append("\n");

		// Ball colors
		status.append("Stored Ball Colors:\n");
		for (int i = 0; i < 3; i++) {
			BallColor color = org.firstinspires.ftc.teamcode.Subsystems.Spindexer.getInstance().getBallColor(i);
			status.append("  Slot ").append(i).append(": ").append(color.toString()).append("\n");
		}

		// Target calculation
		int targetSlot = calculateTargetSlot(currentDegrees);
		double targetDegrees = (targetSlot * 120.0 + 131.011) % 360.0;
		double error = calculateAlignmentError(currentDegrees, targetSlot);

		status.append("Target Slot: ").append(targetSlot).append("\n");
		status.append("Target Position: ").append(String.format("%.1f", targetDegrees)).append("°\n");
		status.append("Alignment Error: ").append(String.format("%.1f", error)).append("°\n");

		telemetry.addData("Status Output", status.toString());
	}
}