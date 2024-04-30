package step.learning.android_spd_111;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;

public class CalcActivity extends AppCompatActivity {

    private TextView tvHistory;
    private TextView tvResult;

    @SuppressLint({"DiscouragedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);
       if(savedInstanceState==null)
       {tvResult.setText("0");
           tvHistory.setText("");
       }
        for(int i=0;i<10;i++) {
            findViewById(
                    getResources().getIdentifier(
                            "calc_btn_" + i,
                            "id",
                            getPackageName()
                    )
            ).setOnClickListener(this::onDigitButtonClick);
        }
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::onInverseClick);
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::onPlusClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::onMinusClick);
        findViewById(R.id.calc_btn_percent).setOnClickListener(this::onPercentClick);
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(this::onSqrtClick);
        findViewById(R.id.calc_btn_square).setOnClickListener(this::onSquareClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::onMultiplyClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::onDivideClick);
        findViewById(R.id.calc_btn_equals).setOnClickListener(this::onEqualsClick);
        findViewById(R.id.calc_btn_c).setOnClickListener(this::onCClick);
        findViewById(R.id.calc_btn_ce).setOnClickListener(this::onCEClick);
        findViewById(R.id.calc_btn_backspace).setOnClickListener(this::onBackspaceClick);
        findViewById(R.id.calc_btn_comma).setOnClickListener(this::onCommaClick);

        findViewById(R.id.main).setOnTouchListener(new OnSwipeListener(this) {
            @Override
            public void onSwipeLeft() {
                Toast.makeText(CalcActivity.this,
                        "SWIPE - Backspace", Toast.LENGTH_SHORT).show();
                onBackspaceClick(findViewById(R.id.main));
            }
        });
    }



private void onInverseClick(View view){
        String result= tvResult.getText().toString();
        double x = Double.parseDouble(result);
        if(x==0){
            Toast.makeText(this,R.string.calc_zero_division, Toast.LENGTH_SHORT).show();
            return;
        }
        x=0.1/x;
        String str = String.valueOf(x);
        if(str.length()>13){
            str = str.substring(0, 13);
        }
        tvResult.setText(str);
};

    private void onCClick(View view) {
        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();
        if(!result.equals("0")) {
            result = "0";
        }
        if(!history.equals("")) {
            history = "";
        }
        tvResult.setText(result);
        tvHistory.setText(history);
    }
    private void onCEClick(View view) {
        String result = tvResult.getText().toString();
        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }
        if (result.isEmpty()) {
            result = "0";
        }
        tvResult.setText(result);
    }
    private void onBackspaceClick(View view) {
        onCEClick(view);
    }
    private void onCommaClick(View view) {
        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();
        if (!result.endsWith("+") && !result.endsWith("*")
                && !result.endsWith("-") && !result.endsWith("/")
                && !result.endsWith(".")) {
            result += ".";
            history+= ".";
        }
        tvResult.setText(result);
        tvHistory.setText(history);
    }




    private void onPlusClick(View view){
        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();

           result = evaluateExpression(result);
           Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();


        if(!result.equals("-") && !result.isEmpty()) {
            if (result.matches("^-?\\d.*")) {
                if (!result.endsWith("+") && !result.endsWith("*") && !result.endsWith("-") && !result.endsWith("/")) {
                    result += "+";
                    history+= "+";
                }
                else if (result.endsWith("-") || result.endsWith("*") || result.endsWith("/")) {
                    result = result.substring(0, result.length() - 1) + "+";
                    history= history.substring(0, history.length() - 1) + "+";
                }
            }
        }
        tvResult.setText(result);
        tvHistory.setText(history);
    };

    private void onMultiplyClick(View view) {
        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();
        if(!result.equals("-") && !result.isEmpty()) {

            if (result.matches("^-?\\d.*")) {
                if (!result.endsWith("+") && !result.endsWith("*") && !result.endsWith("-") && !result.endsWith("/")) {
                    result += "*";
                    history += "*";
                }
                else if (result.endsWith("-") || result.endsWith("*")||  result.endsWith("+") || result.endsWith("/")) {
                    result = result.substring(0, result.length() - 1);
                    result = evaluateExpression(result);
                    result += "*";
                    history = history.substring(0, result.length() - 1) + "*";
                }
            }
        }
        tvResult.setText(result);
        tvHistory.setText(history);
    }
    private void onEqualsClick(View view) {
        String result = tvResult.getText().toString();
        try {

            String str = evaluateExpression(result);
            if(str.length() > 13) {
                str = str.substring(0, 13);
            }
            tvHistory.setText(result);
            tvResult.setText(str);
        } catch (ArithmeticException e) {
            Toast.makeText(this, R.string.calc_wrong_expression, Toast.LENGTH_SHORT).show();
        }
    }
    private void onDivideClick(View view) {
        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();


        if(!result.equals("-") && !result.isEmpty()) {
            result = evaluateExpression(result);
            if (result.matches("^-?\\d.*")) {
                if (!result.endsWith("+") && !result.endsWith("*") && !result.endsWith("-") && !result.endsWith("/")) {
                    result += "/";
                    history += "/";
                }
                else if (result.endsWith("-") || result.endsWith("*") || result.endsWith("+")) {
                    result = result.substring(0, result.length() - 1) + "/";
                    history = history.substring(0, result.length() - 1) + "/";
                }
            }
        }
        tvResult.setText(result);
        tvHistory.setText(history);
    }
    private void onMinusClick(View view){
        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();


        if(!result.equals("-") && !result.isEmpty()) {
            result = evaluateExpression(result);
            if (result.matches("^-?\\d.*")) {
                if (!result.endsWith("+") && !result.endsWith("*") && !result.endsWith("-") && !result.endsWith("/")) {
                    result += "-";
                    history+= "-";
                }
                else if (result.endsWith("-") || result.endsWith("*") || result.endsWith("/")) {
                    result = result.substring(0, result.length() - 1) + "-";
                    history= history.substring(0, history.length() - 1) + "-";
                }
            }
        }
        tvResult.setText(result);
        tvHistory.setText(history);
    };
    private void onPercentClick(View view) {
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        x /= 100;
        String str = (x == (int)x) ? String.valueOf((int)x) : String.valueOf(x);
        if(str.length() > 13) {
            str = str.substring(0, 13);
        }
        tvResult.setText(str);
    }




    private void onSqrtClick(View view) {
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        if(x <= 0) {
            Toast.makeText(this, R.string.calc_negative_sqrt, Toast.LENGTH_SHORT).show();
            return;
        }
        x = Math.sqrt(x);
        String str = (x == (int)x) ? String.valueOf((int)x) : String.valueOf(x);
        if(str.length() > 13) {
            str = str.substring(0, 13);
        }
        tvResult.setText(str);
    }
    private void onSquareClick(View view) {
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        x *= x;
        String str = (x == (int)x) ? String.valueOf((int)x) : String.valueOf(x);
        if(str.length() > 13) {
            str = str.substring(0, 13);
        }
        tvResult.setText(str);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tvResult", tvResult.getText());
        outState.putCharSequence("tvHistory", tvHistory.getText());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tvResult"));
        tvHistory.setText(savedInstanceState.getCharSequence("tvHistory"));
    }
    private void onDigitButtonClick(View view){

        String result = tvResult.getText().toString();
        String history = tvHistory.getText().toString();

        if(result.length()>=10){
            Toast.makeText(this, R.string.calc_limit_exceeded, Toast.LENGTH_SHORT).show();
            return;
        }
        if(result.equals("0")){
            result="";
            history="";
        }


        result+=((Button)view).getText();
        history+=((Button)view).getText();
        tvResult.setText(result);
        tvHistory.setText(history);
    }

    public static String evaluateExpression(String expression) {
        String[] tokens = expression.split("(?<=[-+*/])|(?=[-+*/])"); // Разбиваем выражение на токены (+, -, *, /)

        double total = Double.parseDouble(tokens[0]); // Инициализируем результат первым числом

        for (int i = 1; i < tokens.length; i += 2) {
            String operator = tokens[i];
            double operand = Double.parseDouble(tokens[i + 1]);

            switch (operator) {
                case "+":
                    total += operand;
                    break;
                case "-":
                    total -= operand;
                    break;
                case "*":
                    total *= operand;
                    break;
                case "/":
                    total /= operand;
                    break;
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        return decimalFormat.format(total);

    }
}