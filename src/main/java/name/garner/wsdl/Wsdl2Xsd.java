package name.garner.wsdl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jlibs.xml.sax.crawl.XMLCrawler;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class Wsdl2Xsd {
	private static org.slf4j.Logger LOG = LoggerFactory
			.getLogger(Wsdl2Xsd.class);

	public static void wsdlToXSD(InputStream is, Writer writer) {
		DocumentBuilder builder;
		try {
			builder = getDocBuilder();
			Document wsdlDoc = builder.parse(is);
			Document xsdDoc = getDocBuilder().newDocument();
			populateXsdDoc(wsdlDoc, xsdDoc);
			writeToStream(xsdDoc, writer);
			is.close();
			writer.flush();
			writer.close();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			LOG.error(sw.toString());
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
			if (nd.getNodeName().equals("s:schema")) {
				NodeList xsdNodes = nd.getChildNodes();
				for (int j = 0; j < xsdNodes.getLength(); j++) {
					Node temp = xsdNodes.item(j);
					Node toAdded = xsdDoc.importNode(temp, true);
					root.appendChild(toAdded);
				}
			}
		}
	}

	private static void writeToStream(Document document, Writer writer)
			throws IOException {
		DOMImplementationLS domImplementationLS = (DOMImplementationLS) document
				.getImplementation();
		LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
		String xsd = lsSerializer.writeToString(document);
		writer.write(xsd);
		writer.flush();
	}

	private static DocumentBuilder getDocBuilder()
			throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	public static void main(String[] args) {
//		StringBuffer sb = new StringBuffer();
		try {
			URL url = new URL(
					"http://wsf.cdyne.com/WeatherWS/Weather.asmx?WSDL");
			
			XMLCrawler xmlCrawler = new XMLCrawler();
			InputSource wsdl = new InputSource(url.openStream());
			wsdl.setSystemId("http://wsf.cdyne.com/WeatherWS/Weather.asmx?WSDL");
			File folder = new File("xsd");
			xmlCrawler.crawlInto(wsdl, folder);

//			Wsdl2Xsd.wsdlToXSD(url.openStream(), CharStreams.asWriter(sb));
		} catch (IOException ioe) {
			LOG.error("Error", ioe);
		}
//		System.out.println(sb.toString());
	}
}
