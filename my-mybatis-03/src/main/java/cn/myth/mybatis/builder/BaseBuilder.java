package cn.myth.mybatis.builder;

import cn.myth.mybatis.session.Configuration;

/**
 * 构建器的基类，建造者模式
 */
public class BaseBuilder {

    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }


}
