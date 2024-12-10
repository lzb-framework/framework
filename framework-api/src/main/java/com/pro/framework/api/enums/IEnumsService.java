package com.pro.framework.api.enums;

import com.pro.framework.api.IReloadService;

import java.util.List;
import java.util.Map;

/**
 * 枚举入库服务
 */
@SuppressWarnings("deprecation")
public interface IEnumsService extends IReloadService {

    void executeSql();

    Class<? extends Enum> getEnumClass(String simpleClassName);

    List<Map<String, Object>> getFullList(Class<? extends Enum> eClass);
}
