package lava.walkinggroup.dataobjects;

import android.content.res.Resources;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lava.walkinggroup.R;
import lava.walkinggroup.dataobjects.rewards.ColorTheme;
import lava.walkinggroup.dataobjects.rewards.MapTheme;
import lava.walkinggroup.dataobjects.rewards.ProfilePic;
import lava.walkinggroup.utility.CurrentSession;

/**
 * Custom class that your group can change the format of in (almost) any way you like
 * to encode the rewards that this user has earned.
 *
 * This class gets serialized/deserialized as part of a User object. Server stores it as
 * a JSON string, so it has no direct knowledge of what it contains.
 * (Rewards may not be used during first project iteration or two)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarnedRewards {
    private String title;
    private Integer score;
    private Integer currentLevel;
    private List<Level> levels;
    private MapTheme selectedMapTheme;
    private Set<MapTheme> unlockedMapThemes;
    private ColorTheme selectedColorTheme;
    private Set<ColorTheme> unlockedColorThemes;
    private ProfilePic selectedProfilePic;
    private Set<ProfilePic> unlockedProfilePics;

    // Needed for JSON deserialization
    public EarnedRewards() {
        initLevels();
        unlockedMapThemes = new HashSet<>();
        unlockedColorThemes = new HashSet<>();
        unlockedProfilePics = new HashSet<>();
    }

    public static EarnedRewards init() {
        EarnedRewards rewards = new EarnedRewards();
        rewards.setCurrentLevel(0);
        rewards.setScore(0);
        rewards.setSelectedColorTheme(rewards.levels.get(0).colorTheme);
        rewards.setSelectedMapTheme(rewards.levels.get(0).mapTheme);
        rewards.setSelectedColorTheme(rewards.levels.get(0).colorTheme);
        rewards.setSelectedProfilePic(rewards.levels.get(0).profilePic);
        return rewards;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        TextView titleView = CurrentSession.getMapsActivity().findViewById(R.id.nav_header_subtitle);
        if (titleView != null) {
            titleView.setText(title);
        }
    }
    @JsonIgnore
    public Integer getScore() {
        return score;
    }
    @JsonIgnore
    public void setScore(Integer score) {
        if (score == null) {
            score = 0;
        }
        this.score = score;
        int level;
        if (currentLevel == null) {
            level = 0;
        } else {
            level = currentLevel-1;
        }
        while ( (level+1 < levels.size()) && (score >= levels.get(level+1).minScore) ){
            level++;
        }
        setCurrentLevel(level);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
        for(int i = 0; i <= currentLevel; i++) {
            unlockLevel(i);
        }
        if (levels == null) {
            new RuntimeException("Levels model in Earned Rewards not initialized").printStackTrace();
            return;
        }
        this.title = levels.get(currentLevel).title;
    }
    @JsonIgnore
    public MapTheme getSelectedMapTheme() {
        return selectedMapTheme;
    }
    @JsonIgnore
    public void setSelectedMapTheme(MapTheme selectedMapTheme) {
        this.selectedMapTheme = selectedMapTheme;
    }

    public void setSelectedMapThemeName(String mapName) {
        for (MapTheme map : unlockedMapThemes) {
            if(map.getName().equals(mapName)) {
                setSelectedMapTheme(map);
            }
        }
    }

    public String getSelectedMapThemeName() {
        return selectedMapTheme.getName();
    }

    @JsonIgnore
    public Set<MapTheme> getUnlockedMapThemes() {
        return unlockedMapThemes;
    }

    @JsonIgnore
    public void setUnlockedMapThemes(Set<MapTheme> unlockedMapThemes) {
        this.unlockedMapThemes = unlockedMapThemes;
    }

    @JsonIgnore
    public ColorTheme getSelectedColorTheme() {
        return selectedColorTheme;
    }

    @JsonIgnore
    public void setSelectedColorTheme(ColorTheme selectedColorTheme) {
        this.selectedColorTheme = selectedColorTheme;
    }

    public void setSelectedColorThemeName(String colorName) {
        for (ColorTheme color : unlockedColorThemes) {
            if(color.getName().equals(colorName)) {
                setSelectedColorTheme(color);
            }
        }
    }

    public String getSelectedColorThemeName() {
        return selectedColorTheme.getName();
    }

    @JsonIgnore
    public Set<ColorTheme> getUnlockedColorThemes() {
        return unlockedColorThemes;
    }

    @JsonIgnore
    public void setUnlockedColorThemes(Set<ColorTheme> unlockedColorThemes) {
        this.unlockedColorThemes = unlockedColorThemes;
    }

    @JsonIgnore
    public ProfilePic getSelectedProfilePic() {
        return selectedProfilePic;
    }

    @JsonIgnore
    public void setSelectedProfilePic(ProfilePic selectedProfilePic) {
        this.selectedProfilePic = selectedProfilePic;
    }

    public void setSelectedProfilePicName(String profilePicName) {
        for (ProfilePic profilePic : unlockedProfilePics) {
            if(profilePic.getName().equals(profilePicName)) {
                setSelectedProfilePic(profilePic);
            }
        }
    }

    public String getSelectedProfilePicName() {
        return selectedProfilePic.getName();
    }

    @JsonIgnore
    public Set<ProfilePic> getUnlockedProfilePics() {
        return unlockedProfilePics;
    }

    @JsonIgnore
    public void setUnlockedProfilePics(Set<ProfilePic> unlockedProfilePics) {
        this.unlockedProfilePics = unlockedProfilePics;
    }

    private void unlockLevel(int i) {
        Level level = levels.get(i);
        MapTheme mapTheme = level.mapTheme;
        if (mapTheme != null) {
            unlockedMapThemes.add(mapTheme);
        }
        ColorTheme colorTheme = level.colorTheme;
        if (colorTheme != null) {
            unlockedColorThemes.add(colorTheme);
        }
        ProfilePic profilePic = level.profilePic;
        if (profilePic != null) {
            unlockedProfilePics.add(profilePic);
        }
    }

    private void initLevels () {
        Resources resources = CurrentSession.getAppContext().getResources();
        levels = new ArrayList<>();
        levels.add(new Level(0, resources.getString(R.string.level_0_title), MapTheme.STANDARD, ColorTheme.BLUE, ProfilePic.ELDER));
        levels.add(new Level(50, resources.getString(R.string.level_1_title), null,null, ProfilePic.SHAMAN));
        levels.add(new Level(150, resources.getString(R.string.level_2_title), null, ColorTheme.GRAY, ProfilePic.PHARAOH));
        levels.add(new Level(350, resources.getString(R.string.level_3_title), MapTheme.DARK, null, ProfilePic.SENSEI));
        levels.add(new Level(750, resources.getString(R.string.level_4_title), null, ColorTheme.RED, ProfilePic.SAGE));
        levels.add(new Level(1550, resources.getString(R.string.level_5_title), null, null, ProfilePic.EXARCH));
        levels.add(new Level(2400, resources.getString(R.string.level_6_title), null, ColorTheme.GREEN, ProfilePic.CONSUL));
        levels.add(new Level(3200, resources.getString(R.string.level_7_title), MapTheme.RETRO, null, ProfilePic.GUARDIAN));
        levels.add(new Level(4000, resources.getString(R.string.level_8_title), null, ColorTheme.PURPLE, ProfilePic.MASTER));
        levels.add(new Level(4800, resources.getString(R.string.level_9_title), null, ColorTheme.GOLD, ProfilePic.WARLOCK));
    }

    private class Level {
        final int minScore;
        final String title;
        final MapTheme mapTheme;
        final ColorTheme colorTheme;
        final ProfilePic profilePic;
        Level (int minScore, String title, MapTheme mapTheme, ColorTheme colorTheme, ProfilePic profilePic) {
            this.minScore = minScore;
            this.title = title;
            this.mapTheme = mapTheme;
            this.colorTheme = colorTheme;
            this.profilePic = profilePic;
        }
    }
}