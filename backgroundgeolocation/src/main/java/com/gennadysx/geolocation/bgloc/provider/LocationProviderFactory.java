/*
According to apache license

This is fork of christocracy cordova-plugin-background-geolocation plugin
https://github.com/christocracy/cordova-plugin-background-geolocation

This is a new class
*/

package com.gennadysx.geolocation.bgloc.provider;

import android.content.Context;

import com.gennadysx.geolocation.bgloc.Config;
import com.gennadysx.geolocation.bgloc.DistanceFilterLocationProvider;

import java.lang.IllegalArgumentException;

/**
 * LocationProviderFactory
 */
public class LocationProviderFactory {

    private Context mContext;

    public LocationProviderFactory(Context context) {
        this.mContext = context;
    };

    public LocationProvider getInstance (Integer locationProvider) {
        LocationProvider provider;
        switch (locationProvider) {
            case Config.DISTANCE_FILTER_PROVIDER:
                provider = new DistanceFilterLocationProvider(mContext);
                break;
            case Config.ACTIVITY_PROVIDER:
                provider = new ActivityRecognitionLocationProvider(mContext);
                break;
            case Config.RAW_PROVIDER:
                provider = new RawLocationProvider(mContext);
                break;
            default:
                throw new IllegalArgumentException("Provider not found");
        }

        return provider;
    }
}
