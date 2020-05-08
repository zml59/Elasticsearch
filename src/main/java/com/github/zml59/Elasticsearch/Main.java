package com.github.zml59.Elasticsearch;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Main {
    public static void main(String[] args) {
        @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
        CrawlerDAO dao = new MyBatisCrawlerDAO();
        for (int i = 0; i < 3; i++) {
            new Crawler(dao).start();
        }
    }
}
