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
import com.kwizzad.model.OpenTransaction;
import com.kwizzad.model.events.Reward;
import com.kwizzad.property.RxSubscriber;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Set<OpenTransaction> shownEvents = new HashSet<>();
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
                            .show(getFragmentManager(), "preload");
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

        RxSubscriber.subscribe(this, Kwizzad.pendingTransactions(), pendingEvents -> {
            if (pendingEvents.size() > 0) {
                QLog.d("should show event " + pendingEvents);
                // there are multiple callbacks possibly coming. its up to you if you want to show them all now
                // or you want to show them some very different way.
                // maybe its something like a notification for you
                // thats what i will do here

                // we dont wanna show this twice at least for the same session here
                // you can handle that any way you want though
                for (OpenTransaction openTransaction : pendingEvents) {
                    if (shownEvents.contains(openTransaction) == false) {
                        QLog.d("showing event " + openTransaction);

                        Reward reward = openTransaction.reward;
                        if (reward != null)
                        {
                            // Here you get the reward amount for the user.
                            int rewardAmount = reward.amount;
                            String rewardCurrency = reward.currency; // e.g. chips, coins, loot, smiles as configued by KWIZZAD.

                            /*

                            The following part is optional: If you want to know which type of reward the notification was about
                            How to distinguish between Call2Action (Instant-Reward) and Callback (full billing)

                            Reward.Type rewardType = reward.type; // CALL2ACTIONSTARTED, CALLBACK, GOALREACHED

                            if (openTransaction.reward.type.equals(Reward.Type.CALL2ACTIONSTARTED)) { ... }

                            else if (openTransaction.reward.type.equals(Reward.Type.CALLBACK)) { ... }

                          */

                        }

                        showEvent(openTransaction);
                    }
                    else {
                        QLog.d("already shown event "+openTransaction);
                    }
                }
            }
        });
    }

    private void showEvent(final OpenTransaction openTransaction) {
        shownEvents.add(openTransaction);


        new AlertDialog.Builder(this)
                .setTitle(openTransaction.reward.type+" !!!")
                .setMessage(openTransaction.toString())
                .setPositiveButton(android.R.string.ok,
                        (dialog, whichButton) -> {
                            Kwizzad.completeTransaction(openTransaction);
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        (dialog, whichButton) -> {
                            dialog.dismiss();
                            shownEvents.remove(openTransaction);
                        }
                )
                .setOnDismissListener(dialog -> shownEvents.remove(openTransaction))
                .create()
                .show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        RxSubscriber.unsubscribe(this);
    }
}
