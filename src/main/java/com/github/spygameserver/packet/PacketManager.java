package com.github.spygameserver.packet;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.packet.auth.ServerHandshakePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketManager {

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    private final Map<Integer, Supplier<AbstractPacket>> packetIdToPacketMap;

    public PacketManager(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;

        this.packetIdToPacketMap = new HashMap<>();

        // Authentication packets
        registerPacket(ServerHandshakePacket::new);

        // Game lobby and leaderboard packets
        registerPacket(JoinGamePacket::new);
        registerPacket(LeaderboardPacket::new);
        registerPacket(ShowPublicGamesPacket::new);
    }

    private void registerPacket(Supplier<AbstractPacket> abstractPacketSupplier) {
        int packetId = abstractPacketSupplier.get().getPacketId();

        packetIdToPacketMap.put(packetId, abstractPacketSupplier);
    }

    public AbstractPacket getNewPacket(int packetNumber) {
        Supplier<AbstractPacket> abstractPacketSupplier = packetIdToPacketMap.get(packetNumber);
        return abstractPacketSupplier == null ? null : abstractPacketSupplier.get();
    }

    public GameDatabase getGameDatabase() {
        return this.gameDatabase;
    }

    public AuthenticationDatabase getAuthenticationDatabase() {
        return this.authenticationDatabase;
    }

}
