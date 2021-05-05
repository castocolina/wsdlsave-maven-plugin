package dev.castocolina.maven.plugins.wsdlsave.mojos;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/** @author castocolina.dev@gmail.com 00/00/0000 */
public class WSDLSave1Test extends AbstractMojoTestCase {

  /** @see junit.framework.TestCase#setUp() */
  protected void setUp() throws Exception {
    // required for mojo lookups to work
    super.setUp();
  }

  protected void tearDown() throws Exception {
    // required
    super.tearDown();
  }

  /** @throws Exception */
  @Test
  public void testMojoGoal1() throws Exception {

    File testPom = new File("target/test-classes", "sample-pom1.xml");

    assertNotNull(testPom);
    assertTrue(testPom.exists());

    WSDLSaveMojo mojo = (WSDLSaveMojo) lookupMojo("update", testPom);

    assertNotNull(mojo);

    mojo.execute();

    mojo.getLog().info(FilenameUtils.getBaseName(testPom.getAbsolutePath()));
    mojo.getLog().info(FilenameUtils.getExtension(testPom.getAbsolutePath()));
    File target =
            new File("target/generated-test-sources/wsdl1", "BLZService.wsdl");
    Assert.assertTrue("Expectted target exist", target.exists());
  }

  /** @throws Exception */
  @Test
  public void testMojoGoal2() throws Exception {

    File testPom = new File("target/test-classes", "sample-pom1-2-multi_xsd.xml");

    assertNotNull(testPom);
    assertTrue(testPom.exists());

    WSDLSaveMojo mojo = (WSDLSaveMojo) lookupMojo("update", testPom);

    assertNotNull(mojo);

    mojo.execute();

    mojo.getLog().info(FilenameUtils.getBaseName(testPom.getAbsolutePath()));
    mojo.getLog().info(FilenameUtils.getExtension(testPom.getAbsolutePath()));

    File target =
            new File("target/generated-test-sources/wsdl1-2", "mainDef.wsdl");
    Assert.assertTrue("Expectted target exist", target.exists());

  }
}
