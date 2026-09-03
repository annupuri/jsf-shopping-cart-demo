package shopping.support;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import shopping.business.generated.Product;

/**
 * Factory helpers for building {@link Product} fixtures in tests.
 */
public final class TestProducts {

	private TestProducts() {
	}

	public static Product product(String id, String name, String price, int quantity) {
		final Product p = new Product();
		p.setId(id);
		p.setName(name);
		p.setPrice(new BigDecimal(price));
		p.setQuantity(quantity);
		p.setDescription(name + " description");
		return p;
	}

	public static Product product(String id, int quantity) {
		return product(id, id, "1.00", quantity);
	}

	public static List<Product> catalogue() {
		return Arrays.asList(product("A-1", "Alpha", "10.00", 10),
				product("A-2", "Beta", "20.00", 5));
	}
}
