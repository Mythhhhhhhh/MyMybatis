package cn.myth.mybatis.datasource.unpooled;

import cn.myth.mybatis.datasource.DataSourceFactory;
import cn.myth.mybatis.reflection.MetaObject;
import cn.myth.mybatis.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected DataSource dataSource;

    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    /**
     * 之前我们对于数据源中属性信息的获取都是采用的硬编码，那么这回在setProperties方法中则可以使用SystemMetaObject.forObject(dataSource)获取DataSource的元对象了
     * 也就是通过反射就能把我们需要的属性值设置进去
     * 这样在数据源 UnpooledDataSource、PooledDataSource中就可以拿到对应的属性值信息了，而不是我们那种在两个数据源的实现中硬编码操作
     */

    @Override
    public void setProperties(Properties pros) {
        MetaObject metaObject = SystemMetaObject.forObject(dataSource);
        for (Object key : pros.keySet()) {
            String propertyName = (String) key;
            if (metaObject.hasSetter(propertyName)) {
                String value = (String) pros.get(propertyName);
                Object convertedValue = convertValue(metaObject, propertyName, value);
                metaObject.setValue(propertyName, convertedValue);
            }
        }
    }

    @Override
    public DataSource getDataSource() {
//        UnpooledDataSource unpooledDataSource = new UnpooledDataSource();
//        unpooledDataSource.setDriver(props.getProperty("driver"));
//        unpooledDataSource.setUrl(props.getProperty("url"));
//        unpooledDataSource.setUsername(props.getProperty("username"));
//        unpooledDataSource.setPassword(props.getProperty("password"));
//        return unpooledDataSource;
        return dataSource;
    }

    /**
     * 根据setter的类型，将配置文件中的值强转成相应的类型
     */
    private Object convertValue(MetaObject metaObject, String propertyName, String value) {
        Object convertedValue = value;
        Class<?> targetType = metaObject.getGetterType(propertyName);
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }
}
