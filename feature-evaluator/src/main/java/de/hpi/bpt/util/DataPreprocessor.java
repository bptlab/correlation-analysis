package de.hpi.bpt.util;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class DataPreprocessor {

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

    public Instances remove(Collection<String> attributesToIgnore, Instances data) {
        var indicesToRemove = IntStream.range(0, data.numAttributes()).filter(i -> {
            for (String attributeToIgnore : attributesToIgnore) {
                var name = data.attribute(i).name();
                if (name.equals(attributeToIgnore)) {
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
