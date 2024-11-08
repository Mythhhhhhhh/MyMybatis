package cn.myth.mybatis.reflection;

import cn.myth.mybatis.reflection.invoker.GetFieldInvoker;
import cn.myth.mybatis.reflection.invoker.Invoker;
import cn.myth.mybatis.reflection.invoker.MethodInvoker;
import cn.myth.mybatis.reflection.invoker.SetFieldInvoker;
import cn.myth.mybatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射器，属性 get/set 的映射器
 * 缓存了反射操作所需要使用的类的元信息
 * Reflector反射器专门用于解耦对象信息的，只有把一个对象信息所含带的属性、方法以及关联的类都以此解析出来，才能满足后续对属性值的设置和获取
 */
public class Reflector {

    private static boolean classCacheEnabled = true;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    // 线程安全的缓存
    private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();

    private Class<?> type;
    // 可读属性的名称集合，可读属性就是存在相应getter方法的属性，初始为空数组
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
    // 可写属性的名称集合，可写属性就是存在相应setter方法的属性，初始为空数组
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
    // 记录了属性响应的setter方法，key是属性名称，value是Invoker对象，它是对setter方法对应Method对象的封装
    private Map<String, Invoker> setMethods = new HashMap<>();
    // 属性对应的getter方法集合，key是属性名称，value也是Invoker对象
    private Map<String, Invoker> getMethods = new HashMap<>();
    // 记录了属性相应的setter方法的参数值类型，key是属性名称，value是参数类型
    private Map<String, Class<?>> setTypes = new HashMap<>();
    // 记录了属性相应的getter方法的参数值类型，key是属性名称，value是参数类型
    private Map<String, Class<?>> getTypes = new HashMap<>();
    // 记录了默认构造方法
    private Constructor<?> defaultConstructor;
    // 记录了所有属性名称集合
    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();


    /**
     * 构造函数，对上述字段初始化
     */
    public Reflector(Class<?> clazz) {
        // 初始化type字段
        this.type = clazz;
        // 加入构造函数 初始化defaultConstructor
        addDefaultConstructor(clazz);
        // 加入getter 初始化getMethods和getTypes，通过遍历getting方法
        addGetMethods(clazz);
        // 加入setter 初始化setMethods和setTypes，通过遍历setting方法
        addSetMethods(clazz);
        // 加入字段 初始化getMethods + getTypes 和 setMethods + setTypes，通过遍历fields属性
        addFields(clazz);
        // 将所有属性名的大写形式作为键，属性名作为值，存入到 caseInsensitivePropertyMap中
        readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
        writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
        for (String propName : readablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for (String propName : writeablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }

    /**
     * 获取指定Class对象的默认构造方法，即无参构造方法
     * 通过反射遍历所有的构造方法，然后通过过滤查询到默认构造函数，并赋值给defaultConstructor字段
     */
    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] consts = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : consts) {
            // 默认构造方法没有参数
            if (constructor.getParameterTypes().length == 0) {
                // 允许利用反射检查任意类的私有变量
                if (canAccessPrivateMethods()) {
                    try {
                        constructor.setAccessible(true);
                    } catch (Exception ignore) {
                        // Ignored. This os only a final precaution, nothing we can do
                    }
                    if (constructor.isAccessible()) {
                        this.defaultConstructor = constructor;
                    }
                }
            }
        }
    }


    /**
     * 负责解析类中定义的getter方法，并填充getMethods集合和getTypes集合
     * getClassMethods方法：主要用来获取当前类以及其父类中定义的所有方法的唯一签名以及相应的Method对象
     * addMethodConflict方法：主要用来获取字段名和字段名对应的getter方法的映射集合，并把结果保存到conflictingGetters变量中
     * resolveGetterConflicts方法：主要用来处理重复的方法。
     * 注意：一个key会对应多个Method的原因是：当子类覆盖了父类的getter方法且返回值发生变化时，会产生两个签名不同的方法
     */
    private void addGetMethods(Class<?> clazz) {
        // 记录所有符合条件的getter方法的Method对象
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
         for (Method method : methods) {// 循环，判断getter方法，并填充相应集合
             if (method.getParameterTypes().length > 0) {// getter方法一定没有参数，所以首先排除有参数的方法
                 continue;
             }

             String name = method.getName();
             if ((name.startsWith("get") && name.length() > 3)
                || (name.startsWith("is")) && name.length() > 2) { // 判断getter方法，即is或get开头的方法
                 // 根据Method对象的方法名称，获取getter或setter方法对应的属性名称
                 name = PropertyNamer.methodToProperty(name);
                 // 把属性名作为key，属性名对应的所有method对象的集合作为值，然后填充到conflictingMethods中
                 addMethodConflict(conflictingGetters, name , method);
             }
         }
         // 因为get方法子类和父类可能同时存在所以进行筛选
        resolveGetterConflicts(conflictingGetters);
    }

    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            if (method.getParameterTypes().length != 1) {
                continue;
            }

            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3) {
                name = PropertyNamer.methodToProperty(name);
                addMethodConflict(conflictingSetters, name, method);
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (String propName : conflictingSetters.keySet()) {
            List<Method> setters = conflictingSetters.get(propName);
            Method firstMethod = setters.get(0);
            if (setters.size() == 1) {
                addSetMethod(propName, firstMethod);
            } else {
                Class<?> expectedType = getTypes.get(propName);
                if (expectedType == null) {
                    throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                            + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                            "specification and can cause unpredicatble results.");
                } else {
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    while (methods.hasNext()) {
                        Method method = methods.next();
                        if (method.getParameterTypes().length == 1
                                && expectedType.equals(method.getParameterTypes()[0])) {
                            setter = method;
                            break;
                        }
                    }
                    if (setter == null) {
                        throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                                "specification and can cause unpredicatble results.");
                    }
                    addSetMethod(propName, setter);
                }
            }
        }
    }

    private void addSetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, new MethodInvoker(method));
            setTypes.put(name, method.getParameterTypes()[0]);
        }
    }

    /**
     * 它是 #addGetMethods(...) 和 #addSetMethods(...)方法的补充
     * 因为有些field，不存在对应的setting或getting方法，所以直接使用对应的field
     */
    private void addFields(Class<?> clazz) {
        // 获取某个类的所有声明的字段，即包括public、private、protected，但是不包括父类的字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (canAccessPrivateMethods()) {
                try {
                    field.setAccessible(true);
                } catch (Exception e) {
                    // Ignored. This is only a final precaution, nothing we can do.
                }
            }
            if (field.isAccessible()) {
                if (!setMethods.containsKey(field.getName())) {
                    // issue #379 - removed the check for final because JDK 1.5 allows
                    // modification of final fields through reflection (JSR-133). (JGB)
                    // pr #16 - final static can only be set by the classloader
                    // 属性没有set方法的话，且不是final和static的话，也可以放进setMethods通过反射来设置值的
                    int modifiers = field.getModifiers();
                    if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                        addSetField(field);
                    }
                }
                // 没有get方法的，给他加个get方法，也是通过反射来获取值的
                if (!getMethods.containsKey(field.getName())) {
                    addGetField(field);
                }
            }
        }
        // 递归处理父类
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }
    }

    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMethods.put(field.getName(), new GetFieldInvoker(field));
            getTypes.put(field.getName(), field.getType());
        }
    }

    /**
     * 用来把上一步addMethodConflict方法产生conflictingGetters数据中重复的Method对象进行处理，
     * 变成一个字段对应一个Method对象形式的数据，然后通过addGetMethod方法把对应key和value存到变量getMethods和getTypes中
     */
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
            Method winner = null;
            String propName = entry.getKey();
            for (Method candidate : entry.getValue()) {
                if (winner == null) {
                    winner = candidate;
                    continue;
                }
                Class<?> winnerType = winner.getReturnType();
                Class<?> candidateType = candidate.getReturnType();
                if (candidateType.equals(winnerType)) {
                    // 特殊处理返回值是boolean类型的方法
                    if (!boolean.class.equals(candidateType)) {
                        throw new RuntimeException(
                                "Illegal overloaded getter method with ambiguous type for property "
                                        + propName + " in class " + winner.getDeclaringClass()
                                        + ". This breaks the JavaBeans specification and can cause unpredictable results.");
                    } else if (candidate.getName().startsWith("is")) {
                        winner = candidate;
                    }
                }
                // 不符合选择子类
                else if (candidateType.isAssignableFrom(winnerType)) {
                    // isAssignableFrom，判定此Class对象所表示的类或接口是否相同，或是否是其超类或超接口 如果是则返回true；否则返回false
                    // OK getter type is descendant
                    // 符合选择子类。因为子类可以修改放大返回值。
                    // eg：父类的一个方法的返回值为List，子类对该方法的返回值可以覆写为ArrayList
                }
                else if (winnerType.isAssignableFrom(candidateType)) {
                    winner = candidate;
                }
                else {
                    throw new RuntimeException(
                            "Illegal overloaded getter method with ambiguous type for property "
                                    + propName + " in class " + winner.getDeclaringClass()
                                    + ". This breaks the JavaBeans specification and can cause unpredictable results.");
                }
            }
            addGetMethod(propName, winner);
        }
    }


    /**
     * 主要是把对应key和value存到变量getMethods和getTypes中。
     * 其中，key对应的字段是name，value对应的是把method对象封装成invoker对象。
     */
    private void addGetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            getMethods.put(name, new MethodInvoker(method));
            getTypes.put(name, method.getReturnType());
        }
    }

    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }


    /**
     * 用来把getClassMethod方法中得到的Method数组（getter方法），根据字段名进行分类，对应的getter方法可能有多个
     * （因为当子类覆盖了父类的getter方法且返回值发生变化时会产生两个签名不同的方法，所以一个字段对应多个Method方法）
     * 所以addMethodConflict方法，会把处理结果储存到Map<String, List>类型的conflictingGetters变量中
     */
    private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
        List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(method);
    }


    private static boolean canAccessPrivateMethods() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }


    /**
     * 此方法返回一个数组，该数组包含该类中声明的所有方法和任何超类
     * 我们使用此方法不是为了代替 Class.getMethod()
     * 因为我们想访问类中的私有方法
     *
     * @param cls Class对象
     * @return 包含该类中所有方法的数组
     */
    private Method[] getClassMethods(Class<?> cls) {
        // 用于记录指定类中定义的全部方法的唯一签名以及对应的Method对象
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            // 记录当前类中定义的所有方法
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // 记录接口中定义的方法
            // we also need to look for interface methods -
            // because the class may be abstract
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterfaces : interfaces) {
                addUniqueMethods(uniqueMethods, anInterfaces.getDeclaredMethods());
            }
            // 当前类的父类
            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        // 转化成数组返回
        return methods.toArray(new Method[methods.size()]);
    }

    /**
     * 为每个方法生成唯一签名，并记录到uniqueMethods集合中
     */
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                // 得到方法签名
                String signature = getSignature(currentMethod);
                // 根据方法签名去重
                // check to see if the method is already known
                // if it is known, then an extended class must hava
                // overridden a method
                if (!uniqueMethods.containsKey(signature)) {
                    if (canAccessPrivateMethods()) {
                        try {
                            currentMethod.setAccessible(true);
                        } catch (Exception e) {
                            // Ignored. This is only a final precaution, nothing we can do.

                        }
                    }
                    // 记录签名与方法的对应关系
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }


    /**
     * 获取方法签名 eg:java.lang.String#getSignature:java.lang.reflect.Method
     */
    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        // 方法返回类型
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append('#');
        }
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    public Class<?> getType() {
        return type;
    }

    public Constructor<?> getDefaultConstructor() {
        if (defaultConstructor != null) {
            return defaultConstructor;
        } else {
            throw new RuntimeException("There is no default constructor for " + type);
        }
    }

    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }

    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    public Invoker getSetInvoker(String propertyName) {
        Invoker method = setMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    /*
     * Gets the type for a property getter
     *
     * @param propertyName - the name of the property
     * @return The Class of the propery getter
     */
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    /*
     * Gets an array of the readable properties for an object
     *
     * @return The array
     */
    public String[] getGetablePropertyNames() {
        return readablePropertyNames;
    }

    /*
     * Gets an array of the writeable properties for an object
     *
     * @return The array
     */
    public String[] getSetablePropertyNames() {
        return writeablePropertyNames;
    }

    /*
     * Check to see if a class has a writeable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a writeable property by the name
     */
    public boolean hasSetter(String propertyName) {
        return setMethods.keySet().contains(propertyName);
    }

    /*
     * Check to see if a class has a readable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a readable property by the name
     */
    public boolean hasGetter(String propertyName) {
        return getMethods.keySet().contains(propertyName);
    }

    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    /*
     * Gets an instance of ClassInfo for the specified class.
     * 得到某个类的反射器，是静态方法，而且要缓存，又要多线程，所以REFLECTOR_MAP是一个ConcurrentHashMap
     *
     * @param clazz The class for which to lookup the method cache.
     * @return The method cache for the class
     */
    public static Reflector forClass(Class<?> clazz) {
        if (classCacheEnabled) {
            // synchronized (clazz) removed see issue #461
            // 对于每个类来说，我们假设它是不会变的，这样可以考虑将这个类的信息(构造函数，getter,setter,字段)加入缓存，以提高速度
            Reflector cached = REFLECTOR_MAP.get(clazz);
            if (cached == null) {
                cached = new Reflector(clazz);
                REFLECTOR_MAP.put(clazz, cached);
            }
            return cached;
        } else {
            return new Reflector(clazz);
        }
    }

    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.classCacheEnabled = classCacheEnabled;
    }

    public static boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }




}
