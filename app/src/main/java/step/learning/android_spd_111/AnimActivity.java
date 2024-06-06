package step.learning.android_spd_111;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {
private Animation opacityAnimation;
    private Animation sizeAnimation;
    private Animation size2Animation;
    private Animation arcAnimation;
    private Animation bellAnimation;
    private Animation moveAnimation;
    private  boolean inMovePlaying;

    private AnimationSet comboAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        opacityAnimation= AnimationUtils.loadAnimation(this, R.anim.opacity);
        findViewById(R.id.anim_opacity_block).setOnClickListener(this::opacityClic);

        sizeAnimation= AnimationUtils.loadAnimation(this, R.anim.size);
        findViewById(R.id.anim_size_block).setOnClickListener(this::sizeClic);

        size2Animation= AnimationUtils.loadAnimation(this, R.anim.size2);
        findViewById(R.id.anim_size2_block).setOnClickListener(this::size2Clic);

        arcAnimation= AnimationUtils.loadAnimation(this, R.anim.arc);
        findViewById(R.id.anim_arc_block).setOnClickListener(this::arcClic);

        bellAnimation= AnimationUtils.loadAnimation(this, R.anim.bell);
        findViewById(R.id.anim_bell_block).setOnClickListener(this::bellClic);

        moveAnimation= AnimationUtils.loadAnimation(this, R.anim.move);
        moveAnimation.reset();
        findViewById(R.id.anim_move_block).setOnClickListener(this::moveClic);
        inMovePlaying=false;
        comboAnimation = new AnimationSet(false);
        comboAnimation.addAnimation(opacityAnimation);
        comboAnimation.addAnimation(sizeAnimation);

        findViewById(R.id.anim_combo_block).setOnClickListener(this::comboClic);

    }

    private void moveClic(View view)
    {
       view.startAnimation(moveAnimation);
       if(inMovePlaying){
           view.clearAnimation();
       }
       else {
           view.startAnimation(moveAnimation);
       }
       inMovePlaying=!inMovePlaying;
    }
    private void bellClic(View view)
    {
        view.startAnimation(bellAnimation);

    }

    private void comboClic(View view)
    {
        view.startAnimation(comboAnimation);

    }
    private void opacityClic(View view)
    {
        view.startAnimation(opacityAnimation);
    }

    private void sizeClic(View view)
    {
        view.startAnimation(sizeAnimation);
    }

    private void size2Clic(View view)
    {
        view.startAnimation(size2Animation);
    }

    private void arcClic(View view)
    {
        view.startAnimation(arcAnimation);
    }
}