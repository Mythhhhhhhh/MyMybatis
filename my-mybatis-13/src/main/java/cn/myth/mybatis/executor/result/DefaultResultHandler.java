package cn.myth.mybatis.executor.result;

import cn.myth.mybatis.reflection.factory.ObjectFactory;
import cn.myth.mybatis.session.ResultContext;
import cn.myth.mybatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认结果处理器
 */
public class DefaultResultHandler implements ResultHandler {
    // 存储结果上下文数据
    private final List<Object> list;

    public DefaultResultHandler() {
        this.list = new ArrayList<>();
    }

    /**
     * 通过 ObjectFactory 反射工具类，产生特定的空List
     * 用 ObjectFactory.create(List.class)创建并没有什么特殊，只是统一工具列
     */
    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        this.list = objectFactory.create(List.class);
    }

    @Override
    public void handleResult(ResultContext context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList() {
        return list;
    }

}
