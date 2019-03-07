package lava.walkinggroup.dataobjects.rewards;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import lava.walkinggroup.R;
import lava.walkinggroup.utility.CurrentSession;

public class ColorTheme {
    private static Resources resources = CurrentSession.getAppContext().getResources();
    public static ColorTheme BLUE = new ColorTheme("Blue Theme", 1, resources.getDrawable(R.drawable.blue_theme, null));
    public static ColorTheme GRAY = new ColorTheme("Gray Theme", 2, resources.getDrawable(R.drawable.gray_theme, null));
    public static ColorTheme PURPLE = new ColorTheme("Purple Theme", 3, resources.getDrawable(R.drawable.purple_theme, null));
    public static ColorTheme RED = new ColorTheme("Red Theme", 4, resources.getDrawable(R.drawable.red_theme, null));
    public static ColorTheme GREEN = new ColorTheme("Green Theme", 5, resources.getDrawable(R.drawable.green_theme, null));
    public static ColorTheme GOLD = new ColorTheme("Gold Theme", 6, resources.getDrawable(R.drawable.gold_theme, null));

    private int colorTheme;
    private Drawable preview;
    private String name;

    public ColorTheme() {

    }

    private ColorTheme(String name, int colorTheme, Drawable preview) {
        setColorTheme(colorTheme);
        setName(name);
        setPreview(preview);
    }

    private void setColorTheme(int colorTheme) {
        this.colorTheme = colorTheme;
    }

    private void setPreview(Drawable preview) {
        this.preview = preview;
    }

    private void setName(String name) {
        this.name = name;
    }

    public int[] getColorTheme() {
        int[] theme = new int[4];
        if (colorTheme == 1){
            theme[0] = R.color.set1P;
            theme[1] = R.color.set1PD;
            theme[2] = R.color.set1A;
            theme[3] = R.color.set1F;
        }else if (colorTheme == 2){
            theme[0] = R.color.set2P;
            theme[1] = R.color.set2PD;
            theme[2] = R.color.set2A;
            theme[3] = R.color.set2F;
        }else if (colorTheme == 3){
            theme[0] = R.color.set3P;
            theme[1] = R.color.set3PD;
            theme[2] = R.color.set3A;
            theme[3] = R.color.set3F;
        }else if (colorTheme == 4){
            theme[0] = R.color.set4P;
            theme[1] = R.color.set4PD;
            theme[2] = R.color.set4A;
            theme[3] = R.color.set4F;
        }else if (colorTheme == 5){
            theme[0] = R.color.set5P;
            theme[1] = R.color.set5PD;
            theme[2] = R.color.set5A;
            theme[3] = R.color.set5F;
        }else if (colorTheme == 6){
            theme[0] = R.color.set6P;
            theme[1] = R.color.set6PD;
            theme[2] = R.color.set6A;
            theme[3] = R.color.set6F;
        }
        return theme;
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
