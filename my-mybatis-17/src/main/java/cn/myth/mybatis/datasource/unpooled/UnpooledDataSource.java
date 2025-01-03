package cn.myth.mybatis.datasource.unpooled;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 无池化数据源实现
 * 无池化的数据源链接实现比较简单，核心在于 initializerDriver 初始化驱动中使用了 Class.forName 和 newInstance 的方式创建了数据源链接操作。
 * 在创建完成连接以后，把链接存放到驱动注册器中，方便后续使用中可以直接获取链接，避免重复创建所带来的资源消耗。
 */
public class UnpooledDataSource implements DataSource {

    private ClassLoader driverClassLoader;

    // 驱动配置，也可以扩展属性信息 driver.encoding=UTF8
    private Properties driverProperties;

    // 注册驱动器
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

    // 驱动
    private String driver;

    // DB链接地址
    private String url;

    // 帐号
    private String username;

    // 密码
    private String password;

    // 是否自动提交
    private Boolean autoCommit;

    // 事务级别
    private Integer defaultTransactionIsolationLevel;

    static {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }


    /**
     *  初始化驱动
     */
    private synchronized void initializeDriver() throws SQLException {
        if (!registeredDrivers.containsKey(driver)) {
            try {
                Class<?> driverType = Class.forName(driver, true, driverClassLoader);
                // https://www.kfu.com/~nsayer/Java/jdbc.html
                Driver driverInstance = (Driver) driverType.newInstance();
                DriverManager.registerDriver(new DriverProxy(driverInstance));
            } catch (Exception e) {
                throw new RuntimeException("Error setting driver on UnpooledDataSource. Cause: " + e);
            }
        }
    }

    private static class DriverProxy implements Driver {

        private Driver driver;

        DriverProxy(Driver driver) {
            this.driver = driver;
        }

        // 参数u表示连接地址，p表示连接属性
        @Override
        public Connection connect(String u, Properties p) throws SQLException {
            return driver.connect(u, p);
        }

        // 通过此url建立连接，成功返回true，失败返回false
        @Override
        public boolean acceptsURL(String u) throws SQLException {
            return driver.acceptsURL(u);
        }

        // 参数u表示建立链接所需的地址，格式为jdbc:subprotocol:subname
        // jdbc表示是一个固定的字符串，表示是遵从jdbc规范的
        // subprotocol表示连接数据库的方式，例如Oracle数据库oracle:thin的方式
        // subname表示标识数据库，格式为@ip:port/dbname?option
        // ip用来表示数据库所在服务器的ip地址，port表示端口号，dbname表示数据库的schema的名称，option表示连接时的其他参数，如编码集，超时时间等
        // 示例 jdbc:oracle:thin:@localhost:1521/test:encoding=UTF8
        // 参数p表示连接时的属性，例如必要属性用户名和密码，也可以包含其他可选参数，也可以包含其他可选参数，例如编码集等
        @Override
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        // 获取驱动的主版本号
        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        // 获取驱动的副版本号
        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        // 该连接是否遵守jdbc规范，url以jdbc开头则返回true，否则返回false
        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return doGetConnection(props);
    }

    private Connection doGetConnection(Properties properties) throws SQLException {
        initializeDriver();
        Connection connection = DriverManager.getConnection(url, properties);
        if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            connection.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
        return connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public ClassLoader getDriverClassLoader() {
        return driverClassLoader;
    }

    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }

    public Properties getDriverProperties() {
        return driverProperties;
    }

    public void setDriverProperties(Properties driverProperties) {
        this.driverProperties = driverProperties;
    }

    public static Map<String, Driver> getRegisteredDrivers() {
        return registeredDrivers;
    }

    public static void setRegisteredDrivers(Map<String, Driver> registeredDrivers) {
        UnpooledDataSource.registeredDrivers = registeredDrivers;
    }

    public String getDriver() {
        return driver;
    }

    public synchronized void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return defaultTransactionIsolationLevel;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

}
