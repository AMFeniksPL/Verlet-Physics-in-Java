package main;

import java.awt.*;

public class Game {
    public final static int GAME_WIDTH = 1600;
    public final static int GAME_HEIGHT = 900;

    private Thread gameThread;
    private final int FPS_SET = 60;
    private final int UPS_SET = 60;

    private GamePanel gamePanel;
    private GameWindow gameWindow;
    public Game(){
        gamePanel = new GamePanel(this);
        gameWindow = new GameWindow(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.requestFocus();

        startGameLoop();
    }


    private void startGameLoop(){
        run();
    }

    public void update(){
        gamePanel.update();
    }

    public void run() {
        double timePerFrame = 1000000000.0 / FPS_SET;
        double timePerUpdate = 1000000000.0 / UPS_SET;

        long previousTime = System.nanoTime();

        int frames = 0;
        int updates = 0;
        long lastCheck = System.currentTimeMillis();

        double deltaU = 0;
        double deltaF = 0;

        while(true){
            long currentTime = System.nanoTime();

            deltaU += (currentTime - previousTime)/ timePerUpdate;
            deltaF += (currentTime - previousTime)/ timePerFrame;
            previousTime = currentTime;

//            if (deltaU >= 1){
//                update();
//                updates++;
//                deltaU--;
//            }

            if(deltaF >= 1){
                gamePanel.repaint();
                frames++;
                deltaF--;
            }


            if(System.currentTimeMillis() - lastCheck >= 1000 ){
                lastCheck = System.currentTimeMillis();


                frames = 0;
                updates = 0;
            }
        }
    }
}
