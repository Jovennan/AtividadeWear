package androidimc.jovennan.com.br.atividadewear;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "Atividade Principal";

    private NotificationManagerCompat mNotificationManagerCompat;

    private int mSelectedNotification = 0;

    private TextView mNotificationDetailsTextView;
    private Spinner mSpinner;

    public static final int NOTIFICATION_ID = 888;

    // Used for Notification Style array and switch statement for Spinner selection.
    private static final String BIG_TEXT_STYLE = "BIG_TEXT_STYLE";
    private static final String BIG_PICTURE_STYLE = "BIG_PICTURE_STYLE";

    // Collection of notification styles to back ArrayAdapter for Spinner.
    private static final String[] NOTIFICATION_STYLES =
            {BIG_TEXT_STYLE, BIG_PICTURE_STYLE};

    private static final String[] NOTIFICATION_STYLES_DESCRIPTION =
            {
                    "Demos reminder type app using BIG_TEXT_STYLE",
                    "Demos social type app using BIG_PICTURE_STYLE + inline notification response"
            };



    private CharSequence[] mPossiblePostResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNotificationDetailsTextView = (TextView) findViewById(R.id.notificationDetails);
        mSpinner = (Spinner) findViewById(R.id.spinner);

        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        NOTIFICATION_STYLES);
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);


        // This would be possible responses based on the contents of the post.
        mPossiblePostResponses = new CharSequence[]{"Sim", "Não", "Talvez?"};
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected(): position: " + position + " id: " + id);

        mSelectedNotification = position;

        mNotificationDetailsTextView.setText(
                NOTIFICATION_STYLES_DESCRIPTION[mSelectedNotification]);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onClick(View view) {

        Log.d(TAG, "onClick()");

        boolean areNotificationsEnabled = mNotificationManagerCompat.areNotificationsEnabled();

        if (!areNotificationsEnabled) {
            // Because the user took an action to create a notification, we create a prompt to let
            // the user re-enable notifications for this application again
            return;
        }

        String notificationStyle = NOTIFICATION_STYLES[mSelectedNotification];

        switch (notificationStyle) {
            case BIG_TEXT_STYLE:
                generateBigTextStyleNotification();
                break;

            case BIG_PICTURE_STYLE:
                generateBigPictureStyleNotification();
                break;

            default:
                // continue below
        }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that supports both phone/tablet and wear. For devices
     * on API level 16 (4.1.x - Jelly Bean) and after, displays BIG_TEXT_STYLE. Otherwise, displays
     * a basic notification.
     */
    private void generateBigTextStyleNotification() {

        Log.d(TAG, "generateBigTextStyleNotification()");

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up main Intent for notification
        //      4. Create additional Actions for the Notification
        //      5. Build and issue the notification


        // 2. Build the BIG_TEXT_STYLE.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                // Overrides ContentText in the big form of the template.
                .bigText(getResources().getString(R.string.big_text))
                // Overrides ContentTitle in the big form of the template.
                .setBigContentTitle(getResources().getString(R.string.big_text_title))
                // Summary line after the detail section in the big form of the template.
                // Note: To improve readability, don't overload the user with info. If Summary Text
                // doesn't add critical information, you should skip it.
                .setSummaryText(getResources().getString(R.string.big_text_summary));


        // 3. Set up main Intent for notification.
        Intent notifyIntent = new Intent(this, BigTextMainActivity.class);



        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // 4. Create additional Actions (Intents) for the Notification.

        // In our case, we create two additional actions: a Snooze action and a Dismiss action.
        // Snooze Action.
        Intent snoozeIntent = new Intent(this, BigTextIntentService.class);
        snoozeIntent.setAction(BigTextIntentService.ACTION_SNOOZE);

        PendingIntent snoozePendingIntent = PendingIntent.getService(this, 0, snoozeIntent, 0);
        NotificationCompat.Action snoozeAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_alarm_white_48dp,
                        "Snooze",
                        snoozePendingIntent)
                        .build();


        // Dismiss Action.
        Intent dismissIntent = new Intent(this, BigTextIntentService.class);
        dismissIntent.setAction(BigTextIntentService.ACTION_DISMISS);

        PendingIntent dismissPendingIntent = PendingIntent.getService(this, 0, dismissIntent, 0);
        NotificationCompat.Action dismissAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_cancel_white_48dp,
                        "Dismiss",
                        dismissPendingIntent)
                        .build();


        // 5. Build and issue the notification.

        // Because we want this to be a new notification (not updating a previous notification), we
        // create a new Builder. Later, we use the same global builder to get back the notification
        // we built here for the snooze action, that is, canceling the notification and relaunching
        // it several seconds later.

        // Notification Channel Id is ignored for Android pre O (26).
        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(
                        getApplicationContext());

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

        Notification notification = notificationCompatBuilder
                // BIG_TEXT_STYLE sets title and content for API 16 (4.1 and after).
                .setStyle(bigTextStyle)
                // Title for API <16 (4.0 and below) devices.
                .setContentTitle(getResources().getString(R.string.big_text_title))
                // Content for API <24 (7.0 and below) devices.
                .setContentText(getResources().getString(R.string.big_text))
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.ic_alarm_white_48dp))
                .setContentIntent(notifyPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Set primary color (important for Wear 2.0 Notifications).
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))

                // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                // devices and all Wear devices. If you have more than one notification and
                // you prefer a different summary notification, set a group key and create a
                // summary notification via
                // .setGroupSummary(true)
                // .setGroup(GROUP_KEY_YOUR_NAME_HERE)

                .setCategory(Notification.CATEGORY_REMINDER)

                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                // visibility is set in the NotificationChannel.
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                // Adds additional actions specified above.
                .addAction(snoozeAction)
                .addAction(dismissAction)

                .build();

        mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    /*
     * Generates a BIG_PICTURE_STYLE Notification that supports both phone/tablet and wear. For
     * devices on API level 16 (4.1.x - Jelly Bean) and after, displays BIG_PICTURE_STYLE.
     * Otherwise, displays a basic notification.
     *
     * This example Notification is a social post. It allows updating the notification with
     * comments/responses via RemoteInput and the BigPictureSocialIntentService on 24+ (N+) and
     * Wear devices.
     */
    private void generateBigPictureStyleNotification() {

        Log.d(TAG, "generateBigPictureStyleNotification()");

        // Main steps for building a BIG_PICTURE_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the BIG_PICTURE_STYLE
        //      3. Set up main Intent for notification
        //      4. Set up RemoteInput, so users can input (keyboard and voice) from notification
        //      5. Build and issue the notification

//
//        // 2. Build the BIG_PICTURE_STYLE.
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                // Provides the bitmap for the BigPicture notification.
                .bigPicture(
                        BitmapFactory.decodeResource(
                                getResources(),
                                R.drawable.earth))
                // Overrides ContentTitle in the big form of the template.
                .setBigContentTitle(getResources().getString(R.string.big_image_title))
                // Summary line after the detail section in the big form of the template.
                .setSummaryText(getResources().getString(R.string.big_image_summary));

        // 3. Set up main Intent for notification.
        Intent mainIntent = new Intent(this, BigPictureSocialMainActivity.class);
//
//        // When creating your Intent, you need to take into account the back state, i.e., what
//        // happens after your Activity launches and the user presses the back button.
//
//        // There are two options:
//        //      1. Regular activity - You're starting an Activity that's part of the application's
//        //      normal workflow.
//
//        //      2. Special activity - The user only sees this Activity if it's started from a
//        //      notification. In a sense, the Activity extends the notification by providing
//        //      information that would be hard to display in the notification itself.
//
//        // Even though this sample's MainActivity doesn't link to the Activity this Notification
//        // launches directly, i.e., it isn't part of the normal workflow, a social app generally
//        // always links to individual posts as part of the app flow, so we will follow option 1.
//
//        // For an example of option 2, check out the BIG_TEXT_STYLE example.
//
//        // For more information, check out our dev article:
//        // https://developer.android.com/training/notify-user/navigation.html

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack.
        stackBuilder.addParentStack(BigPictureSocialMainActivity.class);
        // Adds the Intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent);
        // Gets a PendingIntent containing the entire back stack.
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

//        // 4. Set up RemoteInput, so users can input (keyboard and voice) from notification.
//
//        // Note: For API <24 (M and below) we need to use an Activity, so the lock-screen presents
//        // the auth challenge. For API 24+ (N and above), we use a Service (could be a
//        // BroadcastReceiver), so the user can input from Notification or lock-screen (they have
//        // choice to allow) without leaving the notification.
//
//        // Create the RemoteInput.
        String replyLabel = getString(R.string.reply_label);
        RemoteInput remoteInput =
                new RemoteInput.Builder(BigPictureSocialIntentService.EXTRA_COMMENT)
                        .setLabel(replyLabel)
                        // List of quick response choices for any wearables paired with the phone
                        .setChoices(getPossiblePostResponses())
                        .build();

//        // Pending intent =
//        //      API <24 (M and below): activity so the lock-screen presents the auth challenge
//        //      API 24+ (N and above): this should be a Service or BroadcastReceiver
        PendingIntent replyActionPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(this, null);
            intent.setAction(BigPictureSocialIntentService.ACTION_COMMENT);
            replyActionPendingIntent = PendingIntent.getService(this, 0, intent, 0);

        } else {
            replyActionPendingIntent = mainPendingIntent;
        }

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_reply_white_18dp,
                        replyLabel,
                        replyActionPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

//        // 5. Build and issue the notification.
//
//        // Because we want this to be a new notification (not updating a previous notification), we
//        // create a new Builder. Later, we use the same global builder to get back the notification
//        // we built here for a comment on the post.

        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(getApplicationContext());

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);
//
        notificationCompatBuilder
                // BIG_PICTURE_STYLE sets title and content for API 16 (4.1 and after).
                .setStyle(bigPictureStyle)
                // Title for API <16 (4.0 and below) devices.
                .setContentTitle(getResources().getString(R.string.big_image_title))
                // Content for API <24 (7.0 and below) devices.
                .setContentText(getResources().getString(R.string.big_image_summary))
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.ic_person_black_48dp))
//                .setContentIntent(mainPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Set primary color (important for Wear 2.0 Notifications).
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
//
//                // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
//                // devices and all Wear devices. If you have more than one notification and
//                // you prefer a different summary notification, set a group key and create a
//                // summary notification via
//                // .setGroupSummary(true)
//                // .setGroup(GROUP_KEY_YOUR_NAME_HERE)
//
                .setSubText(Integer.toString(1))
                .addAction(replyAction)
                .setCategory(Notification.CATEGORY_SOCIAL);
//
//                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
//                // 'importance' which is set in the NotificationChannel. The integers representing
//                // 'priority' are different from 'importance', so make sure you don't mix them.
//                .setPriority(bigPictureStyleSocialAppData.getPriority())
//
//                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
//                // visibility is set in the NotificationChannel.
//                .setVisibility(bigPictureStyleSocialAppData.getChannelLockscreenVisibility());
//
//        // If the phone is in "Do not disturb mode, the user will still be notified if
//        // the sender(s) is starred as a favorite.
//        for (String name : bigPictureStyleSocialAppData.getParticipants()) {
//            notificationCompatBuilder.addPerson(name);
//        }
//
        Notification notification = notificationCompatBuilder.build();

        mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }


    public CharSequence[] getPossiblePostResponses() {
        return mPossiblePostResponses;
    }
}
