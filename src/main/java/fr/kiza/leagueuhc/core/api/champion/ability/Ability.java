package fr.kiza.leagueuhc.core.api.champion.ability;

import fr.kiza.leagueuhc.core.api.champion.Champion;
import fr.kiza.leagueuhc.core.game.GamePlayer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Ability {

    public enum Trigger {
        RIGHT_CLICK,
        LEFT_CLICK,
        SNEAK,
        SNEAK_RIGHT_CLICK,
        SNEAK_LEFT_CLICK,
        BLOCK_PLACE,
        PROJECTILE_LAUNCH,
        PASSIVE,
        MANUAL
    }

    protected Champion owningChampion;

    /**
     * Nom de l'ability (utilisé pour l'identification et l'affichage).
     */
    public abstract String getName();

    /**
     * Description courte de l'ability.
     */
    public abstract String getDescription();

    /**
     * Type de déclencheur pour cette ability.
     */
    public abstract Trigger getTrigger();

    /**
     * Exécute l'ability.
     *
     * @param caster Le joueur qui utilise l'ability
     * @param ctx    Le contexte d'exécution (contient l'event source)
     */
    public abstract void execute(GamePlayer caster, AbilityContext ctx);

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES OPTIONNELLES - À override si nécessaire
    // ═══════════════════════════════════════════════════════════════

    /**
     * Cooldown en secondes. Retourne 0 pour pas de cooldown.
     */
    public int getCooldownSeconds() {
        return 0;
    }

    /**
     * Material de l'item requis pour déclencher l'ability.
     * Retourne null si aucun item n'est requis.
     */
    public Material getItemMaterial() {
        return null;
    }

    /**
     * Si true, l'item doit avoir le nom de l'ability dans son displayName.
     * Ignoré si getItemMaterial() retourne null.
     */
    public boolean requiresNamedItem() {
        return true;
    }

    /**
     * Retourne l'ItemStack de l'ability (pour donner au joueur).
     * Par défaut, crée un item basique avec le material défini.
     */
    public ItemStack createItemStack() {
        if (getItemMaterial() == null) {
            return null;
        }
        return new ItemStack(getItemMaterial());
    }

    /**
     * Appelé chaque tick pour les abilities PASSIVE.
     * Ignoré pour les autres types de trigger.
     */
    public void onTick(GamePlayer owner) {}

    /**
     * Appelé quand l'ability est activée (champion assigné).
     */
    public void onEnable(GamePlayer owner) {}

    /**
     * Appelé quand l'ability est désactivée (champion retiré).
     * Utilisé pour nettoyer les états persistants.
     */
    public void onDisable(GamePlayer owner) {}

    /**
     * Envoie un message de cooldown formaté au joueur.
     */
    protected final void sendCooldownMessage(Player player, long remainingMs) {
        long seconds = (remainingMs + 999) / 1000; // Arrondi supérieur
        player.sendMessage(ChatColor.RED + "⏳ " + getName() + " disponible dans " + seconds + "s");
    }

    /**
     * Envoie un message d'activation au joueur.
     */
    protected final void sendActivationMessage(Player player) {
        player.sendMessage(ChatColor.GREEN + "✔ " + getName() + " activé !");
    }

    /**
     * Envoie un message d'erreur au joueur.
     */
    protected final void sendErrorMessage(Player player, String message) {
        player.sendMessage(ChatColor.RED + "✖ " + message);
    }

    /**
     * Vérifie si le joueur a assez de l'item requis.
     */
    protected final boolean hasRequiredItem(Player player, int amount) {
        if (getItemMaterial() == null) return true;
        return player.getInventory().contains(getItemMaterial(), amount);
    }

    /**
     * Consomme une quantité de l'item requis.
     */
    protected final void consumeItem(Player player, int amount) {
        if (getItemMaterial() == null) return;
        player.getInventory().removeItem(new ItemStack(getItemMaterial(), amount));
    }

    /**
     * Définit le champion propriétaire (appelé par Champion.registerAbility).
     */
    public void setOwningChampion(Champion champion) {
        this.owningChampion = champion;
    }

    /**
     * Récupère le champion propriétaire de cette ability.
     */
    public Champion getOwningChampion() {
        return owningChampion;
    }

    @Override
    public String toString() {
        return "Ability{name='" + getName() + "', trigger=" + getTrigger() + ", cooldown=" + getCooldownSeconds() + "s}";
    }
}