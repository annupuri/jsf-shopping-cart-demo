/**
 * ProductDao.java
 */
package shopping.integration;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import shopping.business.generated.Product;
import shopping.common.AppUtils;

/**
 * Data access object for the products XML file.
 */
public class ProductDao {

	private static final String XML_ROOT_ELEMENT = "Products";
	private static final String XML_FILE_NAME = "Products.xml";

	private static final ProductDao instance = new ProductDao();

	private final Log log = LogFactory.getLog(getClass());
	private final File dataDir;
	private final Marshaller marshaller;
	private final Unmarshaller unmarshaller;

	/**
	 * Creates the shared instance backed by {@link AppUtils#getDataDirPath()}.
	 */
	private ProductDao() {
		this(new File(AppUtils.getDataDirPath()));
	}

	/**
	 * Creates a DAO that reads and writes {@value #XML_FILE_NAME} in the given
	 * directory. Exposed so tests can point it at a temporary directory.
	 *
	 * @param dataDir
	 *            directory holding the products XML file.
	 */
	public ProductDao(File dataDir) {
		this.dataDir = dataDir;
		try {
			this.marshaller = JAXBContext.newInstance(Product[].class)
					.createMarshaller();
			this.unmarshaller = JAXBContext.newInstance(
					UnmarshalExceptionAvoidance.class).createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the shared instance.
	 */
	public static ProductDao getInstance() {
		return instance;
	}

	public List<Product> findProducts() throws JAXBException {
		final File file = new File(this.dataDir, XML_FILE_NAME);
		if (!file.isFile()) {
			log.info("no product store at " + file.getAbsolutePath()
					+ "; treating the catalogue as empty");
			return Collections.emptyList();
		}
		log.debug("findProducts : file.path=" + file.getAbsolutePath());
		final Object object = this.unmarshaller.unmarshal(file);
		if (!(object instanceof JAXBElement)) {
			throw new JAXBException("unexpected root element: " + object);
		}
		final Object value = ((JAXBElement<?>) object).getValue();
		if (!(value instanceof Product[])) {
			throw new JAXBException("unexpected root content: " + value);
		}
		final Product[] products = (Product[]) value;
		log.debug("findProducts : loaded " + products.length + " product(s)");
		return Arrays.asList(products);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void saveProducts(List<Product> productList) throws JAXBException,
			FileNotFoundException {
		final File file = new File(this.dataDir, XML_FILE_NAME);
		final File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		log.info("file.path=" + file.getAbsolutePath());
		final Product[] products = CollectionUtils.isNotEmpty(productList)
				? productList.toArray(new Product[0])
				: new Product[0];
		this.marshaller.marshal(new JAXBElement(new QName(XML_ROOT_ELEMENT),
				Product[].class, products), file);
	}

	@XmlRegistry
	private static class UnmarshalExceptionAvoidance {
		@SuppressWarnings("rawtypes")
		@XmlElementDecl(name = XML_ROOT_ELEMENT)
		JAXBElement thisCanAlsoBeAnyName(Product[] value) {
			// as long as you don't use this, you don't need to implement this.
			return null;
		}
	}
}
