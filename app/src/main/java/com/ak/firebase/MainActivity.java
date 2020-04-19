package com.ak.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

    public class MainActivity extends AppCompatActivity {

        private static final int RC_SIGN_IN = 1000;
        Button button;
        EditText editText;
        ArrayList<String> notes;
        ListView listView;
        FirebaseUser firebaseUser;
        ArrayAdapter<String> arrayAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            button = findViewById(R.id.btnDb);
            editText = findViewById(R.id.edtDb);
            listView = findViewById(R.id.lsview);
            notes = new ArrayList<>();

            arrayAdapter = new ArrayAdapter<String>(this,
                    R.layout.items_row,
                    R.id.tvitemsrow, notes);

            listView.setAdapter(arrayAdapter);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null){
                addListeners();
            }

            else {
                //loggedout

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.PhoneBuilder().build())).build(),
                        RC_SIGN_IN);

            }

        }


        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
            if (requestCode == RC_SIGN_IN) {
                IdpResponse response = IdpResponse.fromResultIntent(data);

                // Successfully signed in
                if (resultCode == RESULT_OK)
                {
                    addListeners();
                }
                else {
                    // Sign in failed
                    if (response == null) {
                        // User pressed back button
                        return;
                    }

                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {

                        return;
                    }

                }
            }
        }

        public void addListeners(){
            final DatabaseReference dbreference = FirebaseDatabase.getInstance().getReference();
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText = findViewById(R.id.edtDb);
                    String note = editText.getText().toString();


                    //upload the note to firebase
                    dbreference.child("note").push().setValue(note);
                    Toast.makeText(getApplicationContext(), "Note Has been updated successfully", Toast.LENGTH_SHORT).show();

                }
            });


            dbreference.child("note").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String data = dataSnapshot.getValue(String.class);
                    notes.add(data);
                    arrayAdapter.notifyDataSetChanged();

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }
