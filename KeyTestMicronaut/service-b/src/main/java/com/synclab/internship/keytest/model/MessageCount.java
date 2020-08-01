package com.synclab.internship.keytest.model;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "message")
public class MessageCount {

    @Column(name = "count")
    private int count;

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "count=" + count;
    }
}
