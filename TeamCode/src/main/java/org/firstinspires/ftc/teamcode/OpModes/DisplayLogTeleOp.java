package org.firstinspires.ftc.teamcode.OpModes;

import android.os.Environment;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@TeleOp(name = "Display Obelisk Log", group = "Utility")
public class DisplayLogTeleOp extends LinearOpMode {

    private static final String LOG_FILE_NAME = "ObeliskDetectionLog.csv";
    private static final String BASE_FOLDER_NAME = "FIRST";

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE); // Optional: for better formatting
        telemetry.addLine("Reading log file: " + LOG_FILE_NAME);
        telemetry.update();

        File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + BASE_FOLDER_NAME + "/" + LOG_FILE_NAME);

        if (!logFile.exists()) {
            telemetry.addLine("Log file does not exist.");
            telemetry.update();
            waitForStart();
            while (opModeIsActive() && !isStopRequested()) {
                idle();
            }
            return;
        }

        telemetry.addLine("Press START to display log content.");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                int lineNum = 0;
                telemetry.clearAll();
                telemetry.addLine("--- Log Content ---");
                while ((line = reader.readLine()) != null && opModeIsActive()) {
                    telemetry.addData(String.format("%03d", ++lineNum), line);
                    // Update telemetry frequently, but not too fast to be unreadable
                    if (lineNum % 8 == 0) { // Update every 8 lines or so
                        telemetry.update();
                    }
                }
                telemetry.update(); // Final update for any remaining lines

            } catch (IOException e) {
                telemetry.clearAll();
                telemetry.addLine("Error reading log file:" + e.getMessage());
                telemetry.update();
            }

            // Keep showing the log until stop is pressed
            while (opModeIsActive() && !isStopRequested()) {
                idle();
            }
        }
    }
}
