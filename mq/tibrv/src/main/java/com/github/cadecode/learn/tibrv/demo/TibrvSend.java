package com.github.cadecode.learn.tibrv.demo;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvRvdTransport;
import lombok.SneakyThrows;

/**
 * Tibco RV 发送示例
 *
 * @author Cade Li
 * @date 2022/10/11
 */
public class TibrvSend {

    @SneakyThrows
    public static void send(String service, String network, String daemon, String subject) {
        // 开启 Tibrv 服务
        Tibrv.open();
        // 创建 RVD 通道
        TibrvRvdTransport rvdTransport = new TibrvRvdTransport(service, network, daemon);
        // 循环发送测试
        int index = 0;
        while (true) {
            // 创建消息
            TibrvMsg tibrvMsg = new TibrvMsg();
            tibrvMsg.setSendSubject(subject);
            tibrvMsg.update("DATA", index);
            System.out.println("发送了消息：" + index++);
            // 发送
            rvdTransport.send(tibrvMsg);
            Thread.sleep(2000L);
        }
    }
}
