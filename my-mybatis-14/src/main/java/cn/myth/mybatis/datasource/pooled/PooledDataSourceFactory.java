package cn.myth.mybatis.datasource.pooled;

import cn.myth.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }

}
