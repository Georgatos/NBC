package dev.andreasgeorgatos.nbc.data.likelihood;

import dev.andreasgeorgatos.nbc.data.attributes.AttributeDescription;

public class LikelihoodData {
    private final AttributeDescription attributeDescription;
    private final Object attributeValue;
    private final Object className;
    private final Object classValue;

    private long count;
    private long totalCount;

    private double conditionalProbability;

    private double sum;
    private double sumSquare;
    private double mean;

    private double variance;

    public LikelihoodData(AttributeDescription attributeDescription, Object attributeValue, Object className, Object classValue) {
        this.attributeDescription = attributeDescription;
        this.attributeValue = attributeValue;
        this.className = className;
        this.classValue = classValue;
        this.count = 0;
        this.conditionalProbability = 0.0;
        this.sum = 0.0;
        this.sumSquare = 0.0;
        this.mean = 0.0;
        this.variance = 0.0;
    }

    public double getVariance() {
        return variance;
    }

    public void calculateVariance() {
        if (count > 1) {
            double meanSquare = mean * mean;
            variance = (sumSquare / count) - meanSquare;
            variance *= count / (count - 1.0);
        } else {
            variance = 0.0;
        }
    }


    public double getMean() {
        return mean;
    }

    public void calculateMean() {
        if (count > 0) {
            mean = sum / count;
        } else {
            mean = 0.0;
        }
    }

    public void increaseSum(double value) {
        sum += value;
        sumSquare += value * value;
        calculateMean();
        calculateVariance();

    }

    public double getConditionalProbability() {
        return conditionalProbability;
    }

    public void setConditionalProbability(double conditionalProbability) {
        this.conditionalProbability = conditionalProbability;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void increaseCount() {
        count++;
    }

    public AttributeDescription getAttributeDescription() {
        return attributeDescription;
    }


    public Object getAttributeValue() {
        return attributeValue;
    }

    public Object getClassName() {
        return className;
    }

    public Object getClassValue() {
        return classValue;
    }

    public long getCount() {
        return count;
    }
}
