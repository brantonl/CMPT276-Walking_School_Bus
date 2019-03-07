package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import lava.walkinggroup.dataobjects.Group;
import lava.walkinggroup.dataobjects.Message;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import retrofit2.Call;

/**
 * This class is for the user to input the content of the messages that they are going to send
 */
public class ComposeActivity extends AppCompatActivity {
    private static final String GROUP_ID_TAG = "GroupIdExtra";
    private static final String MESSAGE_TYPE_TAG = "MessageTypeExtra";
    private static final String TAG = "Compose Activity";

    public enum MessageType{
        PANIC_MESSAGE, PARENT_LEADER_BROADCAST, GROUP_BROADCAST
    }

    private MessageType messageType;
    private Long recipientGroupId;
    private boolean isEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getDataFromIntent();
        getLayoutElement();
        initMessageField();
        initEmergencyCheckBox();
        initSendButton();
        initCancelButton();
    }

    private void getLayoutElement() {
        Button sendBtn = findViewById(R.id.ComposeActivity_Button_Send);
        Button cancelBtn = findViewById(R.id.ComposeActivity_Button_Cancel);
        EditText message = findViewById(R.id.ComposeActivity_EditText_MessageField);
        CheckBox isEmergency = findViewById(R.id.ComposeActivity_CheckBox_IsEmergency);
        TextView descrip = findViewById(R.id.textView5);

        View root = sendBtn.getRootView();
        CurrentSession session = CurrentSession.getInstance();
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors = new int[] { getResources().getColor(theme[0])};

        int[][] states2 = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors2 = new int[] { getResources().getColor(theme[1])};

        ColorStateList rippleList = new ColorStateList(states, colors);
        ColorStateList buttonList = new ColorStateList(states2, colors2);

        RippleDrawable back = (RippleDrawable) sendBtn.getBackground();
        back.setColor(rippleList);
        sendBtn.setTextColor(getResources().getColor(theme[3]));
        sendBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) cancelBtn.getBackground();
        back.setColor(rippleList);
        cancelBtn.setTextColor(getResources().getColor(theme[3]));
        cancelBtn.setBackgroundTintList(buttonList);

        message.setTextColor(getResources().getColor(theme[3]));
        descrip.setTextColor(getResources().getColor(theme[3]));
        isEmergency.setTextColor(getResources().getColor(theme[3]));

        int states3[][] = {{android.R.attr.state_checked}, {}};
        int colors3[] = {getResources().getColor(theme[2]), getResources().getColor(theme[3])};
        CompoundButtonCompat.setButtonTintList(isEmergency, new ColorStateList(states3, colors3));

        root.setBackgroundColor(getResources().getColor(theme[0]));

    }

    private void initEmergencyCheckBox() {
        CheckBox emergCheckBox = findViewById(R.id.ComposeActivity_CheckBox_IsEmergency);
        emergCheckBox.setChecked(isEmergency);
    }

    private void initMessageField() {
        if(messageType == MessageType.PANIC_MESSAGE){
            EditText messageField = findViewById(R.id.ComposeActivity_EditText_MessageField);
            messageField.setText(R.string.panic_default_message);
        }
    }

    private void initSendButton() {
        Button sendButton = findViewById(R.id.ComposeActivity_Button_Send);
        sendButton.setOnClickListener(nothing -> sendMessage());
    }

    private void sendMessage() {
        EditText messageField = findViewById(R.id.ComposeActivity_EditText_MessageField);
        CheckBox emergencyCheckBox = findViewById(R.id.ComposeActivity_CheckBox_IsEmergency);
        isEmergency = emergencyCheckBox.isChecked();
        String messageText = messageField.getText().toString();
        Message message = new Message();
        message.setText(messageText);
        message.setEmergency(isEmergency);
        switch(messageType) {
            case GROUP_BROADCAST:
                Call<List<Message>> groupCaller = CurrentSession.proxy.newMessageToGroup(recipientGroupId, message);
                ProxyBuilder.callProxy(groupCaller, sentMessageList -> onSendSuccess(sentMessageList));
                break;
            case PARENT_LEADER_BROADCAST:
                //fall through to panic message case as server call is the same.
            case PANIC_MESSAGE:
                Call<List<Message>> parentLeaderCaller = CurrentSession.proxy.newMessageToParentsOf(CurrentSession.getCurrentUser().getId(), message);
                ProxyBuilder.callProxy(parentLeaderCaller, sentMessageList -> onSendSuccess(sentMessageList));
                break;
        }
    }

    private void initCancelButton() {
        Button cancelButton = findViewById(R.id.ComposeActivity_Button_Cancel);
        cancelButton.setOnClickListener(nothing -> finish());
    }

    private void onSendSuccess(List<Message> sentMessageList) {
        notifyUserViaLogAndToast(getString(R.string.send_confirmation));
        finish();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        this.messageType = (MessageType) intent.getSerializableExtra(MESSAGE_TYPE_TAG);
        if(messageType == MessageType.GROUP_BROADCAST){
            recipientGroupId = intent.getLongExtra(GROUP_ID_TAG, -1);
        }
        if(this.messageType == MessageType.PANIC_MESSAGE){
            this.isEmergency = true;
        } else {
            this.isEmergency = false;
        }
    }

    /**
     *
     * @param context context of the calling activity
     * @param group recipient of message
     * @return intent to launch compose activity
     */
    public static Intent getIntent(Context context, Group group){
        Intent intent = new Intent(context, ComposeActivity.class);
        intent.putExtra(GROUP_ID_TAG, group.getId().longValue());
        intent.putExtra(MESSAGE_TYPE_TAG, MessageType.GROUP_BROADCAST);
        return intent;
    }

    public static Intent getIntent(Context context, MessageType messageType){
        Intent intent = new Intent(context, ComposeActivity.class);
        intent.putExtra(MESSAGE_TYPE_TAG, messageType);
        if(messageType == MessageType.GROUP_BROADCAST){
            throw new UnsupportedOperationException("Must pass a group object in order to send a group broadcast message");
        }
        return intent;
    }

    /**
     * Put message up in toast and logcat
     * @param message message to be displayed
     */
    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
