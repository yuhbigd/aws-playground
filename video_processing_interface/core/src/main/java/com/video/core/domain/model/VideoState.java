package com.video.core.domain.model;

public enum VideoState {
    PENDING("PENDING"), PROCESSING("PROCESSING"), PROCESSED("PROCESSED"), FAILED("FAILED");

    private String value;

    private VideoState(String name) {
        this.value = name;
    }

    public String getValue() {
        return value;
    }

    public static VideoState fromValue(String value) {
        for (VideoState state : VideoState.values()) {
            if (state.getValue().equals(value)) {
                return state;
            }
        }
        return null;
    }
}
