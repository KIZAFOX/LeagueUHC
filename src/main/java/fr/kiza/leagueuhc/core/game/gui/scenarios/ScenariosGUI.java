package fr.kiza.leagueuhc.core.game.gui.scenarios;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.game.gui.settings.SettingsGUI;
import fr.kiza.leagueuhc.core.game.scenario.Scenario;
import fr.kiza.leagueuhc.core.game.context.GameContext;
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
    private final GameContext context;

    public ScenariosGUI(final LeagueUHC instance, final Player player) {
        this.instance = instance;
        this.context = instance.getGameEngine().getContext();

        buildGUI(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
    }

    private void buildGUI(final Player player) {
        GuiBuilder gui = new GuiBuilder(instance)
                .title(ChatColor.LIGHT_PURPLE + "Scénarios UHC")
                .size(54);

        // Parcourir tous les scénarios
        Scenario[] scenarios = Scenario.values();
        int slot = 10;
        int row = 0;

        for (Scenario scenario : scenarios) {
            // Calculer le slot (éviter les bords)
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;

            boolean isActive = context.isScenarioActive(scenario);
            int percentage = context.getScenarioPercentage(scenario);

            Material material = getMaterialForScenario(scenario);
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

            final int currentSlot = slot;
            gui.button(slot, builder.toItemStack(), (p, inv) -> {
                handleScenarioClick(p, scenario, inv);
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
                        .toItemStack(), (p, inv) -> { });
            }
        }

        gui.build().open(player);
    }

    private void handleScenarioClick(Player player, Scenario scenario, org.bukkit.inventory.Inventory inv) {
        boolean wasActive = context.isScenarioActive(scenario);

        if (scenario.hasPercentage()) {
            // Ouvrir le menu de modification du pourcentage
            player.closeInventory();
            new ScenarioPercentageGUI(instance, player, scenario);
        } else {
            // Toggle simple
            if (wasActive) {
                context.removeScenario(scenario);
                player.sendMessage(ChatColor.RED + "[UHC] Scénario désactivé: " + ChatColor.YELLOW + scenario.getName());
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0f, 1.0f);
            } else {
                context.addScenario(scenario);
                player.sendMessage(ChatColor.GREEN + "[UHC] Scénario activé: " + ChatColor.YELLOW + scenario.getName());
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
            }

            player.closeInventory();
            new ScenariosGUI(instance, player);
        }
    }

    private Material getMaterialForScenario(Scenario scenario) {
        switch (scenario) {
            case CUTCLEAN: return Material.FURNACE;
            case GRAB_ORE: return Material.HOPPER;
            case NO_LAVA: return Material.LAVA_BUCKET;
            case BOOST_XP: return Material.EXP_BOTTLE;
            case BOOST_POMME: return Material.APPLE;
            case BOOST_SILEX: return Material.FLINT;
            case BOOST_PLUME: return Material.FEATHER;
            case BOOST_DIAMANT: return Material.DIAMOND;
            case BOOST_OR: return Material.GOLD_INGOT;
            case BOOST_CAVE: return Material.STONE;
            case OUTIL: return Material.DIAMOND_PICKAXE;
            case ARBRE: return Material.LOG;
            case FINAL_HEALTH: return Material.GOLDEN_APPLE;
            default: return Material.PAPER;
        }
    }
}