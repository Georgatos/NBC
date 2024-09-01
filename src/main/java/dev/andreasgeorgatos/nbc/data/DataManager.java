package dev.andreasgeorgatos.nbc.data;

import dev.andreasgeorgatos.nbc.FileManager;
import dev.andreasgeorgatos.nbc.calculations.LikelihoodCalculator;
import dev.andreasgeorgatos.nbc.calculations.PriorCalculator;
import dev.andreasgeorgatos.nbc.data.attributes.AttributeDescription;
import dev.andreasgeorgatos.nbc.data.attributes.AttributeType;
import dev.andreasgeorgatos.nbc.data.attributes.TrainingPattern;
import dev.andreasgeorgatos.nbc.data.likelihood.LikelihoodData;
import dev.andreasgeorgatos.nbc.data.prior.PriorData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final Logger logger = LoggerFactory.getLogger(DataManager.class);


    private final Set<AttributeDescription> attributeDescription;
    private final List<TrainingPattern> trainingPatterns;

    private final Set<PriorData> priorClasses;
    private final Map<Object, Double> priorProbabilities;
    private final List<LikelihoodData> likelihoodData;

    private final LikelihoodCalculator likelihoodCalculator;

    private Object className;


    public DataManager(FileManager fileManager, PriorCalculator priorCalculator, LikelihoodCalculator likelihoodCalculator) {
        this.attributeDescription = loadAttributeDescriptions(fileManager.getTrainingData());
        this.trainingPatterns = loadTrainingPatterns(fileManager.getTrainingData());
        this.likelihoodCalculator = likelihoodCalculator;

        priorClasses = priorCalculator.getClassMentions(trainingPatterns);
        priorProbabilities = priorCalculator.getPriorProbability(priorClasses);
        likelihoodData = likelihoodCalculator.calculateLikelihood(trainingPatterns, attributeDescription, className);
    }

    private List<TrainingPattern> loadTrainingPatterns(File trainingData) {

        List<TrainingPattern> trainingPatterns = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(trainingData))) {
            String line;

            while ((line = br.readLine()) != null && !line.isEmpty()) ;

            line = br.readLine(); //Skipping it

            className = line.split(",")[line.split(",").length - 1];

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                if (parseCommaSeperatedDataPattern(line.split(",")) != null) {
                    TrainingPattern trainingPattern = parseCommaSeperatedDataPattern(line.split(","));
                    addClassValue(trainingPattern, line.split(",")[line.split(",").length - 1]);
                    trainingPattern.setClassName(className);
                    trainingPatterns.add(trainingPattern);
                }
            }
        } catch (IOException e) {
            logger.info("We had an exception while in loadAttributeDescriptions(File trainingData), error:{} and cause: ", e.getMessage(), e.getCause());
        }
        return trainingPatterns;
    }

    private void addClassValue(TrainingPattern trainingPattern, String className) {
        trainingPattern.setClassValue(className);
    }


    private @NotNull Set<AttributeDescription> loadAttributeDescriptions(File trainingData) {

        Set<AttributeDescription> attributeDescriptions = new LinkedHashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(trainingData))) {
            String line;

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                attributeDescriptions.add(parseCommaSeperatedDataAttributes(line.split(",")));
            }
        } catch (IOException e) {
            logger.info("We had an exception while in loadAttributeDescriptions(File trainingData), error:{} and cause: ", e.getMessage(), e.getCause());
        }
        return attributeDescriptions;
    }

    private @Nullable TrainingPattern parseCommaSeperatedDataPattern(String[] commaSeperatedDataPattern) {
        Map<AttributeDescription, Object> pattern = new HashMap<>();

        int index = 0;

        for (AttributeDescription attribute : attributeDescription) {
            String value = commaSeperatedDataPattern[index];

            if (attribute.type() == AttributeType.DISTINCT) {
                if (attribute.allowedAttributeValues().contains(value)) {
                    pattern.put(attribute, value);
                }
            } else if (attribute.type() == AttributeType.CONTINUOUS) {
                try {
                    double minValue = Double.parseDouble(attribute.allowedAttributeValues().get(0).toString());
                    double maxValue = Double.parseDouble(attribute.allowedAttributeValues().get(1).toString());

                    double parsedValue = Double.parseDouble(value);

                    if (parsedValue >= minValue && parsedValue <= maxValue) {
                        pattern.put(attribute, parsedValue);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Failed to parse {} as a number for attribute {}", value, attribute.name(), e);
                }
            }
            index++;
        }

        if (pattern.size() != attributeDescription.size()) {
            logger.warn("Pattern size mismatch: expected {}, found {}", attributeDescription.size(), pattern.size());
            return null;
        }
        return new TrainingPattern(pattern);
    }


    private AttributeDescription parseCommaSeperatedDataAttributes(String[] commaSeperatedData) {
        String attributeName = commaSeperatedData[0];
        AttributeType attributeType = AttributeType.fromString(commaSeperatedData[1]);
        List<Object> allowedAttributeValues = new ArrayList<>();

        if (attributeType == AttributeType.CONTINUOUS) {
            String[] values = commaSeperatedData[2].split("\\.\\.");

            double minValue = Double.parseDouble(values[0]);
            double maxValue = Double.parseDouble(values[1]);

            allowedAttributeValues.add(minValue);
            allowedAttributeValues.add(maxValue);
        } else if (attributeType == AttributeType.DISTINCT) {
            allowedAttributeValues.addAll(Arrays.asList(commaSeperatedData).subList(2, commaSeperatedData.length));
        }
        return new AttributeDescription(attributeName, attributeType, allowedAttributeValues);
    }

    public List<LikelihoodData> getLikelihoodData() {
        return likelihoodData;
    }

    public Set<AttributeDescription> getAttributeDescription() {
        return attributeDescription;
    }

    public Set<PriorData> getPriorClasses() {
        return priorClasses;
    }

    public Map<Object, Double> getPriorProbabilities() {
        return priorProbabilities;
    }
}
