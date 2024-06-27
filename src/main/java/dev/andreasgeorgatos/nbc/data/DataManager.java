package dev.andreasgeorgatos.nbc.data;

import dev.andreasgeorgatos.nbc.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class DataManager {
    private final Logger logger = LoggerFactory.getLogger(DataManager.class);

    private final List<Attribute> attributes;
    private final List<TrainingPatterns> patterns;

    public DataManager(FileManager fileManager) {
        patterns = new ArrayList<>();
        attributes = new ArrayList<>();
        int lastLine = loadAttributes(fileManager.getTrainingData());
        loadTrainingPatterns(fileManager.getTrainingData(), lastLine);
    }

    private void loadTrainingPatterns(File trainingDataFile, int index) {
        try (BufferedReader br = new BufferedReader(new FileReader(trainingDataFile))) {

            for (int i = 0; i <= index; i++) {
                if (br.readLine() == null) {
                    logger.error("File does not have {} lines, returning.", index);
                    return;
                }
            }

            String[] header = br.readLine().split(",");
            String lookingFor = header[header.length - 1];


            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                Map<Object, Object> results = new HashMap<>();

                results.put(lookingFor, parseData(parts[parts.length - 1]));

                for (int i = 0; i < parts.length - 1; i++) {
                    if (attributes.get(i).getAllowedValues().contains(parseData(parts[i]))) {
                        patterns.add(new TrainingPatterns(attributes.get(i), parts[i], results));
                    } else {
                        logger.warn("Value mismatch: we are here with: {} for the attribute: {} allowed values: {}", parts[i], attributes.get(i).getAttributeName(), attributes.get(i).getAllowedValues());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Something went wrong with the file {}", trainingDataFile.getName());
        }
    }


    private int loadAttributes(File trainingDataFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(trainingDataFile))) {

            String line;

            int index = 0;

            while (!((line = br.readLine()).isEmpty())) {
                String[] data = line.split(",");

                String attributeName = data[0];

                AttributeType attributeType = AttributeType.fromString(data[1]);
                List<Object> values = new ArrayList<>();

                if (attributeType == AttributeType.DISTINCT) {
                    for (int i = 2; i < data.length; i++) {
                        values.add(parseData(data[i]));
                        logger.info("Adding attribute: '{}' to the '{}' new value list: {}", data[i], attributeName ,values);
                    }
                } else if (attributeType == AttributeType.CONTINUOUS) {
                    String[] continuousValues = data[2].split("\\.\\.");

                    if (parseData(continuousValues[0]) instanceof Integer || parseData(continuousValues[1]) instanceof Integer) {
                        int minValue = (int) parseData(continuousValues[0]);
                        int maxValue = (int) parseData(continuousValues[1]);
                        addValuesInteger(minValue, maxValue, values);
                    } else {
                        double minValue = (double) parseData(continuousValues[0]);
                        double maxValue = (double) parseData(continuousValues[1]);
                        addValuesDouble(minValue, maxValue, values);
                    }
                    logger.info("Loaded attribute: '{}' with allowed values: {}", attributeName, values);
                }

                logger.info("Loaded attribute: '{}' with values: {}", attributeName, values);
                attributes.add(new Attribute(attributeName, attributeType, values));

                index++;
            }

            return index;

        } catch (IOException e) {
            logger.error("Something went wrong with the file {}", trainingDataFile.getName());
        }
        return -1;
    }

    private void addValuesInteger(int minValue, int maxValue, List<Object> values) {
        while (minValue <= maxValue) {
            values.add(minValue);
            minValue++;
        }
    }

    private void addValuesDouble(double minValue, double maxValue, List<Object> values) {
        while (minValue <= maxValue) {
            values.add(minValue);
            minValue += 0.1;
        }
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

    public Set<Object> getAllPossibleResults() {
        Set<Object> results = new HashSet<>();
        for (TrainingPatterns pattern : patterns) {
            results.addAll(pattern.getResult().values());
        }
        return results;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<TrainingPatterns> getPatterns() {
        return patterns;
    }
}
