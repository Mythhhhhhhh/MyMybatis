package cn.myth.mybatis.executor.keygen;

import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 不用键值生成器
 */
public class NoKeyGenerator implements KeyGenerator {

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do nothing
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }
}
