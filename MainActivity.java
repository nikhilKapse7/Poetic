package com.example.wordprocessor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import static com.example.wordprocessor.HomeActivity.HtmlList;
import static com.example.wordprocessor.HomeActivity.currentFileIndex;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    RichEditor textEditor;
    EditText editText;
    ImageButton boldButton;
    ImageButton italicButton;
    ImageButton underlineButton;
    ImageButton leftAlignButton;
    ImageButton centerAlignButton;
    ImageButton rightAlignButton;
    ImageButton bulletButton;
    NavigationView navigationView;
    RecyclerView recyclerView;
    DrawerLayout drawerLayout;
    RecyclerAdapter recyclerAdapter;
    EditText rhymeTextField;
    Button searchButton;
    ArrayList<RhymeObject> rhymeObjectList = new ArrayList<>();
    String selectedWord = "";

    public AlertDialog alertDialog;
    public CharSequence[] dialogItems = {"All", "Iamb", "Dactyl", "Trochee", "Anapest"};

    public static int fileNumber = 0;
    public static ArrayList<String> fileNames = new ArrayList<>();
    public boolean[] checkedItems = {true, true, true, true, true};
    public RhymeObject[] sampleList = {new RhymeObject("Cajoled", "2"),
            new RhymeObject("Paroled", "2"),
            new RhymeObject("Behold", "2"),
            new RhymeObject("Enrolled", "2"),
            new RhymeObject("Uphold", "2"),
            new RhymeObject("Unfold", "2"),
            new RhymeObject("Consoled", "2"),
            new RhymeObject("Extolled", "2"),
            new RhymeObject("Untold", "2"),
            new RhymeObject("Resold", "2"),
            new RhymeObject("Retold", "2")};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.navigationView);

        // inflate header layout and setup IDs
        LayoutInflater headerInflater = getLayoutInflater();
        View navigationHeader = headerInflater.inflate(R.layout.nav_header, null);
        LinearLayout headerLinearLayout = navigationHeader.findViewById(R.id.headerLinearLayout);
        rhymeTextField = findViewById(headerLinearLayout.getChildAt(0).getId());
        searchButton = findViewById(headerLinearLayout.getChildAt(1).getId());

        rhymeTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                selectedWord = s.toString();
            }
        });

        //toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // back button setup
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button functionality(intent)
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        textEditor = findViewById(R.id.editor);
        boldButton = findViewById(R.id.bold_button);
        italicButton = findViewById(R.id.italic);
        underlineButton = findViewById(R.id.underline_button);

        leftAlignButton = findViewById(R.id.left_align_button);
        centerAlignButton = findViewById(R.id.center_align_button);
        rightAlignButton = findViewById(R.id.right_align_button);
        bulletButton = findViewById(R.id.bullet_button);


        //Slide-out menu setup
        //navigationDrawerSetup();
        recyclerView = findViewById(R.id.recyclerView);
        drawerLayout = findViewById(R.id.drawerLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new RecyclerAdapter(rhymeObjectList);
        recyclerView.setAdapter(recyclerAdapter);

        // setting up text editor object
        editorSetup(textEditor);
        editorButtonsFunctionality();
        textEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                //saveText(text);
                HtmlList.set(currentFileIndex, textEditor.getHtml());
            }
        });

        // ADD APPROPRIATE TEXT ACCORDING TO TEXT FILE THAT IS LOADED
        //loadText();
        if(HtmlList.get(currentFileIndex) != null) {
            if (!HtmlList.get(currentFileIndex).equals("")) {
                textEditor.setHtml(HtmlList.get(currentFileIndex));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == searchButton.getId()) {
            Log.d("LOGTAG", "editText content = " + rhymeTextField.getText().toString());
            selectedWord = rhymeTextField.getText().toString();

            AsyncTask asyncTask = new AsyncThread();
            asyncTask.execute(new String[]{selectedWord});

            Log.d("LOGTAG", "AsyncTask executed from searchButton onClick");
        }
    }

    public class AsyncThread extends AsyncTask<String, Integer, ArrayList<RhymeObject>> {
        @Override
        protected ArrayList<RhymeObject> doInBackground(String... strings) {
            Log.d("LOGTAG","doInBackground task entered");

            ArrayList<RhymeObject> rhymeList = new ArrayList<>();


            JSONArray mainJsonArray = new JSONArray();
            try {
                //Establish API Connection
                URL url = new URL("https://rhymebrain.com/talk?function=getRhymes&word=" + strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                //Parse Through JSON

                StringBuffer URLText = new StringBuffer();
                String textLine;
                BufferedReader JSONReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while((textLine = JSONReader.readLine()) != null) {
                    URLText.append(textLine);
                }
                urlConnection.disconnect();
                mainJsonArray = new JSONArray((URLText.toString()));


                if(checkedItems[0] == true) {
                    Log.d("LOGTAG", "All has been selected");
                    for(int i = 0; i < mainJsonArray.length(); i++) {
                        JSONObject currentObject = mainJsonArray.getJSONObject(i);
                        String currentRhymeWord = currentObject.get("word").toString();
                        String currentSyllableNumber = currentObject.get("syllables").toString();
                        if(Integer.parseInt((currentObject.get("score")).toString()) > 156) {
                            rhymeList.add(new RhymeObject(currentRhymeWord, currentSyllableNumber));
                        }
                    }
                }
                else {
                    //Pass through wordnik for metrical feet distinction
                    boolean live = false;
                    if(live) {
                        for (int i = 0; i < mainJsonArray.length(); i++) {
                            try {
                                JSONObject currentObject = mainJsonArray.getJSONObject(i);
                                String currentRhymeWord = currentObject.get("word").toString();
                                int currentSyllableNumber = Integer.parseInt(currentObject.get("syllables").toString());
                                URL stressURL = new URL("https://api.wordnik.com/v4/word.json/" + currentRhymeWord.toLowerCase() + "/hyphenation?useCanonical=false&limit=50&api_key=uf4mbgnf0xxu9qwhazc1h7n2cbfu05785gl7hjud8xuioa8k4");

                                HttpURLConnection wordnikConnection = (HttpURLConnection) stressURL.openConnection();
                                wordnikConnection.setRequestMethod("GET");
                                StringBuffer wordnikText = new StringBuffer();
                                String textLine2;
                                BufferedReader JSONReader2 = new BufferedReader(new InputStreamReader(wordnikConnection.getInputStream()));

                                while ((textLine2 = JSONReader2.readLine()) != null) {
                                    wordnikText.append(textLine2);
                                }
                                wordnikConnection.disconnect();

                                JSONArray wordnikArray = new JSONArray(wordnikText.toString());
                                boolean[] stresses = new boolean[4];
                                if (currentSyllableNumber > 3) {
                                    currentSyllableNumber = 4;
                                }

                                for (int n = 0; n < currentSyllableNumber; n++) {
                                    if (wordnikArray.getJSONObject(n).length() == 3) {
                                        stresses[n] = true;
                                    }
                                }

                                if (Integer.parseInt(currentObject.get("score").toString()) > 156) {
                                    //Iambs
                                    if (checkedItems[1] = true) { //iambs selected
                                        if (currentSyllableNumber == 2 && stresses[0] == false && stresses[1] == true) {
                                            rhymeList.add(new RhymeObject(currentRhymeWord, currentSyllableNumber + ""));
                                        }
                                    }
                                    //Dactyl
                                    if (checkedItems[2] = true) {
                                        if (currentSyllableNumber == 3 && stresses[0] == true && stresses[1] == false && stresses[2] == false) {
                                            rhymeList.add(new RhymeObject(currentRhymeWord, currentSyllableNumber + ""));
                                        }
                                    }
                                    //Trochee
                                    if (checkedItems[3] = true) {
                                        if (currentSyllableNumber == 2 && stresses[0] == true && stresses[1] == false) {
                                            rhymeList.add(new RhymeObject(currentRhymeWord, currentSyllableNumber + ""));
                                        }
                                    }
                                    //Anapest
                                    if (checkedItems[4] = true) {
                                        if (currentSyllableNumber == 3 && stresses[0] == false && stresses[1] == false && stresses[2] == true) {
                                            rhymeList.add(new RhymeObject(currentRhymeWord, currentSyllableNumber + ""));
                                        }
                                    }
                                }

                            } catch (FileNotFoundException e) {
                                Log.d("LOGTAG", "FNF in loop");
                            }
                        }
                    }
                    else {
                        for(int i = 0; i < sampleList.length; i++) {
                            rhymeList.add(sampleList[i]);
                        }
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("LOGTAG", "Malformed URL");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("LOGTAG", "FNF Exception");
            } catch (IOException e) {
                Log.d("LOGTAG", "IOException");
            } catch (JSONException e) {
                Log.d("LOGTAG", "JSONException");
                e.printStackTrace();
            }
            return rhymeList;
        }
            @Override
        protected void onPostExecute(ArrayList<RhymeObject> rhymeList) {

            for(RhymeObject rhymeObject: rhymeList) {
                rhymeObjectList.add(rhymeObject);
            }
            recyclerAdapter.notifyDataSetChanged();
        }
        @Override
        protected void onPreExecute() {
                rhymeObjectList.clear();
                recyclerAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Log.d("LOGTAG", "home item entered");
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            case R.id.adjustScheme:
                openDialogMenu();
                break;
            case R.id.getRhymes:
                //button functionality
                drawerLayout.openDrawer(Gravity.RIGHT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openDialogMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);

        builder.setTitle("Rhyme Filters");
        builder.setMultiChoiceItems(dialogItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch(which) {
                    case 0: // All
                        if(isChecked) {
                            ((AlertDialog) dialog).getListView().setItemChecked(1, true);
                            ((AlertDialog) dialog).getListView().setItemChecked(2, true);
                            ((AlertDialog) dialog).getListView().setItemChecked(3, true);
                            ((AlertDialog) dialog).getListView().setItemChecked(4, true);
                            checkedItems[0] = true;
                        }
                        else {
                            checkedItems[0] = false;
                        }
                        break;
                    case 1: //Iamb
                        if(isChecked) {
                            checkedItems[1] = true;
                        }
                        else {
                            checkedItems[1] = false;
                        }
                        break;
                    case 2: //Dactyl
                        if(isChecked) {
                            checkedItems[2] = true;
                        }
                        else {
                            checkedItems[2] = false;
                        }
                        break;
                    case 3: // Trochee
                        if(isChecked) {
                            checkedItems[3] = true;
                        }
                        else {
                            checkedItems[3] = false;
                        }
                        break;
                    case 4: //Anapest
                        if(isChecked) {
                            checkedItems[4] = true;
                        }
                        else {
                            checkedItems[4] = false;
                        }
                        break;
                }
            }
        });

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        Drawable drawable = getDrawable(R.drawable.dialog_background);
        alertDialog.getWindow().setBackgroundDrawable(drawable);
        alertDialog.show();
    }


    private void editorSetup(RichEditor textEditor) {
        textEditor.setEditorFontSize(14);
        textEditor.setPadding(15, 15, 15,15);
        textEditor.setPlaceholder("Start writing...");
        textEditor.setHtml("");
        textEditor.setVerticalScrollBarEnabled(true);
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        List<RhymeObject> list;

        public RecyclerAdapter (List<RhymeObject> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            View rhymeLayoutView = layoutInflater.inflate(R.layout.rhymelist_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(rhymeLayoutView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RhymeObject rhymeObject = list.get(position);

            final Button rhymeWordButton = holder.rhymeWordButton;
            TextView syllablesView = holder.syllablesView;

            rhymeWordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Rhyme", rhymeWordButton.getText());
                    clipboardManager.setPrimaryClip(clip);
                    Toast toast = Toast.makeText(MainActivity.this, "Rhyme copied to clipboard!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            rhymeWordButton.setText(rhymeObject.getRhyme());
            syllablesView.setText(rhymeObject.getSyllableCount());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends  RecyclerView.ViewHolder {
            public TextView syllablesView;
            public Button rhymeWordButton;

            public ViewHolder(View itemView) {
                super(itemView);
                syllablesView = itemView.findViewById(R.id.syllablesView);
                rhymeWordButton = itemView.findViewById(R.id.rhymeWordButton);
            }
        }
    }

    public class RhymeObject {
        String rhymeWord;
        String syllableCount;
        public RhymeObject(String rhymeWord, String syllableCount) {
            this.rhymeWord = rhymeWord;
            this.syllableCount = syllableCount;
        }
        public String getRhyme() {
            return rhymeWord;
        }
        public String getSyllableCount() {
            return syllableCount;
        }
        public String toString() {
            return rhymeWord + ": " + syllableCount;
        }
    }

    private void editorButtonsFunctionality() {
        // Button Functionality
        boldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setBold();
            }
        });
        italicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setItalic();
            }
        });
        underlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setUnderline();
            }
        });
        leftAlignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setAlignLeft();
            }
        });
        rightAlignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setAlignRight();
            }
        });
        centerAlignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setAlignCenter();
            }
        });
        bulletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEditor.setBullets();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}
