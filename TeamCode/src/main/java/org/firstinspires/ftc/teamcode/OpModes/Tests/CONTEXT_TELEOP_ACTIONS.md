## Teleop Actions
Most sample code in the docs involving actions use Actions.runBlocking() to run them. runBlocking() is a great fit for most autonomous programs, though it’s hard to integrate into teleop where there’s already a loop monitoring the gamepads.

Let’s see what’s going on inside the function and see how we can repurpose it.

```java
public static void runBlocking(Action action) {
    FtcDashboard dash = FtcDashboard.getInstance();
    Canvas previewCanvas = new Canvas();
    action.preview(previewCanvas);

    boolean running = true;
    while (running && !Thread.currentThread().isInterrupted()) {
        TelemetryPacket packet = new TelemetryPacket();
        packet.fieldOverlay().getOperations().addAll(previewCanvas.getOperations());

        running = action.run(packet);

        dash.sendTelemetryPacket(packet);
    }
}
```

At its core, runBlocking() is calling run() on the specified action until it returns false. The rest is to give feedback on the actions execution in FTC Dashboard. We can replicate this in teleop.

```java
public class TeleopWithActions extends OpMode {
    private FtcDashboard dash = FtcDashboard.getInstance();
    private List<Action> runningActions = new ArrayList<>();

    @Override
    public void init() {
    }

    @Override
    public void loop() {
        TelemetryPacket packet = new TelemetryPacket();

        // updated based on gamepads

        // update running actions
        List<Action> newActions = new ArrayList<>();
        for (Action action : runningActions) {
            action.preview(packet.fieldOverlay());
            if (action.run(packet)) {
                newActions.add(action);
            }
        }
        runningActions = newActions;

        dash.sendTelemetryPacket(packet);
    }
}
```

Actions can be queued up by adding them to the list.

```java
if (gamepad1.a) {
    runningActions.add(new SequentialAction(
        new SleepAction(0.5),
        new InstantAction(() -> servo.setPosition(0.5))
    ));
}
```