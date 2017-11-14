package com.netcracker.mediation.dataflow.content.handler;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SaxJsonContentHandlerTest extends AbstractHolderTest {

    private SaxJsonContentHandler handler = new SaxJsonContentHandler();

    @Test
    public void createOneNewValueElement() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "element", mock(Attributes.class));
        handler.endElement(null, null, "element");
        handler.endDocument();

        assertNotNull(handler.getTree().get("element"));
    }

    @Test
    public void whenCharactersThenSaveValue() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "element", mock(Attributes.class));
        handler.characters("something".toCharArray(), 0, "something".length());
        handler.endElement(null, null, "element");
        handler.endDocument();

        assertEquals("something", handler.getTree().get("element").textValue());
    }

    @Test
    public void whenSeveralChunksThenAppendCharactersToPreviousSaved() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "element", mock(Attributes.class));
        handler.characters("many".toCharArray(), 0, "many".length());
        handler.characters(" ".toCharArray(), 0, " ".length());
        handler.characters("chunks".toCharArray(), 0, "chunks".length());
        handler.endElement(null, null, "element");
        handler.endDocument();

        assertEquals("many chunks", handler.getTree().get("element").textValue());
    }

    @Test
    public void whenCharactersIsBlankThanNotIntroduce() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "element", mock(Attributes.class));
        handler.characters("\n   ".toCharArray(), 0, "\n   ".length());
        handler.endElement(null, null, "element");
        handler.endDocument();

        assertTrue(handler.getTree().get("element").isNull());
    }

    @Test
    public void whenEndElementThenWriteToTheParent() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "element", mock(Attributes.class));
        handler.characters("my value".toCharArray(), 0, "my value".length());
        handler.endElement(null, null, "element");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertEquals("my value", handler.getTree().get("element").textValue());
    }

    @Test
    public void whenEndElementWithAttributesThenWriteToTheParent() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "element", getMockedAttributes());
        handler.characters("super value".toCharArray(), 0, "super value".length());
        handler.endElement(null, null, "element");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertEquals(4, handler.getTree().get("element").size());
        assertEquals("super value", handler.getTree().get("element").get("value").textValue());
        assertEquals("1", handler.getTree().get("element").get("int").textValue());
        assertEquals("true", handler.getTree().get("element").get("bool").textValue());
        assertEquals("hello", handler.getTree().get("element").get("str").textValue());
    }

    @Test
    public void whenAddingElementWithoutAttributesWithExistingNameThenWouldBeCreatedArray() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Dave".toCharArray(), 0, "Dave".length());
        handler.endElement(null, null, "person");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertTrue(handler.getTree().get("person").isArray());
        assertEquals(2, handler.getTree().get("person").size());

        assertEquals("Bob", handler.getTree().get("person").get(0).textValue());
        assertEquals("Dave", handler.getTree().get("person").get(1).textValue());
    }

    @Test
    public void whenAddingElementWithAttributesWithExistingNameThenWouldBeCreatedArray() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Dave".toCharArray(), 0, "Dave".length());
        handler.endElement(null, null, "person");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertTrue(handler.getTree().get("person").isArray());
        assertEquals(2, handler.getTree().get("person").size());

        assertEquals("Bob", handler.getTree().get("person").get(0).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(0).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(0).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(0).get("str").textValue());

        assertEquals("Dave", handler.getTree().get("person").get(1).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(1).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(1).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(1).get("str").textValue());
    }

    @Test
    public void whenAddingElementWithoutAttributesWithExistingArrayNameThenAddToArray() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Dave".toCharArray(), 0, "Dave".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Mary".toCharArray(), 0, "Mary".length());
        handler.endElement(null, null, "person");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertTrue(handler.getTree().get("person").isArray());
        assertEquals(3, handler.getTree().get("person").size());

        assertEquals("Bob", handler.getTree().get("person").get(0).textValue());
        assertEquals("Dave", handler.getTree().get("person").get(1).textValue());
        assertEquals("Mary", handler.getTree().get("person").get(2).textValue());
    }

    @Test
    public void whenAddingElementWithAttributesWithExistingArrayNameThenAddToArray() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Dave".toCharArray(), 0, "Dave".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Mary".toCharArray(), 0, "Mary".length());
        handler.endElement(null, null, "person");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertTrue(handler.getTree().get("person").isArray());
        assertEquals(3, handler.getTree().get("person").size());

        assertEquals("Bob", handler.getTree().get("person").get(0).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(0).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(0).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(0).get("str").textValue());

        assertEquals("Dave", handler.getTree().get("person").get(1).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(1).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(1).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(1).get("str").textValue());

        assertEquals("Mary", handler.getTree().get("person").get(2).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(2).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(2).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(2).get("str").textValue());
    }

    @Test
    public void whenAddingElementWithoutAttributesInArrayWhereElementsHasAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Dave".toCharArray(), 0, "Dave".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Mary".toCharArray(), 0, "Mary".length());
        handler.endElement(null, null, "person");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertTrue(handler.getTree().get("person").isArray());
        assertEquals(3, handler.getTree().get("person").size());

        assertEquals("Bob", handler.getTree().get("person").get(0).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(0).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(0).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(0).get("str").textValue());

        assertEquals("Dave", handler.getTree().get("person").get(1).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(1).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(1).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(1).get("str").textValue());

        assertEquals("Mary", handler.getTree().get("person").get(2).textValue());
    }

    @Test
    public void whenAddingElementWithAttributesInArrayWhereElementsHasNotAttributes() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Dave".toCharArray(), 0, "Dave".length());
        handler.endElement(null, null, "person");

        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Mary".toCharArray(), 0, "Mary".length());
        handler.endElement(null, null, "person");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertTrue(handler.getTree().get("person").isArray());
        assertEquals(3, handler.getTree().get("person").size());

        assertEquals("Bob", handler.getTree().get("person").get(0).textValue());
        assertEquals("Dave", handler.getTree().get("person").get(1).textValue());

        assertEquals("Mary", handler.getTree().get("person").get(2).get("value").textValue());
        assertEquals("1", handler.getTree().get("person").get(2).get("int").textValue());
        assertEquals("true", handler.getTree().get("person").get(2).get("bool").textValue());
        assertEquals("hello", handler.getTree().get("person").get(2).get("str").textValue());
    }

    @Test
    public void whenElementWithoutAttributesPutInOtherElementThenDoubleNesting() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "managers", mock(Attributes.class));
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");
        handler.endElement(null, null, "managers");
        handler.endDocument();

        assertEquals(1, handler.getTree().size());
        assertEquals(1, handler.getTree().get("managers").size());
    }

    @Test
    public void whenElementWithAttributesPutInOtherElementThenDoubleNesting() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "managers", getMockedAttributes());
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.endElement(null, null, "managers");
        handler.endDocument();

        assertEquals(4, handler.getTree().get("managers").size());
        assertEquals(4, handler.getTree().get("managers").get("person").size());

        assertEquals("1", handler.getTree().get("managers").get("int").textValue());
        assertEquals("true", handler.getTree().get("managers").get("bool").textValue());
        assertEquals("hello", handler.getTree().get("managers").get("str").textValue());

        assertEquals("Bob", handler.getTree().get("managers").get("person").get("value").textValue());
        assertEquals("1", handler.getTree().get("managers").get("person").get("int").textValue());
        assertEquals("true", handler.getTree().get("managers").get("person").get("bool").textValue());
        assertEquals("hello", handler.getTree().get("managers").get("person").get("str").textValue());
    }

    @Test
    public void whenElementWithAttributesPutInOtherElementWithoutThenDoubleNesting() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "managers", mock(Attributes.class));
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.startElement(null, null, "person", getMockedAttributes());
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.endElement(null, null, "managers");
        handler.endDocument();

        assertEquals(1, handler.getTree().get("managers").size());
        assertEquals(4, handler.getTree().get("managers").get("person").size());

        assertEquals("Bob", handler.getTree().get("managers").get("person").get("value").textValue());
        assertEquals("1", handler.getTree().get("managers").get("person").get("int").textValue());
        assertEquals("true", handler.getTree().get("managers").get("person").get("bool").textValue());
        assertEquals("hello", handler.getTree().get("managers").get("person").get("str").textValue());
    }

    @Test
    public void whenElementWithoutAttributesPutInOtherElementWithThenDoubleNesting() throws Exception {
        handler.startDocument();
        handler.startElement(null, null, "managers", getMockedAttributes());
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.startElement(null, null, "person", mock(Attributes.class));
        handler.characters("Bob".toCharArray(), 0, "Bob".length());
        handler.endElement(null, null, "person");
        handler.characters("\n    ".toCharArray(), 0, "\n    ".length());
        handler.endElement(null, null, "managers");
        handler.endDocument();

        assertEquals(4, handler.getTree().get("managers").size());

        assertEquals("1", handler.getTree().get("managers").get("int").textValue());
        assertEquals("true", handler.getTree().get("managers").get("bool").textValue());
        assertEquals("hello", handler.getTree().get("managers").get("str").textValue());
        assertEquals("Bob", handler.getTree().get("managers").get("person").textValue());
    }

    @Test
    public void testOneNodeExample() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/oneNode.xml");

        JSONAssert.assertEquals(
                "{\"note\":{\"to\":\"Tove\",\"from\":\"Jani\",\"heading\":\"Reminder\",\"body\":\"Don't forget me this weekend!\"}}",
                handler.getTree().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void checkArrayCdWithoutNumberConvertion() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/arrayCd.xml");


        JSONAssert.assertEquals(
                "{\"CATALOG\":{\"CD\":[{\"TITLE\":\"Empire Burlesque\",\"ARTIST\":\"Bob Dylan\",\"COUNTRY\":\"USA\",\"COMPANY\":\"Columbia\",\"PRICE\":\"10.90\",\"YEAR\":\"1985\"},{\"TITLE\":\"Hide your heart\",\"ARTIST\":\"Bonnie Tyler\",\"COUNTRY\":\"UK\",\"COMPANY\":\"CBS Records\",\"PRICE\":\"9.90\",\"YEAR\":\"1988\"},{\"TITLE\":\"Greatest Hits\",\"ARTIST\":\"Dolly Parton\",\"COUNTRY\":\"USA\",\"COMPANY\":\"RCA\",\"PRICE\":\"9.90\",\"YEAR\":\"1982\"},{\"TITLE\":\"Red\",\"ARTIST\":\"The Communards\",\"COUNTRY\":\"UK\",\"COMPANY\":\"London\",\"PRICE\":\"7.80\",\"YEAR\":\"1987\"}]}}",
                handler.getTree().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void checkArrayAnagraficaWithoutNumberConvertion() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        ElementValueHolderFactory elementValueHolderFactory = new ElementValueHolderFactory();
        elementValueHolderFactory.setValuePrefix("text");
        SaxJsonContentHandler handler = new SaxJsonContentHandler(elementValueHolderFactory);

        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/arrayAnagrafica.xml");

        JSONAssert.assertEquals(
                "{\"anagrafica\":{\"testata\":{\"nomemercato\":{\"id\":\"007\",\"text\":\"Mercato di test\"},\"data\":\"Giovedi 18 dicembre 2003 16.05.29\"},\"record\":[{\"codice_cliente\":\"5\",\"rag_soc\":\"Miami American Cafe\",\"codice_fiscale\":\"IT07654930130\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Viale Carlo Espinasse 5, Como\"},\"num_prodotti\":\"13\"},{\"codice_cliente\":\"302\",\"rag_soc\":\"Filiberto Gilardi\",\"codice_fiscale\":\"IT87654770157\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Biancospini 20, Messina\"},\"num_prodotti\":\"8\"},{\"codice_cliente\":\"1302\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT887511231\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Bassini 17/2, Milano\"},\"num_prodotti\":\"18\"},{\"codice_cliente\":\"202\",\"rag_soc\":\"SkillNet\",\"codice_fiscale\":\"IT887642131\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Chiasserini 11A, Milano\"},\"num_prodotti\":\"24\"},{\"codice_cliente\":\"12\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT04835710965\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Via Cignoli 17/2, Roma\"},\"num_prodotti\":\"1112\"},{\"codice_cliente\":\"5\",\"rag_soc\":\"Miami American Cafe\",\"codice_fiscale\":\"IT07654930130\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Viale Carlo Espinasse 5, Como\"},\"num_prodotti\":\"13\"},{\"codice_cliente\":\"302\",\"rag_soc\":\"Filiberto Gilardi\",\"codice_fiscale\":\"IT87654770157\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Biancospini 20, Messina\"},\"num_prodotti\":\"8\"},{\"codice_cliente\":\"1302\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT887511231\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Bassini 17/2, Milano\"},\"num_prodotti\":\"18\"},{\"codice_cliente\":\"202\",\"rag_soc\":\"SkillNet\",\"codice_fiscale\":\"IT887642131\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Chiasserini 11A, Milano\"},\"num_prodotti\":\"24\"},{\"codice_cliente\":\"202\",\"rag_soc\":\"SkillNet\",\"codice_fiscale\":\"IT887642131\",\"indirizzo\":{\"tipo\":\"ufficio\",\"text\":\"Via Chiasserini 11A, Milano\"},\"num_prodotti\":\"24\"},{\"codice_cliente\":\"12\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT04835710965\",\"indirizzo\":{\"tipo\":\"casa\",\"text\":\"Via Cignoli 17/2, Roma\"},\"num_prodotti\":\"1112\"}]}}",
                handler.getTree().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void nestedNodes() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        ElementValueHolderFactory factory = new ElementValueHolderFactory();
        factory
                .setAttrPrefix("@")
                .setConvertToJsonPrimitives(false)
                .setUsePrefixForAttributes(true)
                .setValuePrefix("#text");
        SaxJsonContentHandler handler = new SaxJsonContentHandler(factory);

        xmlReader.setContentHandler(handler);
        xmlReader.parse("src/test/resources/nestedNode.xml");

        JSONAssert.assertEquals(
                handler.getTree().toString(),
                "{\"anagrafica\":{\"testata\":{\"nomemercato\":{\"@id\":\"007\",\"#text\":\"Mercato di test\"},\"data\":\"Giovedi 18 dicembre 2003 16.05.29\"},\"record\":[{\"codice_cliente\":\"5\",\"rag_soc\":\"Miami American Cafe\",\"codice_fiscale\":\"IT07654930130\",\"indirizzo\":{\"@tipo\":\"casa\",\"#text\":\"Viale Carlo Espinasse 5, Como\"},\"num_prodotti\":{\"testata\":{\"nomemercato\":{\"@id\":\"007\",\"#text\":\"Mercato di test\"},\"data\":\"Giovedi 18 dicembre 2003 16.05.29\"}}},{\"codice_cliente\":\"302\",\"rag_soc\":\"Filiberto Gilardi\",\"codice_fiscale\":\"IT87654770157\",\"indirizzo\":{\"@tipo\":\"ufficio\",\"#text\":\"Via Biancospini 20, Messina\"},\"num_prodotti\":{\"testata\":{\"nomemercato\":{\"@id\":\"007\",\"#text\":\"Mercato di test\"},\"data\":\"Giovedi 18 dicembre 2003 16.05.29\"}}},{\"codice_cliente\":\"1302\",\"rag_soc\":\"Eidon\",\"codice_fiscale\":\"IT887511231\",\"indirizzo\":{\"@tipo\":\"ufficio\",\"#text\":\"Via Bassini 17/2, Milano\"},\"num_prodotti\":{\"testata\":{\"nomemercato\":{\"@id\":\"007\",\"#text\":\"Mercato di test\"},\"data\":\"Giovedi 18 dicembre 2003 16.05.29\"}}}]}}",
                JSONCompareMode.LENIENT);
    }
}
