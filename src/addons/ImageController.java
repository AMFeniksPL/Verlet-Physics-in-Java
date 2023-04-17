package addons;

import objectsToDraw.Circle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ImageController {

    private final BufferedImage screenShot;
    private BufferedImage screenshot;
    String fileWithColors;
    public ImageController(String imageToRender, String fileWithColors) throws IOException {
        this.fileWithColors = fileWithColors;
        screenShot = ImageIO.read(new File(imageToRender));
    }

    public void read_file(ArrayList<Color> listOfColors){
        try {
            File myObj = new File(fileWithColors);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(" ");
                double x = Double.parseDouble(data[0]);
                double y = Double.parseDouble(data[1]);
                Color c = new Color(Integer.parseInt(data[2]), Integer.parseInt(data[3]),Integer.parseInt(data[4]));
                listOfColors.add(c);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write_file(Circle[] listOfCircle, int SIMULATION_X, int SIMULATION_Y) throws AWTException, IOException {
        int newWidth = 1600;
        int newHeight = 900;

        BufferedImage newImage = resize(screenShot, newWidth, newHeight);

        int diffX = (newImage.getWidth(null) - SIMULATION_X)/2;
        int diffY = (newImage.getHeight(null) - SIMULATION_Y)/2;
        System.out.println(diffX + " " + diffY);

        try {
            FileWriter myWriter = new FileWriter(fileWithColors);
            for (Circle circle: listOfCircle) {
                if (circle != null) {
                    myWriter.write((circle.getX_cur()) + " " + (circle.getY_cur()) + " ");

                    Color color = new Color(
                            newImage.getRGB(
                                    (int) (circle.getX_cur() + circle.getRadius() + diffX),
                                    (int) (circle.getY_cur() + circle.getRadius() + diffY)
                            )
                    );
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    myWriter.write(red + " " + green + " " + blue + " " + "\n");
                }
                else{
                    break;
                }
            }

            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        make_screenshot();

        System.exit(0);

    }

    private void make_screenshot() {
        try {
            Robot robot = new Robot();
            BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

            ImageIO.write(screenshot, "png", new File("screenshot.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}
