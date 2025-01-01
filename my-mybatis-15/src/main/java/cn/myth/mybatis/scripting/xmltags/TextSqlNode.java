package cn.myth.mybatis.scripting.xmltags;

import cn.myth.mybatis.parsing.GenericTokenParser;
import cn.myth.mybatis.parsing.TokenHandler;
import cn.myth.mybatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * 文本SQL节点(CDATA TEXT)
 * 此节点处理是否是动态Sql的判断，还有一个是${}的参数替换
 */
public class TextSqlNode implements SqlNode {

    private String text;
    private Pattern injectionFilter;

    public TextSqlNode(String text) {
        this(text, null);
    }

    public TextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    /**
     * 判断是否是动态sql
     * 如果触发了TokenHandler#handleToken，说明是动态sql
     */
    public boolean isDynamic() {
        DynamicCheckerTokenParse checker = new DynamicCheckerTokenParse();
        GenericTokenParser parser = createParser(checker);
        parser.parse(text);
        return checker.isDynamic;
    }

    @Override
    public boolean apply(DynamicContext context) {
        GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
        context.appendSql(parser.parse(text));
        return true;
    }

    // 处理${}替换值的情况
    private GenericTokenParser createParser(TokenHandler handler) {
        return new GenericTokenParser("${", "}", handler);
    }

    /**
     * 绑定记号解析器
     */
    private static class BindingTokenParser implements TokenHandler {

        private DynamicContext context;
        private Pattern injectionFilter;

        public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null) {
                context.getBindings().put("value", null);
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                context.getBindings().put("value", parameter);
            }

            // 从缓存里取得值
            Object value = OgnlCache.getValue(content, context.getBindings());
            String srtValue = (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
            checkInjection(srtValue);
            return srtValue;
        }

        // 检查是否匹配正则表达式
        private void checkInjection(String value) {
            if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
                throw new RuntimeException("Invalid input. Please conform to regex" + injectionFilter.pattern());
            }
        }
    }

    /**
     * 动态SQL检查器
     */
    private static class DynamicCheckerTokenParse implements TokenHandler {

        private boolean isDynamic;

        public DynamicCheckerTokenParse() {
            // Prevent Synthetic Access
        }

        public boolean isDynamic() {
            return isDynamic;
        }

        @Override
        public String handleToken(String content) {
            // 设置 isDynamic为true，即调用饿了这个类就必定是动态SQL
            this.isDynamic = true;
            return null;
        }
    }
}
