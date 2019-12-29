package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.feature.AbstractActivityPairFeature;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;

import java.util.Set;

public abstract class AbstractActivityPairAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var activities = modelInstance.getModelElementsByType(Activity.class);
        var analysisResult = feature();

        for (Activity activity : activities) {
            var firstActivityName = activity.getName();
            findCorrespondingActivities(activity)
                    .forEach(secondActivityName -> analysisResult.addActivityPair(firstActivityName, secondActivityName));
        }

        analysisResults.add(analysisResult);
    }

    abstract AbstractActivityPairFeature feature();

    abstract Set<String> findCorrespondingActivities(Activity activity);
}
