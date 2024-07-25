package com.github.cadecode.learn.tibrv.common.bean;

/**
 * Tibrv 消息包装之 header
 *
 * @author Cade Li
 * @date 2022/10/13
 */
public abstract class TibrvMsgHeader {

    public abstract String getMessageName();

    public abstract String getTransactionId();

}
