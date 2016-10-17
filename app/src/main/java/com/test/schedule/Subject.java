package com.test.schedule;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by szyme on 15.10.2016.
 */

public class Subject{
    private String name;

    public Subject(String name) {
        this.name = name;
    }

    Subject(JSONObject data) throws JSONException {
        this.name = data.getString("Name");
    }

    public String getName() {
        return name;
    }
}
