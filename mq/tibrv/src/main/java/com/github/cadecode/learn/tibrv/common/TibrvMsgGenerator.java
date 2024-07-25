package com.github.cadecode.learn.tibrv.common;

import com.github.cadecode.learn.tibrv.common.TibrvAutoConfig.TibrvTransportSender;
import com.github.cadecode.learn.tibrv.common.TibrvProperties.TibrvEndpoint;
import com.github.cadecode.learn.tibrv.common.bean.TibrvMsgBody;
import com.github.cadecode.learn.tibrv.common.bean.TibrvMsgWrapper;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * 消息生成器，负责包装业务消息实体到 TibrvMsg，负责从 TibrvMsg 解析出 MsgName 以匹配 TibrvMsgParser
 * 通过 @TibrvMsgBinder 指定 endpoint，不需要指定 messageName，因为一个 endpoint 一般使用一套消息结构
 * 注：发消息时需要实现此接口，注入容器
 *
 * @author Cade Li
 * @date 2023/2/28
 */
public abstract class TibrvMsgGenerator {

    public abstract TibrvMsgWrapper<? extends TibrvMsgBody> defaultWrap(TibrvMsgBody body);

    public abstract TibrvMsg getTibrvMsg(TibrvTransportSender sender, TibrvMsgWrapper<? extends TibrvMsgBody> wrapper) throws TibrvException;

    /**
     * 从 TibrvMsg 的 data 中获取 header messageName
     */
    public abstract String getMsgName(TibrvMsg tibrvMsg, TibrvEndpoint endpoint, String data);
}
