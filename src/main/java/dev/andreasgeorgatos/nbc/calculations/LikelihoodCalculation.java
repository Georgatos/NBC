package dev.andreasgeorgatos.nbc.calculations;

import dev.andreasgeorgatos.nbc.data.Attribute;
import dev.andreasgeorgatos.nbc.data.DataManager;
import dev.andreasgeorgatos.nbc.data.TrainingPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LikelihoodCalculation {

    private DataManager dataManager;
    private Map<Attribute, Map<Object, Map<Object, Integer>>> likelihoods;
    private Map<Attribute, Map<Object, Integer>> totalResultCounts;
    private Map<Attribute, Map<Object, Map<Object, Double>>> conditionalProbabilities;

    public LikelihoodCalculation(DataManager dataManager) {
        this.dataManager = dataManager;
        this.likelihoods = new HashMap<>();
        this.totalResultCounts = new HashMap<>();
        this.conditionalProbabilities = new HashMap<>();
        calculateLikelihood();
        calculateConditionalProbabilities();
        printLikelihoodTable();
        printConditionalProbabilityTable();
    }

    public void calculateLikelihood() {
        for (Attribute attribute : dataManager.getAttributes()) {
            Map<Object, Map<Object, Integer>> valueResultCount = new HashMap<>();
            for (Object value : attribute.getAllowedValues()) {
                valueResultCount.put(value, new HashMap<>());
            }
            likelihoods.put(attribute, valueResultCount);
        }

        for (TrainingPatterns pattern : dataManager.getPatterns()) {
            Object result = pattern.getResult().values().iterator().next();
            Attribute attribute = pattern.getAttribute();
            Object value = pattern.getValue();

            Map<Object, Integer> resultCount = likelihoods.get(attribute).get(value);
            resultCount.put(result, resultCount.getOrDefault(result, 0) + 1);

            totalResultCounts
                    .computeIfAbsent(attribute, k -> new HashMap<>())
                    .computeIfAbsent(result, k -> 0);

            totalResultCounts.get(attribute).put(result, totalResultCounts.get(attribute).get(result) + 1);
        }

        applyLaplaceSmoothingIfNeeded();
    }

    private void applyLaplaceSmoothingIfNeeded() {
        boolean needsSmoothing = false;

        for (Attribute attribute : likelihoods.keySet()) {
            for (Object value : likelihoods.get(attribute).keySet()) {
                for (Object result : totalResultCounts.get(attribute).keySet()) {
                    Map<Object, Integer> resultCount = likelihoods.get(attribute).get(value);
                    if (!resultCount.containsKey(result)) {
                        needsSmoothing = true;
                    }
                }
            }
        }

        if (needsSmoothing) {
            for (Attribute attribute : likelihoods.keySet()) {
                for (Object value : likelihoods.get(attribute).keySet()) {
                    Map<Object, Integer> resultCount = likelihoods.get(attribute).get(value);
                    for (Object result : totalResultCounts.get(attribute).keySet()) {
                        resultCount.put(result, resultCount.getOrDefault(result, 0) + 1);
                    }
                }

                for (Object result : totalResultCounts.get(attribute).keySet()) {
                    totalResultCounts.get(attribute).put(result, totalResultCounts.get(attribute).get(result) + likelihoods.get(attribute).size());
                }
            }
        }
    }

    public void calculateConditionalProbabilities() {
        for (Attribute attribute : likelihoods.keySet()) {
            Map<Object, Map<Object, Double>> valueResultProb = new HashMap<>();
            for (Object value : likelihoods.get(attribute).keySet()) {
                Map<Object, Double> resultProb = new HashMap<>();
                for (Object result : likelihoods.get(attribute).get(value).keySet()) {
                    int count = likelihoods.get(attribute).get(value).get(result);
                    int total = totalResultCounts.get(attribute).get(result);
                    double probability = (double) count / total;
                    resultProb.put(result, probability);
                }
                valueResultProb.put(value, resultProb);
            }
            conditionalProbabilities.put(attribute, valueResultProb);
        }
    }

    public void printLikelihoodTable() {
        System.out.println("Likelihood Table:");
        System.out.println(String.format("%-20s %-20s %-20s %s", "Attribute", "Value", "Result", "Count (Total)"));
        likelihoods.forEach((attribute, valueResultCounts) -> {
            System.out.println("Attribute: " + attribute.getAttributeName());
            valueResultCounts.forEach((value, resultCounts) -> {
                resultCounts.forEach((result, count) -> {
                    int total = totalResultCounts.get(attribute).get(result);
                    System.out.println(String.format("%-20s %-20s %-20d %d / %d", value, result, count, count, total));
                });
            });
            System.out.println();
        });
    }

    public void printConditionalProbabilityTable() {
        System.out.println("Conditional Probability Table:");
        System.out.println(String.format("%-20s %-20s %-20s %s", "Attribute", "Value", "Result", "Probability"));
        conditionalProbabilities.forEach((attribute, valueResultProbs) -> {
            System.out.println("Attribute: " + attribute.getAttributeName());
            valueResultProbs.forEach((value, resultProbs) -> {
                resultProbs.forEach((result, probability) -> {
                    System.out.println(String.format("%-20s %-20s %-20s %.4f", value, result, result, probability));
                });
            });
            System.out.println();
        });
    }

    public Map<Attribute, Map<Object, Map<Object, Double>>> getConditionalProbabilities() {
        return conditionalProbabilities;
    }
}
