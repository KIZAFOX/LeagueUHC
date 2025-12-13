package fr.kiza.leagueuhc.core.game.gui.champions;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.gui.settings.SettingsGUI;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.champion.ability.Ability;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChampionsGUI {

    private final LeagueUHC instance;
    private final GameContext context;
    private final int page;
    private static final int CHAMPIONS_PER_PAGE = 28;

    public ChampionsGUI(final LeagueUHC instance, final Player player, final int page) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();
        this.page = page;
        buildGUI(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
    }

    private void buildGUI(final Player player) {
        List<Champion> allChampions = new ArrayList<>(ChampionRegistry.getChampions());
        int totalPages = (int) Math.ceil((double) allChampions.size() / CHAMPIONS_PER_PAGE);
        int currentPage = Math.min(page, Math.max(0, totalPages - 1));
        int enabledCount = context.getEnabledChampions().size();
        int totalCount = allChampions.size();

        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.GOLD + "⚔ Champions " + ChatColor.GRAY + "(" + (currentPage + 1) + "/" + Math.max(1, totalPages) + ")")
                .size(54);

        for (int i = 0; i < 9; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 1)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        gui.button(4, new ItemBuilder(Material.NETHER_STAR)
                .setName(ChatColor.GOLD + "⚔ " + ChatColor.BOLD + "CHAMPIONS")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "┃ Activés: " + ChatColor.GREEN + enabledCount + ChatColor.GRAY + "/" + ChatColor.YELLOW + totalCount,
                        ChatColor.GRAY + "┃",
                        ChatColor.GRAY + "┃ " + ChatColor.GREEN + "✦ Enchanté" + ChatColor.GRAY + " = Activé",
                        ChatColor.GRAY + "┃ " + ChatColor.RED + "0" + ChatColor.GRAY + " = Désactivé",
                        ""
                ))
                .toItemStack(), (p, inv) -> { });

        gui.button(0, new ItemBuilder(Material.EMERALD)
                .setName(ChatColor.GREEN + "✓ " + ChatColor.BOLD + "TOUT ACTIVER")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Active tous les champions",
                        "",
                        ChatColor.YELLOW + "► Cliquer pour activer tout"
                ))
                .toItemStack(), (p, inv) -> {
            context.enableAllChampions();
            p.sendMessage(ChatColor.GREEN + "[UHC] " + ChatColor.YELLOW + "Tous les champions ont été activés !");
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
            p.closeInventory();
            new ChampionsGUI(instance, p, currentPage);
        });

        gui.button(8, new ItemBuilder(Material.REDSTONE)
                .setName(ChatColor.RED + "✗ " + ChatColor.BOLD + "TOUT DÉSACTIVER")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Désactive tous les champions",
                        "",
                        ChatColor.YELLOW + "► Cliquer pour désactiver tout"
                ))
                .toItemStack(), (p, inv) -> {
            context.disableAllChampions();
            p.sendMessage(ChatColor.RED + "[UHC] " + ChatColor.YELLOW + "Tous les champions ont été désactivés !");
            p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            p.closeInventory();
            new ChampionsGUI(instance, p, currentPage);
        });

        int[] championSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIndex = currentPage * CHAMPIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + CHAMPIONS_PER_PAGE, allChampions.size());

        for (int i = 0; i < championSlots.length; i++) {
            int championIndex = startIndex + i;
            int slot = championSlots[i];

            if (championIndex < endIndex) {
                Champion champion = allChampions.get(championIndex);
                boolean isEnabled = context.isChampionEnabled(champion);

                ItemBuilder builder = new ItemBuilder(champion.getIcon())
                        .setName((isEnabled ? ChatColor.GREEN : ChatColor.RED) + champion.getName());

                List<String> lore = new ArrayList<>();
                lore.add(champion.getCategory().getColoredName());
                lore.add(champion.getRegion().getColoredName());
                lore.add("");

                if (champion.getDescription() != null && !champion.getDescription().isEmpty()) {
                    lore.add(ChatColor.GRAY + champion.getDescription());
                    lore.add("");
                }

                if (!champion.getAbilities().isEmpty()) {
                    lore.add(ChatColor.YELLOW + "Capacités:");
                    for (Ability ability : champion.getAbilities()) {
                        lore.add(ChatColor.GRAY + "  • " + ChatColor.WHITE + ability.getName());
                    }
                }

                lore.add("");
                lore.add(isEnabled
                        ? ChatColor.GREEN + "✓ Activé " + ChatColor.GRAY + "(clic pour désactiver)"
                        : ChatColor.RED + "✗ Désactivé " + ChatColor.GRAY + "(clic pour activer)");

                builder.setLore(lore);

                if (isEnabled) {
                    builder.addEnchant(Enchantment.DURABILITY, 1);
                    builder.hideEnchant();
                }

                gui.button(slot, builder.toItemStack(), (p, inv) -> {
                    toggleChampion(p, champion, currentPage);
                });
            } else {
                gui.button(slot, new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 15)
                        .setName(" ")
                        .toItemStack(), (p, inv) -> { });
            }
        }

        int[] borderSlots = {9, 17, 18, 26, 27, 35, 36, 44};
        for (int slot : borderSlots) {
            gui.button(slot, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 7)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        for (int i = 45; i < 54; i++) {
            gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 1)
                    .setName(" ")
                    .toItemStack(), (p, inv) -> { });
        }

        if (currentPage > 0) {
            gui.button(45, new ItemBuilder(Material.ARROW)
                    .setName(ChatColor.YELLOW + "◄ Page Précédente")
                    .setLore(Arrays.asList("", ChatColor.GRAY + "Aller à la page " + currentPage, ""))
                    .toItemStack(), (p, inv) -> {
                p.closeInventory();
                new ChampionsGUI(instance, p, currentPage - 1);
                p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.0f);
            });
        }

        gui.button(49, new ItemBuilder(Material.BARRIER)
                .setName(ChatColor.RED + "✖ " + ChatColor.BOLD + "RETOUR")
                .setLore(Arrays.asList("", ChatColor.GRAY + "Retour au menu principal", ""))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsGUI(instance, p);
            p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 0.5f);
        });

        if (currentPage < totalPages - 1) {
            gui.button(53, new ItemBuilder(Material.ARROW)
                    .setName(ChatColor.YELLOW + "Page Suivante ►")
                    .setLore(Arrays.asList("", ChatColor.GRAY + "Aller à la page " + (currentPage + 2), ""))
                    .toItemStack(), (p, inv) -> {
                p.closeInventory();
                new ChampionsGUI(instance, p, currentPage + 1);
                p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.0f);
            });
        }

        gui.button(48, new ItemBuilder(Material.PAPER)
                .setName(ChatColor.YELLOW + "Page " + (currentPage + 1) + "/" + Math.max(1, totalPages))
                .setLore(Arrays.asList("", ChatColor.GRAY + "Champions: " + (startIndex + 1) + "-" + endIndex + " / " + totalCount))
                .toItemStack(), (p, inv) -> { });

        gui.build().open(player);
    }

    private void toggleChampion(Player player, Champion champion, int currentPage) {
        boolean wasEnabled = context.isChampionEnabled(champion);

        if (wasEnabled) {
            context.disableChampion(champion);
            player.sendMessage(ChatColor.RED + "[UHC] Champion désactivé: " + ChatColor.YELLOW + champion.getName());
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
        } else {
            context.enableChampion(champion);
            player.sendMessage(ChatColor.GREEN + "[UHC] Champion activé: " + ChatColor.YELLOW + champion.getName());
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
        }

        player.closeInventory();
        new ChampionsGUI(instance, player, currentPage);
    }
}