package com.gennadysx.bgloc;

import com.gennadysx.bgloc.data.BackgroundActivity;
import com.gennadysx.bgloc.data.BackgroundLocation;

/**
 * Created by finch on 27.11.2017.
 */

public interface PluginDelegate {
    void onAuthorizationChanged(int authStatus);
    void onLocationChanged(BackgroundLocation location);
    void onStationaryChanged(BackgroundLocation location);
    void onActivityChanged(BackgroundActivity activity);
    void onServiceStatusChanged(int status);
    void onAbortRequested();
    void onHttpAuthorization();
    void onError(PluginException error);
}
