package cn.myth.mybatis.session.defaults;

import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.RowBounds;
import cn.myth.mybatis.session.SqlSession;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 默认SqlSession实现类
 */
public class DefaultSqlSession implements SqlSession {

    private Logger logger = LoggerFactory.getLogger(DefaultSqlSession.class);

    private Configuration configuration;
    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // 链路关系 Executor#query -> SimpleExecutor#doQuery -> StatementHandler#parameterize -> PreparedStatement#parameterize -> ParameterHandler#setParameters
        logger.info("执行查询 statement：{} parameter：{}", statement, JSON.toJSONString(parameter));
        MappedStatement ms = configuration.getMappedStatement(statement);
        List<T> list = executor.query(ms, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, ms.getSqlSource().getBoundSql(parameter));
        return list.get(0);
    }

//    @Override
//    public <T> T selectOne(String statement, Object parameter) {
//        try {
//            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
//            Environment environment = configuration.getEnvironment();
//
//            Connection connection = environment.getDataSource().getConnection();
//
//            BoundSql boundSql = mappedStatement.getBoundSql();
//            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
//            preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
//            return objList.get(0);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
//        List<T> list = new ArrayList<>();
//        try {
//            ResultSetMetaData metaData = resultSet.getMetaData();
//            int columnCount = metaData.getColumnCount();
//            // 每次遍历行值
//            while (resultSet.next()) {
//                T obj = (T) clazz.newInstance();
//                for (int i = 1; i <= columnCount; i++) {
//                    Object value = resultSet.getObject(i);
//                    String columnName = metaData.getColumnName(i);
//                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
//                    Method method;
//                    if (value instanceof Timestamp) {
//                        method = clazz.getMethod(setMethod, Date.class);
//                    } else {
//                        method = clazz.getMethod(setMethod, value.getClass());
//                    }
//                    method.invoke(obj, value);
//                }
//                list.add(obj);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

}
