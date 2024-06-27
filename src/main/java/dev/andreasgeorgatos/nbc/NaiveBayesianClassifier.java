package dev.andreasgeorgatos.nbc;

import dev.andreasgeorgatos.nbc.calculations.LikelihoodCalculation;
import dev.andreasgeorgatos.nbc.calculations.PosteriorCalculation;
import dev.andreasgeorgatos.nbc.calculations.PriorCalculation;
import dev.andreasgeorgatos.nbc.data.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NaiveBayesianClassifier {

    private final Logger logger = LoggerFactory.getLogger(NaiveBayesianClassifier.class);
    private final FileManager fileManager;
    private final DataManager dataManager;
    private final PriorCalculation priorCalculation;
    private final LikelihoodCalculation likelihoodCalculation;

    public NaiveBayesianClassifier() {
        fileManager = new FileManager();
        dataManager = new DataManager(fileManager);
        priorCalculation = new PriorCalculation(dataManager);
        likelihoodCalculation = new LikelihoodCalculation(dataManager);
        new PosteriorCalculation(dataManager, priorCalculation, likelihoodCalculation);
    }

    public static void main(String[] args) {
        new NaiveBayesianClassifier();
    }
}
