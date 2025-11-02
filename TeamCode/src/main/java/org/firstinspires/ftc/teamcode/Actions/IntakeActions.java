package org.firstinspires.ftc.teamcode.Actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeActions {

    DcMotor intake;

    CRServo intakeDoorLeft;
    CRServo intakeDoorRight;

    public IntakeActions(HardwareMap hw){
        intake = hw.get(DcMotor.class, "intake");
        intakeDoorLeft = hw.get(CRServo.class, "intakeDoorLeft");
        intakeDoorRight = hw.get(CRServo.class, "intakeDoorRight");
    }

    public class intakeDoorIn implements Action {
            private boolean initialized = false;
            double counter = 0;

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {

            if (!initialized) {
                intakeDoorLeft.setPower(1);
                intakeDoorRight.setPower(1);
                initialized = true;
            }

            counter = counter + 1;

            if (counter >= 3) {
                return true;
            } else {
                return false;
            }
        }

    }

    public Action intakeDoorIn() {
        return new intakeDoorIn();
    }


    public class intakeDoorOut implements Action {
        private boolean initialized = false;
         double counter = 0;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            if (!initialized) {
                intakeDoorLeft.setPower(-1);
                intakeDoorRight.setPower(-1);
                initialized = true;
            }
            counter = counter + 1;

            if (counter >= 3) {
                return true;
            } else {
                return false;
            }
        }

    }

    public Action intakeDoorOut(){
        return new intakeDoorOut();
    }


    public class intakeMotorIn implements Action {
        private boolean initialized = false;

        double counter = 0;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {

            if (!initialized) {
                intakeDoorLeft.setPower(-1);
                intakeDoorRight.setPower(-1);

                initialized = true;
            }
            counter = counter + 1;

            if (counter >= 3) {
                return true;
            } else {
                return false;
            }

        }

        }
    public Action intakeMotorIn(){
        return new intakeMotorIn();
    }


    public class intakeMotorOut implements Action {
        public boolean initialized = false;
        public double counter = 0;
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            if (!initialized) {
                intakeDoorLeft.setPower(-1);
                intakeDoorRight.setPower(-1);

                initialized = true;
            }
            counter = counter + 1;

            if (counter >= 3) {
                return true;
            } else {
                return false;
            }
        }
    }

    public Action intakeMotorOut(){
        return new intakeMotorOut();
    }



    public Action intakein(){
       return new ParallelAction(
               intakeDoorIn(),
               intakeMotorIn()
       );
    }

    public Action intakeOut(){
        return new ParallelAction(
                intakeDoorOut(),
                intakeMotorOut()
        );
    }



}
