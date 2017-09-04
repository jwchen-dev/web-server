package com.jw.webserver.threadpool;

import com.jw.webserver.HttpUtils;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by jw on 2017/8/25.
 */
public class WorkerRunnable implements Runnable {

    private Socket clientSocket = null;
    private String serverTest = null;
    private HttpUtils httpUtils = new HttpUtils();

    public WorkerRunnable(Socket clientSocket, String serverTest) {
        this.clientSocket = clientSocket;
        this.serverTest = serverTest;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        OutputStream output = null;
        long time = System.currentTimeMillis();

        try {
            if (clientSocket.isInputShutdown()) {
                System.out.println("Input stream down............");
                return;
            }

            //一定要將client的data讀進來,否則用apache ab測試時,會出現"connection reset by peer (104)"
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());

            Map<String, Object> headerMap = httpUtils.readRequest(inputStreamReader);

            String fileName = null;
            try {
                fileName = ((String[]) headerMap.get("GET"))[0];
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("[]" + (String) headerMap.get("raw"));
            }
//            String fileName="/";

            output = clientSocket.getOutputStream();

            switch (fileName) {
                case "/":
                    CONTENT = "<html><body>" + time + "</body></html>";
                    output.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Length: " + CONTENT.length() + "\r\n" +
                            "Content-Type: text/html\r\n" +
                            "\r\n" +
                            CONTENT).getBytes("UTF-8"));
                    break;
                default:
                    CONTENT_404 = "404 Not Found";
                    output.write(("HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: " + CONTENT_404.length() + "\r\n" +
                            "Content-Type: text/html\r\n" +
                            "\r\n" +
                            CONTENT_404).getBytes("UTF-8"));
                    break;
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

    private static String CONTENT_404 = "404 Not Found";

    private static String CONTENT = "<div class=\"post-body entry-CONTENT\" id=\"post-body-4816523915605938719\" itemprop=\"description articleBody\">\n" +
            "上一篇說明了 <a href=\"http://stevenitlife.blogspot.tw/2015/07/nio2-tcp-blocking.html\">blocking 程式</a>，這一篇要說明如何寫 non-blocking 程式，首先看一下兩者的差別:<br>\n" +
            "<ul>\n" +
            "<li>blocking 模式一個 thread 只能有一個 channel，所以，server 端要服務多個 client 的話，就要為每個 client 建立一個 thread。</li>\n" +
            "<li>non-blocking 模式引進一個新的類別 - Selector，這個類別的用處在於，讓 non-blocking 成為事件驅動的運作模式。在 blocking 模式下，都是由程式本身決定何時連線與讀寫，這造成的限制就是當要同時有多個連線同時運作時，就要有多個 thread; Selector 會從 ServerSocketChannel 中選擇一個目前需要服務的 channel 進行服務，傳給程式 channel 及這個 channel 需要什麼類型的服務。</li>\n" +
            "</ul>\n" +
            "了解了 non-blocking 的運作方式後，要先說明另一個類別 - SelectionKey，這個類別中定義了四個運作模式 (也可視為四種事件)，也就是 Selector 要告訴程式，這個 channel 需要什麼服務，如下:<br>\n" +
            "<ul>\n" +
            "<li>SelectionKey.OP_ACCEPT: 這事件通常是用在 server 端，當 client 連線到 server 時，產生這個運作模式。</li>\n" +
            "<li>SelectionKey.OP_CONNECT: 這通常用在 client 端，當 server 端接受了 client 端的連線，client 端就會收到這個 event。</li>\n" +
            "<li>SelectionKey.OP_READ: 當有資料進來，可以讀的時候，會收到這個事件。</li>\n" +
            "<li>SelectionKey.OP_WRITE: 當收到這個事件，即可以寫出資料到該事件所關聯的 channel。</li>\n" +
            "</ul>\n" +
            "<div>\n" +
            "要看程式前，還是要先說明，non-blocking 是建立在 blocking 的基礎上，增加的這些類別，是為將運作方式轉換成事件驅動，由 ServerSocketChannel 傳訊息給 Selector，再由 Selector 傳給程式，了解這些觀念之後再來看下面的程式，應該就很容易了解了。<br>\n" +
            "(感覺 non-blocking 有點像 IoC，把程式的主動權交出去，反而讓程式得到更多彈性。)</div>\n" +
            "<script async=\"\" src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script><br>\n" +
            "<!-- 回應式廣告 --><br>\n" +
            "<ins class=\"adsbygoogle\" data-ad-client=\"ca-pub-6013763518402598\" data-ad-format=\"auto\" data-ad-slot=\"6839636951\" style=\"display: block; height: 90px;\" data-adsbygoogle-status=\"done\"><ins id=\"aswift_1_expand\" style=\"display:inline-table;border:none;height:90px;margin:0;padding:0;position:relative;visibility:visible;width:770px;background-color:transparent;\"><ins id=\"aswift_1_anchor\" style=\"display:block;border:none;height:90px;margin:0;padding:0;position:relative;visibility:visible;width:770px;background-color:transparent;\"><iframe width=\"770\" height=\"90\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\" vspace=\"0\" hspace=\"0\" allowtransparency=\"true\" scrolling=\"no\" allowfullscreen=\"true\" onload=\"var i=this.id,s=window.google_iframe_oncopy,H=s&amp;&amp;s.handlers,h=H&amp;&amp;H[i],w=this.contentWindow,d;try{d=w.document}catch(e){}if(h&amp;&amp;d&amp;&amp;(!d.body||!d.body.firstChild)){if(h.call){setTimeout(h,0)}else if(h.match){try{h=s.upd(h,i)}catch(e){}w.location.replace(h)}}\" id=\"aswift_1\" name=\"aswift_1\" style=\"left:0;position:absolute;top:0;width:770px;height:90px;\"></iframe></ins></ins></ins><br>\n" +
            "<script>\n" +
            "(adsbygoogle = window.adsbygoogle || []).push({});\n" +
            "</script><br>\n" +
            "<pre style=\"font-family: 'Courier New' !important; font-size: 12.8px; line-height: 17.28px; padding: 0px; white-space: pre-wrap; word-wrap: break-word;\"><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> <span style=\"font-size: x-small;\">1</span></span><span style=\"font-size: x-small;\"> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">package</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> idv.steven.async;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  2</span> \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  3</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.io.IOException;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  4</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.net.InetSocketAddress;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  5</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.net.StandardSocketOptions;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  6</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.nio.ByteBuffer;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  7</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.nio.channels.SelectionKey;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  8</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.nio.channels.Selector;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">  9</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.nio.channels.ServerSocketChannel;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 10</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.nio.channels.SocketChannel;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 11</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.util.Arrays;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 12</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.util.Iterator;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 13</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.util.Set;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 14</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.util.concurrent.ExecutorService;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 15</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">import</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> java.util.concurrent.Executors;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 16</span> \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 17</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">public</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">class</span> EchoServer2 <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">implements</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> Runnable {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 18</span>     <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">public</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">static</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">int</span> DEFAULT_PORT = 5555<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 19</span> \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 20</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">    @Override\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 21</span>     <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">public</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">void</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> run() {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 22</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">        ServerSocketChannel serverChannel;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 23</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">        Selector selector;\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 24</span>         \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 25</span>         <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">try</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 26</span>             serverChannel =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> ServerSocketChannel.open();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 27</span>             selector =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> Selector.open();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 28</span>             \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 29</span>             <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">if</span> (serverChannel.isOpen() &amp;&amp;<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> selector.isOpen()) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 30</span>                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 設定為 false 即表示要用 non-blocking 模式</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 31</span>                 serverChannel.configureBlocking(<span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">false</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 32</span>                 \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 33</span>                 serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 34</span>                 serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">true</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 35</span>                 \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 36</span>                 serverChannel.bind(<span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">new</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> InetSocketAddress(DEFAULT_PORT));\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 37</span>                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 註冊 OP_ACCEPT 給 ServerSocketChannel，這樣 ServerSocketChannel 接到 client 端連線時，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">就會有一個值為 OP_ACCEPT 的 SelectionKey。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 39</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                serverChannel.register(selector, SelectionKey.<span style=\"color: red;\">OP_ACCEPT</span>);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 40</span>                 \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 41</span>                 <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">while</span> (<span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">true</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 42</span>                     <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 程式會在這裡停下，等待關心的\"selection operation\"出現。</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">這個 method 有另一個版本，可以傳入 timeout 值，以免程式一直停在這裡。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 44</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                    selector.select();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 45</span>                     \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 46</span>                     Set&lt;SelectionKey&gt; readyKeys =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> selector.selectedKeys();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 47</span>                     Iterator&lt;SelectionKey&gt; iterator =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> readyKeys.iterator();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 48</span>                     <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">while</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (iterator.hasNext()) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 49</span>                         SelectionKey key =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> iterator.next();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 50</span>                         <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 移除將處理的 selection operation 很重要!\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 51</span>                         <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 避免之後又重複接收到。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 52</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                        iterator.remove();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 53</span>                         \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 54</span>                         <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">try</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 55</span>                             <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">if</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (key.isAcceptable()) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 56</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> ServerSocketChannel 的用處就只是接受 client 連線，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">當收到 OP_ACCEPT 時，表示有 client 要連線，所以用 ServerSocketChannel 接收。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 58</span>                                 ServerSocketChannel server =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (ServerSocketChannel) key.channel();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 59</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 等待 client 連線</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 60</span>                                 SocketChannel client =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> server.accept();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 61</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 連線後設定這個連線為 non-blocking</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 62</span>                                 client.configureBlocking(<span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">false</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 63</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 這是一個 echo server，連線後緊接著要收 client 送來的訊息，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">所以註冊這個 socket channel 要關注 OP_READ。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 65</span>                                 SelectionKey clientKey =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> client.register(selector, SelectionKey.<span style=\"color: red;\">OP_READ</span>);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 66</span>                                 ByteBuffer buffer = ByteBuffer.allocate(1024<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 67</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> non-blocking 模式有點像事件模式，每次都是待 selection operation 出現，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">程式才會接手做相關的事，在這不同的 selection operation 之間，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">要如何保留讀入的資料? 使用 attach() 及 attachment()，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">讓這些資料保存在 SelectionKey 中。 </span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 71</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                clientKey.attach(buffer);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 72</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                            }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 73</span>                             <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">else</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">if</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (key.isReadable()) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 74</span>                                 SocketChannel client =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (SocketChannel) key.channel();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 75</span> //<span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">在 OP_ACCEPT 階段，程式有預先產生一個 ByteBuffer，</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">這時將它取出，用來儲存讀入的資料。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 77</span>                                 ByteBuffer output =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (ByteBuffer) key.attachment();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 78</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                client.read(output);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 79</span>                                 System.out.println(\"recv: \" + <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">new</span> String(Arrays.copyOfRange(output.array(), 0<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">, output.limit())));\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 80</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> echo server 讀到資料後，當然接著要寫回給 client，所以註冊 OP_WRITE。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 81</span>                                 SelectionKey clientKey =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> client.register(selector, SelectionKey.<span style=\"color: red;\">OP_WRITE</span>);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 82</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                            }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 83</span>                             <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">else</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">if</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (key.isWritable()) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 84</span>                                 SocketChannel client =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (SocketChannel) key.channel();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 85</span>                                 <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 在 OP_READ 階段讀取到的資料，現在將它取出。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 86</span>                                 ByteBuffer output =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (ByteBuffer) key.attachment();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 87</span>                                 <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">if</span> (output != <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">null</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 88</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                    output.flip();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 89</span>                                     <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">if</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (output.hasRemaining()) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 90</span>                                         <span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">//</span><span style=\"color: green; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 把讀取到的資料寫回給 client。</span>\n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 91</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                        client.write(output);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 92</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                        output.compact();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 93</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                    }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 94</span>                                     <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">else</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 95</span>                                         System.out.println(\"output has not remaining\"<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 96</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                    }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 97</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 98</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                            }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> 99</span>                         } <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">catch</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (IOException e) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">100</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                            key.cancel();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">101</span>                             <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">try</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">102</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                                key.channel().close();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">103</span>                             } <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">catch</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (IOException ex) { }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">104</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                        }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">105</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                    }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">106</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">                }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">107</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">            }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">108</span>         } <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">catch</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> (IOException e) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">109</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">            e.printStackTrace();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">110</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">        }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">111</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">    }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">112</span>     \n" +
            "<span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">113</span>     <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">public</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">static</span> <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">void</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> main(String[] args) {\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">114</span>         ExecutorService svc =<span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> Executors.newSingleThreadExecutor();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">115</span>         EchoServer2 echo = <span style=\"color: blue; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">new</span><span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\"> EchoServer2();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">116</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">        svc.execute(echo);\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">117</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">        svc.shutdown();\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">118</span> <span style=\"font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">    }\n" +
            "</span><span style=\"color: teal; font-size: 12px !important; line-height: 1.8; margin: 0px; padding: 0px;\">119</span> }</span></pre>\n" +
            "使用上一篇的 client 來測試，得到如下結果:<br>\n" +
            "<div class=\"separator\" style=\"clear: both; text-align: center;\">\n" +
            "<a href=\"http://2.bp.blogspot.com/-sr76xgkrCHY/Vbuchh9AVUI/AAAAAAAAFfI/eRN9YfurBCg/s1600/result_02.png\" imageanchor=\"1\" style=\"margin-left: 1em; margin-right: 1em;\"><img border=\"0\" height=\"544\" src=\"http://2.bp.blogspot.com/-sr76xgkrCHY/Vbuchh9AVUI/AAAAAAAAFfI/eRN9YfurBCg/s640/result_02.png\" width=\"640\"></a></div>\n" +
            "<br>\n" +
            "我開兩個視窗，編寫了批次程式，讓兩邊都有 client 程式一直送資料給 server，可以看到都能得到正確回應。\n" +
            "<div style=\"clear: both;\"></div>\n" +
            "</div>";
}
