package com.example.emptyapplication.data.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriviaServer {

    public static final int DEFAULT_PORT = 8888;

    public interface ServerListener {
        void onClientConnected(String clientId);
        void onMessageReceived(String clientId, SocketMessage message);
        void onClientDisconnected(String clientId);
        void onError(Throwable throwable);
    }

    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private ExecutorService serverExecutor;
    private ServerListener listener;

    public synchronized void start(ServerListener listener) {
        start(DEFAULT_PORT, listener);
    }

    public synchronized void start(int port, ServerListener listener) {
        if (isRunning.get()) {
            return;
        }
        this.listener = listener;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            notifyError(e);
            return;
        }
        this.isRunning.set(true);
        this.serverExecutor = Executors.newCachedThreadPool();

        serverExecutor.execute(() -> {
            try {
                while (isRunning.get() && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientId = UUID.randomUUID().toString();
                        ClientHandler handler = new ClientHandler(clientId, clientSocket);
                        clients.put(clientId, handler);
                        if (serverExecutor != null && !serverExecutor.isShutdown()) {
                            serverExecutor.execute(handler);
                        }
                        if (TriviaServer.this.listener != null) {
                            TriviaServer.this.listener.onClientConnected(clientId);
                        }
                    } catch (SocketException e) {
                        if (!isRunning.get() || (serverSocket != null && serverSocket.isClosed())) {
                            break;
                        }
                        notifyError(e);
                    } catch (IOException e) {
                        if (isRunning.get()) {
                            notifyError(e);
                        }
                    }
                }
            } finally {
                stop();
            }
        });
    }

    public void broadcast(SocketMessage message) {
        if (message == null) {
            return;
        }
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    public void sendToClient(String clientId, SocketMessage message) {
        if (clientId == null || message == null) {
            return;
        }
        ClientHandler handler = clients.get(clientId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    public synchronized void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        for (ClientHandler handler : new ArrayList<>(clients.values())) {
            handler.close();
        }
        clients.clear();

        if (serverExecutor != null && !serverExecutor.isShutdown()) {
            serverExecutor.shutdownNow();
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public int getConnectedClientCount() {
        return clients.size();
    }

    public int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : -1;
    }

    private void notifyError(Throwable throwable) {
        if (listener != null) {
            listener.onError(throwable);
        }
    }

    public class ClientHandler implements Runnable {

        private final String clientId;
        private final Socket socket;
        private final AtomicBoolean isClosed = new AtomicBoolean(false);
        private final Object writeLock = new Object();
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(String clientId, Socket socket) throws IOException {
            this.clientId = clientId;
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        public String getClientId() {
            return clientId;
        }

        @Override
        public void run() {
            try {
                String line;
                while (!isClosed.get() && (line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    try {
                        SocketMessage message = SocketMessage.fromJson(trimmed);
                        if (message != null && listener != null) {
                            listener.onMessageReceived(clientId, message);
                        }
                    } catch (Exception e) {
                        notifyError(e);
                    }
                }
            } catch (IOException e) {
                if (!isClosed.get() && isRunning.get()) {
                    notifyError(e);
                }
            } finally {
                close();
            }
        }

        public void sendMessage(SocketMessage message) {
            if (isClosed.get() || message == null) {
                return;
            }
            synchronized (writeLock) {
                if (writer != null && !isClosed.get()) {
                    writer.println(message.toJson());
                    writer.flush();
                }
            }
        }

        public void close() {
            if (!isClosed.compareAndSet(false, true)) {
                return;
            }

            clients.remove(clientId);

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {}

            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {}

            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) {}

            if (listener != null) {
                listener.onClientDisconnected(clientId);
            }
        }
    }
}
