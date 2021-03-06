package com.jerbotron_mac.spotishake.activities.settings.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jerbotron_mac.spotishake.R;
import com.jerbotron_mac.spotishake.activities.settings.SettingsPresenter;
import com.jerbotron_mac.spotishake.shared.AppConstants;
import com.jerbotron_mac.spotishake.utils.SharedUserPrefs;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import static com.jerbotron_mac.spotishake.shared.AppConstants.CLIENT_ID;
import static com.jerbotron_mac.spotishake.shared.AppConstants.REDIRECT_URI;
import static com.jerbotron_mac.spotishake.shared.AppConstants.SETTINGS_PREF_REQUEST_CODE;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainPreferencesFragment extends PreferenceFragment {

    private LoginPreference loginPref;
    private SettingsPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
        setHasOptionsMenu(true);

        loginPref = (LoginPreference) findPreference("account_login");
        loginPref.init(this, presenter);

        SwitchPreference autoSavePref = (SwitchPreference) findPreference("auto_save_songs");
        presenter.bindPreferenceSummaryToValue(autoSavePref);

        Preference deleteHistoryPref = findPreference("delete_history");
        deleteHistoryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                onDeleteHistoryClick();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void init(SettingsPresenter presenter) {
        this.presenter = presenter;
    }

    public void openLoginWindow() {
        // request AuthenticationResponse.Type.CODE to do refresh token/auth flow
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(AppConstants.SPOTIFY_CLIENT_SCOPES)
                .build();
        AuthenticationClient.openLoginActivity(getActivity(), SETTINGS_PREF_REQUEST_CODE, request);
    }

    public void handleUserLogin() {
        loginPref.setLoggedIn();
    }

    public void onDeleteHistoryClick() {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        View view = alertDialog.getLayoutInflater().inflate(R.layout.dialog_delete_history, null);

        Button cancel = (Button) view.findViewById(R.id.cancel_delete);
        Button delete = (Button) view.findViewById(R.id.confirm_delete);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.deleteHistory();
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(view);
        alertDialog.show();
    }
}