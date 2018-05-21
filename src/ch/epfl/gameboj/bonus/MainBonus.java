/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.util.List;

import ch.epfl.gameboj.bonus.save.Cartridge;
import ch.epfl.gameboj.bonus.save.GameBoy;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.gui.ImageConverter;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

public class MainBonus extends Application{

    private static DoubleProperty simSpeedProperty = new SimpleDoubleProperty(1);
    private static boolean paused = false;
    
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
    
    public  GameBoy gameboj;
    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        //++++++++++++++++++++++++++++++++++ INTRO WINDOW ++++++++++++++++++++++++
        StackPane powerPane = new StackPane();
        ImageView powerBg = new ImageView("file:powerImage.jpg");
        
        List<String> romNames = List.of("Tetris", "2048", "Snake", "Tasmania Story", "Flappy Boy", "Donkey Kong", "BomberMan", "Super Mario Land 1", 
                "Super Mario Land 2", "Legend of Zelda, Link's Awakening");
        
        Button power = new Button("POWER");
        power.setDisable(true);
        ChoiceBox romChoice = new ChoiceBox(FXCollections.observableArrayList(romNames));
        HBox powerChoice = new HBox();
        powerChoice.getChildren().addAll(romChoice, power);
        powerChoice.setAlignment(Pos.CENTER);
        powerChoice.setPadding(new Insets(25, 0, 0, 0));
        
        powerPane.getChildren().addAll(powerBg, powerChoice);
        
        //++++++++++++++++++++++++++++++++++ LAYOUT +++++++++++++++++++++++++++
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefHeight(LcdController.LCD_HEIGHT*2);
        
        //----------------------------- LEFT --------------------------------
        VBox menuPane = new VBox();
        menuPane.setPadding(new Insets(5));
        int menuWidth = 80;
        menuPane.setMinWidth(menuWidth);
        menuPane.setPadding(new Insets(10));

//        VBox buttonPane = new VBox(); 
//        buttonPane.setAlignment(Pos.CENTER);
//        Button startButton = new Button("Start");
//        ToggleButton pauseButton = new ToggleButton("Pause");
//        buttonPane.getChildren().addAll(startButton, pauseButton);
//        
        
        VBox speedPane = new VBox();
        speedPane.setAlignment(Pos.CENTER);
        speedPane.setPadding(new Insets(10));
        Slider speedSlider = new Slider(0.2, 4.0, 1);
        Label speedLabel = new Label("Speed");
        speedLabel.setLayoutX(menuWidth/2);
        Label valueLabel = new Label("");
        valueLabel.textProperty().bind(speedSlider.valueProperty().asString("%1$.2f x"));
        simSpeedProperty.bind(speedSlider.valueProperty());
        speedPane.getChildren().addAll(speedLabel, valueLabel, speedSlider);
        
        TabPane colorTabs = new TabPane();
        colorTabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        Tab customTab = new Tab("Custom");
        VBox colorMenuPane = new VBox();
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
        
        Button colorButton = new Button("change color");
        colorButton.setOnAction(e -> {
            ImageConverter.setCustomColors(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
        });
        colorMenuPane.getChildren().addAll(
                redLabel,redVLabel,redSlider,
                greenLabel,greenVLabel,greenSlider,
                blueLabel,blueVLabel, blueSlider,
                colorButton);
        customTab.setContent(colorMenuPane);
        
        Tab presetTab = new Tab("Presets");
        GridPane palettePane = new GridPane();
        Button randomize = new Button("Randomize!");
        randomize.setOnAction(e -> { 
            ColorSet.randomize();
            ImageConverter.setColorSet(ColorSet.Random);
        });
        palettePane.addColumn(0,
                new Palette(ColorSet.GAMEBOY, "GAMEBOY"), 
                new Palette(ColorSet.FOREST, "Forest"),
                new Palette(ColorSet.CITY, "City"),
                new Palette(ColorSet.DESERT, "Desert"),
                new Palette(ColorSet.PASTEL, "Pastel"));
        palettePane.addColumn(1,
                new Palette(ColorSet.SEASIDE, "Seaside"),
                new Palette(ColorSet.MOUNTAIN, "Mountain"),
                new Palette(ColorSet.WONDERLAND, "Wonderland"),
                new Palette(ColorSet.INVERTED, "Inverted"),
                randomize);
        presetTab.setContent(palettePane);
        colorTabs.getTabs().addAll(presetTab, customTab);
        
//        menuPane.getChildren().addAll(buttonPane, speedPane, colorTabs);
        menuPane.getChildren().addAll(speedPane, colorTabs);
        mainPane.setLeft(menuPane);
        
        //------------------------------ CENTER -----------------------------

        // TODO TODO j'arrive pas a utiliser le path local...
        Pane backgroundPane = new StackPane();
        backgroundPane.setPadding(new Insets(50));
        
        Image gbImage = new Image("https://d3nevzfk7ii3be.cloudfront.net/igi/CbGZEBGdw52JMsnB.large");
        PixelReader reader = gbImage.getPixelReader();
        WritableImage newGbImage = new WritableImage(reader, 240, 40, 320, 520);
        ImageView resizedGbImage = new ImageView(newGbImage);
        backgroundPane.getChildren().add(resizedGbImage);
        
        
        BorderPane interactivePane = new BorderPane();
        backgroundPane.getChildren().add(interactivePane);

        ImageView lcd = new ImageView();
        lcd.setFitWidth(LcdController.LCD_WIDTH*1.7);
        lcd.setFitHeight(LcdController.LCD_HEIGHT*1.7);
        lcd.setPreserveRatio(true);
        
        interactivePane.setPadding(new Insets(30));
        interactivePane.setTop(lcd);

        BorderPane joyPane = new BorderPane();
        
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
        
        //++++++++++++++++++++++++++++++++++++++++++ CONTROLLER +++++++++++++++++++++++++++++++++++++
        
        // ------------------------------------------------------ gameboj simulation ---------------------------------
        AnimationTimer timer = new AnimationTimer()
        {
        long before = System.nanoTime();
        long gameboyCycles;
        
            @Override
            public void handle(long now) {
                double deltaTime = (now - before);
                before = now;
                gameboyCycles += (long) (deltaTime * GameBoy.CYCLES_PER_NANOSEC * simSpeedProperty.doubleValue());
                gameboj.runUntil(gameboyCycles);
                lcd.setImage(ImageConverter
                        .convert(gameboj.lcdController().currentImage()));
//                if(!pauseButton.isSelected())
//                    simSpeed = speedSlider.getValue();
            }
        };
        
        // --------------------------------- Power scene handling ----------------------------
//        romChoice.setOn
        romChoice.getSelectionModel().selectedIndexProperty()
            .addListener( (ov, v, nv) -> power.setDisable(false));
        
        power.setOnAction((e) -> {
            romPath = ROM_PATHS[(int) romChoice.getSelectionModel().getSelectedIndex()];
            //TODO i dont want this try catch
            try {
                gameboj = new GameBoy(Cartridge.ofFile(new File(romPath)));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            lcd.setImage(ImageConverter.convert(gameboj.lcdController().currentImage()));
            timer.start();
            primaryStage.setScene(gbScene);
            primaryStage.centerOnScreen();
        });

        // ----------------------------------------- keyboard interaction -------------------------------
        Map<String, Joypad.Key> buttonMap = new HashMap<>(Map.of(
            "a", Joypad.Key.A,
            "b", Joypad.Key.B,
            "s", Joypad.Key.START,
            " ", Joypad.Key.SELECT));
        Map<KeyCode, Joypad.Key> joystickMap = new HashMap<>(Map.of(
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
        
//        Runnable togglePause = ()-> {
//            if(!pauseButton.isSelected()) {
//                simSpeed = 0;
//                pauseButton.setSelected(true);
//                backgroundPane.setOpacity(0.5);}
//            else {
//                simSpeed = speedSlider.getValue();
//                pauseButton.setSelected(false);
//                backgroundPane.setOpacity(1);
//            }
//            };
            Runnable togglePause = ()-> {
                if(!paused) {
                    simSpeedProperty.unbind();
                    simSpeedProperty.set(0);
                    backgroundPane.setOpacity(0.5);
                }
                else {
                    simSpeedProperty.bind(speedSlider.valueProperty());
                    backgroundPane.setOpacity(1);
                }
                
                paused = !paused;
            };
//            
//        pauseButton.setOnAction(e -> togglePause.run());
        
        EventHandler<KeyEvent> keyboardHandler = (e -> {
            
            if(e.getEventType() == KeyEvent.KEY_PRESSED) {
                Joypad.Key p = buttonMap.getOrDefault(e.getText(),
                        joystickMap.get(e.getCode()));
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
                        joystickMap.get(e.getCode()));
            if (p != null)
                gameboj.joypad().keyReleased(p);
            
            Shape s = joyButtonMap.getOrDefault(e.getText(),
                    joyArrowMap.get(e.getCode()));
            if (s != null)
                s.setFill(Color.DARKSLATEBLUE);
            }
            
            e.consume();   
            
        });
        
        //had to implement a filter otherwise the slider was receiving the bubbling event
        mainPane.addEventFilter(KeyEvent.ANY, e -> {
            keyboardHandler.handle(e);
            e.consume();
        });
        
        
        
        // +++++++++++++++++++++++++++++++ FINAL SETUP +++++++++++++++++++++++
            
        primaryStage.setScene(powerScene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("gameboj");
        primaryStage.show();
        primaryStage.setResizable(false);
        backgroundPane.requestFocus();
    }

}
