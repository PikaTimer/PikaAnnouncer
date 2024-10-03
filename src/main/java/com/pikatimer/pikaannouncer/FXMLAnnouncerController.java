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
package com.pikatimer.pikaannouncer;

import de.siegmar.fastcsv.reader.CsvReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author John Garner <segfaultcoredump@gmail.com>
 */
public class FXMLAnnouncerController {

    @FXML
    private GridPane rootGridPane;
    @FXML
    private Button loadButton;
    @FXML
    private TextField bibTextField;
    @FXML
    private Label resultLabel;
    @FXML
    private Label participantCountLabel;
    @FXML
    private Button clearListButton;
    @FXML
    private Spinner<Integer> fontSizeSpinner;
    @FXML
    private ToggleButton fullScreenToggleButton;
    @FXML
    private ToggleSwitch autoSyncToggleSwitch;
    @FXML
    private Label autoSyncLabel;

    @FXML
    private ListView<Participant> finisherListView;

    private BooleanProperty autoSyncStatus = new SimpleBooleanProperty(true);

    Preferences prefs = Preferences.userRoot().node("PikaTimer");

    ObservableList<Participant> displayedParticipantsList = FXCollections.observableArrayList();
    Map<String, Participant> participantMap = new HashMap();

    Integer partcicipantCount = 0;

    Integer baseFontSize = 36;

    EventWebSocketClient eventClient;

    Stage primaryStage = Announcer.getInstance().getPrimaryStage();

    private static final Logger logger = LoggerFactory.getLogger(FXMLAnnouncerController.class);

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        // TODO
        loadButton.setOnAction((ActionEvent event) -> {
            loadParticipants();
        });

        clearListButton.setOnAction((ActionEvent event) -> {
            displayedParticipantsList.clear();
        });
        participantCountLabel.setText("0");
        finisherListView.setItems(displayedParticipantsList);
        bibTextField.setOnAction((event) -> {
            showParticipant();
        });

        // if the bib text field looses focus, pull it back
        bibTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(false)) {
                if (!participantMap.isEmpty()) {
                    Platform.runLater(() -> bibTextField.requestFocus());
                }
            }
        });

        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            fullScreenToggleButton.setSelected(newVal);
        });
        fullScreenToggleButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (primaryStage.fullScreenProperty().get() != newVal) {
                primaryStage.setFullScreen(newVal);
            }
        });

        finisherListView.setStyle("-fx-font-size:" + baseFontSize.toString() + ";");

//        Broken due to a JDK bug not fixed until 9
        Platform.runLater(() -> {
            logger.debug("Adding Accelerators");
            primaryStage = Announcer.getInstance().getPrimaryStage();
            primaryStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN), () -> {
                baseFontSize++;
                if (baseFontSize > 99) {
                    baseFontSize = 99;
                }
                logger.info("Font size is now " + baseFontSize.toString());
                //updateFontSize();
                fontSizeSpinner.getValueFactory().setValue(baseFontSize);

            });
            primaryStage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN), () -> {
                baseFontSize--;
                if (baseFontSize < 8) {
                    baseFontSize = 8;
                }
                logger.info("Font size is now " + baseFontSize.toString());
                //updateFontSize();
                fontSizeSpinner.getValueFactory().setValue(baseFontSize);
            });
            logger.debug(primaryStage.getScene().getAccelerators().size() + " Accelerators");
        });

        fontSizeSpinner.setStyle("-fx-font-size: 18;");
        //fontSizeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(14, 99, baseFontSize));
        fontSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            logger.info("New spinner value: " + newValue);
            baseFontSize = newValue;
            updateFontSize();
        });

        finisherListView.setCellFactory(param -> new ListCell<Participant>() {
            Label bib = new Label("");
            Label fullName = new Label("");
            Label age = new Label("");
            Label sex = new Label("");
            Label city = new Label("");
            Label state = new Label("");
            Label country = new Label("");
            Label time = new Label("");
            Label race = new Label("");
            Label note = new Label("");
            Label t = new Label("Time:");

            VBox toVBox = new VBox();
            HBox nameHBox = new HBox();
            HBox detailsHBox = new HBox();
            HBox timeHBox = new HBox();

            @Override
            protected void updateItem(Participant to, boolean empty) {
                super.updateItem(to, empty);

                if (empty || to == null || to.getBib() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    toVBox.setSpacing(0);
                    bib.setText("  (" + to.getBib() + ")");
                    fullName.textProperty().bind(to.fullNameProperty());
                    fullName.setStyle("-fx-font-weight: bold;");
                    nameHBox.setSpacing(8);
                    nameHBox.getChildren().setAll(fullName, sex, age, bib);

                    city.textProperty().bind(Bindings.concat(to.cityProperty(), ","));
                    state.textProperty().bind(to.stateProperty());
                    country.textProperty().bind(to.countryProperty());
                    age.textProperty().bind(to.ageProperty());
                    sex.textProperty().bind(to.sexProperty());
                    note.textProperty().bind(to.noteProperty());
                    time.textProperty().bind(to.timeProperty());
                    race.textProperty().bind(to.raceProperty());
                    note.textProperty().bind(to.noteProperty());

                    detailsHBox.setSpacing(8);
                    detailsHBox.getChildren().setAll(city, state, country);

                    timeHBox.setSpacing(8);
                    timeHBox.getChildren().setAll(t, race, time);
                    timeHBox.managedProperty().bind(time.textProperty().isEmpty().not());
                    timeHBox.visibleProperty().bind(time.textProperty().isEmpty().not());
                    timeHBox.setStyle("-fx-font-weight: bold;");

                    note.managedProperty().bind(note.textProperty().isEmpty().not());
                    note.visibleProperty().bind(note.textProperty().isEmpty().not());

                    toVBox.getChildren().setAll(nameHBox, timeHBox, detailsHBox, note);

                    setText(null);
                    setGraphic(toVBox);

                }
            }
        });

        autoSyncLabel.visibleProperty().bind(autoSyncToggleSwitch.selectedProperty());
        autoSyncLabel.managedProperty().bind(autoSyncToggleSwitch.selectedProperty());
        loadButton.visibleProperty().bind(autoSyncToggleSwitch.selectedProperty().not());
        loadButton.managedProperty().bind(autoSyncToggleSwitch.selectedProperty().not());
        autoSyncToggleSwitch.selectedProperty().addListener((obs, prevVal, newVal) -> {
            if (newVal) {
                autoSyncStatus.setValue(true);
                autoSyncLabel.setText("Searching...");
                autoPikaSync();
            } else {
                autoSyncStatus.setValue(false);
            }

        });
    }

    private void autoPikaSync() {
        // Start a background thread that will 
        // search for a copy of PikaTimer running out there.
        // When it finds one, pull the participants list

        Task pikaSearch = new Task<Void>() {
            @Override
            public Void call() {

                byte[] packetData = "DISCOVER_PIKA_REQUEST".getBytes();

                Boolean pikaFound = false;
                StringProperty pikaURL = new SimpleStringProperty();
                // Find the server using UDP broadcast
                //Loop while the dialog box is open
                // UDP Broadcast code borrowed from https://michieldemey.be/blog/network-discovery-using-udp-broadcast/
                // with a few modifications to protect the guilty and to bring it up to date
                // (e.g., try-with-resources 
                while (!pikaFound && autoSyncStatus.get()) {
                    try (DatagramSocket broadcastSocket = new DatagramSocket()) {
                        broadcastSocket.setBroadcast(true);
                        // 2 second timeout for responses
                        broadcastSocket.setSoTimeout(2000);

                        // Send a packet to 255.255.255.255 on port 8080
                        DatagramPacket probeDatagramPacket = new DatagramPacket(packetData, packetData.length, InetAddress.getByName("255.255.255.255"), 8080);
                        broadcastSocket.send(probeDatagramPacket);

                        logger.debug("Sent UDP Broadcast to 255.255.255.255");
                        // Broadcast the message over all the network interfaces

                        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                        while (interfaces.hasMoreElements()) {
                            NetworkInterface networkInterface = interfaces.nextElement();

                            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                                continue; // Don't want to broadcast to the loopback interface
                            }

                            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                                InetAddress broadcast = interfaceAddress.getBroadcast();
                                if (broadcast == null) {
                                    continue;
                                }
                                // Send the broadcast package!
                                try {
                                    DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, broadcast, 8888);
                                    broadcastSocket.send(sendPacket);
                                    logger.debug("Sent UDP Broadcast to " + broadcast.getHostAddress());
                                } catch (Exception e) {
                                }
                            }
                        }

                        //Wait for a response
                        try {
                            while (true) { // the socket timeout should stop this
                                byte[] recvBuf = new byte[1500]; // mass overkill
                                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                                broadcastSocket.receive(receivePacket);

                                String message = new String(receivePacket.getData()).trim();

                                logger.debug("Pika Finder Response: " + receivePacket.getAddress().getHostAddress() + " " + message);
                                pikaFound = true;
                                pikaURL.set(message);
                                Platform.runLater(() -> {
                                    autoSyncToggleSwitch.disableProperty().set(true);
                                    autoSyncLabel.setText("Syncing with \n" + message);
                                });
                            }
                        } catch (Exception ex) {
                        }

                    } catch (IOException ex) {
                        //Logger.getLogger(this.class.getName()).log(Level.SEVERE, null, ex);
                        logger.debug("oops...", ex);
                    }
                }
                logger.debug("Done scanning for Pikas");
                //Platform.runLater(() -> {scanCompleted.set(true);});
                if (autoSyncStatus.get()) {
                    try {

                        URL url = new URL(pikaURL.getValue() + "/participants/");//your url i.e fetch data from .
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        //conn.setRequestProperty("Accept", "application/json");
                        if (conn.getResponseCode() != 200) {
                            throw new RuntimeException("Failed : HTTP Error code : "
                                    + conn.getResponseCode());
                        }
                        InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader br = new BufferedReader(in);
                        String output;
                        String fullResponse = "";
                        while ((output = br.readLine()) != null) {
                            logger.debug(output);
                            fullResponse += output;
                        }
                        conn.disconnect();
                        JSONObject json = new JSONObject(fullResponse);
                        JSONArray jsonP = json.getJSONArray("Participants");
                        for (int i = 0; i < jsonP.length(); i++) {

                            Participant p = new Participant();
                            p.setFromJSON(jsonP.getJSONObject(i));
                            logger.debug("Map: " + p.getBib() + " -> " + p.fullNameProperty().getValueSafe());
                            participantMap.put(p.getBib(), p);
                            partcicipantCount++;
                            Platform.runLater(() -> {
                                participantCountLabel.setText(partcicipantCount.toString());
                            });
                        }

                        // Start listening for events....
                        String wsPikaURL = pikaURL.getValue().replace("http://", "ws://") + "/eventsocket/";
                        logger.info("Connecting to wsPikaURL: " + wsPikaURL);
                        startEventListener(wsPikaURL);

                    } catch (Exception e) {
                        logger.debug("Exception in NetClientGet: ", e);
                    }
                }

                return null;
            }
        };
        Thread scanner = new Thread(pikaSearch);
        scanner.setDaemon(true);
        scanner.setName("Pika Scanner");
        scanner.start();
    }

    private void setupEventListener(String url) {

    }

    public void startEventListener(String wsPikaURL) {
        try {
            // Start listening for events....
            logger.info("Connecting to wsPikaURL: " + wsPikaURL);
            eventClient = new EventWebSocketClient(wsPikaURL, participantMap, displayedParticipantsList, this);
            eventClient.connectBlocking(60, TimeUnit.SECONDS);

            Platform.runLater(() -> {
                if (eventClient.isOpen()) {
                    autoSyncLabel.setText("Connected to\n" + wsPikaURL);
                }
            });
        } catch (Exception ex) {
            //Logger.getLogger(FXMLAnnouncerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showParticipant() {
        logger.debug("showParticipant() with " + bibTextField.getText());
        String bib = bibTextField.getText();
        if (!bib.isEmpty()) {
            if (participantMap.containsKey(bib)) {
                Participant p = participantMap.get(bib);
                if (displayedParticipantsList.contains(p)) {
                    displayedParticipantsList.remove(p);
                }
                displayedParticipantsList.add(0, p);
                logger.debug("Added " + bib + " -> " + p.toString());
                resultLabel.setText("");
            } else {
                resultLabel.setText("Bib " + bib + " not found");
            }
            bibTextField.setText("");
        }
    }

    private void updateFontSize() {
        //fontSizeSpinner.getValueFactory().setValue(baseFontSize);
        finisherListView.setStyle("-fx-font-size:" + baseFontSize.toString() + ";");
    }

    private void loadParticipants() {
        partcicipantCount = 0;
        participantMap.clear();

        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open CSV File");

        File lastEventFolder = new File(prefs.get("PikaEventHome", System.getProperty("user.home")));
        if (!lastEventFolder.exists()) {
            // we have a minor problem
            lastEventFolder = new File(System.getProperty("user.home"));
        } else if (lastEventFolder.exists() && lastEventFolder.isFile()) {
            lastEventFolder = new File(lastEventFolder.getParent());

        }

        logger.debug("Using initial directory of " + lastEventFolder.getAbsolutePath());

        fileChooser.setInitialDirectory(lastEventFolder);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV/TXT Files", "*.csv", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*")
        );
        File file = fileChooser.showOpenDialog(rootGridPane.getScene().getWindow());
        logger.info("Opening existing file....");
        if (file != null && file.exists() && file.isFile() && file.canRead()) {

            // Let's play the "What type of text file is this..." game
            // Try UTF-8 and see if it blows up on the decode. 
            // If it does, assume that we are dealing with a windows box and CP1252 :-/
            Charset charset = StandardCharsets.UTF_8;
            CharsetDecoder uft8Decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);

            try {
                String result = new BufferedReader(new InputStreamReader(new FileInputStream(file), uft8Decoder)).lines().collect(Collectors.joining("\n"));
            } catch (Exception ex) {
                logger.debug("Not UTF-8: " + ex.getMessage());
                charset = Charset.forName("windows-1252");
            }

            try {
                CsvReader.builder().detectBomHeader(true).ofNamedCsvRecord(file.toPath(), charset).forEach(r -> {

                    Participant p = new Participant();
                    p.setAttributes(r.getFieldsAsMap());
                    logger.debug("Map: " + p.getBib() + " -> " + p.fullNameProperty().getValueSafe());
                    participantMap.put(p.getBib(), p);
                    partcicipantCount++;
                });
            } catch (IOException ex) {

            }

            logger.info("Loaded " + partcicipantCount + " participants");
            participantCountLabel.setText(partcicipantCount.toString());

        }
    }

}
