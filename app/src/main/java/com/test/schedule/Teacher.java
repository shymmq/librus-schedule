package com.test.schedule;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by szyme on 15.10.2016.
 */

public class Teacher{
    String firstName,lastName;

    public Teacher(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Teacher(JSONObject data) throws JSONException {
        this.firstName=data.getString("FirstName");
        this.lastName=data.getString("LastName");
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getName(){
        return firstName+ " " + lastName;
    }
}
