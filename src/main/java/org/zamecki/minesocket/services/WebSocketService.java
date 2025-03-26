package org.zamecki.minesocket.services;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.zamecki.minesocket.config.MineSocketConfiguration;

import java.net.InetSocketAddress;

import static org.zamecki.minesocket.ModData.logger;

public class WebSocketService {
    private final MineSocketConfiguration config;
    private final MessageService messageService;
    private InetSocketAddress address;
    private WebSocketServer wsServer;
    private boolean isRunning = false;

    public WebSocketService(MineSocketConfiguration config, MessageService messageService) {
        this.address = new InetSocketAddress(config.host, config.port);
        this.messageService = messageService;
        this.config = config;
    }

    public boolean isRunning() {
        return wsServer != null && isRunning;
    }

    public boolean tryToStart() {
        if (isRunning()) {
            logger.error("WebSocket server is already running");
            return false;
        }
        try {
            wsServer = new WebSocketServer(address) {
                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    logger.info("New connection from {}", conn.getRemoteSocketAddress());
                }

                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    logger.info("Closed connection to {}", conn.getRemoteSocketAddress());
                }

                @Override
                public void onMessage(WebSocket conn, String message) {
                    logger.info("Received message from {}: {}", conn.getRemoteSocketAddress(), message);
                    messageService.handleMessage(message);
                }

                @Override
                public void onError(WebSocket conn, Exception ex) {
                    logger.error("Error on connection to {}", conn.getRemoteSocketAddress(), ex);
                }

                @Override
                public void onStart() {
                    isRunning = true;
                }
            };
            wsServer.start();
            logger.info("WebSocket server started on {}:{}", address.getHostString(), address.getPort());
            return true;
        } catch (Exception e) {
            logger.error("Error starting WebSocket server: ", e);
            return false;
        }
    }

    public boolean tryToStop() {
        if (!isRunning()) {
            return false;
        }
        try {
            wsServer.stop();
            wsServer = null;
            logger.info("WebSocket server stopped");
            return true;
        } catch (Exception e) {
            logger.error("Error stopping WebSocket server: ", e);
            return false;
        }
    }

    public boolean tryToReload() {
        this.address = new InetSocketAddress(config.host, config.port);
        return tryToStop() && tryToStart();
    }
}
