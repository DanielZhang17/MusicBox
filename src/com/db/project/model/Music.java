package com.db.project.model;

import javafx.beans.property.SimpleStringProperty;

public class Music {
	 private final SimpleStringProperty Song;
	 private final SimpleStringProperty Singer;
	 private final SimpleStringProperty Album;
	 private final int id;
	    public Music(int id,String Song, String Singer, String Album) {
	    	this.id = id;
	        this.Song = new SimpleStringProperty(Song);
	        this.Singer = new SimpleStringProperty(Singer);
	        this.Album = new SimpleStringProperty(Album);
	    }
	    public int getID() {
	    	return id;
	    }
	    public String getSong() {
	        return Song.get();
	    }
	    public void setSong(String song) {
	        Song.set(song);
	    }
	        
	    public String getSinger() {
	        return Singer.get();
	    }
	    public void setSinger(String singer) {
	    	Singer.set(singer);
	    }
	    
	    public String getAlbum() {
	        return Album.get();
	    }
	    public void setAlbum(String album) {
	    	Album.set(album);
	    }
	    public SimpleStringProperty singerProperty() {
	        return Singer;
	    }
	    public SimpleStringProperty songProperty() {
	        return Song;
	    }
	    public SimpleStringProperty albumProperty() {
	        return Album;
	    }
}
