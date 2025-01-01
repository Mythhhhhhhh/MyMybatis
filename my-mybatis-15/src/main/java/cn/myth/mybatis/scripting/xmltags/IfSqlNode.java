package cn.myth.mybatis.scripting.xmltags;

/**
 * IF SQL 节点
 */
public class IfSqlNode implements SqlNode {

    private ExpressionEvaluator evaluator;
    private String test;
    private SqlNode contexts;

    public IfSqlNode(SqlNode contexts, String test) {
        this.test = test;
        this.contexts = contexts;
        this.evaluator = new ExpressionEvaluator();
    }

    /**
     * <if test="null != activityId">
     *     activityId = #{activityId}
     * </if>
     */
    @Override
    public boolean apply(DynamicContext context) {
        // 如果满足条件，则apply，并返回true
        if (evaluator.evaluateBoolean(test, context.getBindings())) {
            // 拼接if标签里的Sql语句
            contexts.apply(context);
            return true;
        }
        return false;
    }

}
