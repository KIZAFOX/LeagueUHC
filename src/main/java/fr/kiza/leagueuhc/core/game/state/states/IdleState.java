package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.core.api.packets.builder.TitleBuilder;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.event.PvPEvent;
import fr.kiza.leagueuhc.core.game.host.HostManager;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;

import org.bukkit.*;
import org.bukkit.entity.Player;


public class IdleState extends BaseGameState {

    public IdleState() {
        super(GameState.IDLE.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        context.reset();

        Bukkit.getOnlinePlayers().forEach(players -> this.setupPlayer(context, players));
        Bukkit.getPluginManager().callEvent(new PvPEvent(false));
    }

    @Override
    public void onExit(GameContext context) { context.setData("hostStarted", false); }

    @Override
    public void update(GameContext context, long deltaTime) { }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        switch (input.getType()) {
            case PLAYER_JOIN:
                this.setupPlayer(context, input.getPlayer());
                break;
            case PLAYER_LEAVE:
                Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "-" + ChatColor.GRAY  +"] " + ChatColor.YELLOW + input.getPlayer().getName()));
                context.removePlayer(input.getPlayer().getUniqueId());
                break;
            case HOST_START:
                if (context.getPlayerCount() == 1 || context.getPlayerCount() >= 1) context.setData("hostStarted", true);
                break;
            default:
                break;
        }
    }

    private void setupPlayer(final GameContext context, final Player player) {
        context.addPlayer(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY  +"] " + ChatColor.YELLOW + player.getName()));

        TitleBuilder.create()
                .title(ChatColor.YELLOW + "Bienvenue sur LeagueUHC !")
                .subTitle(ChatColor.RED + "En attente du lancement de la partie...")
                .times(20, 40,  20)
                .send(player);

        player.setGameMode(GameMode.ADVENTURE);

        player.setFoodLevel(20);
        player.setWalkSpeed(0.20F);
        player.setFlySpeed(0.15F);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setExp(0);
        player.setLevel(0);
        player.setMaxHealth(20.0D);
        player.setHealth(player.getMaxHealth());

        player.getInventory().setArmorContents(null);
        player.getInventory().clear();

        player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 100, 0));

        HostManager.giveItem(player);
    }
}