package dev.andreasgeorgatos.nbc.data;

import java.util.List;

public class Attribute {
    private final String attributeName;
    private final AttributeType type;
    private final List<Object> allowedValues;

    public Attribute(String attributeName, AttributeType type, List<Object> allowedValues) {
        this.attributeName = attributeName;
        this.type = type;
        this.allowedValues = allowedValues;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public AttributeType getType() {
        return type;
    }

    public List<Object> getAllowedValues() {
        return allowedValues;
    }
}
