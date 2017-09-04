package com.jw.webserver.threadpool.queue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Tip.
 * <p>
 * ref: http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html
 * <p>
 * Created by jw on 2017/8/25.
 */
public class QueueServer implements Runnable {

    protected int serverPort = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected Queue<Socket> connected = new ArrayBlockingQueue<Socket>(8 * 1024);

    private QueueProcessor queueProcessor = null;

    public QueueServer(int port) {
        this.serverPort = port;
        this.queueProcessor = new QueueProcessor(connected, 1);
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();

        new Thread(this.queueProcessor).start();

        while (!isStopped()) {
            try {
                Socket clientSocket = this.serverSocket.accept();

                connected.add(clientSocket);
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    return;
                }

                throw new RuntimeException("Error accepting client connection", e);
            }

//            try {
//                this.threadPool.execute(new WorkerRunnable(connected.poll(), "multithread server"));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        System.out.println("Server Stopped.");
    }

    private boolean isStopped() {
        return this.isStopped;
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort, 16 * 1024);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

    public synchronized void stop() {
        this.isStopped = true;

        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    public static void main(String[] args) {
        QueueServer server = new QueueServer(8080);
        new Thread(server).start();

//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Stopping Server");
//        server.stop();
    }
}