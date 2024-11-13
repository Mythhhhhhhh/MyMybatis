package cn.myth.mybatis.scripting.defaults;

import cn.myth.mybatis.builder.SqlSourceBuilder;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.scripting.xmltags.DynamicContext;
import cn.myth.mybatis.scripting.xmltags.SqlNode;
import cn.myth.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * 原始SQL源码
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParse = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParse.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
}
