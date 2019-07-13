package samt.al.goludo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class HomeFrag extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        long time = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong("time", 0);
        return (time != 0 ? inflater.inflate(R.layout.fragment_home, container, false) : inflater.inflate(R.layout.fragment_nohome, container, false) );
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState)
    {

        ImageButton fab = view.findViewById(R.id.smallfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.fab.setImageDrawable(getResources().getDrawable(R.drawable.back));
                getActivity().getFragmentManager().beginTransaction().replace(R.id.contentContainer, new SettingsFrag(), "settings").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack("").commit();
            }
        });


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        long time = sharedPreferences.getLong("time", 0);
        if(time != 0)
        {
            int wins = sharedPreferences.getInt("wins", 0);
            int losses = sharedPreferences.getInt("losses", 0);
            int rank = sharedPreferences.getInt("ranking", 0);

            String username = sharedPreferences.getString("pref_account_username", "");

            TextView statstitletext  = view.findViewById(R.id.statstitle);
            TextView averanktext = view.findViewById(R.id.averanktext);
            TextView avetimetext = view.findViewById(R.id.avetimetext);
            TextView winstext = view.findViewById(R.id.winstext);
            TextView lossestext = view.findViewById(R.id.lossestext);

            statstitletext.setText(username.replace(username.substring(0,1),username.substring(0,1).toUpperCase()) + "'s Stats");
            averanktext.setText("#" + rank / (wins + losses));
            avetimetext.setText((((time / (wins + losses)) / 1000) / 60) + "'");
            winstext.setText(String.valueOf(wins));
            lossestext.setText(String.valueOf(losses));
        }
    }
}
