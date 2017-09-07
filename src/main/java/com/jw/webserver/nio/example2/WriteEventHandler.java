package com.jw.webserver.nio.example2;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by jw on 2017/9/6.
 */
public class WriteEventHandler implements EventHandler {

    @Override
    public void handleEvent(SelectionKey handle) throws Exception {
//        System.out.println("===== Write Event Handler =====");

        try {
            SocketChannel socketChannel = (SocketChannel) handle.channel();
            //ByteBuffer bb = ByteBuffer.wrap("Hello Client!\n".getBytes());
            ByteBuffer inputBuffer = (ByteBuffer) handle.attachment();
//            ByteBuffer tb = ByteBuffer.wrap(("HTTP/1.1 200 OK\r\n" +
//                    "Content-Length: " + inputBuffer.limit() + "\r\n" +
//                    "Content-Type: application/json\r\n" +
//                    "\r\n" + new String(inputBuffer.array())).getBytes("UTF-8"));
            ByteBuffer tb = ByteBuffer.wrap(("HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + "Hello World!".length() + "\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n" + "Hello World!").getBytes("UTF-8"));
            socketChannel.write(tb);
//            socketChannel.close();
        } finally {
            handle.cancel();
            handle.channel().close();
        }
    }
}
