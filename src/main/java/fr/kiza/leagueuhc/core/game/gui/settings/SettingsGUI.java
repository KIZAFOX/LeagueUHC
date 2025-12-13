package fr.kiza.leagueuhc.core.game.gui.settings;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.gui.scenarios.ScenariosGUI;
import fr.kiza.leagueuhc.core.game.gui.champions.ChampionsGUI;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.input.GameInput;
import fr.kiza.leagueuhc.core.game.input.InputType;
import fr.kiza.leagueuhc.core.game.state.GameState;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.core.game.host.HostManager;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack itemStack = event.getItem();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (itemStack == null || itemStack.getType() != Material.REDSTONE_TORCH_ON) return;
        if (!itemStack.getItemMeta().getDisplayName().equals(ChatColor.RED + "" + ChatColor.BOLD + "Settings")) return;
        if (!HostManager.isHost(player)) return;

        event.setCancelled(true);
        new SettingsGUI(this.instance, player);
    }

    private void buildGUI(final Player player) {
        String currentState = instance.getGameEngine().getCurrentState();
        boolean isIdle = currentState.equals(GameState.IDLE.getName());
        boolean isPregenComplete = !CommandUHC.pregenManager.isRunning() && CommandUHC.pregenManager.getWorld() != null;

        int activeScenarios = context.getActiveScenarioIds().size();
        int activeChampions = context.getEnabledChampions().size();
        int totalChampions = ChampionRegistry.getCount();

        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.DARK_RED + "⚔ " + ChatColor.RED + ChatColor.BOLD + "UHC SETTINGS" + ChatColor.DARK_RED + " ⚔")
                .size(45);

        for (int i = 0; i < 9; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 14)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        String scenarioStatus = activeScenarios > 0
                ? ChatColor.GREEN + "✓ " + activeScenarios + " actifs"
                : ChatColor.GRAY + "Aucun actif";

        gui.button(11, new ItemBuilder(Material.ENCHANTED_BOOK)
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

        String championStatus = activeChampions > 0
                ? ChatColor.GREEN + "✓ " + activeChampions + "/" + totalChampions + " activés"
                : ChatColor.RED + "✗ Aucun activé";

        gui.button(15, new ItemBuilder(Material.NETHER_STAR)
                .setName(ChatColor.GOLD + "⚔ " + ChatColor.BOLD + "CHAMPIONS")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "┃ Status: " + championStatus,
                        ChatColor.GRAY + "┃",
                        ChatColor.GRAY + "┃ " + ChatColor.YELLOW + "► Clic pour gérer les champions",
                        ChatColor.GRAY + "┃ " + ChatColor.DARK_GRAY + "Ahri, Yasuo, Lee Sin...",
                        ""
                ))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new ChampionsGUI(instance, p, 0);
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.5f);
        });

        for (int i = 18; i < 27; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 7)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        Material buttonMaterial;
        String buttonName;
        List<String> buttonLore = new ArrayList<>();
        boolean canStart = isIdle && isPregenComplete && activeChampions > 0;
        short glassColor;

        if (!isIdle) {
            buttonMaterial = Material.REDSTONE_BLOCK;
            buttonName = ChatColor.RED + "" + ChatColor.BOLD + "✖ PARTIE EN COURS ✖";
            glassColor = 14;
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
            glassColor = 14;
            buttonLore.addAll(Arrays.asList(
                    "",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ChatColor.GRAY + "  La map doit être pré-générée",
                    ChatColor.GRAY + "  avant de lancer la partie !",
                    "",
                    ChatColor.RED + "  ✗ Pré-génération requise",
                    "",
                    ChatColor.YELLOW + "  Commande:",
                    ChatColor.WHITE + "  /uhc pregen start",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));
        } else if (activeChampions == 0) {
            buttonMaterial = Material.BARRIER;
            buttonName = ChatColor.RED + "" + ChatColor.BOLD + "⚠ AUCUN CHAMPION ⚠";
            glassColor = 14;
            buttonLore.addAll(Arrays.asList(
                    "",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ChatColor.GRAY + "  Au moins un champion doit",
                    ChatColor.GRAY + "  être activé pour jouer !",
                    "",
                    ChatColor.RED + "  ✗ Activez des champions",
                    ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));
        } else {
            buttonMaterial = Material.EMERALD_BLOCK;
            buttonName = ChatColor.GREEN + "" + ChatColor.BOLD + "▶ LANCER LA PARTIE ▶";
            glassColor = 5;
            buttonLore.addAll(Arrays.asList(
                    "",
                    ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ChatColor.GRAY + "  Joueurs: " + ChatColor.YELLOW + context.getPlayerCount(),
                    ChatColor.GRAY + "  Map: " + ChatColor.GREEN + "✓ Générée",
                    ChatColor.GRAY + "  Champions: " + ChatColor.YELLOW + activeChampions + " activés",
                    ChatColor.GRAY + "  Scénarios: " + ChatColor.YELLOW + activeScenarios + " actifs",
                    "",
                    ChatColor.GREEN + "  ✓ Tout est prêt !",
                    "",
                    ChatColor.YELLOW + "  ► Cliquer pour démarrer",
                    ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                    ""
            ));
        }

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
                p.sendMessage(ChatColor.YELLOW + "  Utilisez: " + ChatColor.WHITE + "/uhc pregen start");
                p.sendMessage(ChatColor.RED + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                p.sendMessage("");
            } else if (activeChampions == 0) {
                p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                p.sendMessage(ChatColor.RED + "✘ Activez au moins un champion avant de lancer !");
            } else {
                p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
                p.sendMessage(ChatColor.RED + "✘ La partie est déjà en cours !");
            }
        });

        int[] decorationSlots = {21, 22, 23, 30, 32};
        for (int slot : decorationSlots) {
            gui.button(slot, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability(glassColor)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        for (int i = 36; i < 44; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 14)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        gui.button(44, new ItemBuilder(Material.BARRIER)
                .setName(ChatColor.RED + "✖ " + ChatColor.BOLD + "FERMER")
                .setLore(Arrays.asList("", ChatColor.GRAY + "Fermer ce menu", ""))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 0.5f);
        });

        int[] usedSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26, 30, 31, 32, 36, 37, 38, 39, 40, 41, 42, 43, 44};

        for (int i = 0; i < 45; i++) {
            boolean isUsed = false;
            for (int used : usedSlots) {
                if (i == used) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed) {
                gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 15)
                        .setName(" ")
                        .toItemStack(), (p, inv) -> { });
            }
        }

        gui.build().open(player);
    }
}