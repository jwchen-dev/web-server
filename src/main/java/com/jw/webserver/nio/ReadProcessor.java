package com.jw.webserver.nio;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Created by jw on 2017/9/5.
 */
public class ReadProcessor implements Runnable{

    private SelectionKey serverSelectionKey=null;

    public ReadProcessor(SelectionKey serverSelectionKey){
        this.serverSelectionKey=serverSelectionKey;
    }

    @Override
    public void run() {
        SocketChannel clientSocketChannel = (SocketChannel) serverSelectionKey.channel();
System.out.println("[ReadProcessor]"+serverSelectionKey);
        ByteBuffer buffer = (ByteBuffer) serverSelectionKey.attachment();

        String request = null;
        try {
            request = new String(readRequest(clientSocketChannel, buffer));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SelectionKey clientKey = serverSelectionKey.interestOps(SelectionKey.OP_WRITE);
        clientKey.attach(request);
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
}
