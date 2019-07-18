package de.hpi.bpt;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

class DataLoader {

    private Set<String> attributesToIgnore = new HashSet<>();

    DataLoader ignoring(String... attributeNames) {
        attributesToIgnore.addAll(Arrays.asList(attributeNames));
        return this;
    }

    Instances loadData(String dataAsArff) {
        try (var inputStream = new ByteArrayInputStream(dataAsArff.getBytes(StandardCharsets.UTF_8))) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(inputStream);
            var data = arffLoader.getDataSet();
            attributesToIgnore.forEach(attribute -> data.deleteAttributeAt(data.attribute(attribute).index()));
            return convertStringFieldsToNominal(data);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    Instances loadDataFromFile(String filePath) {
        try (var inputStream = new FileInputStream(filePath)) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(inputStream);
            var data = arffLoader.getDataSet();
            return convertStringFieldsToNominal(data);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances convertStringFieldsToNominal(Instances data) {
        var stringFieldIndices = IntStream.range(0, data.numAttributes())
                .filter(i -> data.attribute(i).isString())
                .mapToObj(i -> String.valueOf(i + 1))
                .collect(joining(","));

        try {
            StringToNominal stringToNominal = new StringToNominal();
            stringToNominal.setOptions(new String[]{"-R", stringFieldIndices});
            stringToNominal.setInputFormat(data);
            return Filter.useFilter(data, stringToNominal);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
