package com.example.michi.michaelvoicerecognition;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private SpeechRecognizer speechRecognizer;
    private TextView messageField;
    public ListView chatHistory;
    public ArrayAdapter<String> chatHistoryAdapter;
    public ArrayList<String> chatHistoryList;
    private String chatUserName="Me";
    private boolean speakButtonPressed;
    private boolean autoSend;
    private float listeningTime = 0f ;

    private Button sendButton;
    private ImageButton speakButton;
    private Chat chat;
    private BoundBluetoothService boundBluetoothService;
    /*

    */
    private final BroadcastReceiver connectionToServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String connection = intent.getAction();
            System.out.println("Context: "+context+" getAction: "+connection);
            if (connection.equals("ConnectionToService")) {
                initializeChat();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        requestRecordAudioPermission();

        setupGUI();
        //check if the activity was started with Intent extra BluetoothConnection
        if(getIntent().getExtras().get("BluetoothConnection")!=null && getIntent().getExtras().getBoolean("BluetoothConnection") == true)
        {
            setupBluetoothSeviceCommunication();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new CustomRecognitionListener());


    }

    private void setupGUI()
    {
        chatHistoryList = new ArrayList<String>();
        chatHistoryAdapter = new ArrayAdapter<String>(this,
                R.layout.my_custom_list_item,
                chatHistoryList);


        messageField = (TextView)findViewById(R.id.messageField);

        chatHistory = (ListView)findViewById(R.id.chatHistory);
        chatHistory.setAdapter(chatHistoryAdapter);
        chatHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {


            }
        });

        sendButton = (Button) findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if there even is a connection to send things
                if(chat!=null)
                {
                    if(chatUserName.equals("Me")) {
                        chat.sendMessage("Other Person" + ": " + messageField.getText().toString());
                    }
                    else{
                        chat.sendMessage(chatUserName + ": " + messageField.getText().toString());
                    }
                }
                updateChatHistory(chatUserName+": ",messageField.getText().toString());
                messageField.setText("");
            }
        });

        speakButtonPressed = false;

        speakButton = (ImageButton) findViewById(R.id.button);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    //if(!speakButtonPressed) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

                        //TODO
                        System.out.println("listeningTime "+listeningTime);
                        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, listeningTime);
                        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, listeningTime);


                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

                        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                        speechRecognizer.startListening(intent);
                        speakButtonPressed = true;
                    /**}
                    else {
                        //TODO
                        speechRecognizer.stopListening();
                        speechRecognizer.cancel();
                        speakButtonPressed = false;
                    }**/



            }
        });
    }

    /**
     *  Register ConnectionToService receiver and initializes boundBluetoothService.
     */
    private void setupBluetoothSeviceCommunication()
    {
        IntentFilter filter = new IntentFilter("ConnectionToService");
        registerReceiver(connectionToServiceReceiver,filter);

        boundBluetoothService = new BoundBluetoothService(this);
    }

    /**
     * Initialize the Chat.
     */
    private void initializeChat()
    {
        chat = new Chat(boundBluetoothService.getService().getBluetoothSocket(), this);
    }
    /**
     * Shows a message that requests record audio permission at runtime.
     */
    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    /**
     * Scroll to the bottom of the chatHistory.
     */
    public void scrollMyListViewToBottom() {
        chatHistory.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                chatHistory.setSelection(chatHistoryAdapter.getCount() - 1);
            }
        });
    }

    /**
     * Adds another item to the chatHistoryList.
     * @param preText contains the own username if the chat is changed locally
     * @param text  contains the text of the added ListItem
     */
    public void updateChatHistory(String preText, String text)
    {
        chatHistoryList.add(preText + text);
        //notify adapter about the change
        chatHistoryAdapter.notifyDataSetChanged();
        //scroll the history down
        scrollMyListViewToBottom();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_menu, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.name:
                final EditText txtField = new EditText(this);

                // Set the default text to a link of the Queen
                txtField.setHint("Type your name");

                new AlertDialog.Builder(this)
                        .setTitle("What's your name?")
                        .setView(txtField)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String text = txtField.getText().toString();
                                chatUserName = text;
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;
            case R.id.autoSend:
                if(autoSend)
                {
                    autoSend=false;
                }
                else
                {
                    autoSend=true;
                }
                return true;
            case R.id.longConversation:
                final EditText txtFieldLongConversation = new EditText(this);

                // Set the default text to a link of the Queen
                txtFieldLongConversation.setHint("for example 1 hour = 1; 1 and a half = 1,5");

                new AlertDialog.Builder(this)
                        .setTitle("Type the approximate length of your conversation in hours.")
                        .setView(txtFieldLongConversation)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String text = txtFieldLongConversation.getText().toString();
                                try {
                                    //hours to milliseconds *60*60*100
                                    listeningTime = Float.parseFloat(text.toString())*60*60*1000;
                                } catch(NumberFormatException nfe) {
                                    System.out.println("Could not parse " + nfe);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

                return true;
            case R.id.help:
                Toast.makeText(getApplicationContext(),"no help available :D",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class CustomRecognitionListener implements RecognitionListener
    {
        private final String TAG = "ChatActivity";

        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndOfSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG,  "error " +  error);
            messageField.setText("error " + error);
        }
        public void onResults(Bundle results)
        {
            String str = new String();

            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            /**for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }**/
            str += data.get(0);

            Log.d(TAG, "onResults " + str);
            messageField.setText(str);
            messageField.setBackgroundColor(Color.TRANSPARENT);
        }
        public void onPartialResults(Bundle partialResults)
        {
            ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(data.size() - 1);
            Log.d(TAG, "onPartialResults "+word);
            messageField.setText(word);
            messageField.setBackgroundColor(Color.RED);
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

}
