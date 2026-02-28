package com.github.cadecode.java.practice.mq.tibrv.common;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Tibrv 配置项
 *
 * @author Cade Li
 * @since 2023/9/6
 */
@Slf4j
@Data
public class TibrvProperties {

    private Boolean enable;

    private List<TibrvEndpoint> endpoints;

    /**
     * Tibrv 端口配置
     */
    @Data
    public static class TibrvEndpoint {

        private String name;

        private Boolean enable;

        private String dataField;

        /**
         * 配置组播
         */
        private String service;

        private String network;

        /**
         * 配置 rvd 服务
         */
        private String daemon;

        public TibrvSendSubject sendSubject;

        public TibrvListenSubject listenSubject;

    }

    /**
     * Tibrv 发送 subject
     */
    @Data
    public static class TibrvSendSubject {

        private String subjectName;

        /**
         * cmName 不为空表示使用 TibrvCmTransport 来监听
         */
        private String cmName;
    }

    /**
     * Tibrv 监听 subject
     */
    @Data
    public static class TibrvListenSubject {

        private String subjectName;

        /**
         * queueName 不为空表示使用 TibrvCmQueueTransport 来监听
         */
        private String queueName;
    }
}
