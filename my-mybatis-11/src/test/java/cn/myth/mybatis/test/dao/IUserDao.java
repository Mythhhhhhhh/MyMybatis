package cn.myth.mybatis.test.dao;

import cn.myth.mybatis.test.po.User;

import java.util.List;

public interface IUserDao {

    User queryUserInfoById(Long id);

    User queryUserInfo(User req);

    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
