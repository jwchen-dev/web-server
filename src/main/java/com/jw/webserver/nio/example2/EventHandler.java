package com.jw.webserver.nio.example2;

import java.nio.channels.SelectionKey;

/**
 * Created by jw on 2017/9/6.
 */
public interface EventHandler {

    void handleEvent(SelectionKey handle) throws Exception;
}
