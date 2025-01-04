package cn.myth.middleware.mybatis.spring.test.dao;

import cn.myth.middleware.mybatis.spring.test.po.Activity;

public interface IActivityDao {

    Activity queryActivityById(Activity activity);

}
