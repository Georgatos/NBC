package dev.andreasgeorgatos.nbc.data.attributes;

import java.util.List;

public record AttributeDescription(String name, AttributeType type, List<Object> allowedAttributeValues) {
}
