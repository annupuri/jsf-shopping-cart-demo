/**
 * ProductDaoTest.java
 */
package shopping.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static shopping.support.TestProducts.product;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import shopping.business.generated.Product;

class ProductDaoTest {

	@TempDir
	File dataDir;

	@Test
	void savesThenReadsBackTheSameProducts() throws Exception {
		final ProductDao dao = new ProductDao(dataDir);
		dao.saveProducts(java.util.Arrays.asList(
				product("P-1", "Widget", "12.34", 7),
				product("P-2", "Gadget", "0.50", 0)));

		assertTrue(new File(dataDir, "Products.xml").isFile());

		final List<Product> products = dao.findProducts();
		assertEquals(2, products.size());
		assertEquals("P-1", products.get(0).getId());
		assertEquals(7, products.get(0).getQuantity());
		assertEquals("Gadget", products.get(1).getName());
	}

	@Test
	void savingAnEmptyListDoesNotFail() throws Exception {
		final ProductDao dao = new ProductDao(dataDir);
		dao.saveProducts(Collections.emptyList());
		dao.saveProducts(null);

		final List<Product> products = dao.findProducts();
		assertTrue(products == null || products.isEmpty());
	}

	@Test
	void createsTheDataDirectoryWhenMissing() throws Exception {
		final File nested = new File(dataDir, "does/not/exist/yet");
		final ProductDao dao = new ProductDao(nested);

		dao.saveProducts(java.util.Arrays.asList(product("P-1", 1)));

		assertTrue(new File(nested, "Products.xml").isFile());
	}

	@Test
	void findProductsIsEmptyWhenTheFileIsMissing() throws Exception {
		final ProductDao dao = new ProductDao(dataDir);
		assertTrue(dao.findProducts().isEmpty());
	}

	@Test
	void sharedInstanceIsAvailable() {
		assertNotNull(ProductDao.getInstance());
	}
}
