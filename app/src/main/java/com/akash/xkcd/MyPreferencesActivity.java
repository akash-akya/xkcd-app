/*
 * vachana. An application for Android users, it contains kannada vachanas
 * Copyright (c) 2016. akash
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.akash.xkcd;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.webkit.WebView;

public class MyPreferencesActivity extends AppCompatActivity {
    private static final String TAG = "MyPreferencesActivity";

    public static final String PREF_NIGHT_MODE = "night_mode";
    public static final String PREF_OFFLINE_MODE = "offline_mode";
    public static final String PREF_OFFLINE_DIR = "offline_directory";
    public static final String PREF_OFFLINE_DIR_PATH = "offline_dir_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        getSupportFragmentManager().beginTransaction().replace(R.id.preference_content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        public static final String TAG = "MyPreferenceFragment";

        private SwitchPreferenceCompat nightMode;
        private Preference license;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);

            nightMode = (SwitchPreferenceCompat) getPreferenceManager().findPreference(PREF_NIGHT_MODE);
            nightMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Intent data = new Intent();
                    data.putExtra(PREF_NIGHT_MODE, 1);
                    getActivity().setResult(RESULT_OK, data);
                    return true;
                }
            });

            license = getPreferenceManager().findPreference("license");
            license.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    WebView webView = new WebView(getContext());
                    webView.loadUrl("file:///android_res/raw/copyrights.html");

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setView(webView);
                    dialog.show();

                    return true;
                }
            });
        }

        @Override
        public void setDivider(Drawable divider) {
            super.setDivider(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        public void setDividerHeight(int height) {
            super.setDividerHeight(0);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
