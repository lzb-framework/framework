package com.pro.framework.mybatisplus.wrapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;

public class MyLambdaQueryChainWrapper<T>  extends LambdaQueryChainWrapper<T> {
    public MyLambdaQueryChainWrapper(BaseMapper<T> baseMapper) {
        super(baseMapper);
        super.wrapperChildren = new MyLambdaQueryWrapper<>();
    }
}
