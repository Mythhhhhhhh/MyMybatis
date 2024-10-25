package cn.myth.mybatis.datasource.pooled;

import cn.myth.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

import javax.sql.DataSource;

public class PooledDataSourceFactory extends UnpooledDataSourceFactory {


    /**
     * 有池化的数据源工厂实现也比较简单,只是继承UnpooledDataSourceFactory共用获取属性的能力，以及实例化出池化数据源即可
     */
    @Override
    public DataSource getDataSource()
    {
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(props.getProperty("driver"));
        pooledDataSource.setUrl(props.getProperty("url"));
        pooledDataSource.setUsername(props.getProperty("username"));
        pooledDataSource.setPassword(props.getProperty("password"));
        return pooledDataSource;
    }
}
