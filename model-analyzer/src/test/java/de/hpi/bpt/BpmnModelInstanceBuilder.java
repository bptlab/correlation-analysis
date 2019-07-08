package de.hpi.bpt;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;

public class BpmnModelInstanceBuilder {

    private final BpmnModelInstance modelInstance;

    public BpmnModelInstanceBuilder() {
        modelInstance = Bpmn.createEmptyModel();

        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        Process process = modelInstance.newInstance(Process.class);
        process.setId("process");
        definitions.addChildElement(process);
    }

    public <T extends BpmnModelElementInstance> T createElement(String name, Class<T> elementClass) {
        var process = modelInstance.getModelElementById("process");
        T element = modelInstance.newInstance(elementClass);
        element.setAttributeValue("id", name, true);
        element.setAttributeValue("name", name);
        process.addChildElement(element);
        return element;
    }

    public void connect(FlowNode from, FlowNode to) {
        String identifier = from.getId() + "-" + to.getId();
        SequenceFlow sequenceFlow = createElement(identifier, SequenceFlow.class);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
    }

    public BpmnModelInstance getModelInstance() {
        Bpmn.validateModel(modelInstance);
        return modelInstance;
    }
}
