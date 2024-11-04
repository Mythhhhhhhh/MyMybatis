package cn.myth.mybatis.session;

import cn.myth.mybatis.binding.MapperRegistry;
import cn.myth.mybatis.datasource.druid.DruidDataSourceFactory;
import cn.myth.mybatis.datasource.pooled.PooledDataSourceFactory;
import cn.myth.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.executor.SimpleExecutor;
import cn.myth.mybatis.executor.parameter.ParameterHandler;
import cn.myth.mybatis.executor.resulset.DefaultResultSetHandler;
import cn.myth.mybatis.executor.resulset.ResultSetHandler;
import cn.myth.mybatis.executor.statement.PreparedStatementHandler;
import cn.myth.mybatis.executor.statement.StatementHandler;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.Environment;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.reflection.MetaObject;
import cn.myth.mybatis.reflection.factory.DefaultObjectFactory;
import cn.myth.mybatis.reflection.factory.ObjectFactory;
import cn.myth.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.myth.mybatis.reflection.wrapper.ObjectWrapperFactory;
import cn.myth.mybatis.scripting.LanguageDriver;
import cn.myth.mybatis.scripting.LanguageDriverRegistry;
import cn.myth.mybatis.scripting.xmltags.XMLLanguageDriver;
import cn.myth.mybatis.transaction.Transaction;
import cn.myth.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.myth.mybatis.type.TypeAliasRegistry;
import cn.myth.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  配置项
 *  整个Mybatis的操作都是使用Configuration配置项进行串联流程，所以所有的内容都会在Configuration中进行衔接
 */
public class Configuration {

    // 环境
    protected Environment environment;

    // 映射注册机
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    // 映射的语句，存在Map里
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    // 类型别名注册机
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    // 脚本语言注册机
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    // 类型处理注册机
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    // 对象工厂和对象包装器工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final Set<String> loadedResources = new HashSet<>();

    protected String databaseId;

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);

        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
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

    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * 创建结果集处理器
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this, transaction);
    }

    /**
     * 创建语句处理器
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        return new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, boundSql);
    }

    // 创建元对象
    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    /**
     * 类型处理器注册机
     */
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        //创建参数处理器
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 插件的一些参数，也是在这里处理，暂时不添加这部分内容 interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

}
