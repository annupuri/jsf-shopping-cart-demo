/**
 * ProductXmlCreater.java
 */
package shopping.tool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shopping.business.generated.Product;
import shopping.integration.ProductDao;

/**
 * Writes the sample {@code Products.xml} and reads it back. Run with
 * {@code mvn compile exec:java}.
 */
public class ProductXmlCreater {

	private static final Logger log = LoggerFactory.getLogger(ProductXmlCreater.class);

	private ProductXmlCreater() {
	}

	private static Product product(String id, String name, String price, int quantity,
			String description) {
		final Product product = new Product();
		product.setId(id);
		product.setName(name);
		product.setPrice(new BigDecimal(price));
		product.setQuantity(quantity);
		product.setDescription(description);
		return product;
	}

	public static void main(String[] args) {
		try {
			final List<Product> productList = new ArrayList<>();
			productList.add(product("DPC-013678", "Desktop PC DPC-4100", "58780.0", 24,
					"Intel Core i5 1.7GHz quad-core, 4GB, 1TB"));
			productList.add(product("DPC-024812", "Desktop PC DPC-4400", "79800.0", 24,
					"Intel Core i7 2.2GHz quad-core, 6GB, 2TB"));
			productList.add(product("LPC-013502", "Laptop PC LPC-1100", "48700.0", 17,
					"Intel Core i3 1.6GHz dual-core, 4GB, 500GB, 6-cell Lithium Ion"));
			productList.add(product("LPC-013503", "Laptop PC LPC-1200", "68800.0", 25,
					"Intel Core i5 2.0GHz dual-core, 4GB, 1TB, 6-cell Lithium Ion"));
			productList.add(product("PRN-003895", "Laser Printer", "28900.0", 23, "4800dpi"));

			final ProductDao dao = ProductDao.getInstance();
			dao.saveProducts(productList);
			for (Product product : dao.findProducts()) {
				log.info("product={}", product);
			}
		} catch (Exception e) {
			log.error("failed to create the sample XML", e);
		}
	}
}
