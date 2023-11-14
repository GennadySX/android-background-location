package com.gennadysx.bgloc.headless;

import android.os.Bundle;

public abstract class Task implements JsCallback {
    public abstract String getName();
    public abstract Bundle getBundle();
}
