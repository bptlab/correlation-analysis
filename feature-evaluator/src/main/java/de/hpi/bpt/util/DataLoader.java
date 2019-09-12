package de.hpi.bpt.util;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

public class DataLoader {

    private Set<String> attributesToIgnore = new HashSet<>();

    public DataLoader ignoring(String... attributeNames) {
        attributesToIgnore.addAll(Arrays.asList(attributeNames));
        return this;
    }

    public Instances prepareDataFromFile(String filePath, String classAttributeName) {
        var processedFilePath = filePath.replace(".arff", "_processed.arff.gz");
        Instances processedData;
        if (new File(processedFilePath).isFile()) {
            processedData = loadCompressedDataFromFile(processedFilePath);
        } else {
            processedData = process(loadDataFromFile(filePath), classAttributeName);
            writeDataToFile(processedData, processedFilePath);
        }

        return removeSelectedAttributes(processedData);
    }

    private Instances removeSelectedAttributes(Instances data) {
        var indicesToRemove = IntStream.range(0, data.numAttributes()).filter(i -> {
            for (String attributeToIgnore : attributesToIgnore) {
                var name = data.attribute(i).name();
                if (name.contains(attributeToIgnore)) {
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

    private Instances process(Instances data, String classAttributeName) {
        try {
            data.setClass(data.attribute(classAttributeName));
            data.deleteWithMissingClass();
            return applyFilters(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private Instances applyFilters(Instances data) throws Exception {
        var nominal = stringToNominal(data);
        var discrete = numericToNominal(nominal);
        var removeUseless = removeUseless(discrete);
        var nonEmpty = removeEmptyAttributes(removeUseless);
        var merged = mergeInfrequentNominalValues(nonEmpty);
        return nominalToBinary(merged);
    }

    private Instances mergeInfrequentNominalValues(Instances data) throws Exception {
        var mergeInfrequentNominalValues = new MergeInfrequentNominalValues();
        mergeInfrequentNominalValues.setMinimumFrequency(100);
        mergeInfrequentNominalValues.setAttributeIndices("first-last");
        mergeInfrequentNominalValues.setInputFormat(data);
        return Filter.useFilter(data, mergeInfrequentNominalValues);
    }

    private Instances stringToNominal(Instances data) throws Exception {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        stringToNominal.setInputFormat(data);
        return Filter.useFilter(data, stringToNominal);
    }

    private Instances discretize(Instances data) throws Exception {
        var discretize = new Discretize();
        discretize.setAttributeIndices("first-last");
        discretize.setFindNumBins(true);
        discretize.setMakeBinary(true);
        discretize.setInputFormat(data);
        return Filter.useFilter(data, discretize);
    }

    private Instances numericToNominal(Instances data) throws Exception {
        var numericToNominal = new NumericToNominal();
        numericToNominal.setAttributeIndices("first-last");
        numericToNominal.setInputFormat(data);
        return Filter.useFilter(data, numericToNominal);
    }

    private Instances nominalToBinary(Instances data) throws Exception {
        var nominalToBinary = new NominalToBinary();
        nominalToBinary.setBinaryAttributesNominal(true);
        nominalToBinary.setAttributeIndices("first-last");
        nominalToBinary.setInputFormat(data);
        return Filter.useFilter(data, nominalToBinary);
    }

    private Instances removeUseless(Instances data) throws Exception {
        var removeUseless = new RemoveUseless();
        removeUseless.setMaximumVariancePercentageAllowed(20.0);
        removeUseless.setInputFormat(data);
        return Filter.useFilter(data, removeUseless);
    }

    private Instances removeEmptyAttributes(final Instances data) throws Exception {
        var toRemove = IntStream.range(0, data.numAttributes())
                .filter(i -> {
                    var attribute = data.attribute(i);
                    return attribute.isNominal() && attribute.numValues() == 0;
                })
                .toArray();
        var remove = new Remove();
        remove.setAttributeIndicesArray(toRemove);
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
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

    private Instances loadCompressedDataFromFile(String filePath) {
        try (var fileInputStream = new FileInputStream(filePath);
             var gzipStream = new GZIPInputStream(fileInputStream)) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(gzipStream);
            return arffLoader.getDataSet();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void writeDataToFile(Instances data, String filePath) {
        try (var outputStream = new FileOutputStream(filePath)) {
            var arffSaver = new ArffSaver();
            arffSaver.setDestination(outputStream);
            arffSaver.setInstances(data);
            arffSaver.setCompressOutput(true);
            arffSaver.writeBatch();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
