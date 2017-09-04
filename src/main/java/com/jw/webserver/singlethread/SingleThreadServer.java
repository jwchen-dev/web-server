package com.jw.webserver.singlethread;

import com.jw.webserver.HttpUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Tip. 以ab做壓測時,concurrent為10,做到第二round,server就會拋NullException然後shutdown.
 * <p>
 * ref: http://tutorials.jenkov.com/java-multithreaded-servers/singlethreaded-server.html
 * <p>
 * Created by jw on 2017/8/25.
 */
public class SingleThreadServer implements Runnable {

    protected int serverPort = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    private HttpUtils httpUtils = new HttpUtils();

    public SingleThreadServer(int port) {
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
                processClientRequest(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Server Stopped.");
    }

    private void processClientRequest(Socket clientSocket) throws IOException {
        InputStreamReader inputStreamReader = null;
        OutputStream output = null;
        long time = System.currentTimeMillis();
        String content = null;

        try {
            //一定要將client的data讀進來,否則用apache ab測試時,會出現"connection reset by peer (104)"
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());

            Map<String, Object> headerMap = httpUtils.readRequest(inputStreamReader);

            String fileName = ((String[]) headerMap.get("GET"))[0];

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
        } finally {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }

            if (output != null) {
                output.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }
        }
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
        SingleThreadServer server = new SingleThreadServer(8080);
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
