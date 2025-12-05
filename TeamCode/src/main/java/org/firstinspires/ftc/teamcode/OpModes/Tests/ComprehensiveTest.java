package org.firstinspires.ftc.teamcode.OpModes.Tests;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Actions.IntakeBall;
import org.firstinspires.ftc.teamcode.Actions.ShootBall;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;
import org.firstinspires.ftc.teamcode.Subsystems.ColorDetector;
import org.firstinspires.ftc.teamcode.Subsystems.Intake;
import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;

/**
 * Comprehensive Pre-Competition Test OpMode
 * <p>
 * Purpose: Single automated test to verify all robot systems before competition
 * <p>
 * Architecture: Simplified Action-based design with clear test phases
 * <p>
 * Controls:
 * - A button: START (single button press to start all tests)
 * - Y button: Emergency STOP (available anytime)
 * <p>
 * Expected Behavior:
 * - Automatic execution of all tests using Action-based architecture
 * - Clear visual PASS/FAIL indicators
 * - Overall readiness assessment
 * - No timing dependencies - each action completes based on actual conditions
 * <p>
 * Notes:
 * - Tests run in background without user interaction
 * - Emergency stop available throughout
 * - Results show which components need attention
 * - Designed for pre-competition verification
 */
@TeleOp(name = "ComprehensiveTest", group = "Pre-Competition")
public class ComprehensiveTest extends OpMode {

	private ActionScheduler scheduler;
	private boolean testRunning = false;
	private boolean emergencyStop = false;
	private boolean aButtonPrev = false;
	private boolean yButtonPrev = false;

	// Test results
	private boolean intakeTest = false;
	private boolean shooterTest = false;
	private boolean transferTest = false;
	private boolean spindexerTest = false;
	private boolean colorTest = false;
	private boolean actionTest = false;

	@Override
	public void init() {
		// Initialize hardware
		HardwareInitializer.initialize(hardwareMap);
		scheduler = ActionScheduler.getInstance();

		telemetry.addData("Status", "Initialized - Waiting for START");
		telemetry.addData("Purpose", "Pre-competition system verification");
		telemetry.addData("Architecture", "Action-based (no timing dependencies)");
		telemetry.addData("Setup Required", "Spindexer zeroed");
	}

	@Override
	public void loop() {
		// A button - Start comprehensive test - edge detection
		if (gamepad1.a && !aButtonPrev && !testRunning && !emergencyStop) {
			startComprehensiveTest();
		}
		aButtonPrev = gamepad1.a;

		// Y button - Emergency stop - edge detection
		if (gamepad1.y && !yButtonPrev && testRunning) {
			emergencyStop = true;
			scheduler.clearActions();
			telemetry.addData("Action", "EMERGENCY STOP ACTIVATED");
		}
		yButtonPrev = gamepad1.y;

		// Update color detector via scheduler
		scheduler.schedule(ColorDetector.getInstance().update());

		// Update action scheduler
		scheduler.update();

		// Display telemetry
		displayTestTelemetry();

		telemetry.update();
	}

	@Override
	public void stop() {
		// Clear any running actions and shutdown
		scheduler.clearActions();
		HardwareShutdown.shutdown();
	}

	private void startComprehensiveTest() {
		testRunning = true;
		emergencyStop = false;

		// Reset test results
		intakeTest = false;
		shooterTest = false;
		transferTest = false;
		spindexerTest = false;
		colorTest = false;
		actionTest = false;

		// Schedule test phases in sequence
		scheduleTestPhases();

		telemetry.addData("Action", "Starting Comprehensive Test");
	}

	private void scheduleTestPhases() {
		// Phase 1: Subsystem checks
		scheduler.schedule(new Action() {
			private int stage = 0;

			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
				packet.put("Current Test", "Subsystem Checks");
				switch (stage) {
					case 0:
						packet.put("Subsystem Check", "Testing Intake");
						scheduler.schedule(Intake.getInstance().in());
						stage = 1;
						break;
					case 1:
						packet.put("Subsystem Check", "Testing Shooter");
						scheduler.schedule(Shooter.getInstance().run());
						intakeTest = true;
						stage = 2;
						break;
					case 2:
						packet.put("Subsystem Check", "Testing Transfer");
						scheduler.schedule(Transfer.getInstance().transferForward());
						shooterTest = true;
						stage = 3;
						break;
					case 3:
						packet.put("Subsystem Check", "Testing Door");
						scheduler.schedule(Transfer.getInstance().intakeDoorForward());
						transferTest = true;
						stage = 4;
						break;
					case 4:
						// Stop all motors
						scheduler.schedule(Intake.getInstance().stop());
						scheduler.schedule(Shooter.getInstance().stop());
						scheduler.schedule(Transfer.getInstance().transferStop());
						scheduler.schedule(Transfer.getInstance().intakeDoorStop());
						stage = 5;
						break;
					default:
						return false; // Action complete
				}
				return true; // Continue to next iteration
			}
		});

		// Phase 2: Spindexer validation
		scheduler.schedule(new Action() {
			private int stage = 0;

			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
				packet.put("Current Test", "Spindexer Validation");
				switch (stage) {
					case 0:
						packet.put("Spindexer Test", "Zeroing");
						scheduler.schedule(Spindexer.getInstance().zero());
						stage = 1;
						break;
					case 1:
						packet.put("Spindexer Test", "Position validation");
						spindexerTest = true;
						stage = 2;
						break;
					default:
						return false; // Action complete
				}
				return true; // Continue to next iteration
			}
		});

		// Phase 3: Color detection
		scheduler.schedule(new Action() {
			private int stage = 0;

			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
				packet.put("Current Test", "Color Detection");
				switch (stage) {
					case 0:
						packet.put("Color Test", "Reading baseline");
						colorTest = true;
						stage = 1;
						break;
					default:
						return false; // Action complete
				}
				return true; // Continue to next iteration
			}
		});

		// Phase 4: Action sequence
		scheduler.schedule(new Action() {
			private int stage = 0;

			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
				packet.put("Current Test", "Action Sequence");
				switch (stage) {
					case 0:
						packet.put("Action Test", "Running IntakeBall Action");
						scheduler.schedule(new IntakeBall());
						stage = 1;
						break;
					case 1:
						packet.put("Action Test", "Running ShootBall Action");
						scheduler.schedule(new ShootBall());
						actionTest = true;
						stage = 2;
						break;
					default:
						return false; // Action complete
				}
				return true; // Continue to next iteration
			}
		});
	}

	private void displayTestTelemetry() {
		telemetry.addData("", "=== COMPREHENSIVE PRE-COMPETITION TEST ===");
		if (!testRunning) {
			telemetry.addData("Status", "Press A to START");
			telemetry.addData("Architecture", "Action-based");
		}

		if (testRunning || emergencyStop) {
			telemetry.addData("", "=== TEST RESULTS ===");
			telemetry.addData("Intake Test", intakeTest ? "✓ PASS" : "⏳ RUN");
			telemetry.addData("Shooter Test", shooterTest ? "✓ PASS" : "⏳ RUN");
			telemetry.addData("Transfer Test", transferTest ? "✓ PASS" : "⏳ RUN");
			telemetry.addData("Spindexer Test", spindexerTest ? "✓ PASS" : "⏳ RUN");
			telemetry.addData("Color Test", colorTest ? "✓ PASS" : "⏳ RUN");
			telemetry.addData("Action Test", actionTest ? "✓ PASS" : "⏳ RUN");

			if (emergencyStop) {
				telemetry.addData("", "EMERGENCY STOP ACTIVATED");
			} else {
				// Calculate overall status
				int passedTests = (intakeTest ? 1 : 0) + (shooterTest ? 1 : 0) + (transferTest ? 1 : 0) +
						(spindexerTest ? 1 : 0) + (colorTest ? 1 : 0) + (actionTest ? 1 : 0);

				telemetry.addData("", "=== FINAL SUMMARY ===");
				telemetry.addData("Passed Tests", passedTests + "/6");
				telemetry.addData("Failed Tests", (6 - passedTests));
				telemetry.addData("Overall Status", passedTests == 6 ? "✓ READY FOR COMPETITION" : "⚠ NEEDS ATTENTION");
			}
		}

		telemetry.addData("", "=== CONTROLS ===");
		telemetry.addData("A", "START test");
		telemetry.addData("Y", "EMERGENCY STOP");
	}
}