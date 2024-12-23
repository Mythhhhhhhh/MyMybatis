package cn.myth.mybatis.scripting.xmltags;

/**
 * SQL节点，描述Mapper文件中配置的SQL信息
 */
public interface SqlNode {

    boolean apply(DynamicContext context);
}
