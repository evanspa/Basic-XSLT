package us.paulevans.basicxslt.test;

import junit.framework.TestCase;

/**
 * Test case for Error class
 * @author PXE123
 *
 */
public class TestError extends TestCase {
	
	// instance member...
	private us.paulevans.basicxslt.Error error;
	
	/**
	 * Test fixture setup
	 */
	public void setUp() {
		error = new us.paulevans.basicxslt.Error(10, 25, "this is a test");
	}
	
	/**
	 * Test fixture for getColumn
	 *
	 */
	public void testGetColumn() {
		assertEquals(10, error.getColumn());		
	}
	
	/**
	 * Test fixture for getRow
	 *
	 */
	public void testGetRow() {
		assertEquals(25, error.getLine());		
	}
	
	/**
	 * Test fixture for getMessage
	 *
	 */
	public void testGetMessage() {
		assertEquals("this is a test", error.getMessage());
	}
}
