package de.hpi.bpt.util;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class DataPreprocessor {

    private Set<String> attributesToIgnore = new HashSet<>();

    public DataPreprocessor ignoring(Set<String> attributesToIgnore) {
        this.attributesToIgnore.addAll(attributesToIgnore);
        return this;
    }

    public DataPreprocessor ignoring(String... attributeNames) {
        attributesToIgnore.addAll(Arrays.asList(attributeNames));
        return this;
    }

    public Instances simplePreprocess(Instances data, String classAttributeName) {
        try {
            data.setClass(data.attribute(classAttributeName));
            data.deleteWithMissingClass();
            var filter = stringToNominal();
            filter.setInputFormat(data);
            data = Filter.useFilter(data, filter);
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Instances process(Instances data, String classAttributeName) {
        data.setClass(data.attribute(classAttributeName));
        var multiFilter = new MultiFilter();

        try {
            var filters = new Filter[]{
                    stringToNominal(),
                    mergeInfrequentNominalValues(),
                    renameMergedAttributes(),
                    removeUseless()
            };
            multiFilter.setFilters(filters);
            multiFilter.setInputFormat(data);
            data = Filter.useFilter(data, multiFilter);
            data = removeSelectedAttributes(data);
            data.deleteWithMissingClass();
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Filter stringToNominal() {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        return stringToNominal;
    }

    private Filter mergeInfrequentNominalValues() {
        var mergeInfrequentNominalValues = new MergeInfrequentNominalValues();
        mergeInfrequentNominalValues.setMinimumFrequency(10);
        mergeInfrequentNominalValues.setUseShortIDs(true);
        mergeInfrequentNominalValues.setAttributeIndices("first-last");
        return mergeInfrequentNominalValues;
    }

    private Filter renameMergedAttributes() {
        var renameAttribute = new RenameAttribute();
        renameAttribute.setAttributeIndices("first-last");
        renameAttribute.setFind("_merged_infrequent_values");
        renameAttribute.setReplace("");
        return renameAttribute;
    }

    private Filter removeUseless() {
        var removeUseless = new RemoveUseless();
        removeUseless.setMaximumVariancePercentageAllowed(20.0);
        return removeUseless;
    }

    public Instances nominalToBinary(Instances data) {
        try {
            var nominalToBinary = new NominalToBinary();
            var attributesToConsider = IntStream.range(0, data.numAttributes())
                    .filter(i -> data.attribute(i).isNominal() && data.attribute(i).numValues() > 4 && data.attribute(i).index() != data.classIndex())
                    .mapToObj(i -> String.valueOf(i + 1))
                    .collect(joining(","));
            nominalToBinary.setAttributeIndices(attributesToConsider);
            nominalToBinary.setBinaryAttributesNominal(true);
            nominalToBinary.setInputFormat(data);
            return Filter.useFilter(data, nominalToBinary);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

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

    public Instances removeSelectedAttributes(Instances data) {
        var selectedClassAttribute = data.classAttribute().name();
        attributesToIgnore.add(selectedClassAttribute);
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
}
