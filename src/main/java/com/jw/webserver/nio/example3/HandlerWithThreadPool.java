package com.jw.webserver.nio.example3;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jw on 2017/9/7.
 */
public class HandlerWithThreadPool extends Handler {

    static ExecutorService pool = Executors.newFixedThreadPool(4);
    static final int PROCESSING = 2;

    public HandlerWithThreadPool(Selector sel, SocketChannel c) throws IOException {
        super(sel, c);
    }

    //Handler从SocketChannel中读到数据后，把“数据的处理”这个工作扔到线程池里面执行
    void read() throws IOException {
        int readCount = socketChannel.read(input);
        if (readCount > 0) {
            state = PROCESSING;

            //execute是非阻塞的，所以要新增一个state（PROCESSING），表示数据在处理当中，Handler还不能执行send操作
            pool.execute(new Processer(readCount));
        }
        //We are interested in writing back to the client soon after read processing is done.
        //这时候虽然设置了OP_WRITE，但下一次本Handler被选中时不会执行send()方法，因为state=PROCESSING
        //或者可以把这个设置放到Processer里面，等process完成后再设为OP_WRITE
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    //Start processing in a new Processer Thread and Hand off to the reactor thread.
    synchronized void processAndHandOff(int readCount) {
        readProcess(readCount);
        //Read processing done. Now the server is ready to send a message to the client.
        state = SENDING;
    }

    class Processer implements Runnable {
        int readCount;
        Processer(int readCount) {
            this.readCount =  readCount;
        }
        public void run() {
            processAndHandOff(readCount);
        }
    }
}
