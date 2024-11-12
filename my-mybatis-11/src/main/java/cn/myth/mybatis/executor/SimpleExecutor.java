package cn.myth.mybatis.executor;

import cn.myth.mybatis.executor.statement.StatementHandler;
import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.ResultHandler;
import cn.myth.mybatis.session.RowBounds;
import cn.myth.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * 简单执行器
 */
public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    // 处理实例化语句处理器、获取连接、语句准备、参数设置、更新Sql等流程，其实也算是个模板模式的思路
    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            // 新建一个 StatementHandler 调用创建语句处理器-PreparedStatementHandler
            // update方法没有 resultHandler、boundSql 两个参数
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
            // 准备语句
            stmt = prepareStatement(handler);
            // StatementHandler.update
            return handler.update(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            Configuration configuration = ms.getConfiguration();
            // 新建一个 StatementHandler
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, rowBounds, resultHandler, boundSql);
            // 准备语句
            stmt = prepareStatement(handler);
            // 返回结果
            return handler.query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }


    private Statement prepareStatement(StatementHandler handler) throws SQLException {
        Statement stmt;
        Connection connection = transaction.getConnection();
        // 准备语句 调用语句处理器-准备操作，如初始化参数
        stmt = handler.prepare(connection);
        // 设置参数
        handler.parameterize(stmt);
        return stmt;
    }
}
