package com.github.cadecode.java.practice.java8.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * BIO Server
 *
 * @author Cade Li
 * @since 2022/8/12
 */
public class BIOServer {

    public static ServerSocket serverSocket;

    public static void start(String[] args) throws IOException {
        serverSocket = new ServerSocket(8080);
        // 没有连接则阻塞
        Socket request = serverSocket.accept();
        try {
            InputStream inputStream = request.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String msg;
            // 没有数据则阻塞
            while ((msg = reader.readLine()) != null) {
                // 处理数据
                System.out.println(msg);
            }
        } catch (IOException e) {
            //
        } finally {
            //
        }
    }
}
