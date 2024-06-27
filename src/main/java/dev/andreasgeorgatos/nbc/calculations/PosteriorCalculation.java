package dev.andreasgeorgatos.nbc.calculations;

import dev.andreasgeorgatos.nbc.data.Attribute;
import dev.andreasgeorgatos.nbc.data.DataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PosteriorCalculation {
    private final DataManager dataManager;
    private final PriorCalculation priorCalculation;
    private final LikelihoodCalculation likelihoodCalculation;

    public PosteriorCalculation(DataManager dataManager, PriorCalculation priorCalculation, LikelihoodCalculation likelihoodCalculation) {
        this.dataManager = dataManager;
        this.priorCalculation = priorCalculation;
        this.likelihoodCalculation = likelihoodCalculation;
        Map<Attribute, Object> input = getInputFromKeyboard();
        printPosteriorProbabilities(input);
    }

    public Map<Object, Double> calculatePosteriorProbabilities(Map<Attribute, Object> input) {
        Map<Object, Double> posteriorProbabilities = new HashMap<>();
        Map<Attribute, Map<Object, Map<Object, Double>>> conditionalProbabilities = likelihoodCalculation.getConditionalProbabilities();
        Map<Object, Double> priorProbabilities = priorCalculation.getPriorProbabilities();

        for (Object possibleResult : dataManager.getAllPossibleResults()) {
            double posterior = 1.0;
            for (Map.Entry<Attribute, Object> entry : input.entrySet()) {
                Attribute attribute = entry.getKey();
                Object value = entry.getValue();
                double conditionalProbability = conditionalProbabilities.get(attribute).get(value).get(possibleResult);
                posterior *= conditionalProbability;
            }
            double priorProbability = priorProbabilities.getOrDefault(possibleResult, 0.0);
            posterior *= priorProbability;
            posteriorProbabilities.put(possibleResult, posterior);
        }
        return normalize(posteriorProbabilities);
    }

    private Map<Object, Double> normalize(Map<Object, Double> posteriorProbabilities) {
        double sum = posteriorProbabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        posteriorProbabilities.replaceAll((key, value) -> value / sum);
        return posteriorProbabilities;
    }

    public void printPosteriorProbabilities(Map<Attribute, Object> input) {
        Map<Object, Double> posteriorProbabilities = calculatePosteriorProbabilities(input);
        System.out.println("Posterior Probabilities:");
        posteriorProbabilities.forEach((result, probability) -> {
            System.out.println(String.format("Class: %s, Probability: %.4f", result, probability));
        });
    }

    private Map<Attribute, Object> getInputFromKeyboard() {
        Scanner scanner = new Scanner(System.in);
        Map<Attribute, Object> input = new HashMap<>();

        System.out.println("Please provide the attribute values:");
        for (Attribute attribute : dataManager.getAttributes()) {
            System.out.print("Enter value for " + attribute.getAttributeName() + ": ");
            String value = scanner.nextLine();
            Object parsedValue = parseData(value);
            input.put(attribute, parsedValue);
        }

        return input;
    }

    private Object parseData(String data) {
        data = data.trim().toLowerCase();
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(data);
            } catch (NumberFormatException ex) {
                return data;
            }
        }
    }
}
