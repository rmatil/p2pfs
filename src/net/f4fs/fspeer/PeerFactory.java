package net.f4fs.fspeer;

import net.f4fs.config.Config;
import net.f4fs.util.RandomDevice;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

import java.net.Inet4Address;

/**
 */
public class PeerFactory {
    static PeerDHT DHTPeer() throws Exception {
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(
                Inet4Address.getLocalHost());

        // b.addInterface("eth0");

        return new PeerBuilderDHT(
                new PeerBuilder(new Number160(RandomDevice.INSTANCE.getRand()))
                        .ports(Config.DEFAULT.getPort())
                        .bindings(b)
                        .start())
                .start();
    }
}
