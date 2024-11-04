package cn.myth.mybatis.reflection.wrapper;

import cn.myth.mybatis.reflection.MetaObject;
import cn.myth.mybatis.reflection.factory.ObjectFactory;
import cn.myth.mybatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * 对象包装器
 * 相当于是更进一步反射调用的包装处理，同时也为不同的对象类型提供不同的包装策略
 * 在对象包装器接口中定义了更加明确的需要使用的方法，包括定义出get/set标准的通用方法、获取get/set属性名称和属性类型，以及添加属性等操作
 */
public interface ObjectWrapper {

    // get
    Object get(PropertyTokenizer prop);

    // set
    void set(PropertyTokenizer prop, Object value);

    // 查找属性
    String findProperty(String name, boolean useCamelCaseMapping);

    // 取得getter的名字列表
    String[] getGetterNames();

    // 取得setter的名字列表
    String[] getSetterNames();

    //取得setter的类型
    Class<?> getSetterType(String name);

    // 取得getter的类型
    Class<?> getGetterType(String name);

    // 是否有指定的setter
    boolean hasSetter(String name);

    // 是否有指定的getter
    boolean hasGetter(String name);

    // 实例化属性
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

    // 是否是集合
    boolean isCollection();

    // 添加属性
    void add(Object element);

    // 添加属性
    <E> void addAll(List<E> element);

}
