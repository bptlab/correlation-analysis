package de.hpi.bpt;

import weka.core.Instances;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;

public class CommonValueCollector {

    public ArrayList<CommonValue> collectCommonValues(Instances filteredData) {
        var numAttributes = filteredData.numAttributes();
        var numValues = filteredData.size();
        var commonValues = new ArrayList<CommonValue>();
        for (int i = 0; i < numAttributes; i++) {
            var attribute = filteredData.attribute(i);

            DoubleFunction<Object> valueMapper;
            if (attribute.isNumeric()) {
                valueMapper = d -> d;
            } else if (attribute.isDate()) {
                valueMapper = d -> new Date((long) d);
            } else {
                valueMapper = d -> attribute.value((int) d);
            }

            var valuesAsDouble = filteredData.attributeToDoubleArray(i);
            ConcurrentMap<Object, Integer> counts = Arrays.stream(valuesAsDouble)
                    .mapToObj(valueMapper)
                    .collect(Collectors.toConcurrentMap(
                            w -> w, w -> 1, Integer::sum));

            var maxAttribute = Collections.max(counts.entrySet(), Comparator.comparingInt(Map.Entry::getValue));

            if (maxAttribute.getValue() > numValues / 2) {
                commonValues.add(new CommonValue(attribute.name(), maxAttribute.getKey(), maxAttribute.getValue()));
            }
        }
        return commonValues;
    }

    public static class CommonValue {
        private String attributeName;
        private Object value;
        private int occurrences;

        public CommonValue(String attributeName, Object value, int occurrences) {
            this.attributeName = attributeName;
            this.value = value;
            this.occurrences = occurrences;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public Object getValue() {
            return value;
        }

        public int getOccurrences() {
            return occurrences;
        }
    }
}
