package com.jw.webserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by jw on 2017/8/28.
 */
public class NioServer implements Runnable {

    private int serverPort = 8080;
    private int backlog = 8 * 1024;

    private ServerSocketChannel serverSocketChannel;

    public NioServer(int serverPort) {
        this(serverPort, 50);
    }

    public NioServer(int serverPort, int backlog) {
        this.serverPort = serverPort;
        this.backlog = backlog;
    }

    @Override
    public void run() {
        Selector serverSelector = null;

        try {
            serverSelector = Selector.open();

            openServer(serverSelector);

            while (true) {
                int readyChannels = serverSelector.select();

                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> serverSelectedKeySet = serverSelector.selectedKeys();

                Iterator<SelectionKey> serverSelectedKeyIte = serverSelectedKeySet.iterator();

                SelectionKey serverSelectionKey = null;

                while (serverSelectedKeyIte.hasNext()) {
                    serverSelectionKey = serverSelectedKeyIte.next();

                    if (serverSelectionKey.isAcceptable()) {
//                        System.out.println("isAcceptable");

                        ServerSocketChannel server = (ServerSocketChannel) serverSelectionKey.channel();

                        SocketChannel clientSocketChannel = server.accept();
                        clientSocketChannel.configureBlocking(false);
                        SelectionKey clientKey = clientSocketChannel.register(serverSelector, SelectionKey.OP_READ);

                        ByteBuffer buffer = ByteBuffer.allocate(10);
                        clientKey.attach(buffer);
                    } else if (serverSelectionKey.isConnectable()) {
//                        System.out.println("isConnectable");
                    } else if (serverSelectionKey.isReadable()) {
//                        System.out.println("isReadable");

                        SocketChannel clientSocketChannel = (SocketChannel) serverSelectionKey.channel();

                        ByteBuffer buffer = (ByteBuffer) serverSelectionKey.attachment();

                        String request = new String(readRequest(clientSocketChannel, buffer));

                        SelectionKey clientKey = serverSelectionKey.interestOps(SelectionKey.OP_WRITE);
                        clientKey.attach(request);
                    } else if (serverSelectionKey.isWritable()) {
//                        System.out.println("isWritable");

                        SocketChannel clientSocketChannel = (SocketChannel) serverSelectionKey.channel();

                        String content = (String) serverSelectionKey.attachment();

                        clientSocketChannel.write(ByteBuffer.wrap(("HTTP/1.1 200 OK\r\n" +
                                "Content-Length: " + content.length() + "\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" + content).getBytes("UTF-8")));

                        serverSelectionKey.cancel();
                        serverSelectionKey.channel().close();
                    }

                    serverSelectedKeyIte.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSelector != null) {
                try {
                    serverSelector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private char[] readRequest(SocketChannel socketChannel, ByteBuffer byteBuffer) throws Exception {
        int bytesRead = socketChannel.read(byteBuffer);
        char[] buff = new char[byteBuffer.limit()];
        int offset = 0;

        while (bytesRead > 0) {
            byteBuffer.flip();

            while (byteBuffer.hasRemaining()) {
                buff[offset++] = (char) byteBuffer.get();
            }

            byteBuffer.clear();

            bytesRead = socketChannel.read(byteBuffer);

            if ((offset + bytesRead) >= buff.length) {
                buff = Arrays.copyOf(buff, buff.length * 2);
            }
        }

        byteBuffer.clear();

        return buff;
    }

    private void openServer(Selector selector) {
        if (serverSocketChannel == null) {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(this.serverPort), this.backlog);
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                throw new RuntimeException("Cannot open port " + this.serverPort, e);
            }

        }
    }


    public static void main(String[] args) {
        NioServer nioServer = new NioServer(8080);
        new Thread(nioServer).run();
    }

}