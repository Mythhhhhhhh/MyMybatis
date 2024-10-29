package cn.myth.mybatis.reflection.invoker;

/**
 * 调用者
 * 关于对象类中的属性值获取和设置可以分为Field字段的get/set还有普通的Method的调用
 * 为了减少使用方的过多的处理，这里可以把集中调用者的实现包装成调用策略，统一接口不同策略不同的实现类
 */
public interface Invoker {

    // 无论任何类型的反射调用，都离不开对象和入参，只要我们把这两个字段和返回结果定义的通用，就可以包住不同策略的实现类了
    Object invoke(Object target, Object[] args) throws Exception;

    Class<?> getType();

}
