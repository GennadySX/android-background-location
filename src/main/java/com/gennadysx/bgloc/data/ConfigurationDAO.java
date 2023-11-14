package com.gennadysx.bgloc.data;

import org.json.JSONException;

import com.gennadysx.bgloc.Config;

public interface ConfigurationDAO {
    boolean persistConfiguration(Config config) throws NullPointerException;
    Config retrieveConfiguration() throws JSONException;
}
