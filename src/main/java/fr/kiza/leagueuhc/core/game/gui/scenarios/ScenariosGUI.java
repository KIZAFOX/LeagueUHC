package fr.kiza.leagueuhc.core.game.gui.scenarios;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.scenario.Scenario;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioManager;
import fr.kiza.leagueuhc.core.game.gui.settings.SettingsGUI;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScenariosGUI {

    private final LeagueUHC instance;
    private final ScenarioManager scenarioManager;

    public ScenariosGUI(LeagueUHC instance, Player player) {
        this.instance = instance;
        this.scenarioManager = instance.getGameEngine().getGameHelper().getManager().getScenarioManager();

        buildGUI(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
    }

    private void buildGUI(Player player) {
        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.LIGHT_PURPLE + "Scénarios UHC")
                .size(54);

        int slot = 10;

        for (Scenario scenario : scenarioManager.getAll()) {
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;

            boolean isActive = scenarioManager.isActive(scenario.getId());
            int percentage = scenarioManager.getPercentage(scenario.getId());

            Material material = scenario.getIcon();
            ItemBuilder builder = new ItemBuilder(material)
                    .setName((isActive ? ChatColor.GREEN : ChatColor.GRAY) + scenario.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + scenario.getDescription());
            lore.add("");

            if (scenario.hasPercentage()) {
                lore.add(ChatColor.GRAY + "Pourcentage: " + ChatColor.YELLOW + percentage + "%");
                lore.add("");
                lore.add(ChatColor.YELLOW + "▸ Clic gauche: " + ChatColor.WHITE + "Activer/Désactiver");
                lore.add(ChatColor.YELLOW + "▸ Clic droit: " + ChatColor.WHITE + "Modifier %");
            } else {
                lore.add(ChatColor.YELLOW + "▸ Cliquer pour " + (isActive ? "désactiver" : "activer"));
            }

            lore.add("");
            lore.add(isActive ? ChatColor.GREEN + "✓ Activé" : ChatColor.RED + "✗ Désactivé");

            builder.setLore(lore);

            if (isActive) {
                builder.addEnchant(Enchantment.DURABILITY, 1);
                builder.hideEnchant();
            }

            String scenarioId = scenario.getId();
            gui.button(slot, builder.toItemStack(), (p, inv) -> {
                handleScenarioClick(p, scenarioId, scenario.hasPercentage());
            });

            slot++;
        }

        // Bouton Retour
        gui.button(49, new ItemBuilder(Material.ARROW)
                .setName(ChatColor.YELLOW + "← Retour")
                .toItemStack(), (p, inv) -> {
            p.closeInventory();
            new SettingsGUI(instance, p);
        });

        // Remplissage
        for (int i = 0; i < 54; i++) {
            if (gui.build().getInventory().getItem(i) == null) {
                gui.button(i, new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) 15)
                        .setName(" ")
                        .toItemStack(), (p, inv) -> {});
            }
        }

        gui.build().open(player);
    }

    private void handleScenarioClick(Player player, String scenarioId, boolean hasPercentage) {
        Scenario scenario = scenarioManager.get(scenarioId);
        if (scenario == null) return;

        boolean wasActive = scenarioManager.isActive(scenarioId);

        if (hasPercentage) {
            player.closeInventory();
            new ScenarioPercentageGUI(instance, player, scenario);
        } else {
            scenarioManager.toggle(scenarioId);

            if (wasActive) {
                player.sendMessage(ChatColor.RED + "[UHC] Scénario désactivé: " + ChatColor.YELLOW + scenario.getName());
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            } else {
                player.sendMessage(ChatColor.GREEN + "[UHC] Scénario activé: " + ChatColor.YELLOW + scenario.getName());
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
            }

            player.closeInventory();
            new ScenariosGUI(instance, player);
        }
    }

}