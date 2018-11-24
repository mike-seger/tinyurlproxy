package com.net128.app.tinyurl;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

@Service
public class ProxyService {

    public void proxyRequest(HttpServletRequest request, HttpServletResponse response, URI targetUri) throws IOException, URISyntaxException {
            HttpUriRequest proxiedRequest = createHttpUriRequest(request, targetUri);
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpResponse proxiedResponse = httpclient.execute(proxiedRequest);
                writeToResponse(proxiedResponse, response);
            }
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

        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            rb.addHeader(headerName, headerValue);
        }

        HttpUriRequest proxiedRequest = rb.build();
        return proxiedRequest;
    }
}
