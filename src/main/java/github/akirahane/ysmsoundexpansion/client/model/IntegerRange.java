package github.akirahane.ysmsoundexpansion.client.model;


import com.google.gson.JsonObject;

public record IntegerRange(int min, int max) {

    public IntegerRange(JsonObject jsonObject) {
        this(
                jsonObject.get("min") != null ? jsonObject.get("min").getAsInt() : 0,
                jsonObject.get("max") != null ? jsonObject.get("max").getAsInt() : 0
        );
    }

    public boolean contains(Integer value) {
        if (value == null) return false;
        return value >= min && value < max;
    }
}
