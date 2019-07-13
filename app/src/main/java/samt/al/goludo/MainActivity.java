package samt.al.goludo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
{

    public static FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        getSupportFragmentManager().beginTransaction().replace(R.id.contentContainer, new HomeFrag()).commit();
        final String username = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_account_username", "");
        final ProgressBar progressBar = findViewById(R.id.progress);
        final FrameLayout frameLayout = findViewById(R.id.contentContainer);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment settingsfrag = getFragmentManager().findFragmentByTag("settings");
                if(settingsfrag != null && settingsfrag.isVisible())
                {
                    getFragmentManager().popBackStack();
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    getSupportFragmentManager().beginTransaction().replace(R.id.contentContainer, new HomeFrag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.controller));
                }
                else
                {
                    if ((!getSupportFragmentManager().findFragmentById(R.id.contentContainer).equals(new PlayFrag()))) {
                        if (username.isEmpty()) {
                            Toast.makeText(MainActivity.this, "You haven't chosen a username yet.", Toast.LENGTH_LONG).show();
                            getFragmentManager().beginTransaction().replace(R.id.contentContainer, new SettingsFrag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                        } else {
                            Toast.makeText(MainActivity.this, "Joining as " + username.replace(username.substring(0, 1), username.substring(0, 1).toUpperCase()), Toast.LENGTH_LONG).show();
                            frameLayout.setAlpha((float) 0.5);
                            progressBar.setVisibility(View.VISIBLE);
                            final String selurl = "login?player=" + username;
                            @SuppressLint("StaticFieldLeak") final AsyncTask<String, Void, String> logintask = new AsyncTask<String, Void, String>() {
                                @Override
                                protected String doInBackground(String... url) {
                                    String baseurl = "http://192.168.43.205/";
                                    OkHttpClient client = new OkHttpClient();

                                    Request request = new Request.Builder()
                                            .url(baseurl + selurl)
                                            .build();

                                    CallbackFuture future = new CallbackFuture();
                                    client.newCall(request).enqueue(future);
                                    Response response = null;
                                    try {
                                        response = future.get();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        return (response != null ? response.body().string().trim() : "");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return "";
                                }

                                @Override
                                protected void onPostExecute(String string) {
                                    if (string.isEmpty())
                                        Toast.makeText(MainActivity.this, "There was an error while logging into the server.", Toast.LENGTH_LONG).show();
                                    else if (!string.contains("Error")) {
                                        fab.setVisibility(View.GONE);
                                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("player", string).commit();
                                        getSupportFragmentManager().beginTransaction().replace(R.id.contentContainer, new PlayFrag(),"play").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                                    } else
                                        Toast.makeText(MainActivity.this, string, Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    frameLayout.setAlpha(1);

                                }
                            };
                            logintask.execute();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}


