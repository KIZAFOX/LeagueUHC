package fr.kiza.leagueuhc.champion.ability;

import fr.kiza.leagueuhc.game.GamePlayer;
import org.bukkit.inventory.ItemStack;

public interface ChampionAbility {

    enum TriggerType {
        RIGHT_CLICK_ITEM,
        LEFT_CLICK_ITEM,
        BLOCK_PLACE,
        PROJECTILE_LAUNCH,
        PASSIVE_TICK
    }

    String getName();
    String getDescription();
    ItemStack getItem();

    int getCooldownTicks();

    TriggerType getTriggerType();

    void execute(final GamePlayer caster, final AbilityContext context);
}
