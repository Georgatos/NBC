package dev.andreasgeorgatos.nbc.data;

import java.util.Map;

public class TrainingPatterns {
    private Attribute attribute;
    private Object value;
    private Map<Object, Object> result;

    public TrainingPatterns(Attribute attribute, Object value, Map<Object, Object> result) {
        this.attribute = attribute;
        this.value = value;
        this.result = result;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Object getValue() {
        return value;
    }

    public Map<Object, Object> getResult() {
        return result;
    }
}
