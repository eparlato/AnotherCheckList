package eparlato.anotherchecklist;

import android.os.Parcel;
import android.os.Parcelable;

public class CheckList implements Parcelable {

    private int id;
    private String name;
    private long creationDate;

    public CheckList(int id, String name, long creationDate) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
    }

    public CheckList(Parcel in) {
        readFromParcel(in);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(creationDate);
    }

    private void readFromParcel(Parcel in) {

        id = in.readInt();
        name = in.readString();
        creationDate = in.readLong();
    }

    public static final Creator<CheckList> CREATOR = new Creator<CheckList>() {

        @Override
        public CheckList createFromParcel(Parcel source) {
            return new CheckList(source);
        }

        @Override
        public CheckList[] newArray(int size) {
            return new CheckList[size];
        }
    };

}
