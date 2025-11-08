package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class Transfer {
    private final CRServo transferLeft;
    private final CRServo transferRight;
    private final CRServo intakeDoorLeft;
    private final CRServo intakeDoorRight;

    // Servo power constants
    public static double FORWARD_POWER = 1.0;
    public static double BACKWARD_POWER = -1.0;
    public static double STOP_POWER = 0.0;

    public Transfer(HardwareMap hardwareMap) {
        transferLeft = hardwareMap.get(CRServo.class, "transferLeft");
        transferRight = hardwareMap.get(CRServo.class, "transferRight");
        intakeDoorLeft = hardwareMap.get(CRServo.class, "intakeDoorLeft");
        intakeDoorRight = hardwareMap.get(CRServo.class, "intakeDoorRight");
    }

    // Actions for transfer servos
    public Action transferForward() {
        return packet -> {
            transferLeft.setPower(FORWARD_POWER);
            transferRight.setPower(FORWARD_POWER);
            return true;
        };
    }

    public Action transferBackward() {
        return packet -> {
            transferLeft.setPower(BACKWARD_POWER);
            transferRight.setPower(BACKWARD_POWER);
            return true;
        };
    }

    public Action transferStop() {
        return packet -> {
            transferLeft.setPower(STOP_POWER);
            transferRight.setPower(STOP_POWER);
            return true;
        };
    }

    // Actions for intake door servos
    public Action intakeDoorForward() {
        return packet -> {
            intakeDoorLeft.setPower(FORWARD_POWER);
            intakeDoorRight.setPower(FORWARD_POWER);
            return true;
        };
    }

    public Action intakeDoorBackward() {
        return packet -> {
            intakeDoorLeft.setPower(BACKWARD_POWER);
            intakeDoorRight.setPower(BACKWARD_POWER);
            return true;
        };
    }

    public Action intakeDoorStop() {
        return packet -> {
            intakeDoorLeft.setPower(STOP_POWER);
            intakeDoorRight.setPower(STOP_POWER);
            return true;
        };
    }
}
