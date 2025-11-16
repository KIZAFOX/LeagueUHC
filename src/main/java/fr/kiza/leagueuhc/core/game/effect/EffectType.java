package fr.kiza.leagueuhc.core.game.effect;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public enum EffectType {
    FORCE(
            "Force",
            "Augmente les dégâts infligés en combat",
            PotionEffectType.INCREASE_DAMAGE,
            Material.BLAZE_POWDER,
            ChatColor.RED,
            "⚔"
    ),
    RESISTANCE(
            "Résistance",
            "Réduit les dégâts reçus en combat",
            PotionEffectType.DAMAGE_RESISTANCE,
            Material.IRON_CHESTPLATE,
            ChatColor.BLUE,
            "🛡"
    ),
    SPEED(
            "Vitesse",
            "Augmente la vitesse de déplacement",
            PotionEffectType.SPEED,
            Material.SUGAR,
            ChatColor.AQUA,
            "⚡"
    );

    private final String name;
    private final String description;
    private final PotionEffectType potionType;
    private final Material displayMaterial;
    private final ChatColor color;
    private final String icon;

    public static final int MIN_PERCENTAGE = 20;
    public static final int MAX_PERCENTAGE = 100;
    public static final int DEFAULT_PERCENTAGE = 20;

    EffectType(String name, String description, PotionEffectType potionType,
               Material displayMaterial, ChatColor color, String icon) {
        this.name = name;
        this.description = description;
        this.potionType = potionType;
        this.displayMaterial = displayMaterial;
        this.color = color;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public PotionEffectType getPotionType() {
        return potionType;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public String getColoredName() {
        return color + icon + " " + name;
    }

    /**
     * Valide un pourcentage
     */
    public static boolean isValidPercentage(int percentage) {
        return percentage >= MIN_PERCENTAGE && percentage <= MAX_PERCENTAGE;
    }

    /**
     * Clamp un pourcentage dans les limites
     */
    public static int clampPercentage(int percentage) {
        return Math.max(MIN_PERCENTAGE, Math.min(MAX_PERCENTAGE, percentage));
    }

    /**
     * Récupère le type d'effet depuis le PotionEffectType
     */
    public static EffectType fromPotionType(PotionEffectType potionType) {
        for (EffectType type : values()) {
            if (type.getPotionType().equals(potionType)) {
                return type;
            }
        }
        return null;
    }
}