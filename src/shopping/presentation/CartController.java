/**
 * CartController.java
 */
package shopping.presentation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import shopping.business.ProductService;
import shopping.business.generated.Product;

/**
 * The controller of requests for the cart.
 */
@Named
@SessionScoped
public class CartController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(CartController.class);

	private transient ProductService productService;
	private Product product = new Product();
	private List<Product> productList = new ArrayList<>();
	private BigDecimal totalPrice = BigDecimal.ZERO;
	private int totalQuantity = 0;

	/**
	 * Constructor used by the container; delegates to
	 * {@link ProductService#getInstance()}.
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

	/** Re-resolves the singleton after session passivation nulls the field. */
	private ProductService service() {
		if (this.productService == null) {
			this.productService = ProductService.getInstance();
		}
		return this.productService;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public List<Product> getProductList() {
		return this.productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}

	public BigDecimal getTotalPrice() {
		return this.totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public int getTotalQuantity() {
		return this.totalQuantity;
	}

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
		copy.setId(source.getId());
		copy.setName(source.getName());
		copy.setPrice(source.getPrice());
		copy.setQuantity(source.getQuantity());
		copy.setDescription(source.getDescription());
		return copy;
	}

	private void addLine(Product newProduct) {
		log.debug("start.");
		try {
			final String productId = newProduct.getId();
			if (productId == null || productId.isBlank()) {
				log.error("newProduct.id is blank: {}", newProduct);
				return;
			}
			clearTotal();
			boolean newItem = true;
			for (Product eachProduct : this.productList) {
				if (Objects.equals(productId, eachProduct.getId())) {
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
			final Map<String, Product> sellProductMap = new HashMap<>();
			for (Product line : this.productList) {
				final Product order = new Product();
				order.setId(line.getId());
				order.setQuantity(line.getQuantity());
				sellProductMap.put(order.getId(), order);
			}
			service().sellProducts(sellProductMap);
			clear();
		} catch (RuntimeException e) {
			log.error("failed to check out: {}", e.toString());
			throw e;
		} finally {
			log.debug("end.");
		}
	}
}
