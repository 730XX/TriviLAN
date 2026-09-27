package com.example.emptyapplication.data.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriviaClient {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;

    public interface ClientListener {
        void onConnected();
        void onMessageReceived(SocketMessage message);
        void onDisconnected();
        void onError(Throwable throwable);
    }

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ExecutorService executor;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final Object sendLock = new Object();
    private ClientListener listener;

    public synchronized void connect(String hostIp, int port, ClientListener listener) {
        if (isConnected.get()) {
            disconnect();
        }

        this.listener = listener;
        this.executor = Executors.newCachedThreadPool();

        executor.execute(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(hostIp, port), DEFAULT_CONNECT_TIMEOUT_MS);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                isConnected.set(true);
                if (TriviaClient.this.listener != null) {
                    TriviaClient.this.listener.onConnected();
                }

                String line;
                while (isConnected.get() && (line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    try {
                        SocketMessage message = SocketMessage.fromJson(trimmed);
                        if (message != null && TriviaClient.this.listener != null) {
                            TriviaClient.this.listener.onMessageReceived(message);
                        }
                    } catch (Exception e) {
                        notifyError(e);
                    }
                }
            } catch (IOException e) {
                notifyError(e);
            } finally {
                disconnect();
            }
        });
    }

    public void sendMessage(SocketMessage message) {
        if (!isConnected.get() || message == null) {
            return;
        }

        ExecutorService currentExecutor = this.executor;
        if (currentExecutor == null || currentExecutor.isShutdown()) {
            return;
        }

        try {
            currentExecutor.execute(() -> {
                synchronized (sendLock) {
                    if (writer != null && isConnected.get()) {
                        writer.println(message.toJson());
                        writer.flush();
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            // Ignored if client is shutting down
        }
    }

    public synchronized void disconnect() {
        boolean wasConnected = isConnected.getAndSet(false);

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

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        if (wasConnected && listener != null) {
            listener.onDisconnected();
        }
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    private void notifyError(Throwable throwable) {
        if (listener != null) {
            listener.onError(throwable);
        }
    }
}
