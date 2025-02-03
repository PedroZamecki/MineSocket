package org.zamecki.minesocket.services;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.zamecki.minesocket.controller.LangController;

import java.net.InetSocketAddress;

public class WebSocketService extends WebSocketServer {
    Logger logger;
    LangController langController;
    MessageService messageService;

    public WebSocketService(InetSocketAddress address, Logger _logger, LangController _langController, MessageService _messageService) {
        super(address);
        logger = _logger;
        langController = _langController;
        messageService = _messageService;
    }

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
        logger.info("WebSocket server started");
    }
}
