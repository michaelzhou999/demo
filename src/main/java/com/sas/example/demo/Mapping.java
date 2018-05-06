package com.sas.example.demo;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Key/value mapping
 */
@Entity
@Table(name = "STRINGMAPPING")
public class Mapping implements Serializable {

    // toString template
    private static final String TEMPLATE = "%s : %s";

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private long Id;

    /**
     *  Key has to be unique, empty but not null
     *
     *  Though HTTP protocol itself does not impose any constraints on the
     *  length of request URL, we have to put a limit on this for practical
     *  purpose. Of course, this can be adjustable.
     */
    @Column(name = "KEY", nullable = false, unique = true, length = 128)
    private String key;

    /**
     *  Value can be empty but not null
     *
     *  Similarly, we limit the length of the value to 1K characters.
     */
    @Column(name = "VALUE", nullable = false, length = 1024)
    private String value;

    public Mapping(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Mapping() {

    }

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

    @Override
    public String toString() {
        return String.format(TEMPLATE, key, value);
    }

}
