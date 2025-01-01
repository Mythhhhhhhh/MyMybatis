package cn.myth.mybatis.executor.result;

import cn.myth.mybatis.session.ResultContext;

/**
 * 默认结果上下文
 */
public class DefaultResultContext implements ResultContext {

    // 结果数据对象
    private Object resultObject;
    private int resultCount;

    public DefaultResultContext() {
        this.resultObject = null;
        this.resultCount = 0;
    }

    @Override
    public Object getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    public void nextResultObject(Object resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }


}
