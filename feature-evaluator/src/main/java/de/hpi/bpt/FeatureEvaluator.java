package de.hpi.bpt;

import weka.attributeSelection.ReliefFAttributeEval;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class FeatureEvaluator {

    private String targetVariable = "duration";

    public Map<String, Double> calculateFeatureScores(String filePath) {
        try {
            var reliefF = new ReliefFAttributeEval();
            var data = loadData(filePath);

            reliefF.buildEvaluator(data);

            var attributeScore = new HashMap<String, Double>();
            for (int i = 0; i < data.numAttributes(); i++) {
                attributeScore.put(
                        data.get(0).attribute(i).name(),
                        reliefF.evaluateAttribute(i)
                );
            }
            return attributeScore;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Instances loadData(String filePath) throws Exception {
        var data = loadDataFromFile(filePath);
        return prepareData(data);
    }

    private Instances prepareData(Instances data) throws Exception {
        setDataClass(data);
        return convertStringFieldsToNominal(data);
    }

    private void setDataClass(Instances data) {
        data.setClass(data.attribute(targetVariable));
    }

    private Instances convertStringFieldsToNominal(Instances data) throws Exception {
        var stringFieldIndices = IntStream.range(0, data.numAttributes())
                .filter(i -> data.attribute(i).isString())
                .mapToObj(i -> String.valueOf(i + 1))
                .collect(joining(","));

        StringToNominal stringToNominal = new StringToNominal();
        stringToNominal.setOptions(new String[]{"-R", stringFieldIndices});
        stringToNominal.setInputFormat(data);
        return Filter.useFilter(data, stringToNominal);
    }

    private Instances loadDataFromFile(String filePath) {
        try (var fileStream = new FileInputStream(filePath)) {
            return new ConverterUtils.DataSource(fileStream).getDataSet();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public FeatureEvaluator target(String targetVariable) {
        this.targetVariable = targetVariable;
        return this;
    }
}
