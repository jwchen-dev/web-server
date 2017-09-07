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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jw on 2017/8/28.
 */
public class NioServer implements Runnable {

    private int serverPort = 8080;
    private int backlog = 8 * 1024;

    private static Queue<SocketChannel> queue = new ArrayBlockingQueue<SocketChannel>(8 * 1024);

    private ServerSocketChannel serverSocketChannel;

    private Selector workSelector = null;

    private ExecutorService readThreadPool = Executors.newFixedThreadPool(1);
    private ExecutorService writeThreadPool = Executors.newFixedThreadPool(1);

    public NioServer(int serverPort) throws IOException {
        this(serverPort, 50);
    }

    public NioServer(int serverPort, int backlog) throws IOException {
        this.serverPort = serverPort;
        this.backlog = backlog;
        this.workSelector = Selector.open();
    }

    @Override
    public void run() {

//        SocketChannel client = null;
//
//        SelectionKey clientKey = null;
//
//        while (true) {
//            client = this.queue.poll();
//
//            while (client != null) {
//                try {
//                    //
//                    client.configureBlocking(false);
//                    client.socket().setReuseAddress(true);
//                    //
//
//
//                    client.register(workSelector, SelectionKey.OP_READ);
//
//                    Iterator<SelectionKey> clientKeyIte = workSelector.selectedKeys().iterator();
//System.out.println("XXXXX");
//                    while (clientKeyIte.hasNext()) {
//                        clientKey = clientKeyIte.next();
//                        System.out.println("[clientKey]"+clientKey);
//                        System.out.println("[clientKey.isConnectable()]" + clientKey.isConnectable());
//                        System.out.println("[clientKey.isAcceptable()]" + clientKey.isAcceptable());
//                        System.out.println("[clientKey.isReadable()]" + clientKey.isReadable());
//                        System.out.println("[clientKey.isWritable()]" + clientKey.isWritable());
//                        System.out.println("[clientKey.isValid()]" + clientKey.isValid());
//
//                        if (clientKey.isReadable()) {
//                            System.out.println("is read...");
//                        } else if (clientKey.isWritable()) {
//                            System.out.println("is write...");
//                        }
//
//                        clientKeyIte.remove();
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                client = this.queue.poll();
//            }
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

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

                    try {
                        if (serverSelectionKey.isAcceptable()) {
//                        System.out.println("isAcceptable");

                            ServerSocketChannel server = (ServerSocketChannel) serverSelectionKey.channel();

                            SocketChannel clientSocketChannel = server.accept();
                            clientSocketChannel.configureBlocking(false);
                            SelectionKey clientKey = clientSocketChannel.register(serverSelector, SelectionKey.OP_READ);

                            ByteBuffer buffer = ByteBuffer.allocate(10);
                            clientKey.attach(buffer);

                            System.out.println("[NioServer]" + serverSelectionKey);
                        } else if (serverSelectionKey.isReadable()) {
//                        System.out.println("isReadable");

                            readThreadPool.execute(new ReadProcessor(serverSelectionKey));
//                        SocketChannel clientSocketChannel = (SocketChannel) serverSelectionKey.channel();
//
//                        ByteBuffer buffer = (ByteBuffer) serverSelectionKey.attachment();
//
//                        String request = new String(readRequest(clientSocketChannel, buffer));
//
//                        SelectionKey clientKey = serverSelectionKey.interestOps(SelectionKey.OP_WRITE);
//                        clientKey.attach(request);
                        } else if (serverSelectionKey.isWritable()) {
//                        System.out.println("isWritable");
//                            writeThreadPool.execute(new WriteProcessor(serverSelectionKey));
                            SocketChannel clientSocketChannel = (SocketChannel) serverSelectionKey.channel();

                            String content = (String) serverSelectionKey.attachment();

                            clientSocketChannel.write(ByteBuffer.wrap(("HTTP/1.1 200 OK\r\n" +
                                    "Content-Length: " + content.length() + "\r\n" +
                                    "Content-Type: text/html\r\n" +
                                    "\r\n" + content).getBytes("UTF-8")));

                            serverSelectionKey.cancel();
                            serverSelectionKey.channel().close();
                        }
                    } finally {
                        serverSelectedKeyIte.remove();
                    }
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


    public static void main(String[] args) throws IOException {
//        AcceptProcessor acceptProcessor = new AcceptProcessor(queue);
//        new Thread(acceptProcessor).start();


        NioServer nioServer = new NioServer(8080);
        new Thread(nioServer).start();
    }

}