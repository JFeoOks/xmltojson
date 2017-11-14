package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

public class SaxToJsonValueConverter {

    private static JsonNodeFactory factory = JsonNodeFactory.instance;

    private static final List<SaxToJsonValueConverter.FormatAction<String, ValueNode>> converters;

    static {
        converters = new ArrayList<>();
        converters.add(new SaxToJsonValueConverter.FormatAction<String, ValueNode>() {
            @Override
            public ValueNode convert(String text) {
                if (text == null || !isParsable(text)) return null;
                if (text.contains(".")) {
                    int scale = text.length() - (text.indexOf('.') + 1);
                    BigDecimal value = new BigDecimal(text).setScale(scale, BigDecimal.ROUND_CEILING);
                    return factory.numberNode(value);
                } else return factory.numberNode(new BigInteger(text));
            }
        });
        converters.add(new SaxToJsonValueConverter.FormatAction<String, ValueNode>() {
            @Override
            public ValueNode convert(String text) {
                if (text == null || !isBoolean(text)) return null;

                return factory.booleanNode(toBoolean(text));
            }
        });
        converters.add(new SaxToJsonValueConverter.FormatAction<String, ValueNode>() {
            @Override
            public ValueNode convert(String text) {
                return factory.textNode(text);
            }
        });
    }

    private interface FormatAction<T, D> {
        D convert(T t);
    }

    private static boolean isBoolean(String text) {
        return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
    }

    public static ValueNode convert(final String text, boolean isConvertToJsonPrimitives) {
        if (isConvertToJsonPrimitives) {
            for (SaxToJsonValueConverter.FormatAction<String, ValueNode> converter : converters) {
                ValueNode value = converter.convert(text);
                if (value != null) return value;
            }
        }
        return factory.textNode(text);
    }
}
