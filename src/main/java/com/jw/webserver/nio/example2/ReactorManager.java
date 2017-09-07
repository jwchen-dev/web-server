package com.jw.webserver.nio.example2;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by jw on 2017/9/6.
 */
public class ReactorManager {
    private static final int SERVER_PORT = 8080;


    public void startReactor(int port) throws Exception {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(port),8*1024);
        server.socket().setReuseAddress(true);
        server.configureBlocking(false);

        Reactor reactor = new Reactor();
        reactor.registerChannel(SelectionKey.OP_ACCEPT, server);
//        reactor.registerReadChannel(SelectionKey.OP_READ, server);

        reactor.registerEventHandler(SelectionKey.OP_ACCEPT, new AcceptEventHandler(reactor.getDemultiplexer()));

        reactor.registerEventHandler(SelectionKey.OP_READ, new ReadEventHandler(reactor.getDemultiplexer()));
//        new Thread(new ReadEventHandler(reactor.getReadDemultiplexer())).start();

        reactor.registerEventHandler(SelectionKey.OP_WRITE, new WriteEventHandler());

        reactor.run();
    }

    public static void main(String[] args) {
        System.out.println("Server Started at port : " + SERVER_PORT);
        try {
            new ReactorManager().startReactor(SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
