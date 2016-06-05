package it.andreacioni.kp2a.plugin.keelink;

/**
 * Created by andreacioni on 04/06/16.
 */

public class RecentListEntry {

    private String title=null,user=null;

    public RecentListEntry(String title, String user) {
        this.title = title;
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecentListEntry that = (RecentListEntry) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return user != null ? user.equals(that.user) : that.user == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RecentListEntry{" +
                "title='" + title + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
