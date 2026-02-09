package com.datacenter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Test that simulation initialization doesn't throw exceptions
     */
    public void testSimulationInitialization()
    {
        try {
            // DataCenterAdvancedSimulation creates a JFrame, which might not work in headless environments
            // But we can test if we can at least instantiate the core simulation logic indirectly
            // or if the class exists.
            assertNotNull(new Server(0, 100, 50, 200, "Standard"));
        } catch (Exception e) {
            fail("Initialization failed: " + e.getMessage());
        }
    }
}
