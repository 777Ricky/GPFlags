package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.FlightManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlagDef_NoGravity extends PlayerMovementFlagDefinition {

    private final Map<UUID, Boolean> previousGravity = new HashMap<>();

    public FlagDef_NoGravity(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public void onFlagSet(Claim claim, String parameters) {
        syncPlayers(claim);
    }

    @Override
    public void onFlagUnset(Claim claim) {
        Bukkit.getScheduler().runTask(plugin, () -> syncPlayers(claim));
    }

    @Override
    public void onChangeClaim(Player player, Location from, Location to, Claim claimFrom, Claim claimTo,
                              @Nullable Flag flagFrom, @Nullable Flag flagTo) {
        syncPlayer(player, flagTo);
    }

    private void syncPlayers(Claim claim) {
        for (Player player : Util.getPlayersIn(claim)) {
            syncPlayer(player, getFlagInstanceAtLocation(player.getLocation(), player));
        }
    }

    private void syncPlayer(Player player, @Nullable Flag flag) {
        if (flag == null || player.hasPermission("gpflags.bypass.nogravity")) {
            restoreGravity(player, true);
            return;
        }

        previousGravity.putIfAbsent(player.getUniqueId(), player.hasGravity());
        player.setFallDistance(0);
        player.setGravity(false);
    }

    private void restoreGravity(Player player, boolean protectFromFall) {
        Boolean hadGravity = previousGravity.remove(player.getUniqueId());
        if (hadGravity == null) return;

        if (protectFromFall && hadGravity && !player.hasGravity()) {
            FlightManager.considerForFallImmunity(player);
        }
        player.setFallDistance(0);
        player.setGravity(hadGravity);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        restoreGravity(event.getPlayer(), false);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != plugin) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            restoreGravity(player, false);
        }
    }

    @Override
    public String getName() {
        return "NoGravity";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnableNoGravity);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisableNoGravity);
    }
}
