package cn.myth.mybatis.session.defaults;

import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.RowBounds;
import cn.myth.mybatis.session.SqlSession;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
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
        List<T> list = this.<T>selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new RuntimeException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        // 链路关系 Executor#query -> SimpleExecutor#doQuery -> StatementHandler#parameterize -> PreparedStatement#parameterize -> ParameterHandler#setParameters
        logger.info("执行查询 statement：{} parameter：{}", statement, JSON.toJSONString(parameter));
        MappedStatement ms = configuration.getMappedStatement(statement);
        try {
            return executor.query(ms, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, ms.getSqlSource().getBoundSql(parameter));
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database.  Cause: " + e);
        }
    }


    @Override
    public int insert(String statement, Object parameter) {
        // 在 Mybatis 中 insert 调用的是 update
        return update(statement, parameter);
    }

    @Override
    public int update(String statement, Object parameter) {
        // 查询要处理哪个mapper语句
        MappedStatement ms = configuration.getMappedStatement(statement);
        try {
            return executor.update(ms, parameter);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating database.  Cause: " + e);
        }
    }

    @Override
    public Object delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public void commit() {
        try {
            executor.commit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error committing transaction.  Cause: " + e);
        }
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
