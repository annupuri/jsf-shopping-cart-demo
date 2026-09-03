/**
 * ProductServiceTest.java
 */
package shopping.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static shopping.support.TestProducts.product;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import shopping.business.generated.Product;
import shopping.integration.ProductDao;

class ProductServiceTest {

	@TempDir
	File dataDir;

	private ProductDao dao;
	private ProductService service;

	@BeforeEach
	void setUp() throws Exception {
		this.dao = new ProductDao(dataDir);
		this.dao.saveProducts(Arrays.asList(
				product("A-1", "Alpha", "10.00", 10),
				product("A-2", "Beta", "20.00", 5)));
		this.service = new ProductService(dao);
	}

	private static Map<String, Product> map(Product... products) {
		final Map<String, Product> m = new HashMap<>();
		for (Product p : products) {
			m.put(p.getId(), p);
		}
		return m;
	}

	@Test
	void getProductsReturnsTheStoredCatalogue() {
		final List<Product> products = service.getProducts();
		assertEquals(2, products.size());
	}

	@Test
	void sharedInstanceIsAvailable() {
		assertNotNull(ProductService.getInstance());
	}

	@Test
	void buyAddsQuantityToAnExistingProduct() throws Exception {
		service.buyProducts(map(product("A-1", 4)));

		assertEquals(14, reload("A-1").getQuantity());
		assertEquals(2, service.getProducts().size());
	}

	@Test
	void buyAddsABrandNewProduct() throws Exception {
		service.buyProducts(map(product("A-9", 3)));

		assertEquals(3, service.getProducts().size());
		assertEquals(3, reload("A-9").getQuantity());
	}

	@Test
	void buyIgnoresAnEmptyMap() {
		service.buyProducts(Collections.emptyMap());
		assertEquals(2, service.getProducts().size());
	}

	@Test
	void buyStartsFromScratchWhenTheStoreIsEmpty() throws Exception {
		dao.saveProducts(Collections.emptyList());

		service.buyProducts(map(product("A-1", 2)));

		assertEquals(1, service.getProducts().size());
	}

	@Test
	void sellReducesTheQuantity() throws Exception {
		service.sellProducts(map(product("A-1", 3)));

		assertEquals(7, reload("A-1").getQuantity());
		assertEquals(2, service.getProducts().size());
	}

	@Test
	void sellRemovesAProductWhenTheStockIsExhausted() throws Exception {
		service.sellProducts(map(product("A-1", 10)));

		assertEquals(1, service.getProducts().size());
	}

	@Test
	void sellIgnoresAnEmptyMap() {
		service.sellProducts(Collections.emptyMap());
		assertEquals(2, service.getProducts().size());
	}

	@Test
	void sellLeavesTheCatalogueAloneForUnknownIds() {
		service.sellProducts(map(product("Z-9", 1)));

		assertEquals(2, service.getProducts().size());
		assertEquals(10, service.getProducts().get(0).getQuantity());
	}

	@Test
	void sellIgnoresEverythingWhenTheStoreIsEmpty() throws Exception {
		dao.saveProducts(Collections.emptyList());

		service.sellProducts(map(product("A-1", 1)));

		assertTrue(service.getProducts().isEmpty());
	}

	@Test
	void wrapsLoadFailuresFromTheDao() {
		final ProductService faulty = new ProductService(new ProductDao(dataDir) {
			@Override
			public List<Product> findProducts() throws JAXBException {
				throw new JAXBException("boom");
			}
		});

		assertThrows(RuntimeException.class, faulty::getProducts);
		assertThrows(RuntimeException.class, () -> faulty.sellProducts(map(product("A-1", 1))));
		assertThrows(RuntimeException.class, () -> faulty.buyProducts(map(product("A-1", 1))));
	}

	@Test
	void rethrowsRuntimeFailuresFromTheDao() {
		final ProductService faulty = new ProductService(new ProductDao(dataDir) {
			@Override
			public List<Product> findProducts() {
				throw new IllegalStateException("boom");
			}
		});

		assertThrows(IllegalStateException.class, () -> faulty.sellProducts(map(product("A-1", 1))));
		assertThrows(IllegalStateException.class, () -> faulty.buyProducts(map(product("A-1", 1))));
	}

	private Product reload(String id) throws Exception {
		for (Product p : dao.findProducts()) {
			if (id.equals(p.getId())) {
				return p;
			}
		}
		throw new AssertionError("product not found: " + id);
	}
}
