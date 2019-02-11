package com.niesciur.pawel.remindlyv1;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

//@RunWith(AndroidJUnit4.class)
//public class NoteCreationTest {
//    @Rule
//    ActivityTestRule<NoteListActivity> mNoteListActivityRule =
//            new ActivityTestRule<>(NoteListActivity.class);
//
//    @Test
//    public void createNewNote() {
//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());
//        onView(withId(R.id.fab))
//
//    }
//}