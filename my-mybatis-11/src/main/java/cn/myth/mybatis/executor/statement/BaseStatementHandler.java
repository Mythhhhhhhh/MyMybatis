package cn.myth.mybatis.executor.statement;

import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.executor.parameter.ParameterHandler;
import cn.myth.mybatis.executor.resulset.ResultSetHandler;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.ResultHandler;
import cn.myth.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 语句处理器抽象基类
 * 在语句处理器基类中，将参数信息，结果信息进行封装处理。
 */
public abstract class BaseStatementHandler implements StatementHandler {

    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;

    protected final Object parameterObject;
    protected final ResultSetHandler resultSetHandler;
    protected final ParameterHandler parameterHandler;

    protected final RowBounds rowBounds;
    protected BoundSql boundSql;

    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        // 新增判断，因为update不会传入boundSql参数，所以这里需要做初始化处理
        if (boundSql == null) {
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }
        this.boundSql = boundSql;

        this.parameterObject = parameterObject;
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, resultHandler, boundSql);
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            // 实例化 Statement
            statement = instantiateStatement(connection);
            // 参数设置，可以被抽取，提供配置
            statement.setQueryTimeout(350);
            statement.setFetchSize(10000);
            return statement;
        } catch (Exception e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }
    }

    // 定义实例化的抽象方法，这个方法交由各个具体的实现子类进行处理（
    // 包括SimpleStatementHandler简单语句处理器和PreparedStatementHandler预处理语句处理器）
    // 简单语句处理器只是对SQL的最基本执行，没有参数的设置
    // 预处理语句处理器则是我们在JDBC中使用的最多的操作方式，PreparedStatement设置SQL，传递参数的设置过程
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;
}
