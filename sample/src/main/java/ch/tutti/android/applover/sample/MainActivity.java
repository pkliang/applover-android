package ch.tutti.android.applover.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidParameterException;

import ch.tutti.android.applover.AppLover;
import ch.tutti.android.applover.AppLoverDialogStyle;
import ch.tutti.android.applover.criteria.AppLoverAppLaunchCriteria;
import ch.tutti.android.applover.criteria.AppLoverCriteriaBuilder;
import ch.tutti.android.applover.criteria.AppLoverCustomEventCriteria;
import ch.tutti.android.applover.criteria.AppLoverFirstLaunchDaysCriteria;
import ch.tutti.android.applover.criteria.AppLoverInstallDaysCriteria;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String LNBR = "\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup AppLover with custom configuration
        AppLover appLover = AppLover.get(this);

        // Basic configuration. Email is needed. Analytics and style are optional.
        appLover.setFeedbackEmail("support@yourapp.com")
                .setOnTrackListener(new SampleOnTrackListener())
                .setStyle(new AppLoverDialogStyle()
                        .loveStyle(new AppLoverDialogStyle.Style()
                                .positiveBackground(R.drawable.button_positive)
                                .negativeBackground(R.drawable.button_negative))
                        .rateStyle(new AppLoverDialogStyle.Style()
                                .positiveBackground(R.drawable.button_positive))
                        .emailStyle(new AppLoverDialogStyle.Style()
                                .positiveBackground(R.drawable.button_positive)));

        // Setting up of thresholds
        // All of the thresholds have to happen for the dialog to trigger
        appLover.setFirstLaunchDaysThreshold(0) // default 10, 0 means same day.
                .setInstallDaysThreshold(0)     // default 10
                .setLaunchCountThreshold(3)     // default 10
                .setCustomEventCountThreshold("1", 3)  // default 0
                .setCustomEventCountThreshold("2", 3); // default 0

        // set up custom criteria example
        // default is every criteria with AND
        // install days && first launch days && app launch && (event 1 || event 2)
        appLover.setShowDialogCriteria(
                new AppLoverCriteriaBuilder(
                        new AppLoverInstallDaysCriteria()
                ).and(
                        new AppLoverFirstLaunchDaysCriteria()
                ).and(
                        new AppLoverAppLaunchCriteria()
                ).and(
                        new AppLoverCriteriaBuilder(
                                new AppLoverCustomEventCriteria("1")
                        ).or(
                                new AppLoverCustomEventCriteria("2")
                        ).build()
                ).build()
        );

        // Optional to track with analytics
        appLover.setOnTrackListener(new AppLover.OnTrackListener() {
            @Override
            public void onTrackDialogShown(int dialogType) {
                toast(dialogType, "SHOW");
            }

            @Override
            public void onTrackDialogCanceled(int dialogType) {
                toast(dialogType, "CANCEL");
            }

            @Override
            public void onTrackDialogButtonPressed(int dialogType, String button) {
                toast(dialogType, "BUTTON: " + button);
            }

            private void toast(int dialogType, String action) {
                Toast.makeText(MainActivity.this,
                        getDialogTypeString(dialogType) + ": " + action, Toast.LENGTH_SHORT).show();
            }

            private String getDialogTypeString(int dialogType) {
                switch (dialogType) {
                    case AppLover.DIALOG_TYPE_FIRST:
                        return "Love";
                    case AppLover.DIALOG_TYPE_RATE:
                        return "Rate";
                    case AppLover.DIALOG_TYPE_EMAIL:
                        return "Email";
                }
                throw new InvalidParameterException("dialogType is incorrect");
            }
        });

        // Monitor launch
        appLover.monitorLaunch(this);

        // Show a dialog if meets conditions
        appLover.showDialogIfConditionsMet(this);

        findViewById(R.id.button_event1).setOnClickListener(this);
        findViewById(R.id.button_event2).setOnClickListener(this);
        findViewById(R.id.button_clear).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCurrentStats();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_event1:
                AppLover.get(this).monitorCustomEvent(this, "1");
                showCurrentStats();
                break;
            case R.id.button_event2:
                AppLover.get(this).monitorCustomEvent(this, "2");
                showCurrentStats();
                break;
            case R.id.button_clear:
                AppLover.get(this).reset(this);
                showCurrentStats();
                break;
        }
    }

    public void showCurrentStats() {
        AppLover appLover = AppLover.get(this);

        TextView text = (TextView) findViewById(R.id.text);
        text.setText("");
        text.append("Do not show?:\t\t\t\t" + appLover.isDoNotShowAnymore(this) + LNBR);
        text.append("App launch count:\t" + appLover.getLaunchCount() + "/" + appLover
                .getLaunchCountThreshold() + LNBR);
        text.append("Days since install:\t" + appLover.getInstalledDays(this) + "/"
                + appLover.getInstallDaysThreshold() + LNBR);
        text.append("Days since first launch:\t" + appLover.getDaysSinceFirstLaunch() + "/"
                + appLover.getFirstLaunchDaysThreshold() + LNBR);
        text.append("Custom event 1:\t\t" + appLover.getCustomEventCount(this, "1") + "/" + appLover
                .getCustomEventCountThreshold("1") + LNBR);
        text.append("Custom event 2:\t\t" + appLover.getCustomEventCount(this, "2") + "/" + appLover
                .getCustomEventCountThreshold("2") + LNBR);
    }

    public static class SampleOnTrackListener implements AppLover.OnTrackListener {

        @Override
        public void onTrackDialogShown(int dialogType) {
            Log.i(TAG, "SHOWING: " + getDialogTypeString(dialogType) + " Dialog");
        }

        @Override
        public void onTrackDialogCanceled(int dialogType) {
            Log.i(TAG, "CANCELED: " + getDialogTypeString(dialogType) + " Dialog");
        }

        @Override
        public void onTrackDialogButtonPressed(int dialogType, String button) {
            Log.i(TAG, "PRESSED: " + button + " Button on " + getDialogTypeString(dialogType)
                    + " Dialog");
        }

        private String getDialogTypeString(int dialogType) {
            switch (dialogType) {
                case AppLover.DIALOG_TYPE_FIRST:
                    return "Love";
                case AppLover.DIALOG_TYPE_RATE:
                    return "Rate";
                case AppLover.DIALOG_TYPE_EMAIL:
                    return "Email";
            }
            throw new InvalidParameterException("dialogType is incorrect");
        }
    }
}