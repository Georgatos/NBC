package dev.andreasgeorgatos.nbc.calculations;

import dev.andreasgeorgatos.nbc.data.DataManager;
import dev.andreasgeorgatos.nbc.data.attributes.AttributeDescription;
import dev.andreasgeorgatos.nbc.data.attributes.AttributeType;
import dev.andreasgeorgatos.nbc.data.likelihood.LikelihoodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class PosteriorCalculation {

    private final Logger logger = LoggerFactory.getLogger(PosteriorCalculation.class);

    public void printPosteriorProbability(Set<AttributeDescription> attributeDescription, DataManager dataManager) {
        Map<Object, Double> probabilities = calculatePosteriorProbability(attributeDescription, dataManager);

        for (Map.Entry<Object, Double> entry : probabilities.entrySet()) {
            System.out.println("Posterior probability of class " + entry.getKey() + ": " + entry.getValue());
        }
    }

    public Map<Object, Double> calculatePosteriorProbability(Set<AttributeDescription> attributeDescription, DataManager dataManager) {
        Map<AttributeDescription, Object> userInput = getInputFromKeyboard(attributeDescription);

        Map<Object, Double> posteriorProbabilities = new HashMap<>();

        for (LikelihoodData likelihood : dataManager.getLikelihoodData()) {
            posteriorProbabilities.put(likelihood.getClassValue(), 1.0);
        }

        for (Map.Entry<AttributeDescription, Object> entry : userInput.entrySet()) {
            for (LikelihoodData likelihood : dataManager.getLikelihoodData()) {
                if (likelihood.getAttributeDescription().equals(entry.getKey())) {
                    double currentProbability = posteriorProbabilities.get(likelihood.getClassValue());
                    if (likelihood.getAttributeDescription().type() == AttributeType.DISTINCT) {
                        if (likelihood.getAttributeValue().equals(entry.getValue())) {
                            posteriorProbabilities.put(likelihood.getClassValue(), currentProbability * likelihood.getConditionalProbability());
                        }
                    } else if (likelihood.getAttributeDescription().type() == AttributeType.CONTINUOUS) {
                        double mean = likelihood.getMean();
                        double variance = likelihood.getVariance();
                        double value = Double.parseDouble((String) entry.getValue());

                        double normalDistribution = calculateNormalDistribution(value, mean, variance);
                        posteriorProbabilities.put(likelihood.getClassValue(), currentProbability * normalDistribution);
                    }
                }
            }
        }

        for (Map.Entry<Object, Double> prior : dataManager.getPriorProbabilities().entrySet()) {
            for (Map.Entry<Object, Double> posterior : posteriorProbabilities.entrySet()) {
                if (posterior.getKey().equals(prior.getKey())) {
                    posterior.setValue(posterior.getValue() * prior.getValue());
                }
            }
        }

        double totalProbability = posteriorProbabilities.values().stream().mapToDouble(Double::doubleValue).sum();

        for (Map.Entry<Object, Double> entry : posteriorProbabilities.entrySet()) {
            posteriorProbabilities.put(entry.getKey(), entry.getValue() / totalProbability);
        }

        return posteriorProbabilities;
    }

    private double calculateNormalDistribution(double value, double mean, double variance) {
        double exponent = Math.exp(-((value - mean) * (value - mean)) / (2 * variance));
        return (1 / Math.sqrt(2 * Math.PI * variance)) * exponent;
    }


    public Map<AttributeDescription, Object> getInputFromKeyboard(Set<AttributeDescription> attributeDescription) {
        Scanner scanner = new Scanner(System.in);
        Map<AttributeDescription, Object> data = new HashMap<>();

        for (AttributeDescription attribute : attributeDescription) {
            if (attribute.type() == AttributeType.DISTINCT) {
                System.out.println("Input data for attribute: " + attribute.name() + " you are only allowed to input the following data: " + attribute.allowedAttributeValues());
                String inputData = scanner.nextLine();


                while (!attribute.allowedAttributeValues().contains(inputData)) {
                    System.out.println("Please input the correct data: " + attribute.allowedAttributeValues());
                    inputData = scanner.nextLine();
                }
                data.put(attribute, inputData);
            } else if (attribute.type() == AttributeType.CONTINUOUS) {
                System.out.println("Input data for attribute: " + attribute.name() +
                        ". You are only allowed to input a value between " +
                        attribute.allowedAttributeValues().get(0) + " and " +
                        attribute.allowedAttributeValues().get(1) + ".");

                String inputData = scanner.nextLine();
                double inputValue = 0;

                double minValue = (double) attribute.allowedAttributeValues().get(0);
                double maxValue = (double) attribute.allowedAttributeValues().get(1);

                boolean validInput = false;

                try {
                    inputValue = Double.parseDouble(inputData);
                    if (inputValue >= minValue && inputValue <= maxValue) {
                        validInput = true;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("That's not a number.");
                }

                while (!validInput) {
                    System.out.println("Please input a value between " + minValue + " and " + maxValue + ":");
                    inputData = scanner.nextLine();

                    try {
                        inputValue = Double.parseDouble(inputData);
                        if (inputValue >= minValue && inputValue <= maxValue) {
                            validInput = true;
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("That's not a number.");
                    }
                }
                data.put(attribute, inputData);
            }
        }
        return data;

    }

}
