/**
 * ProductDao.java
 */
package shopping.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import shopping.business.generated.Product;
import shopping.common.AppUtils;

/**
 * Data access object for the products XML file.
 */
public class ProductDao {

	private static final Logger log = LoggerFactory.getLogger(ProductDao.class);

	private static final String XML_ROOT_ELEMENT = "Products";
	private static final String XML_FILE_NAME = "Products.xml";

	private static final ProductDao instance = new ProductDao();

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
			log.info("no product store at {}; treating the catalogue as empty",
					file.getAbsolutePath());
			return Collections.emptyList();
		}
		log.debug("findProducts : file.path={}", file.getAbsolutePath());
		final Object object = this.unmarshaller.unmarshal(file);
		if (!(object instanceof JAXBElement)) {
			throw new JAXBException("unexpected root element: " + object);
		}
		final Object value = ((JAXBElement<?>) object).getValue();
		if (!(value instanceof Product[])) {
			throw new JAXBException("unexpected root content: " + value);
		}
		final Product[] products = (Product[]) value;
		log.debug("findProducts : loaded {} product(s)", products.length);
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
		log.info("saveProducts : file.path={}", file.getAbsolutePath());
		final Product[] products = productList != null
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
