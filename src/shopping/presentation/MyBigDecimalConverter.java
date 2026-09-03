/**
 * MyBigDecimalConverter.java
 */
package shopping.presentation;

import java.math.BigDecimal;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.ConverterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Custom BigDecimal converter which has smarter conversion logic
 * than the parent class.
 */
public class MyBigDecimalConverter extends BigDecimalConverter {
	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Constructor.
	 */
	public MyBigDecimalConverter() {
	}

	@Override
	public Object getAsObject(FacesContext facesContext,
			UIComponent uiComponent, String value) {
		try {
			final Object number = super.getAsObject(facesContext, uiComponent, value);
			return number != null ? number : BigDecimal.ZERO;
		} catch (ConverterException e) {
			log.debug("standard conversion rejected \"" + value + "\", retrying leniently");
			try {
				return parseLenient(value);
			} catch (NumberFormatException nfe) {
				log.warn("cannot convert to BigDecimal: \"" + value + "\"");
				throw e;
			}
		}
	}

	/**
	 * Parses a decimal after stripping the characters the standard converter
	 * rejects: a leading {@code \} (yen sign on Japanese keyboards) and grouping
	 * commas.
	 *
	 * @param value
	 *            the raw input.
	 * @return the parsed value.
	 * @throws NumberFormatException
	 *             if it still cannot be parsed.
	 */
	static BigDecimal parseLenient(String value) {
		return new BigDecimal(value.replace("\\", "").replace(",", "").trim());
	}

	@Override
	public String getAsString(FacesContext facesContext,
			UIComponent uiComponent, Object value) {
		return super.getAsString(facesContext, uiComponent, value);
	}
}
