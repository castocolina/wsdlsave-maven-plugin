package dev.castocolina.maven.plugins.wsdlsave.utils;

import dev.castocolina.maven.plugins.wsdlsave.exceptions.SaveWSDLException;
import dev.castocolina.maven.plugins.wsdlsave.exceptions.SaveWSDLParserException;
import dev.castocolina.maven.plugins.wsdlsave.exceptions.SaveWSDLReadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor castocolina.dev@gmail.com on 00/00/0000
 */
public final class FileUtil {

    private static final String INDENT_YES = "yes";
    private static final String EMPTY_STR = "";
    private static final String IMPORT = "import";
    private static final String INCLUDE = "include";
    private static final String XSD_LOCATION = "schemaLocation";
    private static final String WSDL_LOCATION = "location";
    private static final String EXT_XSD = ".xsd";
    private static final String EXT_WSDL = ".wsdl";
    private static final String WSDL = "_wsdl";
    private static final String XSD = "_xsd";

    private Map<String, String> mapUrlFile = new HashMap<>();

    private Log log;

    private boolean indent;

    public FileUtil(Log log, boolean indent) {
        this.log = log;
        this.indent = indent;
    }

    public Document importLocation(Document doc, File parentDir, URL contextUrl)
            throws SaveWSDLException {
        // Get a list of all elements in the document
        NodeList list = doc.getElementsByTagName("*");
        // recursive call when imported file has import
        for (int i = 0; i < list.getLength(); i++) {
            // Get element
            Element element = (Element) list.item(i);
            if (!importDefinition(element) || LocationType.INVALID.equals(getLocation(element).getType())) {
                continue;
            }

            Location location = getLocation(element);

            URL url = getLocationURL(location.getStrLocation(), contextUrl);
            File importFile = urlNameToFileName(url, location, parentDir);

            if (!importFile.exists()) {
                log.info("guardando definicion desde:" + url.toExternalForm());
                log.info("Hasta : " + importFile.getAbsolutePath());
                saveURL(url, importFile);
                Document importDocument = parseXmlFile(importFile, false);
                // recursive, find imports in the loaded document
                importDocument = importLocation(importDocument, parentDir, url);
                saveDom(importFile, importDocument);
            }

            log.info("Actualizacion del atributo import location/schemaLocation en WSDL/XSD a >> " +
                    importFile.getName());

            String attrName = WSDL_LOCATION;
            if (LocationType.XSD.equals(location.getType())) {
                attrName = XSD_LOCATION;
            }
            element.setAttribute(attrName, importFile.getName());


        }
        return doc;
    }

    public File urlNameToFileName(URL url, Location location, File parentDir) throws SaveWSDLException {
        log.debug("URL 2 FILE ::::::: ");
        File importFile;
        try {
            String urlFileStr = java.net.URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());

            String urlBaseFileStr = urlFileStr.substring(urlFileStr.lastIndexOf('/') + 1, urlFileStr.length());
            String extension = EXT_WSDL;
            if (LocationType.XSD.equals(location.getType())) {
                extension = EXT_XSD;
            }

            String normalizedFileName = normalizeFileName(urlBaseFileStr);
            log.debug("FULL URL : " + url.toString());
            log.debug("URL FILE: " + urlFileStr);
            log.debug("PATH FILE NAME: " + normalizedFileName);

            String importFileName = normalizedFileName + extension;
            importFile = new File(parentDir, importFileName);

            //check file exist and && add number before .extension
            if (importFile.exists()) {
                importFile = renameIfExist(urlFileStr, importFile);
            }
            mapUrlFile.put(urlFileStr, importFile.getName());
        } catch (UnsupportedEncodingException e) {
            throw new SaveWSDLException(e.getMessage(), e);
        }
        return importFile;
    }

    public File renameIfExist(String urlFileStr, File file) {
        File newFile;
        File baseDir = file.getParentFile();

        if (mapUrlFile.get(urlFileStr) == null) {

            String baseName = FilenameUtils.getBaseName(file.getAbsolutePath());
            String extension = FilenameUtils.getExtension(file.getAbsolutePath());

            int num = 1;
            String newName = baseName + "_" + num + "." + extension;
            newFile = new File(baseDir, newName);
            while (newFile.exists()) {
                newName = baseName + "_" + (num++) + "." + extension;
                newFile = new File(baseDir, newName);
            }

            mapUrlFile.put(urlFileStr, newFile.getName());
        } else {
            String fileName = mapUrlFile.get(urlFileStr);
            log.warn("LA ruta [" + urlFileStr + "] ya existe en >> " + fileName);
            newFile = new File(baseDir, fileName);
        }

        return newFile;
    }

    public boolean importDefinition(Element element) throws SaveWSDLReadException {
        boolean importDef = false;

        if (element.getTagName().endsWith(IMPORT) || element.getTagName().endsWith(INCLUDE)) {
            importDef = true;
        }
        return importDef;
    }

    public Location getLocation(Element element) throws SaveWSDLReadException {
        Location location = new Location();

        String strLocation = element.getAttribute(XSD_LOCATION);
        if (strLocation != null && !EMPTY_STR.equals(strLocation)) {
            location.setStrLocation(strLocation);
            location.setType(LocationType.XSD);
        } else {
            strLocation = element.getAttribute(WSDL_LOCATION);
            if (strLocation != null || !EMPTY_STR.equals(strLocation)) {
                location.setStrLocation(strLocation);
                location.setType(LocationType.WSDL);
            }
        }

        return location;
    }

    public URL getLocationURL(String location, URL contextUrl) throws SaveWSDLReadException {
        URL url;
        try {
            if (contextUrl == null) {
                url = new URL(location);
            } else {
                url = new URL(contextUrl, location);
            }
        } catch (MalformedURLException ex) {
            throw new SaveWSDLReadException("Problemas al leer la URL " + contextUrl + " / " + location, ex);
        }
        return url;
    }

    /**
     * Reemplazar caracteres distintos a letras y numeros por underscore (-). Author: castocolina@lynqus.net
     *
     * @param name
     * @return java.lang.String
     */
    public String normalizeFileName(String name) {
        String normalizedFileName;
        StringBuilder normalized = new StringBuilder(EMPTY_STR);
        char[] namec = name.toCharArray();
        for (char ch : namec) {
            if (Character.isLetterOrDigit(ch)) {
                normalized.append(ch);
            } else {
                normalized.append('_');
            }
        }

        normalizedFileName = normalized.toString().toLowerCase();
        if (normalizedFileName.indexOf(WSDL) != -1) {
            normalizedFileName = normalizedFileName.replaceAll(WSDL, EMPTY_STR);
        } else if (normalizedFileName.indexOf(XSD) != -1) {
            normalizedFileName = normalizedFileName.replaceAll(XSD, EMPTY_STR);
        }

        return normalizedFileName;
    }

    /**
     * Guardar el contenido de una URL a un archivo. <br/>
     * <br/>
         * <b>Author:</b> castocolina.dev@gmail.com
     *
     * @param url
     * @param fileOut
     * @throws IOException
     */
    public void saveURL(URL url, File fileOut) throws SaveWSDLReadException {

        try {

            BufferedWriter out = new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                if (EMPTY_STR.equals(str)) {
                    continue;
                }
                out.write(str);
                out.newLine();
            }
            in.close();
            out.close();
        } catch (IOException ioe) {
            throw new SaveWSDLReadException("Error al guardar " + url + " a >> " + fileOut, ioe);
        }
    }

    /**
     * Parses an XML file and returns a DOM document. If validating is true, the contents is validated against the DTD
     * specified in the file. <br/>
     * <br/>
     * <b>Author:</b> castocolina.dev@gmail.com
     *
     * @param file
     * @param validating
     * @return
     * @throws SaveWSDLParserException
     */
    public Document parseXmlFile(File file, boolean validating) throws SaveWSDLParserException {
        Document doc;
        try {
            log.debug("CARGANDO [validacion = " + validating + "]" + file.getAbsolutePath());
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);

            // Create the builder and parse the file
            doc = factory.newDocumentBuilder().parse(file);
        } catch (SAXException | ParserConfigurationException e) {
            throw new SaveWSDLParserException("Error parseando archivo", e);
        } catch (IOException e) {
            throw new SaveWSDLParserException("Error leyendo documento " + file.getAbsolutePath(), e);
        }

        return doc;
    }

    /**
     * <br/>
     * <br/>
     * <b>Author:</b> castocolina.dev@gmail.com
     *
     * @param outFile
     * @param doc
     * @throws SaveWSDLParserException
     */
    public void saveDom(File outFile, Node doc) throws SaveWSDLParserException {

        try {

            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            log.debug("save file --> " + outFile.getAbsolutePath());
            log.debug("\t" + transformer.getClass().getCanonicalName() + "\t indent == " + indent);

            if (indent) {
                transformer.setOutputProperty(OutputKeys.INDENT, INDENT_YES);
            }

            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(outFile);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

        } catch (TransformerException e) {
            throw new SaveWSDLParserException("Problemas guardando el archivo xml", e);
        }
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all deletions were successful. If a deletion
     * fails, the method stops attempting to delete and returns false. <br/>
     * <br/>
     * <b>Author:</b> castocolina.dev@gmail.com
     *
     * @param dir
     * @return
     */
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return false;
            }
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    public enum LocationType {
        INVALID,
        XSD,
        WSDL;
    }

    public static class Location {
        private LocationType type = LocationType.INVALID;
        private String strLocation;

        public LocationType getType() {
            return type;
        }

        public void setType(LocationType type) {
            this.type = type;
        }

        public String getStrLocation() {
            return strLocation;
        }

        public void setStrLocation(String strLocation) {
            this.strLocation = strLocation;
        }

        @Override
        public String toString() {
            final StringBuilder sb =
                    new StringBuilder("FileUtil.Location{");
            sb.append("type=").append(type);
            sb.append(", strLocation='").append(strLocation).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
