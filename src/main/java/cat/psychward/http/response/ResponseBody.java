package cat.psychward.http.response;

public abstract class ResponseBody {

    private final byte[] content;

    public ResponseBody(byte[] content) {
        this.content = content;
    }

    public byte[] content() {
        return this.content;
    }

}