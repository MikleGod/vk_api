package com.example.music.vk_api;

import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

/**
 * Created by Mikle on 16.01.2018.
 */

public class VKApplication extends android.app.Application {



    @Override
    public void onCreate() {
        super.onCreate();

        VKSdk.initialize(this);


    }
}
