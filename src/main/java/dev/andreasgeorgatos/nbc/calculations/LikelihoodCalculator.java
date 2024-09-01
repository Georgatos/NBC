package dev.andreasgeorgatos.nbc.calculations;

import dev.andreasgeorgatos.nbc.data.attributes.AttributeDescription;
import dev.andreasgeorgatos.nbc.data.attributes.AttributeType;
import dev.andreasgeorgatos.nbc.data.attributes.TrainingPattern;
import dev.andreasgeorgatos.nbc.data.likelihood.LikelihoodData;

import java.util.*;

public class LikelihoodCalculator {

    public void printLikelihoodData(List<LikelihoodData> likelihoodData) {
        System.out.printf("%-15s %-15s %-15s %-15s %-10s %-10s %-15s %-15s %-15s%n",
                "Attribute Name", "Attribute Value", "Class Name", "Class Value", "Count", "Total", "Probability", "Mean", "Variance");

        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------");

        for (LikelihoodData data : likelihoodData) {
            System.out.printf("%-15s %-15s %-15s %-15s %-10d %-10d %-15.5f %-15.5f %-15.5f%n",
                    data.getAttributeDescription().name(),
                    data.getAttributeValue(),
                    data.getClassName(),
                    data.getClassValue(),
                    data.getCount(),
                    data.getTotalCount(),
                    data.getConditionalProbability(),
                    data.getMean(),
                    data.getVariance());
        }
    }


    public void calculateConditionalProbability(List<LikelihoodData> likelihoodData) {
        for (LikelihoodData data : likelihoodData) {
            if (data.getAttributeDescription().type() == AttributeType.DISTINCT) {
                data.setConditionalProbability((double) data.getCount() / data.getTotalCount());
            }
        }
    }


    public List<LikelihoodData> calculateLikelihood(List<TrainingPattern> trainingPatterns, Set<AttributeDescription> attributeDescriptions, Object className) {
        List<LikelihoodData> list = new ArrayList<>();
        for (TrainingPattern trainingPattern : trainingPatterns) {
            for (Map.Entry<AttributeDescription, Object> entry : trainingPattern.getPattern().entrySet()) {
                if (entry.getKey().type() == AttributeType.DISTINCT) {
                    if (doesDataExistDiscrete(list, entry.getKey(), entry.getValue(), trainingPattern.getClassName(), trainingPattern.getClassValue())) {
                        getLikelihoodDataDiscrete(list, entry.getKey(), entry.getValue(), trainingPattern.getClassName(), trainingPattern.getClassValue()).increaseCount();
                    } else {
                        list.add(new LikelihoodData(entry.getKey(), entry.getValue(), trainingPattern.getClassName(), trainingPattern.getClassValue()));
                        getLikelihoodDataDiscrete(list, entry.getKey(), entry.getValue(), trainingPattern.getClassName(), trainingPattern.getClassValue()).increaseCount();
                    }
                } else if (entry.getKey().type() == AttributeType.CONTINUOUS) {
                    LikelihoodData existingData = getContinuesLikelihoodData(list, entry.getKey(), className, trainingPattern.getClassValue());
                    if (existingData != null) {
                        existingData.increaseCount();
                        existingData.increaseSum((double) entry.getValue());
                    } else {
                        LikelihoodData newData = new LikelihoodData(entry.getKey(), null, className, trainingPattern.getClassValue());
                        newData.increaseCount();
                        newData.increaseSum((double) entry.getValue());
                        list.add(newData);
                    }
                }
            }
        }

//        if (needsSmoothing(list, attributeDescriptions)) {
//            applyLaplaceSmoothing(list, attributeDescriptions, className);
//
//            for (LikelihoodData data : list) {
//                long totalCount = trainingPatterns.stream()
//                        .filter(tp -> tp.getClassName().equals(data.getClassName()) && tp.getClassValue().equals(data.getClassValue()))
//                        .count();
//
//                long distinctAttributeValueCount = attributeDescriptions.stream()
//                        .filter(ad -> ad.equals(data.getAttributeDescription()))
//                        .flatMap(ad -> ad.allowedAttributeValues().stream())
//                        .distinct()
//                        .count();
//
//                data.setTotalCount(totalCount + distinctAttributeValueCount);
//            }
//        } else {
            for (LikelihoodData data : list) {
                long totalCount = trainingPatterns.stream()
                        .filter(tp -> tp.getClassName().equals(data.getClassName()) && tp.getClassValue().equals(data.getClassValue()))
                        .count();

                data.setTotalCount(totalCount);
//            }
        }

        return list;
    }


    private void applyLaplaceSmoothing(List<LikelihoodData> list, Set<AttributeDescription> attributeDescriptions, Object className) {
        Set<Object> distinctClassValues = new HashSet<>();

        for (LikelihoodData data : list) {
            distinctClassValues.add(data.getClassValue());
        }
        for (Object classValue : distinctClassValues) {
            for (AttributeDescription attributeDescription : attributeDescriptions) {

                if (attributeDescription.type() == AttributeType.CONTINUOUS) {
                    continue;
                }

                for (Object allowedValue : attributeDescription.allowedAttributeValues()) {
                    if (doesDataExistDiscrete(list, attributeDescription, allowedValue, className, classValue)) {
                        getLikelihoodDataDiscrete(list, attributeDescription, allowedValue, className, classValue).increaseCount();
                    } else {
                        list.add(new LikelihoodData(attributeDescription, allowedValue, className, classValue));
                    }
                }
            }
        }
    }

    private boolean needsSmoothing(List<LikelihoodData> list, Set<AttributeDescription> attributeDescriptions) {
        Set<Object> distinctClassValues = new HashSet<>();

        for (LikelihoodData data : list) {
            distinctClassValues.add(data.getClassValue());
        }

        for (Object classValue : distinctClassValues) {
            for (AttributeDescription attributeDescription : attributeDescriptions) {
                for (Object allowedValue : attributeDescription.allowedAttributeValues()) {
                    boolean exists = false;
                    for (LikelihoodData data : list) {
                        if (data.getAttributeDescription().equals(attributeDescription) &&
                                data.getClassValue().equals(classValue) &&
                                data.getAttributeValue().equals(allowedValue)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private LikelihoodData getContinuesLikelihoodData(List<LikelihoodData> list, AttributeDescription attributeDescriptions, Object className, Object classValue) {
        for (LikelihoodData data : list) {
            if (data.getAttributeDescription().equals(attributeDescriptions) && data.getClassName().equals(className) && data.getClassValue().equals(classValue)) {
                return data;
            }
        }
        return null;
    }

    private LikelihoodData getLikelihoodDataDiscrete(List<LikelihoodData> list, AttributeDescription
            attributeDescription, Object attributeValue, Object className, Object classValue) {
        return list.stream()
                .filter(likelihoodData ->
                        likelihoodData.getAttributeDescription().equals(attributeDescription) &&
                                likelihoodData.getAttributeValue().equals(attributeValue) &&
                                likelihoodData.getClassName().equals(className) &&
                                likelihoodData.getClassValue().equals(classValue)
                )
                .findFirst()
                .orElse(null);
    }

    private boolean doesDataExistDiscrete(List<LikelihoodData> list, AttributeDescription
            attributeDescription, Object attributeValue, Object className, Object classValue) {
        return list.stream()
                .anyMatch(likelihoodData ->
                        likelihoodData.getAttributeDescription().equals(attributeDescription) &&
                                likelihoodData.getAttributeValue().equals(attributeValue) &&
                                likelihoodData.getClassName().equals(className) &&
                                likelihoodData.getClassValue().equals(classValue)
                );
    }
}
