package com.github.cadecode.java.practice.mq.tibrv.common;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.github.cadecode.java.practice.mq.tibrv.common.TibrvMsgHandler.TibrvSystemMsgHandler;
import com.github.cadecode.java.practice.mq.tibrv.common.TibrvProperties.TibrvEndpoint;
import com.github.cadecode.java.practice.mq.tibrv.common.TibrvProperties.TibrvListenSubject;
import com.github.cadecode.java.practice.mq.tibrv.common.TibrvProperties.TibrvSendSubject;
import com.tibco.tibrv.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Tibrv 自动配置
 *
 * @author Cade Li
 * @date 2022/10/13
 */
@Slf4j
@Data
public class TibrvAutoConfig {

    private static Map<String, TibrvTransportSender> SENDER_MAP = new ConcurrentHashMap<>();
    private static Map<TibrvListener, TibrvEndpoint> LISTENER_MAP = new ConcurrentHashMap<>();

    private final TibrvProperties tibrvProperties;
    private final TibrvMsgHandler msgHandler;
    private final TibrvSystemMsgHandler systemMsgHandler;

    public static Map<String, TibrvTransportSender> senderMap() {
        return SENDER_MAP;
    }

    public static Map<TibrvListener, TibrvEndpoint> listenerMap() {
        return LISTENER_MAP;
    }

    /**
     * 生命周期方法
     * 启动后初始化 Tibrv 配置
     */
    public void onApplicationStarted() {
        // 遍历 Tibrv 配置
        tibrvProperties.getEndpoints().forEach(endpoint -> {
            try {
                TibrvRvdTransport rvdTransport = initTransport(endpoint);
                if (Objects.isNull(rvdTransport)) {
                    return;
                }
                generateSender(endpoint, rvdTransport);
                startListener(endpoint, rvdTransport);
                log.info("TibrvEndpoint {} service config over, service:{}, network:{}, daemon:{}", endpoint.getName(),
                        endpoint.getService(), endpoint.getNetwork(), endpoint.getDaemon());
            } catch (TibrvException e) {
                throw new RuntimeException(e);
            }
        });
        // Map 不可修改
        SENDER_MAP = Collections.unmodifiableMap(SENDER_MAP);
        LISTENER_MAP = Collections.unmodifiableMap(LISTENER_MAP);
    }

    public TibrvRvdTransport initTransport(TibrvEndpoint endpoint) throws TibrvException {
        // 检查 name
        Assert.isTrue(StrUtil.isEmpty(endpoint.getName()), " TibrvEndpointName name cannot be null");
        // 检查 dataField
        Assert.isTrue(StrUtil.isEmpty(endpoint.getDataField()), "TibrvEndpointName dataField cannot be null");
        // 检查是否重复配置
        Assert.isTrue(senderMap().containsKey(endpoint.getName()),
                StrUtil.format("TibrvEndpointName name {} config is repeated", endpoint.getName()));
        // 检查是否启用
        if (Objects.equals(endpoint.getEnable(), false)) {
            log.info("TibrvEndpointName {} enable false", endpoint.getName());
            return null;
        }
        Tibrv.open();
        TibrvRvdTransport rvdTransport = new TibrvRvdTransport(endpoint.getService(), endpoint.getNetwork(), endpoint.getDaemon());
        // 监听系统 subject, 以获取消息确认、连接状态
        Stream.of(TibrvSysSubjectConst.RVCM_CONFIRM,
                        TibrvSysSubjectConst.RVD_CONNECTED,
                        TibrvSysSubjectConst.RVD_DISCONNECTED)
                .forEach(o -> {
                    try {
                        TibrvListener tibrvListener = new TibrvListener(Tibrv.defaultQueue(), systemMsgHandler, rvdTransport, o, null);
                        listenerMap().put(tibrvListener, endpoint);
                    } catch (TibrvException e) {
                        throw new RuntimeException(e);
                    }
                });
        new TibrvDispatcher(Tibrv.defaultQueue());
        return rvdTransport;
    }

    public void generateSender(TibrvEndpoint endpoint, TibrvRvdTransport rvdTransport) throws TibrvException {
        TibrvSendSubject sendSubject = endpoint.getSendSubject();
        if (Objects.isNull(sendSubject)) {
            return;
        }
        TibrvTransportSender sender = TibrvTransportSender.builder().endpoint(endpoint).build();
        // 定义 CM 发送通道
        TibrvCmTransport cmTransport;
        // 判断发送者是否使用 CM 模式
        if (StrUtil.isNotEmpty(sendSubject.getCmName())) {
            cmTransport = new TibrvCmTransport(rvdTransport, sendSubject.getCmName(), true);
            sender.setTransport(cmTransport);
            senderMap().put(endpoint.getName(), sender);
            log.info("TibrvEndpoint {} create transport TibrvCmTransport:{}, cm:{}", endpoint.getName(),
                    sendSubject.getSubjectName(), sendSubject.getCmName());
        } else {
            sender.setTransport(rvdTransport);
            senderMap().put(endpoint.getName(), sender);
            log.info("TibrvEndpoint {} create transport TibrvRvdTransport:{}", endpoint.getName(), sendSubject.getSubjectName());
        }
    }

    public void startListener(TibrvEndpoint endpoint, TibrvRvdTransport rvdTransport) throws TibrvException {
        // 启动定义的监听器
        TibrvListenSubject listenSubject = endpoint.getListenSubject();
        if (Objects.isNull(listenSubject)) {
            return;
        }
        TibrvQueue tibrvQueue = new TibrvQueue();
        TibrvListener listener;
        // 判断监听者是否使用 CMQ 模式
        if (StrUtil.isNotEmpty(listenSubject.getQueueName())) {
            TibrvCmQueueTransport cmQueueTransport = new TibrvCmQueueTransport(rvdTransport, listenSubject.getQueueName());
            listener = new TibrvCmListener(tibrvQueue, msgHandler, cmQueueTransport, listenSubject.getSubjectName(), null);
            ((TibrvCmListener) listener).setExplicitConfirm();
            log.info("TibrvEndpoint {} crate listener TibrvCmListener:{}, queue:{}", endpoint.getName(),
                    listenSubject.getSubjectName(), listenSubject.getQueueName());
        } else {
            listener = new TibrvListener(tibrvQueue, msgHandler, rvdTransport, listenSubject.getSubjectName(), null);
            log.info("TibrvEndpoint {} crate listener TibrvListener:{}", endpoint.getName(), listenSubject.getSubjectName());
        }
        listenerMap().put(listener, endpoint);
        new TibrvDispatcher(tibrvQueue);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TibrvTransportSender {

        private TibrvTransport transport;

        private TibrvEndpoint endpoint;
    }
}

