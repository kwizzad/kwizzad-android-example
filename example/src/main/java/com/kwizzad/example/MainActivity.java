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
import com.kwizzad.model.PendingCallback;
import com.kwizzad.property.RxSubscriber;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Set<PendingCallback> shownCallbacks = new HashSet<>();
    private TextInputLayout placementIdInput;
    private View preloadButton;
    private View simpleButton;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        Kwizzad.resume(this);

        RxSubscriber.subscribe(this, Kwizzad.pendingCallbacks(), pendingCallbacks -> {
            if (pendingCallbacks.size() > 0) {
                QLog.d("should show callback " + pendingCallbacks);
                // there are multiple callbacks possibly coming. its up to you if you want to show them all now
                // or you want to show them some very different way.
                // maybe its something like a notification for you
                // thats what i will do here

                // we dont wanna show this twice at least for the same session here
                // you can handle that any way you want though
                for (PendingCallback pendingCallback : pendingCallbacks) {
                    if (shownCallbacks.contains(pendingCallback) == false) {
                        QLog.d("showing callback " + pendingCallback);
                        showCallback(pendingCallback);

                        // just showing the first one
                        return;
                    }
                }

                return;
            }
        });
    }

    private void showCallback(final PendingCallback pendingCallback) {
        shownCallbacks.add(pendingCallback);

        new AlertDialog.Builder(this)
                .setTitle("CALLBACK!")
                .setMessage(pendingCallback.toString())
                .setPositiveButton(android.R.string.ok,
                        (dialog, whichButton) -> {
                            Kwizzad.completeCallback(pendingCallback);
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        (dialog, whichButton) -> {
                            dialog.dismiss();
                            shownCallbacks.remove(pendingCallback);
                        }
                )
                .setOnDismissListener(dialog -> shownCallbacks.remove(pendingCallback))
                .create()
                .show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        RxSubscriber.unsubscribe(this);
    }
}
