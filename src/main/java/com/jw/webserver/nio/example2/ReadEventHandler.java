package com.jw.webserver.nio.example2;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by jw on 2017/9/6.
 */
public class ReadEventHandler implements EventHandler,Runnable {

    private Selector demultiplexer;

    private ByteBuffer inputBuffer = ByteBuffer.allocate(2 * 1024);

    private SelectionKey handle;

    public ReadEventHandler(Selector demultiplexer) {
        this.demultiplexer = demultiplexer;
    }

    @Override
    public void handleEvent(SelectionKey handle) throws Exception {
//        System.out.println("===== Read Event Handler =====");
//        ByteBuffer inputBuffer = ByteBuffer.allocate(2 * 1024);

        SocketChannel socketChannel = (SocketChannel) handle.channel();
inputBuffer.clear();
        socketChannel.read(inputBuffer); // Read data from client

        inputBuffer.flip();
        // Rewind the buffer to start reading from the beginning

        byte[] buffer = new byte[inputBuffer.limit()];
        inputBuffer.get(buffer);

//        System.out.println("Received message from client : " + new String(buffer));
        inputBuffer.flip();
        // Rewind the buffer to start reading from the beginning
        // Register the interest for writable readiness event for
        // this channel in order to echo back the message

        socketChannel.register(demultiplexer, SelectionKey.OP_WRITE, inputBuffer);
    }

    @Override
    public void run() {
        try {
            //        System.out.println("===== Read Event Handler =====");
//        ByteBuffer inputBuffer = ByteBuffer.allocate(2 * 1024);

            SocketChannel socketChannel = (SocketChannel) this.handle.channel();
            inputBuffer.clear();
            socketChannel.read(inputBuffer); // Read data from client

            inputBuffer.flip();
            // Rewind the buffer to start reading from the beginning

            byte[] buffer = new byte[inputBuffer.limit()];
            inputBuffer.get(buffer);

//        System.out.println("Received message from client : " + new String(buffer));
            inputBuffer.flip();
            // Rewind the buffer to start reading from the beginning
            // Register the interest for writable readiness event for
            // this channel in order to echo back the message

            socketChannel.register(this.demultiplexer, SelectionKey.OP_WRITE, inputBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHandle(SelectionKey handle) {
        this.handle = handle;
    }
}
