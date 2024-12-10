package com.pro.framework.api.util;

import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpUtil;

public class HttpUtils extends HttpUtil {
    static {
        HttpGlobalConfig.setTimeout(10000);
    }
}
