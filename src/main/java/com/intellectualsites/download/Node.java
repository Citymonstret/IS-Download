package com.intellectualsites.download;

import org.json.simple.JSONObject;

public abstract class Node<T> {

    protected abstract String getIdentifier();

    protected abstract JSONObject generateJSON();

    protected abstract T getChild(final String key);

    public final JSONObject toJSON() {
        final JSONObject jsonObject = this.generateJSON();
        jsonObject.put("identifier", this.getIdentifier());
        return jsonObject;
    }

}
