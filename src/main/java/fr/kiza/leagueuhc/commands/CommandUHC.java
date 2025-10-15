package fr.kiza.leagueuhc.commands;

import fr.kiza.leagueuhc.LeagueUHC;
import fr.kiza.leagueuhc.champion.Champion;
import fr.kiza.leagueuhc.champion.ChampionRegistry;
import fr.kiza.leagueuhc.game.GamePlayer;
import fr.kiza.leagueuhc.gui.manager.GuiManager;
import fr.kiza.leagueuhc.gui.core.AbstractGui;
import fr.kiza.leagueuhc.gadget.CrownManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandUHC implements CommandExecutor, TabCompleter {

    private final LeagueUHC instance;

    public CommandUHC(LeagueUHC instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        final Player player = (Player) sender;

        if(args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /uhc <champion|gui|crown>");
            return true;
        }

        final String sub = args[0].toLowerCase();

        switch(sub) {

            case "champion": {
                if(args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /uhc champion <nom>");
                    return true;
                }

                final String championName = args[1].toLowerCase();
                final Champion champion = ChampionRegistry.getChampion(championName);

                if(champion == null) {
                    player.sendMessage(ChatColor.RED + "Champion introuvable: " + championName);
                    return true;
                }

                GamePlayer gamePlayer = GamePlayer.get(player);
                if(gamePlayer == null) gamePlayer = new GamePlayer(player);

                gamePlayer.assignChampion(champion);
                player.sendMessage(ChatColor.GREEN + "✔ Champion " + ChatColor.GOLD + champion.getName() + ChatColor.GREEN + " assigné !");
                return true;
            }

            case "gui": {
                AbstractGui gui = GuiManager.getRegisteredMenu("ExampleGui");

                if(gui == null) {
                    player.sendMessage(ChatColor.RED + "⚠ Aucun menu 'ExampleGui' enregistré !");
                    return true;
                }

                gui.open(player);
                player.sendMessage(ChatColor.GREEN + "📘 Ouverture du menu d'exemple...");
                return true;
            }

            case "crown": {
                if(args.length < 2) {
                    player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Types de Couronnes " + ChatColor.GOLD + "===");
                    player.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.WHITE + "/uhc crown <type|off>");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + "Types disponibles:");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.GREEN + "simple" + ChatColor.DARK_GRAY + " - Couronne basique");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.GOLD + "golden" + ChatColor.DARK_GRAY + " - Flammes dorées");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.LIGHT_PURPLE + "love" + ChatColor.DARK_GRAY + " - Coeurs roses");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.AQUA + "enchanted" + ChatColor.DARK_GRAY + " - Particules magiques");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.RED + "redstone" + ChatColor.DARK_GRAY + " - Particules rouges");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.YELLOW + "critical" + ChatColor.DARK_GRAY + " - Étoiles magiques");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.DARK_PURPLE + "portal" + ChatColor.DARK_GRAY + " - Particules de portail");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "snow" + ChatColor.DARK_GRAY + " - Flocons de neige");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.GOLD + "" + ChatColor.BOLD + "royal" + ChatColor.DARK_GRAY + " - Double anneau");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.DARK_GRAY + "dragon" + ChatColor.DARK_GRAY + " - Spirale de fumée");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.LIGHT_PURPLE + "rainbow" + ChatColor.DARK_GRAY + " - Notes colorées");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.GREEN + "slime" + ChatColor.DARK_GRAY + " - Particules de slime");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.RED + "lava" + ChatColor.DARK_GRAY + " - Gouttes de lave");
                    player.sendMessage(ChatColor.GRAY + "• " + ChatColor.RED + "off" + ChatColor.DARK_GRAY + " - Désactiver la couronne");
                    return true;
                }

                final String type = args[1].toUpperCase();

                if(type.equals("OFF")) {
                    this.instance.getCrownManager().disableCrown(player);
                    player.sendMessage(ChatColor.GREEN + "✔ Couronne désactivée !");
                    return true;
                }

                try {
                    CrownManager.CrownType crownType = CrownManager.CrownType.valueOf(type);
                    this.instance.getCrownManager().enableCrown(player, crownType);
                    player.sendMessage(ChatColor.GREEN + "✔ Couronne activée: " + ChatColor.GOLD + "👑 " + type);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "✘ Type de couronne invalide: " + ChatColor.GRAY + args[1]);
                    player.sendMessage(ChatColor.YELLOW + "Utilisez " + ChatColor.WHITE + "/uhc crown" + ChatColor.YELLOW + " pour voir la liste");
                }
                return true;
            }

            default:
                player.sendMessage(ChatColor.RED + "Usage: /uhc <champion|gui|crown>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> completions = new ArrayList<>();

        if(args.length == 1) {
            final String input = args[0].toLowerCase();
            if("champion".startsWith(input)) completions.add("champion");
            if("gui".startsWith(input)) completions.add("gui");
            if("crown".startsWith(input)) completions.add("crown");
            return completions;
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("champion")) {
            final String input = args[1].toLowerCase();
            for(String name : ChampionRegistry.getRegisteredNames()) {
                if(name.toLowerCase().startsWith(input)) completions.add(name);
            }
        }

        if(args.length == 2 && args[0].equalsIgnoreCase("crown")) {
            final String input = args[1].toLowerCase();
            List<String> crownTypes = Arrays.stream(CrownManager.CrownType.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            crownTypes.add("off");

            for(String type : crownTypes) {
                if(type.startsWith(input)) completions.add(type);
            }
        }

        return completions;
    }
}