package de.hpi.bpt.correlationanalysis;

import ro.pippo.core.Pippo;

/**
 * Run this to start the server, then navigate to <a href="http://localhost:8080">http://localhost:8080</a>.
 * Port can be changed in {@code resources/application.properties}.
 */
public class Demo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo(new CorrelationAnalysisApplication());
        pippo.start();
    }
}
