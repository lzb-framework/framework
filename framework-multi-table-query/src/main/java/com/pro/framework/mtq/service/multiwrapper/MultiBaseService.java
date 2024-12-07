package com.pro.framework.mtq.service.multiwrapper;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.pro.framework.jdbc.sqlexecutor.DbAdaptor;
import com.pro.framework.mtq.service.multiwrapper.entity.IMultiBaseService;
import com.pro.framework.mtq.service.multiwrapper.util.MultiRelationCaches;
import com.pro.framework.mtq.service.multiwrapper.util.MultiTuple3;
import com.pro.framework.mtq.service.multiwrapper.util.MultiUtil;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@SuppressWarnings("SqlNoDataSourceInspection")
@Slf4j
@Component
@Data
public class MultiBaseService<T> extends MultiBaseReadService<T> implements IMultiBaseService<T> {

    @Autowired
    private DbAdaptor multiDbAdaptor;

    @Override
    public boolean save(T entity) {
        Class<?> clazz = entity.getClass();
        Map<Field, Object> fieldValMap = getFieldValMap(getFields(clazz), entity);
        return this.save(entity, fieldValMap);
    }

    @Override
    public boolean saveOrUpdate(T entity) {
        Class<?> clazz = entity.getClass();
        Map<Field, Object> fieldValMap = getFieldValMap(getFields(clazz), entity);
        Field idField = this.getIdField(clazz);
        Object idVal = fieldValMap.get(idField);
        if (idVal == null) {
            return this.save(entity, fieldValMap);
        } else {
            return this.updateById(entity, clazz, idField, idVal, fieldValMap);
        }
    }

    @Override
    public boolean updateById(T entity) {
        Class<?> clazz = entity.getClass();
        Map<Field, Object> fieldValMap = getFieldValMap(getFields(clazz), entity);
        Field idField = this.getIdField(clazz);
        Object idVal = fieldValMap.get(idField);
        return updateById(entity, clazz, idField, idVal, fieldValMap);
    }


    @Override
    // 根据主键删除实体
    @SneakyThrows
    public boolean removeById(Class<?> clazz, Serializable id) {
        Field idField = getIdField(clazz);
        // 构造删除 SQL 语句
        String sql = "DELETE FROM " + MultiUtil.camelToUnderline(clazz.getSimpleName()) + " WHERE " + MultiUtil.camelToUnderline(idField.getName()) + "=?";
        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        // 执行删除操作
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setObject(1, id);
        return preparedStatement.executeUpdate() > 0;
    }

    @Override
    @SneakyThrows
    public boolean saveBatch(Collection<T> entityList) {
        if (entityList.isEmpty()) {
            return true; // 没有数据需要保存
        }

        T firstEntity = entityList.iterator().next();
        Class<?> clazz = firstEntity.getClass();
        List<Field> fields = getFields(clazz);

        // 构造插入 SQL 语句
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(MultiUtil.camelToUnderline(clazz.getSimpleName())).append("(");

        for (Field field : fields) {
            sql.append(MultiUtil.camelToUnderline(field.getName())).append(",");
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(") VALUES (");

        for (Field ignored : fields) {
            sql.append("?,");
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(")");

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        @Cleanup PreparedStatement ps = connection.prepareStatement(sql.toString());
        // 批量执行插入
        for (T entity : entityList) {
            Map<Field, Object> fieldValMap = getFieldValMap(fields, entity);
            int parameterIndex = 1;
            for (Field field : fields) {
                setVal(ps, parameterIndex, fieldValMap.get(field));
                parameterIndex++;
            }
            ps.addBatch();
        }

        int[] results = ps.executeBatch();
        for (int result : results) {
            if (result <= 0) {
                return false; // 任何一次插入失败都返回 false
            }
        }
        return true;

    }

    @Override
    @SneakyThrows
    public boolean removeBatchByIds(Collection<?> idList) {
        if (idList.isEmpty()) {
            return true; // 没有数据需要删除
        }

        Class<?> clazz = idList.iterator().next().getClass();
        Field idField = getIdField(clazz);

        // 构造删除 SQL 语句
        String sql = "DELETE FROM " + MultiUtil.camelToUnderline(clazz.getSimpleName()) +
                " WHERE " + MultiUtil.camelToUnderline(idField.getName()) + "=?";

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 批量执行删除
        for (Object id : idList) {
            preparedStatement.setObject(1, id);
            preparedStatement.addBatch();
        }

        int[] results = preparedStatement.executeBatch();
        for (int result : results) {
            if (result <= 0) {
                return false; // 任何一次删除失败都返回 false
            }
        }
        return true;
    }

    @Override
    @SneakyThrows
    public boolean updateBatchById(Collection<T> entityList) {
        if (entityList.isEmpty()) {
            return true; // 没有数据需要更新
        }

        T firstEntity = entityList.iterator().next();
        Class<?> clazz = firstEntity.getClass();
        List<Field> fields = getFields(clazz);
        Field idField = getIdField(clazz);

        // 构造更新 SQL 语句
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(MultiUtil.camelToUnderline(clazz.getSimpleName())).append(" SET ");

        for (Field field : fields) {
            if (!field.equals(idField)) {
                sql.append(MultiUtil.camelToUnderline(field.getName())).append("=?,");
            }
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(" WHERE ").append(MultiUtil.camelToUnderline(idField.getName())).append("=?");

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        // 批量执行更新
        for (T entity : entityList) {
            Map<Field, Object> fieldValMap = getFieldValMap(fields, entity);
            Object idValue = fieldValMap.get(idField);
            int parameterIndex = 1;
            for (Field field : fields) {
                if (!field.equals(idField)) {
                    preparedStatement.setObject(parameterIndex++, fieldValMap.get(field));
                }
            }
            preparedStatement.setObject(parameterIndex, idValue);
            preparedStatement.addBatch();
        }

        int[] results = preparedStatement.executeBatch();
        for (int result : results) {
            if (result <= 0) {
                return false; // 任何一次更新失败都返回 false
            }
        }
        return true;
    }

//    @SneakyThrows
//    public boolean executeSql(String sql) {
//        @Cleanup Connection connection = multiDbAdaptor.getConnection();
//        // 执行删除操作
//        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
//        return preparedStatement.executeUpdate() > 0;
//    }
//    public static EnumTypeHandler enumTypeHandler = new EnumTypeHandler<>();


    // 新增实体
    @SneakyThrows
    private boolean save(T entity, Map<Field, Object> fieldValMap) {
        Class<?> clazz = entity.getClass();
        // 构造插入 SQL 语句
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(MultiUtil.camelToUnderline(clazz.getSimpleName())).append("(");


        List<Field> fields = new ArrayList<>(fieldValMap.keySet());

        for (Field field : fields) {
            sql.append(MultiUtil.camelToUnderline(field.getName())).append(",");
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(") VALUES (");

        for (Field ignored : fields) {
            sql.append("?,");
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(")");
//        Connection connection = multiDbAdaptor.getConnection();
        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        // 执行插入操作
//            PreparedStatement ps = connection.prepareStatement(sql.toString());
        @Cleanup PreparedStatement ps = connection.prepareStatement(sql.toString());
        int parameterIndex = 1;
        for (Field field : fields) {
            setVal(ps, parameterIndex, fieldValMap.get(field));
            parameterIndex++;
        }
        return ps.executeUpdate() > 0;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    private static void setVal(PreparedStatement ps, int parameterIndex, Object val) throws SQLException {
        // 枚举类型
        if (val instanceof Enum) {
            Serializable value;
            if (val instanceof IEnum) {
                value = ((IEnum) val).getValue();
            } else {
                value = ((Enum<?>) val).name();
            }
            ps.setObject(parameterIndex, value);
        } else {
            ps.setObject(parameterIndex, val);
        }
    }

    // 保存或更新实体
    @SneakyThrows
    private boolean updateById(T entity, Class<?> clazz, Field idField, Object idValue, Map<Field, Object> fieldValMap) {
        // 如果主键值为 null，则执行插入操作
        if (idValue == null) {
            return save(entity);
        }

        // 构造更新 SQL 语句
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(MultiUtil.camelToUnderline(clazz.getSimpleName())).append(" SET ");

        List<Field> fields = new ArrayList<>(fieldValMap.keySet());

        for (Field field : fields) {
            if (!field.equals(idField)) {
                sql.append(MultiUtil.camelToUnderline(field.getName())).append("=?,");
            }
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(" WHERE ").append(MultiUtil.camelToUnderline(idField.getName())).append("=?");
        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        // 执行更新操作
        @Cleanup PreparedStatement ps = connection.prepareStatement(sql.toString());
        int parameterIndex = 1;
        for (Field field : fields) {
            if (!field.equals(idField)) {
                setVal(ps, parameterIndex, fieldValMap.get(field));
                parameterIndex++;
            }
        }
        ps.setObject(parameterIndex, idValue);
        int i = 0;
        try {
            i = ps.executeUpdate();
        } catch (SQLException e) {
            log.error("updateById {}", sql);
            throw e;
        }
        return i > 0;
    }


    @SneakyThrows
    private static <T> Map<Field, Object> getFieldValMap(List<Field> fields, T entity) {
        Map<Field, Object> fieldValMap = new LinkedHashMap<>();
        for (Field field : fields) {
            if (!field.canAccess(entity)) {
                field.setAccessible(true);
            }
            Object val = field.get(entity);
            if (val != null) {
                fieldValMap.put(field, val);
            }
        }
        return fieldValMap;
    }


    private static List<Field> getFields(Class<?> clazz) {
        return MultiRelationCaches.getClassInfosFull(clazz).values().stream().map(MultiTuple3::getT1).collect(Collectors.toList());
    }

    // 获取实体类的主键字段
    private Field getIdField(Class<?> clazz) {
        return MultiRelationCaches.getTableIdField(clazz);
    }
}
