package org.firstinspires.ftc.teamcode.OpModes;

import android.os.Environment;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.io.File;

@TeleOp(name = "Reset Obelisk Log", group = "Utility")
public class ResetLogTeleOp extends LinearOpMode {

    private static final String LOG_FILE_NAME = "ObeliskDetectionLog.csv";
    private static final String BASE_FOLDER_NAME = "FIRST";

    @Override
    public void runOpMode() {
        telemetry.addLine("This OpMode will reset the Obelisk Detection Log.");
        telemetry.addLine("Press START to delete the log file.");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + BASE_FOLDER_NAME + "/" + LOG_FILE_NAME);

            if (logFile.exists()) {
                if (logFile.delete()) {
                    telemetry.addLine("Log file '" + LOG_FILE_NAME + "' has been deleted.");
                    telemetry.addLine("A new log file will be created by LeaveTest.");
                } else {
                    telemetry.addLine("Error: Could not delete log file.");
                }
            } else {
                telemetry.addLine("Log file does not exist. Nothing to reset.");
            }
            telemetry.update();

            // Keep showing the status until stop is pressed
            while (opModeIsActive() && !isStopRequested()) {
                idle();
            }
        }
    }
}
