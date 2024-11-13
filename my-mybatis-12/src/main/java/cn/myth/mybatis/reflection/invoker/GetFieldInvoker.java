package cn.myth.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * 调用者
 */
public class GetFieldInvoker implements Invoker {

    private Field field;

    public GetFieldInvoker(Field filed) {
        this.field = filed;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
