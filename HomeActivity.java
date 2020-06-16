package com.example.wordprocessor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.wordprocessor.MainActivity.fileNames;
import static com.example.wordprocessor.MainActivity.fileNumber;


public class HomeActivity extends AppCompatActivity {
    public static int currentFileIndex = 0;
    public static ArrayList<String> HtmlList = new ArrayList<>();

    public static ListView filesListView;
    FloatingActionButton createFileButton;
    public static ArrayList<FileObject> files = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        filesListView = findViewById(R.id.files_listview);
        createFileButton = findViewById(R.id.floatingActionButton);

        View view = findViewById(R.id.textView3);
        filesListView.setEmptyView(view);

        final CustomAdapter customAdapter = new CustomAdapter(this, R.layout.activity_files, files);
        filesListView.setAdapter(customAdapter);

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentFileIndex = position;
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        createFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add a file

                HtmlList.add("");
                fileNumber++;
                String newFileName = "File" + (fileNumber) + ".txt";
                fileNames.add(newFileName);
                files.add(new FileObject("Current Date", "File " + (fileNumber), fileNumber, ""));
                customAdapter.notifyDataSetChanged();
                // start intent of word processor
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

    }
    public class CustomAdapter extends ArrayAdapter<FileObject>{
        Context context;
        List<FileObject> previewObjectList;
        int xmlResource;

        public CustomAdapter(@NonNull Context context, int resource, @NonNull List<FileObject> objects) {
            super(context, resource, objects);
            this.context = context;
            previewObjectList = objects;
            xmlResource = resource;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            final View adapterView = inflater.inflate(xmlResource, null);

            TextView dateView = adapterView.findViewById(R.id.date_view);
            TextView preview = adapterView.findViewById(R.id.preview);

            FileObject currentObject = previewObjectList.get(position);

            dateView.setText(currentObject.getDate());
            preview.setText(currentObject.getPreviewText());

            return adapterView;

        }
    }
    public class FileObject {
        String date;
        String previewText;
        int fileIndex;
        String htmlContent;
        public FileObject(String date, String previewText, int fileIndex, String htmlContent) {
            this.date = date;
            this.previewText = previewText;
            this.fileIndex = fileIndex;
            this.htmlContent = htmlContent;
        }
        public String getDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
            Date currentDate = Calendar.getInstance().getTime();
            return sdf.format(currentDate);
        }
        public String getPreviewText() {
            return previewText;
        }
        public int getFileIndex() {
            return fileIndex;
        }
        public String getHtmlContent() {
            return htmlContent;
        }
    }
}
