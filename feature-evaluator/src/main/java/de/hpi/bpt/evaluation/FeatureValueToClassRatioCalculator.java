package de.hpi.bpt.evaluation;

import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeatureValueToClassRatioCalculator {

    public List<FeatureToClassRatio> calculate(Instances data) {
        var result = new ArrayList<FeatureToClassRatio>();

        var classAttribute = data.classAttribute();
        var classValuesAsDouble = data.attributeToDoubleArray(classAttribute.index());
        for (int i = 0; i < data.numAttributes(); i++) {
            var attribute = data.attribute(i);
            if ("caseid".equals(attribute.name()) || "vendor".equals(attribute.name())) {
                continue;
            }
            var uniqueValues = new HashMap<Double, List<Integer>>();
            var valuesAsDouble = data.attributeToDoubleArray(i);
            for (int j = 0; j < valuesAsDouble.length; j++) {
                var value = valuesAsDouble[j];
                uniqueValues.computeIfAbsent(value, k -> new ArrayList<>());
                uniqueValues.get(value).add(j);
            }

            var featureToClassRatio = new FeatureToClassRatio(attribute.name());
            for (var uniqueValue : uniqueValues.entrySet()) {
                var classValues = new HashMap<Object, Integer>();

                for (Integer valueIndex : uniqueValue.getValue()) {
                    var classValue = classAttribute.value(new Double(classValuesAsDouble[valueIndex]).intValue());
                    classValues.merge(classValue, 1, Integer::sum);
                }

                featureToClassRatio.addFeatureValue(
                        attribute.value(uniqueValue.getKey().intValue()),
                        uniqueValue.getValue().size(),
                        classValues
                );
            }
            result.add(featureToClassRatio);
        }
        return result;
    }

    static class FeatureToClassRatio {

        private String featureName;
        private List<FeatureValueToClassRatio> values = new ArrayList<>();

        public FeatureToClassRatio(String featureName) {
            this.featureName = featureName;
        }

        public void addFeatureValue(Object featureValue, int numCasesWithThisValue, Map<Object, Integer> valueDistribution) {
            values.add(new FeatureValueToClassRatio(featureValue, numCasesWithThisValue, valueDistribution));
        }

        public boolean hasNotableSplit() {
            return values.stream().anyMatch(FeatureValueToClassRatio::hasNotableSplit);
        }

        @Override
        public String toString() {
            return featureName + "\n" + values.stream().map(FeatureValueToClassRatio::toString).collect(Collectors.joining("\n------\n"));
        }

        static class FeatureValueToClassRatio {

            private Object featureValue;
            private int numCasesWithThisValue;
            private Map<Object, Integer> valueDistribution = new HashMap<>();

            FeatureValueToClassRatio(Object featureValue, int numCasesWithThisValue, Map<Object, Integer> valueDistribution) {

                this.featureValue = featureValue;
                this.numCasesWithThisValue = numCasesWithThisValue;
                this.valueDistribution.putAll(valueDistribution);
            }

            boolean hasNotableSplit() {
                return numCasesWithThisValue > 50 && valueDistribution.values().stream()
                        .map(value -> (double) (value * 100) / (double) numCasesWithThisValue)
                        .anyMatch(percentage -> percentage > 65);
            }

            @Override
            public String toString() {
                var sb = new StringBuilder();
                sb
                        .append("\tvalue: ").append(featureValue).append("\n\tclass value distribution:\n\t\t");

                for (var entry : valueDistribution.entrySet()) {
                    double percentage = (double) (entry.getValue() * 100) / (double) numCasesWithThisValue;
                    sb.append(entry.getKey()).append(" -> ").append(percentage).append("% (")
                            .append(entry.getValue()).append("/").append(numCasesWithThisValue).append(")\n\t\t");
                }
                return sb.toString();
            }
        }
    }
}
