package de.hpi.bpt.util;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataPreprocessor {

    private Set<String> attributesToIgnore = new HashSet<>();

    private boolean convertNumericToNominal = false;
    private boolean replaceMissingNominalValues = false;

    public DataPreprocessor ignoring(List<String> attributesToIgnore) {
        this.attributesToIgnore.addAll(attributesToIgnore);
        return this;
    }

    public DataPreprocessor ignoring(String... attributeNames) {
        attributesToIgnore.addAll(Arrays.asList(attributeNames));
        return this;
    }

    public DataPreprocessor convertNumericToNominal() {
        convertNumericToNominal = true;
        return this;
    }

    public DataPreprocessor replaceMissingNominalValues() {
        replaceMissingNominalValues = true;
        return this;
    }

    public Instances process(Instances data, String classAttributeName) {
        data.setClass(data.attribute(classAttributeName));

        var processedData = data;
        if (replaceMissingNominalValues) {
            processedData = replaceMissingStringValuesWithConstant(processedData);
        }
        processedData = stringToNominal(processedData);
        if (convertNumericToNominal) {
            processedData = numericToNominal(processedData);
        }
        processedData = mergeInfrequentNominalValues(processedData);
        processedData = removeUseless(processedData);
        processedData = removeSelectedAttributes(processedData);

        processedData.deleteWithMissingClass();
        return processedData;
    }

    private Instances removeSelectedAttributes(Instances data) {
        var selectedClassAttribute = data.classAttribute().name();
        attributesToIgnore.add(selectedClassAttribute.split("=")[0]);
        var indicesToRemove = IntStream.range(0, data.numAttributes()).filter(i -> {
            for (String attributeToIgnore : attributesToIgnore) {
                var name = data.attribute(i).name();
                if (name.contains(attributeToIgnore) && !name.equals(selectedClassAttribute)) {
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

    private Instances mergeInfrequentNominalValues(Instances data) {
        try {
            var mergeInfrequentNominalValues = new MergeInfrequentNominalValues();
            mergeInfrequentNominalValues.setMinimumFrequency(10);
            mergeInfrequentNominalValues.setUseShortIDs(true);
            mergeInfrequentNominalValues.setAttributeIndices("first-last");
            mergeInfrequentNominalValues.setInputFormat(data);
            var merged = Filter.useFilter(data, mergeInfrequentNominalValues);

            var renameAttribute = new RenameAttribute();
            renameAttribute.setAttributeIndices("first-last");
            renameAttribute.setFind("_merged_infrequent_values");
            renameAttribute.setReplace("");
            renameAttribute.setInputFormat(merged);
            return Filter.useFilter(merged, renameAttribute);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances stringToNominal(Instances data) {
        try {
            var stringToNominal = new StringToNominal();
            stringToNominal.setOptions(new String[]{"-R", String.valueOf(data.classIndex()), "-V", "true"});
            stringToNominal.setInputFormat(data);
            return Filter.useFilter(data, stringToNominal);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances numericToNominal(Instances data) {
        try {
            var numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices(String.valueOf(data.classIndex()));
            numericToNominal.setInvertSelection(true);
            numericToNominal.setInputFormat(data);
            return Filter.useFilter(data, numericToNominal);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances removeUseless(Instances data) {
        try {
            var removeUseless = new RemoveUseless();
            removeUseless.setMaximumVariancePercentageAllowed(20.0);
            removeUseless.setInputFormat(data);
            return Filter.useFilter(data, removeUseless);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances replaceMissingStringValuesWithConstant(Instances data) {
        try {
            var nominalAttributes = IntStream.range(0, data.numAttributes())
                    .filter(i -> data.attribute(i).isString())
                    .mapToObj(i -> String.valueOf(i + 1))
                    .collect(Collectors.joining(","));

            var replaceMissing = new ReplaceMissingWithUserConstant();
            replaceMissing.setNominalStringReplacementValue("<MISSING>");
            replaceMissing.setDateFormat("YYYY");
            replaceMissing.setDateReplacementValue("2000"); // just to make the filter work. Will only be applied to nominal values
            replaceMissing.setAttributes(nominalAttributes);
            replaceMissing.setInputFormat(data);
            return Filter.useFilter(data, replaceMissing);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
