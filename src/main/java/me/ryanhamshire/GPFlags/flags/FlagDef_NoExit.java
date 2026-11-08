package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.TextMode;
import me.ryanhamshire.GPFlags.event.PlayerPreClaimBorderEvent;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlagDef_NoExit extends PlayerMovementFlagDefinition {

    public FlagDef_NoExit(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public boolean allowMovement(PlayerPreClaimBorderEvent event) {
        if (event.isTeleport()) return true;
        return allowMovement(event.getPlayer(), event.getLocFrom(), event.getLocTo(),
                event.getClaimFrom(), event.getClaimTo());
    }

    @Override
    public boolean allowMovement(Player player, Location from, Location to, Claim claimFrom, Claim claimTo) {
        Flag flagFrom = getEffectiveFlag(claimFrom, from);
        if (flagFrom == null) return true;

        Flag flagTo = getEffectiveFlag(claimTo, to);
        if (flagFrom == flagTo) return true;
        if (Util.canAccess(claimFrom, player)) return true;
        if (player.hasPermission("gpflags.bypass.noexit")) return true;

        MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoExitMessage);
        return false;
    }

    @Override
    public String getName() {
        return "NoExit";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnabledNoExit);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisabledNoExit);
    }
}
