package cat.psychward.http.response.impl;

import cat.psychward.http.response.ResponseBody;

import java.nio.charset.StandardCharsets;

public final class StringResponseBody extends ResponseBody {

    public StringResponseBody(byte[] content) {
        super(content);
    }

    @Override
    public String toString() {
        return new String(this.content(), StandardCharsets.UTF_8);
    }

}