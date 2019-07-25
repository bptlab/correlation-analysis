package de.hpi.bpt;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

class TimeTracker {
    static <T> T runTimed(Supplier<T> function, String message) {
        var stopWatch = start(message);
        var result = function.get();
        stopAndPrint(message, stopWatch);
        return result;
    }

    static void runTimed(Runnable runnable, String message) {
        var stopWatch = start(message);
        runnable.run();
        stopAndPrint(message, stopWatch);
    }

    private static StopWatch start(String message) {
        StopWatch stopWatch = new StopWatch();
        System.out.println("Starting: " + message);
        stopWatch.start();
        return stopWatch;
    }

    private static void stopAndPrint(String message, StopWatch stopWatch) {
        stopWatch.stop();
        System.out.println("Finished: " + message + " (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)");
    }
}
