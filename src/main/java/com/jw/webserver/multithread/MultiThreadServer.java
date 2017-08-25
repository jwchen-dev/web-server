package com.jw.webserver.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Tip. 以ab做壓測時,concurrent為50~60,server就會拋NullException.
 * <p>
 * ref: http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 * <p>
 * Created by jw on 2017/8/25.
 */
public class MultiThreadServer implements Runnable {

    protected int serverPort = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;

    public MultiThreadServer(int port) {
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
                new Thread(new WorkerRunnable(clientSocket, "multithread server")).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Server Stopped.");
    }

    private void processClientRequest(Socket clientSocket) throws IOException {
        BufferedReader bufferedReader = null;
        OutputStream output = null;
        long time = System.currentTimeMillis();
        String content = null;
        String requestMessageLine = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //一定要將client的data讀進來,否則用apache ab測試時,會出現"connection reset by peer (104)"
            requestMessageLine = bufferedReader.readLine();

            // sample: GET / HTTP/1.0
            StringTokenizer tokenizer = new StringTokenizer(requestMessageLine);

            if ("GET".equals(tokenizer.nextToken())) {
                String fileName = tokenizer.nextToken();

                output = clientSocket.getOutputStream();

                switch (fileName) {
                    case "/":
                        content = "<html><body>" + time + "</body></html>";
                        output.write(("HTTP/1.1 200 OK\r\n" +
                                "Content-Length: " + content.length() + "\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" +
                                content).getBytes("UTF-8"));
                        break;
                    default:
                        content = "404 Not Found";
                        output.write(("HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: " + content.length() + "\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" +
                                content).getBytes("UTF-8"));
                        break;
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (output != null) {
                output.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }
        }

//        System.out.println("Request processed: " + time);
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
        MultiThreadServer server = new MultiThreadServer(8080);
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