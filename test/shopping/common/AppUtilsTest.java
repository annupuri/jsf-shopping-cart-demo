/**
 * AppUtilsTest.java
 */
package shopping.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AppUtilsTest {

	@AfterEach
	void clearFacesContext() throws Exception {
		setCurrentFacesContext(null);
		System.clearProperty(AppConstants.DATA_DIR_PROPERTY);
	}

	private static void setCurrentFacesContext(FacesContext context) throws Exception {
		final Method m = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
		m.setAccessible(true);
		m.invoke(null, context);
	}

	@Test
	void getServletContextIsNullOutsideAContainer() {
		assertNull(AppUtils.getServletContext());
	}

	@Test
	void getServletContextResolvesThroughTheFacesContext() throws Exception {
		final ServletContext servletContext = mock(ServletContext.class);
		final ExternalContext externalContext = mock(ExternalContext.class);
		when(externalContext.getContext()).thenReturn(servletContext);
		final FacesContext facesContext = mock(FacesContext.class);
		when(facesContext.getExternalContext()).thenReturn(externalContext);
		setCurrentFacesContext(facesContext);

		assertEquals(servletContext, AppUtils.getServletContext());
	}

	@Test
	void getServletContextIsNullWhenTheExternalContextIsMissing() throws Exception {
		final FacesContext facesContext = mock(FacesContext.class);
		when(facesContext.getExternalContext()).thenReturn(null);
		setCurrentFacesContext(facesContext);

		assertNull(AppUtils.getServletContext());
	}

	@Test
	void getServletContextIsNullWhenTheContainerContextIsMissing() throws Exception {
		final ExternalContext externalContext = mock(ExternalContext.class);
		when(externalContext.getContext()).thenReturn(null);
		final FacesContext facesContext = mock(FacesContext.class);
		when(facesContext.getExternalContext()).thenReturn(externalContext);
		setCurrentFacesContext(facesContext);

		assertNull(AppUtils.getServletContext());
	}

	@Test
	void getServletContextSwallowsErrors() throws Exception {
		final FacesContext facesContext = mock(FacesContext.class);
		when(facesContext.getExternalContext()).thenThrow(new IllegalStateException("boom"));
		setCurrentFacesContext(facesContext);

		assertNull(AppUtils.getServletContext());
	}

	@Test
	void getCurrentClassLoaderIsNotNull() {
		assertNotNull(AppUtils.getCurrentClassLoader());
	}

	@Test
	void getClassRootPathFallsBackToTheClasspathRoot() {
		final String path = AppUtils.getClassRootPath();
		assertNotNull(path);
		assertTrue(path.endsWith("classes/") || path.endsWith("classes"));
	}

	@Test
	void getClassRootPathUsesTheServletContextWhenDeployed() throws Exception {
		final ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getRealPath("/WEB-INF/classes")).thenReturn("/opt/app/WEB-INF/classes");
		final ExternalContext externalContext = mock(ExternalContext.class);
		when(externalContext.getContext()).thenReturn(servletContext);
		final FacesContext facesContext = mock(FacesContext.class);
		when(facesContext.getExternalContext()).thenReturn(externalContext);
		setCurrentFacesContext(facesContext);

		assertEquals("/opt/app/WEB-INF/classes", AppUtils.getClassRootPath());
	}

	@Test
	void getDataDirPathDefaultsToTheConstant() {
		assertTrue(AppUtils.getDataDirPath().endsWith("product_db"));
	}

	@Test
	void getDataDirPathHonoursTheSystemProperty() {
		System.setProperty(AppConstants.DATA_DIR_PROPERTY, "/var/data/products");
		assertEquals("/var/data/products", AppUtils.getDataDirPath());
	}
}
