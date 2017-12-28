package ca.edumedia.riba0007.doorsopenottawa.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *  Store building data reeived by the server
 *  @author Priscila Ribas da Costa (riba0007)
 */
public class BuildingPOJO
        implements Parcelable {

    private String _id;
    private Integer buildingId;
    private String nameEN;
    private String descriptionEN;
    private String descriptionFR;
    private String categoryFR;
    private Integer categoryId;
    private Float longitude;
    private Float latitude;
    private String postalCode;
    private String province;
    private String city;
    private String imageDescriptionFR;
    private String imageDescriptionEN;
    private String image;
    private String sundayClose;
    private String sundayStart;
    private String saturdayClose;
    private String saturdayStart;
    private String categoryEN;
    private String addressFR;
    private String addressEN;
    private Boolean isNewBuilding;
    private String nameFR;

    public String get_Id() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public String getNameEN() {
        return nameEN;
    }

    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
    }

    public String getDescriptionEN() {
        return descriptionEN;
    }

    public void setDescriptionEN(String descriptionEN) {
        this.descriptionEN = descriptionEN;
    }

    public String getDescriptionFR() {
        return descriptionFR;
    }

    public void setDescriptionFR(String descriptionFR) {
        this.descriptionFR = descriptionFR;
    }

    public String getCategoryFR() {
        return categoryFR;
    }

    public void setCategoryFR(String categoryFR) {
        this.categoryFR = categoryFR;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImageDescriptionFR() {
        return imageDescriptionFR;
    }

    public void setImageDescriptionFR(String imageDescriptionFR) {
        this.imageDescriptionFR = imageDescriptionFR;
    }

    public String getImageDescriptionEN() {
        return imageDescriptionEN;
    }

    public void setImageDescriptionEN(String imageDescriptionEN) {
        this.imageDescriptionEN = imageDescriptionEN;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSundayClose() {
        return sundayClose;
    }

    public void setSundayClose(String sundayClose) {
        this.sundayClose = sundayClose;
    }

    public String getSundayStart() {
        return sundayStart;
    }

    public void setSundayStart(String sundayStart) {
        this.sundayStart = sundayStart;
    }

    public String getSaturdayClose() {
        return saturdayClose;
    }

    public void setSaturdayClose(String saturdayClose) {
        this.saturdayClose = saturdayClose;
    }

    public String getSaturdayStart() {
        return saturdayStart;
    }

    public void setSaturdayStart(String saturdayStart) {
        this.saturdayStart = saturdayStart;
    }

    public String getCategoryEN() {
        return categoryEN;
    }

    public void setCategoryEN(String categoryEN) {
        this.categoryEN = categoryEN;
    }

    public String getAddressFR() {
        return addressFR;
    }

    public void setAddressFR(String addressFR) {
        this.addressFR = addressFR;
    }

    public String getAddressEN() {
        return addressEN;
    }

    public void setAddressEN(String addressEN) {
        this.addressEN = addressEN;
    }

    public Boolean getIsNewBuilding() {
        return isNewBuilding;
    }

    public void setIsNewBuilding(Boolean isNewBuilding) {
        this.isNewBuilding = isNewBuilding;
    }

    public String getNameFR() {
        return nameFR;
    }

    public void setNameFR(String nameFR) {
        this.nameFR = nameFR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeValue(this.buildingId);
        dest.writeString(this.nameEN);
        dest.writeString(this.descriptionEN);
        dest.writeString(this.descriptionFR);
        dest.writeString(this.categoryFR);
        dest.writeValue(this.categoryId);
        dest.writeValue(this.longitude);
        dest.writeValue(this.latitude);
        dest.writeString(this.postalCode);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.imageDescriptionFR);
        dest.writeString(this.imageDescriptionEN);
        dest.writeString(this.image);
        dest.writeString(this.sundayClose);
        dest.writeString(this.sundayStart);
        dest.writeString(this.saturdayClose);
        dest.writeString(this.saturdayStart);
        dest.writeString(this.categoryEN);
        dest.writeString(this.addressFR);
        dest.writeString(this.addressEN);
        dest.writeValue(this.isNewBuilding);
        dest.writeString(this.nameFR);
    }

    public BuildingPOJO() {
    }

    protected BuildingPOJO(Parcel in) {
        this._id = in.readString();
        this.buildingId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.nameEN = in.readString();
        this.descriptionEN = in.readString();
        this.descriptionFR = in.readString();
        this.categoryFR = in.readString();
        this.categoryId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.longitude = (Float) in.readValue(Float.class.getClassLoader());
        this.latitude = (Float) in.readValue(Float.class.getClassLoader());
        this.postalCode = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.imageDescriptionFR = in.readString();
        this.imageDescriptionEN = in.readString();
        this.image = in.readString();
        this.sundayClose = in.readString();
        this.sundayStart = in.readString();
        this.saturdayClose = in.readString();
        this.saturdayStart = in.readString();
        this.categoryEN = in.readString();
        this.addressFR = in.readString();
        this.addressEN = in.readString();
        this.isNewBuilding = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.nameFR = in.readString();
    }

    public static final Parcelable.Creator<BuildingPOJO> CREATOR = new Parcelable.Creator<BuildingPOJO>() {
        @Override
        public BuildingPOJO createFromParcel(Parcel source) {
            return new BuildingPOJO(source);
        }

        @Override
        public BuildingPOJO[] newArray(int size) {
            return new BuildingPOJO[size];
        }
    };
}
