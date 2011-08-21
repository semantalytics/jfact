package datatypes.test;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class CheckURItest {
	public static void main(String[] args) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(CheckURItest.class.getResourceAsStream("/schema.xsd"));
	}
}
