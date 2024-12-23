package cn.myth.mybatis.builder;

import cn.myth.mybatis.mapping.ParameterMapping;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.parsing.GenericTokenParser;
import cn.myth.mybatis.parsing.TokenHandler;
import cn.myth.mybatis.reflection.MetaClass;
import cn.myth.mybatis.reflection.MetaObject;
import cn.myth.mybatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL 源码构建器
 * 此类主要完成以下两个操作
 * 1.一方面是解析Sql中的#{}占位符定义的属性，如jdbcType、javaType（使用较少）eg:#{id, jdbcType=INTEGER}
 * 2.一方面是把#{}占位符替换为?占位符
 */
public class SqlSourceBuilder extends BaseBuilder {

    private static Logger logger = LoggerFactory.getLogger(SqlSourceBuilder.class);

    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        // 将Sql中的#{}替换?，并把#{}内容转变为ParameterMapping对象
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        // 定位标签#{}并与ParameterMappingTokenHandler合作替换?
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        // 得到处理后可执行的Sql
        String sql = parser.parse(originalSql);
        // 返回静态 SQL
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        // 构建参数映射
        private ParameterMapping buildParameterMapping(String content) {
            // 先解析参数映射,就是转化成一个 HashMap | #{favouriteSection,jdbcType=VARCHAR}
            Map<String, String> propertiesMap = new ParameterExpression(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType = parameterType;
            // 首先if判断对应的参数类型是否在 TypeHandlerRegistry注册器中，如果不在则拆解对象，按属性进行获取propertyType的操作
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            } else if (property != null) {
                MetaClass metaClass = MetaClass.forClass(parameterType);
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            } else {
                propertyType = Object.class;
            }

            logger.info("构建参数映射 property：{} propertyType：{}", property, propertyType);
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }

    }
}
