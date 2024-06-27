package dev.andreasgeorgatos.nbc.calculations;

import dev.andreasgeorgatos.nbc.data.DataManager;
import dev.andreasgeorgatos.nbc.data.TrainingPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriorCalculation {
    private final Logger logger = LoggerFactory.getLogger(PriorCalculation.class);

    private DataManager dataManager;
    private Map<Object, Double> priorProbabilities;

    public PriorCalculation(DataManager dataManager) {
        this.dataManager = dataManager;
        this.priorProbabilities = new HashMap<>();
        calculateProbabilities();
        printPriorProbabilities();
    }

    public void calculateProbabilities() {
        List<TrainingPatterns> patterns = dataManager.getPatterns();
        Map<Object, Integer> classCounts = new HashMap<>();
        int totalPatterns = patterns.size();

        for (TrainingPatterns pattern : patterns) {
            pattern.getResult().values().forEach(classValue ->
                    classCounts.merge(classValue, 1, Integer::sum));
        }

        classCounts.forEach((key, count) ->
                priorProbabilities.put(key, count / (double) totalPatterns));
    }

    public void printPriorProbabilities() {
        if (priorProbabilities.isEmpty()) {
            logger.info("No prior probabilities calculated. Please calculate them first.");
            return;
        }
        priorProbabilities.forEach((key, value) ->
                System.out.println("Class: " + key + ", Prior Probability: " + value));
    }


    public Map<Object, Double> getPriorProbabilities() {
        return priorProbabilities;
    }
}
