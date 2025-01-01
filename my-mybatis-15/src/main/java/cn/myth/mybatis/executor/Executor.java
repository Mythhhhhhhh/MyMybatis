package cn.myth.mybatis.executor;

import cn.myth.mybatis.mapping.BoundSql;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.session.ResultHandler;
import cn.myth.mybatis.session.RowBounds;
import cn.myth.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器
 * 以Executor接口定义为执行器入口，确认出事物和操作和SQL执行的统一标准接口。
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    // 对于JDBC来说SQL的增删改都是修改，所以增删改的操作只用调用update就可以了
    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    Transaction getTransaction();

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollback);
}
