package com.github.cadecode.learn.java8.io;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO Client
 *
 * @author Cade Li
 * @since 2022/8/26
 */
public class NIOClient {

    @SneakyThrows
    public static void start() {
        // 创建客户端通道
        SocketChannel clientChannel = SocketChannel.open();
        // 连接服务端
        boolean connected = clientChannel.connect(new InetSocketAddress("127.0.0.1", 9999));
        if (!connected) {
            System.out.println("连接失败");
            return;
        }
        // 向服务端发送消息
        String msg = "Hello server!";
        clientChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        // 读取服务端消息
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int len = clientChannel.read(buffer);
        // 打印服务端消息
        System.out.println(new String(buffer.array(), 0, len, StandardCharsets.UTF_8));
        // 关闭连接
        clientChannel.close();
    }

    @SneakyThrows
    public static void startWithSelector() {
        // 创建客户端
        @Cleanup SocketChannel client = SocketChannel.open();
        // 采用非阻塞模式
        client.configureBlocking(false);
        // 连接服务端
        client.connect(new InetSocketAddress("127.0.0.1", 9999));
        // 创建通道选择器
        @Cleanup Selector selector = Selector.open();
        // 注册到 Selector 监听 OP_CONNECT
        client.register(selector, SelectionKey.OP_CONNECT);
        // Selector 阻塞等待事件
        while (selector.select() > 0) {
            // 遍历准备好的事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                // 当连接服务端成功时
                if (selectionKey.isConnectable()) {
                    connectHandler(selector, selectionKey);
                }
                // 当有数据可读时
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey);
                }
                // 移除已处理的 key
                iterator.remove();
            }
        }
    }

    private static void connectHandler(Selector selector, SelectionKey selectionKey) throws IOException {
        // 获取通道
        SocketChannel server = (SocketChannel) selectionKey.channel();
        // 检测连接是否完成，当连接未完成时
        if (server.isConnectionPending()) {
            // 继续完成连接，调用该方法时会阻塞，直到完成连接或连接失败
            server.finishConnect();
        }
        // 配置为非阻塞模式
        server.configureBlocking(false);
        // 注册到 Selector 监听 OP_READ
        server.register(selector, SelectionKey.OP_READ);
        // 连接成功后，向服务端发送数据
        String message = "Hello server!";
        server.write(ByteBuffer.wrap(message.getBytes()));
    }

    private static void readHandler(SelectionKey selectionKey) throws IOException {
        // 获取服务端
        SocketChannel server = (SocketChannel) selectionKey.channel();
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 读取服务端信息
        int length = server.read(buffer);
        // 打印消息
        System.out.println(new String(buffer.array(), 0, length));
    }
}
