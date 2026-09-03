/**
 * CartController.java
 */
package shopping.presentation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import shopping.business.ProductService;
import shopping.business.generated.Product;



/**
 * The controller of requests for the cart.
 */
@ManagedBean
@SessionScoped
public class CartController {
	private final Log log = LogFactory.getLog(getClass());
	private final ProductService productService;
	private Product product = new Product();
	private List<Product> productList = new ArrayList<Product>();
	private BigDecimal totalPrice = BigDecimal.ZERO;
	private int totalQuantity = 0;

	/**
	 * Constructor used by JSF; delegates to {@link ProductService#getInstance()}.
	 */
	public CartController() {
		this(ProductService.getInstance());
	}

	/**
	 * Constructor for tests, allowing the service to be injected.
	 *
	 * @param productService
	 *            the service to delegate to.
	 */
	public CartController(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * @return the addProduct
	 */
	public Product getProduct() {
		return this.product;
	}

	/**
	 * @param addProduct
	 *            the addProduct to set
	 */
	public void setProduct(Product addProduct) {
		this.product = addProduct;
	}

	/**
	 * @return the productList
	 */
	public List<Product> getProductList() {
		return this.productList;
	}

	/**
	 * @param productList
	 *            the productList to set
	 */
	public void setProductList(List<Product> selectedProductList) {
		this.productList = selectedProductList;
	}

	/**
	 * @return the totalPrice
	 */
	public BigDecimal getTotalPrice() {
		return this.totalPrice;
	}

	/**
	 * @param totalPrice
	 *            the totalPrice to set
	 */
	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	/**
	 * @return the totalQuantity
	 */
	public int getTotalQuantity() {
		return this.totalQuantity;
	}

	/**
	 * @param totalQuantity
	 *            the totalQuantity to set
	 */
	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	/**
	 * Add one unit of the given catalogue product to the cart. Called straight
	 * from the product table with the row's product as the argument.
	 *
	 * @param source
	 *            the catalogue product to add.
	 */
	public void addToCart(Product source) {
		final Product line = copyOf(source);
		line.setQuantity(1);
		addLine(line);
	}

	/**
	 * Add the product currently bound to {@link #getProduct()} to the cart.
	 */
	public void addProduct() {
		addLine(copyOf(this.product));
	}

	private static Product copyOf(Product source) {
		final Product copy = new Product();
		try {
			BeanUtils.copyProperties(copy, source);
		} catch (Exception e) {
			throw new RuntimeException("product may be null or non-bean.", e);
		}
		return copy;
	}

	private void addLine(Product newProduct) {
		log.debug("start.");
		try {
			if (StringUtils.isBlank(newProduct.getId())) {
				try {
					log.error("newProduct.id is blank. newProduct="
							+ BeanUtils.describe(newProduct));
				} catch (Exception e) {
					log.error("newProduct.id is blank.", e);
				}
				return;
			}
			clearTotal();
			final String productId = newProduct.getId();
			boolean newItem = true;
			for (Product eachProduct : this.productList) {
				if (ObjectUtils.equals(productId, eachProduct.getId())) {
					newItem = false;
					// add price
					final BigDecimal price = eachProduct.getPrice();
					final BigDecimal addPrice = newProduct.getPrice();
					if (price == null && addPrice == null) {
						eachProduct.setPrice(BigDecimal.ZERO);
					} else if (price == null) {
						eachProduct.setPrice(addPrice);
					} else if (addPrice == null) {
						eachProduct.setPrice(price);
					} else {
						eachProduct.setPrice(price.add(addPrice));
					}
					// add quantity
					eachProduct.setQuantity(eachProduct.getQuantity()
							+ newProduct.getQuantity());
				}
				this.totalPrice = this.totalPrice.add(eachProduct.getPrice());
				this.totalQuantity += eachProduct.getQuantity();
			}
			if (newItem) {
				this.productList.add(newProduct);
				this.totalPrice = this.totalPrice.add(newProduct.getPrice());
				this.totalQuantity += newProduct.getQuantity();
			}
		} finally {
			log.debug("end.");
		}
	}

	/**
	 * Clear the cart table.
	 */
	public void clear() {
		log.debug("start.");
		try {
			this.product = new Product();
			this.productList.clear();
			clearTotal();
		} finally {
			log.debug("end.");
		}
	}

	/**
	 * Clear the total row of the cart table.
	 */
	private void clearTotal() {
		this.totalPrice = BigDecimal.ZERO;
		this.totalQuantity = 0;
	}

	/**
	 * Order every line currently in the cart, then empty it.
	 */
	public void checkOut() {
		log.debug("start.");
		try {
			if (this.productList.isEmpty()) {
				log.warn("checkOut called with an empty cart.");
				return;
			}
			final Map<String, Product> sellProductMap = new HashMap<String, Product>();
			for (Product line : this.productList) {
				final Product order = new Product();
				order.setId(line.getId());
				order.setQuantity(line.getQuantity());
				sellProductMap.put(order.getId(), order);
			}
			this.productService.sellProducts(sellProductMap);
			clear();
		} catch (RuntimeException e) {
			log.error("failed to check out: " + e);
			throw e;
		} finally {
			log.debug("end.");
		}
	}
}
