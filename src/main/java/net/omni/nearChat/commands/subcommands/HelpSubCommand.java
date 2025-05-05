package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import org.bukkit.command.CommandSender;

public class HelpSubCommand extends SubCommand {
    public HelpSubCommand(NearChatPlugin plugin, MainCommand mainCommand) {
        super(plugin, mainCommand);
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"h"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        StringBuilder toSend = new StringBuilder("\n");

        for (String line : mainCommand.getHelpText()) {
            if (line.isBlank()) continue;

            String firstWord = line.split(" ")[0];

            if (firstWord.startsWith("<nearchat")) {
                // has permission check
                String permission = firstWord.replaceAll("<", "").replaceAll(">", "");

                if (sender.hasPermission(permission))
                    toSend.append(line.replace(permission, "").strip());

                continue;
            }

            toSend.append(line).append("\n");
        }

        sender.sendMessage(toSend.toString());
        return true;
    }

    @Override
    public int getArg() {
        return 0;
    }

    @Override
    public String getPermission() {
        return "nearchat.use";
    }
}
