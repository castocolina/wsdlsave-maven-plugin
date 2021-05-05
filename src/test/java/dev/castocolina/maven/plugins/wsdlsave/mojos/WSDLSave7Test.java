package dev.castocolina.maven.plugins.wsdlsave.mojos;

import dev.castocolina.maven.plugins.wsdlsave.utils.FileUtil;
import dev.castocolina.maven.plugins.wsdlsave.utils.FileUtil.Location;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * @author castocolina.dev@gmail.com 00/00/0000
 */
public class WSDLSave7Test extends AbstractMojoTestCase {

    private Log log;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();
        log = new SystemStreamLog();
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
    public void testMojoGoal7_1() throws Exception {
        FileUtil fUtil = new FileUtil(log, false);

        fUtil.normalizeFileName("_papa_xsd");
        fUtil.normalizeFileName("_papa_wsdl");

    }

    /**
     * @throws Exception
     */
    @Test
    public void testMojoGoal7_2() throws Exception {
        FileUtil fUtil = new FileUtil(log, false);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<WL5G3N0:definitions xmlns:WL5G3N0=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:WL5G3N1=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMonedaDiaRq/mpi\" xmlns:WL5G3N10=\"http://osb.castocolina.dev/ent/bch/neg/mci/cabeceraServicios/v/4\" xmlns:WL5G3N11=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMoneda\" xmlns:WL5G3N12=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:WL5G3N2=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMonedaDiaRs/mpi\" xmlns:WL5G3N3=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFDiaRq/mpi\" xmlns:WL5G3N4=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFDiaRs/mpi\" xmlns:WL5G3N5=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFMesRq/mpi\" xmlns:WL5G3N6=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFMesRs/mpi\" xmlns:WL5G3N7=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUTMRq/mpi\" xmlns:WL5G3N8=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUTMRs/mpi\" xmlns:WL5G3N9=\"http://osb.castocolina.dev/ent/bch/infra/mci/errorDetails/v/6\" name=\"CTR.000701_1.0.ConsultaValorMoneda\" targetNamespace=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMoneda\">\n" +
                "  <WL5G3N0:types>\n" +
                "    <xsd:schema xmlns:error=\"http://osb.castocolina.dev/ent/bch/infra/mci/errorDetails/v/6\" xmlns:head=\"http://osb.castocolina.dev/ent/bch/neg/mci/cabeceraServicios/v/4\" xmlns:inp1=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMonedaDiaRq/mpi\" xmlns:inp2=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFDiaRq/mpi\" xmlns:inp3=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFMesRq/mpi\" xmlns:inp4=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUTMRq/mpi\" xmlns:out1=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMonedaDiaRs/mpi\" xmlns:out2=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFDiaRs/mpi\" xmlns:out3=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUFMesRs/mpi\" xmlns:out4=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorUTMRs/mpi\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMoneda\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "      <xsd:import namespace=\"http://osb.castocolina.dev/bch/neg/sgt/consultaValorMonedaDiaRq/mpi\" " +
                "location=\"1_schema_2fsrv_bch_neg_sgt_2fsrv_000701_1_0_consultavalormoneda_2foperations_2fconsultavalormonedadia_2fspecifications_2fmsg_000701_1_0_consultavalormonedadiarq.xsd\"/>" +
                "</xsd:schema>\n" +
                "  </WL5G3N0:types>" +
                "</WL5G3N0:definitions>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        NodeList list = doc.getElementsByTagName("*");
        // recursive call when imported file has import
        for (int i = 0; i < list.getLength(); i++) {

            Element element = (Element) list.item(i);
            if (!fUtil.importDefinition(element)) {
                continue;
            }

            Location location = fUtil.getLocation(element);
            log.debug(location.toString());
        }
    }

}
