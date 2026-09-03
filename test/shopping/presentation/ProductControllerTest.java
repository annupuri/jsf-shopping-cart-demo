/**
 * ProductControllerTest.java
 */
package shopping.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import shopping.business.ProductService;
import shopping.business.generated.Product;
import shopping.integration.ProductDao;
import shopping.support.TestProducts;

public class ProductControllerTest {

	@Test
	void getProductListDelegatesToTheService(@TempDir File dataDir) throws Exception {
		final ProductDao dao = new ProductDao(dataDir);
		dao.saveProducts(TestProducts.catalogue());
		final ProductController controller = new ProductController(new ProductService(dao));

		final List<Product> products = controller.getProductList();

		assertEquals(2, products.size());
	}

	@Test
	void noArgConstructorUsesTheSharedService() {
		assertNotNull(new ProductController());
	}
}
