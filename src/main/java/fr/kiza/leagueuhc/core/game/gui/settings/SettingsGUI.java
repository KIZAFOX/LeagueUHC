package fr.kiza.leagueuhc.core.game.gui.settings;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.gui.effects.EffectsConfigGUI;
import fr.kiza.leagueuhc.core.game.gui.scenarios.ScenariosGUI;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.managers.commands.CommandUHC;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        boolean isPregenComplete = !CommandUHC.pregenManager.isRunning() && CommandUHC.pregenManager.getWorld() != null;

        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.DARK_RED + "⚔ " + ChatColor.RED + ChatColor.BOLD + "UHC SETTINGS" + ChatColor.DARK_RED + " ⚔")
                .size(54);

        // ═══════════════════════════════════════
        // LIGNE DE SÉPARATION SUPÉRIEURE (Row 0)
        // ═══════════════════════════════════════
        for (int i = 0; i < 9; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 14) // Rouge
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        // ═══════════════════════════════════════
        // SECTION CONFIGURATION (Row 1-2)
        // ═══════════════════════════════════════

        // Titre section
        gui.button(10, new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) 1) // Orange
                .setName(ChatColor.GOLD + "▎" + ChatColor.BOLD + "CONFIGURATION")
                .toItemStack(), (p, inv) -> { });

        // Joueurs Maximum
        gui.button(11, new ItemBuilder(Material.SKULL_ITEM)
                .setDurability((short) 3)
                .setName(ChatColor.RED + "⚔ " + ChatColor.BOLD + "JOUEURS MAXIMUM")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "┃ Actuel: " + ChatColor.YELLOW + ChatColor.BOLD + maxPlayers + " joueurs",
                        ChatColor.GRAY + "┃",
                        ChatColor.GRAY + "┃ " + ChatColor.YELLOW + "► Clic gauche/droit pour modifier",
                        ChatColor.GRAY + "┃ " + ChatColor.DARK_GRAY + "Range: 2-32",
                        ""
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsEditorGUI(instance, p, SettingsType.MAX_PLAYERS);
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.5f);
        });

        // Countdown
        gui.button(12, new ItemBuilder(Material.WATCH)
                .setName(ChatColor.GOLD + "⏱ " + ChatColor.BOLD + "COUNTDOWN")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "┃ Actuel: " + ChatColor.YELLOW + ChatColor.BOLD + countdown + " secondes",
                        ChatColor.GRAY + "┃",
                        ChatColor.GRAY + "┃ " + ChatColor.YELLOW + "► Clic gauche/droit pour modifier",
                        ChatColor.GRAY + "┃ " + ChatColor.DARK_GRAY + "Range: 5-60s",
                        ""
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsEditorGUI(instance, p, SettingsType.COUNTDOWN);
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.5f);
        });

        // Effets
        gui.button(13, new ItemBuilder(Material.POTION)
                .setDurability((short) 8201)
                .setName(ChatColor.AQUA + "✦ " + ChatColor.BOLD + "EFFETS")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "┃ Configure les effets de potion",
                        ChatColor.GRAY + "┃ appliqués aux joueurs",
                        ChatColor.GRAY + "┃",
                        ChatColor.GRAY + "┃ " + ChatColor.YELLOW + "► Clic pour gérer les effets",
                        ChatColor.GRAY + "┃ " + ChatColor.DARK_GRAY + "Force, Résistance, Speed...",
                        ""
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new EffectsConfigGUI(instance, p);
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.5f);
        });

        // Scénarios
        int activeScenarios = context.getActiveScenarios().size();
        String scenarioStatus = activeScenarios > 0
                ? ChatColor.GREEN + "✓ " + activeScenarios + " actifs"
                : ChatColor.GRAY + "Aucun actif";

        gui.button(14, new ItemBuilder(Material.ENCHANTED_BOOK)
                .setName(ChatColor.LIGHT_PURPLE + "✹ " + ChatColor.BOLD + "SCÉNARIOS")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "┃ Status: " + scenarioStatus,
                        ChatColor.GRAY + "┃",
                        ChatColor.GRAY + "┃ " + ChatColor.YELLOW + "► Clic pour gérer les scénarios",
                        ChatColor.GRAY + "┃ " + ChatColor.DARK_GRAY + "CutClean, DiamondLimit...",
                        ""
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new ScenariosGUI(instance, p);
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.5f);
        });

        gui.button(15, new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) 1) // Orange
                .setName(" ")
                .toItemStack(), (p, inv) -> { });

        // ═══════════════════════════════════════
        // SÉPARATEUR (Row 2-3)
        // ═══════════════════════════════════════
        for (int i = 18; i < 27; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 7) // Gris
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        // ═══════════════════════════════════════
        // SECTION LANCEMENT (Row 3-4)
        // ═══════════════════════════════════════

        // Remplissage gauche
        for (int i = 27; i < 30; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 15) // Noir
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        // BOUTON LANCER LA PARTIE (GRAND FORMAT 3x3)
        Material buttonMaterial;
        String buttonName;
        List<String> buttonLore = new ArrayList<>();
        boolean canStart = isIdle && isPregenComplete;
        short glassColor;

        if (!isIdle) {
            buttonMaterial = Material.REDSTONE_BLOCK;
            buttonName = ChatColor.RED + "" + ChatColor.BOLD + "✖ PARTIE EN COURS ✖";
            glassColor = 14; // Rouge
            buttonLore.addAll(Arrays.asList(
                    "",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ChatColor.GRAY + "  La partie est déjà lancée !",
                    "",
                    ChatColor.RED + "  ✗ Impossible de relancer",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));
        } else if (!isPregenComplete) {
            buttonMaterial = Material.BARRIER;
            buttonName = ChatColor.RED + "" + ChatColor.BOLD + "⚠ MAP NON GÉNÉRÉE ⚠";
            glassColor = 14; // Rouge
            buttonLore.addAll(Arrays.asList(
                    "",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ChatColor.GRAY + "  La map doit être pré-générée",
                    ChatColor.GRAY + "  avant de lancer la partie !",
                    "",
                    ChatColor.RED + "  ✗ Pré-génération requise",
                    "",
                    ChatColor.YELLOW + "  Commande:",
                    ChatColor.WHITE + "  /uhc pregen <taille>",
                    ChatColor.DARK_GRAY + "  Exemple: /uhc pregen 1000",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));
        } else {
            buttonMaterial = Material.EMERALD_BLOCK;
            buttonName = ChatColor.GREEN + "" + ChatColor.BOLD + "▶ LANCER LA PARTIE ▶";
            glassColor = 5; // Vert citron
            buttonLore.addAll(Arrays.asList(
                    "",
                    ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ChatColor.GRAY + "  Joueurs: " + ChatColor.YELLOW + context.getPlayerCount() + ChatColor.DARK_GRAY + "/" + ChatColor.YELLOW + maxPlayers,
                    ChatColor.GRAY + "  Map: " + ChatColor.GREEN + "✓ Générée",
                    ChatColor.GRAY + "  Scénarios: " + ChatColor.YELLOW + activeScenarios + " actifs",
                    ChatColor.GRAY + "  Countdown: " + ChatColor.YELLOW + countdown + "s",
                    "",
                    ChatColor.GREEN + "  ✓ Tout est prêt !",
                    "",
                    ChatColor.YELLOW + "  ► Cliquer pour démarrer",
                    ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));
        }

        // Bouton central principal
        gui.button(31, new ItemBuilder(buttonMaterial)
                .setName(buttonName)
                .setLore(buttonLore)
                .toItemStack(), (p, inv) -> {
            if (canStart) {
                p.closeInventory();

                GameInput input = new GameInput(InputType.HOST_START, p, null);
                instance.getGameEngine().handleInput(input);

                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 0.5f, 2.0f);
            } else if (!isPregenComplete) {
                p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                p.sendMessage("");
                p.sendMessage(ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                p.sendMessage(ChatColor.RED + "⚠ " + ChatColor.BOLD + "MAP NON GÉNÉRÉE" + ChatColor.RED + " ⚠");
                p.sendMessage("");
                p.sendMessage(ChatColor.YELLOW + "  Utilisez: " + ChatColor.WHITE + "/uhc pregen <taille>");
                p.sendMessage(ChatColor.GRAY + "  Exemple: " + ChatColor.WHITE + "/uhc pregen 1000");
                p.sendMessage(ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                p.sendMessage("");
            } else {
                p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                p.sendMessage(ChatColor.RED + "✘ La partie est déjà en cours !");
            }
        });

        // Décoration autour du bouton principal
        int[] decorationSlots = {21, 22, 23, 30, 32, 39, 40, 41};
        for (int slot : decorationSlots) {
            gui.button(slot, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability(glassColor)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        // Remplissage droite
        for (int i = 33; i < 36; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 15) // Noir
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        // ═══════════════════════════════════════
        // LIGNE DE SÉPARATION INFÉRIEURE (Row 5)
        // ═══════════════════════════════════════
        for (int i = 45; i < 53; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 14) // Rouge
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        // Bouton Fermer
        gui.button(53, new ItemBuilder(Material.BARRIER)
                .setName(ChatColor.RED + "✖ " + ChatColor.BOLD + "FERMER")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Fermer ce menu",
                        ""
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 0.5f);
        });

        int[] usedSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 39, 40, 41, 45, 46, 47, 48, 49, 50, 51, 52, 53};

        for (int i = 0; i < 54; i++) {
            boolean isUsed = false;
            for (int used : usedSlots) {
                if (i == used) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed) {
                gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 15) // Noir
                        .setName(" ")
                        .toItemStack(), (p, inv) -> { });
            }
        }

        gui.build().open(player);
    }
}