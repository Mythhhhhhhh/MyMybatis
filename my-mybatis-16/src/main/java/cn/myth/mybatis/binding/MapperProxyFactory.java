package cn.myth.mybatis.binding;

import cn.myth.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理类工厂
 */
public class MapperProxyFactory<T> {
    /**
     * 工厂操作相当于把代理的创建给封装起来了，如果不做这层封装，
     * 那么每一个创建代理类的操作，都需要自己使用 Proxy.newProxyInstance 进行处理，
     * 那么这样的操作方式就显得比较麻烦了。
     */

    private final Class<T> mapperInterface;

    private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession){
        final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);

    }

}
