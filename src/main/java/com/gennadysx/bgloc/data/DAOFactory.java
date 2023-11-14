package com.gennadysx.bgloc.data;

import android.content.Context;

import com.gennadysx.bgloc.data.provider.ContentProviderLocationDAO;
import com.gennadysx.bgloc.data.sqlite.SQLiteConfigurationDAO;

public abstract class DAOFactory {
    public static LocationDAO createLocationDAO(Context context) {
        return new ContentProviderLocationDAO(context);
    }

    public static ConfigurationDAO createConfigurationDAO(Context context) {
        return new SQLiteConfigurationDAO(context);
    }
}
