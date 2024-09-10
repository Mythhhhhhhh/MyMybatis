package cn.myth.mybatis.builder;

import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.type.TypeAliasRegistry;

/**
 * 构建器的基类，建造者模式
 */
public class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }


}
