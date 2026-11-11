package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;

public class Snake {
    private Color headColor = Color.web("2eb872");
    private Color bodyColor = Color.web("a3de83");
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private Obstacle obstacle;
    private Image imgInputHead = new Image("resources/views/res/snakehead.png");
    private ImageView imageHead = new ImageView(imgInputHead);
    private Image imgInputBody = new Image("resources/views/res/snakebody.png");
    private ImageView imageBody = new ImageView(imgInputBody);
    private Image imgInputTurn = new Image("resources/views/res/snaketurnleft.png");
    private ImageView imageTurn = new ImageView(imgInputTurn);
    private Image imgInputTail = new Image("resources/views/res/snaketail.png");
    private ImageView imageTail = new ImageView(imgInputTail);
    private int snakeDir = 270;

    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
    }

    public void move(char dir) {
        body.add(new Point(head));
        switch (dir) {
            case 'U':
                snakeDir = 0;
                head.translate(0, -1);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(0, 1);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    // Wrap to the opposite vertical edge.
                    // Check the wrapped head against obstacles immediately.
                    // Border wrapping must not bypass obstacle collisions.
                    head.setLocation(head.getX(), gy - 1);
                    if (obstacle.getObstacles().contains(head)) {
                        dead = true;
                    }
                }
                break;

            case 'D':
                snakeDir = 180;
                head.translate(0, 1);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(0, -1);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    // Wrap to the opposite vertical edge.
                    // Check the wrapped head against obstacles immediately.
                    // Border wrapping must not bypass obstacle collisions.
                    head.setLocation(head.getX(), 0);
                    if (obstacle.getObstacles().contains(head)) {
                        dead = true;
                    }
                }
                break;

            case 'L':
                snakeDir = 270;
                head.translate(-1, 0);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(1, 0);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    // Wrap to the opposite horizontal edge.
                    // Check the wrapped head against obstacles immediately.
                    // Border wrapping must not bypass obstacle collisions.
                    head.setLocation(gx - 1, head.getY());
                    if (obstacle.getObstacles().contains(head)) {
                        dead = true;
                    }
                }
                break;

            case 'R':
                snakeDir = 90;
                head.translate(1, 0);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(-1, 0);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    // Wrap to the opposite horizontal edge.
                    // Check the wrapped head against obstacles immediately.
                    // Border wrapping must not bypass obstacle collisions.
                    head.setLocation(0, head.getY());
                    if (obstacle.getObstacles().contains(head)) {
                        dead = true;
                    }
                }
                break;
        }

        // Check self-collision for snakes of every length.
        // Ignore only the tail tip that will move next.
        // The hard-coded length limit must not hide collisions.
        if (hitSelf() && !body.get(0).equals(head)) {
            dead = true;
        }
    }

    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return 270;
        } else {
            return 0;
        }
    }

    public boolean ateCoin(Coin coin) {
        if (!head.equals(coin.getPos())) {
            body.remove(0);
        }
        return head.equals(coin.getPos());
    }

    private boolean borderControl() {
        return head.getX() < 0 || head.getY() < 0 || head.getX() >= gx || head.getY() >= gy;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public ArrayList<Point> getBody() {
        return new ArrayList<>(body);
    }

    public Point getHead() {
        return new Point(head);
    }

    public Color getHeadColor() {
        return headColor;
    }

    public Color getBodyColor() {
        return bodyColor;
    }

    public boolean getDead() {
        return dead;
    }

    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    public ImageView getImageViewHead() {
        return imageHead;
    }

    public ImageView getImageViewBody() {
        return imageBody;
    }

    public ImageView getImageViewTurn() {
        return imageTurn;
    }

    public int getSnakeDir() {
        return snakeDir;
    }

    public ImageView getImageViewTail() {
        return imageTail;
    }
}
