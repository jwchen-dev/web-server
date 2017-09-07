package com.jw.webserver.nio;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;

/**
 * Created by jw on 2017/9/4.
 */
public class WorkProcessor implements Runnable {

    private Queue<SocketChannel> queue = null;

    private Selector workSelector = null;

    public WorkProcessor(Queue<SocketChannel> queue) throws IOException {
        this.workSelector = Selector.open();

        this.queue = queue;
    }

    @Override
    public void run() {
        SocketChannel client = this.queue.poll();

        while (true) {
            while (client != null) {
                try {
                    SelectionKey clientKey = client.register(workSelector, SelectionKey.OP_READ);

                    if (clientKey.isReadable()) {
                        System.out.println("is read...");
                    } else if (clientKey.isWritable()) {
                        System.out.println("is write...");
                    }
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                client = this.queue.poll();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
