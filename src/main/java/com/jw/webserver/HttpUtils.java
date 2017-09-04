package com.jw.webserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by jw on 2017/8/30.
 */
public class HttpUtils {

    public Map<String, Object> readRequest(InputStreamReader inputStream) throws IOException {
        int len = 8 * 1024;
        char[] buffer = new char[len];
        char[] readBuffer = new char[len];
        int offset = 0;
        int readLen = -1;

        //不能以!=-1來判斷是否讀完,因為Socket一直開著,所以會使程式hang住
        while ((readLen = inputStream.read(readBuffer)) == len) {

            if ((offset + readLen) >= buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }

            System.arraycopy(readBuffer, 0, buffer, offset, readLen);

            offset = offset + readLen;
        }

        if (readLen > 0) {
            if ((offset + readLen) >= buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }

            System.arraycopy(readBuffer, 0, buffer, offset, readLen);

            offset = offset + readLen;
        }

        Map<String, Object> map = new TreeMap<String, Object>();

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, size = buffer.length; i < size; i++) {
            if (buffer[i] == '\n') {
                String line = stringBuilder.toString();
                line = line.trim();
                if (line.length() > 0) {
                    StringTokenizer stringTokenizer = new StringTokenizer(line);
                    String name = stringTokenizer.nextToken();

                    switch (name) {
                        // sample: GET / HTTP/1.0
                        case "GET":
                            String[] vals = new String[2];
                            vals[0] = stringTokenizer.nextToken();
                            vals[1] = stringTokenizer.nextToken();
                            map.put(name, vals);
                            break;
                        default:
                            map.put(name, stringTokenizer.nextToken());
                            break;
                    }
                }
                stringBuilder = new StringBuilder();
            } else {
                stringBuilder.append(buffer[i]);
            }
        }

        map.put("raw", new String(buffer));

        return map;
    }
}
