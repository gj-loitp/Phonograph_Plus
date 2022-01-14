package player.phonograph.model;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import player.phonograph.R;
import player.phonograph.helper.MusicPlayerRemote;
import player.phonograph.helper.menu.SongMenuHelper;
import player.phonograph.helper.menu.SongsMenuHelper;
import player.phonograph.interfaces.Displayable;
import player.phonograph.util.MusicUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable, Displayable {
    public static final Song EMPTY_SONG = new Song(-1, "", -1, -1, -1, "", -1, -1, "", -1, "");

    public final long id;
    public final String title;
    public final int trackNumber;
    public final int year;
    public final long duration;
    public final String data;
    public final long dateModified;
    public final long albumId;
    public final String albumName;
    public final long artistId;
    public final String artistName;

    public Song(long id, String title, int trackNumber, int year, long duration, String data, long dateModified, long albumId, String albumName, long artistId, String artistName) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;
        this.data = data;
        this.dateModified = dateModified;
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (trackNumber != song.trackNumber) return false;
        if (year != song.year) return false;
        if (duration != song.duration) return false;
        if (dateModified != song.dateModified) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (data != null ? !data.equals(song.data) : song.data != null) return false;
        if (albumName != null ? !albumName.equals(song.albumName) : song.albumName != null)
            return false;
        return artistName != null ? artistName.equals(song.artistName) : song.artistName == null;

    }

    @Override
    public int hashCode() {
        int result = (int) id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        result = 31 * result + year;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + (int) albumId;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (int) artistId;
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", year=" + year +
                ", duration=" + duration +
                ", data='" + data + '\'' +
                ", dateModified=" + dateModified +
                ", albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);
        dest.writeString(this.data);
        dest.writeLong(this.dateModified);
        dest.writeLong(this.albumId);
        dest.writeString(this.albumName);
        dest.writeLong(this.artistId);
        dest.writeString(this.artistName);
    }

    protected Song(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        this.year = in.readInt();
        this.duration = in.readLong();
        this.data = in.readString();
        this.dateModified = in.readLong();
        this.albumId = in.readLong();
        this.albumName = in.readString();
        this.artistId = in.readLong();
        this.artistName = in.readString();
    }

    @Keep
    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public long getItemID() {
        return id;
    }

    @NonNull
    @Override
    public CharSequence getDisplayTitle() {
        return title;
    }

    @Nullable
    @Override
    public CharSequence getDescription() {
        return MusicUtil.getSongInfoString(this);
    }

    @Nullable
    @Override
    public Uri getPic() {
        return null; //todo
    }

    @Nullable
    @Override
    public String getSortOrderReference() {
        return title; //todo
    }

    @Override
    public int menuRes() {
        return R.menu.menu_item_song_short;
    }

    @Nullable
    @Override
    public Function3<AppCompatActivity, Displayable, Integer, Boolean> menuHandler() {
        return (appCompatActivity, displayable, integer) -> SongMenuHelper.handleMenuClick(appCompatActivity, (Song) displayable, integer);
    }

    @Nullable
    @Override
    public Function3<AppCompatActivity, List<? extends Displayable>, Integer, Boolean> multiMenuHandler() {
        return (appCompatActivity, list, integer) -> SongsMenuHelper.handleMenuClick(appCompatActivity, (List<Song>) list, integer);
    }

    @NonNull
    @Override
    public Function3<FragmentActivity, Displayable, List<? extends Displayable>, Unit> clickHandler() {
        return (activity, displayable, queue) -> {
            MusicPlayerRemote.openQueue((List<Song>) queue, queue.indexOf(displayable), true);
            return null;
        };
    }
}
