package cn.myth.mybatis.scripting.defaults;

import cn.myth.mybatis.builder.SqlSourceBuilder;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.scripting.xmltags.DynamicContext;
import cn.myth.mybatis.scripting.xmltags.SqlNode;
import cn.myth.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * 原始SQL源码，比DynamicSqlSource动态SQL处理快
 * 存储的是只有"#{}"或者没有标签的纯文本信息SQL
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    // 数据Sql解析
    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        // Sql源构建器
        SqlSourceBuilder sqlSourceParse = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        // 解析最终可执行的Sql
        sqlSource = sqlSourceParse.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 获取已绑定过的SQL
        return sqlSource.getBoundSql(parameterObject);
    }

    // 获取Sql
    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        // 将Sql信息存入dynamicContext的sqlBuilder里
        rootSqlNode.apply(context);
        // 从dynamicContext的sqlBuilder里得到Sql文本
        return context.getSql();
    }
}
