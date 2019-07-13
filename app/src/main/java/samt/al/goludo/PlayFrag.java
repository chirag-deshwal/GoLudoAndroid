package samt.al.goludo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.text.TextUtils.split;

public class PlayFrag extends Fragment
{
    int totalmoves = 0;
    int moverolled = 0;

    String username = "";
    String player;
    String selurl;

    String piece1lastpos = "";
    String piece2lastpos = "";
    String piece3lastpos = "";
    String piece4lastpos = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    { return inflater.inflate(R.layout.fragment_play, container, false); }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState)
    {
        username = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_account_username", "");
        player = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("player","");

        final Button piece1 = view.findViewById(R.id.piece1);
        final Button piece2 = view.findViewById(R.id.piece2);
        final Button piece3 = view.findViewById(R.id.piece3);
        final Button piece4 = view.findViewById(R.id.piece4);

        final Chronometer cmTimer = view.findViewById(R.id.time);
        cmTimer.setFormat("%s");
        cmTimer.setBase(SystemClock.elapsedRealtime() + 15000);
        cmTimer.setCountDown(true);
        cmTimer.start();

        setText(piece1,1, "");
        setText(piece2,2, "");
        setText(piece3,3, "");
        setText(piece4,4, "");

        final TextView ranking = view.findViewById(R.id.ranking);
        Button roller = view.findViewById(R.id.roll);
        final TextView moves = view.findViewById(R.id.moves);
        final TextView playertext = view.findViewById(R.id.username);
        LinearLayout playlayout = view.findViewById(R.id.play_layout);

        moves.setText(String.valueOf(totalmoves));
        ranking.setText("#?");
        roller.setClickable(false);
        roller.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
        roller.setText("LOADING");

        final Button roll = roller;

        switch (player)
        {
            case "Red":
                playlayout.setBackground(getResources().getDrawable(R.drawable.wallpaper3red));
                break;
            case "Green":
                playlayout.setBackground(getResources().getDrawable(R.drawable.wallpaper3green));
                break;
            case "Blue":
                playlayout.setBackground(getResources().getDrawable(R.drawable.wallpaper3blue));
                break;
            case "Yellow":
                playlayout.setBackground(getResources().getDrawable(R.drawable.wallpaper3yellow));
                break;
        }

        playertext.setText("Waiting for other players:");

        final FloatingActionButton rollfab = view.findViewById(R.id.rollfab);

        rollfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selurl = "roll?player=" + username;
                @SuppressLint("StaticFieldLeak") AsyncTask<String,Void,String> task = new AsyncTask<String,Void,String>() {
                    @Override
                    protected String doInBackground(String... url) {
                        String baseurl ="http://192.168.43.205/";
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
                            return response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                    @Override
                    protected void onPostExecute(String string)
                    {
                        if(string.contains(","))
                        {
                            String[] splitted = split(string,",");
                            TextView roller = view.findViewById(R.id.roll);
                            roller.setText("YOU rolled " + splitted[4].trim());
                            roller.setClickable(false);
                            roller.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
                            moverolled = Integer.valueOf(splitted[4].trim());
                            if(splitted[0].equals(splitted[1]) && splitted[1].equals(splitted[2]) && splitted[2].equals(splitted[3]) && splitted[3].equals("false"))
                            {
                                setText(piece1,1,piece1lastpos);
                                setText(piece2,2,piece2lastpos);
                                setText(piece3,3,piece3lastpos);
                                setText(piece4,4,piece4lastpos);
                            }
                            else
                            {
                                setText(piece1,1,splitted[0]);
                                setText(piece2,2,splitted[1]);
                                setText(piece3,3,splitted[2]);
                                setText(piece4,4,splitted[3]);
                            }
                        }
                        else Toast.makeText(getContext(),"There was an error while communicating with the server.",Toast.LENGTH_LONG).show();
                    }
                };
                task.execute();
            }
        });


        final Handler h = new Handler();
        final Runnable myrunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(cmTimer.isCountDown())
                {
                    cmTimer.stop();
                    cmTimer.setBase(SystemClock.elapsedRealtime());
                    cmTimer.setCountDown(false);
                    cmTimer.start();
                    playertext.setText(player + " player: " + username.replace(username.substring(0,1),username.substring(0,1).toUpperCase()));
                    rollfab.setClickable(false);
                    roll.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
                }
                selurl = "wait?player=" + username;
                @SuppressLint("StaticFieldLeak") final AsyncTask<String,Void,String> movetask = new AsyncTask<String,Void,String>() {
                    @Override
                    protected String doInBackground(String... url) {
                        String baseurl ="http://192.168.43.205/";
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
                    @SuppressLint("RestrictedApi")
                    @Override
                    protected void onPostExecute(String string)
                    {
                        if(string.contains(","))
                        {
                            String[] splitted = split(string, ",");
                            if(splitted[0].equals("true"))
                            {
                                rollfab.setClickable(true);
                                roll.setTextColor(getResources().getColor(android.R.color.black));
                                roll.setText("TAP TO ROLL");
                                // Get instance of Vibrator from current Context
                                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(50);
                                ranking.setText("#" + ( splitted[1] != null && !splitted[1].isEmpty() && (Integer.valueOf(splitted[1]) < 5 || Integer.valueOf(splitted[1]) > 0) ? Integer.valueOf(splitted[1]) : "?" ));
                            }
                            else if(splitted[0].equals("end"))
                            {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                                sharedPreferences.edit().putInt("moves",sharedPreferences.getInt("moves", 0) + totalmoves).commit();
                                sharedPreferences.edit().putInt("ranking",sharedPreferences.getInt("ranking", 0) + Integer.valueOf(splitted[1])).commit();
                                sharedPreferences.edit().putLong("time", sharedPreferences.getLong("time", 0) + (SystemClock.elapsedRealtime() - cmTimer.getBase())).commit();
                                if(Integer.valueOf(splitted[1]) > 1) sharedPreferences.edit().putInt("losses",sharedPreferences.getInt("losses", 0) + 1).commit();
                                else sharedPreferences.edit().putInt("wins",sharedPreferences.getInt("wins", 0) + 1).commit();
                                MainActivity.fab.setVisibility(View.VISIBLE);
                                getFragmentManager().beginTransaction().replace(R.id.contentContainer, new HomeFrag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                                Toast.makeText(getContext(),"The match has ended:" + (Integer.valueOf(splitted[1]) > 1 ? " you lost." : " you won!"),Toast.LENGTH_LONG).show();
                                h.removeCallbacksAndMessages(null);
                            }
                            else
                            {
                                roll.setText(splitted[1].replace("=","") + "'S TURN");
                                roll.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                ranking.setText("#" + ( splitted[2] != null && !splitted[2].isEmpty() && (Integer.valueOf(splitted[2]) < 5 || Integer.valueOf(splitted[2]) > 0) ? Integer.valueOf(splitted[2]) : "?" ));
                            }

                        }
                        else Toast.makeText(getContext(),"There was an error while communicating with the server.",Toast.LENGTH_LONG).show();

                    }
                };
                boolean a = (roll.getText().toString().contains("ROLLED") || piece1.isEnabled() || piece2.isEnabled() || piece3.isEnabled() || piece4.isEnabled() || roll.getText().toString().contains("TAP"));
                AsyncTask.Status status = movetask.getStatus();
                if(!a && (status != AsyncTask.Status.RUNNING)) movetask.execute();
                h.postDelayed(this, 3000);
            }
        };
        h.postDelayed(myrunnable, 15000); // 1 second delay (takes millis)

        piece1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rollfab.setClickable(false);
                roll.setText("YOU MOVED A TOKEN");
                piece1.setEnabled(false);
                piece2.setEnabled(false);
                piece3.setEnabled(false);
                piece4.setEnabled(false);
                setText(piece1,1,"");
                setText(piece2,2,"");
                setText(piece3,3,"");
                setText(piece4,4,"");

                roll.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));

                selurl = "move?player=" + username + "&piece=0&steps=" + moverolled;
                @SuppressLint("StaticFieldLeak") final AsyncTask<String,Void,String> movetask = new AsyncTask<String,Void,String>() {
                    @Override
                    protected String doInBackground(String... url) {
                        String baseurl ="http://192.168.43.205/";
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
                    protected void onPostExecute(String string)
                    {
                        if (!string.isEmpty() && string.contains(","))
                        {
                            String[] splitted = split(string, ",");
                            assert splitted[0] != null;
                            setText(piece1,1,"POS. " + splitted[0]);
                            assert splitted[1] != null;
                            setText(piece2,2,"POS. " + splitted[1]);
                            assert splitted[2] != null;
                            setText(piece3,3,"POS. " + splitted[2]);
                            assert splitted[3] != null;
                            setText(piece4,4,"POS. " + splitted[3]);
                            assert splitted[4] != null;
                            ranking.setText("#" + splitted[4]);
                            SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
                            s.edit().putInt("moves",s.getInt("moves",0) + 1).commit();
                            totalmoves++;
                            moves.setText(String.valueOf(totalmoves));
                        }
                        else Toast.makeText(getContext(),"There was an error while communicating with the server.",Toast.LENGTH_LONG).show();

                    }
                };
                movetask.execute();
                moves.setText(String.valueOf(totalmoves));
            }
        });
        piece2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roll.setText("YOU MOVED A TOKEN");
                rollfab.setClickable(false);
                piece1.setEnabled(false);
                piece2.setEnabled(false);
                piece3.setEnabled(false);
                piece4.setEnabled(false);
                setText(piece1,1,"");
                setText(piece2,2,"");
                setText(piece3,3,"");
                setText(piece4,4,"");

                roll.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
                selurl = "move?player=" + username + "&piece=1&steps=" + moverolled;
                @SuppressLint("StaticFieldLeak") final AsyncTask<String,Void,String> movetask = new AsyncTask<String,Void,String>() {
                    @Override
                    protected String doInBackground(String... url) {
                        String baseurl ="http://192.168.43.205/";
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
                    protected void onPostExecute(String string)
                    {
                        if (!string.isEmpty() && string.contains(","))
                        {
                            String[] splitted = split(string, ",");
                            assert splitted[0] != null;
                            setText(piece1,1,"POS. " + splitted[0]);
                            assert splitted[1] != null;
                            setText(piece2,2,"POS. " + splitted[1]);
                            assert splitted[2] != null;
                            setText(piece3,3,"POS. " + splitted[2]);
                            assert splitted[3] != null;
                            setText(piece4,4,"POS. " + splitted[3]);
                            assert splitted[4] != null;
                            ranking.setText("#" + splitted[4]);
                            SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
                            s.edit().putInt("moves",s.getInt("moves",0) + 1).commit();
                            totalmoves++;
                            moves.setText(String.valueOf(totalmoves));
                        }
                        else Toast.makeText(getContext(),"There was an error while communicating with the server.",Toast.LENGTH_LONG).show();

                    }
                };
                movetask.execute();
            }
        });
        piece3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollfab.setClickable(false);
                roll.setText("YOU MOVED A TOKEN");
                piece1.setEnabled(false);
                piece2.setEnabled(false);
                piece3.setEnabled(false);
                piece4.setEnabled(false);
                setText(piece1,1,"");
                setText(piece2,2,"");
                setText(piece3,3,"");
                setText(piece4,4,"");

                roll.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
                selurl = "move?player=" + username + "&piece=2&steps=" + moverolled;
                @SuppressLint("StaticFieldLeak") final AsyncTask<String,Void,String> movetask = new AsyncTask<String,Void,String>() {
                    @Override
                    protected String doInBackground(String... url) {
                        String baseurl ="http://192.168.43.205/";
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
                    protected void onPostExecute(String string)
                    {
                        if (!string.isEmpty() && string.contains(","))
                        {
                            String[] splitted = split(string, ",");
                            assert splitted[0] != null;
                            setText(piece1,1,"POS. " + splitted[0]);
                            assert splitted[1] != null;
                            setText(piece2,2,"POS. " + splitted[1]);
                            assert splitted[2] != null;
                            setText(piece3,3,"POS. " + splitted[2]);
                            assert splitted[3] != null;
                            setText(piece4,4,"POS. " + splitted[3]);
                            assert splitted[4] != null;
                            ranking.setText("#" + splitted[4]);
                            SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
                            s.edit().putInt("moves",s.getInt("moves",0) + 1).commit();
                            totalmoves++;
                            moves.setText(String.valueOf(totalmoves));
                        }
                        else Toast.makeText(getContext(),"There was an error while communicating with the server.",Toast.LENGTH_LONG).show();
                    }
                };
                movetask.execute();
            }
        });
        piece4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollfab.setClickable(false);

                roll.setText("YOU MOVED A TOKEN");
                piece1.setEnabled(false);
                piece2.setEnabled(false);
                piece3.setEnabled(false);
                piece4.setEnabled(false);
                setText(piece1,1,"");
                setText(piece2,2,"");
                setText(piece3,3,"");
                setText(piece4,4,"");

                roll.setTextColor(getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
                selurl = "move?player=" + username + "&piece=3&steps=" + moverolled;
                @SuppressLint("StaticFieldLeak") final AsyncTask<String,Void,String> movetask = new AsyncTask<String,Void,String>() {
                    @Override
                    protected String doInBackground(String... url) {
                        String baseurl ="http://192.168.43.205/";
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
                    protected void onPostExecute(String string)
                    {
                        if (!string.isEmpty() && string.contains(","))
                        {
                            String[] splitted = split(string, ",");
                            assert splitted[0] != null;
                            setText(piece1,1,"POS. " + splitted[0]);
                            assert splitted[1] != null;
                            setText(piece2,2,"POS. " + splitted[1]);
                            assert splitted[2] != null;
                            setText(piece3,3,"POS. " + splitted[2]);
                            assert splitted[3] != null;
                            setText(piece4,4,"POS. " + splitted[3]);
                            assert splitted[4] != null;
                            ranking.setText("#" + splitted[4]);
                            SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
                            s.edit().putInt("moves",s.getInt("moves",0) + 1).commit();
                            totalmoves++;
                            moves.setText(String.valueOf(totalmoves));
                        }
                        else Toast.makeText(getContext(),"There was an error while communicating with the server.",Toast.LENGTH_LONG).show();
                    }
                };
                movetask.execute();
            }
        });
    }

    private void setText(Button btn,int piece,String piecestatus)
    {
        if(piece == 1 && piecestatus.contains("POS.")) piece1lastpos = piecestatus;
        if(piece == 2 && piecestatus.contains("POS.")) piece2lastpos = piecestatus;
        if(piece == 3 && piecestatus.contains("POS.")) piece3lastpos = piecestatus;
        if(piece == 4 && piecestatus.contains("POS.")) piece4lastpos = piecestatus;
        Spannable span;
        span = new SpannableString("Token " + piece + (piecestatus.equals("") ? "" : "\n") + (piecestatus.equals("true") ? "MAKE " + moverolled + " STEP(S)" : piecestatus.equals("end") ? "MAKE " + moverolled + "\nLAST STEP(S)" : piecestatus.equals("out") ? "GET IT OUT" : piecestatus.contains("POS. 0") ? "STARTING POINT" : piecestatus.contains("30") ? "ARRIVED" : piecestatus.contains("POS.") && !piecestatus.contains(" 0") ? piecestatus : ""));
        span.setSpan(new AbsoluteSizeSpan(70), 0, span.length() - (span.toString().contains("LAST STEP(S)") ? 20 : span.toString().contains(" STEP(S)") ? 14 : span.toString().contains("GET") ? 10 : span.toString().contains("ARRIVED") ? 7 : span.toString().length() - 7 ), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        btn.setText(span);
        boolean status = !piecestatus.equals("false") && !piecestatus.equals("") && !piecestatus.contains("POS.");
        btn.setTextColor(status ? getResources().getColor(android.R.color.white) : getResources().getColor((player.equals("Red") ? R.color.colorAccentRed : player.equals("Green") ?  R.color.colorAccentGreen  : player.equals("Blue") ?  R.color.colorAccentBlue  :  R.color.colorAccentYellow)));
        btn.setEnabled(status);
    }
}

class CallbackFuture extends CompletableFuture<Response> implements Callback
{
    public void onResponse(Call call, Response response)
    {
        super.complete(response);
    }
    public void onFailure(Call call, IOException e)
    {
        super.completeExceptionally(e);
    }
}