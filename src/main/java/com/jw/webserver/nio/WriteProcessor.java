package com.jw.webserver.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Created by jw on 2017/9/5.
 */
public class WriteProcessor implements Runnable{

    private SelectionKey serverSelectionKey=null;

    public WriteProcessor(SelectionKey serverSelectionKey){
        this.serverSelectionKey=serverSelectionKey;
    }

    @Override
    public void run() {
        SocketChannel clientSocketChannel = (SocketChannel) serverSelectionKey.channel();
        System.out.println("[WriteProcessor]"+serverSelectionKey);
        String content = (String) serverSelectionKey.attachment();

        try {
            clientSocketChannel.write(ByteBuffer.wrap(("HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + content.length() + "\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" + content).getBytes("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                serverSelectionKey.cancel();
                serverSelectionKey.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
