package com.space.core;

import java.lang.reflect.Method;

/**
 * Created by lucifel on 19-8-3.
 */
public class MyHandlerMapping {
    private String controllerClassName;
    private Method controllerMothod;

    public String getControllerClassName() {
        return controllerClassName;
    }

    public void setControllerClassName(String controllerClassName) {
        this.controllerClassName = controllerClassName;
    }

    public Method getControllerMothod() {
        return controllerMothod;
    }

    public void setControllerMothod(Method controllerMothod) {
        this.controllerMothod = controllerMothod;
    }
}
