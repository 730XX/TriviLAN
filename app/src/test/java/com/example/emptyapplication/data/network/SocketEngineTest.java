package com.example.emptyapplication.data.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.emptyapplication.domain.model.Player;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SocketEngineTest {

    @Test
    public void testPlayerModel() {
        Player player = new Player("p1", "Alice");
        assertEquals("p1", player.getId());
        assertEquals("Alice", player.getName());
        assertEquals(0, player.getScore());
        assertEquals(false, player.isFinished());

        player.setScore(100);
        player.setFinished(true);
        assertEquals(100, player.getScore());
        assertTrue(player.isFinished());
    }

    @Test
    public void testNetworkUtilsLocalIp() {
        String ip = NetworkUtils.getLocalIpAddress();
        assertNotNull(ip);
        assertTrue(!ip.isEmpty());
    }

    @Test
    public void testSocketMessageSerialization() {
        SocketMessage message = new SocketMessage(SocketMessage.TYPE_JOIN, "{\"name\":\"Bob\"}");
        String json = message.toJson();
        assertNotNull(json);

        SocketMessage parsed = SocketMessage.fromJson(json);
        assertEquals(SocketMessage.TYPE_JOIN, parsed.getType());
        assertEquals("{\"name\":\"Bob\"}", parsed.getPayload());
    }

    @Test
    public void testServerClientCommunication() throws Exception {
        TriviaServer server = new TriviaServer();
        TriviaClient client = new TriviaClient();

        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<SocketMessage> receivedOnServer = new AtomicReference<>();
        AtomicReference<SocketMessage> receivedOnClient = new AtomicReference<>();

        try {
            server.start(0, new TriviaServer.ServerListener() {
                @Override
                public void onClientConnected(String clientId) {
                    server.sendToClient(clientId, new SocketMessage(SocketMessage.TYPE_LOBBY_UPDATE, "Lobby Ready"));
                }

                @Override
                public void onMessageReceived(String clientId, SocketMessage message) {
                    receivedOnServer.set(message);
                    messageLatch.countDown();
                }

                @Override
                public void onClientDisconnected(String clientId) {}

                @Override
                public void onError(Throwable throwable) {}
            });

            int boundPort = server.getPort();
            assertTrue("Server should bind to valid port", boundPort > 0);

            client.connect("127.0.0.1", boundPort, new TriviaClient.ClientListener() {
                @Override
                public void onConnected() {
                    connectedLatch.countDown();
                }

                @Override
                public void onMessageReceived(SocketMessage message) {
                    receivedOnClient.set(message);
                    client.sendMessage(new SocketMessage(SocketMessage.TYPE_JOIN, "player-test"));
                }

                @Override
                public void onDisconnected() {}

                @Override
                public void onError(Throwable throwable) {}
            });

            assertTrue("Client should connect within 5 seconds", connectedLatch.await(5, TimeUnit.SECONDS));
            assertTrue("Message should be received on server within 5 seconds", messageLatch.await(5, TimeUnit.SECONDS));

            assertNotNull(receivedOnClient.get());
            assertEquals(SocketMessage.TYPE_LOBBY_UPDATE, receivedOnClient.get().getType());

            assertNotNull(receivedOnServer.get());
            assertEquals(SocketMessage.TYPE_JOIN, receivedOnServer.get().getType());
            assertEquals("player-test", receivedOnServer.get().getPayload());
        } finally {
            client.disconnect();
            server.stop();
        }
    }
}
