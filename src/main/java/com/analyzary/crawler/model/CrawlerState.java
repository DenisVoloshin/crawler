package com.analyzary.crawler.model;

import com.google.gson.Gson;

/**
 * Defines a Crawler state
 */
public class CrawlerState {
    private String id;
    private State state;
    private int depth;
    // There is nothing in the GSON instance that makes it related to a specific instance.
    private static Gson jsonManager = new Gson();

    public CrawlerState(String id, State state,int depth) {
        this.id = id;
        this.state = state;
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

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
        CRAWLING,
        COMPLETE,
        RECOVERY,
        RE_VISIT

    }

    public String toJSON() {
        return jsonManager.toJson(this);
    }

    public static CrawlerState fromJSON(String json) {
        return jsonManager.fromJson(json, CrawlerState.class);
    }
}
