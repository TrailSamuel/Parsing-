package robotGame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.nio.file.Path;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import robotGame.RobotBody.Colour;

@SuppressWarnings("serial")
public class Main extends JFrame {
  private WorldComponent worldComp= new WorldComponent();
  public static final String assetDirectory="src/assets/";     // the folder containing the images for the robot
  public static final String codeDirectory= "src/programs/";   // the folder containing the robot programs 
  public Main() {
    super("Robots");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    add(worldComp, BorderLayout.CENTER);
    setJMenuBar(new Menus());
    pack();
    setLocationRelativeTo(null);//centers GUI in screen
    setVisible(true);
  }
  private class Menus extends JMenuBar{
    JMenuItem load1;
    JMenuItem load2;
    JMenuItem start;
    Menus(){
      this.start = makeMenuItem ("Start",this,e->{
        enableMenus(false);
        worldComp.start();
      });
      makeMenuItem("Reset", this,(ActionEvent e) ->{
        worldComp.reset();
        enableMenus(true);
      });
      JMenu loadMenu= new JMenu("Load Program");
      add(loadMenu);
      this.load1= makeMenuItem("Robot 1 (Red)",loadMenu,
        e->worldComp.loadRobotProgram(Colour.Red, getCodeFile()));
      this.load2= makeMenuItem("Robot 2 (Blue)",loadMenu,
        e->worldComp.loadRobotProgram(Colour.Blue, getCodeFile()));
      makeMenuItem("Quit", this,
        e->dispatchEvent(new WindowEvent(Main.this, WindowEvent.WINDOW_CLOSING)));
      }
    void enableMenus(boolean flag){
      load1.setEnabled(flag);
      load2.setEnabled(flag);
      start.setEnabled(flag);
    }
    JMenuItem makeMenuItem(String name, JComponent menu, ActionListener action){
      JMenuItem menuItem = new JMenuItem(name);
      menu.add(menuItem);
      menuItem.addActionListener(action);
      return menuItem;
    }
  }
  public Path getCodeFile() {
    JFileChooser chooser= new JFileChooser(codeDirectory);
    int userChoice= chooser.showOpenDialog(this);
    boolean approved= userChoice == JFileChooser.APPROVE_OPTION;
    if (!approved){ return null; }
    return chooser.getSelectedFile().toPath();
  }
  public static void main(String[] a){ SwingUtilities.invokeLater(Main::new); }
}