package si.uni_lj.fri.lrk.activityrecognitionexample;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;

public class DetectedActivitiesIntentService extends IntentService {

    private static final String TAG = "DetectedActivitiesIS";

    public DetectedActivitiesIntentService() {
        super(TAG);
    }


    private static String activityToString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.IN_VEHICLE:
                return "IN VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.TILTING:
                return "TILTING";
            default:
                return "UNKNOWN";
        }
    }

    private static String transitionToString(int transition) {
        switch (transition) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "onHandleIntent");

        // TODO: Extract result from the intent
        ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);


        if (result != null) {

            Log.d(TAG, "result not null");

            StringBuilder output = new StringBuilder();

            for (ActivityTransitionEvent event : result.getTransitionEvents()) {

                String info = "Transition: " + activityToString(event.getActivityType()) +
                        " (" + transitionToString(event.getTransitionType()) + ")" + "   " +
                        new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());

                output.append(info).append("\n");

            }


            Log.d(TAG, output.toString());

            // TODO: Send the transition descriptions to MainActivity via Broadcast
            Intent intentBcast = new Intent(MainActivity.ATBroadcastReceiver.TRANSITIONS_RECEIVER_ACTION);
            intentBcast.putExtra(MainActivity.ATBroadcastReceiver.TRANSITIONS_TEXT,
                    output.toString());
            sendBroadcast(intentBcast);

        }
    }
}
