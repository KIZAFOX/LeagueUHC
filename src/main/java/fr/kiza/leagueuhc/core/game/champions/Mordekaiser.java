package fr.kiza.leagueuhc.core.game.champions;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.api.champion.annotations.ChampionEntry;
import org.bukkit.Material;

@ChampionEntry
public class Mordekaiser extends Champion {
    protected Mordekaiser() {
        super(
                "Mordekaiser",
                "Revenant de fer",
                Category.FIGHTER,
                Region.SOLITAIRE,
                Material.ANVIL
        );
    }
}
