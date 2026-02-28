package com.github.cadecode.java.practice.mq.tibrv.common;

import com.github.cadecode.java.practice.mq.tibrv.common.TibrvProperties.TibrvEndpoint;
import com.github.cadecode.java.practice.mq.tibrv.common.bean.TibrvMsgBody;
import com.github.cadecode.java.practice.mq.tibrv.common.bean.TibrvMsgWrapper;
import com.tibco.tibrv.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Tibrv 消息解析器
 * 通过 @TibrvMsgBinder 指定 endpoint 和 messagesName
 * 注：接收消息后解析时需要实现此接口，注入容器
 *
 * @author Cade Li
 * @date 2022/10/18
 */
@Slf4j
public abstract class TibrvMsgParser<W extends TibrvMsgWrapper<B>, B extends TibrvMsgBody> {

    public abstract W msgToWrapper(String data);

    public abstract boolean validateMsg(TibrvEndpoint endpoint, W wrapper, B msgBody);

    public abstract void parseMsg(TibrvEndpoint endpoint, W wrapper, B msgBody);

    public void confirmMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg, TibrvEndpoint endpoint, W wrapper) throws TibrvException {
        if (tibrvListener instanceof TibrvCmListener) {
            long seq = TibrvCmMsg.getSequence(tibrvMsg);
            if (seq > 0) {
                ((TibrvCmListener) tibrvListener).confirmMsg(tibrvMsg);
            }
            log.debug("TibrvEndpoint {} parser confirmed, seq:{}, messageName:{}, transactionId:{}",
                    endpoint.getName(), TibrvCmMsg.getSequence(tibrvMsg),
                    wrapper.getMsgHeader().getMessageName(), wrapper.getMsgHeader().getTransactionId());
        }
    }

    public void handleError(TibrvListener tibrvListener, TibrvMsg tibrvMsg, TibrvEndpoint endpoint, W wrapper, Exception e) {
        throw new RuntimeException(e);
    }

    public void doParse(TibrvListener tibrvListener, TibrvMsg tibrvMsg, TibrvEndpoint endpoint, String data) {
        W wrapper = null;
        try {
            try {
                wrapper = msgToWrapper(data);
            } catch (Exception e) {
                log.error("TibrvEndpoint {} parser xml to wrapper error, TibrvMsg:{}", endpoint.getName(), tibrvMsg, e);
                return;
            }
            if (Objects.isNull(wrapper)) {
                log.error("TibrvEndpoint {} parser xml to wrapper return null, TibrvMsg:{}", endpoint.getName(), tibrvMsg);
                return;
            }
            log.debug("TibrvEndpoint {} parser receive {}, header:{}, body:{}, return:{}", endpoint.getName(),
                    wrapper.getMsgHeader().getMessageName(), wrapper.getMsgHeader(), wrapper.getMsgBody(),
                    wrapper.getMsgReturn());
            boolean validateFlag = validateMsg(endpoint, wrapper, wrapper.getMsgBody());
            if (!validateFlag) {
                log.error("TibrvEndpoint {} parser receive {}, validate error", endpoint.getName(),
                        wrapper.getMsgHeader().getMessageName());
                return;
            }
            // 解析消息
            parseMsg(endpoint, wrapper, wrapper.getMsgBody());
            // 确认消息
            confirmMsg(tibrvListener, tibrvMsg, endpoint, wrapper);
        } catch (TibrvException e) {
            handleError(tibrvListener, tibrvMsg, endpoint, wrapper, e);
        }
    }
}
