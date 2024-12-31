package cn.myth.mybatis.executor.keygen;

import cn.myth.mybatis.executor.Executor;
import cn.myth.mybatis.mapping.MappedStatement;
import cn.myth.mybatis.reflection.MetaObject;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.RowBounds;

import java.sql.Statement;
import java.util.List;

/**
 * 键值生成器
 * 有SelectKey时实现执行SelectKey的逻辑
 */
public class SelectKeyGenerator implements KeyGenerator {

    public static final String SELECT_KEY_SUFFIX = "!selectKey";
    private boolean executeBefore;
    private MappedStatement keyStatement;

    public SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
        this.executeBefore = executeBefore;
        this.keyStatement = keyStatement;
    }


    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executeBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executeBefore) {
            processGeneratedKeys(executor, ms, parameter);
        }
    }


    /**
     * 执行selectKey的SQL语句，执行完毕后把返回的id结果放入对应实体中
     */
    private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();// ["id"]
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                if (keyProperties != null) {
                    Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
                    List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
                    if (values.size() == 0) {
                        throw new RuntimeException("SelectKey returned no data.");
                    } else if (values.size() > 1) {
                        throw new RuntimeException("SelectKey returned more than one value.");
                    } else {
                        MetaObject metaResult = configuration.newMetaObject(values.get(0));
                        if (keyProperties.length == 1) {
                            if (metaResult.hasGetter(keyProperties[0])) {
                                setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                            } else {
                                setValue(metaParam, keyProperties[0], values.get(0));
                            }
                        } else {
                            handleMultipleProperties(keyProperties, metaParam, metaResult);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error selecting key or setting result to parameter object. Cause: " + e);
        }
    }

    private void handleMultipleProperties(String[] keyProperties,
                                          MetaObject metaParam, MetaObject metaResult) {
        // 获取keyColumn配置
        String[] keyColumns = keyStatement.getKeyColumns();

        if (keyColumns == null || keyColumns.length == 0) { // 没有匹配列名则直接取属性名
            for (String keyProperty : keyProperties) {
                // 往metaParam中设置该属性为从数据库查询到的数据，从查询到的结果中取属性名
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) { // 列名和属性名的个数不一致则抛出异常
                throw new RuntimeException("If SelectKey has key columns, the number must match the number of key properties.");
            } else {
                for (int i = 0; i < keyProperties.length; i++) {
                    // 往metaParam中设置该属性为从数据库查询到的数据，从查询到的结果中取列名
                    setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
                }
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasGetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new RuntimeException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }
}
