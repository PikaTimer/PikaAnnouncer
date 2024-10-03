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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Garner <segfaultcoredump@gmail.Announcer>
 */
public class Announcer extends Application {

    //private final Event event = Event.getInstance(); 
    private static Stage mainStage;

    private static final Logger logger = LoggerFactory.getLogger(Announcer.class);

    public static final String VERSION = "1.0";

    /**
     * SingletonHolder is loaded on the first execution of
     * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {

        private static final Announcer INSTANCE = new Announcer();
    }

    public static Announcer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Stage getPrimaryStage() {
        return mainStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        mainStage = primaryStage;
        primaryStage.setTitle("PikaAnnouncer");

        Pane myPane = (Pane) FXMLLoader.load(getClass().getResource("FXMLAnnouncer.fxml"));
        Scene myScene = new Scene(myPane);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries so that the main screen is centered.                
        primaryStage.setX((primaryScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primaryScreenBounds.getHeight() - primaryStage.getHeight()) / 2);

        // F11 to toggle fullscreen mode
        myScene.getAccelerators().put(new KeyCodeCombination(KeyCode.F11), () -> {
            mainStage.setFullScreen(mainStage.fullScreenProperty().not().get());
        });

//        // Icons
//        String[] sizes = {"256","128","64","48","32"};
//        for(String s: sizes){
//            primaryStage.getIcons().add(new Image("resources/icons/Pika_"+s+".ico"));
//            primaryStage.getIcons().add(new Image("resources/icons/Pika_"+s+".png"));
//        }
        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setScene(myScene);
        primaryStage.show();

        logger.info("Exiting Announcer.start()");

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
