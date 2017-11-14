package com.netcracker.mediation.dataflow.content.handler;

import org.xml.sax.Attributes;

public class ElementValueHolderFactory {

    private static final String DEFAULT_VALUE_PREFIX = "value";
    private static final String DEFAULT_ATTRIBUTE_PREFIX = "@";

    private boolean isConvertToJsonPrimitives;
    private String valuePrefix = DEFAULT_VALUE_PREFIX;
    private boolean isUsePrefixForAttributes;
    private String attrPrefix = DEFAULT_ATTRIBUTE_PREFIX;


    public ElementValueHolder newInstance(String name, Attributes attributes) {
        return new ElementValueHolder(
                name,
                attributes,
                valuePrefix,
                isConvertToJsonPrimitives,
                isUsePrefixForAttributes,
                attrPrefix
        );
    }

    public ElementValueHolder newRootInstance() {
        return new ElementValueHolder(
                valuePrefix,
                isConvertToJsonPrimitives,
                isUsePrefixForAttributes,
                attrPrefix
        );
    }

    public boolean isConvertToJsonPrimitives() {
        return isConvertToJsonPrimitives;
    }

    public ElementValueHolderFactory setConvertToJsonPrimitives(boolean convertToJsonPrimitives) {
        isConvertToJsonPrimitives = convertToJsonPrimitives;
        return this;
    }

    public String getValuePrefix() {
        return valuePrefix;
    }

    public ElementValueHolderFactory setValuePrefix(String valuePrefix) {
        this.valuePrefix = valuePrefix;
        return this;
    }

    public boolean isUsePrefixForAttributes() {
        return isUsePrefixForAttributes;
    }

    public ElementValueHolderFactory setUsePrefixForAttributes(boolean usePrefixForAttributes) {
        isUsePrefixForAttributes = usePrefixForAttributes;
        return this;
    }

    public String getAttrPrefix() {
        return attrPrefix;

    }

    public ElementValueHolderFactory setAttrPrefix(String attrPrefix) {
        this.attrPrefix = attrPrefix;
        return this;
    }
}
