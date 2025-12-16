package cat.psychward.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public final class HttpResponse {

    private final Map<String, List<String>> headers;
    private final int statusCode;
    private final String message;
    private final byte[] body;

    public HttpResponse(Map<String, List<String>> headers, int statusCode, String message, byte[] body) {
        this.headers = headers;
        this.statusCode = statusCode;
        this.message = message;
        this.body = body;
    }

    public static HttpResponse of(HttpURLConnection connection) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final int responseCode = connection.getResponseCode();
        final byte[] buffer = new byte[4096];
        try (InputStream input = (responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream())) {
            int read;
            while ((read = input.read(buffer)) != -1)
                bytes.write(buffer, 0, read);
        }
        return new HttpResponse(connection.getHeaderFields(), responseCode, connection.getResponseMessage(), bytes.toByteArray());
    }

    public <T extends ResponseBody> T as(Class<T> clazz) throws IOException {
        try {
            final Constructor<T> constructor = clazz.getConstructor(byte[].class);
            return constructor.newInstance((Object) this.body);
        } catch (NoSuchMethodException e) {
            throw new IOException("Failed to find byte[] constructor for " + clazz.getName(), e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IOException("Failed to invoke byte[] constructor for " + clazz.getName(), e);
        }
    }

    public int statusCode() {
        return statusCode;
    }

    public String message() {
        return message;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

}