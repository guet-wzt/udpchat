package com.network.udpchat.common.dao;

import com.network.udpchat.common.domain.Friends;

import java.util.List;

public interface FriendsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Friends record);

    int insertSelective(Friends record);

    Friends selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Friends record);

    int updateByPrimaryKey(Friends record);

    List<Friends> selectFriendsList(String username);

    int insertFriend(Friends friends);
}