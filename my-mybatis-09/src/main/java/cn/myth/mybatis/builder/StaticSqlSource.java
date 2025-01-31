package cn.myth.mybatis.builder;

import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.ParameterMapping;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码
 * 主要创建BoundSql，供其他SqlSource实现类使用，一个中间状态
 */
public class StaticSqlSource implements SqlSource {
    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }



    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
