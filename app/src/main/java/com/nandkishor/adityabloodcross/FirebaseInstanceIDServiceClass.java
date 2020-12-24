package com.nandkishor.adityabloodcross;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.content.Context.MODE_PRIVATE;

public class FirebaseInstanceIDServiceClass extends FirebaseInstanceIdService {
    SharedPreferences sp;
    SharedPreferences.Editor ed;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        sp = getSharedPreferences("TOKEN",MODE_PRIVATE);
        ed = sp.edit();
        ed.putString("Token",token);
        ed.apply();
    }
}
