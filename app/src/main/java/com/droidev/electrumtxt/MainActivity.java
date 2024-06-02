package com.droidev.electrumtxt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 1;
    private String sharedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String action = getIntent().getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            handleSendIntent(getIntent());
        }

        Button buttonPasteText = findViewById(R.id.button_paste_text);

        buttonPasteText.setOnClickListener(v -> showPasteTextDialog());
    }

    private void handleSendIntent(Intent intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (sharedText != null) {
            createFile();
        }
    }

    private void showPasteTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Paste TX String");
        builder.setCancelable(false);

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_NULL);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.setCursorVisible(false);
        input.setHint("Double click the input to allow pasting.");

        input.setOnClickListener(v -> {

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setCursorVisible(true);
            input.setSelection(input.getText().length());
        });

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {

            sharedText = input.getText().toString();

            if (!sharedText.isEmpty()) {
                createFile();

            } else {
                Toast.makeText(MainActivity.this, "No text entered", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());

        String fileName = "ElectrumTXT_" + timeStamp + ".txt";

        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                saveTextToFile(uri);
            }
        }
    }

    private void saveTextToFile(Uri uri) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(getContentResolver().openOutputStream(uri)));

            writer.write(sharedText);
            writer.close();

            Toast.makeText(this, "File saved successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }
}
