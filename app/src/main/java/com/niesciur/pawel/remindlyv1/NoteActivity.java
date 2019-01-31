package com.niesciur.pawel.remindlyv1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_INFO = "com.niesciur.pawel.remindlyv1.NOTE_INFO";
    private NoteInfo mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**  SPINNER ADAPTER
        //  As in: https://developer.android.com/guide/topics/ui/controls/spinner
        */
        Spinner spinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
        //
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //  Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        //  Specify the layout to use when the list of choices appears
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //  Apply the adapter to the spinner
        spinnerCourses.setAdapter(adapterCourses);

        /**READING INTENT CONTENT*/
        readDisplayStateValues();
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNote = intent.getParcelableExtra(NOTE_INFO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
