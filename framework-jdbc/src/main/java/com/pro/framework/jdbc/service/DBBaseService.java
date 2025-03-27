package com.pro.framework.jdbc.service;

import com.pro.framework.api.FrameworkConst;
import com.pro.framework.api.clazz.ClassCaches;
import com.pro.framework.api.database.OrderItem;
import com.pro.framework.api.database.page.IPageInput;
import com.pro.framework.api.database.page.Page;
import com.pro.framework.api.database.page.PageInput;
import com.pro.framework.api.database.wheredata.WhereDataUnit;
import com.pro.framework.api.model.IModel;
import com.pro.framework.api.structure.Tuple3;
import com.pro.framework.api.util.OtherUtil;
import com.pro.framework.api.util.StrUtils;
import com.pro.framework.jdbc.sqlexecutor.DbAdaptor;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@SuppressWarnings("SqlNoDataSourceInspection")
@Slf4j
@Component
@AllArgsConstructor
public class DBBaseService<T extends IModel> implements IBaseService<T> {

    //    @Autowired
    private DbAdaptor multiDbAdaptor;

    @Override
    @SneakyThrows
    public List<T> getList(T entity) {
        return getList(entity, new PageInput(1, 10000));
    }

    @SneakyThrows
    private <T> List<T> executeQuery(String sql, Function<ResultSet, T> function) {
        log.info("Multi 查询Sql:\n {}", sql);
        @Cleanup Connection conn = multiDbAdaptor.getConnection();
        @Cleanup Statement stmt = conn.createStatement();
        @Cleanup ResultSet rs = null;
        List<T> list = new ArrayList<>(2000);
        try {
            rs = stmt.executeQuery(sql);
            if (function == null) {
                return null;
            }
            while (rs.next()) {
                list.add(function.apply(rs));
            }
        } catch (SQLException e) {
            log.error("Multi 查询Sql异常:\n {} \n {}", e.getMessage(), sql);
            throw e;
        }
        return list;
    }

    @Override
    @SneakyThrows
    public List<T> getList(T entity, IPageInput pageInput) {
        //noinspection unchecked
        Class<T> clazz = (Class<T>) entity.getClass();
        String tableName = StrUtils.camelToUnderline(clazz.getSimpleName());

        // 类的具体信息 Map<fieldName,Tuple<Field,setMethod,getMethod>
        Map<String, Tuple3<Field, Method, Method>> classInfosFull = ClassCaches.getClassInfosFull(clazz);
        Map<String, String> propFieldMap = classInfosFull.keySet().stream().collect(Collectors.toMap(prop -> prop, StrUtils::camelToUnderline, (v1, v2) -> v1, LinkedHashMap::new));

        String fields = String.join(",", propFieldMap.values());
        String sql = "SELECT " + fields + " FROM " + tableName; // Base SQL query
        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        // Iterate through the properties of the entity class
        for (Tuple3<Field, Method, Method> fieldFull : classInfosFull.values()) {
            Field field = fieldFull.getT1();
            Method getFun = fieldFull.getT3();
            Object value = getFun.invoke(entity);
            if (value != null) {
                // Append WHERE condition for non-null properties
                if (parameters.size() > 0) {
                    whereClause.append(" AND ");
                }
                whereClause.append(propFieldMap.get(field.getName())).append(" = ?");
                parameters.add(value);
            }
        }
        for (WhereDataUnit whereDataUnit : pageInput.getWhereDataUnits()) {
            if (whereDataUnit.getValues() != null) {
                if (whereClause.length() > 0) {
                    whereClause.append(" AND ");
                } else {
                    whereClause.append(" WHERE ");
                }
                whereClause.append(propFieldMap.get(whereDataUnit.getPropName()))
                        .append(" = ?");
                parameters.add(whereDataUnit.getValues());
            }
        }
        sql += whereClause.toString();

        List<OrderItem> orders = pageInput.getOrders();
        if (!orders.isEmpty()) {
            StringBuilder orderBy = new StringBuilder(" ORDER BY ");
            for (OrderItem orderItem : orders) {
                orderBy.append(propFieldMap.get(orderItem.getColumn())).append(" ");
                if (!orderItem.getAsc()) {
                    orderBy.append("DESC");
                } else {
                    orderBy.append("ASC");
                }
                orderBy.append(",");
            }
            orderBy.deleteCharAt(orderBy.length() - 1); // Remove the trailing comma
            sql += orderBy.toString();
        } else {
            sql += " ORDER BY id ASC";
        }

        Long pageSize = pageInput.getPageSize();
        if (pageSize >= 0) {
            int Offset = (int) ((pageInput.getCurPage() - 1) * pageInput.getPageSize());
            sql += " LIMIT " + pageSize + " OFFSET " + Offset;
        }
        return executeQuery(sql, rs -> mapResultSetToEntity(rs, clazz, classInfosFull));
    }

    @SneakyThrows
    private T mapResultSetToEntity(ResultSet resultSet, Class<T> clazz, Map<String, Tuple3<Field, Method, Method>> classInfosFull) {
        T entity = clazz.getDeclaredConstructor().newInstance();

        // Get all fields of the entity class
        List<Field> fields = getFields(clazz);

        // Iterate through each field and set its value from the ResultSet
        for (Field field : fields) {
            field.setAccessible(true); // Ensure that private fields can be accessed
            if ("MyExecuteTemplate".equals(clazz.getSimpleName())) {
                int i = 0;
            }
            Object value = getValue(StrUtils.camelToUnderline(field.getName()), field.getType(), resultSet);
            field.set(entity, value); // Set the field value in the entity object
        }
        Integer i = 1;
        for (Map.Entry<String, Tuple3<Field, Method, Method>> entry : classInfosFull.entrySet()) {
            Field field = entry.getValue().getT1();
            Method setMethod = entry.getValue().getT2();

            // 取值
            Object value = getValue(i, field.getType(), resultSet);
//            log.debug("setMethod.invoke {} {} | {} {}", field.getName(), field.getType(), null == value ? null : value.getClass(), value);
            setMethod.invoke(entity, value);
            i++;
        }

        return entity;
    }

    public Page<T> getPage(T entity, IPageInput pageInput) {
        // Existing code...
        List<T> entities = getList(entity, pageInput);
        // Execute the count query if requested
        long totalCount = getTotalCount(entity, pageInput.getWhereDataUnits());

        // Construct and return the MultiPage object
        Page<T> multiPage = new Page<>(pageInput.getCurPage(), pageInput.getPageSize());
        multiPage.setRecords(entities);
        multiPage.setTotal(totalCount);
        return multiPage;
    }

    // Method to retrieve the total count of records
    @SneakyThrows
    private long getTotalCount(T entity, List<WhereDataUnit> whereDataUnits) {
        String tableName = StrUtils.camelToUnderline(entity.getClass().getSimpleName());
        // Construct the SQL query to count total records
        String countSql = "SELECT COUNT(*) FROM " + tableName;
        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        // Construct the WHERE clause based on non-null properties
        // Use the same approach as getPage method to construct the WHERE clause

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(countSql);
        // Set parameters and execute the count query
        int parameterIndex = 1;
        for (Object parameter : parameters) {
            preparedStatement.setObject(parameterIndex++, parameter);
        }
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        }
        return 0; // Return 0 if unable to retrieve the total count
    }


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
        String sql = "DELETE FROM " + StrUtils.camelToUnderline(clazz.getSimpleName()) + " WHERE " + StrUtils.camelToUnderline(idField.getName()) + "=?";
        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        // 执行删除操作
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setObject(1, id);
        return preparedStatement.executeUpdate() > 0;
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    // 按照非空字段名称拼接的字符串分组保存实体对象
    public boolean saveBatch(Collection<T> entityList) {
        if (entityList.isEmpty()) {
            return true; // 没有数据需要保存
        }

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
//        T firstEntity = entityList.iterator().next();
        Class<?> clazz = entityList.iterator().next().getClass();
        List<Field> fields = getFields(clazz);

        // 构建分组
        Map<String, List<T>> groupedEntities = new LinkedHashMap<>();
        Map<String, List<Field>> groupedFields = new LinkedHashMap<>();
        String keyBefore = FrameworkConst.Str.EMPTY;
        // 按原来的顺序,同样的字段一起入库(按字段分组)
        int keyNo = 0;
        for (T entity : entityList) {
            List<Field> nonNullFields = new ArrayList<>();
            for (Field field : fields) {
                if (!field.canAccess(entity)) {
                    field.setAccessible(true);
                }
                Object val = field.get(entity);
                if (val != null) {
                    nonNullFields.add(field);
                }
            }
            if (nonNullFields.isEmpty()) {
                continue; // 所有字段都为 null，跳过该实体对象
            }
            String key = nonNullFields.stream().map(Field::getName).sorted().collect(Collectors.joining("_"));
            if (!keyBefore.equals(key)) {
                keyNo++;
            }
            groupedEntities.computeIfAbsent(key + "_" + keyNo, k -> new ArrayList<>()).add(entity);
            groupedFields.computeIfAbsent(key + "_" + keyNo, k -> nonNullFields);
        }

        try {
            // 执行批量插入
            for (String key : groupedEntities.keySet()) {
                saveGroup(connection, clazz, groupedFields.get(key), groupedEntities.get(key));
            }
        } catch (Exception e) {
            log.error("saveBatch error {}", clazz, e);
            throw e;
        }
        return true;
    }

    @SneakyThrows
    private static <T extends IModel> void saveGroup(Connection connection, Class<?> clazz, List<Field> nonNullFields, List<T> group) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(StrUtils.camelToUnderline(clazz.getSimpleName())).append("(");

        for (Field field : nonNullFields) {
            sql.append(StrUtils.camelToUnderline(field.getName())).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");

        for (int i = 0; i < nonNullFields.size(); i++) {
            sql.append("?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");

        log.info("执行sql: {}", sql);
        @Cleanup PreparedStatement ps = connection.prepareStatement(sql.toString());
        for (T entity : group) {
            int parameterIndex = 1;
            for (Field field : nonNullFields) {
                Object val = field.get(entity);
                setVal(ps, parameterIndex, val);
                parameterIndex++;
            }
            ps.addBatch();
        }
        ps.executeBatch();
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
        String sql = "DELETE FROM " + StrUtils.camelToUnderline(clazz.getSimpleName()) +
                " WHERE " + StrUtils.camelToUnderline(idField.getName()) + "=?";

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
        sql.append(StrUtils.camelToUnderline(clazz.getSimpleName())).append(" SET ");

        List<Field> nonNullFields = new ArrayList<>();
        for (Field field : fields) {
            if (!field.equals(idField)) {
                Object val = field.get(firstEntity);
                if (val != null) {
                    sql.append(StrUtils.camelToUnderline(field.getName())).append("=?,");
                    nonNullFields.add(field);
                }
            }
        }
        if (nonNullFields.isEmpty()) {
            return true; // 实体对象中除主键外所有字段都为 null，不执行更新操作
        }

        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(" WHERE ").append(StrUtils.camelToUnderline(idField.getName())).append("=?");

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
        // 批量执行更新
        for (T entity : entityList) {
            Map<Field, Object> fieldValMap = getFieldValMap(fields, entity);
            Object idValue = fieldValMap.get(idField);
            int parameterIndex = 1;
            for (Field field : nonNullFields) {
                preparedStatement.setObject(parameterIndex++, fieldValMap.get(field));
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


    @SneakyThrows
    public boolean executeSql(String sql) {
        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        // 执行删除操作
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        return preparedStatement.executeUpdate() > 0;
    }
//    public static EnumTypeHandler enumTypeHandler = new EnumTypeHandler<>();


    // 新增实体
    @SneakyThrows
    private boolean save(T entity, Map<Field, Object> fieldValMap) {
        Class<?> clazz = entity.getClass();
        // 构造插入 SQL 语句
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(StrUtils.camelToUnderline(clazz.getSimpleName())).append("(");

        // Convert fieldValMap to a list of property-value pairs
        List<Props> propsList = new ArrayList<>();
        for (Map.Entry<Field, Object> entry : fieldValMap.entrySet()) {
            propsList.add(new Props(entry.getKey(), entry.getValue()));
        }

        // Filter out null values
        List<Props> nonNullPropsList = propsList.stream()
                .filter(props -> props.getValue() != null)
                .collect(Collectors.toList());

        for (Props props : nonNullPropsList) {
            sql.append(StrUtils.camelToUnderline(props.getField().getName())).append(",");
        }
        // Check if there are any non-null properties to insert
        if (nonNullPropsList.isEmpty()) {
            // No non-null properties to insert, return false
            return false;
        }

        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(") VALUES (");

        for (int i = 0; i < nonNullPropsList.size(); i++) {
            sql.append("?,");
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(")");

        @Cleanup Connection connection = multiDbAdaptor.getConnection();
        @Cleanup PreparedStatement ps = connection.prepareStatement(sql.toString());
        int parameterIndex = 1;
        for (Props props : nonNullPropsList) {
            setVal(ps, parameterIndex, props.getValue());
            parameterIndex++;
        }
        return ps.executeUpdate() > 0;
    }

    // Helper class to represent property-value pairs
    private static class Props {
        private final Field field;
        private final Object value;

        public Props(Field field, Object value) {
            this.field = field;
            this.value = value;
        }

        public Field getField() {
            return field;
        }

        public Object getValue() {
            return value;
        }
    }


    private static void setVal(PreparedStatement ps, int parameterIndex, Object val) throws SQLException {
        // 枚举类型
        if (val instanceof Enum) {
            Serializable value;
            value = ((Enum<?>) val).name();
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
        sql.append(StrUtils.camelToUnderline(clazz.getSimpleName())).append(" SET ");

        List<Field> fields = new ArrayList<>(fieldValMap.keySet());

        for (Field field : fields) {
            if (!field.equals(idField)) {
                sql.append(StrUtils.camelToUnderline(field.getName())).append("=?,");
            }
        }
        sql.deleteCharAt(sql.length() - 1); // 移除末尾的逗号
        sql.append(" WHERE ").append(StrUtils.camelToUnderline(idField.getName())).append("=?");
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
        return ps.executeUpdate() > 0;
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
        return ClassCaches.getClassInfosFull(clazz).values().stream().map(Tuple3::getT1).collect(Collectors.toList());
    }

    // 获取实体类的主键字段
    private Field getIdField(Class<?> clazz) {
        return ClassCaches.getTableIdField(clazz, "id");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows
    public static Object getValue(String fieldName, Class<?> type, ResultSet resultSet) {
        switch (type.getSimpleName()) {
            case "Long":
                return resultSet.getLong(fieldName);
            case "String":
                return resultSet.getString(fieldName);
            case "Integer":
                return resultSet.getInt(fieldName);
            case "BigDecimal":
                return resultSet.getBigDecimal(fieldName);
            case "Date":
                return resultSet.getDate(fieldName);
            case "Boolean":
                return resultSet.getBoolean(fieldName);
            case "LocalDateTime":
                return OtherUtil.date2LocalDateTime(resultSet.getTimestamp(fieldName));
            case "LocalDate":
                return resultSet.getDate(fieldName).toLocalDate();
            case "LocalTime":
                return resultSet.getTime(fieldName).toLocalTime();

            case "Float":
                return resultSet.getFloat(fieldName);
            case "Double":
                return resultSet.getDouble(fieldName);
            case "Blob":
                return resultSet.getBlob(fieldName);
            default:
                if (Enum.class.isAssignableFrom(type)) {
                    String value = resultSet.getString(fieldName);
                    return null == value ? null : Enum.valueOf((Class<Enum>) type, value);
                }
                throw new IllegalArgumentException("Unsupported data type: " + type.getSimpleName());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows
    public static Object getValue(Integer i, Class<?> type, ResultSet resultSet) {
        switch (type.getSimpleName()) {
            case "Long":
                return resultSet.getLong(i);
            case "String":
                return resultSet.getString(i);
            case "Integer":
                return resultSet.getInt(i);
            case "BigDecimal":
                return resultSet.getBigDecimal(i);
            case "Date":
                return resultSet.getDate(i);
            case "Boolean":
                return resultSet.getBoolean(i);
            case "LocalDateTime":
                return OtherUtil.date2LocalDateTime(resultSet.getTimestamp(i));
            case "LocalDate":
                return resultSet.getDate(i).toLocalDate();
            case "LocalTime":
                return resultSet.getTime(i).toLocalTime();

            case "Float":
                return resultSet.getFloat(i);
            case "Double":
                return resultSet.getDouble(i);
            case "Blob":
                return resultSet.getBlob(i);
            default:
                if (Enum.class.isAssignableFrom(type)) {
                    String value = resultSet.getString(i);
                    return null == value ? null : Enum.valueOf((Class<Enum>) type, value);
                }
                throw new IllegalArgumentException("Unsupported data type: " + type.getSimpleName());
        }
    }
}
