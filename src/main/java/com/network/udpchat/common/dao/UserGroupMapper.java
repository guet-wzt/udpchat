package com.network.udpchat.common.dao;

import com.network.udpchat.common.domain.UserGroup;

import java.util.List;

public interface UserGroupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserGroup record);

    int insertSelective(UserGroup record);

    UserGroup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserGroup record);

    int updateByPrimaryKey(UserGroup record);

    List<UserGroup> selectAll();
}