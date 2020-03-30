package com.github.zml59.Elasticsearch;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;




public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "hello123";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:h2:file:D:/JAVA/IdeaProjects/Elasticsearch/news",
                USER_NAME, PASSWORD);

        String link;
        //从数据库未处理表加载一个未处理链接作为link使用
        //再直接将其从未处理表中删除
        while ((link = getOneLinkThenDelete(connection)) != null) {

            //网址是否已经处理
            if (isLinkUsed(connection, link)) {
                continue;
            }

            if (isInterestingLink(link) || isIndexPage(link)) {
                Document doc = httpGetAndParseHtml(link);
                //爬取页面中的连接并加入到未处理表
                parseUrlsFromPageAndStoreIntoDB(connection, doc);

                storeIntoDBwhileNews(connection, doc, link);
                //把处理过的网址加入到已处理表
                updateDB(connection, link, "insert into PROCESSED_LINKS(LINK) values ( ? )");
            }
        }
    }

    private static String getOneLinkThenDelete(Connection connection) throws SQLException {
        String link = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from unprocessed_links limit 1");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                link = resultSet.getString(1);
            }
        }
        if (link != null) {
            updateDB(connection, link, "delete from UNPROCESSED_LINKS where LINK = ?");
        }
        return link;
    }

    private static void parseUrlsFromPageAndStoreIntoDB(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            //把爬取的网址加入到未处理表
            if (isInterestingLink(href)) {
                updateDB(connection, href, "insert into UNPROCESSED_LINKS(LINK) values ( ? )");
            }
        }
    }

    //询问数据库当前连接是否已经处理
    private static boolean isLinkUsed(Connection connection, String link) throws SQLException {
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

    //把一个网址相对于数据库做删除/新增/更新操作。
    private static void updateDB(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }


    private static void storeIntoDBwhileNews(Connection connection, Document doc, String link) throws SQLException {
        //如果是新闻详情页面就存入数据库，否则不做
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("[class=art_p]").stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(articleTag);
                try (PreparedStatement statement = connection.prepareStatement("insert into NEWS(title, content, url, create_at, update_at)\n" +
                        "values (?, ?, ?, now(), now())")) {
                    statement.setString(1, title);
                    statement.setString(2, content);
                    statement.setString(3, link);
                    statement.executeUpdate();
                }
            }
        }
    }


    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        link = standardUrl(link);
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent",
                "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Mobile Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
//            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static String standardUrl(String link) {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
//        System.out.println(link);
        link = encodeContainsChineseUrl(link);
        return link;
    }

    private static boolean isInterestingLink(String link) {
        return isNewsPage(link) &&
                isNotLoginPage(link);
    }


    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");

    }

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static String encodeContainsChineseUrl(String link) {
        try {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(link);
            while (matcher.find()) {
                String tmp = matcher.group();
                link = link.replaceAll(tmp, java.net.URLEncoder.encode(tmp, "utf-8")).replaceAll("\\\\", "");
            }
            System.out.println(link);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
        return link;
    }
}
