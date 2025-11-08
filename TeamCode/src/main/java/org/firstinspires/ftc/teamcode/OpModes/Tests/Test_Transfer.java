package org.firstinspires.ftc.teamcode.OpModes.Tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Subsystems.Transfer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareInitializer;
import org.firstinspires.ftc.teamcode.LifecycleManagementUtilities.HardwareShutdown;

/**
 * Unit Test OpMode for the Transfer Subsystem
 *
 * Purpose: Tests both transfer belt servos and intake door servos independently
 *
 * Controls - Transfer Servos:
 * - A button: Transfer Forward (move balls from intake to storage)
 * - B button: Transfer Backward (reverse transfer motion)
 * - X button: Stop Transfer servos
 *
 * Controls - Intake Door Servos:
 * - Y button: Intake Door Forward (open door for ball entry)
 * - Right Bumper: Intake Door Backward (close door)
 * - Left Bumper: Stop Intake Door servos
 *
 * DPAD Controls:
 * - DPAD UP: Increase all servo power (add 0.1)
 * - DPAD DOWN: Decrease all servo power (subtract 0.1)
 *
 * Expected Behavior:
 * - Transfer Forward: Smooth belt motion moving balls forward
 * - Transfer Backward: Reverse belt motion (use for jam clearing)
 * - Intake Door Forward: Door opens smoothly and fully
 * - Intake Door Backward: Door closes smoothly and fully
 * - Stop commands should halt all motion immediately
 *
 * Notes:
 * - Verify servo directions are correct (forward vs backward)
 * - Check for smooth operation without grinding or stalling
 * - Test synchronization of left and right servos
 */
@TeleOp(name = "Test_Transfer", group = "Unit Tests")
public class Test_Transfer extends LinearOpMode {

    private Transfer transfer;
    private String transferState = "STOPPED";
    private String doorState = "STOPPED";

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize hardware
        HardwareInitializer.initialize(hardwareMap);
        transfer = Transfer.getInstance();

        telemetry.addData("Status", "Initialized - Waiting for START");
        telemetry.update();

        waitForStart();

        telemetry.addData("Status", "Running");
        telemetry.addData("Transfer Controls", "A=FWD, B=BACK, X=STOP");
        telemetry.addData("Door Controls", "Y=FWD, RB=BACK, LB=STOP");
        telemetry.addData("Power Tune", "DPAD UP/DOWN");
        telemetry.update();

        while (opModeIsActive()) {
            // === TRANSFER SERVO CONTROLS ===

            // A button - Transfer Forward
            if (gamepad1.a) {
                transfer.transferForward().run(null);
                transferState = "FORWARD";
            }

            // B button - Transfer Backward
            if (gamepad1.b) {
                transfer.transferBackward().run(null);
                transferState = "BACKWARD";
            }

            // X button - Transfer Stop
            if (gamepad1.x) {
                transfer.transferStop().run(null);
                transferState = "STOPPED";
            }

            // === INTAKE DOOR SERVO CONTROLS ===

            // Y button - Intake Door Forward (open)
            if (gamepad1.y) {
                transfer.intakeDoorForward().run(null);
                doorState = "FORWARD (OPEN)";
            }

            // Right Bumper - Intake Door Backward (close)
            if (gamepad1.right_bumper) {
                transfer.intakeDoorBackward().run(null);
                doorState = "BACKWARD (CLOSE)";
            }

            // Left Bumper - Intake Door Stop
            if (gamepad1.left_bumper) {
                transfer.intakeDoorStop().run(null);
                doorState = "STOPPED";
            }

            // === POWER ADJUSTMENTS ===

            // DPAD UP - Increase all servo powers
            if (gamepad1.dpad_up) {
                Transfer.FORWARD_POWER = Math.min(Transfer.FORWARD_POWER + 0.1, 1.0);
                Transfer.BACKWARD_POWER = Math.max(Transfer.BACKWARD_POWER - 0.1, -1.0);
                telemetry.addData("Power increased to", Transfer.FORWARD_POWER);
                Thread.sleep(200); // Debounce
            }

            // DPAD DOWN - Decrease all servo powers
            if (gamepad1.dpad_down) {
                Transfer.FORWARD_POWER = Math.max(Transfer.FORWARD_POWER - 0.1, 0.0);
                Transfer.BACKWARD_POWER = Math.min(Transfer.BACKWARD_POWER + 0.1, 0.0);
                telemetry.addData("Power decreased to", Transfer.FORWARD_POWER);
                Thread.sleep(200); // Debounce
            }

            // === DISPLAY TELEMETRY ===
            telemetry.addData("", "--- TRANSFER BELT ---");
            telemetry.addData("Transfer State", transferState);
            telemetry.addData("Transfer Forward Power", Transfer.FORWARD_POWER);
            telemetry.addData("Transfer Backward Power", Transfer.BACKWARD_POWER);

            telemetry.addData("", "--- INTAKE DOOR ---");
            telemetry.addData("Door State", doorState);
            telemetry.addData("Door Forward Power", Transfer.FORWARD_POWER);
            telemetry.addData("Door Backward Power", Transfer.BACKWARD_POWER);

            telemetry.addData("", "--- STOP POWER ---");
            telemetry.addData("Stop Power", Transfer.STOP_POWER);

            telemetry.update();
        }

        // Shutdown
        HardwareShutdown.shutdown();
    }
}
