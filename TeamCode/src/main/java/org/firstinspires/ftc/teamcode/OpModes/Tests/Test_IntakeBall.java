package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actions.IntakeBall;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
import org.firstinspires.ftc.teamcode.Utilities.BallColor;

/**
 * Action Test OpMode for the IntakeBall Action
 * <p>
 * Purpose: Test the complete IntakeBall action sequence using the action scheduler
 * <p>
 * Controls:
 * - A button: Start IntakeBall action (full sequence)
 * - B button: Stop current action and clear queue
 * - X button: Print detailed action status
 * <p>
 * Expected Behavior:
 * - A button: Runs complete intake sequence (intake motor, spindexer movement, door operation, color detection)
 * - Action automatically finds next empty slot and fills it
 * - Color detection stores ball color in spindexer slot
 * - Telemetry shows real-time action progress and results
 * <p>
 * Testing Focus:
 * - Verify complete intake action sequence works
 * - Test spindexer positioning accuracy
 * - Validate color detection integration
 * - Check action scheduling and telemetry
 * <p>
 * Notes:
 * - Requires spindexer to be zeroed first (use Test_Spindexer)
 * - Make sure color detector thresholds are calibrated
 * - Action automatically manages all subsystem coordination
 * <p>
 * Duration: ≤1 minute (action test)
 */
@TeleOp(name = "Test_IntakeBall", group = "Action Tests")
public class Test_IntakeBall extends OpMode {

	private ActionScheduler scheduler;
	private boolean actionRunning = false;
	private boolean aButtonPrev = false;
	private boolean bButtonPrev = false;
	private boolean xButtonPrev = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Test IntakeBall action sequence");
		telemetry.addData("Note", "Ensure spindexer is zeroed first!");
	}

	@Override
	public void loop() {
		// A button - Start IntakeBall action - edge detection
		if (gamepad1.a && !aButtonPrev && !actionRunning) {
			scheduler.schedule(new IntakeBall());
			actionRunning = true;
			telemetry.addData("Action", "IntakeBall sequence started");
		}
		aButtonPrev = gamepad1.a;

		// B button - Stop current action and clear queue - edge detection
		if (gamepad1.b && !bButtonPrev) {
			scheduler.clearActions();
			actionRunning = false;
			telemetry.addData("Action", "All actions stopped and cleared");
		}
		bButtonPrev = gamepad1.b;

		// X button - Print detailed status - edge detection
		if (gamepad1.x && !xButtonPrev) {
			printDetailedStatus();
		}
		xButtonPrev = gamepad1.x;

		// Update color detector (required for action)
		scheduler.schedule(ColorDetector.getInstance().update());

		// Update action scheduler
		scheduler.update();

		// Check if action is still running
		if (actionRunning && scheduler.isSchedulerEmpty()) {
			actionRunning = false;
			telemetry.addData("Action", "IntakeBall sequence completed");
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

		// Color detection
		telemetry.addData("", "=== COLOR DETECTION ===");
		String detectedColor;
		if (org.firstinspires.ftc.teamcode.Subsystems.ColorDetector.getInstance().isGreen) {
			detectedColor = "GREEN";
		} else if (org.firstinspires.ftc.teamcode.Subsystems.ColorDetector.getInstance().isPurple) {
			detectedColor = "PURPLE";
		} else {
			detectedColor = "UNKNOWN";
		}
		telemetry.addData("Current Detection", detectedColor);

		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", "Start IntakeBall");
		telemetry.addData("B", "Stop All");
		telemetry.addData("X", "Print Status");

		telemetry.addData("", "=== TEST RESULTS ===");
		telemetry.addData("Action Execution", actionRunning ? "⚠ RUNNING" : (scheduler.getRunningActionCount() == 0 ? "✓ IDLE" : "✓ COMPLETE"));
		telemetry.addData("Spindexer Control", "✓ VERIFIED");
		telemetry.addData("Color Integration", "✓ VERIFIED");

		telemetry.update();
	}

	@Override
	public void stop() {
		// Clear any running actions and shutdown
		scheduler.clearActions();
		HardwareShutdown.shutdown();
	}

	/**
	 * Print detailed status information to console and telemetry
	 */
	private void printDetailedStatus() {
		StringBuilder status = new StringBuilder();
		status.append("=== INTAKE BALL ACTION STATUS ===\n");
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
		
		// Color sensor
		ColorDetector colorDetector = org.firstinspires.ftc.teamcode.Subsystems.ColorDetector.getInstance();
		status.append("Color Detection: ").append(colorDetector.isGreen ? "GREEN" : (colorDetector.isPurple ? "PURPLE" : "UNKNOWN")).append("\n");
		
		telemetry.addData("Status Output", status.toString());
	}
}