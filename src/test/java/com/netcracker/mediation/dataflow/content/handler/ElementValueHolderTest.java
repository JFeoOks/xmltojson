package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.junit.Test;
import org.xml.sax.Attributes;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ElementValueHolderTest extends AbstractHolderTest {

    JsonNodeFactory factory = JsonNodeFactory.instance;

    @Test
    public void buildRootNode() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "text",
                false,
                false,
                "@"
        );

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(0 ,result.size());
        assertEquals(JsonNodeFactory.instance.objectNode(), result);
    }

    @Test
    public void buildRootNodeWithChild() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "text",
                false,
                false,
                "@"
        );

        NumericNode valueNode = factory.numberNode(1);
        elementValueHolder.getChildren().put(valueNode, "value");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(1 ,result.size());
        assertEquals(valueNode, result.get("value"));
    }

    @Test
    public void buildRootNodeWithChildren() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "text",
                false,
                false,
                "@"
        );

        NumericNode intValue = factory.numberNode(1);
        TextNode strValue = factory.textNode("hello");
        BooleanNode boolValue = factory.booleanNode(true);

        elementValueHolder.getChildren().put(intValue, "value");
        elementValueHolder.getChildren().put(strValue, "text node");
        elementValueHolder.getChildren().put(boolValue, "boolean");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(3 ,result.size());
        assertEquals(intValue, result.get("value"));
        assertEquals(strValue, result.get("text node"));
        assertEquals(boolValue, result.get("boolean"));
    }

    @Test
    public void buildNodeWithChildrenWithConvertion() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "text",
                true,
                false,
                "@"
        );

        NumericNode intValue = factory.numberNode(1);
        TextNode strValue = factory.textNode("hello");
        BooleanNode boolValue = factory.booleanNode(true);

        elementValueHolder.getChildren().put(intValue, "value");
        elementValueHolder.getChildren().put(strValue, "text node");
        elementValueHolder.getChildren().put(boolValue, "boolean");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(6 ,result.size());
        assertEquals(intValue, result.get("value"));
        assertEquals(strValue, result.get("text node"));
        assertEquals(boolValue, result.get("boolean"));

        assertEquals(1, result.get("int").intValue());
        assertEquals(true, result.get("bool").booleanValue());
        assertEquals("hello", result.get("str").textValue());
    }

    @Test
    public void buildNodeWithChildren() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "text",
                false,
                false,
                "@"
        );

        TextNode intValue = factory.textNode("1");
        TextNode strValue = factory.textNode("hello");
        TextNode boolValue = factory.textNode("true");

        elementValueHolder.getChildren().put(intValue, "value");
        elementValueHolder.getChildren().put(strValue, "text node");
        elementValueHolder.getChildren().put(boolValue, "boolean");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(6 ,result.size());
        assertEquals(intValue, result.get("value"));
        assertEquals(strValue, result.get("text node"));
        assertEquals(boolValue, result.get("boolean"));

        assertEquals("1", result.get("int").textValue());
        assertEquals("true", result.get("bool").textValue());
        assertEquals("hello", result.get("str").textValue());
    }

    @Test
    public void buildNodeWithSameNamesChildren() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "text",
                false,
                false,
                "@"
        );

        TextNode strValue1 = factory.textNode("1");
        TextNode strValue2 = factory.textNode("2");
        TextNode strValue3 = factory.textNode("3");

        elementValueHolder.getChildren().put(strValue1, "int");
        elementValueHolder.getChildren().put(strValue2, "int");
        elementValueHolder.getChildren().put(strValue3, "int");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(3, result.size());
        assertTrue(result.get("int").isArray());

        assertEquals("1", result.get("int").get(0).textValue());
        assertEquals("2", result.get("int").get(1).textValue());
        assertEquals("3", result.get("int").get(2).textValue());
        assertEquals("1", result.get("int").get(3).textValue());

        assertEquals("true", result.get("bool").textValue());
        assertEquals("hello", result.get("str").textValue());
    }

    @Test
    public void buildNodeWithSameNamesChildrenWhereNodeCurrentNodeWithoutAttributesWithValueWithConvertion() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                mock(Attributes.class),
                "text",
                true,
                false,
                "@"
        );

        elementValueHolder.setValue("hello");
        elementValueHolder.setValue(" ");
        elementValueHolder.setValue("my friend");

        NumericNode intValue = factory.numberNode(1);
        TextNode strValue = factory.textNode("hello");
        BooleanNode boolValue = factory.booleanNode(true);

        elementValueHolder.getChildren().put(intValue, "value");
        elementValueHolder.getChildren().put(strValue, "text node");
        elementValueHolder.getChildren().put(boolValue, "boolean");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(4 ,result.size());
        assertEquals(intValue, result.get("value"));
        assertEquals(strValue, result.get("text node"));
        assertEquals(boolValue, result.get("boolean"));

        assertEquals("hello my friend", result.get("text").textValue());
    }

    @Test
    public void whenValueIsEmptyThenNullNode() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                mock(Attributes.class),
                "text",
                true,
                false,
                "@"
        );

        elementValueHolder.setValue("\r\n           ");

        JsonNode result = elementValueHolder.buildNode();

        assertTrue(result.isNull());
    }

    @Test
    public void whenUsingPrefixForAttributesTrueThenAddPrefix() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "text",
                false,
                true,
                "@"
        );

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(3, result.size());
        assertEquals("1", result.get("@int").textValue());
        assertEquals("true", result.get("@bool").textValue());
        assertEquals("hello", result.get("@str").textValue());
    }

    @Test
    public void checkBooleanTrueValue() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "value",
                true,
                false,
                "@"
        );

        elementValueHolder.setValue(" true ");

        JsonNode result = elementValueHolder.buildNode();

        assertTrue(result.get("value").booleanValue());
    }

    @Test
    public void checkBooleanFalseValue() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "value",
                true,
                false,
                "@"
        );

        elementValueHolder.setValue(" false ");

        JsonNode result = elementValueHolder.buildNode();

        assertFalse(result.get("value").booleanValue());

    }

    @Test
    public void checkIntegerValue() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "value",
                true,
                false,
                "@"
        );

        elementValueHolder.setValue(" 12345 ");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(BigInteger.valueOf(12345L), result.get("value").bigIntegerValue());
    }

    @Test
    public void checkDecimalValue() throws Exception {
        ElementValueHolder elementValueHolder = new ElementValueHolder(
                "element",
                getMockedAttributes(),
                "value",
                true,
                false,
                "@"
        );

        elementValueHolder.setValue(" 12345.6 ");

        JsonNode result = elementValueHolder.buildNode();

        assertEquals(BigDecimal.valueOf(12345.6), result.get("value").decimalValue());
    }
}
