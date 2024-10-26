package cn.myth.mybatis.test.dao;

import cn.myth.mybatis.test.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);
}
