package com.pro.framework.api.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EnumTopicFramework {
    framework_cache("框架缓存事件消息"),
    ;
    String label;
}
