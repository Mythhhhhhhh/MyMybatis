package cn.myth.mybatis.plugin;

import java.util.Properties;

/**
 * 拦截器接口
 * 面向依赖倒置的入口，插件只是定义标准，具体调用处理结果交由使用方决定
 */
public interface Interceptor {
    /* 用户实现 */

    /**
     * 1.intercept需要由使用方实现
     * 2.plugin和setProperties使用方不做实现
     * 3.setProperties用户设置的属性通过此方法传递过来
     * 4.plugin方法，一个 Interceptor 的实现类就都通过解析的方式，注册到拦截器链中，
     * 在后续需要基于 StatementHandler 语句处理器创建时，就可以使用通过代理的方式，把自定义插件包装到代理方法中
     */
    // 拦截，目的：使用方实现，然后调用到使用方
    Object intercept(Invocation invocation) throws Throwable;

    // 代理
    default Object plugin(Object target) {
        // target=PrepareStatement
        // 把目标类包装成代理类
        return Plugin.wrap(target, this);
    }

    // 设置属性
    default void setProperties(Properties properties) {
        // NOP
    }
}
