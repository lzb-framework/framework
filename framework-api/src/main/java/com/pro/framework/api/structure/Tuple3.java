package com.pro.framework.api.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Administrator
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tuple3<T1,T2,T3>{
    private T1 t1;
    private T2 t2;
    private T3 t3;
}
