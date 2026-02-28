package com.github.cadecode.java.practice.mq.tibrv.demo;

import com.tibco.tibrv.*;
import lombok.SneakyThrows;

import java.util.Date;

/**
 * Tibco CM 监听示例
 *
 * @author Cade Li
 * @date 2022/10/11
 */
public class TibrvCmQueueListen {

    @SneakyThrows
    public static void listen(String service, String network, String daemon, String cmName, String subject) {
        // 开启 Tibrv 服务
        Tibrv.open();
        // 创建队列
        TibrvQueue queue = new TibrvQueue();
        // 创建 RVD 通道
        TibrvRvdTransport rvdTransport = new TibrvRvdTransport(service, network, daemon);
        // 创建 CM 通道
        TibrvCmQueueTransport cmQueueTransport = new TibrvCmQueueTransport(rvdTransport, cmName);
        // 创建监听
        TibrvCmListener cmListener = new TibrvCmListener(queue, (tibrvListener, tibrvMsg) -> {
            try {
                String msg = new Date() +
                        ": subject=" + tibrvMsg.getSendSubject() +
                        ", reply=" + tibrvMsg.getReplySubject() +
                        ", message=" + tibrvMsg;
                System.out.println(msg);
                System.out.flush();
                long seq = TibrvCmMsg.getSequence(tibrvMsg);
                System.out.println("消息 seq: " + seq);
                if (seq > 0) {
                    ((TibrvCmListener) tibrvListener).confirmMsg(tibrvMsg);
                    System.out.println("消费者已确认：" + msg);
                }
            } catch (TibrvException e) {
                e.printStackTrace();
            }
        }, cmQueueTransport, subject, null);
        // 需要手动确认
        cmListener.setExplicitConfirm();
        // Dispatch
        TibrvDispatcher dispatcher = new TibrvDispatcher(queue);
        dispatcher.join();
    }
}
