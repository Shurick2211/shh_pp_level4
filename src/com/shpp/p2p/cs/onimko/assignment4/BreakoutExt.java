package com.shpp.p2p.cs.onimko.assignment4;

import acm.graphics.*;
import acm.util.RandomGenerator;
import acm.util.SoundClip;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;

public class BreakoutExt extends WindowProgram {

  /** Width and height of application window in pixels */
  public static final int APPLICATION_WIDTH = 400;
  public static final int APPLICATION_HEIGHT = 600;

  /** Dimensions of game board (usually the same) */
  private static final int WIDTH = APPLICATION_WIDTH;
  private static final int HEIGHT = APPLICATION_HEIGHT;

  /** Dimensions of the paddle */
  private static final int PADDLE_WIDTH = 60;
  private static final int PADDLE_HEIGHT = 10;

  /** Offset of the paddle up from the bottom */
  private static final int PADDLE_Y_OFFSET = 30;

  /** Number of bricks per row */
  private static final int NBRICKS_PER_ROW = 10;

  /** Number of rows of bricks */
  private static final int NBRICK_ROWS = 10;

  /** Separation between bricks */
  private static final int BRICK_SEP = 4;

  /** Width of a brick */
  private static final int BRICK_WIDTH =
      (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

  /** Height of a brick */
  private static final int BRICK_HEIGHT = 8;

  /** Radius of the ball in pixels */
  private static final int BALL_RADIUS = 10;

  /** Offset of the top brick row from the top */
  private static final int BRICK_Y_OFFSET = 70;

  /** Number of turns */
  private static final int NTURNS = 3;

  /** Menu size */
  private static final int MENU_SIZE = 24;

  /** Colors for bricks lines */
  private final Color [] colorsLine =
      {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN};

  /** The amount of time to pause between frames (48fps). */
  private static final double PAUSE_TIME = 1000.0 / 48;

  /** The ball's acceleration of gravity. */
  private static final double BALL_GRAVITY = 1.015;

  /** Score of bricks the down line */
  private static final int MIN_SCORE = 10;

  /** The ball's start speed. */
  private static final int START_SPEED = 5;
  /** Sound of start*/
  private final SoundClip clipStart = new SoundClip("assets/start.wav");
  private final SoundClip clipBac = new SoundClip("assets/bac.wav");
  /** Sound of contact the ball and the paddle or walls*/
  private final SoundClip clipBall = new SoundClip("assets/ball.wav");
  {
    clipBac.setVolume(1);
    clipBall.setVolume(1);
    clipStart.setVolume(1);
  }
  /** The score message*/
  private final GLabel scoreMess = userMessage("Score: 0" );
  {
    scoreMess.setLocation(WIDTH*2/3, MENU_SIZE);
  }

  // Create object paddle
  private GRect paddle;

  // Create object ball
  private GOval ball;

  // start ball's speed
  private double vx;
  private double vy;
  //true if mouse click
  boolean click;
  /**
   * Start method
   */
  public void run() {
    //score counter
    int score = 0;
    add(scoreMess);
    // Create  paddle
    paddle = paddle();
    // start draw lines of bricks
    bricksLines();
    // number of attempts
    GObject [] lives = controlLives(NTURNS);
    int life = NTURNS;
    // number of bricks on the field
    int bricksCounter = NBRICKS_PER_ROW * NBRICK_ROWS;
    // wait start
    waitForClick("Click for start!");
    // Create the ball
    startBallPosition();
    GObject collision;
    // main cycle
    while (life != 0 && bricksCounter != 0) {
      if (click) waitForClick("Pause");
      // ball's move
      ball.move(vx,vy);
      //acceleration of gravity
      if (vy > 0) vy *= BALL_GRAVITY;
      // check ball's collision
      if ((collision = ballCollision()) != null) {
        if (collision == paddle) {
          clipBall.play();
          if((paddle.getX() >= ball.getX()+BALL_RADIUS && vx > 0 )||
                  (paddle.getX()+PADDLE_WIDTH <= ball.getX()+BALL_RADIUS && vx < 0) ) vx = -vx;
          vy = -START_SPEED;
        } else {
          clipBac.play();
          pause(PAUSE_TIME);
          score += getScore(collision);
          remove(collision);
          vy = -vy;
          scoreMess.setLabel("Score: " + score);
          bricksCounter--;
          if (collision!=null)remove(collision);
        }
        continue;
      }
      // ball on the walls
      double bX = ball.getX();
      double bY = ball.getY();
      if (bX <= 0 ) {
        clipBall.play();
        ball.setLocation(0, bY);
        vx = - vx;
      }
      if (bX+2*BALL_RADIUS >= getWidth()) {
        clipBall.play();
        ball.setLocation(WIDTH - BALL_RADIUS*2, bY);
        vx = - vx;
      }
      // ball on the top
      if (bY <= 0 ) vy = -vy;
      // ball on the floor
      if (bY >= getHeight()-PADDLE_Y_OFFSET ) {
        remove(lives[life]);
        life--;
        remove(ball);
        if (life != 0) {
          waitForClick("Click to continue!");
          startBallPosition();
        }
      }
      pause(PAUSE_TIME);
    }
   finish(life);
  }

  /**
   * Method for finish game or attempt
   * @param life number of lives
   */
  private void finish(int life){
    String message;
    if (life == 0) message = "Game over!";
    else message = "Congratulation! You win!";
    GLabel finMess = userMessage(message);
    add(finMess);
    GLabel restart = userMessage("Click to restart!");
    restart.setLocation(restart.getX(),restart.getY()+restart.getHeight());
    restart.setColor(Color.RED);
    add(restart);
    waitForClick();
    remove(restart);
    remove(finMess);
    remove(paddle);
    remove(scoreMess);
    remove(ball);
    run();
  }

  /**
   * Method draws ball
   * @return the ball
   */
  private GOval ball () {
    GOval oval = new GOval(getWidth() / 2 - BALL_RADIUS, getHeight() / 2 - BALL_RADIUS,
            BALL_RADIUS * 2, BALL_RADIUS * 2);
    oval.setFilled(true);
    oval.setColor(Color.BLACK);
    return oval;
  }

  /**
   * Create the ball on the start's position
   */
  private void startBallPosition() {
    ball = ball();
    add(ball);
    RandomGenerator rgen = RandomGenerator.getInstance();
    vx = rgen.nextDouble(1.0, 3.0);
    if (rgen.nextBoolean(0.5)) vx = -vx;
    vy = START_SPEED;
  }

  /**
   * Method checks ball's collision
   * @return
   */
  private GObject ballCollision() {
    GObject object = getElementAt(ball.getX(), ball.getY());
    if (object == null) object =  getElementAt(ball.getX() + BALL_RADIUS * 2, ball.getY());
    if (object == null) object =  getElementAt(ball.getX() + BALL_RADIUS * 2,
        ball.getY() + BALL_RADIUS * 2);
    if (object == null) object =  getElementAt(ball.getX(), ball.getY() + BALL_RADIUS*2);
    return object;
  }

  /**
   * Method draws brick with fill color.
   * @param x start point (up-left) "X"
   * @param y start point (up-left) "Y"
   * @param color color of fill
   */
  private void brick (double x, double y, Color color) {
    GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
    brick.setFilled(true);
    brick.setColor(color);
    add(brick);
  }

  /**
   * Method draws lines of bricks
   */
  private void bricksLines () {
    // color of the first line
    Color colorBricksLine = colorsLine[0];
    // number of color in array the colorsLine[]
    int numColor;
    // create lines
    for (int row = 0; row < NBRICK_ROWS; row++) {
      //change colors for next two lines
      if (row >= (colorsLine.length << 1)) numColor = (row % (colorsLine.length << 1))>>1;
      else numColor = row >> 1;
      if (row % 2 == 0) colorBricksLine = colorsLine [numColor];
      //draw line
      for (int col = 0; col < NBRICKS_PER_ROW; col++) {
        brick(BRICK_SEP/2 + col * (BRICK_WIDTH + BRICK_SEP),
            BRICK_Y_OFFSET + row * (BRICK_HEIGHT + BRICK_SEP), colorBricksLine);
      }
    }
  }

  /**
   * Method creates paddle with mouse listener
   * @return the paddle
   */
  private GRect paddle () {
    GRect rect = new GRect((getWidth()-PADDLE_WIDTH)/2, getHeight()-PADDLE_Y_OFFSET,
        PADDLE_WIDTH, PADDLE_HEIGHT);
    rect.setFilled(true);
    rect.setColor(Color.BLACK);
    add(rect);
    addMouseListeners();
    return rect;
  }

  /**
   * Set location of paddle when the mouse
   * is moved.
   */
  public void  mouseMoved(MouseEvent e) {
    int x = e.getX();
    if (x < PADDLE_WIDTH / 2) x = PADDLE_WIDTH / 2;
    if (x > getWidth()-PADDLE_WIDTH) x =  getWidth()-PADDLE_WIDTH / 2;
    paddle.setLocation(x - PADDLE_WIDTH / 2,getHeight()-PADDLE_Y_OFFSET);
  }

  /**
   * Action of mouse click
   * @param mouseEvent click
   */
  public void mouseClicked(MouseEvent mouseEvent) {
    click = true;
  }



  /**
   * Print message for User.
   * @param mess text of message
   */
  private GLabel userMessage(String mess) {
    GLabel label = new GLabel(mess);
    label.setFont(new Font("BOLD",1,24) );
    label.setLocation((getWidth()-label.getWidth())/2,(getHeight()-label.getDescent())/2);
    return label;
  }

  /**
   * Method waits for click
   */
  public void waitForClick(String mess) {
    GLabel label = userMessage(mess);
    add(label);
   // while (!click) pause(PAUSE_TIME);
    waitForClick();
    clipStart.play();
    remove(label);
    click = false;
  }


  /**
   * Method draws lives
   * @param num number of lives
   * @return array of GObject
   */
  private GObject[] controlLives(int num) {
    GLabel label = new GLabel("Life: ",0,MENU_SIZE);
    label.setFont(new Font("BOLD",1,24) );
    label.setColor(Color.RED);
    add(label);
    GObject [] lifes = new GObject[4];
    lifes[0] = label;

    for (int i = 1; i < lifes.length; i++){
      GOval ball = ball();
      ball.setLocation(label.getWidth()+(ball.getWidth()+BRICK_SEP)*(i-1),MENU_SIZE/3);
      lifes [i] = ball;
      add(ball);
    }
    return lifes;
  }


  /**
   * Method return score for a brick
   * @param obj the brick
   * @return score int
   */
  private int getScore(GObject obj) {
    int score = 0;
    for (int i = 0; i < colorsLine.length; i++){
      if (obj.getColor() == colorsLine[i]) score = MIN_SCORE * (colorsLine.length-i);
    }
    return score;
  }

}