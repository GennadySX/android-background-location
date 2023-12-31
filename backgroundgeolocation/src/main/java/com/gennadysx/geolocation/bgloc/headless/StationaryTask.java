package com.gennadysx.geolocation.bgloc.headless;

import android.os.Bundle;

import com.gennadysx.geolocation.bgloc.data.BackgroundLocation;

public abstract class StationaryTask extends LocationTask {
    public StationaryTask(BackgroundLocation location) {
        super(location);
    }

    @Override
    public String getName() {
        return "stationary";
    }

    @Override
    public Bundle getBundle() {
        Bundle bundle = super.getBundle();

        return bundle;
    }
}
