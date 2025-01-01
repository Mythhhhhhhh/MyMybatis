package cn.myth.mybatis.test.dao;

import cn.myth.mybatis.test.po.Activity;

public interface IActivityDao {

    Activity queryActivityById(Activity activity);

    Activity queryActivityById2(Activity activity);

}
