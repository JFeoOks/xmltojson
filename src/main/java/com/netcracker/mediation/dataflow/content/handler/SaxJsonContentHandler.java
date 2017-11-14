package com.netcracker.mediation.dataflow.content.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.Deque;

public class SaxJsonContentHandler extends DefaultHandler {

    private JsonNode tree;
    private final ElementValueHolderFactory elementValueFactory;
    private Deque<ElementValueHolder> elementValueHolders = new ArrayDeque<>();

    public SaxJsonContentHandler() {
        this.elementValueFactory = new ElementValueHolderFactory();
    }

    public SaxJsonContentHandler(ElementValueHolderFactory elementValueFactory) {
        this.elementValueFactory = elementValueFactory;
    }

    @Override
    public void startDocument() throws SAXException {
        elementValueHolders.offerFirst(elementValueFactory.newRootInstance());
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        ElementValueHolder element = elementValueFactory.newInstance(qName, attributes);
        elementValueHolders.offerFirst(element);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ElementValueHolder element = elementValueHolders.poll();

        String childName = element.getName();
        JsonNode childNode = element.buildNode();

        ElementValueHolder parentNode = elementValueHolders.peek();
        parentNode.getChildren().put(childNode, childName);
    }

    @Override
    public void endDocument() throws SAXException {
        if (elementValueHolders.size() != 1) throw new IllegalArgumentException("The document is still opened");
        tree = elementValueHolders.poll().buildNode();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        elementValueHolders.peek().setValue(new String(ch, start, length));
    }

    public JsonNode getTree() {
        return tree;
    }

    public ElementValueHolderFactory getElementValueFactory() {
        return elementValueFactory;
    }
}
