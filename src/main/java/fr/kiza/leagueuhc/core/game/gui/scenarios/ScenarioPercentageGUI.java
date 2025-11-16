package fr.kiza.leagueuhc.core.game.gui.scenarios;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.scenario.Scenario;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class ScenarioPercentageGUI {

    private final LeagueUHC instance;
    private final GameContext context;
    private final Scenario scenario;

    public ScenarioPercentageGUI(final LeagueUHC instance, final Player player, final Scenario scenario) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();
        this.scenario = scenario;

        buildGUI(player);
    }

    private void buildGUI(final Player player) {
        boolean isActive = context.isScenarioActive(scenario);
        int currentPercentage = context.getScenarioPercentage(scenario);

        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.LIGHT_PURPLE + scenario.getName())
                .size(45);

        ItemBuilder statusBuilder = new ItemBuilder(Material.PAPER)
                .setName(ChatColor.YELLOW + "Statut & Pourcentage")
                .setLore(Arrays.asList(
                        ChatColor.GRAY + "Statut: " + (isActive ? ChatColor.GREEN + "✓ Activé" : ChatColor.RED + "✗ Désactivé"),
                        ChatColor.GRAY + "Pourcentage: " + ChatColor.YELLOW + currentPercentage + "%",
                        "",
                        ChatColor.DARK_GRAY + scenario.getDescription(),
                        "",
                        ChatColor.GRAY + "Range: 0-500%"
                ));

        if (isActive) {
            statusBuilder.addEnchant(Enchantment.DURABILITY, 1);
            statusBuilder.hideEnchant();
        }

        gui.button(4, statusBuilder.toItemStack(), (p, inv) -> { });

        // Bouton Toggle Activation
        gui.button(22, new ItemBuilder(isActive ? Material.REDSTONE : Material.EMERALD)
                .setName(isActive ? ChatColor.RED + "Désactiver" : ChatColor.GREEN + "Activer")
                .setLore(Collections.singletonList(
                        isActive ? ChatColor.YELLOW + "Cliquer pour désactiver" : ChatColor.YELLOW + "Cliquer pour activer"
                ))
                .toItemStack(), (p, inv) -> {
            if (isActive) {
                context.removeScenario(scenario);
                p.sendMessage(ChatColor.RED + "[UHC] Scénario désactivé: " + ChatColor.YELLOW + scenario.getName());
                p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            } else {
                context.addScenario(scenario);
                p.sendMessage(ChatColor.GREEN + "[UHC] Scénario activé: " + ChatColor.YELLOW + scenario.getName());
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
            }
            p.closeInventory();
            new ScenarioPercentageGUI(instance, p, scenario);
        });

        // ========== BOUTONS D'AJOUT ==========
        gui.button(28, createButton(Material.EMERALD_BLOCK, ChatColor.GREEN + "+100%", "+100", currentPercentage, 100, 500).toItemStack(),
                (p, inv) -> modifyPercentage(p, 100));

        gui.button(29, createButton(Material.EMERALD, ChatColor.GREEN + "+50%", "+50", currentPercentage, 50, 500).toItemStack(),
                (p, inv) -> modifyPercentage(p, 50));

        gui.button(30, createButton(Material.SLIME_BALL, ChatColor.GREEN + "+10%", "+10", currentPercentage, 10, 500).toItemStack(),
                (p, inv) -> modifyPercentage(p, 10));

        gui.button(32, createButton(Material.REDSTONE, ChatColor.RED + "-10%", "-10", currentPercentage, -10, 0).toItemStack(),
                (p, inv) -> modifyPercentage(p, -10));

        gui.button(33, createButton(Material.REDSTONE_BLOCK, ChatColor.RED + "-50%", "-50", currentPercentage, -50, 0).toItemStack(),
                (p, inv) -> modifyPercentage(p, -50));

        gui.button(34, createButton(Material.COAL_BLOCK, ChatColor.RED + "-100%", "-100", currentPercentage, -100, 0).toItemStack(),
                (p, inv) -> modifyPercentage(p, -100));

        // Valeurs prédéfinies
        gui.button(19, new ItemBuilder(Material.IRON_BLOCK)
                .setName(ChatColor.GRAY + "0%")
                .setLore(Collections.singletonList(ChatColor.YELLOW + "Définir à 0%"))
                .toItemStack(), (p, inv) -> setPercentageTo(p, 0));

        gui.button(20, new ItemBuilder(Material.GOLD_BLOCK)
                .setName(ChatColor.YELLOW + "100%")
                .setLore(Collections.singletonList(ChatColor.YELLOW + "Définir à 100%"))
                .toItemStack(), (p, inv) -> setPercentageTo(p, 100));

        gui.button(24, new ItemBuilder(Material.DIAMOND_BLOCK)
                .setName(ChatColor.AQUA + "200%")
                .setLore(Collections.singletonList(ChatColor.YELLOW + "Définir à 200%"))
                .toItemStack(), (p, inv) -> setPercentageTo(p, 200));

        gui.button(25, new ItemBuilder(Material.EMERALD_BLOCK)
                .setName(ChatColor.GREEN + "500%")
                .setLore(Collections.singletonList(ChatColor.YELLOW + "Définir à 500%"))
                .toItemStack(), (p, inv) -> setPercentageTo(p, 500));

        // Bouton Retour
        gui.button(40, new ItemBuilder(Material.ARROW)
                .setName(ChatColor.YELLOW + "← Retour")
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new ScenariosGUI(instance, p);
        });

        // Remplissage
        for (int i = 0; i < 45; i++) {
            if (gui.build().getInventory().getItem(i) == null) {
                gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 15)
                        .setName(" ")
                        .toItemStack(), (p, inv) -> { });
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
                        ChatColor.GRAY + "Changer de: " + ChatColor.YELLOW + change + "%",
                        ChatColor.GRAY + "Nouvelle valeur: " + ChatColor.YELLOW + newValue + "%",
                        "",
                        canApply ? ChatColor.GREEN + "✓ Cliquer pour appliquer" : ChatColor.RED + "✗ Limite atteinte"
                ));
    }

    private void modifyPercentage(Player player, int delta) {
        int current = context.getScenarioPercentage(scenario);
        int newValue = current + delta;

        if (newValue < 0 || newValue > 500) {
            player.sendMessage(ChatColor.RED + "[UHC] Valeur hors limites ! (0-500%)");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        context.setScenarioPercentage(scenario, newValue);
        player.sendMessage(ChatColor.GREEN + "[UHC] Pourcentage modifié: " + ChatColor.YELLOW + newValue + "%");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);

        player.closeInventory();
        new ScenarioPercentageGUI(instance, player, scenario);
    }

    private void setPercentageTo(Player player, int value) {
        context.setScenarioPercentage(scenario, value);
        player.sendMessage(ChatColor.GREEN + "[UHC] Pourcentage défini à: " + ChatColor.YELLOW + value + "%");
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 2.0f);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "[UHC] " + player.getName() +
                " a défini " + scenario.getName() + " à " + value + "%");

        player.closeInventory();
        new ScenarioPercentageGUI(instance, player, scenario);
    }
}