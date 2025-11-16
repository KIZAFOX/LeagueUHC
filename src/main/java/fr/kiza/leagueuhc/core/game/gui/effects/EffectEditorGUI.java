package fr.kiza.leagueuhc.core.game.gui.effects;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.core.game.effect.EffectType;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectEditorGUI {

    private final LeagueUHC instance;
    private final GameContext context;
    private final EffectType effectType;

    public EffectEditorGUI(final LeagueUHC instance, final Player player, final EffectType effectType) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();
        this.effectType = effectType;

        buildGUI(player);
    }

    private void buildGUI(final Player player) {
        int current = getCurrentPercentage();
        int level = calculateLevel(current);

        GuiBuilder gui = new GuiBuilder(instance)
                .title(effectType.getColor() + "⚙ " + effectType.getName())
                .size(54);

        // ========== EN-TÊTE AVEC INFO EFFET ==========
        createHeader(gui, current, level);

        // ========== SLIDER DE SÉLECTION (20-100) ==========
        createSlider(gui, current);

        // ========== AJUSTEMENTS RAPIDES ==========
        createQuickAdjust(gui, current);

        // ========== PRESETS ==========
        createPresets(gui);

        // ========== BOUTON RETOUR ==========
        gui.button(49, new ItemBuilder(Material.ARROW)
                .setName(ChatColor.YELLOW + "← Retour")
                .setLore(Arrays.asList("", ChatColor.GRAY + "Retour à la configuration"))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new EffectsConfigGUI(instance, p);
        });

        // ========== BORDURES ==========
        fillBorders(gui);

        gui.build().open(player);
    }

    private void createHeader(GuiBuilder gui, int current, int level) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + effectType.getDescription());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Puissance actuelle: " + getPercentageBar(current));
        lore.add(ChatColor.AQUA + "Niveau: " + getLevelStars(level) + " " + getLevelName(level));
        lore.add("");
        lore.add(ChatColor.GRAY + "Range: " + ChatColor.YELLOW +
                EffectType.MIN_PERCENTAGE + "% - " + EffectType.MAX_PERCENTAGE + "%");

        gui.button(4, new ItemBuilder(effectType.getDisplayMaterial())
                .setName(effectType.getColoredName())
                .setLore(lore)
                .toItemStack(), (p, inv) -> {});

        // Indicateurs de côté
        ItemBuilder indicator = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) (current >= 50 ? 5 : 14)) // Vert ou rouge
                .setName(effectType.getColor() + String.valueOf(current) + "%");

        gui.button(3, indicator.toItemStack(), (p, inv) -> {});
        gui.button(5, indicator.toItemStack(), (p, inv) -> {});
    }

    private void createSlider(GuiBuilder gui, int current) {
        // Titre du slider
        gui.button(19, new ItemBuilder(Material.SIGN)
                .setName(ChatColor.GOLD + "═══ Sélecteur de Puissance ═══")
                .setLore(Arrays.asList("", ChatColor.GRAY + "Cliquez sur une valeur ci-dessous"))
                .toItemStack(), (p, inv) -> {});

        // Valeurs du slider: 20, 30, 40, 50, 60, 70, 80, 90, 100
        int[] sliderValues = {20, 30, 40, 50, 60, 70, 80, 90, 100};
        int[] sliderSlots = {28, 29, 30, 31, 32, 33, 34, 35, 36};

        for (int i = 0; i < sliderValues.length; i++) {
            final int value = sliderValues[i];
            boolean isSelected = current == value;
            boolean nearSelected = Math.abs(current - value) <= 5;

            Material mat;
            short durability = 0;

            if (isSelected) {
                mat = Material.EMERALD_BLOCK;
            } else if (nearSelected) {
                mat = Material.STAINED_GLASS_PANE;
                durability = 5; // Vert lime
            } else {
                mat = Material.STAINED_GLASS_PANE;
                durability = value <= 40 ? 14 : (short) (value <= 70 ? 4 : 5); // Rouge, jaune, vert
            }

            List<String> lore = new ArrayList<>();
            lore.add("");
            if (isSelected) {
                lore.add(ChatColor.GREEN + "✓ Valeur actuelle");
            } else {
                lore.add(ChatColor.YELLOW + "▸ Cliquer pour définir");
            }
            lore.add("");
            lore.add(ChatColor.GRAY + "Niveau: " + getLevelName(calculateLevel(value)));

            ItemBuilder item = new ItemBuilder(mat)
                    .setName((isSelected ? ChatColor.GREEN + "● " : effectType.getColor() + "") + value + "%")
                    .setLore(lore);

            if (mat == Material.STAINED_GLASS_PANE) {
                item.setDurability(durability);
            }

            gui.button(sliderSlots[i], item.toItemStack(), (p, inv) -> setPercentage(p, value));
        }
    }

    private void createQuickAdjust(GuiBuilder gui, int current) {
        // Titre
        gui.button(10, new ItemBuilder(Material.SIGN)
                .setName(ChatColor.AQUA + "Ajustements Rapides")
                .toItemStack(), (p, inv) -> {});

        // -10
        gui.button(11, createAdjustButton(Material.REDSTONE, "-10%", current, -10).toItemStack(),
                (p, inv) -> adjustPercentage(p, -10));

        // -5
        gui.button(12, createAdjustButton(Material.CLAY_BALL, "-5%", current, -5).toItemStack(),
                (p, inv) -> adjustPercentage(p, -5));

        // +5
        gui.button(14, createAdjustButton(Material.SLIME_BALL, "+5%", current, 5).toItemStack(),
                (p, inv) -> adjustPercentage(p, 5));

        // +10
        gui.button(15, createAdjustButton(Material.EMERALD, "+10%", current, 10).toItemStack(),
                (p, inv) -> adjustPercentage(p, 10));
    }

    private ItemBuilder createAdjustButton(Material mat, String name, int current, int delta) {
        int newValue = current + delta;
        boolean canApply = EffectType.isValidPercentage(newValue);

        ChatColor color = delta > 0 ? ChatColor.GREEN : ChatColor.RED;

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Valeur actuelle: " + ChatColor.YELLOW + current + "%");
        lore.add(ChatColor.GRAY + "Nouvelle valeur: " + ChatColor.YELLOW + newValue + "%");
        lore.add("");
        if (canApply) {
            lore.add(ChatColor.GREEN + "✓ Cliquer pour appliquer");
        } else {
            lore.add(ChatColor.RED + "✗ Hors limites");
        }

        return new ItemBuilder(mat)
                .setName(color + name)
                .setLore(lore);
    }

    private void createPresets(GuiBuilder gui) {
        // Titre
        gui.button(37, new ItemBuilder(Material.SIGN)
                .setName(ChatColor.LIGHT_PURPLE + "Valeurs Prédéfinies")
                .toItemStack(), (p, inv) -> {});

        // Minimum (20%)
        gui.button(38, new ItemBuilder(Material.COAL_BLOCK)
                .setName(ChatColor.GRAY + "Minimum (20%)")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Valeur minimale",
                        ChatColor.DARK_GRAY + "• Niveau: Faible",
                        "",
                        ChatColor.YELLOW + "▸ Cliquer pour définir"
                ))
                .toItemStack(), (p, inv) -> setPercentage(p, 20));

        // Par défaut (20%)
        gui.button(39, new ItemBuilder(Material.IRON_BLOCK)
                .setName(ChatColor.WHITE + "Défaut (20%)")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Valeur par défaut",
                        ChatColor.DARK_GRAY + "• Niveau: Faible",
                        "",
                        ChatColor.YELLOW + "▸ Cliquer pour définir"
                ))
                .toItemStack(), (p, inv) -> setPercentage(p, EffectType.DEFAULT_PERCENTAGE));

        // Équilibré (60%)
        gui.button(41, new ItemBuilder(Material.GOLD_BLOCK)
                .setName(ChatColor.YELLOW + "Équilibré (60%)")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Valeur équilibrée",
                        ChatColor.DARK_GRAY + "• Niveau: Moyen",
                        "",
                        ChatColor.YELLOW + "▸ Cliquer pour définir"
                ))
                .toItemStack(), (p, inv) -> setPercentage(p, 60));

        // Maximum (100%)
        gui.button(42, new ItemBuilder(Material.EMERALD_BLOCK)
                .setName(ChatColor.GREEN + "Maximum (100%)")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Valeur maximale",
                        ChatColor.DARK_GRAY + "• Niveau: Maximum",
                        "",
                        ChatColor.YELLOW + "▸ Cliquer pour définir"
                ))
                .toItemStack(), (p, inv) -> setPercentage(p, 100));
    }

    private void fillBorders(GuiBuilder gui) {
        ItemBuilder borderItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) 15)
                .setName(" ");

        int[] borders = {0, 1, 2, 6, 7, 8, 9, 17, 18, 26, 27, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : borders) {
            gui.button(slot, borderItem.toItemStack(), (p, inv) -> {});
        }
    }

    // ========== ACTIONS ==========

    private void adjustPercentage(Player player, int delta) {
        int current = getCurrentPercentage();
        int newValue = current + delta;

        if (!EffectType.isValidPercentage(newValue)) {
            player.sendMessage(ChatColor.RED + "✗ [UHC] Limite atteinte! (" +
                    EffectType.MIN_PERCENTAGE + "-" + EffectType.MAX_PERCENTAGE + "%)");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        setPercentage(player, newValue);
    }

    private void setPercentage(Player player, int value) {
        value = EffectType.clampPercentage(value);
        context.setEffectPercentage(effectType.getPotionType(), value);

        player.sendMessage(ChatColor.GREEN + "✓ [UHC] " + effectType.getName() +
                " défini à " + ChatColor.YELLOW + value + "%");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);

        // Refresh les effets
        if (instance.getGameEngine().getEffectsApplier() != null) {
            instance.getGameEngine().getEffectsApplier().refreshEffects();
        }

        // Recharger le GUI
        player.closeInventory();
        new EffectEditorGUI(instance, player, effectType);
    }

    private int getCurrentPercentage() {
        return context.getEffectPercentage(effectType.getPotionType());
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private String getPercentageBar(int percentage) {
        int bars = (percentage - EffectType.MIN_PERCENTAGE) / 20;
        StringBuilder bar = new StringBuilder();

        bar.append(ChatColor.BOLD).append(percentage).append("% ");
        bar.append(ChatColor.WHITE).append("[");

        for (int i = 0; i < 4; i++) {
            if (i < bars) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        bar.append(ChatColor.WHITE).append("]");
        return bar.toString();
    }

    private String getLevelStars(int level) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < level) {
                stars.append(ChatColor.GOLD).append("★");
            } else {
                stars.append(ChatColor.DARK_GRAY).append("★");
            }
        }
        return stars.toString();
    }

    private String getLevelName(int level) {
        switch (level) {
            case 0: return ChatColor.GRAY + "Faible";
            case 1: return ChatColor.WHITE + "Normal";
            case 2: return ChatColor.YELLOW + "Moyen";
            case 3: return ChatColor.GOLD + "Élevé";
            case 4: return ChatColor.RED + "Maximum";
            default: return ChatColor.GRAY + "Inconnu";
        }
    }

    private int calculateLevel(int percentage) {
        if (percentage <= 20) return 0;
        if (percentage <= 40) return 1;
        if (percentage <= 60) return 2;
        if (percentage <= 80) return 3;
        return 4;
    }
}