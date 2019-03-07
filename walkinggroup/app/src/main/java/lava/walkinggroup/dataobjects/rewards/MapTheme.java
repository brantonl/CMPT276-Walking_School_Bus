package lava.walkinggroup.dataobjects.rewards;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import lava.walkinggroup.R;
import lava.walkinggroup.utility.CurrentSession;

public class MapTheme{
    private static Resources resources = CurrentSession.getAppContext().getResources();
    public static MapTheme STANDARD = new MapTheme("Default Map", R.raw.style_json3, resources.getDrawable(R.drawable.defaultmap, null));
    public static MapTheme DARK = new MapTheme("Dark Theme", R.raw.style_json2, resources.getDrawable(R.drawable.darkmap, null));
    public static MapTheme RETRO = new MapTheme("Retro Map", R.raw.style_json, resources.getDrawable(R.drawable.retromap, null));

    private int mapId;
    private Drawable preview;
    private String name;

    public MapTheme() {

    }

    private MapTheme(String name, int mapId, Drawable preview) {
        setMap(mapId);
        setName(name);
        setPreview(preview);
    }

    private void setMap(int mapId) {
        this.mapId = mapId;
    }

    private void setPreview(Drawable preview) {
        this.preview = preview;
    }

    private void setName(String name) {
        this.name = name;
    }

    public int getMapId() {
        return mapId;
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