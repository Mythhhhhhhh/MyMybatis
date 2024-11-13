package cn.myth.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Long类型处理器
 */
public class LongTypeHandler extends BaseTypeHandler<Long> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter);
    }

    // Long类型处理器获取返回值，则从JDBC的结果集中getLong就可以获取相应类型的值
    @Override
    protected Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getLong(columnName);
    }

}
