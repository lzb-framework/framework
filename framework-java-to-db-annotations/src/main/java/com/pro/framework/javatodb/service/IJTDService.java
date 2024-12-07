package com.pro.framework.javatodb.service;

import com.pro.framework.api.IReloadService;
import com.pro.framework.javatodb.model.JTDTableInfoVo;

public interface IJTDService extends IReloadService {
    // implements InitializingBean
    // @Override
    // @PostConstruct
    void executeSql();

    JTDTableInfoVo readTableInfo(Class<?> clazz);
}
