package cn.myth.mybatis.builder.annotation;

import cn.myth.mybatis.annotations.Delete;
import cn.myth.mybatis.annotations.Insert;
import cn.myth.mybatis.annotations.Select;
import cn.myth.mybatis.annotations.Update;
import cn.myth.mybatis.binding.MapperMethod;
import cn.myth.mybatis.builder.MapperBuilderAssistant;
import cn.myth.mybatis.mapping.SqlCommandType;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.scripting.LanguageDriver;
import cn.myth.mybatis.session.Configuration;
import cn.myth.mybatis.session.ResultHandler;
import cn.myth.mybatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 注解配置构建器 Mapper
 */
public class MapperAnnotationBuilder {

    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();

    private Configuration configuration;
    private MapperBuilderAssistant assistant;
    private Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace(".", "/") + ".java (best guess)";// cn/myth/mybatis/test/dao/IUserDao.java (best guess)
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;

        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);
    }

    public void parse() {
        String resource = type.toString();// interface cn.myth.mybatis.test.dao.IUserDao
        if (!configuration.isResourceLoaded(resource)) {
            assistant.setCurrentNamespace(type.getName());// cn.myth.mybatis.test.dao.IUserDao

            Method[] methods = type.getMethods();
            for (Method method : methods) {
                // 解析语句
                parseStatement(method);
            }
        }
    }

    /**
     * 解析语句
     */
    private void parseStatement(Method method) {
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = getlanguageDriver(method);
        SqlSource sqlSource = getSqlSourceFromAnnotation(method, parameterTypeClass, languageDriver);

        if (sqlSource != null) {
            final String mappedStatementId = type.getName() + "." + method.getName();
            SqlCommandType sqlCommandType = getSqlCommandType(method);
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            String resultMapId = null;
            if (isSelect) {
                resultMapId = parseResultMap(method);
            }

            // 调用助手类
            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    sqlCommandType,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    languageDriver
            );
        }
    }

    /**
     * 重点：DAO 方法的返回类型，如果为 List 则需要获取集合中的对象类型
     */
    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            Type returnTypeParameter = method.getGenericReturnType();
            if (returnTypeParameter instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (issue #443) actual type can be a also a parameterized type
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }
        return returnType;
    }

    /**
     * 解析返回类型处理为ResultMap
     */
    private String parseResultMap(Method method) {
        StringBuilder suffix = new StringBuilder();
        //拼接参数类型
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        // 拼接空返回值
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        // cn.myth.mybatis.dao.IUserDao.queryInfoId-long-void
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        // 添加ResultMap
        Class<?> returnType = getReturnType(method);
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>());
        return resultMapId;
    }

    /**
     * 根据注解类型获取Sql标签
     */
    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if (type == null) {
            return SqlCommandType.UNKNOWN;
        }
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    private SqlSource getSqlSourceFromAnnotation(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        try {
            // 获取注解类型
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            if (sqlAnnotationType != null) {
                // 根据注解类型获取注解类
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                // 获取注解类的值-原始Sql语句
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                // 根据获取的注解类的值构建Sql源
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e);
        }
    }

    /**
     * 拼接SQL，创造数据源
     */
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration, sql.toString(), parameterTypeClass);
    }

    /**
     * 获取注解类型
     */
    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        for (Class<? extends Annotation> type : sqlAnnotationTypes) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) return type;
        }
        return null;
    }


    /**
     * 获取语言驱动器
     */
    private LanguageDriver getlanguageDriver(Method method) {
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        return configuration.getLanguageRegistry().getDriver(langClass);
    }

    /**
     * 获取参数类型
     */
    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        // 根据方法参数获取参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> clazz : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)) {
                if (parameterType == null) {
                    parameterType = clazz;
                } else {
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }



}
