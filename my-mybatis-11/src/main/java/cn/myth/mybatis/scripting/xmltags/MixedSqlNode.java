package cn.myth.mybatis.scripting.xmltags;

import java.util.List;

/**
 * 混合SQL节点
 */
public class MixedSqlNode implements SqlNode {

    // 组合模式
    private List<SqlNode> contents;

    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 依次调用list里每个元素的apply
        contents.forEach(node -> node.apply(context));
        return true;
    }
}
