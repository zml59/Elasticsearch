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


public class Crawler {

    CrawlerDAO dao = new MyBatisCrawlerDAO();

    public void run() throws SQLException, IOException {
        String link;

        while ((link = dao.getOneLinkThenDelete()) != null) {

            //网址是否已经处理
            if (dao.isLinkUsed(link)) {
                continue;
            }

            if (isInterestingLink(link) || isIndexPage(link)) {
                Document doc = httpGetAndParseHtml(link);
                //爬取页面中的连接并加入到未处理表
                parseUrlsFromPageAndStoreIntoDB(doc);

                storeIntoDBwhileNews(doc, link);
                //把处理过的网址加入到已处理表
                dao.insertUsedLink(link);

            }
        }
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }


    private void parseUrlsFromPageAndStoreIntoDB(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            //把爬取的网址加入到未处理表
            if (isInterestingLink(href)) {
                dao.insertUnusedLink(href);
            }
        }
    }

    private void storeIntoDBwhileNews(Document doc, String link) throws SQLException {
        //如果是新闻详情页面就存入数据库，否则不做
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTag.child(0).text();
                String content = articleTag.select("[class=art_p]").stream().map(Element::text).collect(Collectors.joining("\n"));
//                System.out.println(articleTag);
                dao.insertNewsIntoDB(title, content, link);
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
