package me.davidml16.baul.objects.rewards;

public class CosmeticUnlockObject {

    private String id;
    private String cosmeticId;

    public CosmeticUnlockObject(String id, String cosmeticId) {
        this.id = id;
        this.cosmeticId = cosmeticId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCosmeticId() {
        return cosmeticId;
    }

    public void setCosmeticId(String cosmeticId) {
        this.cosmeticId = cosmeticId;
    }

    @Override
    public String toString() {
        return "CosmeticUnlockObject{" +
                "id='" + id + '\'' +
                ", cosmeticId='" + cosmeticId + '\'' +
                '}';
    }
}
