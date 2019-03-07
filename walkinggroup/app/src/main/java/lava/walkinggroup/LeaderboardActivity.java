package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import retrofit2.Call;

import static lava.walkinggroup.utility.CurrentSession.proxy;

/**
 * This Activity is for showing the leader board of points for all the users of the app
 */

public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderBoard";
    private String[] boardContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getUserList();
        getLayoutElements();
    }

    private void getLayoutElements() {
        CurrentSession session = CurrentSession.getInstance();
        //grab currently selected themes from Current User
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        ListView list = findViewById(R.id.leaderboard_list);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setBackgroundColor(getResources().getColor(theme[1]));
        toolbar.setTitleTextColor(getResources().getColor(theme[3]));
        toolbar.setSubtitleTextColor(getResources().getColor(theme[3]));

        list.setBackgroundColor(getResources().getColor(theme[3]));

        View root = list.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));
    }

    private void getUserList() {
        Call<List<User>> listCaller = proxy.getUsers();
        ProxyBuilder.callProxy(this,listCaller,returnedUsers -> populateLeaderBoard(returnedUsers));
    }

    private void populateLeaderBoard(List<User> returnedUsers) {
        boardContent = new String[returnedUsers.size()];
        Collections.sort(returnedUsers, (o1, o2) -> o2.getTotalPointsEarned() - o1.getTotalPointsEarned());
        String name, tmp;
        int spaceIndex;

        for (int i =0; i < returnedUsers.size(); i++) {
            tmp = returnedUsers.get(i).getName();
            spaceIndex = tmp.indexOf(32);
            if (spaceIndex != -1 && spaceIndex + 2 < tmp.length()) {
                name = tmp.substring(0,spaceIndex+2);
            }
            else{
                name = tmp;
            }
            boardContent[i] = getString(R.string.leaderBoard_rowstyle,(i+1)+"",name,returnedUsers.get(i).getTotalPointsEarned().toString());
        }

        ListView leaderBoard = findViewById(R.id.leaderboard_list);
        ArrayAdapter<String> content = new ArrayAdapter<>(this, R.layout.listview_layout, boardContent);
        leaderBoard.setAdapter(content);
    }


    public static Intent getIntent(Context context) {
        return new Intent(context, LeaderboardActivity.class);
    }
}