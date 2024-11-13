package cn.myth.mybatis.session;

/**
 * 结果处理器
 */
public interface ResultHandler {

    /**
     * 处理结果
     */
    void handleResult(ResultContext context);
}
