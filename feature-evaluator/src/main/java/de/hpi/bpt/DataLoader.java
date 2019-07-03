package de.hpi.bpt;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.FileInputStream;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

class DataLoader {

    Instances loadData(String filePath) {
        var data = loadDataFromFile(filePath);
        return convertStringFieldsToNominal(data);
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

    private Instances loadDataFromFile(String filePath) {
        try (var fileStream = new FileInputStream(filePath)) {
            return new ConverterUtils.DataSource(fileStream).getDataSet();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
