package de.hpi.bpt;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    Instances loadDataFromFile(String filePath) {
        try (var inputStream = new FileInputStream(filePath)) {
            var arffLoader = new ArffLoader();
            arffLoader.setSource(inputStream);
            return arffLoader.getDataSet();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
