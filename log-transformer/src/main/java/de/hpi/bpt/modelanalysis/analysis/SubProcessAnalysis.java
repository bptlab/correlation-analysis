package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.SubProcessFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.SubProcess;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class SubProcessAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var activityToSubProcess = new HashMap<String, String>();

        var subProcesses = modelInstance.getModelElementsByType(SubProcess.class);
        for (SubProcess subProcess : subProcesses) {
            var containedActivities = subProcess.getChildElementsByType(Activity.class);
            containedActivities.forEach(activity -> activityToSubProcess.put(activity.getName(), subProcess.getName()));
        }

        analysisResults.add(new SubProcessFeature(
                subProcesses.stream().map(SubProcess::getName).collect(Collectors.toList()),
                activityToSubProcess
        ));
    }
}
