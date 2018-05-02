package com.sas.example.demo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Key/value mapping
 */
@Entity
public class Mapping {
    // Primary key
    @Id
    @NotNull
    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
