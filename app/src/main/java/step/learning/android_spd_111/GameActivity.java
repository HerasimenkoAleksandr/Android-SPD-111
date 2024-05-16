package step.learning.android_spd_111;

import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.LinkedList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
 private static final int FIELD_WIDTH=16;
 private static final int Field_Height =24;
 private static TextView[][] gameField;
 private LinkedList<Vector2> snake = new LinkedList<>();
 private static final Random _random = new Random();
private final Handler handler = new Handler();
private int fieldColor;
private int snakeColor;
private boolean isPlaying;
    private boolean isBonus = false;
private static final String food = new String(Character.toChars(0x1F34E));
private static final String bonus = new String(Character.toChars(0x1F349));
private int count_food;
private String foodEatenLabel;
private TextView foodEatenTextView;
private String speedLabel;
private TextView speedTextView;
private int newSpeedLabel;
private int newSpeed;
private Vector2 foodPosition;
private Direction moveDirection;

private void step(){
    if(!isPlaying)return;
    Vector2 tail = snake.getLast();
    Vector2 head = snake.getFirst();
    Vector2 newHead = new Vector2(head.x, head.y);
    if(head.x==tail.x && head.y==tail.y){
        gameOver();
        return;
    }
    switch (moveDirection)
    {
        case bottom:
            newHead.y+=1;
            break;
        case left:newHead.x-=1;
            break;
        case right:newHead.x+=1;
            break;
        case top:newHead.y-=1;
            break;

    }
    if(newHead.x<0 || newHead.x>= FIELD_WIDTH|| newHead.y<0 || newHead.y>=Field_Height){
        gameOver();
        return;
    }


    if(newHead.x==foodPosition.x && newHead.y==foodPosition.y) {
        gameField[foodPosition.x][foodPosition.y].setText("");
        count_food++;
        if (isBonus) {
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor(fieldColor);


            tail = snake.getLast();
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
            tail = snake.getLast();
            snake.remove(tail);
            gameField[tail.x][tail.y].setBackgroundColor(fieldColor);

            isBonus=false;
        }
        if (count_food!=0 && count_food % 3 == 0) {
            do {
                foodPosition = Vector2.random();

            } while (isCellInSnake(foodPosition));
            gameField[foodPosition.x][foodPosition.y].setText(bonus);
            isBonus=true;
        } else {
            do {
                foodPosition = Vector2.random();

            } while (isCellInSnake(foodPosition));

            gameField[foodPosition.x][foodPosition.y].setText(food);



        }


    }
    else{
        gameField[tail.x][tail.y].setBackgroundColor(fieldColor);
        snake.remove(tail);


    }
    snake.addFirst(newHead);
    gameField[newHead.x][newHead.y].setBackgroundColor(snakeColor);
if(count_food<7)
{
    if(count_food==0)
    {
        newSpeed=50;
        newSpeedLabel=700;
    }
    else {
        newSpeed=100*count_food;
        newSpeedLabel=700-(newSpeed/2);
    }
}


    speedTextView.setText(speedLabel+" "+(newSpeed));
    handler.postDelayed(this::step, 700-newSpeed);
    foodEatenTextView.setText(foodEatenLabel+" "+count_food);

}

private boolean isCellInSnake(Vector2 cell)
{
    for(Vector2 v: snake){
        if(v.x==cell.x && v.y==cell.y) return true;

    }
    return false;
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        foodEatenLabel=getResources().getString(R.string.snake_eaten);
        foodEatenTextView=findViewById(R.id.food_eaten_text_view);

        speedLabel=getResources().getString(R.string.snake_speed);
        speedTextView=findViewById(R.id.speed_view);

        fieldColor = getResources().getColor(R.color.game_field, getTheme());
        snakeColor = getResources().getColor(R.color.game_snake, getTheme());
                // додаємо аналізатор (слухач) свайпів на всю активність (R.id.main)
        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this) {
            @Override
            public void onSwipeBottom() {
                Toast.makeText(GameActivity.this, "Bottom", Toast.LENGTH_SHORT).show();
            if(moveDirection!=Direction.top)
            {
                moveDirection=Direction.bottom;
            }
            }

            @Override
            public void onSwipeLeft() {
                if(moveDirection!=Direction.right)
                {
                    moveDirection=Direction.left;
                }
            }

            @Override
            public void onSwipeRight() {
                if(moveDirection!=Direction.left)
                {
                    moveDirection=Direction.right;
                }
            }

            @Override
            public void onSwipeTop() {
                if (moveDirection != Direction.bottom) {
                    moveDirection = Direction.top;
                }
            }
        });

        initField();
        newGame();

    }
    private void initField(){
        LinearLayout field = findViewById(R.id.game_field);

        LinearLayout.LayoutParams tvLayoutParam = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        tvLayoutParam.weight=1f;
        tvLayoutParam.setMargins(4,4,4,4);

        LinearLayout.LayoutParams rowLayoutParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
              0
        );
        rowLayoutParam.weight=1f;

        gameField = new TextView[FIELD_WIDTH][Field_Height];
        for(int j=0; j<Field_Height; j++)
        {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(rowLayoutParam);
            for(int i=0; i<FIELD_WIDTH; i++)
            {

                TextView tv = new TextView(this);
                tv.setBackgroundColor(fieldColor);
                //tv.setText("0");
                tv.setLayoutParams(tvLayoutParam);
                row.addView(tv);
                gameField[i][j]=tv;
            }
            field.addView(row);
        }
    }

    private void newGame(){
        count_food = 0;
        for(Vector2 v : snake) {
            gameField[v.x][v.y].setBackgroundColor(fieldColor);
        }
        snake.clear();
        if(foodPosition!=null)
        {
            gameField[foodPosition.x][foodPosition.y].setBackgroundColor(fieldColor);
        }

        snake.add(new Vector2(7,10));
        snake.add(new Vector2(7,11));
        snake.add(new Vector2(7,12));
        snake.add(new Vector2(7,13));
        snake.add(new Vector2(7,14));
        for(Vector2 v : snake) {
            gameField[v.x][v.y].setBackgroundColor(snakeColor);
        }

       // foodPosition =new Vector2(3, 14);
        foodPosition =new Vector2(3, 17);
        gameField[foodPosition.x][foodPosition.y].setText(food);

        moveDirection=Direction.top;
        isPlaying=true;
        step();
    }

    private void gameOver()
    {
        isPlaying=false;
        gameField[foodPosition.x][foodPosition.y].setText("");
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Play one more ")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> newGame())
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    };

    @Override
    protected void onPause() { //подія деактивації
        super.onPause();
        if(!isPlaying){

        }
        isPlaying=false;
    }

    @Override
    protected void onResume() {//подія активації
        super.onResume();
        if(!isPlaying){
            isPlaying=true;
            step();
        }

    }

    static class Vector2{

    int x;
    int y;
        Vector2(int x, int y){
             this.x=x;
            this.y=y;
        }

        public static Vector2 random(){
            return new Vector2(_random.nextInt(FIELD_WIDTH), _random.nextInt(Field_Height));
        }
    }

    enum Direction{ bottom, left, right, top}
}
