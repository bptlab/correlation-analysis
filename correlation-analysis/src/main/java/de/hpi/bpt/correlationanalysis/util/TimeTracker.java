package de.hpi.bpt.correlationanalysis.util;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimeTracker {
    public static <T> T runTimed(Supplier<T> function, String message) {
        var stopWatch = start(message);
        var result = function.get();
        stopAndPrint(stopWatch);
        return result;
    }

    public static void runTimed(Runnable runnable, String message) {
        var stopWatch = start(message);
        runnable.run();
        stopAndPrint(stopWatch);
    }

    private static StopWatch start(String message) {
        StopWatch stopWatch = new StopWatch();
        System.out.println(message + "...");
        stopWatch.start();
        return stopWatch;
    }

    private static void stopAndPrint(StopWatch stopWatch) {
        stopWatch.stop();
        System.out.println("\tdone (" + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms)");
    }
}
