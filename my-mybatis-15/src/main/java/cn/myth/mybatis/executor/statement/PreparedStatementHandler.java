package cn.myth.mybatis.executor.statement;

import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.executor.keygen.KeyGenerator;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.session.ResultHandler;
import cn.myth.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 预处理语句处理器（PREPARED）
 * 包括instantiateStatement预处理SQL、parameterize设置参数，以及query查询的执行操作
 */
public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        return connection.prepareStatement(sql);
    }


    @Override
    public void parameterize(Statement statement) throws SQLException {
//        PreparedStatement ps = (PreparedStatement) statement;
//        ps.setLong(1, Long.parseLong(((Object[]) parameterObject)[0].toString()));
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    // 执行查询和对结果的封装
    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.<E> handleResultSets(ps);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        // 执行 selectKey 语句
        int rows = ps.getUpdateCount();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
        return rows;
    }
}
