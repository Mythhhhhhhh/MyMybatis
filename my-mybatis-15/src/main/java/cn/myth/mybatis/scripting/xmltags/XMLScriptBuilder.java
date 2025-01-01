package cn.myth.mybatis.scripting.xmltags;

import cn.myth.mybatis.builder.BaseBuilder;
import cn.myth.mybatis.mapping.SqlSource;
import cn.myth.mybatis.scripting.defaults.RawSqlSource;
import cn.myth.mybatis.session.Configuration;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML脚本构建器
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;
    private boolean isDynamic;
    private Class<?> parameterType;
    private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
        initNodeHandlerMap();
    }

    private void initNodeHandlerMap() {
        // 9种，实现其中2种 trim/where/set/foreach/if/choose/when/otherwise/bind
        nodeHandlerMap.put("trim", new TrimHandler());
        nodeHandlerMap.put("if", new IfHandler());
    }


    /**
     * 解析脚本里的动态节点
     * 更改判断是否动态调用不同的SqlSource
     */
    public SqlSource parseScriptNode() {
        // 将所有的Sql内存储Sql节点里
        List<SqlNode> contents = parseDynamicTags(element);
        // 再将其放入混合节点
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        // 创建源Sql进行解析
        // 增加是否是动态语句的判断，
        SqlSource sqlSource = null;
        if (isDynamic) {
            sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
        return sqlSource;
    }

    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        List<Node> children = element.content();
        for (Node child : children) {
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String data = child.getText();
                TextSqlNode textSqlNode = new TextSqlNode(data);
                if (textSqlNode.isDynamic()) {
                    contents.add(textSqlNode);
                    isDynamic = true;
                } else {
                    contents.add(new StaticTextSqlNode(data));
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = child.getName();
                NodeHandler handler = nodeHandlerMap.get(nodeName);
                if (handler == null) {
                    throw new RuntimeException("Unknown element <" + nodeName + "> in SQL statement.");
                }
                handler.handleNode(element.element(child.getName()), contents);
                isDynamic = true;
            }
        }
        return contents;
    }


    // 节点处理器
    private interface NodeHandler {
        void handleNode(Element nodeToHandle, List<SqlNode> targetContents);
    }

    /**
     * <trim prefix="where" prefixOverrides="AND｜OR" suffixOverrides="and">...</trim>
     * 解析trim标签，把字段 prefix、prefixOverrides、suffixOverrides 都依次获取出来，使用TrimSqlNode构建后存放到List<SqlNode>中
     * 得到trim属性
     */
    private class TrimHandler implements NodeHandler {
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> contents = parseDynamicTags(nodeToHandle);
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
            String prefix = nodeToHandle.attributeValue("prefix");
            String prefixOverrides = nodeToHandle.attributeValue("prefixOverrides");
            String suffix = nodeToHandle.attributeValue("suffix");
            String suffixOverrides = nodeToHandle.attributeValue("suffixOverrides");
            TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
            targetContents.add(trim);
        }
    }

    /**
     * <if test="null != activityId">...</if>
     * 解析if语句标签与解析trim标签类似
     * 获取标签配置 test 语句表达式，使用IfSqlNode进行构建，构建后存放到List<SqlNode>中
     * 得到if标签的属性
     */
    private class IfHandler implements NodeHandler {
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> contents = parseDynamicTags(nodeToHandle);
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
            // 得到test的判断语句
            String test = nodeToHandle.attributeValue("test");
            IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
            targetContents.add(ifSqlNode);
        }
    }



}
