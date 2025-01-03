package cn.myth.mybatis.scripting.xmltags;

import cn.myth.mybatis.reflection.MetaObject;
import cn.myth.mybatis.session.Configuration;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态上下文
 * 此类在RawSqlSource中使用，对传入的parameterObject对象进行'map'化处理,并且提供存储和获取Sql方法
 */
public class DynamicContext {

    // 在编写映射文件时，'${parameter}','databasedId'分别可以取到当前用户传入的参数，以及当前执行的数据库类型
    public static final String PARAMETER_OBJECT_KEY = "_parameter";
    // _databaseId可以指定不同的数据库支持
    public static final String DATABASE_ID_KEY = "_databaseId";

    static {
        // 定义属性->getter方法映射，ContextMap到ContextAccessor的映射，注册到ognl运行时
        // 参考http://commons.apache.org/proper/commons-ognl/developer-guide.html
        OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
        // 将传入的参数对象统一封装为ContextMap对象（继承了HashMap对象），
        // 然后Ognl运行时环境在动态计算sql语句时，
        // 会按照ContextAccessor中描述的Map接口的方式来访问和读取ContextMap对象，获取计算过程中需要的参数。
        // ContextMap对象内部可能封装了一个普通的POJO对象，也可以是直接传递的Map对象，当然从外部是看不出来的，因为都是使用Map的接口来读取数据。
    }

    private final ContextMap bindings;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private int uniqueNumber = 0;


    // 在DynamicContext的构造函数中，根据传入的参数对象是否为Map类型，有两个不同构造ContextMap的方式。
    // 而ContextMap作为一个继承了HashMap的对象，作用就是用于统一参数的访问方式：用Map接口方法来访问数据。
    // 具体来说，当传入的参数对象不是Map类型时，Mybatis会将传入的POJO对象用MetaObject对象来封装。
    // 当动态计算sql过程需要获取数据时，用Map接口的get方法包装 MetaObject对象的取值过程。

    // 构造函数，对传入的parameterObject对象进行'map'化处理
    // 也就是说，你传入的pojo对象，会被当作一个键值对数据来进行处理，读取这个pojo对象的接口，依然是Map对象（依然是以Map接口方式来进行读取）
    public DynamicContext(Configuration configuration, Object parameterObject) {
        /*
            在DynamicContext的构造函数中，可以看到：
            1.根据传入的参数对象是否为Map类型，有两个不同构造ContextMap的方式
            2.而ContextMap作为一个继承了HashMap的对象，作用就是用于统一参数的访问方式：用Map接口的方法来访问数据。具体来说：
                2.1 当传入的参数对象不是Map类型时，Mybatis会将入的POJO对象用MetaObject对象来封装
                2.2 当动态计算Sql过程需要获取数据时，用Map接口的get方法包装MetaObject对象的取值过程
                2.3 ContextMap覆写的get方法正式为了上述目的，具体参见下面的'ContextMap'覆写的get方法里的详细解释
         */

        // 绝大多数调用的地方parameterObject为null
        if (parameterObject != null && !(parameterObject instanceof Map)) {
            // 如果不是map型
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            bindings = new ContextMap(metaObject);
        } else {
            bindings = new ContextMap(null);
        }
        // 向刚构造出来的ContextMap实例中推入用户本次传入的参数parameterObject
        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
        // 向刚构造出来的ContextMap实例中推入用户配置的DatabaseId
        bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }
    public void bind(String name, Object value) {
        bindings.put(name, value);
    }

    public void appendSql(String sql) {
        sqlBuilder.append(sql);
        sqlBuilder.append(" ");
    }

    public String getSql() {
        return sqlBuilder.toString().trim();
    }


    public int getUniqueNumber() {
        return uniqueNumber++;
    }


    // 上下文map，静态内部类
    static class ContextMap extends HashMap<String, Object> {
        private static final long serialVersionUID = 2977601501966151582L;

        private MetaObject parameterMetaObject;
        public ContextMap(MetaObject parameterMetaObject) {
            this.parameterMetaObject = parameterMetaObject;
        }

        @Override
        public Object get(Object key) {
            String strKey = (String) key;
            // 先去map里找
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }
            // 如果没找到，再用ognl表达式去取值
            // 如school[0].class.user
            if (parameterMetaObject != null) {
                // issue #61 do not modify the context when reading
                return parameterMetaObject.getValue(strKey);
            }
            return null;
        }
    }

    // 上下文访问器，静态内部类，实现OGNL的PropertyAccessor
    static class ContextAccessor implements PropertyAccessor {

        @Override
        public Object getProperty(Map context, Object target, Object name) throws OgnlException {
            Map map = (Map) target;

            Object result = map.get(name);
            if (result != null) {
                return result;
            }

            Object parameterObject = context.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map)parameterObject).get(name);
            }

            return null;
        }

        @Override
        public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, value);
        }

        @Override
        public String getSourceAccessor(OgnlContext ognlContext, Object o, Object o1) {
            return null;
        }

        @Override
        public String getSourceSetter(OgnlContext ognlContext, Object o, Object o1) {
            return null;
        }
    }




}
