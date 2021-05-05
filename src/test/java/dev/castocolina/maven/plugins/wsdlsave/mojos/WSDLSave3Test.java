package dev.castocolina.maven.plugins.wsdlsave.mojos;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

import java.io.File;

/**
 * @author castocolina.dev@gmail.com 00/00/0000
 */
public class WSDLSave3Test extends AbstractMojoTestCase {

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
    }

    protected void tearDown()
            throws Exception
    {
        // required
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testMojoGoal() throws Exception {

        File testPom = new File("target/test-classes","sample-pom3.xml");

        assertNotNull( testPom );
        assertTrue( testPom.exists() );

        WSDLSaveMojo mojo = (WSDLSaveMojo) lookupMojo("save", testPom);

        assertNotNull(mojo);

        mojo.execute();
    }

}
