package name.garner.wsdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class WsdlToXsdGenerator {
	private static org.slf4j.Logger LOG = LoggerFactory.getLogger(WsdlToXsdGenerator.class);

	public static void wsdlToXSD(InputStream is, OutputStream os) {
		DocumentBuilder builder;
		try {
			builder = getDocBuilder();
			Document wsdlDoc = builder.parse(is);
			Document xsdDoc = getDocBuilder().newDocument();
			populateXsdDoc(wsdlDoc, xsdDoc);
			writeToStream(xsdDoc, os);
			is.close();
			os.flush();
			os.close();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
		}
	}

	private static void populateXsdDoc(Document wsdlDoc, Document xsdDoc) {
		Element root = xsdDoc.createElement("xsd:schema");
		root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		xsdDoc.appendChild(root);
		Element element = wsdlDoc.getDocumentElement();
		Node node = element.getElementsByTagName("wsdl:types").item(0);
		NodeList lst = node.getChildNodes();
		for (int i = 0; i < lst.getLength(); i++) {
			Node nd = lst.item(i);
			if (nd.getNodeName().equals("xsd:schema")) {
				NodeList xsdNodes = nd.getChildNodes();
				for (int j = 0; j < xsdNodes.getLength(); j++) {
					Node temp = xsdNodes.item(j);
					Node toAdded = xsdDoc.importNode(temp, true);
					root.appendChild(toAdded);
				}
			}
		}
	}

	private static void writeToStream(Document document, OutputStream os)
			throws IOException {
		DOMImplementationLS domImplementationLS = (DOMImplementationLS) document
				.getImplementation();
		LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
		String xsd = lsSerializer.writeToString(document);
		PrintWriter pw = new PrintWriter(os);
		// logger.debug(xsd);
		pw.write(xsd);
		pw.flush();
	}

	private static DocumentBuilder getDocBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();
		WsdlToXsdGenerator.wsdlToXSD(new FileInputStream(new File("ts.wsdl")),
				new WriterOutputStream(new StringWriter(sb)));
		System.out.println(sb.toString());
	}
}
