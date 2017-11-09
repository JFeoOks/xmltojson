package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Deque;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SaxJsonContentHandlerTest {

    private SaxJsonContentHandler handler = new SaxJsonContentHandler(true);

    private Attributes getMockedAttributes() {
        Attributes mock = mock(Attributes.class);
        when(mock.getLength()).thenReturn(3);
        when(mock.getQName(0)).thenReturn("game");
        when(mock.getQName(1)).thenReturn("set");
        when(mock.getQName(2)).thenReturn("match");

        when(mock.getValue(0)).thenReturn("1");
        when(mock.getValue(1)).thenReturn("true");
        when(mock.getValue(2)).thenReturn("hello");

        return mock;
    }

    private Deque<ObjectNode> getCurrentField(Object handler) throws NoSuchFieldException, IllegalAccessException {
        Field field = SaxJsonContentHandler.class.getDeclaredField("current");
        field.setAccessible(true);
        return (Deque<ObjectNode>) field.get(handler);
    }

    @Test
    public void addNewNodeThatHasAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_param", getMockedAttributes());

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals(currentField.size(), 2);

        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();
        ObjectNode currentNode = objectNodeIterator.next();

        assertFalse(currentNode.isArray());

        assertEquals(currentNode.get("game").bigIntegerValue(), BigInteger.ONE);
        assertEquals(currentNode.get("set").booleanValue(), true);
        assertEquals(currentNode.get("match").textValue(), "hello");

        ObjectNode parent = objectNodeIterator.next();

        assertTrue(parent.get("test_param").equals(currentNode));
    }

    @Test
    public void addMultipleNodesToRootThatHasAttributesWithTransformingToArray() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_param", getMockedAttributes());
        handler.endElement(null, null, "test_param");
        handler.startElement(null, null, "test_param", getMockedAttributes());

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals(currentField.size(), 2);

        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();
        ObjectNode currentNode = objectNodeIterator.next();

        assertEquals(currentNode.get("game").bigIntegerValue(), BigInteger.ONE);
        assertEquals(currentNode.get("set").booleanValue(), true);
        assertEquals(currentNode.get("match").textValue(), "hello");

        ObjectNode parent = objectNodeIterator.next();

        assertTrue(parent.get("test_param").isArray());
        assertTrue(parent.get("test_param").get(0).equals(currentNode));
    }

    @Test
    public void addToExistingArrayElementWithAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_array", getMockedAttributes());
        handler.endElement(null, null, "test_array");
        handler.startElement(null, null, "test_array", getMockedAttributes());
        handler.endElement(null, null, "test_array");
        handler.startElement(null, null, "test_array", getMockedAttributes());
        handler.endElement(null, null, "test_array");

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals((currentField.peekLast().get("test_array")).size(), 3);
    }

    @Test
    public void addToExistingArrayElementWithOutAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_array", mock(Attributes.class));
        handler.characters("first_record".toCharArray(), 0, "first_record".length());
        handler.endElement(null, null, "test_array");

        handler.startElement(null, null, "test_array", mock(Attributes.class));
        handler.characters("second_record".toCharArray(), 0, "second_record".length());
        handler.endElement(null, null, "test_array");

        handler.startElement(null, null, "test_array", mock(Attributes.class));
        handler.characters("third_record".toCharArray(), 0, "third_record".length());
        handler.endElement(null, null, "test_array");

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals((currentField.peekLast().get("test_array")).size(), 3);
    }

    @Test
    public void addNodeWithoutAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_param", mock(Attributes.class));

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals(currentField.size(), 1);
    }

    @Test
    public void removeFromQueueWhenEndElementWithAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_param", getMockedAttributes());

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals(currentField.size(), 2);

        handler.endElement(null, null, "test_param");

        assertEquals(currentField.size(), 1);
    }

    @Test
    public void removeFromQueueWhenEndElementWithoutAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_param", mock(Attributes.class));

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals(currentField.size(), 1);

        handler.endElement(null, null, "test_param");

        assertEquals(currentField.size(), 1);
    }

    @Test
    public void addElementToQueueWhenCharacters() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_char_param", mock(Attributes.class));

        Deque<ObjectNode> currentField = getCurrentField(handler);

        assertEquals(currentField.size(), 1);

        handler.characters("hello".toCharArray(), 0, 5);

        Iterator<ObjectNode> objectNodeIterator = currentField.iterator();
        ObjectNode parent = objectNodeIterator.next();

        assertEquals(parent.get("test_char_param").textValue(), "hello");
    }

    @Test
    public void checkBooleanTrueValue() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_boolean", mock(Attributes.class));
        handler.characters("true".toCharArray(), 0, 4);

        Deque<ObjectNode> currentField = getCurrentField(handler);
        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();

        ObjectNode parent = objectNodeIterator.next();

        assertTrue(parent.get("test_boolean").isBoolean());
    }

    @Test
    public void checkBooleanFalseValue() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_boolean", mock(Attributes.class));
        handler.characters("false".toCharArray(), 0, 5);

        Deque<ObjectNode> currentField = getCurrentField(handler);
        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();

        ObjectNode parent = objectNodeIterator.next();

        assertTrue(parent.get("test_boolean").isBoolean());
    }

    @Test
    public void checkIntegerValue() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_integer", mock(Attributes.class));
        handler.characters("12345".toCharArray(), 0, 5);

        Deque<ObjectNode> currentField = getCurrentField(handler);
        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();

        ObjectNode parent = objectNodeIterator.next();

        assertTrue(parent.get("test_integer").isBigInteger());
    }

    @Test
    public void checkDecimalValue() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "test_decimal", mock(Attributes.class));
        handler.characters("12345.6".toCharArray(), 0, 7);

        Deque<ObjectNode> currentField = getCurrentField(handler);
        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();

        ObjectNode parent = objectNodeIterator.next();

        assertTrue(parent.get("test_decimal").isBigDecimal());
    }

    @Test
    public void testOneNodeExample() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/oneNode.xml");

        Field root = SaxJsonContentHandler.class.getDeclaredField("root");
        root.setAccessible(true);


        String result = root.get(handler).toString();

        JSONAssert.assertEquals(
                result,
                "{\"note\":{\"to\":\"Tove\",\"from\":\"Jani\",\"heading\":\"Reminder\",\"body\":\"Don't forget me this weekend!\"}}",
                JSONCompareMode.LENIENT);
    }

    @Test
    public void checkArrayCdWithoutNumberConvertion() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        SaxJsonContentHandler handler = new SaxJsonContentHandler(false);
        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/arrayCd.xml");

        Field root = SaxJsonContentHandler.class.getDeclaredField("root");
        root.setAccessible(true);


        String result = root.get(handler).toString();

        JSONAssert.assertEquals(
                result,
                "{\"CATALOG\":{\"CD\":[{\"TITLE\":\"Empire Burlesque\",\"ARTIST\":\"Bob Dylan\",\"COUNTRY\":\"USA\",\"COMPANY\":\"Columbia\",\"PRICE\":\"10.90\",\"YEAR\":\"1985\"},{\"TITLE\":\"Hide your heart\",\"ARTIST\":\"Bonnie Tyler\",\"COUNTRY\":\"UK\",\"COMPANY\":\"CBS Records\",\"PRICE\":\"9.90\",\"YEAR\":\"1988\"},{\"TITLE\":\"Greatest Hits\",\"ARTIST\":\"Dolly Parton\",\"COUNTRY\":\"USA\",\"COMPANY\":\"RCA\",\"PRICE\":\"9.90\",\"YEAR\":\"1982\"},{\"TITLE\":\"Red\",\"ARTIST\":\"The Communards\",\"COUNTRY\":\"UK\",\"COMPANY\":\"London\",\"PRICE\":\"7.80\",\"YEAR\":\"1987\"}]}}",
                JSONCompareMode.LENIENT);
    }

    @Test
    public void checkArrayCdWithNumberConvertion() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/arrayCd.xml");

        Field root = SaxJsonContentHandler.class.getDeclaredField("root");
        root.setAccessible(true);


        String result = root.get(handler).toString();

        JSONAssert.assertEquals(
                result,
                "{\"CATALOG\":{\"CD\":[{\"TITLE\":\"Empire Burlesque\",\"ARTIST\":\"Bob Dylan\",\"COUNTRY\":\"USA\",\"COMPANY\":\"Columbia\",\"PRICE\":10.90,\"YEAR\":1985},{\"TITLE\":\"Hide your heart\",\"ARTIST\":\"Bonnie Tyler\",\"COUNTRY\":\"UK\",\"COMPANY\":\"CBS Records\",\"PRICE\":9.90,\"YEAR\":1988},{\"TITLE\":\"Greatest Hits\",\"ARTIST\":\"Dolly Parton\",\"COUNTRY\":\"USA\",\"COMPANY\":\"RCA\",\"PRICE\":9.90,\"YEAR\":1982},{\"TITLE\":\"Red\",\"ARTIST\":\"The Communards\",\"COUNTRY\":\"UK\",\"COMPANY\":\"London\",\"PRICE\":7.80,\"YEAR\":1987}]}}",
                JSONCompareMode.LENIENT);
    }

    @Test
    public void checkArrayAnagraficaWithoutNumberConvertion() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        SaxJsonContentHandler handler = new SaxJsonContentHandler(false);
        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/arrayAnagrafica.xml");

        Field root = SaxJsonContentHandler.class.getDeclaredField("root");
        root.setAccessible(true);


        String result = root.get(handler).toString();

        JSONAssert.assertEquals(
                result,
                "{\"anagrafica\":{\"testata\":{\"nomemercato\":{\"id\":\"007\",\"text\":\"Mercato di test\"},\"data\":\"Giovedi 18 dicembre 2003 16.05.29\"},\"record\":[{\"codice_cliente\":\"5\",\"rag_soc\":\"Miami American Cafe\",\"codice_fiscale\":\"IT07654930130\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Viale Carlo Espinasse 5, Como\"},\"num_prodotti\":\"13\"},{\"codice_cliente\":\"302\",\"rag_soc\":\"Filiberto Gilardi\",\"codice_fiscale\":\"IT87654770157\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Biancospini 20, Messina\"},\"num_prodotti\":\"8\"},{\"codice_cliente\":\"1302\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT887511231\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Bassini 17/2, Milano\"},\"num_prodotti\":\"18\"},{\"codice_cliente\":\"202\",\"rag_soc\":\"SkillNet\",\"codice_fiscale\":\"IT887642131\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Chiasserini 11A, Milano\"},\"num_prodotti\":\"24\"},{\"codice_cliente\":\"12\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT04835710965\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Via Cignoli 17/2, Roma\"},\"num_prodotti\":\"1112\"},{\"codice_cliente\":\"5\",\"rag_soc\":\"Miami American Cafe\",\"codice_fiscale\":\"IT07654930130\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Viale Carlo Espinasse 5, Como\"},\"num_prodotti\":\"13\"},{\"codice_cliente\":\"302\",\"rag_soc\":\"Filiberto Gilardi\",\"codice_fiscale\":\"IT87654770157\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Biancospini 20, Messina\"},\"num_prodotti\":\"8\"},{\"codice_cliente\":\"1302\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT887511231\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Bassini 17/2, Milano\"},\"num_prodotti\":\"18\"},{\"codice_cliente\":\"202\",\"rag_soc\":\"SkillNet\",\"codice_fiscale\":\"IT887642131\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Chiasserini 11A, Milano\"},\"num_prodotti\":\"24\"},{\"codice_cliente\":\"202\",\"rag_soc\":\"SkillNet\",\"codice_fiscale\":\"IT887642131\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Chiasserini 11A, Milano\"},\"num_prodotti\":\"24\"},{\"codice_cliente\":\"12\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT04835710965\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Via Cignoli 17/2, Roma\"},\"num_prodotti\":\"1112\"}]}}",
                JSONCompareMode.LENIENT);
    }

    @Test
    public void convertedToNumbersAttributesWithSpecifiedPrefix() throws Exception {
        SaxJsonContentHandler handler = new SaxJsonContentHandler("text", true, true, "@");
        handler.startDocument();
        handler.startElement(null, null, "test_integer", getMockedAttributes());

        Deque<ObjectNode> currentField = getCurrentField(handler);
        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();

        ObjectNode currentNode = objectNodeIterator.next();

        assertEquals(currentNode.get("@game").bigIntegerValue(), BigInteger.ONE);
        assertEquals(currentNode.get("@set").booleanValue(), true);
        assertEquals(currentNode.get("@match").textValue(), "hello");
    }

    @Test
    public void attributesWithSpecifiedPrefix() throws Exception {
        SaxJsonContentHandler handler = new SaxJsonContentHandler("text", false, true, "@");
        handler.startDocument();
        handler.startElement(null, null, "test_integer", getMockedAttributes());

        Deque<ObjectNode> currentField = getCurrentField(handler);
        Iterator<ObjectNode> objectNodeIterator = currentField.descendingIterator();

        ObjectNode currentNode = objectNodeIterator.next();

        assertEquals(currentNode.get("@game").textValue(),"1");
        assertEquals(currentNode.get("@set").textValue(), "true");
        assertEquals(currentNode.get("@match").textValue(), "hello");
    }
}
