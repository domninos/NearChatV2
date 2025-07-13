package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class LicenseSubCommand extends SubCommand {
    public LicenseSubCommand(NearChatPlugin plugin, MainCommand mainCommand) {
        super(plugin, mainCommand);
    }

    @Override
    public String getCommand() {
        return "license";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            mainCommand.sendHelp(sender);
            return true;
        } else if (args.length == 2) {
            String key = args[1];

            if (key == null || key.isBlank()) {
                plugin.sendMessage(sender, "&cPlease supply the correct information.");
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean res = plugin.getLicenseManager().activateLicense(key);

                if (res)
                    plugin.sendConsole("&aSuccessfully activated license!");
                else
                    plugin.sendConsole("&cSomething went wrong activating license.");
            });
        }

        return true;
    }

    @Override
    public int getArg() {
        return 0;
    }

    @Override
    public String getPermission() {
        return "nearchat.license";
    }
}
