package fr.kiza.leagueuhc.core.state.states;

import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import fr.kiza.leagueuhc.core.state.BaseGameState;
import fr.kiza.leagueuhc.core.state.GameState;
import fr.kiza.leagueuhc.packets.builder.ActionBarBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;

public class IdleState extends BaseGameState {

    public IdleState() {
        super(GameState.IDLE.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        context.reset();

        Bukkit.getOnlinePlayers().forEach(players -> context.addPlayer(players.getUniqueId()));

        this.broadcast(ChatColor.GOLD + "═══════════════════════════════");
        this.broadcast(ChatColor.YELLOW + "    " + ChatColor.BOLD + "LEAGUE UHC");
        this.broadcast("");
        this.broadcast(ChatColor.GRAY + "En attente du lancement...");
        this.broadcast(ChatColor.GOLD + "═══════════════════════════════");

        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);
    }

    @Override
    public void onExit(GameContext context) {
        broadcast(ChatColor.GREEN + "✔ Partie lancée par le host !");

        context.setData("hostStarted", false);

        Bukkit.getOnlinePlayers().forEach(players -> {
            players.getInventory().clear();
            players.setGameMode(GameMode.SURVIVAL);
        });
     }

    @Override
    public void update(GameContext context, long deltaTime) {
        Bukkit.getOnlinePlayers().forEach(players -> {
            if (players.isOp()) {
                ActionBarBuilder.create()
                        .message(ChatColor.GOLD + "Joueurs: " + ChatColor.YELLOW + context.getPlayerCount() + ChatColor.GOLD + " - Ouvre les settings pour lancer")
                        .send(players);
            } else {
                ActionBarBuilder.create()
                        .message(ChatColor.GRAY + "En attente du host... (" + ChatColor.YELLOW + context.getPlayerCount() + ChatColor.GRAY + " joueurs)")
                        .send(players);
            }
        });
    }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                final Player player = input.getPlayer();

                Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY  +"] " + ChatColor.YELLOW + player.getName()));

                context.addPlayer(player.getUniqueId());

                player.sendMessage(ChatColor.YELLOW + "Bienvenue sur LeagueUHC !");
                player.sendMessage(ChatColor.GRAY + "En attente du lancement de la partie...");

                this.setupPlayer(player);
                break;

            case PLAYER_LEAVE:
                Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "-" + ChatColor.GRAY  +"] " + ChatColor.YELLOW + input.getPlayer().getName()));
                context.removePlayer(input.getPlayer().getUniqueId());
                break;
            case HOST_START:
                if (context.getPlayerCount() >= 1) {
                    context.setData("hostStarted", true);
                    input.getPlayer().sendMessage(ChatColor.GREEN + "✔ Lancement de la partie avec " + context.getPlayerCount() + " joueur(s) !");
                } else {
                    input.getPlayer().sendMessage(ChatColor.RED + "✘ Impossible de lancer : aucun joueur connecté !");
                }
                break;

            default:
                break;
        }
    }

    private void setupPlayer(Player player) {
        player.getInventory().clear();

        if (player.isOp()) {
            player.setGameMode(GameMode.CREATIVE);
            player.getInventory().setItem(8,
                    new ItemBuilder(Material.REDSTONE_TORCH_ON)
                            .setName(ChatColor.RED + "" + ChatColor.BOLD + "Settings")
                            .setLore(Collections.singletonList(ChatColor.GRAY + "Clic droit pour les paramètres"))
                            .toItemStack()
            );
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}