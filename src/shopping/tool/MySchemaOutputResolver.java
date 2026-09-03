/**
 * MySchemaOutputResolver.java
 */
package shopping.tool;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

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
