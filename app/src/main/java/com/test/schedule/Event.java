package com.test.schedule;

/**
 * Created by szyme on 16.10.2016.
 */

public class Event {
    public Event(String description, String category) {
        this.description = description;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    String category;
    String description;
}
