/**
 * AppConstants.java
 */
package shopping.common;

/**
 * Application constants.
 */
public interface AppConstants {
	/** Directory holding the products XML file, relative to the working directory. */
	String DATA_DIR_PATH = "product_db";

	/** System property that overrides {@link #DATA_DIR_PATH} at runtime. */
	String DATA_DIR_PROPERTY = "shopping.dataDir";
}
