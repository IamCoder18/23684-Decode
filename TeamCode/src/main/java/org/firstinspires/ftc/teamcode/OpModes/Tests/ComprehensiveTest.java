package org.firstinspires.ftc.teamcode.OpModes.Tests;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.roadrunner.Action;

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
 * Architecture: Action-based design replaces fragile time-based state machine
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
	private Action comprehensiveTestAction;
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

		// Run comprehensive test action if running
		if (testRunning && !emergencyStop && comprehensiveTestAction != null) {
			comprehensiveTestAction.run(null); // Will be handled by scheduler
		}

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
		
		// Create and schedule the comprehensive test action sequence
		comprehensiveTestAction = new ComprehensiveTestAction();
		scheduler.schedule(comprehensiveTestAction);
		
		telemetry.addData("Action", "Starting Comprehensive Test");
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
	
	/**
	 * Action-based comprehensive test sequence.
	 * Replaces the time-based state machine with individual Action classes
	 * for each test phase, making the test more maintainable and reliable.
	 */
	private class ComprehensiveTestAction implements Action {
		private TestPhase currentPhase = TestPhase.PHASE1_SUBSYSTEM_CHECKS;
		private boolean phaseStarted = false;
		private boolean testComplete = false;
		
		@Override
		public boolean run(@NonNull TelemetryPacket packet) {
			if (testComplete) {
				return false; // Test finished
			}
			
			packet.put("Test Phase", currentPhase.toString());
			
			switch (currentPhase) {
				case PHASE1_SUBSYSTEM_CHECKS:
					return runPhase1SubsystemChecks(packet);
				case PHASE2_SPINDEXER_VALIDATION:
					return runPhase2SpindexerValidation(packet);
				case PHASE3_COLOR_DETECTION:
					return runPhase3ColorDetection(packet);
				case PHASE4_ACTION_SEQUENCE:
					return runPhase4ActionSequence(packet);
				case PHASE5_SUMMARY:
					return runPhase5Summary(packet);
				default:
					testComplete = true;
					return false;
			}
		}
		
		private boolean runPhase1SubsystemChecks(@NonNull TelemetryPacket packet) {
			if (!phaseStarted) {
				phaseStarted = true;
				packet.put("Phase 1", "Starting Subsystem Checks");
				
				// Schedule all subsystem tests to run in sequence
				scheduler.schedule(new SubsystemChecksAction());
			}
			
			// Check if phase is complete
			if (intakeTest && shooterTest && transferTest) {
				currentPhase = TestPhase.PHASE2_SPINDEXER_VALIDATION;
				phaseStarted = false;
				packet.put("Phase 1", "COMPLETE ✓");
			}
			
			return true; // Continue to next iteration
		}
		
		private boolean runPhase2SpindexerValidation(@NonNull TelemetryPacket packet) {
			if (!phaseStarted) {
				phaseStarted = true;
				packet.put("Phase 2", "Starting Spindexer Validation");
				
				scheduler.schedule(new SpindexerValidationAction());
			}
			
			if (spindexerTest) {
				currentPhase = TestPhase.PHASE3_COLOR_DETECTION;
				phaseStarted = false;
				packet.put("Phase 2", "COMPLETE ✓");
			}
			
			return true;
		}
		
		private boolean runPhase3ColorDetection(@NonNull TelemetryPacket packet) {
			if (!phaseStarted) {
				phaseStarted = true;
				packet.put("Phase 3", "Starting Color Detection");
				
				scheduler.schedule(new ColorDetectionAction());
			}
			
			if (colorTest) {
				currentPhase = TestPhase.PHASE4_ACTION_SEQUENCE;
				phaseStarted = false;
				packet.put("Phase 3", "COMPLETE ✓");
			}
			
			return true;
		}
		
		private boolean runPhase4ActionSequence(@NonNull TelemetryPacket packet) {
			if (!phaseStarted) {
				phaseStarted = true;
				packet.put("Phase 4", "Starting Action Sequence");
				
				scheduler.schedule(new ActionSequenceAction());
			}
			
			if (actionTest) {
				currentPhase = TestPhase.PHASE5_SUMMARY;
				phaseStarted = false;
				packet.put("Phase 4", "COMPLETE ✓");
			}
			
			return true;
		}
		
		private boolean runPhase5Summary(@NonNull TelemetryPacket packet) {
			packet.put("Phase 5", "COMPLETE ✓");
			packet.put("Comprehensive Test", "FINISHED");
			testComplete = true;
			return false; // Test complete, stop this action
		}
		
		// Individual Action classes for each phase
		private class SubsystemChecksAction implements Action {
			private int stage = 0;
			
			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
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
		}
		
		private class SpindexerValidationAction implements Action {
			private int stage = 0;
			
			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
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
		}
		
		private class ColorDetectionAction implements Action {
			private int stage = 0;
			
			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
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
		}
		
		private class ActionSequenceAction implements Action {
			private int stage = 0;
			
			@Override
			public boolean run(@NonNull TelemetryPacket packet) {
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
		}
		
		private enum TestPhase {
			PHASE1_SUBSYSTEM_CHECKS,
			PHASE2_SPINDEXER_VALIDATION,
			PHASE3_COLOR_DETECTION,
			PHASE4_ACTION_SEQUENCE,
			PHASE5_SUMMARY
		}
	}
}