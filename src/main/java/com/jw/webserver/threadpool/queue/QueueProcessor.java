package com.jw.webserver.threadpool.queue;

import com.jw.webserver.threadpool.WorkerRunnable;

import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jw on 2017/8/29.
 */
public class QueueProcessor implements Runnable {

    private Queue<Socket> connected = null;

    private int processorCount = 0;

    private ExecutorService threadPool = null;

    private boolean isStopped = false;

    public QueueProcessor(Queue<Socket> queue) {
        this(queue, 1);
    }

    public QueueProcessor(Queue<Socket> queue, int processorCount) {
        this.connected = queue;
        this.processorCount = processorCount;

        threadPool = Executors.newFixedThreadPool(this.processorCount);
    }

    @Override
    public void run() {
        while (!isStopped()) {
            try {
                Socket socket = connected.poll();
                if (socket != null) {
                    this.threadPool.execute(new WorkerRunnable(socket, "multithread server"));
                } else {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.threadPool.shutdown();
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public void setStopped(boolean stopped) {
        this.isStopped = stopped;
    }
}
