package de.hpi.bpt.evaluation;

import weka.core.Attribute;
import weka.core.Instances;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class CommonValueCollector {

    public String collectCommonValues(Instances singleClassData) {
        var result = new HashMap<String, Map<String, Integer>>();

        var numAttributes = singleClassData.numAttributes();
        IntStream.range(0, numAttributes)
                .mapToObj(i -> singleClassData.attribute(i).name())
                .forEach(attribute -> result.put(attribute, new HashMap<>()));

        var numValues = singleClassData.size();
        var instances = singleClassData.enumerateInstances();

        while (instances.hasMoreElements()) {
            var instance = instances.nextElement();
            var attributes = instance.enumerateAttributes();
            while (attributes.hasMoreElements()) {
                var attribute = attributes.nextElement();
                result.get(attribute.name()).merge(readableValue(attribute, instance.value(attribute)), 1, Integer::sum);
            }
        }

        return result.entrySet().stream()
                .map(attributeEntry -> attributeEntry.getKey() + ": " + attributeEntry.getValue().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(3)
                        .map(valueEntry -> valueEntry.getKey() + " (" + valueEntry.getValue() + ")")
                        .collect(joining(" | "))
                ).collect(joining("\n"));

    }

    private String readableValue(Attribute attribute, double value) {
        if (attribute.isNumeric()) {
            return String.valueOf(value);
        } else if (attribute.isDate()) {
            return Instant.ofEpochMilli((long) value).atZone(ZoneId.systemDefault()).toString();
        } else {
            return Double.isNaN(value) ? "NULL" : attribute.value((int) value);
        }
    }
}
