package com.github.cadecode.learn.tibrv.common;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjUtil;
import com.github.cadecode.learn.tibrv.common.TibrvAutoConfig.TibrvTransportSender;
import com.github.cadecode.learn.tibrv.common.TibrvProperties.TibrvSendSubject;
import com.github.cadecode.learn.tibrv.common.bean.TibrvMsgBody;
import com.github.cadecode.learn.tibrv.common.bean.TibrvMsgWrapper;
import com.tibco.tibrv.TibrvCmTransport;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Tibrv 工具类
 * 发送消息
 *
 * @author Cade Li
 * @date 2022/10/13
 */
@Slf4j
public class TibrvUtil {

    /**
     * 发送消息
     *
     * @param endpointName Tibrv 配置名称
     * @param body         Tibrv 消息包装之消息体
     */
    public static synchronized void sendMsg(String endpointName, TibrvMsgBody body) {
        TibrvTransportSender sender = TibrvAutoConfig.senderMap().get(endpointName);
        Assert.isNull(sender, "sendMsg TibrvTransportSender cannot be null");
        TibrvMsgGenerator msgGenerator = TibrvMsgHandler.msgGeneratorMap().get(endpointName);
        if (ObjUtil.isNull(msgGenerator)) {
            log.error("sendMsg {} no msgGenerators，body:{}", endpointName, body);
            return;
        }
        TibrvMsgWrapper<? extends TibrvMsgBody> wrapper = msgGenerator.defaultWrap(body);
        sendMsg(endpointName, wrapper);
    }

    /**
     * 发送消息
     *
     * @param endpointName Tibrv 配置名称
     * @param wrapper      Tibrv 消息包装
     */
    public static synchronized void sendMsg(String endpointName, TibrvMsgWrapper<? extends TibrvMsgBody> wrapper) {
        TibrvTransportSender sender = TibrvAutoConfig.senderMap().get(endpointName);
        Assert.isNull(sender, "sendMsg TibrvTransportSender cannot be null");
        TibrvMsgGenerator msgGenerator = TibrvMsgHandler.msgGeneratorMap().get(endpointName);
        if (ObjUtil.isNull(msgGenerator)) {
            log.error("sendMsg {} no msgGenerators，wrapper:{}", endpointName, wrapper);
            return;
        }
        try {
            TibrvMsg tibrvMsg = msgGenerator.getTibrvMsg(sender, wrapper);
            TibrvSendSubject sendSubject = sender.getEndpoint().getSendSubject();
            tibrvMsg.setSendSubject(sendSubject.getSubjectName());
            sendMsg(sender, tibrvMsg);
        } catch (TibrvException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void sendMsg(TibrvTransportSender sender, TibrvMsg tibrvMsg) throws TibrvException {
        sender.getTransport().send(tibrvMsg);
        // 睡眠 1s, 保证 CM 模式下的顺序性
        if (sender.getTransport() instanceof TibrvCmTransport) {
            ThreadUtil.sleep(1000L, TimeUnit.MILLISECONDS);
        }
    }
}
