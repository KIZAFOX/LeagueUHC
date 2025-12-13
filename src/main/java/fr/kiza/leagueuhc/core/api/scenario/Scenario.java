package fr.kiza.leagueuhc.core.api.scenario;

import org.bukkit.Material;
import org.bukkit.event.Listener;

public interface Scenario extends Listener {

    ScenarioType getType();

    Material getIcon();

    default String getId() {
        return getType().getId();
    }

    default String getName() {
        return getType().getDisplayName();
    }

    default String getDescription() {
        return getType().getDescription();
    }

    default boolean hasPercentage() {
        return getType().hasPercentage();
    }

    default void onEnable() {}

    default void onDisable() {}
}