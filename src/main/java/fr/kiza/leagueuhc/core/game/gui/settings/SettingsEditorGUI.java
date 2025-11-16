package fr.kiza.leagueuhc.core.game.gui.settings;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class SettingsEditorGUI {

    private final LeagueUHC instance;
    private final GameContext context;
    private final SettingsType type;

    public SettingsEditorGUI(final LeagueUHC instance, final Player player, final SettingsType type) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();
        this.type = type;

        buildGUI(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
    }

    private void buildGUI(final Player player) {
        String title = "";
        int currentValue = 0;
        int minValue = 0;
        int maxValue = 0;

        switch (type) {
            case MAX_PLAYERS:
                title = ChatColor.RED + "Joueurs Maximum";
                currentValue = context.getMaxPlayers();
                minValue = 2;
                maxValue = 32;
                break;
            case COUNTDOWN:
                title = ChatColor.GOLD + "Countdown";
                currentValue = context.getCountdown();
                minValue = 5;
                maxValue = 60;
                break;
        }

        GuiBuilder gui = new GuiBuilder(instance)
                .title(title)
                .size(45);

        gui.button(4, new ItemBuilder(Material.PAPER)
                        .setName(ChatColor.YELLOW + "Valeur actuelle")
                        .setLore(Arrays.asList(
                                ChatColor.GRAY + "Valeur: " + ChatColor.GREEN + currentValue,
                                "",
                                ChatColor.GRAY + "Min: " + minValue + " | Max: " + maxValue
                        ))
                        .toItemStack(),
                (p, inv) -> { });

        // ========== BOUTONS D'AJOUT ==========
        int finalCurrentValue = currentValue;
        int finalMaxValue = maxValue;
        int finalMinValue = minValue;

        gui.button(10, createButton(Material.EMERALD_BLOCK, ChatColor.GREEN + "+10", "+10", finalCurrentValue, 10, finalMaxValue).toItemStack(),
                (p, inv) -> modifyValue(p, 10));

        gui.button(11, createButton(Material.EMERALD, ChatColor.GREEN + "+5", "+5", finalCurrentValue, 5, finalMaxValue).toItemStack(),
                (p, inv) -> modifyValue(p, 5));

        gui.button(12, createButton(Material.SLIME_BALL, ChatColor.GREEN + "+1", "+1", finalCurrentValue, 1, finalMaxValue).toItemStack(),
                (p, inv) -> modifyValue(p, 1));

        gui.button(14, createButton(Material.REDSTONE, ChatColor.RED + "-1", "-1", finalCurrentValue, -1, finalMinValue).toItemStack(),
                (p, inv) -> modifyValue(p, -1));

        gui.button(15, createButton(Material.REDSTONE_BLOCK, ChatColor.RED + "-5", "-5", finalCurrentValue, -5, finalMinValue).toItemStack(),
                (p, inv) -> modifyValue(p, -5));

        gui.button(16, createButton(Material.COAL_BLOCK, ChatColor.RED + "-10", "-10", finalCurrentValue, -10, finalMinValue).toItemStack(),
                (p, inv) -> modifyValue(p, -10));

        gui.button(29, new ItemBuilder(Material.IRON_BLOCK)
                        .setName(ChatColor.GRAY + "Valeur minimum")
                        .setLore(Collections.singletonList(
                                ChatColor.YELLOW + "Définir à: " + ChatColor.WHITE + finalMinValue
                        ))
                        .toItemStack(),
                (p, inv) -> setValueTo(p, finalMinValue));

        gui.button(33, new ItemBuilder(Material.GOLD_BLOCK)
                        .setName(ChatColor.GRAY + "Valeur maximum")
                        .setLore(Collections.singletonList(
                                ChatColor.YELLOW + "Définir à: " + ChatColor.WHITE + finalMaxValue
                        ))
                        .toItemStack(),
                (p, inv) -> setValueTo(p, finalMaxValue));

        gui.button(40, new ItemBuilder(Material.ARROW)
                        .setName(ChatColor.YELLOW + "← Retour")
                        .toItemStack(),
                (p, inv) -> {
                    p.closeInventory();
                    new SettingsGUI(instance, p);
                });

        for (int i = 0; i < 45; i++) {
            if (gui.build().getInventory().getItem(i) == null) {
                gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                                .setDurability((short) 15)
                                .setName(" ")
                                .toItemStack(),
                        (p, inv) -> { });
            }
        }

        gui.build().open(player);
    }

    private ItemBuilder createButton(Material material, String name, String change, int current, int delta, int limit) {
        int newValue = current + delta;
        boolean canApply = (delta > 0 && newValue <= limit) || (delta < 0 && newValue >= limit);

        return new ItemBuilder(material)
                .setName(name)
                .setLore(Arrays.asList(
                        ChatColor.GRAY + "Changer de: " + ChatColor.YELLOW + change,
                        ChatColor.GRAY + "Nouvelle valeur: " + ChatColor.YELLOW + newValue,
                        "",
                        canApply ? ChatColor.GREEN + "✓ Cliquer pour appliquer" : ChatColor.RED + "✗ Limite atteinte"
                ));
    }

    private void modifyValue(Player player, int delta) {
        int current = getCurrentValue();
        int newValue = current + delta;
        int min = getMinLimit();
        int max = getMaxLimit();

        if (newValue < min || newValue > max) {
            player.sendMessage(ChatColor.RED + "[UHC] Valeur hors limites ! (" + min + "-" + max + ")");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        setCurrentValue(newValue);
        player.sendMessage(ChatColor.GREEN + "[UHC] Valeur modifiée: " + ChatColor.YELLOW + newValue);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);

        player.closeInventory();

        new SettingsEditorGUI(instance, player, type);
    }

    private void setValueTo(Player player, int value) {
        setCurrentValue(value);
        player.sendMessage(ChatColor.GREEN + "[UHC] Valeur définie à: " + ChatColor.YELLOW + value);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        String settingName = getSettingName();
        Bukkit.broadcastMessage(ChatColor.YELLOW + "[UHC] " + player.getName() + " a défini " + settingName + " à " + value);

        player.closeInventory();
        new SettingsEditorGUI(instance, player, type);
    }

    private int getCurrentValue() {
        switch (type) {
            case MAX_PLAYERS: return context.getMaxPlayers();
            case COUNTDOWN: return context.getCountdown();
        }
        return 0;
    }

    private void setCurrentValue(int value) {
        switch (type) {
            case MAX_PLAYERS: context.setMaxPlayers(value); break;
            case COUNTDOWN: context.setCountdown(value); break;
        }
    }

    private int getMinLimit() {
        switch (type) {
            case MAX_PLAYERS: return 2;
            case COUNTDOWN: return 5;
        }
        return 0;
    }

    private int getMaxLimit() {
        switch (type) {
            case MAX_PLAYERS: return 32;
            case COUNTDOWN: return 60;
        }
        return 0;
    }

    private String getSettingName() {
        switch (type) {
            case MAX_PLAYERS: return "le maximum de joueurs";
            case COUNTDOWN: return "le countdown";
        }
        return "";
    }
}