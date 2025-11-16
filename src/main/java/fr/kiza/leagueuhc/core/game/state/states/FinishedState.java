package fr.kiza.leagueuhc.core.game.state.states;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;
import fr.kiza.leagueuhc.core.game.state.BaseGameState;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.managers.commands.CommandUHC;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Collections;
import java.util.Objects;

public class FinishedState extends BaseGameState {

    private boolean opsRestored = false;

    public FinishedState() {
        super(GameState.FINISHED.getName());
    }

    @Override
    public void onEnter(GameContext context) {
        this.opsRestored = false;

        final long entryTime = System.currentTimeMillis();
        context.setData("finishedTime", entryTime);

        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "==========================");
        this.broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "PARTIE TERMINÉE");
        this.broadcast(ChatColor.GOLD + "" + ChatColor.BOLD + "==========================");
        this.broadcast("");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (opsRestored) return;
                opsRestored = true;

                Bukkit.getOnlinePlayers().forEach(players -> setupPlayerForLobby(players));
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("LeagueUHC"), 200L);
    }

    @Override
    public void onExit(GameContext context) {  this.deleteUhcWorld(); }

    @Override
    public void update(GameContext context, long deltaTime) { }

    @Override
    public void handleInput(GameContext context, GameInput input) {
        if (Objects.requireNonNull(input.getType()) == InputType.PLAYER_JOIN) {
            final Player player = input.getPlayer();

            this.setupPlayerForLobby(player);

            player.sendMessage(ChatColor.YELLOW + "La partie précédente est terminée.");
            player.sendMessage(ChatColor.GRAY + "Retour au lobby dans quelques secondes...");
        }
    }

    private void setupPlayerForLobby(Player player) {
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 100, 0));

        if (player.isOp()) {
            player.getInventory().clear();

            player.getInventory().setItem(8,
                    new ItemBuilder(Material.REDSTONE_TORCH_ON)
                            .setName(ChatColor.RED + "" + ChatColor.BOLD + "Settings")
                            .setLore(Collections.singletonList(ChatColor.GRAY + "Clic droit pour ouvrir les paramètres"))
                            .toItemStack()
            );

            player.sendMessage(ChatColor.GREEN + "✔ Vous pouvez maintenant redémarrer une partie !");
        }
    }

    /**
     * Supprime complètement le monde UHC (uhc_world)
     */
    private void deleteUhcWorld() {
        World uhcWorld = PregenManager.world;
        if(uhcWorld == null) return;

        String worldName = uhcWorld.getName();
        File worldFolder = uhcWorld.getWorldFolder();

        new BukkitRunnable() {
            /**
             * When an object implementing interface <code>Runnable</code> is used
             * to create a thread, starting the thread causes the object's
             * <code>run</code> method to be called in that separately executing
             * thread.
             * <p>
             * The general contract of the method <code>run</code> is that it may
             * take any action whatsoever.
             *
             * @see Thread#run()
             */
            @Override
            public void run() {
                final PregenManager helper = new PregenManager(LeagueUHC.getInstance());
                helper.resetPregen();

                Bukkit.unloadWorld(uhcWorld, false);

                try {
                    CommandUHC.pregenManager.resetPregen();
                    deleteFolderRecursively(worldFolder);
                    Bukkit.getLogger().info("[LeagueUHC] Monde UHC supprimé : " + worldName);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[LeagueUHC] Impossible de supprimer le monde " + worldName + " : " + e.getMessage());
                }
            }
        }.runTaskLater(LeagueUHC.getInstance(), 20 * 2);
    }

    /**
     * Suppression récursive des fichiers
     */
    private void deleteFolderRecursively(File folder) {
        if(folder == null || !folder.exists()) return;

        File[] files = folder.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isDirectory()) {
                    deleteFolderRecursively(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }
}