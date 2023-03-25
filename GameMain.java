
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
/**
 * Main Game Logic and Display
 */
public class GameMain extends JPanel implements StateTransition {
   private static final long serialVersionUID = 1L;

   // == Define named constants for the game ==
   /** Number of rows of the game board (in cells) */
   public static final int ROWS = 40;
   /** Number of columns of the game board (in cells) */
   public static final int COLS = 40;
   /** Size of the body cell (in pixels) */
   public static final int CELL_SIZE = 16;

   /** App title */
   public static final String TITLE = "Snake";
   /** Width (in pixels) of the game board */
   public static final int PIT_WIDTH = COLS * CELL_SIZE;
   /** Height (in pixels) of the game board */
   public static final int PIT_HEIGHT = ROWS * CELL_SIZE;

   public static final Color COLOR_PIT = Color.DARK_GRAY;
   public static final Color COLOR_GAMEOVER = Color.RED;
   public static final Font FONT_GAMEOVER = new Font("Verdana", Font.BOLD, 30);
   public static final Color COLOR_INSTRUCTION = Color.RED;
   public static final Font FONT_INSTRUCTION = new Font("Dialog", Font.PLAIN, 26);
   public static final Color COLOR_DATA = Color.WHITE;
   public static final Font FONT_DATA = new Font(Font.MONOSPACED, Font.PLAIN, 16);

   // == Define game objects ==
   private Matrix matrix;
   private GamePanel pit;

   /** Current state of the game */
   private State currentState;

   /** Game step timer */
   private Timer stepTimer;
   /** Number of game steps per second */
   public static final int STEPS_PER_SEC = 6;
   /** Step in mini-seconds */
   public static final int STEP_IN_MSEC = 1000 / STEPS_PER_SEC;

   /**
    * Constructor to initialize the UI components and game objects
    */
   public GameMain() {
      // Set up UI components
      initGUI();

      // Perform one-time initialization tasks
      initGame();

      // Reset all properties for a new game
      newGame();
   }

   /**
    * Helper method to create (init) UI components, called in constructor.
    */
   public void initGUI() {
      pit = new GamePanel();
      pit.setPreferredSize(new Dimension(PIT_WIDTH, PIT_HEIGHT));
      pit.setFocusable(true); // to receive key-events
      pit.requestFocus();
      super.add(pit);   // JPanel.add()
   }

   /**
    * Perform one-time initialization tasks. See game state diagram.
    * Initialize all the game objects, run only once in the constructor
    * of the main class.
    */
   @Override
   public void initGame() {
      // Allocate a new snake and a food item.
      matrix = new Matrix();

      // Set up a Swing's timer to repeatedly schedule a task
      //  on the event dispatching thread (KeyEvent also run on EDT).
      stepTimer = new Timer(STEP_IN_MSEC, e -> stepGame());

      // Set up key event handler
      pit.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent evt) {
            int key = evt.getKeyCode();
            if (currentState == State.READY) {
               // Any keys to start the game
               startGame();
            } else if (currentState == State.PLAYING) {
               switch (key) {
                  case KeyEvent.VK_UP:
                     matrix.stepGame(Action.ROTATE_RIGHT);
                     repaint(); break;
                  case KeyEvent.VK_DOWN:
                     matrix.stepGame(Action.DOWN);
                     repaint(); break;
                  case KeyEvent.VK_LEFT:
                     matrix.stepGame(Action.LEFT);
                     repaint(); break;
                  case KeyEvent.VK_RIGHT:
                     matrix.stepGame(Action.RIGHT);
                     repaint(); break;
               }
            } else if (currentState == State.GAMEOVER) {
               // Any keys to re-start the game
               newGame();
               startGame();
            }
         }
      });

      currentState = State.INITIALIZED;
   }

   /**
    * Perform per-game initialization tasks. See game state diagram.
    * Reset all game properties for a new game.
    */
   @Override
   public void newGame() {
      if (currentState != State.GAMEOVER && currentState != State.INITIALIZED)
         throw new IllegalStateException("Cannot run newGame() in state " + currentState);

      // Get a new snake
      matrix.newGame();

      currentState = State.READY;
      repaint();
   }

   /**
    * To start and re-start a new game.
    */
   @Override
   public void startGame() {
      if (currentState != State.READY)
         throw new IllegalStateException("Cannot run startGame() in state " + currentState);
      // Start the step timer, which fires an event at constant rate.
      stepTimer.start();
      currentState = State.PLAYING;
      repaint();
   }

   /**
    * To stop the game, e.g., game over.
    */
   @Override
   public void stopGame() {
      if (currentState != State.PLAYING)
         throw new IllegalStateException("Cannot run stopGame() in state " + currentState);
      stepTimer.stop();
      currentState = State.GAMEOVER;
      repaint();
   }

   /**
    * Run one step of the game. Fire by the step timer at constant rate.
    */
   public void stepGame() {
      if (currentState != State.PLAYING)
         throw new IllegalStateException("Cannot run stepGame() in state " + currentState);
      if(matrix.stepGame(Action.DOWN)){
         matrix.lockDown();
      }
      if(matrix.isFull()){
         stopGame();  // gameover
         return;
      }

      // // Check if the snake moves out of the pit
      // if (!pit.contains(headX, headY)) {
      //    stopGame();  // gameover
      //    return;
      // }

      // // Check if the snake eats itself
      // if (snake.eatItself()) {
      //    stopGame();  // gameover
      //    return;
      // }

      repaint();
   }

   /**
    * Custom drawing panel for the game pit, written as an inner class.
    *
    */
   private class GamePanel extends JPanel {
      private static final long serialVersionUID = 1L;
      /**
       * Override paintComponent() to do custom drawing.
       * Called back via repaint().
       *
       * @param g - The Graphics drawing object
       */
      @Override
      public void paintComponent(Graphics g) {
         super.paintComponent(g); // paint background
         setBackground(COLOR_PIT); // may use an image for background

         // Draw the game objects
         matrix.paint(g);

         // Print game data
         g.setFont(FONT_DATA);
         g.setColor(COLOR_DATA);
         //stats
         // g.drawString("Snake Head: (" + snake.getHeadX() + "," + snake.getHeadY() + ")", 10, 25);
         // g.drawString("Snake Length: " + snake.getLength(), 10, 45);
         // g.drawString("Food: (" + food.x + "," + food.y + ")", 10, 65);
         // g.drawString("Eaten: " + food.foodEaten, 10, 85);

         // READY state
         if (currentState == State.READY) {
            g.setFont(FONT_INSTRUCTION);
            g.setColor(COLOR_INSTRUCTION);
            g.drawString("Push any key to start the game ...", 100, PIT_HEIGHT / 4);
         }

         // GAMEOVER state
         if (currentState == State.GAMEOVER) {
            g.setFont(FONT_GAMEOVER);
            g.setColor(COLOR_GAMEOVER);
            g.drawString("GAME OVER!", 200, PIT_HEIGHT / 2);
            g.setFont(FONT_INSTRUCTION);
            g.drawString("Push any key to start the game ...", 120, PIT_HEIGHT / 2 + 40);
         }
      }

      // Check if this pit contains the given (x, y), for collision detection
      public boolean contains(int x, int y) {
         if ((x < 0) || (x >= ROWS)) {
            return false;
         }
         if ((y < 0) || (y >= COLS)) {
            return false;
         }
         return true;
      }
   }

   /**
    * The entry main method
    */
   public static void main(String[] args) {
      // Use the event-dispatcher thread to build the UI for thread-safety.
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            GameMain main = new GameMain();
            JFrame frame = new JFrame(TITLE);
            frame.setContentPane(main);  // main JPanel as content pane
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null); // center the application window
            frame.setVisible(true);            // show it
         }
      });
   }
}