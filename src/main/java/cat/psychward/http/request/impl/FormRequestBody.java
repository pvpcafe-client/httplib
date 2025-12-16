package cat.psychward.http.request.impl;

import cat.psychward.http.request.RequestBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

public final class FormRequestBody implements RequestBody {

    private final List<Parameter> parameters;

    public FormRequestBody(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public byte[] build() throws IOException {
        StringJoiner joiner = new StringJoiner("&");

        for (Parameter parameter : parameters) {
            String encodedName = URLEncoder.encode(parameter.name, StandardCharsets.UTF_8.name());
            String encodedValue = URLEncoder.encode(parameter.value, StandardCharsets.UTF_8.name());
            joiner.add(encodedName + "=" + encodedValue);
        }

        return joiner.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static class Parameter {
        private final String name;
        private final String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
