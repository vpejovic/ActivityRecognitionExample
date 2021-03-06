package si.uni_lj.fri.lrk.activityrecognitionexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static MainActivity mInstance;

    private PendingIntent mPendingIntent;

    private ATBroadcastReceiver mReceiver;

    public static final int REQUEST_ID_ACTIVITY_PERMISSIONS = 1;


    public static MainActivity getInstance(){
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInstance = this;
        mReceiver = new ATBroadcastReceiver();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Activity recognition not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACTIVITY_RECOGNITION)) {

                Log.d(TAG, "Should show");

                Snackbar.make(findViewById(R.id.layout_main), R.string.permission_activity_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat
                                        .requestPermissions(MainActivity.this,
                                                new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                                                REQUEST_ID_ACTIVITY_PERMISSIONS);
                            }
                        })
                        .show();
            } else {

                Log.d(TAG, "Should just request");

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ID_ACTIVITY_PERMISSIONS);
            }

            return;
        }

        setActivityTransitionDetection();
    }

    private void setActivityTransitionDetection(){
        // TODO: List transitions that we want to be notified of
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
        );

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        // TODO: Make an ActivityTransitionRequest with these transitions
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        // TODO: Define an intent that is to be fired when a transition happens
        Intent intent = new Intent(getApplicationContext(),
                DetectedActivitiesIntentService.class);
        mPendingIntent = PendingIntent.getService(getApplicationContext(),
                0, intent, 0);

        // TODO: Use ActivityRecognition client to subscribe to transition updates
        Task<Void> task = ActivityRecognition.getClient(getApplicationContext())
                .requestActivityTransitionUpdates(request, mPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateUI("Subscribed to transition updates");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                updateUI("Failed to subscribe to transition updates "+e.getMessage());
            }
        });

        // TODO: Register BroadcastReceiver to get updates about transitions from the IntentService
        registerReceiver(mReceiver,new IntentFilter(ATBroadcastReceiver.TRANSITIONS_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: Use ActvityRecognition client to unsubscribe from transition updates
        Task<Void> task = ActivityRecognition.getClient(getApplicationContext())
                .removeActivityTransitionUpdates(mPendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        mPendingIntent.cancel();
                        updateUI("Stopping updates successful");
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        updateUI("Stopping updates failed");
                    }
                }
        );

        // TODO: Unregister BroadcastReceiver
        try {unregisterReceiver(mReceiver);}
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private void updateUI(String text) {
        TextView status = findViewById(R.id.tv_status);
        status.setText(text);
    }

    // TODO: BroadcastReceiver to update the UI
    public static class ATBroadcastReceiver extends BroadcastReceiver {

        public static final String TRANSITIONS_RECEIVER_ACTION = "si.uni_lj.fri.lrk.activityrecognitionexample.RECEIVE";

        public static final String TRANSITIONS_TEXT = "transitions_text";
        private static final String TAG = "ATBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");

            String transitionText = intent.getStringExtra(TRANSITIONS_TEXT);

            if (MainActivity.getInstance() != null){
                MainActivity.getInstance().updateUI(transitionText);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_ACTIVITY_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setActivityTransitionDetection();
                }
            }
        }
    }
}
