package com.github.zml59.Elasticsearch;

public class News {
    private Integer id;
    private String title;
    private String content;
    private String url;

    public News(String title, String content, String link) {
        this.title = title;
        this.content = content;
        this.url = link;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
