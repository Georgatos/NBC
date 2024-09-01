package dev.andreasgeorgatos.nbc.data.attributes;

public enum AttributeType {
    DISTINCT("D"),
    CONTINUOUS("R");

    private String type;

    public String getType() {
        return type;
    }

    public static AttributeType fromString(String type) {
        for (AttributeType attribute : AttributeType.values()) {
            if (attribute.getType().equals(type)) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("Invalid attribute type: " + type);
    }

    AttributeType(String type) {
        this.type = type;
    }
}
