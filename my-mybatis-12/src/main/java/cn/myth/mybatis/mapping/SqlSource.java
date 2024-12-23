package cn.myth.mybatis.mapping;

/**
 * SQL源码
 * 主要作用创建一个SQL语句
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
