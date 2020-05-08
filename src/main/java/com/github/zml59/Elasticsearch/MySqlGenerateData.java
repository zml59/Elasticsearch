package com.github.zml59.Elasticsearch;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MySqlGenerateData {
    private static void generateData(SqlSessionFactory sqlSessionFactory, int num) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)
        ) {
            List<News> currentNews = session.selectList("com.github.MockNews.selectNews");

            int count = num - currentNews.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInsert = currentNews.get(index);
                    Instant currentTime = newsToBeInsert.getCreateAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setCreateAt(currentTime);
                    newsToBeInsert.setUpdateAt(currentTime);
                    session.insert("com.github.MockNews.insertNews", newsToBeInsert);
                    System.out.println("left = " + count);
                    if (count%2000==0){
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        generateData(sqlSessionFactory, 100_0000);
    }
}
