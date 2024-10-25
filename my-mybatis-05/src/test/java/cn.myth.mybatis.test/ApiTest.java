package cn.myth.mybatis.test;

import cn.myth.mybatis.datasource.pooled.PooledDataSource;
import cn.myth.mybatis.io.Resources;
import cn.myth.mybatis.session.SqlSession;
import cn.myth.mybatis.session.SqlSessionFactory;
import cn.myth.mybatis.session.SqlSessionFactoryBuilder;
import cn.myth.mybatis.test.dao.IUserDao;
import cn.myth.mybatis.test.po.User;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        for (int i = 0; i < 50; i++) {
            User user = userDao.queryUserInfoById(1L);
            logger.info("测试结果：{}", JSON.toJSONString(user));
        }

        // 通过使用连接池的配置可以看到，在调用和获取连接的过程中，当调用次数打到10次以后，连接池中就有了10个活跃的链接，再调用的时候则需要等待连接释放后才能使用并执行 SQL 操作。

    }

    @Test
    public void test_pooled() throws SQLException, InterruptedException {
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver("com.mysql.jdbc.Driver");
        pooledDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/mybatis?useUnicode=true");
        pooledDataSource.setUsername("root");
        pooledDataSource.setPassword("123456");
        // 持续获得链接
        while (true) {
            Connection connection = pooledDataSource.getConnection();
            System.out.println(connection);
            Thread.sleep(1000);
            connection.close();
        }
    }
}
