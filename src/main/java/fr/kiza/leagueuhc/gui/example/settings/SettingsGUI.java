package fr.kiza.leagueuhc.gui.example.settings;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.context.GameContext;
import fr.kiza.leagueuhc.core.input.GameInput;
import fr.kiza.leagueuhc.core.input.InputType;
import fr.kiza.leagueuhc.core.state.GameState;
import fr.kiza.leagueuhc.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SettingsGUI implements Listener {

    private final LeagueUHC instance;
    private final GameContext context;

    public SettingsGUI(final LeagueUHC instance, final Player player) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();

        this.buildGUI(player);

        this.instance.getServer().getPluginManager().registerEvents(this, this.instance);
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItem();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (itemStack == null || itemStack.getType() != Material.REDSTONE_TORCH_ON) return;
        if (!itemStack.getItemMeta().getDisplayName().equals(ChatColor.RED + "" + ChatColor.BOLD + "Settings")) return;
        if (!player.isOp()) return;

        event.setCancelled(true);
        new SettingsGUI(this.instance, player);
    }

    private void buildGUI(final Player player) {
        int maxPlayers = context.getMaxPlayers();
        int countdown = context.getCountdown();
        String currentState = instance.getGameEngine().getCurrentState();
        boolean isIdle = currentState.equals(GameState.IDLE.getName());

        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.RED + "UHC Settings")
                .size(27);

        gui.button(11, new ItemBuilder(Material.SKULL_ITEM)
                .setDurability((short) 3)
                .setName(ChatColor.RED + "Joueurs Maximum")
                .setLore(Arrays.asList(
                        ChatColor.GRAY + "Actuel: " + ChatColor.YELLOW + maxPlayers,
                        "",
                        ChatColor.YELLOW + "▸ Cliquer pour modifier",
                        "",
                        ChatColor.GRAY + "Range: 2-32"
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsEditorGUI(instance, p, SettingsType.MAX_PLAYERS);
        });

        // Countdown
        gui.button(13, new ItemBuilder(Material.WATCH)
                .setName(ChatColor.GOLD + "Countdown")
                .setLore(Arrays.asList(
                        ChatColor.GRAY + "Actuel: " + ChatColor.YELLOW + countdown + "s",
                        "",
                        ChatColor.YELLOW + "▸ Cliquer pour modifier",
                        "",
                        ChatColor.GRAY + "Range: 5-60s"
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsEditorGUI(instance, p, SettingsType.COUNTDOWN);
        });

        // BOUTON LANCER LA PARTIE
        gui.button(15, new ItemBuilder(isIdle ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .setName(isIdle ? ChatColor.GREEN + "" + ChatColor.BOLD + "LANCER LA PARTIE" : ChatColor.RED + "" + ChatColor.BOLD + "PARTIE EN COURS")
                .setLore(isIdle ? Arrays.asList(
                        ChatColor.GRAY + "Joueurs connectés: " + ChatColor.YELLOW + context.getPlayerCount(),
                        "",
                        ChatColor.GREEN + "✓ Cliquer pour démarrer",
                        "",
                        ChatColor.GRAY + "La partie passera en mode",
                        ChatColor.GRAY + "attente de joueurs"
                ) : Arrays.asList(
                        ChatColor.GRAY + "La partie est déjà lancée !",
                        "",
                        ChatColor.RED + "✗ Impossible de relancer"
                ))
                .toItemStack(), (p, inv) -> {
            if (isIdle) {
                p.closeInventory();

                GameInput input = new GameInput(InputType.HOST_START, p, null);
                instance.getGameEngine().handleInput(input);

                p.sendMessage(ChatColor.GREEN + "✔ Partie lancée avec succès !");
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
            } else {
                p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                p.sendMessage(ChatColor.RED + "✘ La partie est déjà en cours !");
            }
        });

        // Bouton Fermer
        gui.button(22, new ItemBuilder(Material.BARRIER)
                .setName(ChatColor.DARK_RED + "Fermer")
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.0f);
        });

        // Remplissage avec du verre noir
        for (int i = 0; i < 27; i++) {
            if (i != 11 && i != 13 && i != 15 && i != 22) {
                gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 15)
                        .setName(" ")
                        .toItemStack(), (p, inv) -> { });
            }
        }

        gui.build().open(player);
    }
}