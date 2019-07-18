package de.hpi.bpt;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.FlowNodeRef;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class BpmnModelInstanceBuilder {

    private final BpmnModelInstance modelInstance;
    private Process process;
    private ModelElementInstance parent;

    public BpmnModelInstanceBuilder() {
        modelInstance = Bpmn.createEmptyModel();

        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        process = modelInstance.newInstance(Process.class);
        process.setId("process");
        definitions.addChildElement(process);
        parent = process;
    }

    public BpmnModelInstanceBuilder workIn(ModelElementInstance parent) {
        this.parent = parent;
        return this;
    }

    public <T extends BpmnModelElementInstance> T createElement(String name, Class<T> elementClass) {
        T element = modelInstance.newInstance(elementClass);
        element.setAttributeValue("id", name, true);
        element.setAttributeValue("name", name);
        parent.addChildElement(element);
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

    public Process getProcess() {
        return process;
    }

    public void addFlowNodeRefFor(ModelElementInstance... modelElements) {
        for (ModelElementInstance modelElement : modelElements) {
            var ref = modelInstance.newInstance(FlowNodeRef.class);
            ref.setTextContent(modelElement.getAttributeValue("id"));
            parent.addChildElement(ref);
        }
    }
}
