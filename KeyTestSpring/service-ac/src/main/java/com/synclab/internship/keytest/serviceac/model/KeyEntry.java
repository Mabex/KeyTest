package com.synclab.internship.keytest.serviceac.model;

public class KeyEntry {

    private String serviceName;
    private String publicKey;

    public KeyEntry(String serviceName, String publicKey) {
        this.serviceName = serviceName;
        this.publicKey = publicKey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "KeyEntry{" +
                "serviceName='" + serviceName + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
