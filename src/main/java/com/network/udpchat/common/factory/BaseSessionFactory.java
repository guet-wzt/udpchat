package com.network.udpchat.common.factory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;

public class BaseSessionFactory {

    //打开数据库的工厂
    private SqlSessionFactory sqlSessionFactory;

    public BaseSessionFactory() throws IOException {
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();
    }

    public SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

}
