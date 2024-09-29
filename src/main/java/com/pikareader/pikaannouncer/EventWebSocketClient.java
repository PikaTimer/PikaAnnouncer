/*
 * Copyright (C) 2024 John Garner <segfaultcoredump@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pikareader.pikaannouncer;

/**
 *
 * @author John Garner <segfaultcoredump@gmail.com>
 */
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventWebSocketClient extends WebSocketClient {

    private static Map<String, Participant> participantMap;
    private static ObservableList<Participant> displayedParticipantsList;
    private Integer messageCounter = 0;
    private FXMLAnnouncerController announcerController;
    private String wsURI;

    private static final Logger logger = LoggerFactory.getLogger(EventWebSocketClient.class);

    public EventWebSocketClient(URI serverURI, Map<String, Participant> p, ObservableList<Participant> l) {
        super(serverURI);
        participantMap = p;
        displayedParticipantsList = l;
    }

    EventWebSocketClient(String ws, Map<String, Participant> p, ObservableList<Participant> l, FXMLAnnouncerController mc) throws URISyntaxException {
        super(new URI(ws));
        wsURI = ws;
        participantMap = p;
        displayedParticipantsList = l;
        announcerController = mc;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("New WS connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("WS closed with exit code " + code + " additional info: " + reason);
        announcerController.startEventListener(wsURI);
    }

    @Override
    public void onMessage(String message) {
        logger.debug("Received message #" + messageCounter++ + ": " + message);
        try {
            JSONObject json = new JSONObject(message);
            json.keySet().forEach(k -> {
                logger.debug("JSON Key: " + k);
                String bib;
                switch (k) {
                    case "RESULT":
                        JSONObject result = json.getJSONObject(k);
                        bib = result.optString("Bib");
                        String race = result.optString("Race");
                        String time = result.optString("Time");
                        if (participantMap.containsKey(bib)) {
                            // update on the FX thread to avoid concurrent update issues. 
                            Platform.runLater(() -> participantMap.get(bib).setRaceTime(race, time));
                            logger.debug("Updated: " + bib + " -> " + race + " -> " + time);
                        } else {
                            logger.debug("Result from unknown bib" + bib + " -> " + race + " -> " + time);
                        }
                        ;
                        break;
                    case "PARTICIPANT":
                        Participant p = new Participant();
                        p.setFromJSON(json.getJSONObject(k));
                        if (participantMap.containsKey(p.getBib())) {
                            // update on the FX thread to avoid concurrent update issues. 
                            Platform.runLater(() -> participantMap.get(p.getBib()).setFromJSON(json.getJSONObject(k)));
                            logger.debug("Updated: " + p.getBib() + " -> " + p.fullNameProperty().getValueSafe());
                        } else {
                            participantMap.put(p.getBib(), p);
                            logger.debug("Added: " + p.getBib() + " -> " + p.fullNameProperty().getValueSafe());
                        }
                        ;
                        break;
                    case "ANNOUNCER":
                        bib = json.optString(k);
                        if (!bib.isEmpty() && participantMap.containsKey(bib)) {
                            if (participantMap.containsKey(bib)) {
                                Participant part = participantMap.get(bib);
                                Platform.runLater(() -> {
                                    if (!displayedParticipantsList.contains(part)) {
                                        displayedParticipantsList.add(0, part);
                                    }
                                });
                                logger.debug("Added " + bib + " -> " + part.toString());
                            }

                        } else {
                            logger.debug("Unknown bib " + bib);
                        }
                }

            });
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        logger.debug("received ByteBuffer: {}", message.toString());
    }

    @Override
    public void onError(Exception ex) {
        logger.debug("an error occurred:" + ex);
    }

}
