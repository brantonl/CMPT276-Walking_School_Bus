package lava.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.ArrayList;

/**
 * Store information about the walking groups.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase {
    private String groupDescription;
    private List<Float> routeLatArray;
    private List<Float> routeLngArray;

    private List<User> memberUsers;

    private User leader;

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public List<Float> getRouteLatArray() {
        return routeLatArray;
    }

    public void setRouteLatArray(List<Float> routeLatArray) {
        this.routeLatArray = routeLatArray;
    }

    public List<Float> getRouteLngArray() {
        return routeLngArray;
    }

    public void setRouteLngArray(List<Float> routeLngArray) {
        this.routeLngArray = routeLngArray;
    }

    public void addMember(User user){
        if(memberUsers == null){
            memberUsers = new ArrayList<>();
        }
        memberUsers.add(user);
    }

    public List<User> getMemberUsers() {
        return memberUsers;
    }

    public void setMemberUsers(List<User> memberUsers) {
        this.memberUsers = memberUsers;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getId().equals(((Group)obj).getId());
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupDescription=" + getGroupDescription() +
                ", routeLatArray=" + getRouteLatArray() +
                ", routeLngArray=" + getRouteLngArray() +
                ", memberUsers='" + getMemberUsers() +
                ", leader='" + getLeader() +
                '}';
    }
}