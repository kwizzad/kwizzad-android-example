package com.kwizzad.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kwizzad.AdDialogFragment;
import com.kwizzad.Kwizzad;
import com.kwizzad.log.QLog;
import com.kwizzad.model.AdState;
import com.kwizzad.property.RxSubscriber;

import java.util.HashMap;
import java.util.Map;

public class PreloadingDialogFragment extends DialogFragment {

    private String placementId;
    private View adButton;
    private TextView statusTextView;

    public static PreloadingDialogFragment create(String placementId) {
        PreloadingDialogFragment fragment = new PreloadingDialogFragment();
        Bundle args = new Bundle();
        args.putString("PLACEMENT_ID", placementId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.placementId = getArguments().getString("PLACEMENT_ID");

        /**
         * now we are requesting an ad for the placement.
         */
        Kwizzad.requestAd(placementId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_ad, container, false);

        adButton = view.findViewById(R.id.adButton);

        statusTextView = ((TextView) view.findViewById(R.id.statusText));

        adButton.setOnClickListener(v -> {

            Map<String, Object> customParams = new HashMap<>();
            customParams.put("foo", "bar");

            AdDialogFragment
                    .newBuilder()
                    /*
                     * dont forget to set the placement id
                     */
                    .setPlacementId(placementId)
                    /*
                     * like this
                     */
                    .setCustomParameters(customParams)
                    /*
                     * or like this
                     */
                    .setCustomParameter("bar", "foo")
                    /*
                     * build it
                     */
                    .build()
                    /*
                     * and show it
                     */
                    .show(getFragmentManager(), "ad");
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adButton.setOnClickListener(null);
        adButton = null;
        statusTextView = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * listen to value, bound to the tag "this"
         */
        RxSubscriber.subscribe(this, Kwizzad.placementState(placementId).observe(), placementState -> {

            QLog.d("got state " + placementState.adState);

            adButton.setVisibility(placementState.adState == AdState.AD_READY ? View.VISIBLE : View.GONE);

            switch (placementState.adState) {
                case NOFILL:
                    statusTextView.setText("no ad for placement " + placementId);

                    new AlertDialog.Builder(getActivity())
                            .setTitle("NOFILL")
                            .setMessage("there is no ad. sorry")
                            .setNeutralButton(android.R.string.ok, (dialog, which) -> {
                                try {
                                    dismiss();
                                } catch (Exception ignored) {
                                }
                            })
                            .create()
                            .show();

                    break;
                case RECEIVED_AD:
                    statusTextView.setText("ad ready to show for placement " + placementId);
                    Kwizzad.prepare(placementId, getActivity());
                    break;
                case DISMISSED:
                    statusTextView.setText("finished showing the ad for placement " + placementId);
                    try {
                        dismiss();
                    } catch (Exception ignored) {
                    }
                    break;
                default:
                    statusTextView.setText("loading the ad for placement " + placementId);
                    break;
            }


        });

    }

    @Override
    public void onPause() {
        super.onPause();


        /**
         * unsubscribe everything on the tag "this"
         */
        RxSubscriber.unsubscribe(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Kwizzad.close(placementId);
    }
}
