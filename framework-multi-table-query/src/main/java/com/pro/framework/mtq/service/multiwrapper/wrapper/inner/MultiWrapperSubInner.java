package com.pro.framework.mtq.service.multiwrapper.wrapper.inner;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 */
@Data
@NoArgsConstructor
@SuppressWarnings("unused")
public class MultiWrapperSubInner<SUB, Wrapper extends MultiWrapperSubInner<SUB, Wrapper>> extends MultiWrapperCommonInner<SUB, Wrapper> {
    public MultiWrapperSubInner(Class<SUB> clazz) {
        super(clazz);
    }
}
