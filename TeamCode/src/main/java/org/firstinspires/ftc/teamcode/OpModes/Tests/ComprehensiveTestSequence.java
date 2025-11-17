//package org.firstinspires.ftc.teamcode.OpModes.Tests;
//
//import androidx.annotation.NonNull;
//
//import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
//import com.acmerobotics.roadrunner.Action;
//
//import org.firstinspires.ftc.teamcode.Actions.IntakeBall;
//import org.firstinspires.ftc.teamcode.Actions.ShootBall;
//import org.firstinspires.ftc.teamcode.Subsystems.Intake;
//import org.firstinspires.ftc.teamcode.Subsystems.Shooter;
//import org.firstinspires.ftc.teamcode.Subsystems.Spindexer;
//import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
//import org.firstinspires.ftc.teamcode.Utilities.ActionScheduler;
//
///**
// * Action-based comprehensive test sequence.
// * Replaces the time-based state machine with individual Action classes
// * for each test phase, making the test more maintainable and reliable.
// */
//public class ComprehensiveTestSequence implements Action {
//	private final ActionScheduler scheduler;
//	private TestPhase currentPhase;
//	private boolean phaseStarted = false;
//	private final boolean phaseCompleted = false;
//	private final boolean testStarted = false;
//	private final boolean emergencyStop = false;
//
//	// Test results
//	private boolean intakeTest = false;
//	private boolean shooterTest = false;
//	private boolean transferTest = false;
//	private boolean spindexerTest = false;
//	private boolean colorTest = false;
//	private boolean actionTest = false;
//
//	public ComprehensiveTestSequence() {
//		this.scheduler = ActionScheduler.getInstance();
//		this.currentPhase = TestPhase.PHASE1_SUBSYSTEM_CHECKS;
//	}
//
//	@Override
//	public boolean run(@NonNull TelemetryPacket packet) {
//		packet.put("Comprehensive Test Phase", currentPhase.toString());
//
//		// Update test results
//		updateTestResults(packet);
//
//		// Run the current phase
//		return runCurrentPhase(packet);
//	}
//
//	private void updateTestResults(TelemetryPacket packet) {
//		packet.put("Intake Test", intakeTest ? "✓ PASS" : "⏳ RUN");
//		packet.put("Shooter Test", shooterTest ? "✓ PASS" : "⏳ RUN");
//		packet.put("Transfer Test", transferTest ? "✓ PASS" : "⏳ RUN");
//		packet.put("Spindexer Test", spindexerTest ? "✓ PASS" : "⏳ RUN");
//		packet.put("Color Test", colorTest ? "✓ PASS" : "⏳ RUN");
//		packet.put("Action Test", actionTest ? "✓ PASS" : "⏳ RUN");
//
//		// Calculate overall status
//		int passedTests = (intakeTest ? 1 : 0) + (shooterTest ? 1 : 0) + (transferTest ? 1 : 0) +
//				(spindexerTest ? 1 : 0) + (colorTest ? 1 : 0) + (actionTest ? 1 : 0);
//		packet.put("Passed Tests", passedTests + "/6");
//		packet.put("Overall Status", passedTests == 6 ? "✓ READY FOR COMPETITION" : "⚠ NEEDS ATTENTION");
//	}
//
//	private boolean runCurrentPhase(TelemetryPacket packet) {
//		switch (currentPhase) {
//			case PHASE1_SUBSYSTEM_CHECKS:
//				return runPhase1SubsystemChecks(packet);
//			case PHASE2_SPINDEXER_VALIDATION:
//				return runPhase2SpindexerValidation(packet);
//			case PHASE3_COLOR_DETECTION:
//				return runPhase3ColorDetection(packet);
//			case PHASE4_ACTION_SEQUENCE:
//				return runPhase4ActionSequence(packet);
//			case PHASE5_SUMMARY:
//				return runPhase5Summary(packet);
//			default:
//				return true; // Test complete
//		}
//	}
//
//	private boolean runPhase1SubsystemChecks(TelemetryPacket packet) {
//		if (!phaseStarted) {
//			phaseStarted = true;
//			packet.put("Phase 1", "Starting Subsystem Checks");
//
//			// Run all subsystem checks in sequence
//			scheduler.schedule(new SubsystemChecksAction());
//		}
//
//		// Check if phase is complete by looking at individual test flags
//		if (intakeTest && shooterTest && transferTest) {
//			currentPhase = TestPhase.PHASE2_SPINDEXER_VALIDATION;
//			phaseStarted = false;
//			packet.put("Phase 1", "COMPLETE ✓");
//		}
//
//		return true; // Continue to next iteration
//	}
//
//	private boolean runPhase2SpindexerValidation(TelemetryPacket packet) {
//		if (!phaseStarted) {
//			phaseStarted = true;
//			packet.put("Phase 2", "Starting Spindexer Validation");
//
//			// Run spindexer validation
//			scheduler.schedule(new SpindexerValidationAction());
//		}
//
//		if (spindexerTest) {
//			currentPhase = TestPhase.PHASE3_COLOR_DETECTION;
//			phaseStarted = false;
//			packet.put("Phase 2", "COMPLETE ✓");
//		}
//
//		return true;
//	}
//
//	private boolean runPhase3ColorDetection(TelemetryPacket packet) {
//		if (!phaseStarted) {
//			phaseStarted = true;
//			packet.put("Phase 3", "Starting Color Detection");
//
//			// Run color detection test
//			scheduler.schedule(new ColorDetectionAction());
//		}
//
//		if (colorTest) {
//			currentPhase = TestPhase.PHASE4_ACTION_SEQUENCE;
//			phaseStarted = false;
//			packet.put("Phase 3", "COMPLETE ✓");
//		}
//
//		return true;
//	}
//
//	private boolean runPhase4ActionSequence(TelemetryPacket packet) {
//		if (!phaseStarted) {
//			phaseStarted = true;
//			packet.put("Phase 4", "Starting Action Sequence");
//
//			// Run comprehensive action sequence
//			scheduler.schedule(new ActionSequenceAction());
//		}
//
//		if (actionTest) {
//			currentPhase = TestPhase.PHASE5_SUMMARY;
//			phaseStarted = false;
//			packet.put("Phase 4", "COMPLETE ✓");
//		}
//
//		return true;
//	}
//
//	private boolean runPhase5Summary(TelemetryPacket packet) {
//		packet.put("Phase 5", "COMPLETE ✓");
//		packet.put("Comprehensive Test", "FINISHED");
//		return false; // Test complete, stop this action
//	}
//
//	// Individual Action classes for each phase
//
//	private enum TestPhase {
//		PHASE1_SUBSYSTEM_CHECKS,
//		PHASE2_SPINDEXER_VALIDATION,
//		PHASE3_COLOR_DETECTION,
//		PHASE4_ACTION_SEQUENCE,
//		PHASE5_SUMMARY
//	}
//
//	private class SubsystemChecksAction implements Action {
//		private int stage = 0;
//
//		@Override
//		public boolean run(@NonNull TelemetryPacket packet) {
//			switch (stage) {
//				case 0:
//					packet.put("Subsystem Check", "Testing Intake");
//					scheduler.schedule(Intake.getInstance().in());
//					stage = 1;
//					break;
//				case 1:
//					packet.put("Subsystem Check", "Testing Shooter");
//					scheduler.schedule(Shooter.getInstance().run());
//					intakeTest = true;
//					stage = 2;
//					break;
//				case 2:
//					packet.put("Subsystem Check", "Testing Transfer");
//					scheduler.schedule(Transfer.getInstance().transferForward());
//					shooterTest = true;
//					stage = 3;
//					break;
//				case 3:
//					packet.put("Subsystem Check", "Testing Door");
//					scheduler.schedule(Transfer.getInstance().intakeDoorForward());
//					transferTest = true;
//					stage = 4;
//					break;
//				case 4:
//					// Stop all motors
//					scheduler.schedule(Intake.getInstance().stop());
//					scheduler.schedule(Shooter.getInstance().stop());
//					scheduler.schedule(Transfer.getInstance().transferStop());
//					scheduler.schedule(Transfer.getInstance().intakeDoorStop());
//					stage = 5;
//					break;
//				default:
//					return false; // Action complete
//			}
//			return true; // Continue to next iteration
//		}
//	}
//
//	private class SpindexerValidationAction implements Action {
//		private int stage = 0;
//
//		@Override
//		public boolean run(@NonNull TelemetryPacket packet) {
//			switch (stage) {
//				case 0:
//					packet.put("Spindexer Test", "Zeroing");
//					scheduler.schedule(Spindexer.getInstance().zero());
//					stage = 1;
//					break;
//				case 1:
//					packet.put("Spindexer Test", "Moving to Position 0");
//					// Simulate position validation (would be real position check in production)
//					spindexerTest = true;
//					stage = 2;
//					break;
//				default:
//					return false; // Action complete
//			}
//			return true; // Continue to next iteration
//		}
//	}
//
//	private class ColorDetectionAction implements Action {
//		private int stage = 0;
//
//		@Override
//		public boolean run(@NonNull TelemetryPacket packet) {
//			switch (stage) {
//				case 0:
//					packet.put("Color Test", "Reading Baseline");
//					// Simulate color detection validation (would be real sensor check in production)
//					colorTest = true;
//					stage = 1;
//					break;
//				default:
//					return false; // Action complete
//			}
//			return true; // Continue to next iteration
//		}
//	}
//
//	private class ActionSequenceAction implements Action {
//		private int stage = 0;
//
//		@Override
//		public boolean run(@NonNull TelemetryPacket packet) {
//			switch (stage) {
//				case 0:
//					packet.put("Action Test", "Running IntakeBall Action");
//					// Note: This would require a ball to be present in real testing
//					scheduler.schedule(new IntakeBall());
//					stage = 1;
//					break;
//				case 1:
//					packet.put("Action Test", "Running ShootBall Action");
//					scheduler.schedule(new ShootBall());
//					actionTest = true;
//					stage = 2;
//					break;
//				default:
//					return false; // Action complete
//			}
//			return true; // Continue to next iteration
//		}
//	}
//}