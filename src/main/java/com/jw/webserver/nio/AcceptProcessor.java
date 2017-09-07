package com.jw.webserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by jw on 2017/9/4.
 */
public class AcceptProcessor implements Runnable {

    private ServerSocketChannel serverSocketChannel = null;

    private Selector acceptSelector = null;

    private int serverPort = 8080;

    private int backlog = 8 * 1024;

    private Queue<SocketChannel> queue = null;

    public AcceptProcessor(Queue<SocketChannel> queue) throws IOException {
        this.acceptSelector = Selector.open();

        this.queue = queue;

        openServer(this.acceptSelector);
    }

    private void openServer(Selector selector) {
        if (serverSocketChannel == null) {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(this.serverPort), this.backlog);
//                serverSocketChannel.configureBlocking(false);
//                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                throw new RuntimeException("Cannot open port " + this.serverPort, e);
            }

        }
    }

    @Override
    public void run() {

        while (true) {
            try {
                SocketChannel clientSocketChannel = serverSocketChannel.accept();
//                clientSocketChannel.configureBlocking(false);
                this.queue.add(clientSocketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


//        SelectionKey serverSelectionKey = null;
//
//        while (true) {
//            try {
//                int readyChannel = this.acceptSelector.select();
//
//                if (readyChannel == 0) {
//                    continue;
//                }
//
//                Iterator<SelectionKey> keyIterator = this.acceptSelector.selectedKeys().iterator();
//
//                while (keyIterator.hasNext()) {
//                    serverSelectionKey = keyIterator.next();
//
//                    if (serverSelectionKey.isAcceptable()) {
//                        ServerSocketChannel server = (ServerSocketChannel) serverSelectionKey.channel();
//
//                        SocketChannel clientSocketChannel = server.accept();
//                        clientSocketChannel.configureBlocking(false);
//
//                        SelectionKey clientKey = clientSocketChannel.register(this.acceptSelector, SelectionKey.OP_READ);
//
//                        ByteBuffer buffer = ByteBuffer.allocate(10);
//                        clientKey.attach(buffer);
//
//                        this.queue.add(clientSocketChannel);
//                    }else if(serverSelectionKey.isReadable()){
//                        System.out.println("[]is read...");
//                    }
//
//                    keyIterator.remove();
//                }
//
//                Thread.sleep(200);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}
