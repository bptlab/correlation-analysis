package de.hpi.bpt.util;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static de.hpi.bpt.util.TimeTracker.runTimed;

public class DataLoader {

    private Set<String> attributesToIgnore = new HashSet<>();

    public DataLoader ignoring(String... attributeNames) {
        attributesToIgnore.addAll(Arrays.asList(attributeNames));
        return this;
    }

    public Instances prepareDataFromFileNominalKeepingNumeric(String filePath, String classAttributeName) {
        var data = loadDataFromFile(filePath);
        data.setClass(data.attribute(classAttributeName));
        data.deleteWithMissingClass();

        var nominal = runTimed(() -> stringToNominal(data), "String to nominal");
        var merged = runTimed(() -> mergeInfrequentNominalValues(nominal), "Merge infrequent nominal values");
        var processedData = runTimed(() -> removeUseless(merged), "Remove useless");

        return removeSelectedAttributes(processedData);
    }

    public Instances prepareDataFromFileNominal(String filePath, String classAttributeName) {
        var data = loadDataFromFile(filePath);
        data.setClass(data.attribute(classAttributeName));
        data.deleteWithMissingClass();

        var nominal = runTimed(() -> stringToNominal(data), "String to nominal");
        var discrete = runTimed(() -> numericToNominal(nominal), "Numeric to nominal");
        var merged = runTimed(() -> mergeInfrequentNominalValues(discrete), "Merge infrequent nominal values");
        var processedData = runTimed(() -> removeUseless(merged), "Remove useless");

        return removeSelectedAttributes(processedData);
    }

    public Instances prepareDataFromFileBinaryKeepingNumeric(String filePath, String classAttributeName) {
        var data = loadDataFromFile(filePath);
        data.setClass(data.attribute(classAttributeName));
        data.deleteWithMissingClass();

        var nominal = runTimed(() -> stringToNominal(data), "String to nominal");
        var merged = runTimed(() -> mergeInfrequentNominalValues(nominal), "Merge infrequent nominal values");
        var removeUseless = runTimed(() -> removeUseless(merged), "Remove useless");
        var processedData = runTimed(() -> nominalToBinary(removeUseless), "Nominal to binary");

        return removeSelectedAttributes(processedData);
    }

    public Instances prepareDataFromFileAllBinary(String filePath, String classAttributeName) {
        var data = loadDataFromFile(filePath);
        data.setClass(data.attribute(classAttributeName));
        data.deleteWithMissingClass();

        var nominal = runTimed(() -> stringToNominal(data), "String to nominal");
        var discrete = runTimed(() -> numericToNominal(nominal), "Numeric to nominal");
        var merged = runTimed(() -> mergeInfrequentNominalValues(discrete), "Merge infrequent nominal values");
        var removeUseless = runTimed(() -> removeUseless(merged), "Remove useless");
        var processedData = runTimed(() -> nominalToBinary(removeUseless), "Nominal to binary");

        return removeSelectedAttributes(processedData);
    }

    private Instances removeSelectedAttributes(Instances data) {
        var selectedClassAttribute = data.classAttribute().name();
        attributesToIgnore.add(selectedClassAttribute.split("=")[0]);
        var indicesToRemove = IntStream.range(0, data.numAttributes()).filter(i -> {
            for (String attributeToIgnore : attributesToIgnore) {
                var name = data.attribute(i).name();
                if (name.startsWith(attributeToIgnore) && !name.equals(selectedClassAttribute)) {
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
            mergeInfrequentNominalValues.setMinimumFrequency(100);
            mergeInfrequentNominalValues.setUseShortIDs(true);
            mergeInfrequentNominalValues.setAttributeIndices("first-last");
            mergeInfrequentNominalValues.setInputFormat(data);
            return Filter.useFilter(data, mergeInfrequentNominalValues);
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

    private Instances nominalToBinary(Instances data) {
        try {
            var nominalToBinary = new NominalToBinary();
            nominalToBinary.setBinaryAttributesNominal(true);
            nominalToBinary.setAttributeIndices(String.valueOf(data.classIndex()));
            nominalToBinary.setInvertSelection(true);
            nominalToBinary.setInputFormat(data);
            return Filter.useFilter(data, nominalToBinary);
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

    private Instances loadDataFromFile(String filePath) {
        try (var inputStream = new FileInputStream(filePath)) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(inputStream);
            return arffLoader.getDataSet();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
