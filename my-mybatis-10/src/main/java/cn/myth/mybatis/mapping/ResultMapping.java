package cn.myth.mybatis.mapping;

import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.type.JdbcType;
import cn.myth.mybatis.type.TypeHandler;

/**
 * 结果映射
 */
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();
    }
}
