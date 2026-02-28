package com.github.cadecode.java.practice.mq.tibrv.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.github.cadecode.java.practice.mq.tibrv.common.TibrvProperties.TibrvEndpoint;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tibrv 消息处理器
 *
 * @author Cade Li
 * @date 2022/10/19
 */
@Slf4j
@RequiredArgsConstructor
public class TibrvMsgHandler implements TibrvMsgCallback {

    private static Map<String, TibrvMsgGenerator> MSG_GENERATOR_MAP;
    private static Map<String, Map<String, TibrvMsgParser<?, ?>>> MSG_PARSER_MAP;

    private final List<TibrvMsgParser<?, ?>> parsers;
    private final List<TibrvMsgGenerator> generators;

    public static Map<String, TibrvMsgGenerator> msgGeneratorMap() {
        return MSG_GENERATOR_MAP;
    }

    public static Map<String, Map<String, TibrvMsgParser<?, ?>>> msgParserMap() {
        return MSG_PARSER_MAP;
    }

    public void afterPropertiesSet() {
        // 将消息生成器按 endpoint 分类
        MSG_GENERATOR_MAP = generators.stream()
                .collect(Collectors.groupingBy(o -> {
                    TibrvMsgBinder binder = o.getClass().getAnnotation(TibrvMsgBinder.class);
                    return binder.endpoint();
                }, Collectors.collectingAndThen(Collectors.toList(), l -> l.get(0))));
        // 将消息解析器按 endpoint 和 消息名称分类
        MSG_PARSER_MAP = parsers.stream()
                .collect(Collectors.groupingBy(o -> {
                    TibrvMsgBinder binder = o.getClass().getAnnotation(TibrvMsgBinder.class);
                    return binder.endpoint();
                }, Collectors.groupingBy(o -> {
                    TibrvMsgBinder binder = o.getClass().getAnnotation(TibrvMsgBinder.class);
                    return binder.messageName();
                }, Collectors.collectingAndThen(Collectors.toList(), l -> l.get(0)))));
        // Map 不可修改
        MSG_GENERATOR_MAP = Collections.unmodifiableMap(MSG_GENERATOR_MAP);
        MSG_PARSER_MAP = Collections.unmodifiableMap(MSG_PARSER_MAP);
    }

    @Override
    public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
        // 获取 Tibrv 配置信息
        TibrvEndpoint endpoint = TibrvAutoConfig.listenerMap().get(tibrvListener);
        try {
            // 获取 dataField 的 data
            String data;
            try {
                data = (String) tibrvMsg.get(endpoint.getDataField());
            } catch (TibrvException e) {
                log.error("TibrvEndpoint {} get dataField error，TibrvMsg:{}", endpoint.getName(), tibrvMsg);
                return;
            }
            if (Objects.isNull(data)) {
                log.error("TibrvEndpoint {} data is null，dataField:{}，TibrvMsg:{}", endpoint.getName(),
                        endpoint.getDataField(), tibrvMsg);
                return;
            }
            // 获取消息名称解析器
            TibrvMsgGenerator msgGenerator = msgGeneratorMap().get(endpoint.getName());
            if (ObjUtil.isNull(msgGenerator)) {
                log.error("TibrvEndpoint {} no TibrvMsgGenerator，TibrvMsg:{}", endpoint.getName(), tibrvMsg);
                return;
            }
            // 需要定制化获取 MsgName
            String msgName = msgGenerator.getMsgName(tibrvMsg, endpoint, data);
            // 获取对应解析器
            Map<String, TibrvMsgParser<?, ?>> msgParserMap = msgParserMap().get(endpoint.getName());
            if (CollUtil.isEmpty(msgParserMap)) {
                log.error("TibrvEndpoint {} no TibrvMsgParser Map，TibrvMsg:{}", endpoint.getName(), tibrvMsg);
                return;
            }
            TibrvMsgParser<?, ?> msgParser = msgParserMap.get(msgName);
            if (ObjUtil.isNull(msgParser)) {
                log.error("TibrvEndpoint {} no TibrvMsgParser，msgName {}，TibrvMsg:{}", endpoint.getName(), msgName, tibrvMsg);
                return;
            }
            msgParser.doParse(tibrvListener, tibrvMsg, endpoint, data);
        } catch (Exception e) {
            log.error("TibrvEndpoint {} handler onMsg error", endpoint.getName(), e);
        }
    }

    /**
     * 系统消息回调, 处理 initTransport 中定义的系统消息监听
     */
    public static class TibrvSystemMsgHandler implements TibrvMsgCallback {

        @Override
        public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
            TibrvEndpoint endpoint = TibrvAutoConfig.listenerMap().get(tibrvListener);
            try {
                String subject = tibrvListener.getSubject();
                if (Objects.equals(subject, TibrvSysSubjectConst.RVD_CONNECTED)) {
                    log.debug("TibrvEndpoint {} connected", endpoint.getName());
                    return;
                }
                if (Objects.equals(subject, TibrvSysSubjectConst.RVD_DISCONNECTED)) {
                    log.debug("TibrvEndpoint {} disconnected", endpoint.getName());
                    return;
                }
                if (Objects.equals(subject, TibrvSysSubjectConst.RVCM_CONFIRM)) {
                    // TODO 消息确认重传
                    log.debug("TibrvEndpoint {} receive confirm, TibrvMsg:{}", endpoint.getName(), tibrvMsg);
                }
            } catch (Exception e) {
                log.error("TibrvEndpoint {} TibrvUtil onMsg error", endpoint.getName(), e);
            }
        }
    }
}
