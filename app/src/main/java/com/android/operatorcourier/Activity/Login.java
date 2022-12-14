package com.android.operatorcourier.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.operatorcourier.Api.APIClient;
import com.android.operatorcourier.Api.ApiInterface;
import com.android.operatorcourier.Common.Constant;
import com.android.operatorcourier.Common.Utils;
import com.android.operatorcourier.Model.GBody;
import com.android.operatorcourier.Model.LoginBody;
import com.android.operatorcourier.R;
import com.android.operatorcourier.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        checkInternet();
        setContentView(binding.getRoot());
        if(!Utils.getSharedPreference(context,"token").isEmpty()){
            finish();
            startActivity(new Intent(context,MainActivity.class));
        }
        setListener();
    }

    private void setListener() {
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInternet();
                String username=binding.username.getText().toString();
                String password=binding.password.getText().toString();
                if(Constant.isConnected){
                    Dialog dialog=Utils.getDialogProgressBar(context);
                    dialog.show();

                    if(username.trim().isEmpty() || password.trim().isEmpty()){
                        Toast.makeText(context, "Maglumatlary doly girizin!", Toast.LENGTH_SHORT).show();
                    } else {
                        ApiInterface apiInterface= APIClient.getClient().create(ApiInterface.class);
                        Call<GBody<com.android.operatorcourier.Model.Login>> call=apiInterface.login(new LoginBody(username,password, Utils.getSharedPreference(context,"notif_token")));
                        call.enqueue(new Callback<GBody<com.android.operatorcourier.Model.Login>>() {
                            @Override
                            public void onResponse(Call<GBody<com.android.operatorcourier.Model.Login>> call, Response<GBody<com.android.operatorcourier.Model.Login>> response) {
                                if(response.isSuccessful() && response.body()!=null && response.body().getBody()!=null){
                                    com.android.operatorcourier.Model.Login body=response.body().getBody();
                                    Utils.setPreference("fullname",body.getFullname(),context);
                                    Utils.setPreference("phone_number",body.getPhone_number(),context);
                                    Utils.setPreference("unique_id",body.getUnique_id(),context);
                                    Utils.setPreference("sell_point_id",body.getSell_point_id(),context);
                                    Utils.setPreference("token",body.getToken(),context);
                                    Utils.setPreference("username",username,context);
                                    Utils.setPreference("password",password,context);
                                    finish();
                                    startActivity(new Intent(context,MainActivity.class));
                                } else {
                                    Toast.makeText(Login.this, "Ulanyjy adynyz ya-da acar sozuniz nadogry!", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<GBody<com.android.operatorcourier.Model.Login>> call, Throwable t) {
                                Toast.makeText(Login.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                } else {
                    if(Utils.getSharedPreference(context,"password").isEmpty() || Utils.getSharedPreference(context,"username").isEmpty()){
                        Toast.makeText(context, "Offline maglumatlar tapylmady!", Toast.LENGTH_SHORT).show();
                    } else {
                        if(Utils.getSharedPreference(context,"password").equals(password) && Utils.getSharedPreference(context,"username").equals(username)){
                            finish();
                            startActivity(new Intent(context,MainActivity.class));
                        } else {
                            Toast.makeText(Login.this, "Ulanyjy adynyz ya-da acar sozuniz nadogry!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private void checkInternet() {
        // Initialize connectivity manager
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        // get connection status
        Constant.isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}