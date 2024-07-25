package com.github.cadecode.learn.tibrv.demo;

import cn.hutool.core.io.resource.ResourceUtil;
import com.tibco.tibrv.*;
import lombok.SneakyThrows;

/**
 * Tibco CM 发送示例
 *
 * @author Cade Li
 * @date 2022/10/11
 */
public class TibrvCmSend {

    public static String xmlData;

    static {
        xmlData = ResourceUtil.readUtf8Str("xmlData.xml");
    }

    @SneakyThrows
    public static void send(String service, String network, String daemon,
                            String cmName, String cmSubject, String subject) {
        // 开启 Tibrv 服务
        Tibrv.open();
        // 创建 RVD 通道
        TibrvRvdTransport rvdTransport = new TibrvRvdTransport(service, network, daemon);
        // 创建 CM 通道
        TibrvCmTransport cmTransport = new TibrvCmTransport(rvdTransport, cmName, true);
        // 创建监听
        new TibrvListener(Tibrv.defaultQueue(), (tibrvListener, tibrvMsg) -> {
            System.out.println("消息已被确认：" + tibrvMsg);
            System.out.flush();
        }, rvdTransport, cmSubject, null);
        // Dispatch
        new TibrvDispatcher(Tibrv.defaultQueue());
        // 循环发送测试
        int index = 0;
        while (index < 2) {
            // 创建消息
            TibrvMsg tibrvMsg = new TibrvMsg();
            tibrvMsg.setSendSubject(subject);
            TibrvCmMsg.setTimeLimit(tibrvMsg, 10);
            // tibrvMsg.update("Data", "aa".getBytes(StandardCharsets.UTF_8), TibrvMsg.OPAQUE);
            tibrvMsg.update("xmlData", xmlData);
            System.out.println("发送了消息：" + tibrvMsg);
            index++;
            // 发送
            cmTransport.send(tibrvMsg);
            Thread.sleep(500L);
        }
    }
}
