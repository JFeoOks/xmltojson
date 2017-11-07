package com.netcracker.mediation.dataflow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;

public class XML2JSON2 {
    /**
     * Copy/format JSON using {@link XMLEventWriter#add(XMLEventReader)}.
     *
     * @param args ignored
     * @throws XMLStreamException
     * @throws IOException
     */
    public static void main(String[] args) throws XMLStreamException, IOException {
//        InputStream input = XML2JSON2.class.getResourceAsStream("input.xml");
        InputStream input = new FileInputStream("src/test/resources/arrayAnagrafica.xml");
        OutputStream output = System.out;
        /*
		 * If we want to insert JSON array boundaries for multiple elements,
		 * we need to set the <code>autoArray</code> property.
		 * If our XML source was decorated with <code>&lt;?xml-multiple?&gt;</code>
		 * processing instructions, we'd set the <code>multiplePI</code>
		 * property instead.
		 * With the <code>autoPrimitive</code> property set, element text gets
		 * automatically converted to JSON primitives (number, boolean, null).
		 */
        JsonXMLConfig config = new JsonXMLConfigBuilder()
                .autoArray(true)
                .autoPrimitive(true)
                .prettyPrint(true)
                .build();
        try {
			/*
			 * Create reader (XML).
			 */
            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);

			/*
			 * Create writer (JSON).
			 */
            XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);

			/*
			 * Copy events from reader to writer.
			 */
            writer.add(reader);

			/*
			 * Close reader/writer.
			 */
            reader.close();
            writer.close();
        } finally {
			/*
			 * As per StAX specification, XMLEventReader/Writer.close() doesn't close
			 * the underlying stream.
			 */
            output.close();
            input.close();
        }
    }
}
