package main;

import javax.swing.*;
import java.awt.*;

public class GameWindow {
    private JFrame jFrame;

    private float physics_time;
    private float render_time;

    private int ballCount;

    public GameWindow(GamePanel gamePanel, InfoPanel infoPanel) {
        jFrame = new JFrame("Symulacja");
        jFrame.setLocation(0, 0);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jFrame.setLayout(new BorderLayout());

        jFrame.add(infoPanel, BorderLayout.WEST);
        jFrame.add(gamePanel, BorderLayout.EAST);

        jFrame.setResizable(true);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public static void dispose() {
        dispose();

    }


}
