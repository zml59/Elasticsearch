package com.github.zml59.Elasticsearch;

import java.sql.SQLException;

public interface CrawlerDAO {
    String getOneLinkThenDelete() throws SQLException;

    void insertNewsIntoDB(String title, String content, String url) throws SQLException;

    boolean isLinkUsed(String link) throws SQLException;

    void insertUnusedLink(String href);

    void insertUsedLink(String link);
}
