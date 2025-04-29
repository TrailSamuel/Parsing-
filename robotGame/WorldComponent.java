package robotGame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import robotGame.RobotBody.Colour;

@SuppressWarnings("serial")
class WorldComponent extends JComponent{
  public static final int gridSize= 50;
  public static final int gridTot= gridSize*(Point.coordSize);
  private static final int animationDelay= 20;
  private static final int worldUpdateDelay= 33;
  private World world;
  private Timer timer= new Timer();
  private int tick= 0;
  WorldComponent(){ setPreferredSize(new Dimension(gridTot, gridTot)); }
  void start() {
    world = new World(redCode, blueCode);
    timer.cancel();
    timer = new Timer();
    timer.schedule(new AnimationTask(), 0, animationDelay);
  }
  public void reset(){
    timer.cancel();
    world = new World(redCode, blueCode);
    repaint();
  }
  //Caches the last used code
  private Path redCode;
  private Path blueCode;
  public void loadRobotProgram(Colour c, Path code){
    if (c == Colour.Red && code != null){  redCode = code;}
    if (c == Colour.Blue && code != null){ blueCode = code;}
    if (code == null){ return; }
    reset();
  }
  

  @Override protected void paintComponent(Graphics g){
    super.paintComponent(g);
    useGraphics((Graphics2D) g);
  }
  private void useGraphics(Graphics2D g){
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setColor(Color.BLACK);
    g.setStroke(new BasicStroke(2));
    for (int i = 0; i <= Point.coordSize; i++){
      g.draw(new Line2D.Double(0, i * gridSize, gridTot, i * gridSize));
      g.draw(new Line2D.Double(i * gridSize, 0, i * gridSize, gridTot));
    }
    if (world != null){ world.drawMap(g,getTimeRatio()); }
  }
  //what fraction of the current "frame" are we at
  //(a frame lasts WORLD_UPDATE_DELAY ticks)
  private double getTimeRatio(){
    int base = tick / worldUpdateDelay;
    double time= ((double)tick) / worldUpdateDelay - base;
    assert time >= 0 && time < 1 : time;
    return time;
  }
  private class AnimationTask extends TimerTask {//note: inner class
    public AnimationTask(){ tick = 0; }//note: accessing field of WorldComponent
    @Override public void run() {      //possible because it is inner class
      String msg= world.gameStatus();
      if(!msg.isEmpty()){
        timer.cancel();
        SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(null, msg));
        return;
      }
      tick++;
      if (tick % worldUpdateDelay == 0){ world.nextTurn(); }
      repaint();
    }
  }
}