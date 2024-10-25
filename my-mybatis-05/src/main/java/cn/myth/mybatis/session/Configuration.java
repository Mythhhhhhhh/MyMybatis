package cn.myth.mybatis.session;

import cn.myth.mybatis.binding.MapperRegistry;
import cn.myth.mybatis.datasource.druid.DruidDataSourceFactory;
import cn.myth.mybatis.datasource.pooled.PooledDataSource;
import cn.myth.mybatis.datasource.pooled.PooledDataSourceFactory;
import cn.myth.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import cn.myth.mybatis.mapping.Environment;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.myth.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 *  配置项
 *  整个Mybatis的操作都是使用Configuration配置项进行串联流程，所以所有的内容都会在Configuration中进行衔接
 */
public class Configuration {

    /**
     * 环境
     */
    protected Environment environment;

    /**
     * 映射注册机
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 映射的语句，存在Map里
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 类型别名注册机
     */
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);

        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
    }


    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }


    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
