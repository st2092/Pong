package com.example.sony.pong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import static android.graphics.RectF.intersects;

/**
 * This class is a simple graphical view of an interactive animated application. The ball bounces off the
 * edges of the screen. The right edge is where the player moves the paddle up or
 * down to hit the ball.
 */
public class PongView extends View {
    private static final float BALL_SIZE = 100;
    private static final float BALL_MAX_VELOCITY = 20;
    private static final float PADDLE_HEIGHT = 300;
    private static final float PADDLE_WIDTH = 40;

    private Sprite ball;
    private Sprite paddle;
    private DrawingThread drawing_thread;
    private int player_score = 0;
    private int fps = 60; // frame-per-second rate to redraw view
    private boolean initial_start = true;
    private Paint text_color;

    /*
     * Constructor that sets up the initial state of the view and sprites.
     */
    public PongView(Context context, AttributeSet attribute_set)
    {
        super(context, attribute_set);

        // set up initial state for the ball
        ball = new Sprite();
        ball.setSize(BALL_SIZE, BALL_SIZE);
        ball.setLocation(10, 100);
        ball.setVelocity(
                (float) ((Math.random() - .5) * 2 * BALL_MAX_VELOCITY), // dx
                (float) ((Math.random() - .5) * 2 * BALL_MAX_VELOCITY)  // dy
        );
        ball.color.setARGB(255, 138, 43, 226); //blue violet

        // set up initial state for the paddle
        paddle = new Sprite();
        paddle.setSize(PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setVelocity(0, 0);
        // position paddle at middle of the right screen edge
        // NOTE: due to the size of view  is still unknown in the constructor, we will
        // set the location of the paddle later on such as in onDraw()
        paddle.color.setARGB(255, 73, 49, 28); //

        // start drawing thread to animate screen at 50 frames per second (fps)
        drawing_thread = new DrawingThread(this, fps);
        drawing_thread.start();
    }

    /*
     * This method gets call when the user touches the screen.
     * This method is use to control the paddle's movement.
     * When the user touches below the paddle, the paddle moves downward.
     * When the user touches above the paddle, the paddle moves upward.
     * When the user touches the paddle, the paddle stops in place.
     */
    @Override
    public boolean
    onTouchEvent(MotionEvent motion_event)
    {
        float x = motion_event.getX();
        float y = motion_event.getY();

        if(!isTouchingPaddle(x, y))
        {
            if(paddle.rect.top < y)
            {
                // user wants  to scroll downward
                paddle.dy = 20;
            }
            else if (paddle.rect.top > y)
            {
                // user wants to scroll upward
                paddle.dy = -20;
            }
        }
        else
        {
            // user touch the paddle
            paddle.dy = 0;
        }

        return super.onTouchEvent(motion_event);
    }

    /*
     * This method returns true if the x and y position the user
     * touched is the paddle.
     */
    private boolean
    isTouchingPaddle(float x, float y)
    {
       return paddle.rect.contains(x, y);
    }

    /*
     * This method draws the bouncing ball and paddle on the screen. It also updates
     * their position for the next time the screen is redrawn.
     */
    @Override
    public void
    onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        // position the paddle at middle of right edge when application first start
        if (initial_start) {
            paddle.setLocation(
                    getWidth() - PADDLE_WIDTH,                  // x coordinate
                    (getHeight() / 2) - (PADDLE_HEIGHT / 2)     // y coordinate
            );
            text_color = new Paint();
            text_color.setARGB(255, 0, 0, 0);   // black
            text_color.setTextSize(40);
            initial_start = false;
        }

        canvas.drawARGB(255, 200, 200, 0);  // yellow
        canvas.drawText("Score: " + player_score, (getWidth() / 2) - 100, 30, text_color);
        canvas.drawOval(ball.rect, ball.color);
        canvas.drawRect(paddle.rect, paddle.color);
        updateSprites();
    }

    /*
     * This method updates the sprites' position between frames of animation.
     */
    private void
    updateSprites()
    {
        ball.move();
        paddle.move();

        // handle for paddle going offscreen
        if(paddle.rect.bottom > getHeight())       // paddle reached bottom of right edge of screen
        {
            // set position of paddle to be at bottom of right edge
            paddle.setLocation(
                    getWidth() - PADDLE_WIDTH,  // x
                    getHeight() - PADDLE_HEIGHT // y
            );
        }
        else if (paddle.rect.top < 0)           // paddle reached top of right edge of screen
        {
            // set position of paddle to be at top of right edge
            paddle.setLocation(
                    getWidth() - PADDLE_WIDTH,  // x
                    0 // y
            );
        }

        // handle for ball bouncing off edges or paddle
        if (ball.rect.left < 0)
        {
            // ball hit left edge
            ball.dx = -ball.dx; // reverse the ball's direction on x-axis
            player_score += 1;
        }
        else if (!intersects(ball.rect, paddle.rect) && ball.rect.right >= getWidth())
        {
            // ball hit right edge
            ball.dx = -ball.dx;
            player_score -= 1;
        }
        else if (intersects(ball.rect, paddle.rect))
        {
            // collision between paddle and ball
            ball.dx = -ball.dx;
            Log.d("testing", "Collision between ball and paddle");
        }

        if (ball.rect.top < 0 || ball.rect.bottom >= getHeight())
        {
            // ball hit top or bottom edge
            ball.dy = -ball.dy; // reverse the ball's direction on y-axis
        }
    }
}
