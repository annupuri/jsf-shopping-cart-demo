/**
 * MySchemaOutputResolver.java
 */
package shopping.tool;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.SchemaOutputResolver;

public class MySchemaOutputResolver extends SchemaOutputResolver {
	/**
	 * Constructor.
	 */
	public MySchemaOutputResolver() {
	}

	/**
	 * @see javax.xml.bind.SchemaOutputResolver#createOutput(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Result createOutput(String namespaceUri, String suggestedFileName)
			throws IOException {
		System.out.println(getClass().getSimpleName()
				+ "#createOutput : namespaceUri=" + namespaceUri
				+ ", suggestedFileName=" + suggestedFileName);
		final File file = new File(suggestedFileName);
		System.out.println(getClass().getSimpleName()
				+ "#createOutput : file.path=" + file.getAbsolutePath());
		final StreamResult result = new StreamResult(file);
		result.setSystemId(file.toURI().toURL().toString());
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final JAXBContext jaxbContext = JAXBContext
					.newInstance(Product.class);
			System.out.println("MySchemaOutputResolver#main : jaxbContext="
					+ jaxbContext);
			final SchemaOutputResolver sor = new MySchemaOutputResolver();
			jaxbContext.generateSchema(sor);
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
	}
}
