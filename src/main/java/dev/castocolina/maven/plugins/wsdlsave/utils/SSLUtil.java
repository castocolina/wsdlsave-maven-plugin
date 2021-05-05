package dev.castocolina.maven.plugins.wsdlsave.utils;


import dev.castocolina.maven.plugins.wsdlsave.exceptions.SaveWSDLSSLException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @autor castocolina.dev@gmail.com on 00/00/0000
 */
public final class SSLUtil {

    public SSLUtil() {
        super();
    }

    public void trustSSLs() throws SaveWSDLSSLException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //Dejamos vacios, todos los servidores son validos
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                //Dejamos vacios, todos los clientes son validos
            }
        }};

        try {
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw new SaveWSDLSSLException("ERROR al instalar el trush manager", ex);
        }
    }

    /**
     * Este metodo hace que se agregue un verificador a las conexiones HTTPS el cual permite conexiones con sitios sin
     * validar si pertenece o no el certificado. <br/>
     * <br/>
     * <b>Author:</b> castocolina.dev@gmail.com
     */
    public void skipHostValidation() {
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
