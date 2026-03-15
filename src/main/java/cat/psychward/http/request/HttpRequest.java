package cat.psychward.http.request;

import cat.psychward.http.response.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class HttpRequest implements AutoCloseable {

    private static final List<String> RESPONSE_METHODS = Arrays.asList("GET", "HEAD", "OPTIONS", "DELETE");
    private static final List<String> BODY_METHODS = Arrays.asList("POST", "PUT", "PATCH");

    private final int readTimeout, connectTimeout;
    private final HttpURLConnection connection;
    private final boolean followRedirects;
    private final String method;
    private byte[] content;

    public HttpRequest(URL url, Proxy proxy, String method, boolean followRedirects, int readTimeout, int connectTimeout) throws IOException {
        if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) {
            this.connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + url.getProtocol());
        }

        this.followRedirects = followRedirects;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.method = method;
    }

    public HttpResponse execute() throws IOException {
        this.connection.setInstanceFollowRedirects(this.followRedirects);

        if (RESPONSE_METHODS.contains(this.method))
            this.connection.setDoInput(true);

        if (BODY_METHODS.contains(this.method)) {
            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
        }

        this.connection.setConnectTimeout(this.connectTimeout);
        this.connection.setReadTimeout(this.readTimeout);

        this.connection.setRequestMethod(this.method);
        this.connection.connect();

        if (this.connection.getDoOutput()) {
            try (OutputStream outputStream = this.connection.getOutputStream()) {
                outputStream.write(this.content);
                outputStream.flush();
            }
        }

        return HttpResponse.of(this.connection);
    }

    @Override
    public void close() {
        if (this.connection != null)
            this.connection.disconnect();
    }

    public void addHeader(String name, String value) {
        this.connection.addRequestProperty(name, value);
    }

    public void setHeader(String name, String value) {
        this.connection.setRequestProperty(name, value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String url) throws MalformedURLException, URISyntaxException {
        return builder().url(url);
    }

    public static class Builder {
        private static final List<String> ALLOWED_METHODS = Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "HEAD", "PATCH", "OPTIONS", "TRACE", "CONNECT"
        );

        private final Map<String, String> headers = new HashMap<>(), query = new HashMap<>();
        private final List<Map.Entry<String, String>> multiHeaders = new ArrayList<>();
        private boolean followRedirects = true;
        private int readTimeout, connectTimeout;
        private Proxy proxy = Proxy.NO_PROXY;
        private byte[] content;
        private String method;
        private URL url;

        public Builder proxy(Proxy proxy) {
            if (proxy == null)
                throw new IllegalArgumentException("proxy cannot be null");

            this.proxy = proxy;
            return this;
        }

        public Builder url(URL url) {
            this.url = url;
            return this;
        }

        public Builder url(String url) throws URISyntaxException, MalformedURLException {
            return this.url(new URI(url).toURL());
        }

        public Builder method(String method) {
            this.checkMethod(method);

            this.method = method;
            return this;
        }

        public Builder setHeader(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.multiHeaders.add(new AbstractMap.SimpleEntry<>(name, value));
            return this;
        }

        public Builder body(byte[] content) {
            this.content = content;
            return this;
        }

        public Builder body(RequestBody body) throws IOException {
            this.content = body.build();
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder query(String key, Object value) {
            try {
                this.query.put(URLEncoder.encode(key, StandardCharsets.UTF_8.name()), URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        private void checkMethod(String method) {
            if (method == null || !ALLOWED_METHODS.contains(method.toUpperCase()))
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public HttpRequest build() throws IOException {
            this.checkMethod(this.method);

            try {
                this.url(this.replaceQuery());
            } catch (Exception e) {
                throw new IOException(e);
            }

            final HttpRequest request = new HttpRequest(this.url, this.proxy, this.method, this.followRedirects, this.readTimeout, this.connectTimeout);
            this.multiHeaders.forEach(entry -> request.addHeader(entry.getKey(), entry.getValue()));
            this.headers.forEach(request::setHeader);
            request.content = content;
            return request;
        }

        private URL replaceQuery() throws MalformedURLException, URISyntaxException {
            URI oldUri = url.toURI();

            String query = oldUri.getQuery();
            if (query != null) {
                for (Map.Entry<String, String> entry : this.query.entrySet())
                    query = query.replaceAll("\\$" + entry.getKey(), entry.getValue());
            } else {
                if (!this.query.isEmpty())
                    query = this.query.entrySet().stream().map(entry -> entry.getKey() + '=' + entry.getValue()).collect(Collectors.joining("&"));
            }

            URI newUri = new URI(
                    oldUri.getScheme(),
                    oldUri.getUserInfo(),
                    oldUri.getHost(),
                    oldUri.getPort(),
                    oldUri.getPath(),
                    query,
                    oldUri.getFragment()
            );

            return newUri.toURL();
        }
    }

}