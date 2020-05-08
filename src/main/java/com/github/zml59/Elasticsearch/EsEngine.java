package com.github.zml59.Elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EsEngine {
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("请输入要查询的关键词");
            BufferedReader reader = new BufferedReader(new InputStreamReader((System.in), "UTF-8"));
            String keyword = reader.readLine();
            searchFromES(keyword);
        }
    }

    private static void searchFromES(String keyword) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.99.100", 9200, "http"))
        )) {
            SearchRequest request = new SearchRequest("news");
            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));
            SearchResponse res = client.search(request, RequestOptions.DEFAULT);
            res.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        }
    }
}
