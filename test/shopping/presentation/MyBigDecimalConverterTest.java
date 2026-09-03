/**
 * MyBigDecimalConverterTest.java
 */
package shopping.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Locale;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyBigDecimalConverterTest {

	private final MyBigDecimalConverter converter = new MyBigDecimalConverter();
	private final FacesContext facesContext = mock(FacesContext.class, RETURNS_DEEP_STUBS);
	private final UIComponent component = mock(UIComponent.class, RETURNS_DEEP_STUBS);

	@BeforeEach
	void stubLocale() {
		// The parent converter resolves an error-message bundle for this locale.
		when(facesContext.getViewRoot().getLocale()).thenReturn(Locale.US);
	}

	@Test
	void parsesAPlainNumber() {
		assertEquals(new BigDecimal("1234.5"),
				converter.getAsObject(facesContext, component, "1234.5"));
	}

	@Test
	void treatsNullAsZero() {
		assertEquals(BigDecimal.ZERO, converter.getAsObject(facesContext, component, null));
	}

	@Test
	void recoversFromGroupingCommasAndYenSign() {
		assertEquals(new BigDecimal("1234567"),
				converter.getAsObject(facesContext, component, "\\1,234,567"));
	}

	@Test
	void rethrowsWhenTheValueIsNotANumberAtAll() {
		assertThrows(RuntimeException.class,
				() -> converter.getAsObject(facesContext, component, "not a number"));
	}

	@Test
	void parseLenientStripsNoiseCharacters() {
		assertEquals(new BigDecimal("1234567"), MyBigDecimalConverter.parseLenient("\\1,234,567"));
		assertEquals(new BigDecimal("42"), MyBigDecimalConverter.parseLenient("  42  "));
	}

	@Test
	void parseLenientStillRejectsGarbage() {
		assertThrows(NumberFormatException.class, () -> MyBigDecimalConverter.parseLenient("abc"));
	}

	@Test
	void getAsStringDelegatesToTheParent() {
		assertEquals("5", converter.getAsString(facesContext, component, new BigDecimal("5")));
	}
}
