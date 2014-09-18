/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.com.benasque2014.mercurio.model;

import com.example.android.wizardpager.wizard.model.ModelCallbacks;
import com.example.android.wizardpager.wizard.model.Page;
import com.example.android.wizardpager.wizard.model.ReviewItem;
import com.example.android.wizardpager.wizard.ui.CustomerInfoFragment;
import com.example.com.benasque2014.mercurio.HorasInfoFragment;
import com.example.com.benasque2014.mercurio.PuntosInfoFragment;
import com.example.com.benasque2014.mercurio.RecorridoBasicInfoFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A page asking for a name and an email.
 */
public class PuntosInfoPage extends Page {
    public static final String PUNTOS_DATA_KEY = "puntos";

    public PuntosInfoPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return PuntosInfoFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
    	List<LatLng> list2=PuntosInfoFragment.points;//mData.getParcelable(PuntosInfoPage.PUNTOS_DATA_KEY);
    	if (list2==null)
    		list2=new ArrayList<LatLng>();
        dest.add(new ReviewItem("Numero de puntos en la ruta", list2.size()+"", getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
    	List<LatLng> list2=PuntosInfoFragment.points;//mData.getParcelable(PuntosInfoPage.PUNTOS_DATA_KEY);
        return list2.size()>=2;
    }
}
