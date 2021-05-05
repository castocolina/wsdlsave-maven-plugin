package dev.castocolina.maven.plugins.wsdlsave.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import java.io.File;

/**
 * @author castocolina.dev@gmail.com 00/00/0000
 */
public class WSDLSave2ErrorsTest extends AbstractMojoTestCase {

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
    }

    protected void tearDown()
            throws Exception {
        // required
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testMojoGoal1() throws Exception {
        File testPom = new File("target/test-classes", "sample-pom2-1-error-out-project.xml");

        assertNotNull(testPom);
        assertTrue(testPom.exists());

        WSDLSaveMojo mojo = (WSDLSaveMojo) lookupMojo("save", testPom);

        assertNotNull(mojo);

        Exception ex = null;

        try {
            mojo.getLog().info("TEST 2.1");
            mojo.execute();
        } catch (MojoExecutionException | MojoFailureException e) {
            ex = e;
            mojo.getLog().error("ERROR !!! " + e.getMessage() + "  " + e.getLongMessage());
        }

        assertNotNull(ex);
        assertEquals(MojoExecutionException.class, ex.getClass());
    }


    /**
     * @throws Exception
     */
    @Test
    public void testMojoGoal2() throws Exception {
        File testPom = new File("target/test-classes", "sample-pom2-2-error-parent-path.xml");

        assertNotNull(testPom);
        assertTrue(testPom.exists());

        WSDLSaveMojo mojo = (WSDLSaveMojo) lookupMojo("save", testPom);

        assertNotNull(mojo);

        Exception ex = null;

        try {
            mojo.getLog().info("TEST 2.2");
            mojo.execute();
        } catch (MojoExecutionException | MojoFailureException e) {
            ex = e;
            mojo.getLog().error("ERROR !!! " + e.getMessage() + "  " + e.getLongMessage());
        }

        assertNotNull(ex);
        assertEquals(MojoExecutionException.class, ex.getClass());
    }

/**
     * @throws Exception
     */
    @Test
    public void testMojoGoal4() throws Exception {
        File testPom = new File("target/test-classes", "sample-pom2-4-error-pathfile.xml");
        assertNotNull(testPom);
        assertTrue(testPom.exists());

        WSDLSaveMojo mojo = (WSDLSaveMojo) lookupMojo("save", testPom);
        assertNotNull(mojo);

        Exception ex = null;

        try {
            mojo.getLog().info("TEST 2.4");
            mojo.execute();
        } catch (MojoExecutionException | MojoFailureException e) {
            ex = e;
            mojo.getLog().error("ERROR !!! " + e.getMessage() + "  " + e.getLongMessage());
        }

        assertNotNull(ex);
        assertEquals(MojoExecutionException.class, ex.getClass());
    }

}
