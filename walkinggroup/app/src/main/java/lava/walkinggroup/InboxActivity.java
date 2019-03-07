package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import lava.walkinggroup.dataobjects.Message;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.ListAdapter;
import lava.walkinggroup.utility.UserCache;
import retrofit2.Call;

/**
 * This class is to setup the message box that allows the users to read the messages that they have
 */

public class InboxActivity extends AppCompatActivity {
    private static final String TAG = "InboxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        CurrentSession currentSession = CurrentSession.getInstance(this);
        User user = currentSession.getCurrentUser();
        ListView list = (ListView) findViewById(R.id.inboxMessages);
        ListView elist = (ListView) findViewById(R.id.emergencyMsg);
        Long id = user.getId();
        Call<List<Message>> caller = CurrentSession.proxy.getMessages(id,true);
        ProxyBuilder.callProxy(caller, eMessage -> populateList(eMessage,elist));
        Call<List<Message>> caller1 = CurrentSession.proxy.getMessages(id, false);
        ProxyBuilder.callProxy(caller1, message -> populateList(message,list));

        getLayoutElement();

        registerClickCallback();
        setOnClickNewMessage();
    }

    private void getLayoutElement() {

        CurrentSession session = CurrentSession.getInstance();
        //grab currently selected themes from Current User
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        //create statelists for the Button (ripple and normal state
        int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors = new int[] { getResources().getColor(theme[0])};
        int[][] states2 = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors2 = new int[] { getResources().getColor(theme[1])};

        ColorStateList rippleList = new ColorStateList(states, colors);
        ColorStateList buttonList = new ColorStateList(states2, colors2);
        //statelists^


        TextView header = findViewById(R.id.inboxHeader);
        ListView emergencylist = findViewById(R.id.emergencyMsg);
        ListView generalList = findViewById(R.id.inboxMessages);
        Button msgBtn = findViewById(R.id.newMsg);

        emergencylist.setBackgroundColor(theme[3]);
        generalList.setBackgroundColor(theme[3]);
        View root = header.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));
        header.setTextColor(getResources().getColor(theme[3]));

        RippleDrawable back = (RippleDrawable) msgBtn.getBackground();
        back.setColor(rippleList);
        msgBtn.setTextColor(getResources().getColor(theme[3]));
        msgBtn.setBackgroundTintList(buttonList);

    }

    private void populateList(List<Message> messages, ListView list){

        ListAdapter customAdapter = new ListAdapter(this, R.layout.activity_inbox,messages);
        list.setAdapter(customAdapter);
        if (list.getAdapter().isEmpty()){
            list.setVisibility(View.GONE);
        }
        if (messages!=null&&messages.size()>0){
            if(messages.get(0).isEmergency()){
                elist = messages;
            }else{
                msgList = messages;
            }
        }
    }

    CurrentSession currentSession = CurrentSession.getInstance(this);
    List<Message> elist = null;
    List<Message> msgList = null;
    private void registerClickCallback() {


        ListView list = (ListView) findViewById(R.id.inboxMessages);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView msgId = view.findViewById(R.id.msgId);
                Log.e("HEY LISTEN", "onItemClick: "+msgId.getText());
                for (Message msg: msgList){
                    if (msg.getId().equals(Long.valueOf((String) msgId.getText()))){

                        //update server to know a message has been clicked
                        Call<Message>caller = currentSession.proxy.markMessageAsRead(msg.getId(),true);
                        ProxyBuilder.callProxy(caller, eMessage -> {
                            ImageView ic_msg = view.findViewById(R.id.read);
                            ic_msg.setImageResource(R.drawable.ic_mail_open);
                            TextView from = view.findViewById(R.id.fromWhom);
                            from.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                        });

                        AlertDialog.Builder MessageBox = new AlertDialog.Builder(InboxActivity.this);
                        MessageBox.setPositiveButton(getString(R.string.confirm), (dialog, iid) -> {
                            //Nothing Happens
                        });
                        MessageBox.setMessage(msg.getText());
                        MessageBox.setTitle(UserCache.getInstance().getUser(msg.getFromUser().getId()).getName() );
                        AlertDialog dialog = MessageBox.create();
                        dialog.show();
                    }
                }
            }
        });
        ListView emergencylist = (ListView) findViewById(R.id.emergencyMsg);
        emergencylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView msgId = view.findViewById(R.id.msgId);
                Log.e("HEY LISTEN", "onItemClick: "+msgId.getText());
                for (Message msg: elist){
                    if (msg.getId().equals(Long.valueOf((String) msgId.getText())) ){

                        //update server to know a message has been clicked
                        Call<Message>caller = currentSession.proxy.markMessageAsRead(msg.getId(),true);
                        ProxyBuilder.callProxy(caller, eMessage -> {
                            ImageView ic_msg = view.findViewById(R.id.read);
                            ic_msg.setImageResource(R.drawable.ic_mail_open);
                            TextView from = view.findViewById(R.id.fromWhom);
                            from.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                        });

                        AlertDialog.Builder MessageBox = new AlertDialog.Builder(InboxActivity.this);
                        MessageBox.setPositiveButton(getString(R.string.confirm), (dialog, iid) -> {
                            //Nothing Happens
                        });
                        MessageBox.setMessage(msg.getText());
                        MessageBox.setTitle(UserCache.getInstance().getUser(msg.getFromUser().getId()).getName() );
                        AlertDialog dialog = MessageBox.create();
                        dialog.show();
                    }
                }
            }
        });
    }

    private void setOnClickNewMessage(){
        Button newMsgBtn = InboxActivity.this.findViewById(R.id.newMsg);
        newMsgBtn.setOnClickListener(view -> {
            Intent intent = ComposeActivity.getIntent(InboxActivity.this, ComposeActivity.MessageType.PARENT_LEADER_BROADCAST);
            startActivity(intent);
        });
    }

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, InboxActivity.class);
        return intent;
    }


}
