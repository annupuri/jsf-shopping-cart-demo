/**
 * CartControllerTest.java
 */
package shopping.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static shopping.support.TestProducts.product;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import shopping.business.ProductService;
import shopping.business.generated.Product;
import shopping.integration.ProductDao;

class CartControllerTest {

	@TempDir
	File dataDir;

	private ProductDao dao;
	private CartController cart;

	@BeforeEach
	void setUp() throws Exception {
		this.dao = new ProductDao(dataDir);
		this.dao.saveProducts(Arrays.asList(product("A-1", "Alpha", "10.00", 10)));
		this.cart = new CartController(new ProductService(dao));
	}

	/** Populate {@code cart.product} and add it (the hidden-field entry point). */
	private void stage(String id, String price, int quantity) {
		final Product p = new Product();
		p.setId(id);
		p.setName(id);
		p.setPrice(new BigDecimal(price));
		p.setQuantity(quantity);
		cart.setProduct(p);
		cart.addProduct();
	}

	@Test
	void addToCartAddsOneUnitOfTheCatalogueProduct() {
		cart.addToCart(product("A-1", "Alpha", "10.00", 10));

		assertEquals(1, cart.getProductList().size());
		assertEquals(1, cart.getProductList().get(0).getQuantity());
		assertEquals(new BigDecimal("10.00"), cart.getProductList().get(0).getPrice());
		assertEquals(1, cart.getTotalQuantity());
		assertEquals(new BigDecimal("10.00"), cart.getTotalPrice());
	}

	@Test
	void addToCartAccumulatesRepeatedClicks() {
		final Product catalogueRow = product("A-1", "Alpha", "10.00", 10);
		cart.addToCart(catalogueRow);
		cart.addToCart(catalogueRow);

		assertEquals(1, cart.getProductList().size());
		assertEquals(2, cart.getProductList().get(0).getQuantity());
		assertEquals(new BigDecimal("20.00"), cart.getProductList().get(0).getPrice());
	}

	@Test
	void addToCartLeavesTheCatalogueRowUntouched() {
		final Product catalogueRow = product("A-1", "Alpha", "10.00", 10);
		cart.addToCart(catalogueRow);

		assertEquals(10, catalogueRow.getQuantity());
	}

	@Test
	void addProductAddsANewLine() {
		stage("A-1", "10.00", 2);

		assertEquals(1, cart.getProductList().size());
		assertEquals(new BigDecimal("10.00"), cart.getTotalPrice());
		assertEquals(2, cart.getTotalQuantity());
	}

	@Test
	void addProductAccumulatesTheSameId() {
		stage("A-1", "10.00", 2);
		stage("A-1", "10.00", 3);

		assertEquals(1, cart.getProductList().size());
		assertEquals(5, cart.getProductList().get(0).getQuantity());
		assertEquals(new BigDecimal("20.00"), cart.getProductList().get(0).getPrice());
	}

	@Test
	void addProductIgnoresABlankId() {
		stage("", "10.00", 1);

		assertTrue(cart.getProductList().isEmpty());
	}

	@Test
	void addProductKeepsSeparateLinesForDifferentIds() {
		stage("A-1", "10.00", 1);
		stage("A-2", "20.00", 1);

		assertEquals(2, cart.getProductList().size());
	}

	@Test
	void addProductToleratesANullIncomingPrice() {
		stage("A-1", "5.00", 1);

		final Product noPrice = new Product();
		noPrice.setId("A-1");
		noPrice.setName("A-1");
		noPrice.setQuantity(1);
		cart.setProduct(noPrice);
		cart.addProduct();

		assertEquals(1, cart.getProductList().size());
		assertEquals(new BigDecimal("5.00"), cart.getProductList().get(0).getPrice());
		assertEquals(2, cart.getProductList().get(0).getQuantity());
	}

	@Test
	void clearEmptiesTheCart() {
		stage("A-1", "10.00", 2);

		cart.clear();

		assertTrue(cart.getProductList().isEmpty());
		assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
		assertEquals(0, cart.getTotalQuantity());
		assertNotNull(cart.getProduct());
	}

	@Test
	void checkOutSellsTheCartedItemsAndClears() throws Exception {
		final Product catalogueRow = product("A-1", "Alpha", "10.00", 10);
		cart.addToCart(catalogueRow);
		cart.addToCart(catalogueRow);
		cart.addToCart(catalogueRow);

		cart.checkOut();

		assertEquals(7, dao.findProducts().get(0).getQuantity());
		assertTrue(cart.getProductList().isEmpty());
	}

	@Test
	void checkOutDoesNothingWhenTheCartIsEmpty() throws Exception {
		cart.checkOut();

		assertEquals(10, dao.findProducts().get(0).getQuantity());
	}

	@Test
	void checkOutPropagatesFailuresFromTheService() {
		final ProductService failing = new ProductService(dao) {
			@Override
			public void sellProducts(Map<String, Product> productMap) {
				throw new IllegalStateException("boom");
			}
		};
		final CartController failingCart = new CartController(failing);
		failingCart.addToCart(product("A-1", "Alpha", "10.00", 10));

		assertThrows(IllegalStateException.class, failingCart::checkOut);
	}

	@Test
	void noArgConstructorUsesTheSharedService() {
		assertNotNull(new CartController());
	}

	@Test
	void gettersAndSettersRoundTrip() {
		final Product p = product("Z-1", 1);
		cart.setProduct(p);
		assertEquals(p, cart.getProduct());

		final List<Product> list = new ArrayList<>();
		cart.setProductList(list);
		assertEquals(list, cart.getProductList());

		cart.setTotalPrice(new BigDecimal("3.50"));
		assertEquals(new BigDecimal("3.50"), cart.getTotalPrice());

		cart.setTotalQuantity(9);
		assertEquals(9, cart.getTotalQuantity());
	}
}
