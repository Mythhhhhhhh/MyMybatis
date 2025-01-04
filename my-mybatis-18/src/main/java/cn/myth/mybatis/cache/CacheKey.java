package cn.myth.mybatis.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存Key，一般缓存框架的数据结构基本上都是 Key -> value 方式存储
 * Mybatis 对于其Key的生产采取规则为：[mappedStatementId + offset + limit + SQL + queryParams + environment]生成一个哈希码
 */
public class CacheKey implements Cloneable, Serializable {

    private static final long serialVersionUID = 1146682552656046210L;

    private static final CacheKey NULL_CACHE_KEY = new NullCacheKey();

    private static final int DEFAULT_MULTIPLYER = 37;
    private static final int DEFAULT_HASHCODE = 17;

    private int multiplier;
    private int hashcode;
    private long checksum;
    private int count;
    private List<Object> updateList;

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIPLYER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object[] objects) {
        this();
        updateAll(objects);
    }

    public int getUpdateCount() {
        return updateList.size();
    }

    public void  update(Object object) {
        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(object, i);
                doUpdate(element);
            }
        } else {
            doUpdate(object);
        }
    }

    /**
     * 1.根据参数计算hash码值
     * 2.为了保证不重复处理计算最终的码值
     * 3.并将对象放入updateList集合中
     */
    private void doUpdate(Object object) {
        // 计算Hash值，校验码
        // 确保hashCode一直都是有的
        int baseHashCode = object == null ? 1 : object.hashCode();

        // 为了跟踪缓存更新的次数
        count++;
        // 为了计算一个累积的校验和，用于检测缓存数据的一致性
        checksum += baseHashCode;
        // 引入一个与更新次数相关的权重或因子，影响最终的哈希值
        baseHashCode *= count;
        // 最终的哈希码值，相乘计算保证了对象或其属性变化时，哈希码值都会改变
        hashcode = multiplier * hashcode + baseHashCode;
        // 目的是为了跟存储的参数进行对比
        updateList.add(object);
    }

    public void updateAll(Object[] objets) {
        for (Object o : objets) {
            update(o);
        }
    }

    /**
     * 如果遇到相同的哈希值，避免对象重复，那么CacheKey缓存Key重写了equals对比方法。
     * 这也就为什么在doUpdate计算哈希方法时，把对象添加到updateList.add(object)集合中，就是用于这里equal判断使用
     */
    // 重写对象的equals方法，用于对象判断
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CacheKey)) {
            return false;
        }

        final CacheKey cacheKey = (CacheKey) object;

        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        if (checksum != cacheKey.checksum) {
            return false;
        }
        if (count != cacheKey.count) {
            return false;
        }

        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (thisObject == null) {
                if (thatObject != null) {
                    return false;
                }
            } else {
                if (!thisObject.equals(thatObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    // 将每个参数都以冒号的形式拼接
    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(':').append(checksum);
        for (Object obj : updateList) {
            returnValue.append(':').append(obj);
        }
        return returnValue.toString();
    }

    @Override
    public CacheKey clone() throws CloneNotSupportedException {
        CacheKey clonedCacheKey = (CacheKey) super.clone();
        clonedCacheKey.updateList = new ArrayList<>(updateList);
        return clonedCacheKey;
    }
}
