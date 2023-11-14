package com.gennadysx.geolocation.bgloc.data;

import org.json.JSONException;

import com.gennadysx.geolocation.bgloc.Config;

public interface ConfigurationDAO {
    boolean persistConfiguration(Config config) throws NullPointerException;
    Config retrieveConfiguration() throws JSONException;
}
