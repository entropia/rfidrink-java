package de.entropia.rfidrink;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.smartcardio.*;
import javax.smartcardio.Card;
import javax.xml.bind.DatatypeConverter;
import java.util.List;

public class ReadUidApplication {

    private static SocketIOServer server;
    private static CommandAPDU getUidAPDU = new CommandAPDU(0xFF, 0xCA, 0x00, 0x00, 256);

    public static void main(String[] args) throws CardException, InterruptedException {

        Configuration configuration = new Configuration();
        configuration.setHostname("127.0.0.1");
        configuration.setPort(23421);

        server = new SocketIOServer(configuration);
        server.start();

        /*while (true) {
            server.getBroadcastOperations().sendEvent("tag", RfidToken.builder().uid("test").build());
            Thread.sleep(5000);
        }*/


        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Terminals: " + terminals);

        while (true) {

            for(CardTerminal terminal : terminals) {
                if (!terminal.isCardPresent()) {
                    continue;
                }

                Card card = terminal.connect("*");
                CardChannel cardChannel = card.getBasicChannel();

                try {
                    ResponseAPDU responseAPDU = cardChannel.transmit(getUidAPDU);
                    String uid = BaseEncoding.base16().encode(responseAPDU.getData());

                    RfidToken token = RfidToken.builder().uid(uid).build();
                    server.getBroadcastOperations().sendEvent("tag", token);
                    Thread.sleep(2000);
                    cardChannel.close();
                    continue;
                } catch (Exception ignored) {}
            }

            Thread.sleep(100);
        }
    }
}
