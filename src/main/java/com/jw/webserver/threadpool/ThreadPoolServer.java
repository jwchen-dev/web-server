package com.jw.webserver.threadpool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tip.
 * <p>
 * ref: http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html
 * <p>
 * Created by jw on 2017/8/25.
 */
public class ThreadPoolServer implements Runnable {

    protected int serverPort = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public ThreadPoolServer(int port) {
        this.serverPort = port;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();

        while (!isStopped()) {
            Socket clientSocket = null;

            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    return;
                }

                throw new RuntimeException("Error accepting client connection", e);
            }

            try {
                this.threadPool.execute(new WorkerRunnable(clientSocket, "multithread server"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.threadPool.shutdown();

        System.out.println("Server Stopped.");
    }

    private boolean isStopped() {
        return this.isStopped;
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
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
        ThreadPoolServer server = new ThreadPoolServer(8080);
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