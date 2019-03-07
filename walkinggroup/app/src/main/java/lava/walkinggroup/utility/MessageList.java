package lava.walkinggroup.utility;

import java.util.ArrayList;
import java.util.List;

import lava.walkinggroup.MapsActivity;
import lava.walkinggroup.dataobjects.Message;
import lava.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

/*
 * This class is to cache all the messages from the server and use it locally
 */

public class MessageList {
    private List<Message> allMessages;
    private List<Message> unreadMessages;
    private List<Message> readMessages;
    private static MessageList instance;

    private MessageList(){
        allMessages = new ArrayList<>();
        unreadMessages = new ArrayList<>();
        readMessages = new ArrayList<>();
        update();
    }

    public static MessageList getInstance(){
        if(instance == null){
            instance = new MessageList();
        }
        return instance;
    }



    public void update(){
        if(CurrentSession.getCurrentUser() == null || CurrentSession.proxy == null){
            return;
        }

        Long currentUserId = CurrentSession.getCurrentUser().getId();

        Call<List<Message>> allMessageCaller = CurrentSession.proxy.getMessages(currentUserId);
        ProxyBuilder.callProxy(allMessageCaller, returnedMessageList -> setAllMessages(returnedMessageList));

        Call<List<Message>> unreadMessageCaller = CurrentSession.proxy.getUnreadMessages(currentUserId, null);
        ProxyBuilder.callProxy(unreadMessageCaller, returnedMessageList -> setUnreadMessages(returnedMessageList));

        Call<List<Message>> readMessageCaller = CurrentSession.proxy.getReadMessages(currentUserId, null);
        ProxyBuilder.callProxy(readMessageCaller, returnedMessageList -> setReadMessages(returnedMessageList));

        //Toast.makeText(CurrentSession.getAppContext(), "MESSAGELIST TEST", Toast.LENGTH_SHORT).show();
    }

    public List<Message> getAllMessages() {
        return allMessages;
    }

    public List<Message> getUnreadMessages() {
        return unreadMessages;
    }

    public List<Message> getReadMessages() {
        return readMessages;
    }

    public int getUnreadMessageCount() {
        return unreadMessages.size();
    }

    private void setAllMessages(List<Message> allMessageList) {
        allMessages = allMessageList;
    }

    private void setUnreadMessages(List<Message> unreadMessages) {
        this.unreadMessages = unreadMessages;
        MapsActivity mapsActivity = CurrentSession.getMapsActivity();
        if(mapsActivity != null){
            mapsActivity.refreshUnreadMessagesCount();
        }
    }

    private void setReadMessages(List<Message> readMessages) {
        this.readMessages = readMessages;
    }
}
