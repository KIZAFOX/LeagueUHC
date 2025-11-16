package fr.kiza.leagueuhc.core.game.gui.effects;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.core.game.effect.EffectType;
import fr.kiza.leagueuhc.core.game.gui.settings.SettingsGUI;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectsConfigGUI {

    private final LeagueUHC instance;
    private final GameContext context;

    public EffectsConfigGUI(final LeagueUHC instance, final Player player) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();

        buildGUI(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
    }

    private void buildGUI(final Player player) {
        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.DARK_PURPLE + "⚡ " + ChatColor.BOLD + "Configuration des Effets")
                .size(54);

        // ========== EN-TÊTE DÉCORATIF ==========
        createHeader(gui);

        // ========== CARTES D'EFFETS ==========
        createEffectCard(gui, EffectType.FORCE, 20);
        createEffectCard(gui, EffectType.RESISTANCE, 22);
        createEffectCard(gui, EffectType.SPEED, 24);

        // ========== INFORMATIONS GLOBALES ==========
        createInfoPanel(gui);

        // ========== BOUTONS D'ACTION ==========
        gui.button(48, new ItemBuilder(Material.NETHER_STAR)
                .setName(ChatColor.GOLD + "⟳ Réinitialiser tout")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Remet tous les effets à",
                        ChatColor.GRAY + "leur valeur par défaut",
                        ChatColor.YELLOW + "(" + EffectType.DEFAULT_PERCENTAGE + "%)",
                        "",
                        ChatColor.GREEN + "▸ Cliquer pour réinitialiser"
                ))
                .toItemStack(), (p, inv) -> resetAllEffects(p));

        gui.button(50, new ItemBuilder(Material.ARROW)
                .setName(ChatColor.YELLOW + "← Retour")
                .setLore(Arrays.asList("", ChatColor.GRAY + "Retour au menu principal"))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsGUI(instance, p);
        });

        // ========== BORDURES DÉCORATIVES ==========
        fillBorders(gui);

        gui.build().open(player);
    }

    private void createHeader(GuiBuilder gui) {
        ItemBuilder headerItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) 5) // Vert lime
                .setName(ChatColor.GREEN + "═══════════════");

        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}) {
            gui.button(i, headerItem.toItemStack(), (p, inv) -> {});
        }
    }

    private void createEffectCard(GuiBuilder gui, EffectType effectType, int slot) {
        int percentage = context.getEffectPercentage(effectType.getPotionType());
        int level = calculateLevel(percentage);
        boolean isActive = percentage >= EffectType.MIN_PERCENTAGE;

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "┌─────────────────");
        lore.add(ChatColor.GRAY + "│ " + ChatColor.WHITE + effectType.getDescription());
        lore.add(ChatColor.GRAY + "│");
        lore.add(ChatColor.GRAY + "│ " + ChatColor.YELLOW + "Puissance: " + getPercentageBar(percentage));
        lore.add(ChatColor.GRAY + "│ " + ChatColor.AQUA + "Niveau: " + getLevelStars(level));
        lore.add(ChatColor.GRAY + "│");
        lore.add(ChatColor.GRAY + "│ " + (isActive ?
                ChatColor.GREEN + "✓ Actif" :
                ChatColor.RED + "✗ Désactivé"));
        lore.add(ChatColor.GRAY + "└─────────────────");
        lore.add("");
        lore.add(ChatColor.YELLOW + "▸ Cliquer pour modifier");

        gui.button(slot, new ItemBuilder(effectType.getDisplayMaterial())
                .setName(effectType.getColoredName())
                .setLore(lore)
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new EffectEditorGUI(instance, p, effectType);
        });

        // Indicateur visuel sous la carte
        Material indicatorMaterial = isActive ? Material.EMERALD : Material.REDSTONE;
        gui.button(slot + 9, new ItemBuilder(indicatorMaterial)
                .setName(effectType.getColor() + String.valueOf(percentage) + "%")
                .setLore(Arrays.asList("", ChatColor.GRAY + "Niveau actuel: " + getLevelName(level)))
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new EffectEditorGUI(instance, p, effectType);
        });
    }

    private void createInfoPanel(GuiBuilder gui) {
        int totalEffects = EffectType.values().length;
        int activeEffects = 0;

        for (EffectType type : EffectType.values()) {
            int percentage = context.getEffectPercentage(type.getPotionType());
            if (percentage >= EffectType.MIN_PERCENTAGE) {
                activeEffects++;
            }
        }

        gui.button(40, new ItemBuilder(Material.BOOK)
                .setName(ChatColor.LIGHT_PURPLE + "ℹ Informations")
                .setLore(Arrays.asList(
                        "",
                        ChatColor.GRAY + "Effets actifs: " + ChatColor.GREEN + activeEffects +
                                ChatColor.GRAY + "/" + totalEffects,
                        "",
                        ChatColor.GRAY + "Range de puissance:",
                        ChatColor.YELLOW + "  • Minimum: " + EffectType.MIN_PERCENTAGE + "%",
                        ChatColor.YELLOW + "  • Maximum: " + EffectType.MAX_PERCENTAGE + "%",
                        "",
                        ChatColor.GRAY + "Les effets sont appliqués",
                        ChatColor.GRAY + "automatiquement à tous",
                        ChatColor.GRAY + "les joueurs en jeu."
                ))
                .toItemStack(), (p, inv) -> {});
    }

    private void fillBorders(GuiBuilder gui) {
        ItemBuilder borderItem = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setDurability((short) 15) // Noir
                .setName(" ");

        int[] borderSlots = {9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 51, 52, 53};
        for (int slot : borderSlots) {
            gui.button(slot, borderItem.toItemStack(), (p, inv) -> {});
        }
    }

    private void resetAllEffects(Player player) {
        for (EffectType type : EffectType.values()) {
            context.setEffectPercentage(type.getPotionType(), EffectType.DEFAULT_PERCENTAGE);
        }

        player.sendMessage(ChatColor.GREEN + "✓ [UHC] Tous les effets ont été réinitialisés!");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);

        player.closeInventory();
        new EffectsConfigGUI(instance, player);
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private String getPercentageBar(int percentage) {
        int bars = (percentage - EffectType.MIN_PERCENTAGE) / 20; // 0 à 4 barres
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