package fr.kiza.leagueuhc.managers.commands;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.core.api.champion.ChampionRegistry;
import fr.kiza.leagueuhc.core.api.gui.core.ButtonAction;
import fr.kiza.leagueuhc.core.api.gui.helper.GuiBuilder;
import fr.kiza.leagueuhc.core.api.scenario.Scenario;
import fr.kiza.leagueuhc.core.api.scenario.ScenarioManager;
import fr.kiza.leagueuhc.core.game.GameEngine;
import fr.kiza.leagueuhc.core.game.context.GameContext;
import fr.kiza.leagueuhc.core.game.helper.InventoryHelper;
import fr.kiza.leagueuhc.core.game.helper.pregen.PregenManager;
import fr.kiza.leagueuhc.core.game.host.HostManager;
import fr.kiza.leagueuhc.core.game.state.GameState;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
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

    private ScenarioManager getScenarioManager() {
        return this.instance.getGameEngine().getGameHelper().getManager().getScenarioManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("host")) {
            this.handleHost(sender, args);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est réservée aux joueurs !");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            this.sendHelpMessage(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
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
            case "start":
                this.handleStartCommand(player);
                return true;
            case "stop":
                this.handleStopCommand(player);
                return true;
            case "scenarios":
                this.handleScenariosCommand(player);
                return true;
            case "rules":
                this.handleRulesCommand(player);
                break;
            case "helpop":
                this.handleHelpop(player, args);
                break;
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
        player.sendMessage(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "Configuration: " + ChatColor.RED + "(admin)");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc setup" + ChatColor.GRAY + " - Menu de configuration");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc pregen <start|check|spawn>" + ChatColor.GRAY + " - Pré-générer la map/vérifier la map");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc inv" + ChatColor.GRAY + " - Config inventaire départ");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc start" + ChatColor.GRAY + " - Lancer la partie");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc stop" + ChatColor.GRAY + " - Arrêter la partie");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc host <add|remove|list|clear> [pseudo]" + ChatColor.GRAY + " - Gérer les hosts " + ChatColor.RED + "(console)");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "┃ " + ChatColor.YELLOW + "A propos:");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc inv check" + ChatColor.GRAY + " - Config inventaire départ");
        player.sendMessage(ChatColor.GOLD + "┣ " + ChatColor.WHITE + "/uhc scenarios" + ChatColor.GRAY + " - Voir les scénarios");
        player.sendMessage(ChatColor.GOLD + "┗ " + ChatColor.WHITE + "/uhc rules" + ChatColor.GRAY + " - Affiche les règles de la partie");
        player.sendMessage(ChatColor.GOLD + "┗ " + ChatColor.WHITE + "/uhc helpop" + ChatColor.GRAY + " - Envoyer un message au host de la partie");
        player.sendMessage("");
    }

    private void handleSetupCommand(Player player) {
        if (!this.hasPermission(player)) {
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
        player.sendMessage(ChatColor.WHITE + "• /uhc pregen start" + ChatColor.GRAY + " - Générer la map");
        player.sendMessage(ChatColor.WHITE + "• /uhc inv" + ChatColor.GRAY + " - Inventaire départ");
        player.sendMessage(ChatColor.WHITE + "• Intéragir avec l'objet dans l'inventaire");
        player.sendMessage("");
    }

    private void handlePregenCommand(Player player, String[] args) {
        if (!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "==============================");
            player.sendMessage(ChatColor.GOLD + "  " + ChatColor.YELLOW + "Pre-generation UHC");
            player.sendMessage(ChatColor.GOLD + "==============================");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Configuration actuelle:");
            player.sendMessage(ChatColor.GRAY + "  - Map: " + ChatColor.WHITE + "1000x1000 blocs");
            player.sendMessage(ChatColor.GRAY + "  - Zone arbres: " + ChatColor.WHITE + "rayon 200 blocs au centre");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Commandes:");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen start" + ChatColor.GRAY + " - Lancer la generation");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen stop" + ChatColor.GRAY + " - Annuler la generation");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen pause" + ChatColor.GRAY + " - Mettre en pause");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen resume" + ChatColor.GRAY + " - Reprendre");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen check" + ChatColor.GRAY + " - Teleporter sur la map UHC");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen spawn" + ChatColor.GRAY + " - Retour au lobby");
            player.sendMessage(ChatColor.WHITE + "  /uhc pregen reset" + ChatColor.GRAY + " - Supprimer et reinitialiser");
            player.sendMessage("");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "start":
                pregenManager.startPregen(player);
                break;

            case "stop":
                pregenManager.stopPregen(player);
                break;

            case "check":
                World checkWorld = pregenManager.getWorld();
                if (checkWorld == null) {
                    player.sendMessage(ChatColor.RED + "Le monde UHC n'existe pas encore !");
                    player.sendMessage(ChatColor.YELLOW + "  Utilisez /uhc pregen start pour le creer.");
                    return;
                }

                int y = checkWorld.getHighestBlockYAt(0, 0) + 1;
                Location loc = new Location(checkWorld, 0.5, y, 0.5);
                player.teleport(loc);
                player.setGameMode(GameMode.CREATIVE);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage(ChatColor.GREEN + "Teleporte au centre du monde UHC !");
                player.sendMessage(ChatColor.GRAY + "  Vous etes en mode Creatif pour explorer.");
                break;

            case "spawn":
                World lobby = Bukkit.getWorld("world");
                if (lobby == null) {
                    player.sendMessage(ChatColor.RED + "Le monde principal n'existe pas !");
                    return;
                }
                player.teleport(lobby.getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.GREEN + "Retour au lobby !");
                break;

            case "reset":
                player.sendMessage(ChatColor.YELLOW + "Reinitialisation du monde UHC...");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Sous-commande inconnue: " + args[1]);
                player.sendMessage(ChatColor.GRAY + "Utilisez /uhc pregen pour voir les options.");
                break;
        }
    }

    private void handleInventoryCommand(Player player, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("check")) {
            if (!InventoryHelper.hasStartInventory()) {
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "✘ Aucun inventaire de début  n'a été créé !");
                player.sendMessage(ChatColor.GRAY + "Utilise " + ChatColor.WHITE + "/uhc inv" + ChatColor.GRAY + " pour en créer un");
                player.sendMessage("");
                return;
            }

            InventoryHelper startInv = InventoryHelper.getStartInventory();
            GuiBuilder builder = new GuiBuilder(this.instance);

            builder.title(ChatColor.GOLD + "Inventaire de début").size(54);

            ButtonAction noAction = (p, inv) -> {};

            ItemStack[] contents = startInv.getContents();

            for (int i = 0; i < contents.length && i < 36; i++) {
                if (contents[i] != null) {
                    builder.button(i, contents[i].clone(), noAction);
                }
            }

            ItemStack[] armor = startInv.getArmor();

            if (armor != null) {
                if (armor[0] != null) {
                    builder.button(36, armor[0].clone(), null);
                }
                if (armor[1] != null) {
                    builder.button(37, armor[1].clone(), noAction);
                }
                if (armor[2] != null) {
                    builder.button(38, armor[2].clone(), noAction);
                }
                if (armor[3] != null) {
                    builder.button(39, armor[3].clone(), noAction);
                }
            }

            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta infoMeta = info.getItemMeta();
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
            return;
        }

        if (!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        if (args.length == 1) {
            UUID uuid = player.getUniqueId();

            if (!InventoryHelper.getSavedInventory().containsKey(uuid)) {
                InventoryHelper.getSavedInventory().put(uuid, new InventoryHelper(
                        player.getInventory().getContents().clone(),
                        player.getInventory().getArmorContents().clone(),
                        player.getGameMode()
                ));

                player.setGameMode(GameMode.CREATIVE);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);

                if (InventoryHelper.hasStartInventory()) {
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

                InventoryHelper backup = InventoryHelper.getSavedInventory().remove(uuid);

                player.getInventory().setContents(backup.getContents());
                player.getInventory().setArmorContents(backup.getArmor());
                player.setGameMode(backup.getGameMode());

                player.sendMessage("");
                player.sendMessage(ChatColor.GREEN + "✔ Inventaire de début sauvegardé !");
                player.sendMessage(ChatColor.GOLD + "➜ Tous les joueurs recevront ce kit");
                player.sendMessage(ChatColor.GRAY + "Items et armures enregistrés avec leurs slots");
                player.sendMessage("");

                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

    private void handleStartCommand(Player player) {
        if (!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Bientôt disponible !");
    }

    private void handleStopCommand(Player player) {
        if (!this.hasPermission(player)) {
            player.sendMessage(ChatColor.RED + "✘ Vous n'avez pas la permission !");
            return;
        }

        GameEngine engine = this.instance.getGameEngine();
        GameContext context = this.instance.getGameEngine().getContext();

        if (!engine.getCurrentState().equalsIgnoreCase(GameState.PLAYING.getName())) {
            player.sendMessage(ChatColor.RED + "✘ Aucune partie en cours !");
            return;
        }

        context.setData("gameEnded", true);

        player.sendMessage(ChatColor.GREEN + "✔ Vous avez forcé l'arrêt de la partie !");
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "⚠ La partie a été arrêtée par un administrateur.");
    }

    private void handleHost(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "[UHC] Seule la console peut gérer les hosts.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "[UHC] Usage: /uhc host <add|remove|list|clear> [pseudo]");
            return;
        }

        String hostAction = args[1].toLowerCase();

        switch (hostAction) {
            case "add":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "[UHC] Usage: /uhc host add <pseudo>");
                    return;
                }
                String nameAdd = args[2];
                if (HostManager.isPendingHost(nameAdd) || isAlreadyHost(nameAdd)) {
                    sender.sendMessage(ChatColor.YELLOW + "[UHC] " + nameAdd + " est déjà host.");
                    return;
                }
                HostManager.addHostByName(nameAdd);
                sender.sendMessage(ChatColor.GREEN + "[UHC] " + ChatColor.YELLOW + nameAdd + ChatColor.GREEN + " est maintenant host !");

                Player onlineAdd = Bukkit.getPlayer(nameAdd);
                if (onlineAdd != null && onlineAdd.isOnline()) {
                    onlineAdd.sendMessage(ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    onlineAdd.sendMessage(ChatColor.GOLD + "  ⚔ " + ChatColor.BOLD + "HOST DÉSIGNÉ" + ChatColor.GOLD + " ⚔");
                    onlineAdd.sendMessage("");
                    onlineAdd.sendMessage(ChatColor.GRAY + "  Vous avez été désigné comme host !");
                    onlineAdd.sendMessage(ChatColor.GRAY + "  Vous pouvez maintenant gérer la partie.");
                    onlineAdd.sendMessage(ChatColor.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[UHC] " + ChatColor.YELLOW + nameAdd + ChatColor.GOLD + " est maintenant host de la partie !");
                } else {
                    sender.sendMessage(ChatColor.GRAY + "[UHC] " + nameAdd + " sera notifié à sa connexion.");
                }
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "[UHC] Usage: /uhc host remove <pseudo>");
                    return;
                }
                String nameRemove = args[2];
                if (!HostManager.isPendingHost(nameRemove) && !isAlreadyHost(nameRemove)) {
                    sender.sendMessage(ChatColor.YELLOW + "[UHC] " + nameRemove + " n'est pas host.");
                    return;
                }
                HostManager.removeHostByName(nameRemove);
                sender.sendMessage(ChatColor.GREEN + "[UHC] " + ChatColor.YELLOW + nameRemove + ChatColor.GREEN + " n'est plus host.");

                Player onlineRemove = Bukkit.getPlayer(nameRemove);
                if (onlineRemove != null && onlineRemove.isOnline()) {
                    onlineRemove.sendMessage(ChatColor.RED + "[UHC] Vous n'êtes plus host de la partie.");
                }
                break;

            case "list":
                Set<UUID> hosts = HostManager.getHosts();
                if (hosts.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "[UHC] Aucun host désigné.");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    sender.sendMessage(ChatColor.GOLD + "  ⚔ HOSTS (" + hosts.size() + ")");
                    sender.sendMessage("");
                    for (UUID uuid : hosts) {
                        String name = HostManager.getHostName(uuid);
                        Player hostPlayer = Bukkit.getPlayer(uuid);
                        String status = (hostPlayer != null && hostPlayer.isOnline()) ? ChatColor.GREEN + "●" : ChatColor.RED + "●";
                        sender.sendMessage(ChatColor.GRAY + "  " + status + " " + ChatColor.YELLOW + name);
                    }
                    sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                }
                break;

            case "clear":
                HostManager.clearHosts();
                sender.sendMessage(ChatColor.GREEN + "[UHC] Tous les hosts ont été retirés.");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "[UHC] Usage: /uhc host <add|remove|list|clear> [pseudo]");
                break;
        }
    }

    private void handleScenariosCommand(Player player) {
        GameContext context = this.instance.getGameEngine().getContext();
        ScenarioManager scenarioManager = getScenarioManager();
        Set<String> activeIds = context.getActiveScenarioIds();

        if (activeIds.isEmpty()) {
            player.sendMessage(ChatColor.RED + "✘ Aucun scénario n'a été ajouté !");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage(ChatColor.GOLD + "  " + ChatColor.YELLOW + "📜 Scénarios UHC" + ChatColor.GOLD + "        ");
        player.sendMessage(ChatColor.GOLD + "==========================");
        player.sendMessage("");

        for (String id : activeIds) {
            Scenario scenario = scenarioManager.get(id);
            if (scenario != null) {
                String line = ChatColor.YELLOW + "• " + ChatColor.WHITE + scenario.getName();
                if (scenario.hasPercentage()) {
                    line += ChatColor.GRAY + " | " + ChatColor.GREEN + context.getScenarioPercentage(id) + "%";
                }
                player.sendMessage(line);
            }
        }

        player.sendMessage("");
    }

    private void handleRulesCommand(Player player) {
        GameContext context = this.instance.getGameEngine().getContext();
        ScenarioManager scenarioManager = getScenarioManager();
        Set<String> activeIds = context.getActiveScenarioIds();

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "     " + ChatColor.YELLOW + ChatColor.BOLD + "⚔ RÈGLES DE LA PARTIE ⚔");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage("");

        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ EFFETS ACTIFS:");
        player.sendMessage("");

        player.sendMessage("");

        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "📜 SCÉNARIOS ACTIVÉS:");
        player.sendMessage("");

        if (activeIds.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "  Aucun scénario activé");
        } else {
            for (String id : activeIds) {
                Scenario scenario = scenarioManager.get(id);
                if (scenario != null) {
                    String scenarioInfo = ChatColor.YELLOW + "  • " + ChatColor.WHITE + scenario.getName();
                    if (scenario.hasPercentage()) {
                        int percentage = context.getScenarioPercentage(id);
                        scenarioInfo += ChatColor.GRAY + " - " + ChatColor.GREEN + percentage + "%";
                    }
                    player.sendMessage(scenarioInfo);
                }
            }
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Bonne chance et amusez-vous bien !");
        player.sendMessage("");
    }

    private void handleHelpop(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(ChatColor.RED + "✘ Vous devez écrire un message !");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            stringBuilder.append(args[i]);
            if (i + 1 < args.length) stringBuilder.append(" ");
        }

        if (stringBuilder.length() > 50) {
            player.sendMessage(ChatColor.RED + "✘ Maximum '50' caractères !");
            return;
        }

        Bukkit.getOnlinePlayers().forEach(players -> {
            if (this.hasPermission(players)) {
                players.sendMessage(ChatColor.DARK_GREEN + "[HELPOP - " + player.getName() + "] " + ChatColor.WHITE + stringBuilder);
            }
        });
    }

    private boolean hasPermission(Player player) {
        return HostManager.isHost(player);
    }

    @SuppressWarnings("deprecation")
    private boolean isAlreadyHost(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) {
            return HostManager.isHost(online);
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore()) {
            return HostManager.isHost(offline.getUniqueId());
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        boolean isPlayer = sender instanceof Player;
        boolean isHost = isPlayer && HostManager.isHost((Player) sender);
        boolean isConsole = !isPlayer;

        if (args.length == 1) {
            String input = args[0].toLowerCase();

            List<String> publicCommands = Arrays.asList(
                    "help", "scenarios", "rules", "helpop"
            );

            List<String> hostCommands = Arrays.asList(
                    "setup", "pregen", "inv", "inventory",
                    "start", "stop"
            );

            if (isPlayer) {
                for (String command : publicCommands) {
                    if (command.startsWith(input)) {
                        completions.add(command);
                    }
                }

                if (isHost) {
                    for (String command : hostCommands) {
                        if (command.startsWith(input)) {
                            completions.add(command);
                        }
                    }
                } else {
                    if ("inv".startsWith(input)) {
                        completions.add("inv");
                    }
                }
            }

            if (isConsole) {
                if ("host".startsWith(input)) {
                    completions.add("host");
                }
            }

            return completions;
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase();

            if (args[0].equalsIgnoreCase("pregen") && isHost) {
                List<String> pregenSubs = Arrays.asList(
                        "start", "check", "spawn", "reset"
                );
                for (String sub : pregenSubs) {
                    if (sub.startsWith(input)) {
                        completions.add(sub);
                    }
                }
            }

            if (args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("inventory")) {
                if ("check".startsWith(input)) {
                    completions.add("check");
                }
            }

            if (args[0].equalsIgnoreCase("host") && isConsole) {
                List<String> hostSubs = Arrays.asList("add", "remove", "list", "clear");
                for (String sub : hostSubs) {
                    if (sub.startsWith(input)) {
                        completions.add(sub);
                    }
                }
            }
        }

        if (args.length == 3) {
            String input = args[2].toLowerCase();

            if (args[0].equalsIgnoreCase("host") && isConsole) {
                if (args[1].equalsIgnoreCase("add")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(input)) {
                            completions.add(p.getName());
                        }
                    }
                } else if (args[1].equalsIgnoreCase("remove")) {
                    for (UUID uuid : HostManager.getHosts()) {
                        String name = HostManager.getHostName(uuid);
                        if (name != null && name.toLowerCase().startsWith(input)) {
                            completions.add(name);
                        }
                    }
                    for (String pending : HostManager.getPendingHosts()) {
                        if (pending.toLowerCase().startsWith(input)) {
                            completions.add(pending);
                        }
                    }
                }
            }
        }

        return completions;
    }
}