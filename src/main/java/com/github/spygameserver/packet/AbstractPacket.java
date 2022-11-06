package com.github.spygameserver.packet;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public abstract class AbstractPacket {

    private final int packetId;

    protected AbstractPacket(int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return this.packetId;
    }

    public abstract void process(BufferedReader bufferedReader, BufferedWriter bufferedWriter);

}
