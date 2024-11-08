package cn.myth.mybatis.mapping;

/**
 * SQL源码
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
