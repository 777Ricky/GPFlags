package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlagDef_Fog extends PlayerMovementFlagDefinition {

    private static final int DEFAULT_DISTANCE = 2;
    private static final int MIN_DISTANCE = 2;
    private static final int MAX_DISTANCE = 32;

    private final Map<UUID, Integer> previousSendDistances = new HashMap<>();

    public FlagDef_Fog(FlagManager manager, GPFlags plugin) {
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
        if (flag == null || player.hasPermission("gpflags.bypass.fog")) {
            restoreViewDistance(player);
            return;
        }

        previousSendDistances.putIfAbsent(player.getUniqueId(), player.getSendViewDistance());
        player.setSendViewDistance(getDistance(flag.parameters));
    }

    private void restoreViewDistance(Player player) {
        Integer previousDistance = previousSendDistances.remove(player.getUniqueId());
        if (previousDistance != null) {
            player.setSendViewDistance(previousDistance);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        restoreViewDistance(event.getPlayer());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != plugin) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            restoreViewDistance(player);
        }
    }

    @Override
    public SetFlagResult validateParameters(String parameters, @Nullable CommandSender sender) {
        if (parameters.isEmpty()) {
            return new SetFlagResult(true, getSetMessage(String.valueOf(DEFAULT_DISTANCE)));
        }

        try {
            int distance = Integer.parseInt(parameters);
            if (distance < MIN_DISTANCE || distance > MAX_DISTANCE) {
                return new SetFlagResult(false, new MessageSpecifier(Messages.FogDistanceRequired));
            }
            return new SetFlagResult(true, getSetMessage(String.valueOf(distance)));
        } catch (NumberFormatException ignored) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.FogDistanceRequired));
        }
    }

    private int getDistance(String parameters) {
        if (parameters == null || parameters.isEmpty()) return DEFAULT_DISTANCE;
        return Integer.parseInt(parameters);
    }

    @Override
    public String getName() {
        return "Fog";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.FogSet, parameters);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.FogUnSet);
    }
}
