package cn.myth.mybatis.scripting;

import cn.myth.mybatis.executor.parameter.ParameterHandler;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * 脚本语言驱动
 * 提供创建SQL信息的方法，入参包括了配置、元素、参数
 * 它的实现类一共有3个：XMLLanguageDriver、RawLanguageDriver、VelocityLanguageDriver
 * 这里我们实现默认的第一个即可
 */
public interface LanguageDriver {

    /**
     * 创建SQL源码(mapper xml方式)
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建SQL源码(annotation 注解方式)
     */
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

    /**
     * 创建参数处理器
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
