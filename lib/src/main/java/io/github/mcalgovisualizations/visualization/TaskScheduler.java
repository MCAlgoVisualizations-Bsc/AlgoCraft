package io.github.mcalgovisualizations.visualization;

import java.time.Duration;

@FunctionalInterface
public interface TaskScheduler {

    @FunctionalInterface
    interface TaskHandle {
        void cancel();
    }

    TaskHandle schedule(Runnable runnable, Duration period);
}