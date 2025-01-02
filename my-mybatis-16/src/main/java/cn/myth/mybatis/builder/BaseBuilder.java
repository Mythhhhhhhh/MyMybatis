package cn.myth.mybatis.builder;

import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.type.TypeAliasRegistry;
import cn.myth.mybatis.type.TypeHandler;
import cn.myth.mybatis.type.TypeHandlerRegistry;

/**
 * 构建器的基类，建造者模式
 */
public class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    // 根据别名解析 Class 类型别名注册/事物管理器别名
    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    // 获取TypeHandler
    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null) {
            return null;
        }
        return typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
    }


}
