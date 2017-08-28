package com.jw.webserver.threadpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created by jw on 2017/8/25.
 */
public class WorkerRunnable implements Runnable {

    private Socket clientSocket = null;
    private String serverTest = null;

    public WorkerRunnable(Socket clientSocket, String serverTest) {
        this.clientSocket = clientSocket;
        this.serverTest = serverTest;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        OutputStream output = null;
        long time = System.currentTimeMillis();
        String content = null;
        String requestMessageLine = "";

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
