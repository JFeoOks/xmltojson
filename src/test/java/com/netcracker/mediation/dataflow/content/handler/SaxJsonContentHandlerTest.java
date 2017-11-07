package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.lang.reflect.Field;
import java.util.Deque;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SaxJsonContentHandlerTest {

    private JsonNodeFactory factory = JsonNodeFactory.instance;
    private SaxJsonContentHandler handler = new SaxJsonContentHandler(true);


    private Attributes getMockedAttributes() {
        Attributes mock = mock(Attributes.class);
        when(mock.getLength()).thenReturn(3);
        when(mock.getQName(0)).thenReturn("game");
        when(mock.getQName(1)).thenReturn("set");
        when(mock.getQName(2)).thenReturn("match");

        when(mock.getValue(0)).thenReturn("1");
        when(mock.getValue(1)).thenReturn("2");
        when(mock.getValue(2)).thenReturn("3");

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

        assertNotNull(currentNode.get("game"));
        assertNotNull(currentNode.get("set"));
        assertNotNull(currentNode.get("match"));

        assertEquals(currentNode.get("game").textValue(), "1");
        assertEquals(currentNode.get("set").textValue(), "2");
        assertEquals(currentNode.get("match").textValue(), "3");

        ObjectNode parent = objectNodeIterator.next();

        assertNotNull(parent);
        assertNotNull(parent.get("test_param"));
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

        assertNotNull(currentNode.get("game"));
        assertNotNull(currentNode.get("set"));
        assertNotNull(currentNode.get("match"));

        assertEquals(currentNode.get("game").textValue(), "1");
        assertEquals(currentNode.get("set").textValue(), "2");
        assertEquals(currentNode.get("match").textValue(), "3");

        ObjectNode parent = objectNodeIterator.next();

        assertNotNull(parent);
        assertNotNull(parent.get("test_param"));
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
                root.get(handler).toString(),
                "{\n" +
                        "  \"note\": {\n" +
                        "    \"to\": \"Tove\",\n" +
                        "    \"from\": \"Jani\",\n" +
                        "    \"heading\": \"Reminder\",\n" +
                        "    \"body\": \"Don't forget me this weekend!\"\n" +
                        "  }\n" +
                        "}",
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
                "{\n" +
                        "  \"CATALOG\": {\n" +
                        "    \"CD\": [\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Empire Burlesque\",\n" +
                        "        \"ARTIST\": \"Bob Dylan\",\n" +
                        "        \"COUNTRY\": \"USA\",\n" +
                        "        \"COMPANY\": \"Columbia\",\n" +
                        "        \"PRICE\": \"10.90\",\n" +
                        "        \"YEAR\": \"1985\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Hide your heart\",\n" +
                        "        \"ARTIST\": \"Bonnie Tyler\",\n" +
                        "        \"COUNTRY\": \"UK\",\n" +
                        "        \"COMPANY\": \"CBS Records\",\n" +
                        "        \"PRICE\": \"9.90\",\n" +
                        "        \"YEAR\": \"1988\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Greatest Hits\",\n" +
                        "        \"ARTIST\": \"Dolly Parton\",\n" +
                        "        \"COUNTRY\": \"USA\",\n" +
                        "        \"COMPANY\": \"RCA\",\n" +
                        "        \"PRICE\": \"9.90\",\n" +
                        "        \"YEAR\": \"1982\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Red\",\n" +
                        "        \"ARTIST\": \"The Communards\",\n" +
                        "        \"COUNTRY\": \"UK\",\n" +
                        "        \"COMPANY\": \"London\",\n" +
                        "        \"PRICE\": \"7.80\",\n" +
                        "        \"YEAR\": \"1987\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}",
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
                "{\n" +
                        "  \"CATALOG\": {\n" +
                        "    \"CD\": [\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Empire Burlesque\",\n" +
                        "        \"ARTIST\": \"Bob Dylan\",\n" +
                        "        \"COUNTRY\": \"USA\",\n" +
                        "        \"COMPANY\": \"Columbia\",\n" +
                        "        \"PRICE\": 10.90,\n" +
                        "        \"YEAR\": 1985\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Hide your heart\",\n" +
                        "        \"ARTIST\": \"Bonnie Tyler\",\n" +
                        "        \"COUNTRY\": \"UK\",\n" +
                        "        \"COMPANY\": \"CBS Records\",\n" +
                        "        \"PRICE\": 9.90,\n" +
                        "        \"YEAR\": 1988\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Greatest Hits\",\n" +
                        "        \"ARTIST\": \"Dolly Parton\",\n" +
                        "        \"COUNTRY\": \"USA\",\n" +
                        "        \"COMPANY\": \"RCA\",\n" +
                        "        \"PRICE\": 9.90,\n" +
                        "        \"YEAR\": 1982\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"TITLE\": \"Red\",\n" +
                        "        \"ARTIST\": \"The Communards\",\n" +
                        "        \"COUNTRY\": \"UK\",\n" +
                        "        \"COMPANY\": \"London\",\n" +
                        "        \"PRICE\": 7.80,\n" +
                        "        \"YEAR\": 1987\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}",
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
                "{\n" +
                        "\t\"anagrafica\": {\n" +
                        "\t\t\"testata\": {\n" +
                        "\t\t\t\"nomemercato\": {\n" +
                        "\t\t\t\t\"id\": \"007\",\n" +
                        "\t\t\t\t\"text\": \"Mercato di test\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t\"data\": \"Giovedi 18 dicembre 2003 16.05.29\"\n" +
                        "\t\t},\n" +
                        "\t\t\"record\": [\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"5\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Miami American Cafe\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT07654930130\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"casa\",\n" +
                        "\t\t\t\t\t\"text\": \"Viale Carlo Espinasse 5, Como\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"13\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"302\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Filiberto Gilardi\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT87654770157\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Biancospini 20, Messina\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"8\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"1302\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Eidon\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT887511231\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Bassini 17/2, Milano\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"18\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"202\",\n" +
                        "\t\t\t\t\"rag_soc\": \"SkillNet\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT887642131\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Chiasserini 11A, Milano\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"24\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"12\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Eidon\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT04835710965\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"casa\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Cignoli 17/2, Roma\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"1112\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"5\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Miami American Cafe\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT07654930130\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"casa\",\n" +
                        "\t\t\t\t\t\"text\": \"Viale Carlo Espinasse 5, Como\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"13\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"302\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Filiberto Gilardi\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT87654770157\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Biancospini 20, Messina\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"8\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"1302\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Eidon\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT887511231\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Bassini 17/2, Milano\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"18\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"202\",\n" +
                        "\t\t\t\t\"rag_soc\": \"SkillNet\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT887642131\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Chiasserini 11A, Milano\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"24\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"202\",\n" +
                        "\t\t\t\t\"rag_soc\": \"SkillNet\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT887642131\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"ufficio\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Chiasserini 11A, Milano\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"24\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\t{\n" +
                        "\t\t\t\t\"codice_cliente\": \"12\",\n" +
                        "\t\t\t\t\"rag_soc\": \"Eidon\",\n" +
                        "\t\t\t\t\"codice_fiscale\": \"IT04835710965\",\n" +
                        "\t\t\t\t\"indirizzo\": {\n" +
                        "\t\t\t\t\t\"tipo\": \"casa\",\n" +
                        "\t\t\t\t\t\"text\": \"Via Cignoli 17/2, Roma\"\n" +
                        "\t\t\t\t},\n" +
                        "\t\t\t\t\"num_prodotti\": \"1112\"\n" +
                        "\t\t\t}\n" +
                        "\t\t]\n" +
                        "\t}\n" +
                        "}",
                JSONCompareMode.LENIENT);
    }
}
