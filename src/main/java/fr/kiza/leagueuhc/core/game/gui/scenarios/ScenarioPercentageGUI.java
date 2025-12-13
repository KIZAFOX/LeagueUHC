package fr.kiza.leagueuhc.core.game.gui.scenarios;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.scenario.Scenario;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioManager;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ScenarioPercentageGUI {

    private final LeagueUHC instance;
    private final ScenarioManager scenarioManager;
    private final Scenario scenario;

    public ScenarioPercentageGUI(LeagueUHC instance, Player player, Scenario scenario) {
        this.instance = instance;
        this.scenarioManager = instance.getGameEngine().getGameHelper().getManager().getScenarioManager();
        this.scenario = scenario;

        buildGUI(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
    }

    private void buildGUI(Player player) {
        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.GOLD + scenario.getName() + " - Pourcentage")
                .size(45);

        int currentPercentage = scenarioManager.getPercentage(scenario.getId());
        boolean isActive = scenarioManager.isActive(scenario.getId());

        // Info centrale
        gui.button(13, new ItemBuilder(scenario.getIcon())
                .setName(ChatColor.YELLOW + scenario.getName())
                .setLore(Arrays.asList(
                        ChatColor.GRAY + scenario.getDescription(),
                        "",
                        ChatColor.WHITE + "Pourcentage actuel: " + ChatColor.GOLD + currentPercentage + "%",
                        "",
                        isActive ? ChatColor.GREEN + "✓ Activé" : ChatColor.RED + "✗ Désactivé"
                ))
                .toItemStack(), (p, inv) -> {});

        // Boutons de diminution
        gui.button(19, createPercentageButton(-50, currentPercentage), (p, inv) -> {
            adjustPercentage(p, -50);
        });
        gui.button(20, createPercentageButton(-10, currentPercentage), (p, inv) -> {
            adjustPercentage(p, -10);
        });
        gui.button(21, createPercentageButton(-1, currentPercentage), (p, inv) -> {
            adjustPercentage(p, -1);
        });

        // Boutons d'augmentation
        gui.button(23, createPercentageButton(+1, currentPercentage), (p, inv) -> {
            adjustPercentage(p, +1);
        });
        gui.button(24, createPercentageButton(+10, currentPercentage), (p, inv) -> {
            adjustPercentage(p, +10);
        });
        gui.button(25, createPercentageButton(+50, currentPercentage), (p, inv) -> {
            adjustPercentage(p, +50);
        });

        // Toggle activation
        gui.button(31, new ItemBuilder(isActive ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK)
                .setName(isActive ? ChatColor.RED + "Désactiver" : ChatColor.GREEN + "Activer")
                .setLore(Arrays.asList(
                        ChatColor.GRAY + "Cliquez pour " + (isActive ? "désactiver" : "activer"),
                        ChatColor.GRAY + "ce scénario"
                ))
                .toItemStack(), (p, inv) -> {
            scenarioManager.toggle(scenario.getId());
            p.closeInventory();
            new ScenarioPercentageGUI(instance, p, scenario);

            if (scenarioManager.isActive(scenario.getId())) {
                p.sendMessage(ChatColor.GREEN + "[UHC] Scénario activé: " + ChatColor.YELLOW + scenario.getName());
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
            } else {
                p.sendMessage(ChatColor.RED + "[UHC] Scénario désactivé: " + ChatColor.YELLOW + scenario.getName());
                p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            }
        });

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
                        .toItemStack(), (p, inv) -> {});
            }
        }

        gui.build().open(player);
    }

    private org.bukkit.inventory.ItemStack createPercentageButton(int delta, int current) {
        boolean isIncrease = delta > 0;
        Material mat = isIncrease ? Material.STAINED_GLASS_PANE : Material.STAINED_GLASS_PANE;
        short durability = isIncrease ? (short) 5 : (short) 14; // Lime : Red

        String prefix = isIncrease ? "+" : "";
        int newValue = Math.max(0, Math.min(100, current + delta));

        return new ItemBuilder(mat)
                .setDurability(durability)
                .setName((isIncrease ? ChatColor.GREEN : ChatColor.RED) + prefix + delta + "%")
                .setLore(Arrays.asList(
                        ChatColor.GRAY + "Nouveau: " + ChatColor.WHITE + newValue + "%"
                ))
                .toItemStack();
    }

    private void adjustPercentage(Player player, int delta) {
        int current = scenarioManager.getPercentage(scenario.getId());
        int newValue = Math.max(0, Math.min(100, current + delta));

        scenarioManager.setPercentage(scenario.getId(), newValue);
        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, delta > 0 ? 1.2f : 0.8f);

        player.closeInventory();
        new ScenarioPercentageGUI(instance, player, scenario);
    }
}