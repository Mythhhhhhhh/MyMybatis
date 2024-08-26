package cn.myth.mybatis.test;

import cn.myth.mybatis.binding.MapperRegistry;
import cn.myth.mybatis.session.SqlSession;
import cn.myth.mybatis.session.SqlSessionFactory;
import cn.myth.mybatis.session.defaults.DefaultSqlSessionFactory;
import cn.myth.mybatis.test.dao.ISchoolDao;
import cn.myth.mybatis.test.dao.IUserDao;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTest {

    private Logger log = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory() {
        // 1, 注册 Mapper
        MapperRegistry registry = new MapperRegistry();
        registry.addMappers("cn.myth.mybatis.test.dao");

        // 2.从 SqlSession工厂获取Session
        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(registry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 3. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 4. 测试验证
        String res = userDao.queryUserName("10001");
        log.info("测试结果：{}", res);
        // 测试结果：你被代理了！方法：queryUserName 入参：[Ljava.lang.Object;@191f517
    }

    @Test
    public void test_MapperProxyFactory2() {
        MapperRegistry registry = new MapperRegistry();
        registry.addMapper(ISchoolDao.class);

        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(registry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        ISchoolDao schoolDao = sqlSession.getMapper(ISchoolDao.class);

        String res = schoolDao.querySchoolName("mythSchool");
        log.info("测试结果：{}", res);
    }

}
