package com.jw.webserver.nio.example2;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jw on 2017/9/6.
 */
public class Reactor {
    private Map<Integer, EventHandler> registeredHandlers = new ConcurrentHashMap<Integer, EventHandler>();

    private Selector demultiplexer;
    private Selector readSelector;

    public Reactor() throws IOException {
        this.demultiplexer = Selector.open();
        this.readSelector=Selector.open();
    }

    public Selector getDemultiplexer() {
        return demultiplexer;
    }

    public Selector getReadDemultiplexer(){return readSelector;}

    public void registerEventHandler(int eventType, EventHandler eventHandler) {
        registeredHandlers.put(eventType, eventHandler);
    }

    public void registerChannel(int eventType, SelectableChannel channel) throws ClosedChannelException {
        channel.register(demultiplexer, eventType);
    }

    public void registerReadChannel(int eventType, SelectableChannel channel) throws ClosedChannelException {
        channel.register(readSelector, eventType);
    }

    public void run() {
        try {
            while (true) {
                int readyCount = demultiplexer.select();

                if (readyCount == 0) {
                    continue;
                }

                Iterator<SelectionKey> handleIterator = demultiplexer.selectedKeys().iterator();

                while (handleIterator.hasNext()) {
                    SelectionKey handle = handleIterator.next();
//System.out.println(handle.interestOps());
                    if (handle.isAcceptable()) {
                        EventHandler handler = registeredHandlers.get(SelectionKey.OP_ACCEPT);
                        handler.handleEvent(handle);
                    }

                    if (handle.isReadable()) {
                        EventHandler handler = registeredHandlers.get(SelectionKey.OP_READ);
                        handler.handleEvent(handle);
//                        ((ReadEventHandler)handler).setHandle(handle);
//                        new Thread((ReadEventHandler)handler).start();
                    }

                    if (handle.isWritable()) {
                        EventHandler handler = registeredHandlers.get(SelectionKey.OP_WRITE);
                        handler.handleEvent(handle);
                    }

                    handleIterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
