package IF.Utilities.Patterns;

import IF.Utilities.Patterns.Delegates.Delegate1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedVariable<T>
{
    private       T                        value;
    private final ScheduledExecutorService scheduler;
    public        Delegate1<T>             onStop = new Delegate1<>();

    public TimedVariable(T initialValue, long duration, TimeUnit unit)
    {
        this.value     = initialValue;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(); // Or use a shared executor if appropriate

        // Schedule the nulling task
        scheduler.schedule(() -> {
            stop();
            value = null;
        }, duration, unit);
    }


    public synchronized T getValue()
    {
        return value;
    }


    public void stop()
    {
        onStop.Fire(value);
        scheduler.shutdown(); // Stop the scheduler to prevent leaks
    }

    public boolean isRunning()
    {
        return !scheduler.isShutdown() || !scheduler.isTerminated();
    }


}

