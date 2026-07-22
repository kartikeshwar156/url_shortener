package com.project.url_shortener.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequence_counters")
public class SequenceCounter {

    @Id
    private String id;

    private long value;

    public SequenceCounter() {
    }

    public SequenceCounter(String id, long value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
