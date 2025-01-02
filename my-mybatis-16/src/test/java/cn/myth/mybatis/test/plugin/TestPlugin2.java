package cn.myth.mybatis.test.plugin;

import cn.myth.mybatis.executor.statement.StatementHandler;
import cn.myth.mybatis.plugin.Interceptor;
import cn.myth.mybatis.plugin.Intercepts;
import cn.myth.mybatis.plugin.Invocation;
import cn.myth.mybatis.plugin.Signature;

import java.sql.Connection;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class TestPlugin2 implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("=============Plugin Test=============");
        return invocation.proceed();
    }
}
