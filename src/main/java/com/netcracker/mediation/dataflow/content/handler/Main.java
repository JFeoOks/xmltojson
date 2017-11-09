package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        JsonGenerator generator = new JsonFactory().createGenerator(System.out);
        SaxJsonContentHandler handler = new SaxJsonContentHandler(true);
        xmlReader.setContentHandler(handler);
        xmlReader.parse("F:\\Projects\\Sandbox\\ttxjson\\src\\main\\resources\\input_full.xml");

        System.out.println(handler);
        generator.close();

    }
}
