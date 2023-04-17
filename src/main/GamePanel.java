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
    private double lastTimeOfBallSpawn;
    private boolean canCreateBall;
    private final double ballTimeDelay;
    private final short SUBSTEPS = 6;
    private final double startGameTime;
    private static int MAX_BALLS = 20000;
    private int ballCount = 0;

    ArrayList<Circle> listOfCircle = new ArrayList<Circle>();
    ArrayList<Color> listOfColors = new ArrayList<Color>();

    private final int SIMULATION_X = 1000;
    private final int SIMULATION_Y = 900;

    private final int cellSize = 6;
    private final int cellWidth = (SIMULATION_X / cellSize) + 1;
    private final int cellHeight = (SIMULATION_Y / cellSize) + 1;
    private FixedArrayList[][] collisionGrid;

//    private CircleConstraints circleConstraint;

    private TimeCounter timer;

    private float physics_time;
    private float render_time;

    private ImageController imageController;

    private Game game;

    int threadCount = 4;
    int sliceCount = threadCount * 2;
    int sliceSize = (cellWidth/ sliceCount) * cellHeight;
    public GamePanel(Game game){

        this.game = game;
//        circleConstraint = new CircleConstraints(GAME_WIDTH/2, GAME_HEIGHT/2, 425);

        lastTimeOfBallSpawn = System.nanoTime();
        canCreateBall = false;
        ballTimeDelay = 0.02;
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
            for (int i = 0; i < 30; i++) {
                create_ball(ballCount);
                ballCount++;
            }

            lastTimeOfBallSpawn = System.nanoTime();
            canCreateBall = false;
        }
        else if( System.nanoTime() - lastTimeOfBallSpawn > ballTimeDelay){
            canCreateBall = true;
        }

        for (int i = 0; i < SUBSTEPS; i++){
            add_objects_to_grid();
//            find_collision_grid();
            find_collision_grid_threaded();
            apply_gravity();
            update_positions(((double)1/60) / SUBSTEPS);
            check_constraint_rectangle();
        }
        physics_time = (float) timer.stop_miliseconds();

    }

    private void create_ball(int t) {
//        double x = (double)GAME_WIDTH / 2 + circleConstraint.getRadius() * Math.cos( 0.3) * Math.pow(-1, t);
//        double y = (double)GAME_HEIGHT/ 2 + circleConstraint.getRadius()  * Math.sin( 2);
        double x = (double)150 + (t * 29 % 150);
        double y = 50 + (t % 5) * 5;
        double randRadius = 3;
        Color color;
        try {color = listOfColors.get(t);}
        catch(Exception e){color = Color.WHITE;}

        listOfCircle.add(new Circle(x, y, randRadius, color));
    }


    public void paintComponent(Graphics g){
        update();

        timer.start();
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SIMULATION_X, SIMULATION_Y);

        for (Circle circle: listOfCircle) {
            circle.draw(g);
        }

        check_ending_of_simulation();
//        draw_collision_grid(g);
        render_time = (float) timer.stop_miliseconds();

    }

    public float getPhysics_time(){
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
        for (Circle circle: listOfCircle) {
            circle.accelerate(0, 50);
        }
    }

    public void solve_collision_naive() {
        double normalized_x, normalized_y;
        for (int i = 0; i < ballCount; i++) {
            Circle object1 = listOfCircle.get(i);
            for (int j = 0; j < i; j++) {
                Circle object2 = listOfCircle.get(j);
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
        for (Circle circle : listOfCircle)
            circle.update_position(dt);
    }

    private void add_objects_to_grid(){
        for (int i = 0; i < cellHeight; i++) {
            for (int j = 0; j < cellWidth; j++) {
                collisionGrid[i][j].clear();
            }
        }
        for (int i = 0; i < ballCount; i++){
            int newH = (int)(listOfCircle.get(i).getY_cur() / cellSize);
            int newW = (int)(listOfCircle.get(i).getX_cur() / cellSize);
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
                    Circle object1 = listOfCircle.get(index1);
                    Circle object2 = listOfCircle.get(index2);

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
        for (Circle circle : listOfCircle){
            if (circle.getX_cur() < 100){
                circle.setX_cur(100);
            }
            if (circle.getX_cur() > SIMULATION_X - 120){
                circle.setX_cur(SIMULATION_X - 120);
            }
            if (circle.getY_cur() < 50){
                circle.setY_cur(50);
            }
            if (circle.getY_cur() > SIMULATION_Y- 50){
                circle.setY_cur(SIMULATION_Y - 50);
            }
        }
    }



}
