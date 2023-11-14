package com.gennadysx.geolocation.bgloc.provider;

import com.gennadysx.geolocation.bgloc.PluginException;
import com.gennadysx.geolocation.bgloc.data.BackgroundActivity;
import com.gennadysx.geolocation.bgloc.data.BackgroundLocation;

public interface ProviderDelegate {
    void onLocation(BackgroundLocation location);
    void onStationary(BackgroundLocation location);
    void onActivity(BackgroundActivity activity);
    void onError(PluginException error);
}
