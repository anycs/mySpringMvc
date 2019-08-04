package com.space.core;

/**
 * Created by lucifel on 19-8-2.
 */
public class MyViewResolver {
    private String prefix;
    private String suffix;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String jspMapping(String value){
        return this.prefix + value + this.suffix;
    }
}
