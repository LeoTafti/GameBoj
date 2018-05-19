/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.bonus;

import java.io.File;
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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
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

    private static double simSpeed = 1.0;
    
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
    
    private static String romPath = ROM_PATHS[5];
    
    public static GameBoy gameboj;
    
    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        //construct gameboy with given ROM
        File romFile = new File(romPath);
        gameboj = new GameBoy(Cartridge.ofFile(romFile));
        
        //++++++++++++++++++++++++++++++++++ INTRO TAB ++++++++++++++++++++++++
        
        
        
        //++++++++++++++++++++++++++++++++++ LAYOUT +++++++++++++++++++++++++++
        BorderPane mainPane = new BorderPane();
        mainPane.setPrefHeight(LcdController.LCD_HEIGHT*2);
        
        //----------------------------- LEFT --------------------------------
        VBox menuPane = new VBox();
        menuPane.setPadding(new Insets(5));
        int menuWidth = 80;
        menuPane.setMinWidth(menuWidth);
        menuPane.setPadding(new Insets(10));

        VBox buttonPane = new VBox(); 
        buttonPane.setAlignment(Pos.CENTER);
        Button startButton = new Button("Start");
        ToggleButton pauseButton = new ToggleButton("Pause");
        buttonPane.getChildren().addAll(startButton, pauseButton);
        
        
        VBox speedPane = new VBox();
        speedPane.setAlignment(Pos.CENTER);
        Slider speedSlider = new Slider(0.2, 4.0, 1);
        Label speedLabel = new Label("Speed");
        speedLabel.setLayoutX(menuWidth/2);
        Label valueLabel = new Label("");
        valueLabel.textProperty().bind(speedSlider.valueProperty().asString("%1$.2f x"));
        speedPane.getChildren().addAll(speedLabel, valueLabel, speedSlider);
        
        TabPane colorTabs = new TabPane();
        Tab customTab = new Tab("custom");
        VBox colorMenuPane = new VBox();
        Label colorLabel = new Label("Colorize!"),
             redLabel   = new Label("Red"),
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
        colorMenuPane.getChildren().addAll(colorLabel,
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
//        menuPane.getChildren().add(colorTabs);
        
        menuPane.getChildren().addAll(buttonPane, speedPane, colorTabs);
        mainPane.setLeft(menuPane);
        
        //------------------------------ CENTER -----------------------------
        
//        VBox backgroundPane = new VBox();
//        backgroundPane.setPadding(new Insets(20));
        // TODO TODO j'arrive pas a utiliser le path local...
//        BackgroundImage gbImage = new BackgroundImage(new Image("https://d3nevzfk7ii3be.cloudfront.net/igi/CbGZEBGdw52JMsnB.large"),
//                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
//        backgroundPane.setBackground(new Background(gbImage));
        
        Pane backgroundPane = new StackPane();
        backgroundPane.setPadding(new Insets(50));
        
        Image gbImage = new Image("https://d3nevzfk7ii3be.cloudfront.net/igi/CbGZEBGdw52JMsnB.large");
        PixelReader reader = gbImage.getPixelReader();
        WritableImage newGbImage = new WritableImage(reader, 240, 40, 320, 520);
        ImageView resizedGbImage = new ImageView(newGbImage);
        backgroundPane.getChildren().add(resizedGbImage);
        
        
        BorderPane interactivePane = new BorderPane();
        backgroundPane.getChildren().add(interactivePane);
//        BackgroundImage gbImage = new BackgroundImage(new Image("https://d3nevzfk7ii3be.cloudfront.net/igi/CbGZEBGdw52JMsnB.large"),
//              BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
//        backgroundPane.setBackground(new Background(gbImage));
                
        
//        backgroundPane.setStyle("-fx-background-image : url(roms/gameboy.jpeg);-fx-background-repeat: stretch;");

        ImageView lcd = new ImageView();
        lcd.setFitWidth(LcdController.LCD_WIDTH*1.7);
        lcd.setFitHeight(LcdController.LCD_HEIGHT*1.7);
        lcd.setPreserveRatio(true);
        lcd.setImage(ImageConverter.convert(gameboj.lcdController().currentImage()));
        
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
//        GridPane buttons = new GridPane();
        Circle a = new Circle(20);
        Circle b = new Circle(20);
        
        AnchorPane.setTopAnchor(a, 0.);
        AnchorPane.setLeftAnchor(a, 51.);
        AnchorPane.setTopAnchor(b, 25.);
        AnchorPane.setLeftAnchor(b, 0.);
        
        buttons.getChildren().addAll(a, b);
        buttons.setPadding(new Insets(4, 0, 0, 10));
//        buttons.add(a, 1, 0);
//        buttons.add(b, 0, 1);
        
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
//        joyPane.setAlignment(options, Pos.CENTER);
//        Pane joyPane = new Pane();
//        
//        int shortS = 15, longS = 25, radius = 15, middleMargin = 30, topMargin = 20, interSpace = 10;
//        
//        Rectangle up = new Rectangle( interSpace*2+longS, topMargin, shortS, longS),
//            down     = new Rectangle( interSpace*2+longS, topMargin + interSpace*2 + longS + shortS, shortS, longS),
//            left     = new Rectangle( interSpace, topMargin + interSpace + longS, longS, shortS),
//            right    = new Rectangle( interSpace*3 + longS + shortS, topMargin + interSpace + longS, longS, shortS);
//        
//        Circle a   = new Circle(interSpace*2 + longS*2 + shortS + middleMargin + radius + interSpace , topMargin*2 + radius , radius),
//            b      = new Circle(interSpace*2 + longS*2 + shortS + middleMargin , topMargin*2 + interSpace + shortS + longS, radius),
//            select = new Circle(interSpace*4 + longS + shortS, topMargin + interSpace*2 + longS*3 + shortS, radius/2),
//            start  = new Circle(interSpace*3 + longS + shortS + middleMargin, topMargin + interSpace*2 + longS*3 + shortS, radius/2);
//        
        List<Shape> joyShapes = List.of(up, down, left, right, a, b, start, select, middle);
        
        joyShapes.forEach(s -> {
            s.setFill(Color.DARKSLATEBLUE);
            s.setLayoutY(LcdController.LCD_HEIGHT/2.0);
        });
        
        joyPane.setPadding(new Insets(37, 0, 0, 2));
        interactivePane.setCenter(joyPane);
        
//        backgroundPane.setMinHeight(LcdController.LCD_HEIGHT*4);
        
//        joyPane.translateXProperty().bind(lcd.fitWidthProperty().subtract(LcdController.LCD_WIDTH).divide(2));
       
        
        //responsiveness attempt
//        double lcdRatio = (LcdController.LCD_HEIGHT/(double)LcdController.LCD_WIDTH);
//        lcdPane.fitWidthProperty().bind(backgroundPane.widthProperty());
//        lcdPane.fitHeightProperty().bind(lcdPane.fitWidthProperty().multiply(lcdRatio));
        
//        joyPane.minWidthProperty().bind(lcdPane.fitWidthProperty());
//        joyPane.maxWidthProperty().bind(lcdPane.fitWidthProperty());
//        joyPane.minHeightProperty().bind(lcdPane.fitHeightProperty());
//        joyPane.maxHeightProperty().bind(lcdPane.fitHeightProperty());
        
//        joyPane.translateYProperty().bind(lcdPane.fitHe);
//        joyPane.scaleXProperty().bind(lcdPane.fitWidthProperty().divide(LcdController.LCD_WIDTH));
//        joyPane.scaleYProperty().bind(lcdPane.fitHeightProperty().divide(LcdController.LCD_HEIGHT).multiply(lcdRatio));
//        joyPane.set
        
        mainPane.setCenter(backgroundPane);

        
        //++++++++++++++++++++++++++++++++++++++++++ CONTROLLER +++++++++++++++++++++++++++++++++++++

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
        
        Runnable togglePause = ()-> {
            if(!pauseButton.isSelected()) {
                simSpeed = 0;
                pauseButton.setSelected(true);
                backgroundPane.setOpacity(0.5);}
            else {
                simSpeed = speedSlider.getValue();
                pauseButton.setSelected(false);
                backgroundPane.setOpacity(1);
            }
            };
        //why not??
        pauseButton.setOnAction(e -> {togglePause.run();
        System.out.println("coucou");
        }
        );
        
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
            
        
        // ------------------------------------------------------ gameboj simulation ---------------------------------
        AnimationTimer timer = new AnimationTimer()
        {
        long before = System.nanoTime();
        long gameboyCycles;
        
            @Override
            public void handle(long now) {
                double deltaTime = (now - before);
                before = now;
                gameboyCycles += (long) (deltaTime * GameBoy.CYCLES_PER_NANOSEC * simSpeed);
                gameboj.runUntil(gameboyCycles);
                lcd.setImage(ImageConverter
                        .convert(gameboj.lcdController().currentImage()));
                if(!pauseButton.isSelected())
                simSpeed = speedSlider.getValue();
            }
        };
        timer.start();
        
        
        Scene scene = new Scene(mainPane);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("gameboj");
        primaryStage.show();
        backgroundPane.requestFocus();
    }

}
