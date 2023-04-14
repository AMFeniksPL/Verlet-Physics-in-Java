package main;

import javax.swing.*;

public class GameWindow {
    private JFrame jFrame;
    public GameWindow(GamePanel gamePanel) {
        jFrame = new JFrame();
        jFrame.setLocation(0, 0);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.add(gamePanel);

        jFrame.setResizable(true);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public static void dispose() {
        dispose();

    }
}
