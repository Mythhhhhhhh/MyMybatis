package cn.myth.mybatis.scripting.xmltags;

import cn.myth.mybatis.session.Configuration;

import java.util.*;

/**
 * trim Sql Node 节点解析
 * trim最主要的就是调用处理if的sql节点处理或拼接，然后得到trim属性，把trim属性前缀或后缀进行拼接Sql处理
 * 这里trim的Sql拼接处理都在其内部类实现，是FilteredDynamicContext类
 */
public class TrimSqlNode implements SqlNode {

    private SqlNode contents;
    private String prefix;
    private String suffix;
    private List<String> prefixesToOverride;
    private List<String> suffixesToOverride;
    private Configuration configuration;

    public TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, String prefixesToOverride, String suffix, String suffixesToOverride) {
        this(configuration, contents, prefix, parseOverrides(prefixesToOverride), suffix, parseOverrides(suffixesToOverride));
    }

    protected TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, List<String> prefixesToOverride, String suffix, List<String> suffixesToOverride) {
        this.contents = contents;
        this.prefix = prefix;
        this.prefixesToOverride = prefixesToOverride;
        this.suffix = suffix;
        this.suffixesToOverride = suffixesToOverride;
        this.configuration = configuration;
    }

    @Override
    public boolean apply(DynamicContext context) {
        FilteredDynamicContext filteredDynamicContext = new FilteredDynamicContext(context);
        // 得到trim里的内容，进行处理，最后拼接语句
        // 例如：trim -> if -> 条件语句
        boolean result = contents.apply(filteredDynamicContext);
        // 根据trim的属性添加前后缀
        filteredDynamicContext.applyAll();
        return result;
    }


    /**
     * <trim prefix="where" prefixOverrides="AND｜OR" suffixOverrides="and">
     *
     * </trim>
     * 将prefixOverrides以list形式展示
     */
    private static List<String> parseOverrides(String overrides) {
        if (overrides != null) {
            final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
            final List<String> list = new ArrayList<String>(parser.countTokens());
            while (parser.hasMoreTokens()) {
                list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
            }
            return list;
        }
        return Collections.emptyList();
    }

    private class FilteredDynamicContext extends DynamicContext {

        private DynamicContext delegate;
        private boolean prefixApplied;
        private boolean suffixApplied;
        private StringBuilder sqlBuffer;

        public FilteredDynamicContext(DynamicContext delegate) {
            super(configuration, null);
            this.delegate = delegate;
            this.prefixApplied = false;
            this.suffixApplied = false;
            this.sqlBuffer = new StringBuilder();
        }

        public void applyAll() {
            sqlBuffer = new StringBuilder(sqlBuffer.toString().trim());
            String trimmedUppercaseSql = sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
            if (trimmedUppercaseSql.length() > 0) {
                // 动态加载前缀
                applyPrefix(sqlBuffer, trimmedUppercaseSql);
                // 动态加载后缀
                applySuffix(sqlBuffer, trimmedUppercaseSql);
            }
            // 添加完拼接的前后缀，继续拼接完整的Sql
            delegate.appendSql(sqlBuffer.toString());
        }

        // 获取当前属性
        @Override
        public Map<String, Object> getBindings() {
            return delegate.getBindings();
        }

        // 拼接当前Sql
        @Override
        public void appendSql(String sql) {
            sqlBuffer.append(sql);
        }

        @Override
        public void bind(String name, Object value) {
            delegate.bind(name, value);
        }

        @Override
        public int getUniqueNumber() {
            return delegate.getUniqueNumber();
        }

        @Override
        public String getSql() {
            return delegate.getSql();
        }

        /**
         * 拼接前缀处理
         */
        private void applyPrefix(StringBuilder sql, String trimmedUppercaseSql) {
            if (!prefixApplied) {
                prefixApplied = true;
                if (prefixesToOverride != null) {
                    for (String toRemove : prefixesToOverride) {
                        if (trimmedUppercaseSql.startsWith(toRemove)) {
                            sql.delete(0, toRemove.trim().length());
                            break;
                        }
                    }
                }
                if (prefix != null) {
                    sql.insert(0, " ");
                    sql.insert(0, prefix);
                }
            }
        }

        /**
         * 拼接后缀处理
         */
        private void applySuffix(StringBuilder sql, String trimmedUppercaseSql) {
            if (!suffixApplied) {
                suffixApplied = true;
                if (suffixesToOverride != null) {
                    for (String toRemove : suffixesToOverride) {
                        if (trimmedUppercaseSql.endsWith(toRemove) || trimmedUppercaseSql.endsWith(toRemove.trim())) {
                            int start = sql.length() - toRemove.trim().length();
                            int end = sql.length();
                            sql.delete(start, end);
                            break;
                        }
                    }
                }
                if (suffix != null) {
                    sql.append(" ");
                    sql.append(suffix);
                }
            }
        }

    }

}
