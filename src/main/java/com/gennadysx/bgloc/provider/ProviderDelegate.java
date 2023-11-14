package com.gennadysx.bgloc.provider;

import com.gennadysx.bgloc.PluginException;
import com.gennadysx.bgloc.data.BackgroundActivity;
import com.gennadysx.bgloc.data.BackgroundLocation;

public interface ProviderDelegate {
    void onLocation(BackgroundLocation location);
    void onStationary(BackgroundLocation location);
    void onActivity(BackgroundActivity activity);
    void onError(PluginException error);
}
