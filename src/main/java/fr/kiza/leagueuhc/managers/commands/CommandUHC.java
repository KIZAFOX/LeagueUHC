package fr.kiza.leagueuhc.managers.commands;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.gui.core.AbstractGui;
import fr.kiza.leagueuhc.core.api.gui.core.ButtonAction;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.effect.EffectType;
import fr.kiza.leagueuhc.core.game.effect.EffectsApplier;
import fr.kiza.leagueuhc.core.game.helper.InventoryHelper;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CommandUHC implements CommandExecutor, TabCompleter {

    protected final LeagueUHC instance;

    public static PregenManager pregenManager;

    public CommandUHC(LeagueUHC instance) {
        this.instance = instance;
        pregenManager = new PregenManager(instance);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est réservée aux joueurs !");
            return true;
        }

        final Player player = (Player) sender;

        if(args.length < 1) {
            this.sendHelpMessage(player);
            return true;
        }

        final String sub = args[0].toLowerCase();

        switch(sub) {
            case "setup":
                this.handleSetupCommand(player);
                return true;
            case "pregen":
                this.handlePregenCommand(player, args);
                return true;
            case "inv":
            case "inventory":
                this.handleInventoryCommand(player, args);
                return true;
            case "scenarios":
                this.handleScenariosCommand(player);
                return true;
            case "rules":
                this.handleRulesCommand(player);
                break;
            case "start":
                this.handleStartCommand(player);
                return true;
            case "stop":
                this.handleStopCommand(player);
                return true;
            case "op":
                this.handleOp(player, args);
                return true;
            case "host":
                this.handleHost(player, args);
                return true;
            case "help":
            case "?":
            default:
                this.sendHelpMessage(player);
                return true;
        }
        return false;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage(ChatColor.GOLD + "  " + ChatColor.YELLOW + "⚔ " + ChatColor.BOLD + "LeagueUHC" + ChatColor.RESET + ChatColor.YELLOW + " ⚔" + ChatColor.GOLD + "  ");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Configuration:");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc setup" + ChatColor.GRAY + " - Menu de configuration");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc pregen <start|stop|pause|resume|check|spawn>" + ChatColor.GRAY + " - Pré-générer la map/vérifier la map");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc inv/ui" + ChatColor.GRAY + " - Config inventaire départ");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc scenarios" + ChatColor.GRAY + " - Gérer les scénarios");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc rules" + ChatColor.GRAY + " - Affiche les règles de la partie");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Gestion:");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc start" + ChatColor.GRAY + " - Lancer la partie");
        player.sendMessage(ChatColor.GOLD + "┗ " + ChatColor.WHITE + "/uhc stop" + ChatColor.GRAY + " - Arrêter la partie");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "┗ " + ChatColor.WHITE + "/uhc op [joueur]" + ChatColor.GRAY + " - Mets opérateur le joueur");
        player.sendMessage(ChatColor.GOLD + "┗ " + ChatColor.WHITE + "/uhc host [joueur]" + ChatColor.GRAY + " - Donne les droits de host au joueur");
        player.sendMessage("");
    }

    private void handleSetupCommand(Player player) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage(ChatColor.GOLD + "  " + ChatColor.YELLOW + "⚙ Menu Configuration ⚙" + ChatColor.GOLD + "   ");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Fonctionnalité en développement...");
        player.sendMessage(ChatColor.YELLOW + "Un menu GUI sera bientôt disponible !");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "Commandes disponibles:");
        player.sendMessage(ChatColor.WHITE + "• /uhc pregen <taille>" + ChatColor.GRAY + " - Générer la map");
        player.sendMessage(ChatColor.WHITE + "• /uhc border <taille>" + ChatColor.GRAY + " - Bordure");
        player.sendMessage(ChatColor.WHITE + "• /uhc inv" + ChatColor.GRAY + " - Inventaire départ");
        player.sendMessage(ChatColor.WHITE + "• /uhc scenarios" + ChatColor.GRAY + " - Scénarios");
        player.sendMessage("");
    }

    private void handlePregenCommand(Player player, String[] args) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        if(args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /uhc pregen [start|stop|pause|resume|check|spawn]");
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "start" + ChatColor.GRAY + " - Lance une pregen pour la map UHC");
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "stop" + ChatColor.GRAY + " - Annule la pregen en cours");
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "pause" + ChatColor.GRAY + " - Mets en pause la pregen actuel");
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "resume" + ChatColor.GRAY + " - Relance la pregen en cours");
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "check" + ChatColor.GRAY + " - Téléporte sur la map du UHC");
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "spawn" + ChatColor.GRAY + " - Téléporte au lobby");
            return;
        }

        switch (args[1].toLowerCase()){
            case "start":
                if(pregenManager.isRunning()){
                    player.sendMessage(ChatColor.RED+"✘ Une prégen est déjà en cours !");
                    return;
                }

                player.sendMessage(ChatColor.GREEN+"✔ Lancement de la pré-génération 1000x1000...");
                pregenManager.startPregen(player);
                break;

            case "stop":
                if(!pregenManager.isRunning()){
                    player.sendMessage(ChatColor.RED+"✘ Aucune prégen en cours !");
                    return;
                }
                pregenManager.stopPregen(player);
                break;

            case "pause":
                if(!pregenManager.isRunning()){
                    player.sendMessage(ChatColor.RED+"✘ Aucune prégen en cours !");
                    return;
                }
                if(pregenManager.isPaused()){
                    player.sendMessage(ChatColor.RED+"✘ La prégen est déjà en pause !");
                    return;
                }
                pregenManager.pausePregen(player);
                break;

            case "resume":
                if(!pregenManager.isRunning()){
                    player.sendMessage(ChatColor.RED+"✘ Aucune prégen en cours !");
                    return;
                }
                if(!pregenManager.isPaused()){
                    player.sendMessage(ChatColor.RED+"✘ La prégen n'est pas en pause !");
                    return;
                }
                pregenManager.resumePregen(player);
                break;

            case "check":
                World checkWorld = pregenManager.getWorld();
                if(checkWorld==null){
                    player.sendMessage(ChatColor.RED+"✘ Le monde UHC n'existe pas !");
                    return;
                }
                player.teleport(new Location(checkWorld,0,150,0));
                player.sendMessage(ChatColor.GREEN+"✔ Téléporté dans uhc-world.");
                break;

            case "spawn":
                player.teleport(new Location(Bukkit.getWorld("world"),0,100,0));
                player.sendMessage(ChatColor.GREEN+"✔ Retour au lobby.");
                break;

            default:
                player.sendMessage(ChatColor.RED+"✘ Sous-commande inconnue !");
                break;
        }
    }

    private void handleInventoryCommand(Player player, String[] args) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        if (args.length == 1) {
            final UUID uuid = player.getUniqueId();

            if(!InventoryHelper.getSavedInventory().containsKey(uuid)) {
                InventoryHelper.getSavedInventory().put(uuid, new InventoryHelper(
                        player.getInventory().getContents().clone(),
                        player.getInventory().getArmorContents().clone(),
                        player.getGameMode()
                ));

                player.setGameMode(GameMode.CREATIVE);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);

                if(InventoryHelper.hasStartInventory()) {
                    InventoryHelper startInv = InventoryHelper.getStartInventory();
                    player.getInventory().setContents(startInv.getContents().clone());
                    player.getInventory().setArmorContents(startInv.getArmor().clone());

                    player.sendMessage("");
                    player.sendMessage(ChatColor.GREEN + "✔ Mode édition activé !");
                    player.sendMessage(ChatColor.YELLOW + "➜ Modification du kit universel");
                    player.sendMessage(ChatColor.GOLD + "⚠ Ce kit sera donné à TOUS les joueurs");
                    player.sendMessage(ChatColor.GRAY + "Refais " + ChatColor.WHITE + "/uhc inv" + ChatColor.GRAY + " pour sauvegarder");
                    player.sendMessage("");
                } else {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GREEN + "✔ Mode édition activé !");
                    player.sendMessage(ChatColor.YELLOW + "➜ Crée le kit universel de départ");
                    player.sendMessage(ChatColor.GOLD + "⚠ Ce kit sera donné à TOUS les joueurs");
                    player.sendMessage(ChatColor.GRAY + "Place les items et armures aux bons slots");
                    player.sendMessage(ChatColor.GRAY + "Fais " + ChatColor.WHITE + "/uhc inv" + ChatColor.GRAY + " pour sauvegarder");
                    player.sendMessage("");
                }

            } else {
                InventoryHelper.setStartInventory(new InventoryHelper(
                        player.getInventory().getContents().clone(),
                        player.getInventory().getArmorContents().clone(),
                        GameMode.SURVIVAL
                ));

                final InventoryHelper backup = InventoryHelper.getSavedInventory().remove(uuid);

                player.getInventory().setContents(backup.getContents());
                player.getInventory().setArmorContents(backup.getArmor());
                player.setGameMode(backup.getGameMode());

                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "✔ Kit universel sauvegardé !");
                player.sendMessage(ChatColor.GOLD + "➜ Tous les joueurs recevront ce kit");
                player.sendMessage(ChatColor.GRAY + "Items et armures enregistrés avec leurs slots");
                player.sendMessage("");

                player.setAllowFlight(true);
                player.setFlying(true);
            }
        } else if(args.length == 2 && args[1].equalsIgnoreCase("check")) {
            if(!InventoryHelper.hasStartInventory()) {
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "✘ Aucun kit universel n'a été créé !");
                player.sendMessage(ChatColor.GRAY + "Utilise " + ChatColor.WHITE + "/uhc inv" + ChatColor.GRAY + " pour en créer un");
                player.sendMessage("");
                return;
            }

            this.openKitCheckGui(player);
        }
    }

    private void openKitCheckGui(Player player) {
        final InventoryHelper startInv = InventoryHelper.getStartInventory();
        final GuiBuilder builder = new GuiBuilder(this.instance);

        builder.title(ChatColor.GOLD + "Kit - Check").size(54);

        final ButtonAction noAction = (p, inv) -> {};

        final ItemStack[] contents = startInv.getContents();

        for(int i = 0; i < contents.length && i < 36; i++) {
            if(contents[i] != null) {
                builder.button(i, contents[i].clone(), noAction);
            }
        }

        final ItemStack[] armor = startInv.getArmor();

        if(armor != null) {
            if(armor[0] != null) {
                builder.button(36, armor[0].clone(), null);
            }
            if(armor[1] != null) {
                builder.button(37, armor[1].clone(), noAction);
            }
            if(armor[2] != null) {
                builder.button(38, armor[2].clone(), noAction);
            }
            if(armor[3] != null) {
                builder.button(39, armor[3].clone(), noAction);
            }
        }

        final ItemStack info = new ItemStack(Material.PAPER);
        final ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "ℹ Informations");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Ce kit sera donné à",
                ChatColor.GRAY + "tous les joueurs au début",
                "",
                ChatColor.GOLD + "Slots 1-36: " + ChatColor.WHITE + "Inventaire",
                ChatColor.GOLD + "Slots 37-40: " + ChatColor.WHITE + "Armures"
        ));
        info.setItemMeta(infoMeta);

        builder.button(45, info, noAction);
        builder.build().open(player);
    }

    private void handleScenariosCommand(Player player) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        final GameContext context = this.instance.getGameEngine().getContext();

        if (context.getActiveScenarios().isEmpty()) {
            player.sendMessage(ChatColor.RED + "✘ Aucun scénarios n'a été ajouté !");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage(ChatColor.GOLD + "  " + ChatColor.YELLOW + "📜 Scénarios UHC" + ChatColor.GOLD + "        ");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage("");
        context.getActiveScenarios().forEach(scenario -> {
            player.sendMessage("- " + scenario.getName() + (scenario.hasPercentage() ? " | " + context.getScenarioPercentage(scenario) + " %" : ""));
        });
        player.sendMessage("");
    }

    private void handleRulesCommand(Player player) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        final GameContext context = this.instance.getGameEngine().getContext();
        final EffectsApplier effectsApplier = this.instance.getGameEngine().getEffectsApplier();

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "     " + ChatColor.YELLOW + ChatColor.BOLD + "⚔ RÈGLES DE LA PARTIE ⚔");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage("");

        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ EFFETS ACTIFS:");
        player.sendMessage("");

        boolean hasActiveEffects = false;
        for (EffectType effectType : EffectType.values()) {
            int percentage = effectsApplier.getCurrentPercentage(effectType);

            if (percentage >= EffectType.MIN_PERCENTAGE) {
                hasActiveEffects = true;
                int level = effectsApplier.getCurrentLevel(effectType);

                String levelDisplay = getLevelDisplay(level);
                String bar = getPercentageBar(percentage);

                player.sendMessage(effectType.getColor() + "  " + effectType.getIcon() + " " +
                        effectType.getName() + ChatColor.GRAY + " - " +
                        ChatColor.YELLOW + percentage + "% " + bar);
                player.sendMessage(ChatColor.GRAY + "     └─ Niveau: " + levelDisplay);
            }
        }

        if (!hasActiveEffects) {
            player.sendMessage(ChatColor.GRAY + "  Aucun effet actif");
        }

        player.sendMessage("");

        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "📜 SCÉNARIOS ACTIVÉS:");
        player.sendMessage("");

        if (context.getActiveScenarios().isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "  Aucun scénario activé");
        } else {
            context.getActiveScenarios().forEach(scenario -> {
                String scenarioInfo = ChatColor.YELLOW + "  • " + ChatColor.WHITE + scenario.getName();

                if (scenario.hasPercentage()) {
                    int percentage = context.getScenarioPercentage(scenario);
                    scenarioInfo += ChatColor.GRAY + " - " + ChatColor.GREEN + percentage + "%";
                }

                player.sendMessage(scenarioInfo);
            });
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Bonne chance et amusez-vous bien !");
        player.sendMessage("");
    }

    private void handleStartCommand(Player player) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Bientôt disponible !");
    }

    private void handleStopCommand(Player player) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Bientôt disponible !");
    }

    private void handleOp(Player player, String[] args) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        if(args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /uhc op [joueur]");
            return;
        }

        final Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "✘ Le joueur n'existe pas.");
            return;
        }

        if (target.isOp()) {
            target.setOp(false);

            player.sendMessage(ChatColor.GREEN + "Droits opérateur enlevé de " + ChatColor.BOLD + target.getName());
            target.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Vous avez perdus les droits opérateur !");
        } else {
            target.setOp(true);

            player.sendMessage(ChatColor.GREEN + "Droits opérateur donné à " + ChatColor.BOLD + target.getName());
            target.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Vous avez reçus les droits opérateur !");
        }
    }

    private void handleHost(Player player, String[] args) {
        if(!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        if(args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /uhc host [joueur]");
            return;
        }

        final Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "✘ Le joueur n'existe pas.");
            return;
        }

        final boolean hasHostPermission = target.hasPermission("host.admin");

        if (hasHostPermission) {
            target.addAttachment(this.instance, "host.admin", false);

            player.sendMessage(ChatColor.GREEN + "Droits de host enlevé de " + ChatColor.BOLD + target.getName());
            target.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Vous avez perdus les droits de host !");
        } else {
            target.addAttachment(this.instance, "host.admin", true);

            player.sendMessage(ChatColor.GREEN + "Droits de host donné à " + ChatColor.BOLD + target.getName());
            target.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Vous avez reçus les droits de host !");
        }
    }

    private boolean hasPermission(final Player player) {
        return player.isOp() || player.hasPermission("host.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> completions = new ArrayList<>();

        if(args.length == 1) {
            final String input = args[0].toLowerCase();
            final List<String> commands = Arrays.asList("help", "?", "setup", "pregen", "inv", "inventory", "scenarios", "rules", "start", "stop", "op", "host");
            for(String command : commands) {
                if(command.startsWith(input)) completions.add(command);
            }
            return completions;
        }

        if(args.length == 2) {
            final String input = args[1].toLowerCase();

            if(args[0].equalsIgnoreCase("champion")) {
                for(String name : ChampionRegistry.getRegisteredNames()) {
                    if(name.toLowerCase().startsWith(input)) completions.add(name);
                }
            }

            if(args[0].equalsIgnoreCase("pregen")) {
                completions.addAll(Arrays.asList("check", "reboot", "500", "1000", "1500", "2000", "2500"));
            }
        }

        return completions;
    }

    private String getPercentageBar(int percentage) {
        int filled = (percentage - EffectType.MIN_PERCENTAGE) / 20; // 0 à 4 barres
        StringBuilder bar = new StringBuilder();

        bar.append(ChatColor.WHITE).append("[");

        for (int i = 0; i < 4; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN).append("█");
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        bar.append(ChatColor.WHITE).append("]");
        return bar.toString();
    }

    private String getLevelDisplay(int level) {
        StringBuilder display = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            if (i < level) {
                display.append(ChatColor.GOLD).append("★");
            } else {
                display.append(ChatColor.DARK_GRAY).append("★");
            }
        }

        display.append(" ");

        switch (level) {
            case 0:
                display.append(ChatColor.GRAY).append("Faible");
                break;
            case 1:
                display.append(ChatColor.WHITE).append("Normal");
                break;
            case 2:
                display.append(ChatColor.YELLOW).append("Moyen");
                break;
            case 3:
                display.append(ChatColor.GOLD).append("Élevé");
                break;
            case 4:
                display.append(ChatColor.RED).append("Maximum");
                break;
            default:
                display.append(ChatColor.GRAY).append("Inconnu");
                break;
        }

        return display.toString();
    }
}