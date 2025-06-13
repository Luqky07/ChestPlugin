package pch.luqky.models;

import java.util.List;

//Class to manage secure profiles info
public class SecureProfile {
    private int profileId;
    private String name;
    private String ownerName;
    private Boolean isDefault;
    private List<String> members;

    public SecureProfile(int profileId, String name, String ownerName, Boolean isDefault, List<String> members) {
        this.profileId = profileId;
        this.name = name;
        this.ownerName = ownerName;
        this.isDefault = isDefault;
        this.members = members;
    }

    public int getProfileId() {
        return profileId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getOwnerName() {
        return ownerName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public List<String> getMembers() {
        return members;
    }
}
