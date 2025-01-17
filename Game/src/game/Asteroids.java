package src.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

class DataStructure {
    int number;
    String word;
    String meaning;
}

public class Asteroids extends Application {
    int score;
    int numCha;
    int numRan; // For challenge pic and letter.

    int totalBullets = 5;

    String srcPath = "C://MY PRJ//Ky 1 Nam 2//OOP//BTLOOPN12-master//Game//src//image//";
    Sprite asteroidChallenge;

    Set<Integer> usedChallenge = new HashSet<Integer>();

    private ArrayList<String> keyPress = new ArrayList<String>();
    private ArrayList<String> keyJustPress = new ArrayList<String>();
    private ArrayList<Sprite> bulletList = new ArrayList<>();
    private ArrayList<Sprite> asteroidsList = new ArrayList<>();
    private ArrayList<DataStructure> dataList = new ArrayList<>();
    private Random random = new Random();

    private int randomInt(int a, int b) {
        if (a > b) {
            throw new IllegalArgumentException("Invalid range");
        }
        int range = b - a + 1;
        if (usedChallenge.size() == range) {
            usedChallenge.clear();
        }
        int randomNum;
        do {
            randomNum = random.nextInt(range) + a;
        } while (usedChallenge.contains(randomNum));
        usedChallenge.add(randomNum);
        return randomNum;
    }

    public void readDatafromTextfile(String filepath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\.");
                if (parts.length == 3) {
                    DataStructure temp = new DataStructure();
                    temp.number = Integer.parseInt(parts[0]);
                    temp.word = parts[1];
                    temp.meaning = parts[2];
                    dataList.add(temp);
                } else {
                    System.out.println("Invalid data format in line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(Stage mainStage) {
        mainStage.setTitle("Asteroids");

        BorderPane root = new BorderPane();
        Scene mainScene = new Scene(root);

        mainStage.setScene(mainScene);

        Canvas canvas = new Canvas(800, 500);
        GraphicsContext context = canvas.getGraphicsContext2D();
        root.setCenter(canvas);

        mainScene.setOnKeyPressed(
                (KeyEvent event) -> {
                    String keyName = event.getCode().toString();
                    if (!keyPress.contains(keyName)) {
                        keyPress.add(keyName);
                        keyJustPress.add(keyName);
                    }
                }
        );

        mainScene.setOnKeyReleased(
                (KeyEvent event) -> {
                    String keyName = event.getCode().toString();
                    keyPress.remove(keyName);
                }
        );

        Sprite background = new Sprite(srcPath + "back.png");
        background.position.set(400, 250);

        Sprite spaceship = new Sprite(srcPath + "spaceship.png");
        spaceship.position.set(50, 200);

        readDatafromTextfile(srcPath + "Data.txt");

        score = 0;
        numCha = 1;
        numRan = randomInt(0, 99);
        final DataStructure[] wordChallenge = {dataList.get(numRan)};

        AnimationTimer gameloop = new AnimationTimer() {

            public void initAsteroidChallenge() {
                if (dataList.isEmpty()) {
                    return;
                }
                String fileName = srcPath + (wordChallenge[0].word.charAt(1) - 96) + ".png";
                asteroidChallenge = new Sprite(fileName);
                double picX = 500 * Math.random() + 300;
                double picY = 300 * Math.random() + 100;
                asteroidChallenge.position.set(picX, picY);
                double angle = 360 * Math.random();
                asteroidChallenge.velocity.setLength(20);
                asteroidChallenge.velocity.setAngle(angle);
            }

            private void initAsteroid() {
                int asteroidsCount = 5;
                for (int i = 0; i < asteroidsCount; i++) {
                    int numPic;

                    do {
                        numPic = randomInt(1, 26);
                    } while (numPic == wordChallenge[0].word.charAt(1) - 96);

                    String namePic = srcPath + numPic + ".png";

                    // Create for new asteroid.
                    Sprite asteroid = new Sprite(namePic);
                    double x = 500 * Math.random() + 300;
                    double y = 300 * Math.random() + 100;

                    // Set speed and local for asteroid.
                    asteroid.position.set(x, y);
                    double angle = 360 * Math.random();
                    asteroid.velocity.setLength(20);
                    asteroid.velocity.setAngle(angle);

                    // Thêm asteroid vào danh sách.
                    asteroidsList.add(asteroid);
                }
            }

            @Override
            public void handle(long nanotime) {
                processUserInput();

                updateGameObjects();

                renderGameObjects(context);

                renderUI(context);

                handleBulletAsteroidCollision();
            }

            public void showOut() {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Out of bullet");
                alert.setHeaderText("Your score: " + score);
                alert.setContentText("Do you want to continue playing?");

                ButtonType playAgain = new ButtonType("Play Again");
                ButtonType quitButton = new ButtonType("Quit", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(playAgain, quitButton);

                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent() && result.get() == playAgain) {
                    resetGame();
                } else {
                    System.exit(0);
                }
            }

            public void resetGame() {
                totalBullets = 5;
                score = 0;

                numCha = 1;
                numRan = randomInt(0, 99);
                wordChallenge[0] = dataList.get(numRan);
                renderUI(context);

                keyPress.clear();
                bulletList.clear();
                asteroidsList.clear();
                spaceship.position.set(50, 200);
                usedChallenge.clear();

                initAsteroidChallenge();
                initAsteroid();
            }

            private void handleBulletAsteroidCollision() {
                for (int bulletNum = bulletList.size() - 1; bulletNum >= 0; bulletNum--) {
                    Sprite bullet = bulletList.get(bulletNum);
                    int asteroidNum = 0;

                    while (asteroidNum < asteroidsList.size()) {
                        Sprite asteroid = asteroidsList.get(asteroidNum);
                        if (bullet.overlaps(asteroid)) {
                            score -= 5;
                            bulletList.remove(bulletNum);
                            totalBullets--;
                            if(totalBullets == 0) {
                                System.out.print("Goi ShowOut");
                                Platform.runLater(this::showOut);

                            }
                        } else if (asteroidChallenge != null && bullet.overlaps(asteroidChallenge)) {
                            // Remove all Challenge Objects.
                            removeChallengeObjects(bulletNum, asteroidNum);

                            // Create new Challenge word.
                            numRan = randomInt(0, 99);
                            DataStructure newChallenge = dataList.get(numRan);
                            wordChallenge[0] = newChallenge;
                            renderUI(context);

                            // Create new Image for asteroids.
                            initAsteroid();
                            initAsteroidChallenge();
                        } else {
                            asteroidNum++;
                        }
                    }
                }
            }

            private void removeChallengeObjects(int bulletNum, int asteroidNum) {
                score += 10;
                numCha++;
                asteroidChallenge = null;
                bulletList.remove(bulletNum);
                asteroidsList.remove(asteroidNum);
                asteroidsList.clear();
                spaceship.position.set(50, 200);
            }


            private void processUserInput() {
                if (keyPress.contains("A")) spaceship.rotation -= 2;
                if (keyPress.contains("D")) spaceship.rotation += 2;
                if (keyPress.contains("W")) {
                    spaceship.velocity.setLength(60);
                    spaceship.velocity.setAngle(spaceship.rotation);
                } else {
                    spaceship.velocity.setLength(0);
                }

                if (keyJustPress.contains("SPACE") && totalBullets > 0) {
                    Sprite bullet = new Sprite(srcPath + "bullet.png");
                    bullet.position.set(spaceship.position.x + 10, spaceship.position.y + 10);
                    bullet.velocity.setLength(150);
                    bullet.velocity.setAngle(spaceship.rotation);
                    bulletList.add(bullet);
                }

                keyJustPress.clear();
            }

            private void updateGameObjects() {
                for (Sprite asteroid : asteroidsList) asteroid.update(1 / 60.0);

                for (int i = 0; i < bulletList.size(); i++) {
                    Sprite bullet = bulletList.get(i);
                    bullet.update(1 / 60.0);
                    if (bullet.elapsedTime > 2.5) bulletList.remove(i);
                }

                spaceship.update(1 / 60.0);

                // Check if asteroidChallenge is not null before updating.
                if (asteroidChallenge != null) {
                    asteroidChallenge.update(1 / 60.0);
                } else {
                    // If asteroidChallenge is null, initialize a new challenge.
                    initAsteroidChallenge();
                    initAsteroid();
                }
            }

            private void renderGameObjects(GraphicsContext context) {
                background.render(context);
                spaceship.render(context);
                for (Sprite bullet : bulletList) bullet.render(context);
                for (Sprite asteroid : asteroidsList) asteroid.render(context);

                // Kiểm tra nếu asteroidChallenge không phải là null trước khi render.
                if (asteroidChallenge != null) {
                    asteroidChallenge.render(context);
                }
            }


            private void renderUI(GraphicsContext context) {

                // Shape: Question.
                context.setFill(Color.BEIGE);
                context.setStroke(Color.BEIGE);
                context.fillRect(0, 350, 800, 150);

                // Text: Score.
                context.setFill(Color.YELLOW);
                context.setStroke(Color.YELLOW);
                context.setFont(new Font("Calibri", 25));
                String scoreText = "Score: " + score;
                int scoreX = 680;
                int scoreY = 40;
                context.fillText(scoreText, scoreX, scoreY);
                context.strokeText(scoreText, scoreX, scoreY);

                // Total Bullet.
                context.setFill(Color.YELLOW);
                String totalBul = "Bullets: " + totalBullets;
                int bulletTextX = 30;
                int bulletTextY = 30;
                context.fillText(totalBul, bulletTextX, bulletTextY);

                // Text: Number of Challenge.
                context.setFill(Color.GREY);
                context.setStroke(Color.RED);
                String numChaText = "Challenge " + numCha + ":";
                int questX = 20;
                int questY = 380;
                context.fillText(numChaText, questX, questY);
                context.strokeText(numChaText, questX, questY);

                // Text: Challenge.
                context.setFill(Color.BLACK);
                context.setStroke(Color.CHOCOLATE);
                context.setFont(new Font("Calibri", 45));
                String newWord = wordChallenge[0].word.charAt(0) + "_" + wordChallenge[0].word.substring(2);
                String wordText = newWord + ": " + wordChallenge[0].meaning;
                int wordX = 150;
                int wordY = 440;
                context.fillText(wordText, wordX, wordY);
                context.strokeText(wordText, wordX, wordY);
            }
        };

        gameloop.start();

        mainStage.show();
    }
}