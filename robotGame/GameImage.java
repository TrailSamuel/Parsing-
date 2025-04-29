package robotGame;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

public enum GameImage {
  Fuel("fuel"),
  RobotBlue1("robot_Blue_1"),
  RobotBlue2("robot_Blue_2"),
  RobotRed1("robot_Red_1"),
  RobotRed2("robot_Red_2"),
  ShieldImage("shield");
  public final BufferedImage img;
  GameImage(String imgName){
    try{ img= ImageIO.read(new File(Main.assetDirectory+imgName+".png")); }
    catch (IOException e) { throw new UncheckedIOException(e); }
  }
  void draw(Graphics2D g, double x, double y){
    int size= WorldComponent.gridSize;
    int intX= (int)(x * size + size / 2d - img.getWidth() / 2d);
    int intY= (int)(y * size + size / 2d - img.getHeight() / 2d);
    g.drawImage(img, intX, intY, null);
  }
  void draw(Graphics2D g,AffineTransform xform){
    xform.translate(-img.getWidth() / 2d, -img.getHeight() / 2d);
    g.drawImage(img, xform, null);
  }
}