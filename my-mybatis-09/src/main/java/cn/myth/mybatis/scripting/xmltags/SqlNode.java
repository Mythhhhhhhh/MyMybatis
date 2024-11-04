package cn.myth.mybatis.scripting.xmltags;

/**
 * SQL节点
 */
public interface SqlNode {

    boolean apply(DynamicContext context);
}
