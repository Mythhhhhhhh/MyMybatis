package cn.myth.mybatis.scripting.xmltags;

import cn.myth.mybatis.builder.BaseBuilder;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.scripting.defaults.RawSqlSource;
import cn.myth.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * XML脚本构建器
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;
    private boolean isDynamic;
    private Class<?> parameterType;

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
    }

    public SqlSource parseScriptNode() {
        // 将所有的Sql内存储Sql节点里
        List<SqlNode> contents = parseDynamicTags(element);
        // 再将其放入混合节点
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        // 创建源Sql进行解析
        return new RawSqlSource(configuration, rootSqlNode, parameterType);
    }

    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        // element.getText 拿到 SQL
        // 得到Sql语句文本
        String data = element.getText();
        // 将Sql内容放入静态文本Sql节点，并存储List集合
        contents.add(new StaticTextSqlNode(data));
        return contents;
    }
}
