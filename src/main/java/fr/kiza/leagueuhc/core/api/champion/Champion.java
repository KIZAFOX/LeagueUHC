package fr.kiza.leagueuhc.core.api.champion;

import fr.kiza.leagueuhc.core.api.champion.ability.ChampionAbility;
import fr.kiza.leagueuhc.core.game.GamePlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public abstract class Champion {

    private final String name;
    private final List<ChampionAbility> abilities;

    public static final ItemStack DEFAULT_ITEM = new ItemStack(Material.BONE);

    public Champion(String name, List<ChampionAbility> abilities) {
        this.name = name;
        this.abilities = abilities;
    }

    public abstract void onAssigned(final GamePlayer player);

    public void onUpdate(final GamePlayer player) { }

    public String getName() {
        return name;
    }

    public List<ChampionAbility> getAbilities() {
        return abilities;
    }

    public Optional<ChampionAbility> getAbility(final String name) {
        return this.abilities.stream().filter(ability -> ability.getName().equalsIgnoreCase(name)).findFirst();
    }
}
