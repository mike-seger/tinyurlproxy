package com.net128.app.tinyurl;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

@Service
public class ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

    private CloseableHttpClient httpClient=null;

    public void proxyRequest(HttpServletRequest request, HttpServletResponse response, String targetUri) throws IOException, URISyntaxException {
        HttpUriRequest proxiedRequest = createHttpUriRequest(request, new URI(targetUri));
        HttpResponse proxiedResponse = httpClient.execute(proxiedRequest);
        writeToResponse(proxiedResponse, response);
    }

    private void writeToResponse(HttpResponse proxiedResponse, HttpServletResponse response) throws IOException {
        for(Header header : proxiedResponse.getAllHeaders()){
            if ((! header.getName().equals("Transfer-Encoding")) || (! header.getValue().equals("chunked"))) {
                response.addHeader(header.getName(), header.getValue());
            }
        }

        try (InputStream is = proxiedResponse.getEntity().getContent();
             OutputStream os = response.getOutputStream()){
            IOUtils.copy(is, os);
        }
    }

    private HttpUriRequest createHttpUriRequest(HttpServletRequest request, URI targetUri) throws URISyntaxException{
        RequestBuilder rb = RequestBuilder.create(request.getMethod());
        rb.setUri(targetUri);

        String host=targetUri.getHost();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if("host".equalsIgnoreCase(headerName)) {
                headerValue=host;
            }
            rb.addHeader(headerName, headerValue);
            logger.debug("Header: {} = {}", headerName, headerValue);
        }

        HttpUriRequest proxiedRequest = rb.build();
        return proxiedRequest;
    }

    @PostConstruct
    private void init() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor();
        SSLContext sslcontext= SSLContexts.custom().loadTrustMaterial(null,
            new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
            new String[] { "TLSv1" , "TLSv1.1", "TLSv1.2" }, null, new NoopHostnameVerifier());
        httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        httpInvokerRequestExecutor.setHttpClient(httpClient);
    }
}
