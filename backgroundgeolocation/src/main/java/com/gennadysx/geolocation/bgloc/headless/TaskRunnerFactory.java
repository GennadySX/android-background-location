package com.gennadysx.geolocation.bgloc.headless;

public class TaskRunnerFactory {

    public TaskRunner getTaskRunner(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (TaskRunner) Class.forName(className).newInstance();
    }
}
