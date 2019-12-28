package de.hpi.bpt.correlationanalysis.framework;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class DataPreprocessor {

    /**
     * Prepares the case data for feature selection and classification.
     * Updates the class attribute to contain only 2 different values: "= targetValue" and "!= targetValue"
     *
     * @param data Case data to pre-process.
     * @param targetValue Selected class value, all other values of the class attribute will be merged into one combined value.
     * @return Updated case data.
     */
    public Instances simplePreprocessAndMerge(Instances data, String targetValue) {
        try {
            var processedData = simplePreprocess(data);
            var targetValueIndex = processedData.classAttribute().indexOfValue(targetValue);
            if (targetValueIndex == -1) {
                throw new RuntimeException("Invalid target value selected!");
            }
            var merge = new MergeManyValues();
            merge.setLabel("not " + processedData.classAttribute().value(targetValueIndex));
            merge.setAttributeIndex(String.valueOf(processedData.classIndex() + 1));
            merge.setMergeValueRange(IntStream.range(1, processedData.classAttribute().numValues() + 1).filter(i -> i != (targetValueIndex + 1)).mapToObj(String::valueOf).collect(joining(",")));
            var oldClassIndex = processedData.classIndex();
            // workaround: MergeManyValues will not operate on the class attribute.
            if (oldClassIndex == 0) {
                processedData.setClassIndex(1);
            } else {
                processedData.setClassIndex(0);
            }
            merge.setInputFormat(processedData);
            var result = Filter.useFilter(processedData, merge);
            result.setClassIndex(oldClassIndex);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Prepares the case data for feature selection and classification.
     */
    public Instances simplePreprocess(Instances data) {
        try {
            data.deleteWithMissingClass();
            List<Filter> filters;
            if (data.classAttribute().isNumeric()) {
                filters = List.of(numericToNominal(data.classIndex()), stringToNominal(), removeUseless());
            } else {
                filters = List.of(stringToNominal(), removeUseless());
            }
            for (var filter : filters) {
                filter.setInputFormat(data);
                data = Filter.useFilter(data, filter);
            }

            return data;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Filter numericToNominal(int classAttributeIndex) {
        var numericToNominal = new NumericToNominal();
        numericToNominal.setAttributeIndicesArray(new int[]{classAttributeIndex});
        return numericToNominal;
    }

    private Filter stringToNominal() {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        return stringToNominal;
    }

    private Filter removeUseless() {
        var removeUseless = new RemoveUseless();
        removeUseless.setMaximumVariancePercentageAllowed(100);
        return removeUseless;
    }

    /**
     * Treat missing values as a separate value for classification, assuming that "no value" has a specific
     * meaning for attributes in the process.
     */
    public Instances replaceMissingStringValuesWithConstant(Instances data) {
        try {
            var nominalAttributes = IntStream.range(0, data.numAttributes())
                    .filter(i -> data.attribute(i).isString() || data.attribute(i).isNominal())
                    .mapToObj(i -> String.valueOf(i + 1))
                    .collect(joining(","));

            var replaceMissing = new ReplaceMissingWithUserConstant();
            replaceMissing.setNominalStringReplacementValue("MISSING*");
            replaceMissing.setDateFormat("YYYY");
            replaceMissing.setDateReplacementValue("2000"); // just to make the filter work. Will only be applied to nominal values
            replaceMissing.setAttributes(nominalAttributes);

            replaceMissing.setInputFormat(data);
            return Filter.useFilter(data, replaceMissing);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Instances remove(Collection<String> attributesToRemove, Instances data) {
        var indicesToRemove = IntStream.range(0, data.numAttributes()).filter(i -> {
            for (String attributeToRemove : attributesToRemove) {
                var name = data.attribute(i).name();
                if (name.equals(attributeToRemove)) {
                    System.out.println("Removing attribute '" + name + "'");
                    return true;
                }
            }
            return false;
        }).toArray();

        var remove = new Remove();
        remove.setAttributeIndicesArray(indicesToRemove);
        try {
            remove.setInputFormat(data);
            return Filter.useFilter(data, remove);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
