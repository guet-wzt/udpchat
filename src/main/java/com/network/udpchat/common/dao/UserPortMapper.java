package com.network.udpchat.common.dao;

import com.network.udpchat.common.domain.UserPort;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface UserPortMapper {
    int deleteByPrimaryKey(String username);

    int insert(UserPort record);

    int insertSelective(UserPort record);

    UserPort selectByPrimaryKey(String username);

    int updateByPrimaryKeySelective(UserPort record);

    int updateByPrimaryKey(UserPort record);

    List<UserPort> selectAll();

    List<UserPort> selectSelectiveAll(String username);
}