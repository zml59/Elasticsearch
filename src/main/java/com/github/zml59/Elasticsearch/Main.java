package com.github.zml59.Elasticsearch;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        Set<String> usedLinks = new HashSet<>();
        linkPool.add("https://sina.cn/");
        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }
            //ArrayList从尾部删除更有效率
            String link = linkPool.remove(linkPool.size() - 1);
            if (usedLinks.contains(link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                storeIntoDBwhileNews(doc);
                usedLinks.add(link);
            }
        }
    }

    private static void storeIntoDBwhileNews(Document doc) {
        //如果是新闻详情页面就存入数据库，否则不做
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags
            ) {
                String title = articleTag.child(0).text();
                System.out.println(title);
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
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static String standardUrl(String link) {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        System.out.println(link);
        link = encodeContainsChineseUrl(link);
        return link;
    }


    private static boolean isInterestingLink(String link) {
        return (isNewsPage(link) || isIndexPage(link)) &&
                isNotLoginPage(link);
    }


    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");

    }

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn/");
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
