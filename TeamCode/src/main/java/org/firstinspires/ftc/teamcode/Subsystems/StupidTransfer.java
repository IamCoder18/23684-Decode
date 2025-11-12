package org.firstinspires.ftc.teamcode.Subsystems;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class StupidTransfer {

    CRServo transferRight;
    CRServo transferLeft;

    double averageRPM = 0;


    double needForSpeed = 2080;
    public StupidTransfer (HardwareMap hardwareMap) {

        transferRight = hardwareMap.get(CRServo.class, "transferRight");
        transferLeft = hardwareMap.get(CRServo.class, "transferLeft");

        transferRight.setDirection(DcMotorSimple.Direction.REVERSE);

    }

        public void UpdateTelemetry(TelemetryPacket packet) {
            packet.put("Average RPM", averageRPM);
        }


        public class TransferIn implements Action {
            boolean initialized = false;
            double timmythetime = 0;
            @Override
            public boolean run(@NonNull TelemetryPacket telemetryPacket) {

                timmythetime = timmythetime + 0.5;
                if (!initialized) {
                     transferRight.setPower(-1);
                     transferLeft.setPower(-1);
                    initialized = true;
                }
                return false;
            }
        }

        public Action tranferIn() {
            return new TransferIn();
        }

    public class TransferOut implements Action {
        boolean initialized = false;
        double timmythetime = 0;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {

            timmythetime = timmythetime + 0.5;
            if (!initialized) {
                transferRight.setPower(1);
                transferLeft.setPower(1);
                initialized = true;
            }
            return !true ;
        }
    }

    public Action tranferOut() {
        return new TransferIn();
    }

}
