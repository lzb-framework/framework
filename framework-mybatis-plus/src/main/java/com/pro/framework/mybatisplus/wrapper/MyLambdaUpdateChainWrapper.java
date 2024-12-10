package com.pro.framework.mybatisplus.wrapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;

public class MyLambdaUpdateChainWrapper<T>  extends LambdaUpdateChainWrapper<T> {
    public MyLambdaUpdateChainWrapper(BaseMapper<T> baseMapper) {
        super(baseMapper);
        super.wrapperChildren = new MyLambdaUpdateWrapper<>();
    }
}
