package com.github.cadecode.java.practice.java8.io;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO Server
 *
 * @author Cade Li
 * @since 2022/8/12
 */
public class NIOServer {

    @SneakyThrows
    public static void start() {
        // 创建客户端套接字通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 绑定监听端口号
        serverChannel.bind(new InetSocketAddress(9999));
        // 等待客户端连接，阻塞
        SocketChannel clientChannel = serverChannel.accept();
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 读取客户端消息
        int len = clientChannel.read(buffer);
        // 打印客户端消息
        System.out.println(new String(buffer.array(), 0, len, StandardCharsets.UTF_8));
        // 向客户端返回消息
        String msg = "Hello client!";
        clientChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
        // 关闭通道
        serverChannel.close();
        clientChannel.close();
    }

    @SneakyThrows
    public static void startWithSelector() {
        // 创建服务端套字节通道
        @Cleanup ServerSocketChannel server = ServerSocketChannel.open();
        // 绑定端口
        server.bind(new InetSocketAddress(9999));
        // 服务端配置为非阻塞模式
        server.configureBlocking(false);
        // 创建通道选择器
        @Cleanup Selector selector = Selector.open();
        // 服务端注册到 Selector 监听 OP_ACCEPT
        server.register(selector, SelectionKey.OP_ACCEPT);
        // Selector 阻塞等待事件
        while (selector.select() > 0) {
            // 遍历准备好的事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                // 当有客户端连接时
                if (selectionKey.isAcceptable()) {
                    acceptHandler(server, selector);
                }
                // 当有客户端需要读取数据时
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey);
                }
                // 移除已处理的 key
                iterator.remove();
            }
        }
    }

    private static void acceptHandler(ServerSocketChannel server, Selector selector) throws IOException {
        // 获取客户端
        SocketChannel client = server.accept();
        // 客户端配置为非阻塞模式
        client.configureBlocking(false);
        // 客户端注册到 Selector 监听 OP_READ
        client.register(selector, SelectionKey.OP_READ);
        // 向客户端传输信息
        String message = "Hello client!";
        client.write(ByteBuffer.wrap(message.getBytes()));
    }

    private static void readHandler(SelectionKey selectionKey) throws IOException {
        // 获取客户端
        SocketChannel client = (SocketChannel) selectionKey.channel();
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 读取客户端信息
        int length = client.read(buffer);
        // 打印客消息
        System.out.println(new String(buffer.array(), 0, length));
    }
}
