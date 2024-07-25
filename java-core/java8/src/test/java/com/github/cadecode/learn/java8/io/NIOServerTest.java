package com.github.cadecode.learn.java8.io;

import org.junit.jupiter.api.Test;

/**
 * NIO Server 测试
 *
 * @author Cade Li
 * @since 2023/10/17
 */
public class NIOServerTest {

    @Test
    public void startServer() {
        NIOServer.startWithSelector();
    }

    @Test
    public void startClient() {
        NIOClient.startWithSelector();
    }
}
