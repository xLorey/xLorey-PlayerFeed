package io.xlorey.playerfeed.handlers;

import io.xlorey.fluxloader.events.OnCharacterDeath;
import io.xlorey.fluxloader.server.api.ServerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.playerfeed.PluginUtils;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

/**
 * Author: Deknil
 * Date: 19.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handles player death events and notifies other players.
 * <p> xLoreyPlayerFeed © 2024. All rights reserved. </p>
 */
public class OnPlayerDeathHandler extends OnCharacterDeath {
    @Override
    public void handleEvent(IsoGameCharacter character) {
        if (!(character instanceof IsoPlayer player)) return;

        IsoPlayer attacker = null;

        if (character.getAttackedBy() instanceof IsoPlayer) {
            attacker = (IsoPlayer) character.getAttackedBy();
        }


        String deathText;
        UdpConnection connectionPlayer = GameServer.getConnectionFromPlayer(player);
        if (attacker == null) {
            if (connectionPlayer != null) {
                Logger.print(String.format("PF > Player '%s' (IP: %s | SteamID: %s) died", player.username, connectionPlayer.ip, connectionPlayer.steamID));
            } else {
                Logger.print(String.format("PF > Player '%s' died",
                        player.username
                ));
            }

            deathText = PluginUtils.getMessageText("notify.playerDeath");
        } else{
            UdpConnection connectionAttacker = GameServer.getConnectionFromPlayer(attacker);
            if (connectionPlayer != null && connectionAttacker != null) {
                Logger.print(String.format("PF > Player '%s' (IP: %s | SteamID: %s) died in battle with '%s' (IP: %s | SteamID: %s)",
                        player.username, connectionPlayer.ip, connectionPlayer.steamID,
                        attacker.username, connectionAttacker.ip, connectionAttacker.steamID
                ));
            } else {
                Logger.print(String.format("PF > Player '%s' died in battle with '%s'",
                        player.username,
                        attacker.username
                ));
            }
            deathText = PluginUtils.getMessageText("notify.playerDeathPvp");
            deathText = deathText.replace("<ATTACKER>", attacker.getUsername());
        }

        deathText = PluginUtils.handleText(deathText, player.getUsername());

        if (deathText.isEmpty()) return;

        ServerUtils.sendServerChatMessage(deathText);
    }
}
