/**
 * ProductXmlCreater.java
 */
package shopping.tool;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import shopping.business.generated.Product;
import shopping.integration.ProductDao;

public class ProductXmlCreater {
	private static final Log log = LogFactory.getLog(ProductXmlCreater.class);

	/**
	 * Test for marshaling and un-marshaling.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final List<Product> productList = new ArrayList<Product>();
			{
				final Product product = new Product();
				product.setId("DPC-013678");
				product.setName("Desktop PC DPC-4100");
				product.setPrice(new BigDecimal("58780.0"));
				product.setQuantity(24);
				product.setDescription("Intel Core i5 1.7GHz quad-core, 4GB, 1TB");
				productList.add(product);
			}
			{
				final Product product = new Product();
				product.setId("DPC-024812");
				product.setName("Desktop PC DPC-4400");
				product.setPrice(new BigDecimal("79800.0"));
				product.setQuantity(24);
				product.setDescription("Intel Core i7 2.2GHz quad-core, 6GB, 2TB");
				productList.add(product);
			}
			{
				final Product product = new Product();
				product.setId("LPC-013502");
				product.setName("Laptop PC LPC-1100");
				product.setPrice(new BigDecimal("48700.0"));
				product.setQuantity(17);
				product.setDescription("Intel Core i3 1.6GHz dual-core, 4GB, 500GB, 6-cell Lithium Ion");
				productList.add(product);
			}
			{
				final Product product = new Product();
				product.setId("LPC-013503");
				product.setName("Laptop PC LPC-1200");
				product.setPrice(new BigDecimal("68800.0"));
				product.setQuantity(25);
				product.setDescription("Intel Core i5 2.0GHz dual-core, 4GB, 1TB, 6-cell Lithium Ion");
				productList.add(product);
			}
			{
				final Product product = new Product();
				product.setId("PRN-003895");
				product.setName("Laser Printer");
				product.setPrice(new BigDecimal("28900.0"));
				product.setQuantity(23);
				product.setDescription("4800dpi");
				productList.add(product);
			}
			final ProductDao dao = ProductDao.getInstance();
			dao.saveProducts(productList);
			final Collection<Product> productCollection = dao.findProducts();
			if (CollectionUtils.isNotEmpty(productCollection)) {
				CollectionUtils.forAllDo(productCollection, new Closure() {
					@Override
					public void execute(Object object) {
						try {
							log.info("product="
									+ (object != null ? BeanUtils
											.describe(object) : "null"));
						} catch (IllegalAccessException
								| InvocationTargetException
								| NoSuchMethodException e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				log.info("productCollection is empty.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
