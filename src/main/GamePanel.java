package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import addons.CollisionList;
import addons.TimeCounter;
import objectsToDraw.Circle;
import objectsToDraw.CircleConstraints;

import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class GamePanel extends JPanel {
    private double lastTimeOfBallSpawn;
    private boolean canCreateBall;
    private final double ballTimeDelay;
    private short substeps = 10;
    private final double startGameTime;
    private static int MAX_BALLS = 11350;
    private int ballCount = 0;




    ArrayList<Circle> listOfCircle = new ArrayList<Circle>();
    ArrayList<Color> listOfColors = new ArrayList<Color>();
    ArrayList<Circle> listOfNewCircle = new ArrayList<Circle>();


    private int SIMULATION_X = 1000;
    private int SIMULATION_Y = 900;

    private final int cellSize = 8;
    private final int cellWidth = (SIMULATION_X / cellSize) + 1;
    private final int cellHeight = (SIMULATION_Y / cellSize) + 1;
    private CollisionList [][] collisionGrid;


    private CircleConstraints circleConstraint;

    private TimeCounter timer;

    private float physics_time;
    private float render_time;




    private Game game;
    public GamePanel(Game game){

        this.game = game;
        circleConstraint = new CircleConstraints(GAME_WIDTH/2, GAME_HEIGHT/2, 425);

        lastTimeOfBallSpawn = System.nanoTime();
        canCreateBall = false;
        ballTimeDelay = 0.02;
        startGameTime = System.nanoTime();

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
        collisionGrid = new CollisionList[cellHeight][cellWidth];
        for (int i = 0; i < cellHeight; i++)
            for (int j = 0; j < cellWidth; j++)
                collisionGrid[i][j] = new CollisionList();
        read_file();
    }

    private void read_file(){
        try {
            File myObj = new File("color_positions.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(" ");
                double x = Double.parseDouble(data[0]);
                double y = Double.parseDouble(data[1]);
                Color c = new Color(Integer.parseInt(data[2]), Integer.parseInt(data[3]),Integer.parseInt(data[4]));
                listOfNewCircle.add(new Circle(x, y, 4, c));
                listOfColors.add(c);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            for (int i = 0; i < 600; i++)
                listOfColors.add(Color.WHITE);
        }
    }

    private void write_file() throws AWTException, IOException {

        BufferedImage screenShot = ImageIO.read(new File("res/Phoenix Wallpaper_3.jpg"));
        int diffX = (screenShot.getWidth() - SIMULATION_X)/2;
        int diffY = (screenShot.getHeight() - SIMULATION_Y)/2;

        try {
            FileWriter myWriter = new FileWriter("color_positions.txt");
            for (Circle circle: listOfCircle){
                myWriter.write((circle.getX_cur()) + " " + (circle.getY_cur()) + " ");

                Color color = new Color(
                        screenShot.getRGB(
                                (int)(circle.getX_cur() + circle.getRadius() + diffX),
                                (int)(circle.getY_cur() + circle.getRadius() + diffY)
                        )
                );





                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                myWriter.write(red + " " + green + " " + blue + " " + "\n");
            }

            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            Robot robot = new Robot();
            BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

            ImageIO.write(screenshot, "png", new File("screenshot.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.exit(0);
    }

    public void update(){
        timer.start();
        if (ballCount < MAX_BALLS){
            for (int i = 0; i < 10; i++) {
                create_ball(ballCount);
                ballCount++;
            }

            lastTimeOfBallSpawn = System.nanoTime();
            canCreateBall = false;
        }
        else if( System.nanoTime() - lastTimeOfBallSpawn > ballTimeDelay){
            canCreateBall = true;
        }

        for (int i = 0; i < substeps; i++){
//            apply_gravity();
//            solve_collision_classic();
            add_objects_to_grid();
            find_collision_grid();
//            apply_constraints();
            apply_gravity();
            update_positions(((double)1/60) / substeps);
            check_constraint_rectangle();
        }
        physics_time = (float) timer.stop_miliseconds();

    }

    private void create_ball(int t) {
//        double x = (double)GAME_WIDTH / 2 + circleConstraint.getRadius() * Math.cos( 0.3) * Math.pow(-1, t);
//        double y = (double)GAME_HEIGHT/ 2 + circleConstraint.getRadius()  * Math.sin( 2);
        double x = (double)150 + (t * 39 % 400);
        double y = 50;
        double randRadius = 4;
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

//        circleConstraint.draw(g);


        for (Circle circle: listOfCircle) {
            circle.draw(g);
        }

        check_ending_of_simulation();
//        draw_collision_grid(g);
        render_time = (float) timer.stop_miliseconds();
//        g.setFont(new Font(Font.DIALOG,  Font.PLAIN, 15));
//        g.setColor(Color.WHITE);
//        g.drawString("Physics time: " + physics_time + " ms", -100, 30);
//        g.drawString("Render time: " + render_time + " ms", 10, 50);
//        g.drawString("Balls count: " + ballCount, 10, 70);

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
        if (System.nanoTime() - startGameTime > (double)(40) * 1000000000) {
            try {write_file();}
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
            circle.accelerate(0, 100);
        }
    }

    public void apply_constraints() {

        for (Circle circle : listOfCircle) {
            double distance = Math.sqrt(Math.pow(circle.getX_cur() - circleConstraint.getX(), 2) + Math.pow(circle.getY_cur() - circleConstraint.getY(), 2));
            if (distance > circleConstraint.getRadius()  - circle.getRadius()) {
                double n_x = (circle.getX_cur() - circleConstraint.getX()) / distance;
                double n_y = (circle.getY_cur() - circleConstraint.getY()) / distance;
                circle.setX_cur(circleConstraint.getX() + n_x * (circleConstraint.getRadius()  - circle.getRadius()));
                circle.setY_cur(circleConstraint.getY() + n_y * (circleConstraint.getRadius()  - circle.getRadius()));
            }
        }
    }
    public void solve_collision_naive() {
        double normalized_x;
        double normalized_y;
        for (int i = 0; i < ballCount; i++) {
            Circle object1 = listOfCircle.get(i);
            for (int j = 0; j < i; j++) {
                Circle object2 = listOfCircle.get(j);
                double diff_x = object1.getX_cur() - object2.getX_cur();
                double diff_y = object1.getY_cur() - object2.getY_cur();
                double distance = Math.sqrt(diff_x * diff_x + diff_y * diff_y);

                if (distance < object1.getRadius() + object2.getRadius()) {
                    if (distance != 0) {
                        normalized_x = diff_x / distance;
                        normalized_y = diff_y / distance;
                    }
                    else {
                        normalized_x = 0.4;
                        normalized_y = 0;
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


//    public static Color get_rainbow(double t) {
//        double r = Math.sin(t);
//        double g = Math.sin(t + 0.33 * 2.0 * Math.PI);
//        double b = Math.sin(t + 0.66 * 2.0 * Math.PI);
//        return new Color((int) (255.0 * r * r), (int) (255.0 * g * g), (int) (255.0 * b * b));
//    }

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
                CollisionList cell = collisionGrid[i][j];
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        CollisionList otherCell = collisionGrid[i + di][j + dj];
                        solve_collision_between_cells(cell, otherCell);
                    }
                }
            }
        }
    }

    public void solve_collision_between_cells(CollisionList cell1, CollisionList cell2) {
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
                            distance = 0.0001;
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
    public void check_constraint_rectangle(){
        for (Circle circle : listOfCircle){
            if (circle.getX_cur() < 100){
                circle.setX_cur(100);
            }
            if (circle.getX_cur() > SIMULATION_X - 100){
                circle.setX_cur(SIMULATION_X - 100);
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
