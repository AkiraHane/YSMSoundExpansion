package github.akirahane.ysmsoundexpansion.client.model;


import com.google.gson.JsonObject;

public record EntityConditions(IntegerRange health, IntegerRange hunger, IntegerRange air, IntegerRange xpLevel) {

    public EntityConditions(JsonObject entity) {
        this(
            entity.get("health") != null ? new IntegerRange(entity.get("health").getAsJsonObject()) : null,
            entity.get("hunger") != null ? new IntegerRange(entity.get("hunger").getAsJsonObject()) : null,
            entity.get("air") != null ? new IntegerRange(entity.get("air").getAsJsonObject()) : null,
            entity.get("xp_level") != null ? new IntegerRange(entity.get("xp_level").getAsJsonObject()) : null
        );
    }
}
