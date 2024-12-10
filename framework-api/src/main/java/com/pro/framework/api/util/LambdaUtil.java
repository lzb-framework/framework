package com.pro.framework.api.util;

import com.pro.framework.api.util.inner.FunSerializable;
import com.pro.framework.api.util.inner.SerializedLambda;
import com.pro.framework.api.util.inner.SerializedLambdaData;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LambdaUtil {

    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<String, WeakReference<SerializedLambdaData>> FUNC_CACHE = new ConcurrentHashMap<>();

    public static <T> SerializedLambdaData resolveCache(FunSerializable<T, ?> func) {
        Class<?> clazz = func.getClass();
        String name = clazz.getName();
        return Optional.ofNullable(FUNC_CACHE.get(name))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambdaData data = new SerializedLambdaData(resolve(func));
                    SerializedLambda resolve = resolve(func);
                    FUNC_CACHE.put(name, new WeakReference<>(data));
                    return data;
                });
    }

    /**
     * 通过反序列化转换 lambda 表达式，该方法只能序列化 lambda 表达式，不能序列化接口实现或者正常非 lambda 写法的对象
     *
     * @param lambda lambda对象
     * @return 返回解析后的 SerializedLambda
     */
    @SneakyThrows
    private static SerializedLambda resolve(FunSerializable<?, ?> lambda) {
        ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(OtherUtil.serialize(lambda))) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class<?> clazz;
                try {
                    clazz = Class.forName(objectStreamClass.getName(), false, getDefaultClassLoader());
                } catch (Exception ex) {
                    clazz = super.resolveClass(objectStreamClass);
                }
                return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
            }
        };
        return (SerializedLambda) objIn.readObject();
    }
    public static ClassLoader getDefaultClassLoader() {
          ClassLoader cl = null;
          try {
              cl = Thread.currentThread().getContextClassLoader();
          } catch (Throwable ex) {
              // Cannot access thread context ClassLoader - falling back...
          }
          if (cl == null) {
              // No thread context class loader -> use class loader of this class.
              cl = OtherUtil.class.getClassLoader();
              if (cl == null) {
                  // getClassLoader() returning null indicates the bootstrap ClassLoader
                  try {
                      cl = ClassLoader.getSystemClassLoader();
                  } catch (Throwable ex) {
                      // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                  }
              }
          }
          return cl;
      }
}
