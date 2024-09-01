package dev.andreasgeorgatos.nbc.calculations;

import dev.andreasgeorgatos.nbc.data.attributes.TrainingPattern;
import dev.andreasgeorgatos.nbc.data.prior.PriorData;

import java.util.*;

public class PriorCalculator {

    public void printPriorProbability(Set<PriorData> priorClasses) {
        Map<Object, Double> prior = getPriorProbability(priorClasses);

        for (Map.Entry<Object, Double> entry : prior.entrySet()) {
            System.out.println("prior probability of: " + entry.getKey() + " = " + entry.getValue());
        }
    }

    public Map<Object, Double> getPriorProbability(Set<PriorData> priorClasses) {
        Map<Object, Double> priorProbability = new HashMap<>();

        long totalClassesCount = priorClasses
                .stream()
                .mapToLong(PriorData::getCount)
                .sum();

        for (PriorData priorClass : priorClasses) {
            double probability = (double) priorClass.getCount() / totalClassesCount;
            priorProbability.put(priorClass.getClassValue(), probability);
        }

        return priorProbability;
    }

    public Set<PriorData> getClassMentions(List<TrainingPattern> trainingPatterns) {
        Set<PriorData> priors = new HashSet<>();

        for (TrainingPattern pattern : trainingPatterns) {
            if (containsClassValue(priors, pattern.getClassValue())) {
                PriorData priorData = getPriorData(priors, pattern.getClassValue());
                priorData.increaseCount();
            } else {
                priors.add(new PriorData(pattern.getClassValue()));
            }
        }
        return priors;
    }

    private PriorData getPriorData(Set<PriorData> priorData, Object className) {
        return priorData.stream().filter(prior -> prior.getClassValue().equals(className)).findFirst().orElse(null);
    }

    private boolean containsClassValue(Set<PriorData> priorData, Object className) {
        return priorData
                .stream()
                .anyMatch(prior -> prior.getClassValue().equals(className));
    }
}
