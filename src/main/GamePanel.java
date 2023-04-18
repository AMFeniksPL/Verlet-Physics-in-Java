package main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import addons.FixedArrayList;
import addons.ImageController;
import addons.TimeCounter;
import objectsToDraw.Circle;

public class GamePanel extends JPanel {

    private final short SUBSTEPS = 8;
    private final double startGameTime;
    private static int MAX_BALLS = 44000;
    private int ballCount = 0;

    Circle[] listOfCircle = new Circle[MAX_BALLS + 300];
    ArrayList<Color> listOfColors = new ArrayList<>();

    private final int SIMULATION_X = 1000;
    private final int SIMULATION_Y = 900;

    private final int cellSize = 4;
    private final int cellWidth = (SIMULATION_X / cellSize);
    private final int cellHeight = (SIMULATION_Y / cellSize);
    private FixedArrayList[][] collisionGrid;

//    private CircleConstraints circleConstraint;

    private TimeCounter timer;

    private float[] physics_time;
    private float render_time;

    private ImageController imageController;

    private Game game;

    int threadCount = 20;
    int sliceCount = threadCount * 2;
    int sliceSize = (cellWidth/ sliceCount) * cellHeight;
    public GamePanel(Game game){

        this.game = game;

        startGameTime = System.nanoTime();
        try {
            imageController = new ImageController("res/Phoenix Wallpaper.jpg", "color_positions.txt");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        setPanelSize();
        start();

        timer = new TimeCounter();
        physics_time = new float[4];
        setBackground(Color.black);
    }

    private void setPanelSize() {
        Dimension size = new Dimension(1000, 900);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
    }

    private void start(){
        collisionGrid = new FixedArrayList[cellHeight][cellWidth];
        for (int i = 0; i < cellHeight; i++)
            for (int j = 0; j < cellWidth; j++)
                collisionGrid[i][j] = new FixedArrayList();
        imageController.read_file(listOfColors);
    }


    public void update(){
        timer.start();
        if (ballCount < MAX_BALLS){
            for (int i = 0; i < 110; i++) {
                create_ball(ballCount);
                ballCount++;
            }


        }

        for (int i = 0; i < SUBSTEPS; i++){
            add_objects_to_grid();
//            find_collision_grid();
            find_collision_grid_threaded();
            apply_gravity();
            update_positions(((double)1/60) / SUBSTEPS);
            check_constraint_rectangle();
        }
        check_ending_of_simulation();

    }

    private void create_ball(int t) {
//        double x = (double)GAME_WIDTH / 2 + circleConstraint.getRadius() * Math.cos( 0.3) * Math.pow(-1, t);
//        double y = (double)GAME_HEIGHT/ 2 + circleConstraint.getRadius()  * Math.sin( 2);
        double x = (double)150 + (t * 29 % 450);
//        double y = 50 + (t % 5) * 5;
        double y = 50 + 10 * (t % 7);

        Color color;
        try {color = listOfColors.get(t);}
        catch(Exception e){color = Color.WHITE;}

        listOfCircle[ballCount] = new Circle(x, y, 2, color);
    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);
        for (int i = 0; i < ballCount; i++) {
            listOfCircle[i].draw(g);
        }
    }

    public float[] getPhysics_time(){
        return physics_time;
    }

    public int getBallCount() {
        return ballCount;
    }

    public float getRender_time() {
        return render_time;
    }

    private void check_ending_of_simulation() {
        if (System.nanoTime() - startGameTime > (double)(30) * 1000000000) {
            try {imageController.write_file(listOfCircle, 1000, 900);}
             catch (AWTException | IOException e) {throw new RuntimeException(e);}
        }
    }

    private void draw_collision_grid(Graphics g) {
        g.setColor(new Color(0, 255, 0, 150));
        for (int i = 0; i < cellWidth; i++)
            for (int j = 0; j < cellHeight; j++)
                g.drawRect(i * cellSize, j * cellSize, cellSize, cellSize);
    }

    void apply_gravity(){
        for (int i = 0; i < ballCount; i++) {
            listOfCircle[i].accelerate(0, 50);
        }
    }

    public void solve_collision_naive() {
        double normalized_x, normalized_y;
        for (int i = 0; i < ballCount; i++) {
            Circle object1 = listOfCircle[i];
            for (int j = 0; j < i; j++) {
                Circle object2 = listOfCircle[j];
                double diff_x = object1.getX_cur() - object2.getX_cur();
                double diff_y = object1.getY_cur() - object2.getY_cur();
                double distance = Math.sqrt(diff_x * diff_x + diff_y * diff_y);

                if (distance < object1.getRadius() + object2.getRadius()) {
                    if (distance != 0) {
                        normalized_x = diff_x / distance; normalized_y = diff_y / distance;
                    }
                    else {
                        normalized_x = 0.4; normalized_y = 0;
                    }
                    double delta = object1.getRadius() + object2.getRadius() - distance;
                    object1.setX_cur(object1.getX_cur() + 0.5 * delta * normalized_x);
                    object1.setY_cur(object1.getY_cur() + 0.5 * delta * normalized_y);

                    object2.setX_cur(object2.getX_cur() - 0.5 * delta * normalized_x);
                    object2.setY_cur(object2.getY_cur() - 0.5 * delta * normalized_y);
                }
            }
        }
    }

    public void update_positions(double dt) {
        for (int i = 0; i < ballCount; i++) {
            listOfCircle[i].update_position(dt);
        }
    }

    private void add_objects_to_grid(){
        for (int i = 0; i < cellHeight; i++) {
            for (int j = 0; j < cellWidth; j++) {
                collisionGrid[i][j].clear();
            }
        }
        for (int i = 0; i < ballCount; i++){
            int newH = (int)(listOfCircle[i].getY_cur() / cellSize);
            int newW = (int)(listOfCircle[i].getX_cur() / cellSize);
            collisionGrid[newH][newW].add(i);
        }
    }

    public void find_collision_grid() {
        for (int i = 1; i < this.collisionGrid.length - 1; i++) {
            for (int j = 1; j < this.collisionGrid[0].length - 1; j++) {
                process_cell(i, j);
            }
        }
    }


    private void process_cell(int i, int j){

        FixedArrayList cell = collisionGrid[i][j];
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                FixedArrayList otherCell = collisionGrid[i + di][j + dj];
                solve_collision_between_cells(cell, otherCell);
            }
        }
    }


    public void solve_collision_between_cells(FixedArrayList cell1, FixedArrayList cell2) {

        double normalized_x;
        double normalized_y;
        double diff_x;
        double diff_y;
        double delta;

        for (int index1 : cell1) {
            for (int index2 : cell2) {
                if (index1 != index2){
                    Circle object1 = listOfCircle[index1];
                    Circle object2 = listOfCircle[index2];

                    diff_x = object1.getX_cur() - object2.getX_cur();
                    diff_y = object1.getY_cur() - object2.getY_cur();

                    double distance = Math.sqrt(Math.pow(diff_x, 2) + Math.pow(diff_y, 2));

                    if (distance < object1.getRadius() + object2.getRadius()) {

                        if (distance == 0){
                            distance = 0.1;
                        }
                        normalized_x = diff_x / distance;
                        normalized_y = diff_y / distance;

                        delta = object1.getRadius() + object2.getRadius() - distance;

                        object1.setX_cur(object1.getX_cur() + 0.5 * delta * normalized_x);
                        object1.setY_cur(object1.getY_cur() + 0.5 * delta * normalized_y);

                        object2.setX_cur(object2.getX_cur() - 0.5 * delta * normalized_x);
                        object2.setY_cur(object2.getY_cur() - 0.5 * delta * normalized_y);
                    }
                }
            }
        }
    }
//    public void apply_constraint_circular() {
//        for (Circle circle : listOfCircle) {
//            double distance = Math.sqrt(Math.pow(circle.getX_cur() - circleConstraint.getX(), 2) + Math.pow(circle.getY_cur() - circleConstraint.getY(), 2));
//            if (distance > circleConstraint.getRadius()  - circle.getRadius()) {
//                double n_x = (circle.getX_cur() - circleConstraint.getX()) / distance;
//                double n_y = (circle.getY_cur() - circleConstraint.getY()) / distance;
//                circle.setX_cur(circleConstraint.getX() + n_x * (circleConstraint.getRadius()  - circle.getRadius()));
//                circle.setY_cur(circleConstraint.getY() + n_y * (circleConstraint.getRadius()  - circle.getRadius()));
//            }
//        }
//    }

    public void find_collision_grid_threaded() {

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int sliceIndex = 2 * i;
            executor.submit(() -> solve_collision_threaded( sliceIndex, sliceSize));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int sliceIndex = 2 * i + 1;
            executor.submit(() -> solve_collision_threaded(sliceIndex, sliceSize));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void solve_collision_threaded(int sliceIndex, int sliceSize){

        int start = sliceIndex * sliceSize;
        int end = (sliceIndex + 1) * sliceSize;
        for (int i = start; i < end; i++){
            int newX = Math.min(Math.max(1, i % cellHeight), cellHeight - 2);
            int newY = Math.min(Math.max(1, i / cellHeight), cellWidth - 2);
            process_cell(newX, newY);
        }
    }



    public void check_constraint_rectangle(){
        for (int i = 0; i < ballCount; i++) {
            if (listOfCircle[i].getX_cur() < 100){
                listOfCircle[i].setX_cur(100);
            }
            if (listOfCircle[i].getX_cur() > SIMULATION_X - 120){
                listOfCircle[i].setX_cur(SIMULATION_X - 120);
            }
            if (listOfCircle[i].getY_cur() < 50){
                listOfCircle[i].setY_cur(50);
            }
            if (listOfCircle[i].getY_cur() > SIMULATION_Y- 50){
                listOfCircle[i].setY_cur(SIMULATION_Y - 50);
            }
        }
    }



}
