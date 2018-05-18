/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus;

import java.io.File;
import java.io.IOException;

import com.sun.tools.javac.util.List;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jdk.internal.joptsimple.internal.Strings;

public class Power extends Application{
    
    
    private static final String[] ROM_PATHS = { 
            "roms/Tetris.gb", //0
            "roms/2048.gb", //1
            "roms/snake.gb", //2
            "roms/tasmaniaStory.gb", //3
            "roms/flappyboy.gb", //4
            "roms/DonkeyKong.gb", //5
            "roms/Bomberman.gb", //6
            "roms/SuperMarioLand.gb", //7
            "roms/SuperMarioLand2.gb", //8
            "roms/LegendofZelda,TheLink'sAwakening.gb", //9
            };
    private static String romPath;
    
    public static void initiateGameboy() {
        
        File romFile = new File(romPath);
        try {
            MainBonus.gameboj = new GameBoy(Cartridge.ofFile(romFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) {
        launch(args);
     }

    @Override
    public void start(Stage powerStage) throws Exception {
        VBox introPane = new VBox();
        List<String> romNames = List.of("Tetris", "2048", "Snake" );
        Button power = new Button("POWER");
        power.setDisable(true);
        ChoiceBox romChoice = new ChoiceBox(FXCollections.observableArrayList(romNames));
        romChoice.getSelectionModel().selectedIndexProperty().addListener( new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov,
                    Number value, Number newValue) {
                romPath = ROM_PATHS[(int) newValue];
                power.setDisable(false);
                
            }
        });
        introPane.getChildren().addAll(romChoice, power);
        

        power.setOnAction(e -> {
            MainBonus.main(new String[0]);
        try {
            this.finalize();
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }});
        
        Scene scene = new Scene(introPane);
        powerStage.setScene(scene);
        powerStage.sizeToScene();
        powerStage.setTitle("gameboj");
        powerStage.show();
    }

}
