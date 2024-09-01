package dev.andreasgeorgatos.nbc.data.attributes;

import java.util.Map;

public class TrainingPattern {
    private final Map<AttributeDescription, Object> pattern;
    private Object className;
    private Object classValue;

    public TrainingPattern(Map<AttributeDescription, Object> pattern) {
        this.pattern = pattern;
    }

    public Map<AttributeDescription, Object> getPattern() {
        return pattern;
    }

    public Object getClassName() {
        return className;
    }

    public void setClassName(Object className) {
        this.className = className;
    }

    public Object getClassValue() {
        return classValue;
    }

    public void setClassValue(Object classValue) {
        this.classValue = classValue;
    }
}
