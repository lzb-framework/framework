package com.pro.framework.api.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.pro.framework.api.FrameworkConst;

public class BeanUtils extends BeanUtil {
    public static void copyPropertiesModel(Object o1, Object o2) {
        copyProperties(o1, o2, CopyOptions.create().ignoreNullValue().setIgnoreProperties(FrameworkConst.Str.MODEL_IGNORE_PROPERTIES));
    }
}
