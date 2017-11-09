package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.*;

public class SaxJsonContentHandler extends DefaultHandler {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);
    private static final String DEFAULT_VALUE_PREFIX = "text";
    private static final String DEFAULT_ATTRIBUTE_PREFIX = "@";

    private ObjectNode root = factory.objectNode();
    private Deque<ObjectNode> current = new ArrayDeque<>();

    {
        current.offer(root);
    }

    private ValueHolder rawValue;

    private boolean isConvertToJsonPrimitives = true;
    private String valuePrefix = DEFAULT_VALUE_PREFIX;
    private boolean isUsePrefixForAttributes = false;
    private String attrPrefix = DEFAULT_ATTRIBUTE_PREFIX;

    private static final List<FormatAction<String, ValueNode>> formatters;

    static {
        formatters = new ArrayList<>();
        formatters.add(new FormatAction<String, ValueNode>() {
            @Override
            public ValueNode format(String text) {
                if (text == null || !isParsable(text)) return null;
                if (text.contains(".")) {
                    int scale = text.length() - (text.indexOf('.') + 1);
                    BigDecimal value = new BigDecimal(text).setScale(scale, BigDecimal.ROUND_CEILING);
                    return factory.numberNode(value);
                } else return factory.numberNode(new BigInteger(text));
            }
        });
        formatters.add(new FormatAction<String, ValueNode>() {
            @Override
            public ValueNode format(String text) {
                if (text == null || !isBoolean(text)) return null;

                return factory.booleanNode(toBoolean(text));
            }
        });
        formatters.add(new FormatAction<String, ValueNode>() {
            @Override
            public ValueNode format(String text) {
                return factory.textNode(text);
            }
        });
    }

    private interface FormatAction<T, D> {
        D format(T t);
    }

    private static boolean isBoolean(String text) {
        return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
    }

    public SaxJsonContentHandler(boolean isConvertToJsonPrimitives) {
        this.isConvertToJsonPrimitives = isConvertToJsonPrimitives;
    }

    public SaxJsonContentHandler(String valuePrefix, boolean isConvertToJsonPrimitives, boolean isUsePrefixForAttributes, String attrPrefix) {
        this.valuePrefix = valuePrefix;
        this.isConvertToJsonPrimitives = isConvertToJsonPrimitives;
        this.isUsePrefixForAttributes = isUsePrefixForAttributes;
        this.attrPrefix = attrPrefix;
    }

    /*
    Открытие элемента.
        Есть ли сохраненные данные:
            да  ->   Является ли объемлющая нода ObjectNode'ой:
                        да ->   Создаем ObjectNode;
                                Есть ли элементы с таким же именем в ноде:
                                    да  ->
                                            Она массив:
                                                да  ->  добавляем элемент
                                                нет ->  Создаем массив;
                                                        Удаляем из ноды ноду с таким же именем и кладем в массив;
                                                        Добавляем массив;
                                    нет -> Добавляем
                        нет ->  Ломаемся (пришёл смешанный контент)
            нет ->  ничего не делаем
        Является ли объемлющая нода ObjectNode'ой:
            да  ->  Берем последний элемент из очереди;
                    Есть ли настоящего у элемента атрибуты:
                        да  ->   Создаем ObjectNode;
                                 Есть ли элементы с таким же именем в ноде:
                                     да  -> Создаем массив;
                                            Удаляем из ноды ноду с таким же именем и кладем в массив;
                                            Добавляем массив;
                                     нет -> Добавляем
                        нет -> Сохраняем входные данные, пока не получим значение
            нет -> Ломаемся (пришёл смешанный контент)
     */

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        ObjectNode lastNode = current.peekLast();

        if (rawValue != null) {
            lastNode = writeObjectNode(lastNode, rawValue.qName, factory.objectNode());
            rawValue = null;
        }

        if (attributes.getLength() > 0) {
            ObjectNode node = createObjectNode(attributes);
            writeObjectNode(lastNode, qName, node);
        } else {
            rawValue = new ValueHolder(qName);
        }
    }

    private ObjectNode writeObjectNode(final ObjectNode lastNode, final String name, ObjectNode node) {
        checkLastNode(lastNode);

        JsonNode sameNode = lastNode.get(name);
        if (sameNode != null) addToTheSameNode(sameNode, lastNode, name, node);
        else lastNode.set(name, node);

        current.offerLast(node);
        return node;
    }

    private void addToTheSameNode(final JsonNode sameNode, final ObjectNode lastNode, String name, final JsonNode node) {
        if (sameNode.isArray()) {
            ((ArrayNode) sameNode).add(node);
        } else {
            lastNode.remove(name);

            ArrayNode array = lastNode.putArray(name);
            array
                    .add(sameNode)
                    .add(node);

        }
    }

    private void checkLastNode(ObjectNode lastNode) {
        if (!lastNode.isObject())
            throw new IllegalArgumentException("Mixed content is not supported '" + rawValue.qName + "'");
    }

    private ObjectNode createObjectNode(final Attributes attributes) {
        ObjectNode jsonNodes = factory.objectNode();

        writeAttributes(jsonNodes, attributes);
        return jsonNodes;
    }

    private void writeAttributes(final ObjectNode node, final Attributes attributes) {
        for (int index = 0; index < attributes.getLength(); index++) {
            String name = attributes.getQName(index);
            node.put(
                    isUsePrefixForAttributes ? attrPrefix + name : name,
                    convertValue(attributes.getValue(index))
            );
        }
    }

    /*
    Закрытие элемента.
        Удаляем из очереди.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (rawValue == null) current.pollLast();
        else rawValue = null;
    }

    /*
    Запись значения.
        Есть сохраненный элемент:
            да  -> записываем как сконвертированное значение
            нет -> игнорируем (пришёл mixed контент)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String text = new String(ch, start, length);
        boolean isBlank = isBlank(text);

        ObjectNode lastNode = current.peekLast();

        if (rawValue == null) {
            if (!isBlank) {
                checkLastNode(lastNode);
                lastNode.set(valuePrefix, convertValue(text));
            }
            return;
        }

        if (isBlank) {
            writeObjectNode(lastNode, rawValue.qName, factory.objectNode());
            rawValue = null;
        } else {
            checkLastNode(lastNode);
            JsonNode sameNode = lastNode.get(rawValue.qName);

            if (sameNode != null) addToTheSameNode(sameNode, lastNode, rawValue.qName, convertValue(text));
            else lastNode.set(rawValue.qName, convertValue(text));
        }
    }

    private ValueNode convertValue(final String text) {
        if (isConvertToJsonPrimitives) {
            for (FormatAction<String, ValueNode> formatter : formatters) {
                ValueNode value = formatter.format(text);
                if (value != null) return value;
            }
        }
        return factory.textNode(text);
    }

    @Override
    public String toString() {
        return root.toString();
    }

    private static class ValueHolder {

        private String qName;

        public ValueHolder(String qName) {
            this.qName = qName;
        }
    }

    public SaxJsonContentHandler setConvertToJsonPrimitives(boolean convertToJsonPrimitives) {
        isConvertToJsonPrimitives = convertToJsonPrimitives;
        return this;
    }

    public SaxJsonContentHandler setValuePrefix(String valuePrefix) {
        this.valuePrefix = valuePrefix;
        return this;
    }

    public SaxJsonContentHandler setUsePrefixForAttributes(boolean usePrefixForAttributes) {
        isUsePrefixForAttributes = usePrefixForAttributes;
        return this;
    }

    public SaxJsonContentHandler setAttrPrefix(String attrPrefix) {
        this.attrPrefix = attrPrefix;
        return this;
    }
}
