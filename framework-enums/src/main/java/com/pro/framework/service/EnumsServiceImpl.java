package com.pro.framework.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.pro.framework.EnumProperties;
import com.pro.framework.api.entity.IEntityProperties;
import com.pro.framework.api.enums.*;
import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.api.util.BeanUtils;
import com.pro.framework.api.util.CollUtils;
import com.pro.framework.api.util.LogicUtils;
import com.pro.framework.enums.EnumConstant;
import com.pro.framework.enums.EnumUtil;
import com.pro.framework.jdbc.service.IBaseService;
import com.pro.framework.model.dto.EnumData;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 枚举入库服务
 */
@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
@Slf4j
@AllArgsConstructor
public class EnumsServiceImpl implements IEnumsService {

    private IBaseService dbBaseService;
    private EnumProperties enumProperties;
    private IEntityProperties entityProperties;

    /**
     * 枚举入库
     */
    @Override
    public void executeSql() {
        EnumConstant.load(enumProperties);
        Class<IEnumToDbEnum> intf = IEnumToDbEnum.class;
        List<Class> enumClasses = EnumConstant.simpleNameClassMapNoReplace.values().stream().filter(intf::isAssignableFrom)
                .sorted(Comparator.comparing(c -> null == c.getAnnotation(EnumOrder.class) ? 100 : c.getAnnotation(EnumOrder.class).value()))
                .collect(Collectors.toList());
        enumClasses.forEach(enumClass -> {
            // 读取枚举实例,入库
            try {
                // 获取一个子类在实现接口使用的,泛型类
                Class entityClass = getGeneClass(enumClass, intf);
                // 定制类
                entityClass = entityProperties.getEntityClassReplaceMap().getOrDefault(entityClass.getSimpleName(), entityClass);
                AssertUtil.isTrue(IEnumToDbDb.class.isAssignableFrom(entityClass), "{} must implements IEnumToDbDb", entityClass.getSimpleName());

                readEnumInstancesSaveOrUpdate(dbBaseService, enumClass, entityClass);
            } catch (Exception e) {
                log.error("枚举入库完成 存在异常 {}", enumClass, e);
            }
        });
        log.warn("枚举入库完成 扫描了: {}", enumClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(",")));
    }

    @Override
    public Class<? extends Enum> getEnumClass(String simpleClassName) {
        return EnumConstant.simpleNameClassMap.get(simpleClassName);
    }

    @Override
    public List<Map<String, Object>> getFullList(Class<? extends Enum> eClass) {
        return EnumUtil.getFullList(eClass);
    }

    /**
     * 读取枚举中的所有枚举实例,并保存
     */
    @SneakyThrows
    private <ENUM extends Enum<?> & IEnumToDbEnum<ENTITY>, ENTITY extends IEnumToDbDb> void readEnumInstancesSaveOrUpdate(IBaseService<ENTITY> dbBaseService, Class<ENUM> enumClass, Class<ENTITY> entityClass) {
        LocalDateTime now = LocalDateTime.now();
        // 读取枚举
        List<EnumData<ENTITY>> enumDatas = readEnumInstances(enumClass, entityClass);
        // 查询旧数据
        List<ENTITY> oldEntitys = dbBaseService.getList(entityClass.newInstance());

//        log.error("oldEntitys={}", JSONUtil.toJsonStr(oldEntitys));
        Map<String, ENTITY> oldMap = CollUtils.listToMap(oldEntitys, ENTITY::getEnumToDbCode);
        Set<String> oldCodes = oldMap.keySet();

//        boolean banner = entityClass.getSimpleName().equals("Banner");
//        if (banner) {
//            int i = 0;
//        }
        // 新增
        List<ENTITY> creates = enumDatas.stream().filter(enumData -> !oldCodes.contains(enumData.getEnumToDbCode())).peek(enumData -> enumData.getEntity().setEnumToDbCode(enumData.getEnumToDbCode())).map(EnumData::getEntity).collect(Collectors.toList());
        if (!creates.isEmpty()) {
            log.warn("自动保存数据| {} {}", enumClass.getSimpleName(), JSONUtil.toJsonStr(creates));
        }
        dbBaseService.saveBatch(creates);
        // 更新
        List<ENTITY> updates = enumDatas.stream().filter(enumData -> {
            String code = enumData.getEnumToDbCode();
            ENTITY oldEntity = oldMap.get(code);
            LocalDateTime forceTime = LogicUtils.and(enumData.getForceChangeTime(), DateUtil::parseLocalDateTime);
            LocalDateTime oldUpdateTime = null == oldEntity ? null : oldEntity.getUpdateTime();
            boolean doUpdate = oldCodes.contains(code) && null != forceTime && null != oldUpdateTime && !forceTime.isBefore(oldUpdateTime);
            if (doUpdate) {
                // 回填id
                enumData.getEntity().setEnumToDbCode(oldEntity.getEnumToDbCode());
                enumData.getEntity().setUpdateTime(now);
            }
            return doUpdate;
        }).map(EnumData::getEntity).collect(Collectors.toList());
        dbBaseService.updateBatchById(updates);
        if (!creates.isEmpty()) {
            log.info("枚举入库 {} 新增:{} {}", enumClass, creates.size(), creates.stream().map(ENTITY::getEnumToDbCode).collect(Collectors.joining(",")));
        }
        if (!updates.isEmpty()) {
            log.info("枚举入库 {} 修改:{} {}", enumClass, updates.size(), updates.stream().map(ENTITY::getEnumToDbCode).collect(Collectors.joining(",")));
        }
        enumDatas.stream().filter(e->e.getEnumToDbCode().contains("count")).findAny().ifPresent(enumData -> {
            log.info("枚举入库 count {}", enumClass);
        });
        int i = 0;
    }

    /**
     * 读取枚举中的所有枚举实例
     */
    public static <ENUM extends Enum<?> & IEnumToDbEnum<ENTITY>, ENTITY extends IEnumToDbDb> List<EnumData<ENTITY>> readEnumInstances(Class<ENUM> enumClass, Class<ENTITY> entityClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(e -> EnumsServiceImpl.readEnumInstance(e, entityClass)).collect(Collectors.toList());
    }

    /**
     * 读取枚举实例
     */
    @SneakyThrows
    private static <ENUM extends Enum<?> & IEnumToDbEnum<ENTITY>, ENTITY extends IEnumToDbDb> EnumData<ENTITY> readEnumInstance(ENUM enumInstance, Class<ENTITY> entityClass) {
        // 创建实体对象
        ENTITY entity = entityClass.newInstance();

        // 获取枚举值的所有字段
//        Field[] fields = enumInstance.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            if (!field.canAccess(entity)) {
//                field.setAccessible(true);
//            }
//            Object value = field.get(enumInstance);
//            Field entityField = entityClass.getDeclaredField(field.getName());
//            entityField.setAccessible(true);
//            entityField.set(entity, value);
//        }
        BeanUtils.copyProperties(enumInstance, entity);
        String code = enumInstance.getToDbCode();
        entity.setEnumToDbCode(code);
        return new EnumData<>(entity, code, enumInstance.getForceChangeTime());
    }

    /**
     * 获取一个子类在实现接口使用的,泛型类
     */
    @SneakyThrows
    public static Class<?> getGeneClass(Class<?> chlidClass, Class<?> interfaceClass) {
        Type[] genericInterfaces = chlidClass.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                Type rawType = parameterizedType.getRawType();
                if (rawType.equals(interfaceClass)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        Type typeArgument = actualTypeArguments[0];
                        if (typeArgument instanceof Class) {
                            return (Class<?>) typeArgument;
                        }
                    }
                }
            }
        }
        EnumToDbEnum annotation = chlidClass.getAnnotation(EnumToDbEnum.class);
        if (annotation != null) {
            return Class.forName(annotation.entityClass());
        }
        throw new IllegalArgumentException("Unable to determine generic type.");
    }

    @Override
    public void reload() {
        executeSql();
    }
}
