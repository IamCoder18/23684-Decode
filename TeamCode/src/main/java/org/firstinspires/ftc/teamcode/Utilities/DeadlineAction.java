package org.firstinspires.ftc.teamcode.Utilities;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;

import java.util.Arrays;
import java.util.List;

public class DeadlineAction implements Action {
	private final List<Action> actions;

	public DeadlineAction(List<Action> actions) {
		this.actions = actions;
	}

	public DeadlineAction(Action... actions) {
		this(Arrays.asList(actions));
	}

	@Override
	public boolean run(@NonNull TelemetryPacket p) {
		// Execute all actions to ensure they run
		for (int i = 0; i < actions.size() - 1; i++) {
			actions.get(i).run(p);
		}

		// Execute the deadline action (the last one) and use its result
		// to determine if the entire group is finished.
		if (!actions.isEmpty()) {
			return actions.get(actions.size() - 1).run(p);
		}

		// Fallback if the list is empty (immediate completion)
		return false;
	}

	@Override
	public void preview(@NonNull Canvas fieldOverlay) {
		for (Action a : actions) {
			a.preview(fieldOverlay);
		}
	}
}

