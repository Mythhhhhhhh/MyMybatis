package cn.myth.mybatis.scripting.xmltags;

/**
 * 静态文本SQL节点
 * 描述XML或者注解中不带任何标签的配置信息，即静态文本内容
 */
public class StaticTextSqlNode implements SqlNode {

    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 将文本加入context
        context.appendSql(text);
        return true;
    }
}
