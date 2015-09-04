/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.browser.util;

import android.content.Context;
import android.net.Uri;

import java.util.HashMap;
import java.util.Locale;

/**
 *  Maps a country from the current configuration's locale to a particular Uri.
 *
 *  NOTE: This class is duplicated in other projects. If changes are made here, make sure to make
 *  the same changes elsewhere.
 */
public class DefaultHomePageAndSearchMatcher {
    public static final String BING_SEARCH_KEY = "BING";
    public static final String YAHOO_SEARCH_KEY = "YAHOO";
    public static final String GOOGLE_SEARCH_KEY = "GOOGLE";
    public static final String SEARCH_HOME_OVERRIDE_SYSTEM_PROP = "ro.browser.search_override";
    public static final String SEARCH_HOME_OVERRIDE_GOOGLE = "google";
    public static final String SEARCH_HOME_OVERRIDE_YAHOO = "yahoo";
    public static final String SEARCH_HOME_OVERRIDE_BING = "bing";
    public static final String UNDEFINED_OVERRIDE_PROP = "undefined";
    private static final String AUSTRALIA_COUNTRY_CODE = "AU";
    private static final HomePage sBingHomePage = new BingHomePage();
    private static final HomePage sYahooHomePage = new YahooHomePage();
    private static final HomePage sGoogleHomePage = new GoogleHomePage();
    private static final HomePage sDefaultHomePage = sYahooHomePage;


    private static HashMap<String, HomePage> mLocaleToHomePage = new HashMap<String, HomePage>();
    static {
        mLocaleToHomePage.put(Locale.UK.getCountry(), sBingHomePage);
        mLocaleToHomePage.put(Locale.US.getCountry(), sBingHomePage);
        mLocaleToHomePage.put(Locale.FRANCE.getCountry(), sBingHomePage);
        mLocaleToHomePage.put(Locale.GERMANY.getCountry(), sBingHomePage);
        mLocaleToHomePage.put(Locale.JAPAN.getCountry(), sBingHomePage);
        mLocaleToHomePage.put(AUSTRALIA_COUNTRY_CODE, sBingHomePage);
        mLocaleToHomePage.put(Locale.CANADA.getCountry(), sBingHomePage);
    }


    private static HashMap<String, HomePage> sOverrideToHomePage = new HashMap<String, HomePage>();
    static {
        sOverrideToHomePage.put(SEARCH_HOME_OVERRIDE_GOOGLE, sGoogleHomePage);
        sOverrideToHomePage.put(SEARCH_HOME_OVERRIDE_BING, sBingHomePage);
        sOverrideToHomePage.put(SEARCH_HOME_OVERRIDE_YAHOO, sYahooHomePage);
    }

    private interface HomePage {
        Uri getUri(Context context);
    }

    private static String getSearchOverride() {
        return HomeAndSearchUtils.getSystemProperty(SEARCH_HOME_OVERRIDE_SYSTEM_PROP,
                UNDEFINED_OVERRIDE_PROP);
    }

    public static Uri getHomePageUri(Context context) {
        String overrideString = getSearchOverride();
        if (sOverrideToHomePage.containsKey(overrideString)) {
            return sOverrideToHomePage.get(overrideString).getUri(context);
        }

        HomePage homePage = sDefaultHomePage;
        Locale locale = context.getResources().getConfiguration().locale;
        if (locale != null && locale.getCountry() != null &&
                mLocaleToHomePage.containsKey(locale.getCountry())) {
            homePage = mLocaleToHomePage.get(locale.getCountry());
        }
        return homePage.getUri(context);
    }

    /**
     * Gets the search provider key that indicates which search provider is to be used,
     * based on the current locale.
     * @param context The Context with which to retrieve the locale.
     * @return A String representing the search provider to be used.
     */
    public static String getSearchProvider(Context context) {
        HomePage homePage = sDefaultHomePage;
        Locale locale = context.getResources().getConfiguration().locale;
        if (locale != null && locale.getCountry() != null &&
                mLocaleToHomePage.containsKey(locale.getCountry())) {
            homePage = mLocaleToHomePage.get(locale.getCountry());
        }
        if (homePage == sBingHomePage) {
            return BING_SEARCH_KEY;
        } else if (homePage == sYahooHomePage){
            return YAHOO_SEARCH_KEY;
        } else {
            return GOOGLE_SEARCH_KEY;
        }
    }

    public static class BingHomePage implements HomePage {
        private static final String UNDEFINED_PARTNER = "CY01";
        private static final String PARTNER_CODE_SYSTEM_PROP_BING =
                "ro.browser.search_code_bing";
        private static final String BING_URL = "http://www.bing.com";
        private static final String KEY_PARTNER_CODE = "PC";
        private static final String KEY_FORM_CODE = "FORM";
        private static final String VALUE_FORM_CODE = "CYANDF";

        @Override
        public Uri getUri(Context context) {
            String code = HomeAndSearchUtils.getSystemProperty(PARTNER_CODE_SYSTEM_PROP_BING, UNDEFINED_PARTNER);
            Uri.Builder builder = Uri.parse(BING_URL).buildUpon();
            builder.appendQueryParameter(KEY_PARTNER_CODE, code);
            builder.appendQueryParameter(KEY_FORM_CODE, VALUE_FORM_CODE);
            return builder.build();
        }
    }

    public static class YahooHomePage implements HomePage {
        private static final String UNDEFINED_PARTNER = "cya_oem";
        private static final String PARTNER_CODE_SYSTEM_PROP_YAHOO =
                "ro.browser.search_code_yahoo";
        private static final String YAHOO_URL = "https://search.yahoo.com/";
        private static final String KEY_PARTNER_CODE = ".tsrc";

        @Override
        public Uri getUri(Context context) {
            String code = HomeAndSearchUtils.getSystemProperty(PARTNER_CODE_SYSTEM_PROP_YAHOO, UNDEFINED_PARTNER);
            Uri.Builder builder = Uri.parse(YAHOO_URL).buildUpon();
            builder.appendQueryParameter(KEY_PARTNER_CODE, code);
            return builder.build();
        }
    }

    public static class GoogleHomePage implements HomePage {
        private static final String GOOGLE_URL = "https://www.google.com/";

        @Override
        public Uri getUri(Context context) {
            return Uri.parse(GOOGLE_URL);
        }
    }
}
