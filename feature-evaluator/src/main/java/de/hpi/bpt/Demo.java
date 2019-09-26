package de.hpi.bpt;

import ro.pippo.core.Pippo;

public class Demo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo(new FeatureEvaluationApplication());
        pippo.start();
    }
}
