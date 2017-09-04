package rapidoid;

import org.rapidoid.net.Server;

/**
 * Created by jw on 2017/9/4.
 */
public class HelloWorldServer {

    public static void main(String[] args) {
        Server server = new CustomHttpServer().listen(8080);

//        server.shutdown();
    }
}
