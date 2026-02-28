package com.github.cadecode.java.practice.mq.tibrv.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.cadecode.java.practice.mq.tibrv.common.TibrvProperties;
import com.tibco.tibrv.TibrvMsg;
import lombok.Data;

/**
 * Tibrv 消息包装
 *
 * @author Cade Li
 * @date 2022/10/13
 */
@Data
public abstract class TibrvMsgWrapper<T extends TibrvMsgBody> {

    @JsonIgnore
    private TibrvMsg tibrvMsg;

    @JsonIgnore
    private TibrvProperties.TibrvEndpoint endpoint;

    public abstract TibrvMsgHeader getMsgHeader();

    public abstract T getMsgBody();

    public abstract TibrvMsgReturn getMsgReturn();

}
