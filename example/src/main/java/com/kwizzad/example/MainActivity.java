package com.kwizzad.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.kwizzad.Kwizzad;
import com.kwizzad.log.QLog;
import com.kwizzad.model.PendingEvent;
import com.kwizzad.model.events.Reward;
import com.kwizzad.property.RxSubscriber;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Set<PendingEvent> shownEvents = new HashSet<>();
    private TextInputLayout placementIdInput;
    private View preloadButton;
    private View simpleButton;
    private View simplePreloadButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        placementIdInput = (TextInputLayout) findViewById(R.id.placementId);

        simpleButton = findViewById(R.id.simple);
        if (simpleButton != null) {
            simpleButton.setOnClickListener(v -> {
                if (Kwizzad.initialized().get()) {
                    startActivity(SimpleActivity.createIntent(MainActivity.this, placementIdInput.getEditText().getText().toString()));
                } else {
                    Log.e("KWIZZAD", "not initialized");
                    Snackbar.make(findViewById(R.id.coordinatorLayout), "kwizzad not initialized", Snackbar.LENGTH_INDEFINITE).show();
                }
            });
        }

        preloadButton = findViewById(R.id.preload);
        if (preloadButton != null) {
            preloadButton.setOnClickListener(v -> {
                if (Kwizzad.initialized().get()) {
                    PreloadingDialogFragment.create(placementIdInput.getEditText().getText().toString())
                            .show(getSupportFragmentManager(), "preload");
                } else {
                    Log.e("KWIZZAD", "not initialized");
                    Snackbar.make(findViewById(R.id.coordinatorLayout), "kwizzad not initialized", Snackbar.LENGTH_INDEFINITE).show();
                }
            });
        }

        simplePreloadButton = findViewById(R.id.simplePreload);
        if(simplePreloadButton != null) {
            simplePreloadButton.setOnClickListener(v -> {
                if (Kwizzad.initialized().get()) {
                    startActivity(SimplePreloadingExample.createIntent(this, placementIdInput.getEditText().getText().toString()));
                } else {
                    Log.e("KWIZZAD", "not initialized");
                    Snackbar.make(findViewById(R.id.coordinatorLayout), "kwizzad not initialized", Snackbar.LENGTH_INDEFINITE).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Kwizzad.resume(this);

        RxSubscriber.subscribe(this, Kwizzad.pendingEvents(), pendingEvents -> {
            if (pendingEvents.size() > 0) {
                QLog.d("should show event " + pendingEvents);
                // there are multiple callbacks possibly coming. its up to you if you want to show them all now
                // or you want to show them some very different way.
                // maybe its something like a notification for you
                // thats what i will do here

                // we dont wanna show this twice at least for the same session here
                // you can handle that any way you want though
                for (PendingEvent pendingEvent : pendingEvents) {
                    if (shownEvents.contains(pendingEvent) == false) {
                        QLog.d("showing event " + pendingEvent);

                        // How to distinguish between Call2Action (Instant-Reward) and Callback (full billing)

                        if (pendingEvent.type.equals(PendingEvent.Type.CALL2ACTION))
                        {
                            // The user has completed the kwizzad and can receive an instant reward (based on guarantee payment if agreed)
                            // Currently this event does not contain a specific reward amount, so you'll have to set this manually based on contractual agreement with KWIZZAD
                            final int instantRewardAmount = 5000;
                            // now you can reward the user
                        } else if (pendingEvent.type.equals(PendingEvent.Type.CALLBACK))
                        {
                            // The advertising partner has confirme the transaction. The user can be rewarded the full amount
                            Reward reward = pendingEvent.reward;
                            int rewardAmount = reward.amount;
                            String rewardCurrency = reward.currency; // e.g. chips, coins, loot, smiles as configued by KWIZZAD.
                        }

                        showEvent(pendingEvent);

                        // just showing the first one
                        return;
                    }
                    else {
                        QLog.d("already shown event "+pendingEvent);
                    }
                }
            }
        });
    }

    private void showEvent(final PendingEvent pendingEvent) {
        shownEvents.add(pendingEvent);


        new AlertDialog.Builder(this)
                .setTitle(pendingEvent.type+" !!!")
                .setMessage(pendingEvent.toString())
                .setPositiveButton(android.R.string.ok,
                        (dialog, whichButton) -> {
                            Kwizzad.completeEvent(pendingEvent);
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        (dialog, whichButton) -> {
                            dialog.dismiss();
                            shownEvents.remove(pendingEvent);
                        }
                )
                .setOnDismissListener(dialog -> shownEvents.remove(pendingEvent))
                .create()
                .show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        RxSubscriber.unsubscribe(this);
    }
}
