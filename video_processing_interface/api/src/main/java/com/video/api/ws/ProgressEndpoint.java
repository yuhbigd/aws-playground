package com.video.api.ws;


import com.video.core.port.driver.IUploadVideoService;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnPongMessage;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.vertx.core.buffer.Buffer;
import jakarta.inject.Inject;

@WebSocket(path = "/progress/{taskId}")
public class ProgressEndpoint {

    @Inject
    OpenConnections connections;
    @Inject
    WebSocketConnection webSocketConnection;

    @Inject
    IUploadVideoService uploadVideoService;

    @OnPongMessage
    void pong(Buffer data) {
        System.out.println("Pong received: " + data);
    }

    @OnOpen
    void open() {
        String taskId = webSocketConnection.pathParam("taskId");
        if (!uploadVideoService.canOpenWebSocket(taskId)) {
            throw new RuntimeException("Task id has been processed");
        }
    }

    @OnTextMessage
    void onMessage(WebSocketConnection webSocketConnection) {
        String taskId = webSocketConnection.pathParam("taskId");
        uploadVideoService.consumeProgressEvents(progress -> {
            webSocketConnection.sendTextAndAwait(progress);
        }, taskId);
    }

    @OnClose
    void close() {
        System.out.println("Connection closed");
        String taskId = webSocketConnection.pathParam("taskId");
        uploadVideoService.onCloseWsConnection(taskId);
    }

}