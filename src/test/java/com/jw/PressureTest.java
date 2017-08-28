package com.jw;

import junit.framework.TestCase;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jw on 2017/8/25.
 */
public class PressureTest extends TestCase {

    @Test
    public void test() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 100000; i++) {
            executorService.execute(new Worker());
        }

        executorService.awaitTermination(10, TimeUnit.SECONDS);
        executorService.shutdown();
    }

    class Worker implements Runnable {

        @Override
        public void run() {
            try {
                URL url = new URL("http://192.168.204.129:8080/");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10 * 1000);

                int responseCode = conn.getResponseCode();

                if (responseCode != 200) {
                    System.out.println(conn.getResponseMessage());
                    System.out.println(responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
