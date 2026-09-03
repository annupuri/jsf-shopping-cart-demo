/**
 * ProductController.java
 */
package shopping.presentation;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import shopping.business.ProductService;
import shopping.business.generated.Product;


/**
 * The controller of requests for products.
 */
@ManagedBean
@RequestScoped
public class ProductController {
	private final ProductService productService;

	/**
	 * Constructor used by JSF; delegates to {@link ProductService#getInstance()}.
	 */
	public ProductController() {
		this(ProductService.getInstance());
	}

	/**
	 * Constructor for tests, allowing the service to be injected.
	 *
	 * @param productService
	 *            the service to delegate to.
	 */
	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * Get products.
	 * @return product list in the inventory.
	 */
	public List<Product> getProductList() {
		return this.productService.getProducts();
	}
}
