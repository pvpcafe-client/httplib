package cat.psychward.http.request;

import java.io.IOException;

public interface RequestBody {

    byte[] build() throws IOException;

}