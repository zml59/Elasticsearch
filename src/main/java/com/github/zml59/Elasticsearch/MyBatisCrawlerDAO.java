package com.github.zml59.Elasticsearch;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDAO implements CrawlerDAO {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDAO() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getOneLinkThenDelete() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("com.github.MyMapper.selectOneUnusedLink");
            if (url != null) {
                session.delete("com.github.MyMapper.deleteLink", url);
            }
            return url;
        }
    }


    @Override
    public void insertNewsIntoDB(String title, String content, String url) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.MyMapper.insertNews", new News(title, content, url));
        }
    }

    @Override
    public boolean isLinkUsed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = (Integer) session.selectOne("com.github.MyMapper.isLinkUsed", link);
            return count != 0;
        }
    }

    @Override
    public void insertUnusedLink(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "unprocessed_links");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.MyMapper.insertLink", link);
        }
    }

    @Override
    public void insertUsedLink(String link) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "processed_links");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.MyMapper.insertLink", link);
        }
    }

}
