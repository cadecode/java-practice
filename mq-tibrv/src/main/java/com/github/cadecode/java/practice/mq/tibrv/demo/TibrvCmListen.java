package com.github.cadecode.java.practice.mq.tibrv.demo;

import com.tibco.tibrv.*;
import lombok.SneakyThrows;

import java.util.Date;
import java.util.Objects;

/**
 * Tibco CM 监听示例
 *
 * @author Cade Li
 * @date 2022/10/11
 */
public class TibrvCmListen {

    @SneakyThrows
    public static void listen(String service, String network, String daemon, String cmName, String subject) {
        // 开启 Tibrv 服务
        Tibrv.open();
        // 创建队列
        TibrvQueue queue = new TibrvQueue();
        // 创建 RVD 通道
        TibrvRvdTransport rvdTransport = new TibrvRvdTransport(service, network, daemon);
        // 创建 CM 通道
        TibrvCmTransport cmTransport = new TibrvCmTransport(rvdTransport, cmName, true);
        // 创建监听
        TibrvCmListener cmListener = new TibrvCmListener(queue, (tibrvListener, tibrvMsg) -> {
            try {
                System.out.println((new Date()) +
                        ": subject=" + tibrvMsg.getSendSubject() +
                        ", reply=" + tibrvMsg.getReplySubject() +
                        ", message=" + tibrvMsg);

                // byte[] asBytes = tibrvMsg.getAsBytes();
                // String msgStr = new String(asBytes, StandardCharsets.UTF_8);
                // System.out.println(msgStr);

                Object data = tibrvMsg.get("xmlData");
                if (data instanceof byte[]) {
                    System.out.println(new String((byte[]) data));
                }


                System.out.flush();
                long seq = TibrvCmMsg.getSequence(tibrvMsg);
                if (seq > 0) {
                    ((TibrvCmListener) tibrvListener).confirmMsg(tibrvMsg);
                }
                if (Objects.nonNull(tibrvMsg.getReplySubject())) {
                    TibrvMsg replyMsg = new TibrvMsg(tibrvMsg);
                    cmTransport.sendReply(replyMsg, tibrvMsg);
                }
            } catch (TibrvException e) {
                e.printStackTrace();
            }
        }, cmTransport, subject, null);
        // 需要手动确认
        cmListener.setExplicitConfirm();
        // Dispatch
        TibrvDispatcher dispatcher = new TibrvDispatcher(queue);
        dispatcher.join();
    }
}
