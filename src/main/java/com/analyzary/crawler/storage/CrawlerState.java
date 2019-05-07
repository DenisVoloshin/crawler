package com.analyzary.crawler.storage;

import com.google.gson.Gson;

public class CrawlerState {
    private String id;
    private State state;
    // There is nothing in the GSON instance that makes it related to a specific instance.
    private static Gson jsonManager = new Gson();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        RUNNING,
        COMPLETE
    }

    public String toJSON() {
        return jsonManager.toJson(this);
    }

    public static CrawlerState fromJSON(String json) {
        return jsonManager.fromJson(json, CrawlerState.class);
    }
}
