package cn.myth.mybatis.executor.resulset;

import cn.myth.mybatis.io.Resources;
import cn.myth.mybatis.mapping.ResultMap;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.type.JdbcType;
import cn.myth.mybatis.type.TypeHandler;
import cn.myth.mybatis.type.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * 结果集包装器
 */
public class ResultSetWrapper {

    // JDBC返回的结果集
    private final ResultSet resultSet;
    // 类型处理注册机，可获取具体类型
    private final TypeHandlerRegistry typeHandlerRegistry;
    // 属性名称
    private final List<String> columnNames = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    // JDBC类型
    private final List<JdbcType> jdbcTypes = new ArrayList<>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    // 已被映射的缓存
    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    // 未被映射的缓存
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
        super();
        this.resultSet = rs;
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        // 处理属性名称封装，jdbc类型封装，属性类型封装
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
            jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
            classNames.add(metaData.getColumnClassName(i));
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public List<String> getColumnNames() {
        return this.columnNames;
    }

    public List<String> getClassNames() {
        return this.classNames;
    }

    // 获取类型处理器，在从jdbc结果集中获取值可以通过对应的类型去获取了
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
        TypeHandler<?> handler = null;
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if (columnHandlers == null) {
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        } else {
            handler = columnHandlers.get(propertyType);
        }
        if (handler == null) {
            // 从注册器获取当前类型处理器
            handler = typeHandlerRegistry.getTypeHandler(propertyType, null);
            columnHandlers.put(propertyType, handler);
        }
        return handler;
    }

    private Class<?> resolveClass(String className) {
        try {
            return Resources.classForName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // 获取未被映射的属性
    public List<String> getUnMappedColumnNamesMap(ResultMap resultMap, String columnPrefix) throws SQLException {
        List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if (unMappedColumnNames == null) {
            // 加载没有被映射的属性名
            loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            // 从缓存获取未被映射的属性名数据
            unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }
        return unMappedColumnNames;
    }

    // 加载没有被映射的属性名
    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
        // 有被映射的属性名
        List<String> mappedColumNames = new ArrayList<>();
        // 没有被映射的属性名
        List<String> unmappedColumnNames = new ArrayList<>();
        final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
        final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
        // 已被映射的属性名
        for (String columnName : columnNames) {
            final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
            if (mappedColumns.contains(upperColumnName)) {
                mappedColumNames.add(upperColumnName);
            } else {
                unmappedColumnNames.add(columnName);
            }
        }
        mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumNames);
        unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }

    private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
        if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
            return columnNames;
        }
        final Set<String> prefixed = new HashSet<>();
        for (String columnName : columnNames) {
            prefixed.add(prefix + columnName);
        }
        return prefixed;
    }

    private String getMapKey(ResultMap resultMap, String columnPrefix) {
        return resultMap.getId() + ":" + columnPrefix;
    }
}
