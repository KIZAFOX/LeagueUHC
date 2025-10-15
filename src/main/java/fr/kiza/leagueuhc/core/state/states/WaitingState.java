package fr.kiza.leagueuhc.core.state.states;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import fr.kiza.leagueuhc.core.state.BaseGameState;
import fr.kiza.leagueuhc.core.state.GameState;
import fr.kiza.leagueuhc.packets.builder.ActionBarBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;

public class WaitingState extends BaseGameState {

    public WaitingState() {
        super(GameState.WAITING.getName());
    }

    @Override
    public void onEnter(GameContext context) { }

    @Override
    public void onExit(GameContext context) { }

    @Override
    public void update(GameContext context, long deltaTime) { }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                final Player player = input.getPlayer();

                context.addPlayer(player.getUniqueId());

                player.getInventory().clear();

                if (player.isOp()) {
                    player.getInventory().addItem(
                            new ItemBuilder(Material.REDSTONE_TORCH_ON)
                                    .setName(ChatColor.RED + "" + ChatColor.BOLD + "Settings")
                                    .setLore(Collections.singletonList(ChatColor.GRAY + "Clic droit pour ouvrir les paramètres"))
                                    .toItemStack()
                    );
                    ActionBarBuilder.create()
                            .message(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "Hello host, look your inventory =)")
                            .send(player);
                }
                break;
            case PLAYER_LEAVE:
                context.removePlayer(input.getPlayer().getUniqueId());
                break;
            default:
                break;
        }
    }
}