package fr.kiza.leagueuhc.champion.champions.example;

import fr.kiza.leagueuhc.champion.Champion;
import fr.kiza.leagueuhc.champion.ability.AbilityContext;
import fr.kiza.leagueuhc.champion.ability.ChampionAbility;
import fr.kiza.leagueuhc.game.GamePlayer;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class Example extends Champion {

    private final ExampleAbility ability = new ExampleAbility();

    public Example() {
        super("Example", Collections.singletonList(new ExampleAbility()));
    }

    @Override
    public void onAssigned(GamePlayer player) {
        final ItemStack item = new ItemBuilder(this.ability.getItem())
                .setName(this.ability.getName())
                .setLore(Collections.singletonList(this.ability.getDescription()))
                .toItemStack();

        player.getPlayer().getInventory().addItem(item);
        player.getPlayer().updateInventory();
    }

    private static class ExampleAbility implements ChampionAbility {
        public ExampleAbility() { }

        @Override
        public String getName() {
            return "Example Ability Name";
        }

        @Override
        public String getDescription() {
            return "Example Ability Description";
        }

        @Override
        public ItemStack getItem() {
            return new ItemBuilder(Material.BONE).toItemStack();
        }

        @Override
        public int getCooldownTicks() {
            return 20; //20 ticks = 1s in real life
        }

        @Override
        public TriggerType getTriggerType() {
            return TriggerType.RIGHT_CLICK_ITEM;
        }

        @Override
        public void execute(GamePlayer caster, AbilityContext context) { }
    }
}
