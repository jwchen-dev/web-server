package rapidoid;

import org.rapidoid.buffer.Buf;
import org.rapidoid.http.AbstractHttpServer;
import org.rapidoid.http.HttpStatus;
import org.rapidoid.http.HttpUtils;
import org.rapidoid.http.MediaType;
import org.rapidoid.net.abstracts.Channel;
import org.rapidoid.net.impl.RapidoidHelper;

/**
 * Created by jw on 2017/9/4.
 */
public class CustomHttpServer extends AbstractHttpServer {

    private final byte[] URI_PLAINTEXT = "/plaintext".getBytes();

    private final byte[] URI_JSON = "/json".getBytes();

    private final byte[] HELLO_WORLD = "Hello, World!".getBytes();

    @Override
    protected HttpStatus handle(Channel ctx, Buf buf, RapidoidHelper req) {
        if (req.isGet.value) {
            if (matches(buf, req.path, URI_PLAINTEXT)) {
                return ok(ctx, req.isKeepAlive.value, HELLO_WORLD, MediaType.TEXT_PLAIN);

            } else if (matches(buf, req.path, URI_JSON)) {
                return serializeToJson(HttpUtils.noReq(), ctx, req.isKeepAlive.value, new Message("Hello, World!"));
            }
        }

        return HttpStatus.NOT_FOUND;
    }
}
