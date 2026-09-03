/**
 * AppUtils.java
 */
package shopping.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * Application utilities.
 */
public abstract class AppUtils implements AppConstants {
	private static final Logger log = LoggerFactory.getLogger(AppUtils.class);

	/**
	 * Constructor.
	 */
	private AppUtils() {
	}

	/**
	 * Get the servlet context.
	 * @return
	 * <ul>
	 * <li>not null while this is called on a servlet container like Tomcat.</li>
	 * <li>null while this is called on a non-servlet container.</li>
	 * </ul>
	 */
	public static ServletContext getServletContext() {
		try {
			final FacesContext facesContext = FacesContext.getCurrentInstance();
			if (facesContext == null) {
				log.debug("facesContext is null.");
				return null;
			}
			final ExternalContext externalContext = facesContext
					.getExternalContext();
			if (externalContext == null) {
				log.debug("externalContext is null.");
				return null;
			}
			final ServletContext servletContext = (ServletContext) externalContext
					.getContext();
			if (servletContext == null) {
				log.debug("servletContext is null.");
				return null;
			}
			return servletContext;
		} catch (Exception e) {
			// Best-effort lookup: any failure just means "no servlet context".
			log.warn("failed to resolve the servlet context: " + e);
			return null;
		}
	}

	public static ClassLoader getCurrentClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Get the absolute path of the root of class path.
	 * @return the suffix may be "classes".
	 */
	public static String getClassRootPath() {
		final ServletContext servletContext = AppUtils.getServletContext();
		if (servletContext != null) { // As a web application.
			return servletContext.getRealPath("/WEB-INF/classes");
		} else {
			return AppUtils.getCurrentClassLoader().getResource(".").getPath();
		}
	}

	/**
	 * Get the path of the XML data directory.
	 * <p>
	 * Defaults to {@link #DATA_DIR_PATH}; override with the
	 * {@code shopping.dataDir} system property ({@link #DATA_DIR_PROPERTY}).
	 * @return the data directory path.
	 */
	public static String getDataDirPath() {
		return System.getProperty(DATA_DIR_PROPERTY, DATA_DIR_PATH);
	}
}
