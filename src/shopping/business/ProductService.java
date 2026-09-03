package shopping.business;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBException;
import shopping.business.generated.Product;
import shopping.integration.ProductDao;

/**
 * The service to get/sell/buy products.
 */
public class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	private static final ProductService instance = new ProductService();

	private final ProductDao productDao;

	/**
	 * Creates the shared instance backed by {@link ProductDao#getInstance()}.
	 */
	private ProductService() {
		this(ProductDao.getInstance());
	}

	/**
	 * Creates a service backed by the given DAO. Exposed so tests can inject a
	 * DAO pointed at a temporary directory.
	 *
	 * @param productDao
	 *            the DAO to delegate persistence to.
	 */
	public ProductService(ProductDao productDao) {
		this.productDao = productDao;
	}

	/**
	 * @return the shared instance.
	 */
	public static ProductService getInstance() {
		return instance;
	}

	/**
	 * Get products exclusively.
	 *
	 * @return product list.
	 */
	public synchronized List<Product> getProducts() {
		try {
			final List<Product> productList = this.productDao.findProducts();
			log.debug("productList={}", productList);
			return productList;
		} catch (JAXBException e) {
			throw new RuntimeException("failed to load products.", e);
		}
	}

	/**
	 * Sell products exclusively.
	 *
	 * @param productMap
	 *            keyed by product id.
	 */
	public synchronized void sellProducts(Map<String, Product> productMap) {
		try {
			if (productMap == null || productMap.isEmpty()) {
				log.warn("productMap is empty.");
				return;
			}
			if (log.isDebugEnabled()) {
				for (Map.Entry<String, Product> e : productMap.entrySet()) {
					log.debug("sellProducts(1) : key={}, value={}", e.getKey(), e.getValue());
				}
			}
			List<Product> productList = this.productDao.findProducts();
			if (productList == null || productList.isEmpty()) {
				log.warn("productList is empty.");
				return;
			}
			if (log.isDebugEnabled()) {
				for (Product p : productList) {
					log.debug("sellProducts(2) : product={}", p);
				}
			}
			productList = new ArrayList<>(productList);
			final Map<String, Product> newProductMap = new HashMap<>(productMap);
			for (Iterator<Product> iterator = productList.iterator(); iterator.hasNext();) {
				final Product product = iterator.next();
				final String productId = product.getId();
				for (Iterator<Map.Entry<String, Product>> iterator2 = newProductMap
						.entrySet().iterator(); iterator2.hasNext();) {
					final Map.Entry<String, Product> newProductMapEntry = iterator2.next();
					if (!Objects.equals(productId, newProductMapEntry.getKey())) {
						continue;
					}
					final Product newProduct = newProductMapEntry.getValue();
					// Deduct quantity.
					final int newQuantity = product.getQuantity() - newProduct.getQuantity();
					if (newQuantity > 0) {
						product.setQuantity(newQuantity);
					} else {
						iterator.remove();
					}
					// Prevent duplicate selling.
					iterator2.remove();
				}
			}
			if (log.isDebugEnabled()) {
				for (Product p : productList) {
					log.debug("sellProducts(3) : product={}", p);
				}
			}
			this.productDao.saveProducts(productList);
		} catch (RuntimeException e) {
			log.error("failed to sell products (productMap={}): {}", productMap, e.toString());
			throw e;
		} catch (FileNotFoundException | JAXBException e) {
			throw new RuntimeException("failed to save products.", e);
		}
	}

	/**
	 * Buy products exclusively.
	 * <p>
	 * This method is prepared for future release.
	 *
	 * @param productMap
	 *            keyed by product id.
	 */
	public synchronized void buyProducts(Map<String, Product> productMap) {
		try {
			if (productMap == null || productMap.isEmpty()) {
				log.warn("productMap is empty.");
				return;
			}
			if (log.isDebugEnabled()) {
				for (Map.Entry<String, Product> e : productMap.entrySet()) {
					log.debug("buyProducts(1) : key={}, value={}", e.getKey(), e.getValue());
				}
			}
			List<Product> productList = new ArrayList<>(this.productDao.findProducts());
			if (log.isDebugEnabled()) {
				for (Product p : productList) {
					log.debug("buyProducts(2) : product={}", p);
				}
			}
			final Map<String, Product> newProductMap = new HashMap<>(productMap);
			for (Iterator<Product> iterator = productList.iterator(); iterator.hasNext();) {
				final Product product = iterator.next();
				final String productId = product.getId();
				for (Iterator<Map.Entry<String, Product>> iterator2 = newProductMap
						.entrySet().iterator(); iterator2.hasNext();) {
					final Map.Entry<String, Product> newProductMapEntry = iterator2.next();
					if (!Objects.equals(productId, newProductMapEntry.getKey())) {
						continue;
					}
					final Product newProduct = newProductMapEntry.getValue();
					// Raise quantity.
					final int newQuantity = product.getQuantity() + newProduct.getQuantity();
					if (newQuantity > 0) {
						product.setQuantity(newQuantity);
					} else {
						iterator.remove();
					}
					// Prevent duplicate buying.
					iterator2.remove();
				}
			}
			if (!newProductMap.isEmpty()) { // If new type product.
				productList.addAll(newProductMap.values());
			}
			if (log.isDebugEnabled()) {
				for (Product p : productList) {
					log.debug("buyProducts(3) : product={}", p);
				}
			}
			this.productDao.saveProducts(productList);
		} catch (RuntimeException e) {
			log.error("failed to buy products (productMap={}): {}", productMap, e.toString());
			throw e;
		} catch (FileNotFoundException | JAXBException e) {
			throw new RuntimeException("failed to save products.", e);
		}
	}
}
