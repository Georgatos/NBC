package dev.andreasgeorgatos.nbc;

import dev.andreasgeorgatos.nbc.calculations.LikelihoodCalculator;
import dev.andreasgeorgatos.nbc.calculations.PosteriorCalculation;
import dev.andreasgeorgatos.nbc.calculations.PriorCalculator;
import dev.andreasgeorgatos.nbc.data.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NaiveBayesianClassifier {

    private final Logger logger = LoggerFactory.getLogger(NaiveBayesianClassifier.class);
    private final FileManager fileManager;
    private final PriorCalculator priorCalculation;
    private final LikelihoodCalculator likelihoodCalculator;
    private final DataManager dataManager;
    private final PosteriorCalculation posteriorCalculation;

    public NaiveBayesianClassifier() {
        fileManager = new FileManager();
        priorCalculation = new PriorCalculator();
        likelihoodCalculator = new LikelihoodCalculator();
        posteriorCalculation = new PosteriorCalculation();
        dataManager = new DataManager(fileManager, priorCalculation, likelihoodCalculator);

        priorCalculation.printPriorProbability(dataManager.getPriorClasses());

        likelihoodCalculator.calculateConditionalProbability(dataManager.getLikelihoodData());
        likelihoodCalculator.printLikelihoodData(dataManager.getLikelihoodData());

        posteriorCalculation.printPosteriorProbability(dataManager.getAttributeDescription(), dataManager);
    }

    public static void main(String[] args) {
        new NaiveBayesianClassifier();
    }
}
