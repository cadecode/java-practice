package com.github.cadecode.learn.tibrv.common;

import java.lang.annotation.*;

/**
 * 配合 TibrvMsgParser 绑定消息的处理器
 *
 * @author Cade Li
 * @date 2022/10/19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TibrvMsgBinder {

    String endpoint() default "";

    String messageName() default "";

}
