package shopping.business;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import shopping.business.generated.Product;
import shopping.integration.ProductDao;

/**
 * The service to get/sell/buy products.
 */
public class ProductService {
	private static final ProductService instance = new ProductService();
	private final Log log = LogFactory.getLog(getClass());
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
			// Get products from the XML.
			final List<Product> productList = this.productDao.findProducts();
			if (log.isDebugEnabled()) {
				log.debug("productList=" + productList);
			}
			return productList;
		} catch (JAXBException e) {
			throw new RuntimeException("failed to load products.", e);
		}
	}

	/**
	 * Sell products exclusively.
	 * 
	 * @param productMap
	 *            <ul>
	 *            <li>Key: Product ID</li>
	 *            <li>Value: Product</li>
	 *            </ul>
	 */
	public synchronized void sellProducts(Map<String, Product> productMap) {
		try {
			// Validate parameter.
			if (MapUtils.isEmpty(productMap)) {
				log.warn("productMap is empty.");
				return;
			}
			if (log.isDebugEnabled()) {
				CollectionUtils.forAllDo(productMap.entrySet(), new Closure() {
					@Override
					@SuppressWarnings("unchecked")
					public void execute(Object element) {
						final Map.Entry<String, Product> mapEntry = (Map.Entry<String, Product>) element;
						try {
							log.debug("sellProducts(1) : key="
									+ mapEntry.getKey() + ", value="
									+ BeanUtils.describe(mapEntry.getValue()));
						} catch (Exception e) {
							log.debug(
									"sellProducts(1) : key="
											+ mapEntry.getKey() + ", value="
											+ mapEntry.getValue(), e);
						}
					}
				});
			}
			// Get products from the XML.
			List<Product> productList = this.productDao.findProducts();
			// Validate current data.
			if (CollectionUtils.isEmpty(productList)) {
				log.warn("productList is empty.");
				return;
			}
			if (log.isDebugEnabled()) {
				CollectionUtils.forAllDo(productList, new Closure() {
					@Override
					public void execute(Object element) {
						try {
							log.debug("sellProducts(2) : product="
									+ BeanUtils.describe(element));
						} catch (Exception e) {
							log.debug("sellProducts(2) : product=" + element, e);
						}
					}
				});
			}
			productList = new ArrayList<Product>(productList);
			final Map<String, Product> newProductMap = new HashMap<String, Product>(
					productMap);
			// Update products.
			for (Iterator<Product> iterator = productList.iterator(); iterator
					.hasNext();) {
				final Product product = iterator.next();
				final String productId = product.getId();
				for (Iterator<Map.Entry<String, Product>> iterator2 = newProductMap
						.entrySet().iterator(); iterator2.hasNext();) {
					final Map.Entry<String, Product> newProductMapEntry = (Map.Entry<String, Product>) iterator2
							.next();
					if (!StringUtils.equals(productId,
							newProductMapEntry.getKey())) {
						continue;
					}
					final Product newProduct = newProductMapEntry.getValue();
					// Deduct quantity.
					final int newQuantity = product.getQuantity()
							- newProduct.getQuantity();
					if (newQuantity > 0) {
						product.setQuantity(newQuantity);
					} else {
						iterator.remove();
					}
					// Prevent duplicate selling.
					iterator2.remove();
				}
			}
			CollectionUtils.forAllDo(productList, new Closure() {
				@Override
				public void execute(Object element) {
					try {
						log.debug("sellProducts(3) : product="
								+ BeanUtils.describe(element));
					} catch (Exception e) {
						log.debug("sellProducts(3) : product=" + element, e);
					}
				}
			});
			// Save products.
			this.productDao.saveProducts(productList);
		} catch (RuntimeException e) {
			log.error("failed to sell products (productMap=" + productMap + "): " + e);
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
	 *            <ul>
	 *            <li>Key: Product ID</li> <li>Value: Product</li>
	 *            </ul>
	 */
	public synchronized void buyProducts(Map<String, Product> productMap) {
		try {
			// Validate parameter.
			if (MapUtils.isEmpty(productMap)) {
				log.warn("productMap is empty.");
				return;
			}
			if (log.isDebugEnabled()) {
				CollectionUtils.forAllDo(productMap.entrySet(), new Closure() {
					@Override
					@SuppressWarnings("unchecked")
					public void execute(Object element) {
						final Map.Entry<String, Product> mapEntry = (Map.Entry<String, Product>) element;
						try {
							log.debug("buyProducts(1) : key="
									+ mapEntry.getKey() + ", value="
									+ BeanUtils.describe(mapEntry.getValue()));
						} catch (Exception e) {
							log.debug(
									"buyProducts(1) : key=" + mapEntry.getKey()
											+ ", value=" + mapEntry.getValue(),
									e);
						}
					}
				});
			}
			// Get products from the XML.
			List<Product> productList = this.productDao.findProducts();
			if (log.isDebugEnabled()) {
				CollectionUtils.forAllDo(productList, new Closure() {
					@Override
					public void execute(Object element) {
						try {
							log.debug("buyProducts(2) : product="
									+ BeanUtils.describe(element));
						} catch (Exception e) {
							log.debug("buyProducts(2) : product=" + element, e);
						}
					}
				});
			}
			productList = productList != null ? new ArrayList<Product>(
					productList) : new ArrayList<Product>();
			final Map<String, Product> newProductMap = new HashMap<String, Product>(
					productMap);
			// Update products.
			for (Iterator<Product> iterator = productList.iterator(); iterator
					.hasNext();) {
				final Product product = iterator.next();
				final String productId = product.getId();
				for (Iterator<Map.Entry<String, Product>> iterator2 = newProductMap
						.entrySet().iterator(); iterator2.hasNext();) {
					final Map.Entry<String, Product> newProductMapEntry = (Map.Entry<String, Product>) iterator2
							.next();
					if (!StringUtils.equals(productId,
							newProductMapEntry.getKey())) {
						continue;
					}
					final Product newProduct = newProductMapEntry.getValue();
					// Raise quantity.
					final int newQuantity = product.getQuantity()
							+ newProduct.getQuantity();
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
			CollectionUtils.forAllDo(productList, new Closure() {
				@Override
				public void execute(Object element) {
					try {
						log.debug("buyProducts(3) : product="
								+ BeanUtils.describe(element));
					} catch (Exception e) {
						log.debug("buyProducts(3) : product=" + element, e);
					}
				}
			});
			// Save products.
			this.productDao.saveProducts(productList);
		} catch (RuntimeException e) {
			log.error("failed to buy products (productMap=" + productMap + "): " + e);
			throw e;
		} catch (FileNotFoundException | JAXBException e) {
			throw new RuntimeException("failed to save products.", e);
		}
	}
}
