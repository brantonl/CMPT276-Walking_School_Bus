package lava.walkinggroup.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import lava.walkinggroup.R;
import lava.walkinggroup.dataobjects.Message;

/*
 * This class is to setup the layout of the complex listview for inboxActivity
 */

public class ListAdapter extends ArrayAdapter<Message> {
    public ListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
    public ListAdapter(Context context, int resource, List<Message> messages){
        super(context,resource,messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.activity_inbox_row, null);
        }

        Message p = getItem(position);

        if (p != null) {
            ImageView readorno = (ImageView) v.findViewById(R.id.read);
            ImageView emergency = (ImageView) v.findViewById(R.id.alert);
            TextView from = (TextView) v.findViewById(R.id.fromWhom);
            TextView when = (TextView) v.findViewById(R.id.timeRec);
            TextView id = (TextView) v.findViewById(R.id.msgId);

            if (id != null){
                id.setText(p.getId().toString());
            }
            if (from != null) {
               from.setText(getContext().getResources().getString(R.string.From2)+ UserCache.getInstance().getUser(p.getFromUser().getId()).getName());
            }

            if (when != null) {
                when.setText(p.getTimestamp().toString());
            }
            if (emergency != null){
                if (p.isEmergency()) {
                    emergency.setVisibility(View.VISIBLE);
                } else{
                    emergency.setVisibility(View.GONE);
                }
            }
            if (readorno != null){
                if (p.isRead()) {
                    readorno.setImageResource(R.drawable.ic_mail_open);
                    from.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                } else{
                    readorno.setImageResource(R.drawable.ic_email_closed);
                    from.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                }
            }

        }

        return v;
    }


}
