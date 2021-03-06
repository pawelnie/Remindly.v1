package com.niesciur.pawel.remindlyv1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.view.View;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.niesciur.pawel.remindlyv1.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_POSITION = "com.niesciur.pawel.remindlyv1.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.niesciur.pawel.remindlyv1.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.niesciur.pawel.remindlyv1.ORIGINAL_NOTE_COURSE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.niesciur.pawel.remindlyv1.ORIGINAL_NOTE_COURSE_TEXT";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCanceling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        /**  SPINNER ADAPTER
        //  As in: https://developer.android.com/guide/topics/ui/controls/spinner
        */
        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
        //
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //  Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        //  Specify the layout to use when the list of choices appears
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //  Apply the adapter to the spinner
        mSpinnerCourses.setAdapter(adapterCourses);

        /**READING INTENT CONTENT*/
        readDisplayStateValues();
        if(savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        /**
         * References to editable text fields
         */
        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        /**Method that copies values of mNote fields to new fields that are connected to UI elements
        if added to account for creation of new note via fab button */
        if(!mIsNewNote)
            loadNoteData();

        Log.d(TAG, "onCreate");
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
                + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";
        String[] selectionArgs = {courseId, titleStart + "%"};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCanceling) {
            Log.i(TAG, "Cancelling note at position: " + mNotePosition);
            if(mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }

        Log.d(TAG, "onPause");

    }

    private void storePreviousNoteValues() {
            CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
            mNote.setCourse(course);
            mNote.setTitle(mOriginalNoteTitle);
            mNote.setText(mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex = courses.indexOf(course);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    /**
     * Intent passes parcelable object which is instance of NoteInfo, reference to this object is kept in NOTE_POSITION
     * Below method receives intent's extra content and saves in mNote object
     */
    private void readDisplayStateValues() {
        //When selecting existing note on NoteListActivity, so extra is not null:
        Intent intent = getIntent();
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        //When creating new Note via fab, we recognize that it is new by checking if mNote == null because no extra is provide with intent
        mIsNewNote = mNotePosition == POSITION_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
        Log.i(TAG, "mNotePosition: " + mNotePosition);
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
//        mNote = dm.getNotes().get(mNotePosition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id ==R.id.action_cancel) {
            mIsCanceling = true;
            finish();
        } else if(id == R.id.action_next) {
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     *
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNotePosition < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    /**Sending Note in an email*/
    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);


    }
}






































