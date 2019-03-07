package lava.walkinggroup.utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import lava.walkinggroup.R;
import lava.walkinggroup.dataobjects.PermissionRequest;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.WGServerProxy;

public class PermissionListAdapter extends ArrayAdapter<PermissionRequest> {
    public PermissionListAdapter(Context context, Integer layout, List<PermissionRequest> permissions) {
        super(context, layout, permissions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if(itemView == null)
        {
            LayoutInflater view;
            view = LayoutInflater.from(getContext());
            itemView = view.inflate(R.layout.previouspermissions_layout, null);
        }

        //assign contents accordingly
        TextView status = itemView.findViewById(R.id.text_status);
        EditText content = itemView.findViewById(R.id.text_content);
        TextView responsible = itemView.findViewById(R.id.text_responsible);
        ImageView statusImage = itemView.findViewById(R.id.image_status);

        PermissionRequest request = getItem(position);

        content.setText(itemView.getResources().getString(R.string.Text_content,request.getMessage()));
        content.setClickable(false);
        content.setEnabled(false);
        content.setFocusable(false);


        if (request.getStatus() == WGServerProxy.PermissionStatus.PENDING) {
            status.setText(itemView.getResources().getString(R.string.Text_status_pending));
            statusImage.setImageResource(R.drawable.ic_help_black_24dp);
        }
        else if (request.getStatus() == WGServerProxy.PermissionStatus.APPROVED) {
            status.setText(itemView.getResources().getString(R.string.Text_status_approved));
            statusImage.setImageResource(R.drawable.ic_check_circle_black_24dp);
        }
        else {
            status.setText(itemView.getResources().getString(R.string.Text_status_denied));
            statusImage.setImageResource(R.drawable.ic_clear_black_24dp);
        }

        if (request.getStatus() == WGServerProxy.PermissionStatus.APPROVED) {
            for(PermissionRequest.Authorizor a : request.getAuthorizors()) {
                if(a.getStatus() == WGServerProxy.PermissionStatus.APPROVED) {
                    responsible.setText(itemView.getResources().getString(R.string.Text_approveResponsible,UserCache.getInstance().getUser(a.getWhoApprovedOrDenied().getId()).getName()));
                    break;
                }
            }
        }
        else if (request.getStatus() == WGServerProxy.PermissionStatus.PENDING) {
            boolean foundUser = false;
            for(PermissionRequest.Authorizor a : request.getAuthorizors()) {
                for (User user : a.getUsers()) {
                    if(a.getStatus() == WGServerProxy.PermissionStatus.PENDING) {
                        responsible.setText(itemView.getResources().getString(R.string.Text_pendingResponsible,UserCache.getInstance().getUser(user.getId()).getName()));
                        foundUser = true;
                        break;
                    }
                    if (foundUser) {
                        break;
                    }
                }
                if (foundUser) {
                    break;
                }
            }
        }
        else {
            for (PermissionRequest.Authorizor a : request.getAuthorizors()) {
                if (a.getStatus() == WGServerProxy.PermissionStatus.DENIED) {
                    responsible.setText(itemView.getResources().getString(R.string.Text_denyResponsible, UserCache.getInstance().getUser(a.getWhoApprovedOrDenied().getId()).getName()));
                    break;
                }
            }
        }

        return itemView;
    }
}
