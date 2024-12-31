package cn.myth.mybatis.scripting.xmltags;

import java.util.List;

/**
 * 混合SQL节点
 * 最终由MixedSqlNode将所有的SqlNode实现串起来执行，也看作责任链模式
 * 1.将一组SqlNode对象进行串联执行，通常多个SqlNode对象才能联合表述一个SQL信息，所以就需要借助MixedSqlNode来将其进行串联
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
