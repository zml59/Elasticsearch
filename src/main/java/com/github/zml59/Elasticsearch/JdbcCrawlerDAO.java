package com.github.zml59.Elasticsearch;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDAO implements CrawlerDAO {

    private static final String USER_NAME = "root";
    private static final String PASSWORD = "hello123";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDAO() {
        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:h2:file:D:/JAVA/IdeaProjects/Elasticsearch/news",
                    USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //从数据库未处理表加载一个未处理链接作为link使用
    //再直接将其从未处理表中删除
    public String getOneLinkThenDelete() throws SQLException {
        String link = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from unprocessed_links limit 1");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                link = resultSet.getString(1);
            }
        }
        if (link != null) {
            updateDB(link, "delete from UNPROCESSED_LINKS where LINK = ?");
        }
        return link;
    }

    //把一个网址相对于数据库做删除/新增/更新操作。
    public void updateDB(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNewsIntoDB(String title, String content, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS(title, content, url, create_at, update_at)\n" +
                "values (?, ?, ?, now(), now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);
            statement.executeUpdate();
        }
    }

    //询问数据库当前连接是否已经处理
    public boolean isLinkUsed(String link) throws SQLException {
        ResultSet res = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT LINK FROM processed_links WHERE LINK = ? ")) {
            statement.setString(1, link);
            res = statement.executeQuery();
            while (res.next()) {
                return true;
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        return false;
    }
}
