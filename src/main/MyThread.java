package main;

public class MyThread extends Thread {
    private GamePanel gamePanel;
    private int sliceIndex;
    private int sliceSize;

    public MyThread(GamePanel gamePanel, int sliceIndex, int sliceSize) {
        this.gamePanel = gamePanel;
        this.sliceIndex = sliceIndex;
        this.sliceSize = sliceSize;
    }

    @Override
    public void run() {
        gamePanel.solve_collision_threaded(sliceIndex, sliceSize);
    }
}