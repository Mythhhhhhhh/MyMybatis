package cn.myth.mybatis.session.defaults;

import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.SqlSession;
import cn.myth.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }

}
