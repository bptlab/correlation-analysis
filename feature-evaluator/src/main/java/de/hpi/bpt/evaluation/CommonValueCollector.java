package de.hpi.bpt.evaluation;

import weka.core.Instances;

import java.util.*;
import java.util.function.DoubleFunction;

public class CommonValueCollector {

    public ArrayList<CommonValue> collectCommonValues(Instances singleClassData) {
        var numAttributes = singleClassData.numAttributes();
        var numValues = singleClassData.size();
        var commonValues = new ArrayList<CommonValue>();
        for (int attributeIndex = 0; attributeIndex < numAttributes; attributeIndex++) {
            var attribute = singleClassData.attribute(attributeIndex);

            DoubleFunction<Object> valueMapper;
            if (attribute.isNumeric()) {
                valueMapper = d -> d;
            } else if (attribute.isDate()) {
                valueMapper = d -> new Date((long) d);
            } else {
                valueMapper = d -> Double.isNaN(d) ? "NULL" : attribute.value((int) d);
            }

            var counts = new HashMap<Object, Integer>();
            for (int valueIndex = 0; valueIndex < numValues; valueIndex++) {
                var value = valueMapper.apply(singleClassData.instance(valueIndex).value(attributeIndex));
                counts.merge(value, 1, Integer::sum);
            }

            var maxAttribute = Collections.max(counts.entrySet(), Comparator.comparingInt(Map.Entry::getValue));

            if (maxAttribute.getValue() > (numValues * 0.85)) {
                commonValues.add(new CommonValue(attributeIndex, attribute.name(), maxAttribute.getKey(), (double) maxAttribute.getValue() / (double) numValues));
            }
        }
        return commonValues;
    }

    public static class CommonValue {
        private int attributeIndex;
        private String attributeName;
        private Object value;
        private double percentage;

        public CommonValue(int attributeIndex, String attributeName, Object value, double percentage) {
            this.attributeIndex = attributeIndex;
            this.attributeName = attributeName;
            this.value = value;
            this.percentage = percentage;
        }

        public int getAttributeIndex() {
            return attributeIndex;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public Object getValue() {
            return value;
        }

        public double getPercentage() {
            return percentage;
        }

        @Override
        public String toString() {
            return String.format("%s = %s: %.2f%%", attributeName, value.toString(), percentage * 100.0);
        }
    }
}
