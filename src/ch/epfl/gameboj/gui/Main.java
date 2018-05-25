/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Main extends Application{

    private static double simSpeed = 1;
    
    private static boolean paused = false;
    private static AnimationTimer timer;
    
//    private static final String[] ROM_PATHS = { 
//            "roms/Tetris.gb", //0
//            "roms/2048.gb", //1
//            "roms/snake.gb", //2
//            "roms/tasmaniaStory.gb", //3
//            "roms/flappyboy.gb", //4
//            "roms/DonkeyKong.gb", //5
//            "roms/Bomberman.gb", //6
//            "roms/SuperMarioLand.gb", //7
//            "roms/SuperMarioLand2.gb", //8
//            "roms/LegendofZelda,TheLink'sAwakening.gb", //9
//            };
    
    private static File[] ROM_PATHS = new File("roms").listFiles();;
    
    private Thread saver;
    
    private GameBoy gameboj;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
//========================================= VIEW ======================================================
        
        //++++++++++++++++++++++++++++++++++ Power Scene ++++++++++++++++++++++++++++++++++++++++++++++
        StackPane powerPane = new StackPane();
        ImageView powerBg = new ImageView("file:powerImage.jpg");
        
//        List<String> romNames = List.of(
//                "Tetris",
//                "2048",
//                "Snake",
//                "Tasmania Story",
//                "Flappy Boy",
//                "Donkey Kong",
//                "BomberMan",
//                "Super Mario Land 1", 
//                "Super Mario Land 2",
//                "Legend of Zelda, Link's Awakening");
        List<String> romNames = new LinkedList<>();
        for(int i = 0; i < ROM_PATHS.length; i++) {
            romNames.add(ROM_PATHS[i].getName());
        }

        
        Button power = new Button("POWER");
        power.setDisable(true);
        ChoiceBox<String> romChoice = new ChoiceBox<>(FXCollections.observableArrayList(romNames));
        romChoice.setAccessibleText("salut");
        HBox powerChoice = new HBox(5);
        powerChoice.getChildren().addAll(romChoice, power);
        powerChoice.setAlignment(Pos.CENTER);
        powerChoice.setPadding(new Insets(25, 0, 0, 0));
        
        powerPane.getChildren().addAll(powerBg, powerChoice);
        

        //++++++++++++++++++++++++++++++++++ Gameboy Scene ++++++++++++++++++++++++++++++++++++++++++++
        
        BorderPane mainPane = new BorderPane();
        
        //----------------------------- Menu ----------------------------------------------------------
        VBox menuPane = new VBox();
        int menuWidth = 80;
        menuPane.setMinWidth(menuWidth);
        menuPane.setPadding(new Insets(10));

            //----------------------------- Power Off Button ------------------------------------------
        Button powerOff = new Button("Power Off");
        powerOff.setMaxWidth(Integer.MAX_VALUE);

            //----------------------------- Speed Slider ----------------------------------------------
        VBox speedPane = new VBox();
        speedPane.setAlignment(Pos.CENTER);
        speedPane.setPadding(new Insets(10));

        Slider speedSlider = new Slider(0.2, 4.0, 1);
        Label speedLabel = new Label("Speed");
        speedLabel.setLayoutX(menuWidth/2);
        Label valueLabel = new Label("");
        valueLabel.textProperty().bind(speedSlider.valueProperty().asString("%1$.2f x"));
        speedSlider.valueProperty().addListener(e -> {
            if(!paused) simSpeed = speedSlider.getValue();
        });
        speedPane.getChildren().addAll(speedLabel, valueLabel, speedSlider);


            //----------------------------- Color choosing tabs ----------------------------------------
        TabPane colorTabs = new TabPane();
        colorTabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
                //----------------------------- Preset tab --------------------------------------------
        Tab presetTab = new Tab("Presets");
        GridPane presetPane = new GridPane();
        Button randomize = new Button("Randomize!");
        
        presetPane.addColumn(0,
                new Palette(ColorSet.GAMEBOY, "GAMEBOY"), 
                new Palette(ColorSet.FOREST, "Forest"),
                new Palette(ColorSet.CITY, "City"),
                new Palette(ColorSet.DESERT, "Desert"),
                new Palette(ColorSet.PASTEL, "Pastel"));
        presetPane.addColumn(1,
                new Palette(ColorSet.SEASIDE, "Seaside"),
                new Palette(ColorSet.MOUNTAIN, "Mountain"),
                new Palette(ColorSet.WONDERLAND, "Wonderland"),
                new Palette(ColorSet.INVERTED, "Inverted"),
                randomize);
        presetTab.setContent(presetPane);
        
                //----------------------------- Custom tab ---------------------------------------------
        Tab customTab = new Tab("Custom");
        VBox customPane = new VBox();
        customPane.setPadding(new Insets(10, 0, 0, 0));
        
        Label redLabel   = new Label("Red"),
             greenLabel = new Label("Blue"),
             blueLabel  = new Label("Green"),
             redVLabel   = new Label(), 
             greenVLabel = new Label(),
             blueVLabel  = new Label();
        Slider redSlider = new Slider(0, 1, 1),
             greenSlider = new Slider(0, 1, 1),
             blueSlider = new Slider(0, 1, 1);
        
        redVLabel.textProperty().bind(redSlider.valueProperty().asString("%1$.2f"));
        greenVLabel.textProperty().bind(greenSlider.valueProperty().asString("%1$.2f"));
        blueVLabel.textProperty().bind(blueSlider.valueProperty().asString("%1$.2f"));
        
        Button colorButton = new Button("Change color");
        colorButton.setMaxWidth(Integer.MAX_VALUE);
        VBox.setMargin(colorButton, new Insets(20, 0, 0, 0));
        
        colorTabs.getTabs().addAll(presetTab, customTab);
        
        customPane.getChildren().addAll(
                redLabel,redVLabel,redSlider,
                greenLabel,greenVLabel,greenSlider,
                blueLabel,blueVLabel, blueSlider,
                colorButton);
        customTab.setContent(customPane);
        

        menuPane.getChildren().addAll(powerOff, speedPane, colorTabs);
        mainPane.setLeft(menuPane);
        
        //------------------------------ Gameboy Pane -------------------------------------------------

        Pane backgroundPane = new StackPane();
        backgroundPane.setPadding(new Insets(50));
        
            //------------------------------ Background Image -----------------------------------------
        Image gbImage = new Image("file:gameboy.jpg");
        PixelReader reader = gbImage.getPixelReader();
        WritableImage newGbImage = new WritableImage(reader, 240, 40, 320, 520);
        ImageView resizedGbImage = new ImageView(newGbImage);
        backgroundPane.getChildren().add(resizedGbImage);
        
        
            //------------------------------ Interactive Pane -----------------------------------------
        BorderPane interactivePane = new BorderPane();
        interactivePane.setPadding(new Insets(30));
        backgroundPane.getChildren().add(interactivePane);
        
                //------------------------------ LCD --------------------------------------------------
        ImageView lcd = new ImageView();
        lcd.setFitWidth(LcdController.LCD_WIDTH*1.7);
        lcd.setFitHeight(LcdController.LCD_HEIGHT*1.7);
        lcd.setPreserveRatio(true);
        
        interactivePane.setTop(lcd);

                //------------------------------ JoyPane ----------------------------------------------
        BorderPane joyPane = new BorderPane();
        
        
                    //------------------------------ Arrows -------------------------------------------
        GridPane arrows = new GridPane();
        Rectangle up = new Rectangle(28, 28),
                down = new Rectangle(28, 28),
                right = new Rectangle(28, 28),
                left = new Rectangle(28, 28),
                middle = new Rectangle(28, 28);
        
        arrows.add(up, 1, 0);
        arrows.add(down, 1, 2);
        arrows.add(left, 0, 1);
        arrows.add(right, 2, 1);
        arrows.add(middle, 1, 1);
        
        joyPane.setLeft(arrows);
        
                    //------------------------------ A, B ---------------------------------------------
        AnchorPane buttons = new AnchorPane();
        Circle a = new Circle(20);
        Circle b = new Circle(20);
        
        AnchorPane.setTopAnchor(a, 0.);
        AnchorPane.setLeftAnchor(a, 51.);
        AnchorPane.setTopAnchor(b, 25.);
        AnchorPane.setLeftAnchor(b, 0.);
        
        buttons.getChildren().addAll(a, b);
        buttons.setPadding(new Insets(4, 0, 0, 10));
        
        joyPane.setRight(buttons);
        
                    //------------------------------ start, select ------------------------------------
        HBox options = new HBox();
        Rectangle start = new Rectangle(40, 8);
        start.getTransforms().add(new Rotate(-27));
        Rectangle select = new Rectangle(40, 8);
        select.getTransforms().add(new Rotate(-27));
        
        options.getChildren().addAll(select, start);
        
        options.setSpacing(15.);
        options.setPadding(new Insets(-54, 0, 0, 73));
        options.setMaxHeight(20.);
        
        joyPane.setBottom(options);

        
        List<Shape> joyShapes = List.of(up, down, left, right, a, b, start, select, middle);
        
        joyShapes.forEach(s -> {
            s.setFill(Color.DARKSLATEBLUE);
            s.setLayoutY(LcdController.LCD_HEIGHT/2.0);
        });
        
        joyPane.setPadding(new Insets(37, 0, 0, 2));
        
        interactivePane.setCenter(joyPane);
        
        
        mainPane.setCenter(backgroundPane);
        Scene powerScene = new Scene(powerPane);
        Scene gbScene = new Scene(mainPane);
        
// ================================================ CONTROLER =========================================
        
        // ++++++++++++++++++++++++++++++++++++++++ Power +++++++++++++++++++++++++++++++++++++++++++++
        romChoice.getSelectionModel().selectedIndexProperty()
            .addListener( (ov, v, nv) -> power.setDisable(false));
        
        // ++++++++++++++++++++++++++++++++++++++++ Gameboy +++++++++++++++++++++++++++++++++++++++++++
        
        // ----------------------------------------- Menu ---------------------------------------------
        randomize.setOnAction(e -> { 
            ColorSet.randomize();
            ImageConverter.setColorSet(ColorSet.Random);
        });

        colorButton.setOnAction(e -> {
            ImageConverter.setCustomColors(redSlider.getValue(),
                    greenSlider.getValue(), blueSlider.getValue());
        });
        
        
        // ----------------------------------------- keyboard interaction -----------------------------
        Map<String, Joypad.Key> buttonMap = new HashMap<>(Map.of(
            "a", Joypad.Key.A,
            "b", Joypad.Key.B,
            "s", Joypad.Key.START,
            " ", Joypad.Key.SELECT));
        Map<KeyCode, Joypad.Key> arrowsMap = new HashMap<>(Map.of(
            KeyCode.UP, Joypad.Key.UP,
            KeyCode.DOWN, Joypad.Key.DOWN,
            KeyCode.LEFT, Joypad.Key.LEFT,
            KeyCode.RIGHT, Joypad.Key.RIGHT));
        
        Map<String, Shape> joyButtonMap = new HashMap<>(Map.of(
                "a", a,
                "b", b,
                "s", start,
                " ", select));
        Map<KeyCode, Shape> joyArrowMap = new HashMap<>(Map.of(
                KeyCode.UP, up,
                KeyCode.DOWN, down,
                KeyCode.LEFT, left,
                KeyCode.RIGHT, right));
        

        Runnable togglePause = () -> {
            if (!paused) {
                simSpeed = 0;
                backgroundPane.setOpacity(0.5);
            } else {
                simSpeed = speedSlider.getValue();
                backgroundPane.setOpacity(1);
            }
            paused = !paused;
        };
        
        EventHandler<KeyEvent> keyboardHandler = (e -> {
            
            if(e.getEventType() == KeyEvent.KEY_PRESSED) {
                Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                        arrowsMap.get(e.getCode()));
                if (p != null) {
                    gameboj.joypad().keyPressed(p);

                Shape s = joyButtonMap.getOrDefault(e.getText(),
                        joyArrowMap.get(e.getCode()));
                if (s != null)
                    s.setFill(Color.DEEPPINK);
                }
                
                if(e.getCode().getName() == "P")
                    togglePause.run();
            }
            
            else {
                Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                        arrowsMap.get(e.getCode()));
            if (p != null)
                gameboj.joypad().keyReleased(p);

            Shape s = joyButtonMap.getOrDefault(e.getText(),
                    joyArrowMap.get(e.getCode()));
            if (s != null)
                s.setFill(Color.DARKSLATEBLUE);
            }
        });

        mainPane.addEventFilter(KeyEvent.ANY, e -> {
            keyboardHandler.handle(e);
            e.consume();
        });



// =================================== FINAL SETUP ==========================================
        power.setOnAction((e) -> {
            //TODO : having to give lcd is not clean, but what can I do...
            startGame(romChoice.getValue(), lcd);
            primaryStage.setScene(gbScene);
            primaryStage.centerOnScreen();
        });

        powerOff.setOnAction(e -> {
            endGame(romChoice.getValue());
            primaryStage.setScene(powerScene);
            primaryStage.centerOnScreen();
        });


        primaryStage.setScene(powerScene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("Gameboj");
        primaryStage.show();
        primaryStage.setResizable(false);
        backgroundPane.requestFocus();
    }

    private void startGame(String rom, ImageView lcd) {
      //TODO i dont want this try catch
      try {
          gameboj = new GameBoy(Cartridge.ofFile(new File("roms/" + rom))); //TODO : ugly    
      }
      catch (IOException e) {
          //TODO : do smth
          System.out.println("No such rom");
      }
      
      File saveFile = new File("saves/" + rom.replace(".gb", ".sav"));
      if(saveFile.exists())
          try (InputStream s = new FileInputStream(saveFile)){
              gameboj.cartridge().load(s.readAllBytes());
          }
          catch (IOException e){
              //TODO : qqch
              System.out.println("Problem loading file");
          }
      
      timer = new AnimationTimer() {
      long before = System.nanoTime();
      long gameboyCycles;
      
          @Override
          public void handle(long now) {
              double elapsed = now - before;
              before = now;
              gameboyCycles += (long) (elapsed * GameBoy.CYCLES_PER_NANOSEC * simSpeed);
              gameboj.runUntil(gameboyCycles);
              lcd.setImage(ImageConverter.convert(gameboj.lcdController().currentImage()));
          }
      };
      timer.start();

      saver = new Thread() {
          public void run() {
              endGame(rom);
          }
      };
      
      Runtime.getRuntime().addShutdownHook(saver);
    }
    
    private void endGame(String rom) {
      timer.stop();
      
      byte[] saveData = gameboj.cartridge().save();
      if(saveData != null) {
          File saveFile = new File("saves/" + rom.replace(".gb", ".sav"));
          try(OutputStream s = new FileOutputStream(saveFile)){
              byte[] data = gameboj.cartridge().save();
                  s.write(data);
          }
          catch (IOException e) {
              System.out.println("Problem saving");
              //TODO : Do smth
          }
      }
      Runtime.getRuntime().removeShutdownHook(saver);
    }
}
