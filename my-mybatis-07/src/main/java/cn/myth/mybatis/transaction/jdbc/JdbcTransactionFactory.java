package cn.myth.mybatis.transaction.jdbc;

import cn.myth.mybatis.session.TransactionIsolationLevel;
import cn.myth.mybatis.transaction.Transaction;
import cn.myth.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 以工厂方法模式包装 JDBC 事务实现，为每一个事务实现都提供一个对应的工厂。与简单工厂的接口包装不同
 */
public class JdbcTransactionFactory implements TransactionFactory {
    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
