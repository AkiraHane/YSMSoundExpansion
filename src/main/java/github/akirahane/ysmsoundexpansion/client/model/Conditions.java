package github.akirahane.ysmsoundexpansion.client.model;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record Conditions(
        Pattern blockId,
        Pattern blockTag,
        Pattern blockType,
        Pattern itemId,
        List<String> weathers,
        List<IntegerRange> times,
        Pattern dimensions,
        EntityConditions entity
) {
    public Conditions(JsonObject jsonObject) {
        this(
                jsonObject.get("block_id") != null ? Pattern.compile(jsonObject.get("block_id").getAsString()) : null,
                jsonObject.get("block_tag") != null ? Pattern.compile(jsonObject.get("block_tag").getAsString()) : null,
                jsonObject.get("block_type") != null ? Pattern.compile(jsonObject.get("block_type").getAsString()) : null,
                jsonObject.get("item_id") != null ? Pattern.compile(jsonObject.get("item_id").getAsString()) : null,
                jsonObject.get("weathers") != null ? jsonObject.get("weathers").getAsJsonArray().asList()
                        .stream().map(JsonElement::getAsString).collect(Collectors.toList()) : null,
                jsonObject.get("times") != null ? jsonObject.get("times").getAsJsonArray().asList()
                        .stream().map(
                                jsonElement -> new IntegerRange(jsonElement.getAsJsonObject())
                        )
                        .collect(Collectors.toList()) : null,
                jsonObject.get("dimensions") != null ? Pattern.compile(jsonObject.get("dimensions").getAsString()) : null,
                jsonObject.get("entity") != null ? new EntityConditions(jsonObject.get("entity").getAsJsonObject()) : null
        );
    }
}
