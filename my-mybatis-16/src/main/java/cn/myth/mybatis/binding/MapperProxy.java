package cn.myth.mybatis.binding;

import cn.myth.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 映射器代理类
 * @param <T>
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    /**
     * 通过实现 InvocationHandler#invoke 代理类接口，封装操作逻辑的方式，对外接口提供数据库操作对象。
     */

    private static final long serialVersionUID = -6424540398559729838L;

    private SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            // Object 提供的 toString、hashCode 等方法是不需要代理执行的
            return method.invoke(this, args);
        } else {
            final MapperMethod mapperMethod = cachedMapperMethod(method);
            return mapperMethod.execute(sqlSession, args);
        }
    }

    /**
     * 去缓存中找MapperMethod
     */
    private MapperMethod cachedMapperMethod(Method method) {
        MapperMethod mapperMethod = methodCache.get(method);
        if (mapperMethod == null) {
            //找不到才去new
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }
}
