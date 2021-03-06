package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Ban;
import me.StevenLawson.TotalFreedomMod.TFM_BanManager;
import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TotalFreedomMod;
import net.minecraft.util.org.apache.commons.lang3.ArrayUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Makes someone GTFO (deop and ip ban by username).", usage = "/<command> <partialname>")
public class Command_gtfo extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0)
        {
            return false;
        }

        final Player player = getPlayer(args[0]);

        if (player == null)
        {
            playerMsg(TotalFreedomMod.PLAYER_NOT_FOUND, ChatColor.RED);
            return true;
        }

        String reason = null;
        if (args.length >= 2)
        {
            reason = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");
        }

        TFM_Util.bcastMsg(player.getName() + " has been a VERY bad player.", ChatColor.RED);

        // Silently rollback the user with CoreProtect
        server.dispatchCommand(sender, "co rb u:" + player.getName() + " t:24h r:global #silent");

        // Disabled TFM RollbackManager and WorldEdit undones due to Coreprotect is handle it now.
        // Undo WorldEdits:
        /*       try
         {
         TFM_WorldEditBridge.undo(player, 15);
         }
         catch (NoClassDefFoundError ex)
         {
         }

         // rollback
         TFM_RollbackManager.rollback(player.getName());
         */
        // deop
        player.setOp(false);

        // set gamemode to survival:
        player.setGameMode(GameMode.SURVIVAL);

        // clear inventory:
        player.getInventory().clear();

        // strike with lightning effect:
        final Location targetPos = player.getLocation();
        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                final Location strike_pos = new Location(targetPos.getWorld(), targetPos.getBlockX() + x, targetPos.getBlockY(), targetPos.getBlockZ() + z);
                targetPos.getWorld().strikeLightning(strike_pos);
            }
        }

        // ban IP address:
        String ip = TFM_Util.getFuzzyIp(player.getAddress().getAddress().getHostAddress());
        TFM_Util.bcastMsg(String.format("%s - Banning: %s, & IPs: %s.", sender.getName(), player.getName(), ip), ChatColor.RED);
        TFM_Util.bcastMsg(ChatColor.RED +  (reason != null ? ("Reason: " + ChatColor.YELLOW + reason) : ""));

        TFM_BanManager.addIpBan(new TFM_Ban(ip, player.getName(), sender.getName(), null, reason));

        // ban username:
        TFM_BanManager.addUuidBan(new TFM_Ban(TFM_Util.getUuid(player), player.getName(), sender.getName(), null, reason));

        // kick Player:
        player.kickPlayer(ChatColor.RED + "GTFO" + (reason != null ? ("\nReason: " + ChatColor.YELLOW + reason) : "") + " - " + sender.getName());

        return true;
    }
}
