package de.hpi.bpt.util;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class DataLoader {

    private Set<String> attributesToIgnore = new HashSet<>();

    public DataLoader ignoring(String... attributeNames) {
        attributesToIgnore.addAll(Arrays.asList(attributeNames));
        return this;
    }

    public Instances prepareDataFromString(String dataAsArff, String classAttributeName) {
        return process(loadDataFromString(dataAsArff), classAttributeName);

    }

    public Instances prepareDataFromFile(String filePath, String classAttributeName) {
        return process(loadDataFromFile(filePath), classAttributeName);
    }

    private Instances process(Instances data, String classAttributeName) {
        try {
            attributesToIgnore.forEach(attribute -> data.deleteAttributeAt(data.attribute(attribute).index()));

            var preprocessedData = applyFilters(data);

            preprocessedData.setClass(preprocessedData.attribute(classAttributeName));
            preprocessedData.deleteWithMissingClass();
            return preprocessedData;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private Instances applyFilters(Instances data) throws Exception {
        var nominal = Filter.useFilter(data, stringToNominalFilter(data));
        var removeUseless = Filter.useFilter(nominal, removeUselessFilter(nominal));
        return Filter.useFilter(removeUseless, removeEmptyAttributesFilter(removeUseless));
    }

    private StringToNominal stringToNominalFilter(Instances data) throws Exception {
        var stringToNominal = new StringToNominal();
        stringToNominal.setAttributeRange("first-last");
        stringToNominal.setInputFormat(data);
        return stringToNominal;
    }

    private RemoveUseless removeUselessFilter(Instances data) throws Exception {
        var removeUseless = new RemoveUseless();
        removeUseless.setMaximumVariancePercentageAllowed(100.0);
        removeUseless.setInputFormat(data);
        return removeUseless;
    }

    private Remove removeEmptyAttributesFilter(final Instances data) throws Exception {
        var toRemove = IntStream.range(0, data.numAttributes())
                .filter(i -> {
                    var attribute = data.attribute(i);
                    return attribute.isNominal() && attribute.numValues() == 0;
                })
                .toArray();
        var remove = new Remove();
        remove.setAttributeIndicesArray(toRemove);
        remove.setInputFormat(data);
        return remove;
    }

    private Instances loadDataFromString(String dataAsArff) {
        try (var inputStream = new ByteArrayInputStream(dataAsArff.getBytes(StandardCharsets.UTF_8))) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(inputStream);
            return arffLoader.getDataSet();
        } catch (IOException e) {
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
