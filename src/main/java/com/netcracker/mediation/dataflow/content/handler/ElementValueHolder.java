package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

import java.util.Map;

public class ElementValueHolder {

    private JsonNodeFactory factory = JsonNodeFactory.instance;

    private String name;
    private StringBuilder valueBuilder = new StringBuilder();
    private Attributes attributes;
    private LinkedListMultimap<JsonNode, String> children = LinkedListMultimap.create();
    private boolean isRoot = false;

    private final boolean isConvertToJsonPrimitives;
    private final String valuePrefix;
    private final boolean isUsePrefixForAttributes;
    private final String attrPrefix;

    public ElementValueHolder(
            String valuePrefix,
            boolean isConvertToJsonPrimitives,
            boolean isUsePrefixForAttributes,
            String attrPrefix
    ) {
        this.isRoot = true;
        this.isConvertToJsonPrimitives = isConvertToJsonPrimitives;
        this.valuePrefix = valuePrefix;
        this.isUsePrefixForAttributes = isUsePrefixForAttributes;
        this.attrPrefix = attrPrefix;
    }

    public ElementValueHolder(
            String name,
            Attributes attributes,
            String valuePrefix,
            boolean isConvertToJsonPrimitives,
            boolean isUsePrefixForAttributes,
            String attrPrefix
            ) {
        this.name = name;
        this.attributes = attributes;
        this.isConvertToJsonPrimitives = isConvertToJsonPrimitives;
        this.valuePrefix = valuePrefix;
        this.isUsePrefixForAttributes = isUsePrefixForAttributes;
        this.attrPrefix = attrPrefix;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return valueBuilder.toString().trim();
    }

    public void setValue(String value) {
        valueBuilder.append(value);
    }

    public Multimap<JsonNode, String> getChildren() {
        return children;
    }

    public JsonNode buildNode() {
        if (isRoot) {
            return buildObjectNode();
        } else if (attributes.getLength() > 0 || !children.isEmpty()) {
            ObjectNode node = buildObjectNode();
            if (!StringUtils.isBlank(getValue())) node.set(valuePrefix, SaxToJsonValueConverter.convert(getValue(), isConvertToJsonPrimitives));

            for (int index = 0; index < attributes.getLength(); index++) {
                String childName = attributes.getQName(index);
                if (isUsePrefixForAttributes) childName = attrPrefix + childName;

                ValueNode childNode = SaxToJsonValueConverter.convert(attributes.getValue(index), isConvertToJsonPrimitives);
                addToNode(node, childName, childNode);
            }

            return node;
        } else {
            String value = getValue();
            return StringUtils.isEmpty(value) ? factory.nullNode() : SaxToJsonValueConverter.convert(value, isConvertToJsonPrimitives);
        }
    }

    private ObjectNode buildObjectNode() {
        ObjectNode node = factory.objectNode();

        for (Map.Entry<JsonNode, String> child : children.entries()) {
            String childName = child.getValue();
            JsonNode childNode = child.getKey();

            addToNode(node, childName, childNode);
        }

        return node;
    }

    private void addToNode(final ObjectNode node, final String childName, final JsonNode childNode) {
        JsonNode sameNode = node.get(childName);

        if (sameNode == null) node.set(childName, childNode);
        else {
            if (sameNode.isArray()) {
                ((ArrayNode) sameNode).add(childNode);
                return;
            }

            node.remove(childName);

            ArrayNode array = factory.arrayNode().add(sameNode).add(childNode);
            node.set(childName, array);
        }
    }
}
