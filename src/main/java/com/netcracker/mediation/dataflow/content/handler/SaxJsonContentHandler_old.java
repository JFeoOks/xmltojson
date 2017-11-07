//package com.netcracker.mediation.dataflow.content.handler;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.xml.sax.Attributes;
//import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;
//
//import java.util.ArrayDeque;
//import java.util.Deque;
//
//public class SaxJsonContentHandler_old extends DefaultHandler {
//
//    private JsonNodeFactory factory = JsonNodeFactory.instance;
//    private ObjectNode root = factory.objectNode();
//    private Deque<ObjectNode> current = new ArrayDeque<>();
//    private ValueHolder rawValue;
//
//    @Override
//    public void startDocument() throws SAXException {
//        current.offer(root);
//    }
//
//    @Override
//    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//        ObjectNode lastNode = current.peekLast();
//        if (lastNode == null) {
//            lastNode = factory.objectNode();
//            current.offerLast(lastNode);
//        }
//
//        if (lastNode.get(qName) != null) {
//            JsonNode jsonNode = lastNode.get(qName);
//            if (jsonNode.isArray()) ((ArrayNode) jsonNode).add(createNode(attributes));
//            else {
//                lastNode.remove(qName);
//
//                ObjectNode currentNode = createNode(attributes);
//
//                ArrayNode array = lastNode.putArray(qName);
//                array
//                        .add(jsonNode)
//                        .add(currentNode);
//
//                current.offerLast(currentNode);
//            }
//        } else {
//            ObjectNode node = lastNode.putObject(qName);
//            writeAttributes(node, attributes);
//            current.offerLast(node);
//        }
//    }
//
//    private ObjectNode createNode(final Attributes attributes) {
//        ObjectNode jsonNodes = factory.objectNode();
//
//        writeAttributes(jsonNodes, attributes);
//        return jsonNodes;
//    }
//
//    private void writeAttributes(final ObjectNode node, final Attributes attributes) {
//        for (int index = 0; index < attributes.getLength(); index++) {
//            node.put(attributes.getQName(index), attributes.getValue(index));
//        }
//    }
//
//    @Override
//    public void endElement(String uri, String localName, String qName) throws SAXException {
//        current.pollLast();
//    }
//
//    @Override
//    public void characters(char[] ch, int start, int length) throws SAXException {
//        super.characters(ch, start, length);
//    }
//
//
//    @Override
//    public String toString() {
//        return root.toString();
//    }
//
//    private class ValueHolder {
//
//        private String uri;
//        private String localName;
//        private String qName;
//        private Attributes attributes;
//
//
//    }
//}
