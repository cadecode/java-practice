package com.github.cadecode.java.practice.mq.tibrv.demo;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvRvdTransport;
import lombok.SneakyThrows;

import java.util.Date;

/**
 * Tibco RV 监听示例
 *
 * @author Cade Li
 * @date 2022/10/11
 */
public class TibrvListen {

    @SneakyThrows
    public static void listen(String service, String network, String daemon, String subject) {
        // 开启 Tibrv 服务
        Tibrv.open();
        // 创建 RVD 通道
        TibrvRvdTransport rvdTransport = new TibrvRvdTransport(service, network, daemon);
        // 创建监听
        new TibrvListener(Tibrv.defaultQueue(), (tibrvListener, tibrvMsg) -> {
            System.out.println((new Date()) +
                    ": subject=" + tibrvMsg.getSendSubject() +
                    ", reply=" + tibrvMsg.getReplySubject() +
                    ", message=" + tibrvMsg
            );
        }, rvdTransport, subject, null);
        while (true) {
            Tibrv.defaultQueue().dispatch();
        }
    }

}
