package org.firstinspires.ftc.teamcode.VisionLime;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class AprilTagLime extends OpMode {
	private Limelight3A limelight;
	@Override
	public void init() {
		limelight = hardwareMap.get(Limelight3A.class, "limelight");
		limelight.pipelineSwitch(8);
	}

	@Override
	public void loop() {

	}

	@Override
	public void start() {

	}
}
