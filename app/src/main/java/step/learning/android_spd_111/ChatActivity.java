package step.learning.android_spd_111;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.EditText;

import step.learning.android_spd_111.orm.ChatMessage;
import step.learning.android_spd_111.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private  static  final String CHAT_URL = "https://chat.momentfor.fun/";

    private MediaPlayer newMessageSound;
    private final byte[] buffer = new byte[8069];

    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private EditText etNik;
    private EditText etMessage;
    private LinearLayout container;

    private final Handler handler = new Handler();
    private ScrollView chatScroller;

    private Switch soundSwitch;

    // паралельні запити до кількох ресурсів не працюють, виконується лише один
    // це обмежує вибір виконавчого сервісу.
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
           Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           return insets;
        });


        updateChat();
        urlToImageView("https://www.shutterstock.com/ru/image-vector/currency-symbol-icons-set-dollar-260nw-2362207813.jpg",
                findViewById(R.id.chat_iv_logo));
        etNik=findViewById(R.id.chat_et_nik);
        etMessage=findViewById(R.id.chat_et_message);
        chatScroller=findViewById(R.id.chat_scroller);
         container = findViewById( R.id.chat_container );
        findViewById(R.id.chat_btn_send).setOnClickListener(this::onSendClick);
        container.setOnClickListener((v)->{hideSoftInput();});
        newMessageSound = MediaPlayer.create(this, R.raw.pic);
        soundSwitch = findViewById(R.id.soundSwitch);
    }

    private void updateChat()  {
        if(executorService.isShutdown()) return;

        CompletableFuture
                .supplyAsync( this::loadChat, executorService )
                .thenApplyAsync( this::processChatResponse )
                .thenAcceptAsync( this::displayChatMessages );

        handler.postDelayed(this::updateChat, 3000);
    }

    private void hideSoftInput() {
        // Клавіатура з'являється автоматично через фокус введення, прибрати її - прибрати фокус
        // Шукаємо елемент, що має фокус введення
        View focusedView = getCurrentFocus();
        if(focusedView != null) {
            // Запитуємо систему щодо засобів управління клавіатурою
            InputMethodManager manager = (InputMethodManager)
                    getSystemService( Context.INPUT_METHOD_SERVICE ) ;
            // прибираємо клавіатуру з фокусованого елемента
            manager.hideSoftInputFromWindow( focusedView.getWindowToken(), 0 );
            // прибираємо фокус з елемента
            focusedView.clearFocus();
        }
    }
    private void onSendClick( View v ) {
        String author = etNik.getText().toString();
        String message = etMessage.getText().toString();
        if( author.isEmpty() ) {
            Toast.makeText(this, "Заповніть 'Нік'", Toast.LENGTH_SHORT).show();
            return;
        }
        if( message.isEmpty() ) {
            Toast.makeText(this, "Введіть повідомлення", Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor(author);
        chatMessage.setText(message);

        CompletableFuture
                .runAsync(()->sendChatMessage(chatMessage));
        etMessage.setText("");
        etNik.setEnabled(false);
    }

    private void sendChatMessage( ChatMessage chatMessage ) {
        /*
        Необхідно сформувати POST-запит на URL чату та передати дані форми
        з полями author та msg з відповідними значеннями з chatMessage
        дані форми:
        - заголовок Content-Type: application/x-www-form-urlencoded
        - тіло у вигляді: author=TheAuthor&msg=The%20Message
         */
        try {
            // 1. Готуємо підключення та налаштовуємо його
            URL url = new URL( CHAT_URL ) ;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setChunkedStreamingMode( 0 );  // не ділити на чанки (фрагменти)
            connection.setDoOutput( true );  // запис у підключення -- передача тіла
            connection.setDoInput( true );   // читання -- одержання тіла відповіді від сервера
            connection.setRequestMethod( "POST" );
            // заголовки у connection задаються через setRequestProperty
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Connection", "close" );

            // 2. Запис тіла (DoOutput)
            OutputStream connectionOutput = connection.getOutputStream() ;
            String body = String.format(  // author=TheAuthor&msg=The%20Message
                    "author=%s&msg=%s",
                    URLEncoder.encode( chatMessage.getAuthor(), StandardCharsets.UTF_8.name() ),
                    URLEncoder.encode( chatMessage.getText(), StandardCharsets.UTF_8.name() )
            );
            connectionOutput.write( body.getBytes( StandardCharsets.UTF_8 ) );
            // 3. Надсилаємо - "виштовхуємо" буфер
            connectionOutput.flush();
            // 3.1. Звільняємо ресурс [якщо не вживати форму try(){} ]
            connectionOutput.close();

            // 4. Одержуємо відповідь
            int statusCode = connection.getResponseCode();
            // у разі успіху сервер передає статус 201 і не передає тіло
            // якщо помилка, то статус інший та є тіло з описом помилки
            if( statusCode == 201 ) {
                // якщо потрібне тіло відповіді, то воно у потоці .getInputStream()
                // запустити оновлення чату
                updateChat();
            }
            else {
                // хоча при помилці тіло таке ж, але воно вилучається .getErrorStream()
                InputStream connectionInput = connection.getErrorStream();
                body = readString( connectionInput ) ;
                connectionInput.close();
                Log.e( "sendChatMessage", body ) ;
            }

            // 5. Закриваємо підключення
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e( "sendChatMessage", ex.getMessage() );
        }
    }
    private String loadChat(){
        try (InputStream chatStream = new URL(CHAT_URL).openStream();)
        {
            String response = readString(chatStream);
            return response;
            //  runOnUiThread(()->((TextView)findViewById(R.id.chat_tv_title)).setText(response));
            //  ((TextView)findViewById(R.id.chat_tv_title)).setText(response);
        }
        catch (Exception ex) {
            Log.e("ChatActivity::loadChat()",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage() ) ;
        }
        return null;
    }

    private boolean processChatResponse( String response ) {
        boolean wasNewMessage = false;
        boolean wasNewMessageNoMy = true;
        String author = etNik.getText().toString();
        boolean isFirstProcess = this.chatMessages.isEmpty();
        try {
            ChatResponse chatResponse = ChatResponse.fromJsonString( response ) ;
            for( ChatMessage message : chatResponse.getData() ) {
                if( this.chatMessages.stream().noneMatch(
                        m -> m.getId().equals( message.getId() ) ) ) {
                    // немає жодного повідомлення з таким id, як у message -- це нове повідомлення
                    this.chatMessages.add( message ) ;
                    if(message.getAuthor().equals(author))
                    {
                        wasNewMessageNoMy=false;
                    }
                    wasNewMessage = true;

                }
            }
            if(isFirstProcess){
                this.chatMessages.sort(Comparator.comparing(ChatMessage::getMoment));
            }
            else if(wasNewMessage && wasNewMessageNoMy){
                if (soundSwitch.isChecked()) { // Проверка состояния переключателя
                    newMessageSound.start();
                }
            }
        }
        catch (IllegalArgumentException ex) {
            Log.e("ChatActivity::processChatResponse",
                    ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage() );
        }
        return wasNewMessage;
    }

    private void urlToImageView(String url, ImageView imageView)
    {    CompletableFuture.supplyAsync( () -> {
        try ( java.io.InputStream is = new URL(url)
                .openConnection().getInputStream() ) {
            return BitmapFactory.decodeStream( is );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }    }, executorService ).thenAccept( imageView::setImageBitmap );}

    private void displayChatMessages( boolean wasNewMessage ) {
        if( ! wasNewMessage ) return;
        Drawable myBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_my );

        Drawable theyBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msq_they );

        runOnUiThread( () -> {

            int index = 0;
            for( ChatMessage message : ChatActivity.this.chatMessages ) {
                if(message.getView()!=null)
                {
                    continue;
                }
                String author = message.getAuthor();
                String messageText = message.getText();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String messageDate = dateFormat.format(message.getMoment());
                SpannableStringBuilder messageChat= new SpannableStringBuilder();
                messageChat.append(author+'\n', new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageChat.append(messageText).append(String.valueOf('\n'));
                messageChat.append(messageDate+'\n', new StyleSpan(Typeface.ITALIC), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView tv = new TextView(this);
                tv.setText(messageChat);
                LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                msgParams.setMargins(0, 10, 8, 10);
                String authorFromMyForm = etNik.getText().toString();
               if(authorFromMyForm.equals(author))
               {
                   tv.setBackground( myBackground ) ;
                   msgParams.gravity = Gravity.END;
               }
               else
               {
                   tv.setBackground( theyBackground );
                   msgParams.gravity = Gravity.START;
               }

                tv.setPadding(15, 5, 15, 5);
                tv.setLayoutParams( msgParams );
                container.addView(tv);
                message.setView(tv);
                index++;
            }

              /*
            chatScroller.fullScroll( View.FOCUS_DOWN ) ;
            Асинхронність Android призводить до того, що на момент подачі команди
            не всі представлення, додані до контейнера, вже сформовані.
            Прокрутка діятиме лише на поточне наповнення контейнера.
             */
            chatScroller.post(()->chatScroller.fullScroll(View.FOCUS_DOWN));
        } ) ;
    }





    private String readString (InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while((len=stream.read(buffer))!=-1)
        {
            byteBuilder.write(buffer,0, len);
        }
        String res = byteBuilder.toString();
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();
        super.onDestroy();
    }
}

/*
Робота з мережею Інтернет
основу складає клас java.net.URL
традиційно для Java створення об'єкту не призводить до якоїсь активності,
лише створюється програмний об'єкт.
Підключення та передача даних здійснюється при певних командах, зокрема,
відкриття потоку.
Читання даних з потоку має особливості
 - мульти-байтове кодування: різні символи мають різну байтову довжину. Це
     формує пораду спочатку одержати всі дані у бінарному вигляді і потім
     декодувати як рядок (замість одержання фрагментів даних і їх перетворення)
 - запити до мережі не можуть виконуватись з основного (UI) потоку. Це
     спричинює виняток (android.os.NetworkOnMainThreadException).
     Варіанти рішень
     = запустити в окремому потоці
        + простіше і наочніше
        - складність завершення різних потоків, особливо, якщо їх багато.
     = запустити у фоновому виконавці
        + централізоване завершення
        - не забути завершення
 - Для того щоб застосунок міг звертатись до мережі йому потрібні
      відповідні дозволи. Без них виняток (Permission denied (missing INTERNET permission?))
      Дозволи зазначаються у маніфесті
       <uses-permission android:name="android.permission.INTERNET"/>
 - Необхідність запуску мережних запитів у окремих потоках часто призводить до
     того, що з них обмежено доступ до елементів UI
     (Only the original thread that created a view hierarchy can touch its views.)
     Перехід до UI потоку здійснюється або викликом runOnUiThread або переходом
     до синхронного режиму.
 */

/*
Д.З. Завершити роботу з проєктом "Чат":

- Блокувати можливість зміни автора (Нік) після надсилання першого
повідомлення.

*/