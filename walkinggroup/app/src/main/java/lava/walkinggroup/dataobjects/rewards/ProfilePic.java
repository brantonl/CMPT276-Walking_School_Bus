package lava.walkinggroup.dataobjects.rewards;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import lava.walkinggroup.R;
import lava.walkinggroup.utility.CurrentSession;

public class ProfilePic {
    private static Resources resources = CurrentSession.getAppContext().getResources();
    public static ProfilePic ELDER = new ProfilePic("Elder", resources.getDrawable(R.drawable.elder,null));
    public static ProfilePic SHAMAN = new ProfilePic("Shaman", resources.getDrawable(R.drawable.shaman,null));
    public static ProfilePic PHARAOH = new ProfilePic("Pharaoh", resources.getDrawable(R.drawable.pharaoh,null));
    public static ProfilePic SENSEI = new ProfilePic("Sensei", resources.getDrawable(R.drawable.sensei,null));
    public static ProfilePic SAGE = new ProfilePic("Sage", resources.getDrawable(R.drawable.sage,null));
    public static ProfilePic EXARCH = new ProfilePic("Exarch", resources.getDrawable(R.drawable.exarch,null));
    public static ProfilePic CONSUL = new ProfilePic("Consul", resources.getDrawable(R.drawable.consul,null));
    public static ProfilePic GUARDIAN = new ProfilePic("Guardian", resources.getDrawable(R.drawable.guardian,null));
    public static ProfilePic MASTER = new ProfilePic("Master", resources.getDrawable(R.drawable.grandmaster,null));
    public static ProfilePic WARLOCK = new ProfilePic("Warlock", resources.getDrawable(R.drawable.warlock,null));

    private Drawable profilePic;
    private Drawable preview;
    private String name;

    public ProfilePic() {

    }

    private ProfilePic(String name, Drawable profilePic) {
        setProfilePic(profilePic);
        setName(name);
        setPreview(profilePic);
    }

    private void setProfilePic(Drawable profilePic) {
        this.profilePic = profilePic;
    }

    private void setPreview(Drawable preview) {
        this.preview = preview;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Drawable getProfilePic() {
        return profilePic;
    }

    public Drawable getPreview() {
        return preview;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
