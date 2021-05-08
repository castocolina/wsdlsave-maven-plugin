package dev.castocolina.maven.plugins.wsdlsave.mojos;

import dev.castocolina.maven.plugins.wsdlsave.exceptions.SaveWSDLException;
import dev.castocolina.maven.plugins.wsdlsave.exceptions.SaveWSDLSSLException;
import dev.castocolina.maven.plugins.wsdlsave.utils.FileUtil;
import dev.castocolina.maven.plugins.wsdlsave.utils.SSLUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Este mojo se encarga de tomar una URL y guardar en una ruta indicada junto a todos los esquemas y WSDL relacionados.
 * <br/>
 * Se le pueden pasar algunas opciones, como:<br>
 * La carpeta raiz de los recursos. Por defecto src/main/resources/wsdl<br/>
 * La url del wsdl.<br/>
 * Se puede especificar si se desea eliminar el directorio previamente para borrar todos los archivos previos.<br/>
 * Se puede especififar se se desea omitir la validacion de un certificado digital en caso de que la url este
 * protegida con SSL.<br/>
 * Aun omitiendo un certificado digital cuando la URL esta con SSL se verifica el dominio. Esta verificacion tambien la
 * podremos omitir.<br/>
 * <br/>
 *
 * @author castocolina.dev@gmail.com 00/00/0000
 * @goal save
 * @phase generate-resources
 */
@Mojo( name = "update", defaultPhase = LifecyclePhase.GENERATE_RESOURCES )
public class WSDLSaveMojo extends AbstractMojo {

    private static final String SEPARATOR_SRT = "-----------------------------------";
    /**
     * URL del archivo wsdl a guardar.
     */
    @Parameter(property = "savewsdl.url", readonly = true)
    private URL wsdlUrl;

    /**
     * La carpeta donde se van a guardar los archivos.
     */
    @Parameter(defaultValue="src/main/resources/wsdl", property="savewsdl.resourceDir")
    private File resourceDir;

    /**
     * El nombre del archivo WSDL principal.
     */
    @Parameter(property="savewsdl.wsdlFileName", defaultValue="${artifactId}.wsdl", required = true)
    private String wsdlFileName;

    /**
     * Identar el contenido del wsdl/xsd.
     */
    @Parameter(property="savewsdl.indentContent", defaultValue="false")
    private boolean indentContent = false;

    /**
     * Especificar si se quiere eliminar previamente el directorio donde se colocara el WSDL.
     */
    @Parameter(property="savewsdl.cleanDir", defaultValue="false")
    private boolean cleanDir = false;

    /**
     * Tomar las URLs bajo SSL como validas asi no lo sean.
     */
    @Parameter(property="savewsdl.trustSSL", defaultValue="false")
    private boolean trustSSL = false;

    /**
     * Omitir la validacion de dominio en los certificados digitales de las URLs bajo SSL.
     */
    @Parameter(property="savewsdl.skipHostCheck", defaultValue="false")
    private boolean skipHostCheck = false;

    /**
     * la ruta base del proyecto. Se usa para verificar la ruta donde se colocara el archivo este dentro del proyecto
     */
    @Parameter(property="savewsdl.basedir", required = true, readonly = true)
    private File baseDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        FileUtil fUtil = new FileUtil(getLog(), indentContent);

        getLog().info(SEPARATOR_SRT);
        getLog().info(SEPARATOR_SRT);
        getLog().info(this.getClass().getSimpleName());

        File wsdlFileOut = getFileOut();
        File parentDir = wsdlFileOut.getParentFile();

        getLog().info("----> " + wsdlFileOut.getAbsolutePath());

        try {
            tryDelete(parentDir);

            if (wsdlFileOut.exists()) {
                wsdlFileOut = fUtil.renameIfExist(wsdlFileName, wsdlFileOut);
            }

            fileChecks(parentDir);

            if (!parentDir.exists()) {
                getLog().warn("No existe " + parentDir.getAbsolutePath());
                boolean created = parentDir.mkdirs();
                getLog().warn("Directorio creado >>> " + created + " --- " + resourceDir.getAbsolutePath());
            }

            getLog().info("WSDL::: \n" + wsdlUrl);
            sslConfig();

            getLog().info("saving wsdl:\n " + wsdlUrl + "\n to:\n " + wsdlFileOut.getCanonicalPath());
            fUtil.saveURL(wsdlUrl, wsdlFileOut);

            // Obtain an DOM XML document
            getLog().info("load wsdl file for modifications");
            Document doc = fUtil.parseXmlFile(wsdlFileOut, false);
            getLog().info("find wsdl/xsd imports for save.");
            doc = fUtil.importLocation(doc, parentDir, wsdlUrl);
            fUtil.saveDom(wsdlFileOut, doc);
            getLog().info("save WSDL file DONE!");

        } catch (IOException e) {
            throw new MojoExecutionException("ERROR DE ARCHIVO", e);
        } catch (SaveWSDLException e) {
            throw new MojoExecutionException("Error al obtener el wsdl", e);
        }
        getLog().info(SEPARATOR_SRT);
        getLog().info(SEPARATOR_SRT);
    }

    public void tryDelete(File parentDir) throws IOException {
        FileUtil fUtil = new FileUtil(getLog(), indentContent);
        if (cleanDir && parentDir.exists()) {
            getLog().info("cleaning dir: " + parentDir.getCanonicalPath());
            boolean candelete = fUtil.deleteDir(parentDir);
            getLog().info("DIR_CLEANED: " + candelete);
            if (!candelete) {
                getLog().warn("No se pudo eliminar el directorio!");
            }
        }
    }

    public void sslConfig() throws SaveWSDLSSLException {
        SSLUtil sslUtil = new SSLUtil();

        if (trustSSL) {
            getLog().warn("Confiando en todas las conexiones SSL");
            sslUtil.trustSSLs();
        }
        if (skipHostCheck) {
            getLog().warn("Omitiendo la validacion de host para conexiones SSL");
            sslUtil.skipHostValidation();
        }

    }

    public void fileChecks(File parentDir) throws MojoExecutionException {

        if (resourceDir.exists() && resourceDir.isFile()) {
            throw new MojoExecutionException("La destino no es un directorio >>" + baseDir.getAbsolutePath());
        }

        try {

            // validate final file keep into project dir
            if (!parentDir.getCanonicalPath().contains(baseDir.getCanonicalPath())) {
                getLog().error("TARGET: " + parentDir.getCanonicalPath());
                getLog().error("ROOT: " + baseDir.getCanonicalPath());
                throw new MojoExecutionException("El directorio destino está fuera del proyecto");
            }

            if (parentDir.getCanonicalPath().equals(baseDir.getCanonicalPath())) {
                getLog().error("TARGET: " + parentDir.getCanonicalPath());
                getLog().error("ROOT: " + baseDir.getCanonicalPath());
                throw new MojoExecutionException("El directorio destino no puede ser la raiz del proyecto");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("ERROR DE ARCHIVO", e);
        }

    }

    public File getFileOut() {
        return new File(resourceDir, wsdlFileName);
    }
}
