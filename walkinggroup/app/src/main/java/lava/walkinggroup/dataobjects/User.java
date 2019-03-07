package lava.walkinggroup.dataobjects;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User class to store the data the server expects and returns.
 */

// All model classes coming from server must have this next line.
// It ensures deserialization does not fail if server sends you some fields you are not expecting.
// This is needed for the server to be able to change without breaking your app!
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends IdItemBase{
    // NOTE: id, hasFullData, and href in IdItemBase base class.

    // Data fields for the user.
    // -------------------------------------------------------------------------------------------
    // NOTE: Make numbers Long/Integer, not long/int because only the former will
    //       deserialize if the value is null from the server.
    private String name;
    private String email;
    private String password;

    private Integer currentPoints;
    private Integer totalPointsEarned;
    private EarnedRewards rewards;

    private Integer birthYear;
    private Integer birthMonth;
    private String homePhone;
    private String cellPhone;
    private String address;
    private String grade;
    private String teacherName;
    private String emergencyContactInfo;

    private List<User> monitoredByUsers = new ArrayList<>();
    private List<User> monitorsUsers = new ArrayList<>();

    private List<Group> memberOfGroups = new ArrayList<>();
    private List<Group> leadsGroups = new ArrayList<>();

    // Basic User Data
    // -------------------------------------------------------------------------------------------
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Note: Password never returned by the server; only used to send password to server.
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    // GPS Location
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    private GpsLocation lastGpsLocation;

    // Monitoring
    // -------------------------------------------------------------------------------------------
    public List<User> getMonitoredByUsers() {
        return monitoredByUsers;
    }
    public void setMonitoredByUsers(List<User> monitoredByUsers) {
        this.monitoredByUsers = monitoredByUsers;
    }

    public List<User> getMonitorsUsers() {
        return monitorsUsers;
    }
    public void setMonitorsUsers(List<User> monitorsUsers) {
        this.monitorsUsers = monitorsUsers;
    }

    // Groups
    // -------------------------------------------------------------------------------------------
    public List<Group> getMemberOfGroups() {
        return memberOfGroups;
    }
    public void setMemberOfGroups(List<Group> memberOfGroups) {
        this.memberOfGroups = memberOfGroups;
    }

    public List<Group> getLeadsGroups() {
        return leadsGroups;
    }
    public void setLeadsGroups(List<Group> leadsGroups) {
        this.leadsGroups = leadsGroups;
    }


    // Rewards (custom JSON data)
    // -------------------------------------------------------------------------------------------
    public Integer getCurrentPoints() {
        return currentPoints;
    }
    public void setCurrentPoints(Integer currentPoints) {
        if(currentPoints == null) {
            currentPoints = 0;
        }
        this.currentPoints = currentPoints;
    }

    public Integer getTotalPointsEarned() {
        if (totalPointsEarned == null) {
            setTotalPointsEarned(0);
        }
        return totalPointsEarned;
    }
    public void setTotalPointsEarned(Integer totalPointsEarned) {
        if(totalPointsEarned == null) {
            totalPointsEarned = 0;
        }
        this.totalPointsEarned = totalPointsEarned;
        if(rewards == null) {
            rewards = EarnedRewards.init();
        }
        rewards.setScore(totalPointsEarned);
    }


    // Setter will be called when deserializing User's JSON object; we'll automatically
    // expand it into the custom object.
    public void setCustomJson(String jsonString) {
        if (jsonString == null || jsonString.length() == 0) {
            rewards = EarnedRewards.init();
            Log.w("USER", "De-serializing string is null for User's custom Json rewards; ignoring.");
        } else {
            Log.w("USER", "De-serializing string: " + jsonString);
            try {
                rewards = new ObjectMapper().readValue(
                        jsonString,
                        EarnedRewards.class);
                Log.w("USER", "De-serialized embedded rewards object: " + rewards);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (rewards == null) {
            rewards = EarnedRewards.init();
        }
        rewards.setScore(getTotalPointsEarned());
    }
    // Having a getter will make this function be called to set the value of the
    // customJson field of the JSON data being sent to server.
    public String getCustomJson() {
        // Convert custom object to a JSON string:
        String customAsJson = null;
        try {
            customAsJson = new ObjectMapper().writeValueAsString(rewards);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return customAsJson;
    }

    @JsonIgnore
    public EarnedRewards getRewards() {
        if(rewards == null) {
            rewards = EarnedRewards.init();
        }
        return rewards;
    }

    @JsonIgnore
    public void setRewards(EarnedRewards rewards) {
        this.rewards = rewards;
    }


    public GpsLocation getLastGpsLocation() {
        return lastGpsLocation;
    }
    public void setLastGpsLocation(GpsLocation lastGpsLocation) {
        this.lastGpsLocation = lastGpsLocation;
    }

    // Utility Functions
    // -------------------------------------------------------------------------------------------


    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", currentPoints=" + currentPoints +
                ", totalPointsEarned=" + totalPointsEarned +
                ", rewards=" + rewards +
                ", birthYear=" + birthYear +
                ", birthMonth=" + birthMonth +
                ", homePhone='" + homePhone + '\'' +
                ", cellPhone='" + cellPhone + '\'' +
                ", address='" + address + '\'' +
                ", grade='" + grade + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", emergencyContactInfo='" + emergencyContactInfo + '\'' +
                ", monitoredByUsers=" + monitoredByUsers +
                ", monitorsUsers=" + monitorsUsers +
                ", memberOfGroups=" + memberOfGroups +
                ", leadsGroups=" + leadsGroups +
                '}';
    }

    //User 1 equals user2 iff user1.id equals user2.id
    @Override
    public boolean equals(Object obj) {
        return this.getId().equals( ((User) obj).getId() );
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public Integer getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getEmergencyContactInfo() {
        return emergencyContactInfo;
    }

    public void setEmergencyContactInfo(String emergencyContactInfo) {
        this.emergencyContactInfo = emergencyContactInfo;
    }

}
