package us.paulevans.basicxslt.test;

import junit.framework.TestCase;
import us.paulevans.basicxslt.LabelStringFactory;

/**
 * Test case for LabelStringFactory class
 * @author pevans
 *
 */
public class TestLabelStringFactory extends TestCase {
	
	// instance member...
	private LabelStringFactory labelStringFactory;

	/**
	 * Test fixture setup
	 */
	protected void setUp() throws Exception {
		labelStringFactory = LabelStringFactory.getInstance();
	}

	/**
	 * Test fixture for getString(String)
	 *
	 */
	public final void testGetString() {
		assertEquals("File", labelStringFactory.getString(
				LabelStringFactory.MF_FILE_MENU));
	}

	/**
	 * Test fixture for getMnemonic(String)
	 *
	 */
	public final void testGetMnemonic() {
		assertEquals('F', labelStringFactory.getMnemonic(
				LabelStringFactory.MF_FILE_MENU));
	}

	/**
	 * Test fixture for getInstance()
	 *
	 */
	public final void testGetInstance() {
		assertNotNull(labelStringFactory);
	}
}
