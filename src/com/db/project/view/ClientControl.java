package com.db.project.view;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.util.Duration;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import com.db.project.MainApp;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.db.project.model.*;
import java.io.IOException;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.WordUtils;

public class ClientControl{
	@FXML
	private Label user,id,songinfo,t; 
    @FXML
    private JFXSpinner wait;
    @FXML
    private JFXTextField input,email,newusername;
    @FXML
    private JFXPasswordField oldpass,newpass,confirm;
    @FXML
    private TableView<Music> table;
    @FXML
    private TableColumn<Music,String> Song,Singer,Album;
    @FXML
    private StackPane root,pane,panel;
    @FXML
    private AnchorPane set;
    @FXML
    private JFXComboBox<String> option;
    @FXML
    private JFXSlider time,vol;
    @FXML
    private JFXButton like,playButton;
    @FXML
    private JFXListView<String> his;
    @FXML
    private JFXToggleButton enableHistory;
    protected final String KEY= "d4f6d0b1ec8b699f23caf8e767e19c76489bd024dad0a5e2281a9a5955391898";
    private Connection con;
    // Reference to the main application.
    private MainApp mainApp;
    private final ObservableList<Music> data = FXCollections.observableArrayList();
    private final ObservableList<String> history = FXCollections.observableArrayList();

    private String u="User";
    private int uid=0;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private Music selected;
    private Media media;
    private MediaPlayer mp;
    private Duration duration;
    private boolean atEndOfMedia = false;
    private boolean stopRequested = false;
    private ImageView play = new ImageView(new Image(getClass().getResourceAsStream("img/icons8_Play_48px_1.png")));
    private ImageView pause = new ImageView(new Image(getClass().getResourceAsStream("img/icons8_Pause_48px.png")));
    private ImageView l = new ImageView(new Image(getClass().getResourceAsStream("img/icons8_Love_96px.png")));
    private ImageView ul = new ImageView(new Image(getClass().getResourceAsStream("img/icons8_Heart_Outline_64px_1.png")));
    private PoolDataSource  pds = PoolDataSourceFactory.getPoolDataSource();
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public ClientControl() {
    	
    }

    public void setInfo(String u,int uid) throws SQLException {
    	this.u = u;
    	this.uid = uid;
    	user.setText(u);
    	id.setText("ID:"+uid);
    	System.out.println("loged in user:"+u+",uid:"+uid);
    	con=pds.getConnection();//getting the connection from the pool
		Statement s = con.createStatement();
		String sql = "SELECT SEARCH_KEYWORD FROM SEARCH_HISTORY WHERE ID="+uid;
		ResultSet rs = s.executeQuery(sql);
		System.out.println(sql);
		while(rs.next())
			history.add(rs.getString(1));
		his.refresh();
		con.close();
    }
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     * @throws SQLException 
     */
	@FXML
    private void initialize() throws SQLException {
    	this.setMainApp(mainApp);
    	 table.setItems(data);
    	 his.setItems(history);
     	Singer.setCellValueFactory(cellData -> cellData.getValue().singerProperty());
     	Song.setCellValueFactory(cellData -> cellData.getValue().songProperty());
     	Album.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
     	option.getItems().addAll(
     		    "Song",
     		    "Singer",
     		    "Album"
     		);
     	option.getSelectionModel().selectFirst();
    	//Initialize SQL Connection pool on startup
     	//previous method of using a single connection throughout the app does not work well.
     	//Thus ucp connection pool is used in the latest revison
     	pds.setConnectionFactoryClassName("com.mysql.cj.jdbc.Driver");
     	pds.setURL("jdbc:mysql://projects.anranz.xyz:3306/Project?&useSSL=false");
     	pds.setUser("Project");
     	pds.setPassword("2141");
		mp = new MediaPlayer(new Media("http://projects.anranz.xyz/1.mp3"));//initialize the media player
		mpInitiallize();
		play.setFitWidth(32);play.setFitHeight(32);
		pause.setFitWidth(32);pause.setFitHeight(32);
		pause.setTranslateX(-2);
		l.setFitHeight(24);l.setFitWidth(24);
		ul.setFitHeight(24);ul.setFitWidth(24);
		his.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent click) {
		        if (click.getClickCount() == 2) {
		           //Use ListView's getSelected Item
		           String currentItemSelected = his.getSelectionModel().getSelectedItem();
		           home();
		           input.setText(currentItemSelected);
		           try {
					search();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        }
		    }
		});
		table.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent click) {
		        if (click.getClickCount() == 2) {
		           //Use ListView's getSelected Item
		        	selected = table.getSelectionModel().getSelectedItem();
		           String s = selected.getSinger()+" - "+selected.getSong();
		           songinfo.setText(s);
		           int sel = selected.getID();
		          
		           String url = "http://projects.anranz.xyz/"+sel+".mp3";//for demo purpose only
		           System.out.println("Requesting: "+url);

					mp.stop();
					media = new Media(url);
			           mp= new MediaPlayer(media);
	               	   duration = mp.getCycleDuration();
	               	   time.valueProperty().addListener(new InvalidationListener() {
	                    public void invalidated(Observable ov) {
	                            // multiply duration by percentage calculated by slider position
	                    	if(time.isValueChanging())
	                            mp.seek(duration.multiply(time.getValue() / 100.0));
	                    }
	                });
               	   mp.setVolume(0.5);
               	   duration = mp.getCycleDuration();
               	   mp.setAutoPlay(true);
               	   mpInitiallize();
               	   mp.play();
               	   playButton.setGraphic(pause);
               	try {
					if(!check(selected))
						like.setGraphic(ul);
					else
						like.setGraphic(l);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        }
		    }
		});
		vol.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
        	if(vol.isValueChanging())
                mp.setVolume(vol.getValue() / 100.0);
	        }
	    });
	}
	@FXML
    public void Play() {
            Status status = mp.getStatus();
            
            if (status == Status.UNKNOWN || status == Status.HALTED) {
                // don't do anything in these states
                return;
            }
            if (status == Status.PAUSED
                    || status == Status.READY
                    || status == Status.STOPPED) {
                
                if (atEndOfMedia) {
                    mp.seek(mp.getStartTime());
                    atEndOfMedia = false;
                }
                mp.play();
            } else {
                mp.pause();
            }
            mpInitiallize();
        }
	@FXML
	private void updateValues() {
        if (time != null && time != null) {
            Platform.runLater(new Runnable() {
				public void run() {
                Duration currentTime = mp.getCurrentTime();
                duration = mp.getCycleDuration();
                
                time.setDisable(duration.isUnknown());
                if (!time.isDisabled()
                     && duration.greaterThan(Duration.ZERO)
                     && !time.isValueChanging()) {
                     time.setValue(currentTime.divide(duration).toMillis()* 100.0);
                     t.setText(formatTime(currentTime, duration));
                    }
                }
            });
        }
    }
	private void mpInitiallize() {//refresh the mediaPlayer and the controls
		
		boolean repeat = false;
		mp.currentTimeProperty().addListener(new InvalidationListener() 
        {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });
 
        mp.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mp.pause();
                    stopRequested = false;
                } else {
                    playButton.setText("||");
                    playButton.setGraphic(pause);
                }
            }
        });
 
        mp.setOnPaused(new Runnable() {
            public void run() {
                playButton.setText(">");
                playButton.setGraphic(play);
            }
        });
 
        mp.setOnReady(new Runnable() {
            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues();
            }
        });
 
        mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    playButton.setText(">");
                    playButton.setGraphic(play);
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
       });
	}
	@FXML
    public void permit() {
		home();
		pane.setVisible(false);
    	Alert("Sorry, you do not have the permission to view that.",panel);
    }
    @FXML
    private void showHistory() throws SQLException {
    	pane.setVisible(true);
    	set.setVisible(false);
    	table.setVisible(false);
    	history.clear();
    	con=pds.getConnection();//getting the connection from the pool
		Statement s = con.createStatement();
		String sql = "SELECT SEARCH_KEYWORD FROM SEARCH_HISTORY WHERE ID="+uid;
		ResultSet rs = s.executeQuery(sql);
		System.out.println(sql);
		while(rs.next())
			history.add(rs.getString(1));
		his.refresh();
    	pane.toFront();
    	con.close();//close connection after every query
    }
    @FXML
    private void handleKey(KeyEvent a) throws SQLException{
    	if(a.getCode().equals(KeyCode.ENTER))
    		search();
    }
    @FXML
    private void search() throws SQLException {
    if(input.getText().isEmpty()) {
    	Alert("Please enter a key word to search with !",panel);
    }
    else {
    	home();
    	String in = input.getText().toUpperCase();
    	con=pds.getConnection();//getting the connection from the pool
    	Statement stmt = con.createStatement();
    	String selected = option.getSelectionModel().getSelectedItem();
    	String sql;
    	if(selected.equals("Song")) {
    		System.out.println("Search mode: "+option.getSelectionModel().getSelectedItem());
    		 sql = "SELECT Song_id,SONG_NAME, SINGER_NAME, ALBUM_NAME FROM SONGS JOIN SONGS_ALBUMS_SINGERS USING(SONG_ID) JOIN SINGERS "
    				+ "USING(SINGER_ID) JOIN ALBUMS USING(ALBUM_ID) WHERE SONG_NAME LIKE'%"+in+"%';";
    	}
    	else if(selected.equals("Singer")) {
    		System.out.println("Search mode: "+option.getSelectionModel().getSelectedItem());
    		 sql = "SELECT Song_id,SONG_NAME, SINGER_NAME, ALBUM_NAME FROM SONGS JOIN SONGS_ALBUMS_SINGERS USING(SONG_ID) JOIN SINGERS "
    				+ "USING(SINGER_ID) JOIN ALBUMS USING(ALBUM_ID) WHERE SINGER_NAME LIKE'%"+in+"%';";
    	}
    	else {
    		System.out.println("Search mode: "+option.getSelectionModel().getSelectedItem());
    		 sql = "SELECT Song_id,SONG_NAME, SINGER_NAME, ALBUM_NAME FROM SONGS JOIN SONGS_ALBUMS_SINGERS USING(SONG_ID) JOIN SINGERS "
    				+ "USING(SINGER_ID) JOIN ALBUMS USING(ALBUM_ID) WHERE ALBUM_NAME LIKE'%"+in+"%';";
    	}
    	System.out.println(sql);
    	ResultSet rs=stmt.executeQuery(sql);
    	data.clear();
    	while(rs.next()) {
    		int s_id = rs.getInt(1);
    		String sn = WordUtils.capitalizeFully(rs.getString(2));
    		String ss = WordUtils.capitalizeFully(rs.getString(3));
    		String sa = WordUtils.capitalizeFully(rs.getString(4));
    		table.refresh();
    		data.add(new Music(s_id,sn,ss,sa));
    		table.refresh();
    		sql = "CALL SEARCH_SONGHISTORY(?,?);";
    		PreparedStatement ps = con.prepareStatement(sql);
    		ps.setInt(1, uid);
    		ps.setString(2, in);
    		if(enableHistory.isSelected()) {
    			ps.executeQuery();
    			System.out.println(sql);
    		}
    	}
    	con.close();
    }
    
  }
    @FXML
    public void clearHistory() throws SQLException {
    	history.clear();
    	con=pds.getConnection();//getting the connection from the pool
    	Statement s = con.createStatement();
    	String sql = "DELETE FROM SEARCH_HISTORY WHERE ID = "+uid;
    	JFXButton Agree = new JFXButton("OK");
		JFXDialogLayout content = new JFXDialogLayout();
		content.setActions(Agree);
		content.setHeading(new Text("Confirmation"));
		content.setBody(new Text("Are you sure you want to do that ?"));
		JFXDialog dialog = new JFXDialog(pane, new Label("error"), JFXDialog.DialogTransition.CENTER);
		dialog.setContent(content);		
		Agree.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	dialog.close();
	        	try {
					s.executeUpdate(sql);
					System.out.println(sql);
					Alert("Success !","All records removed.",pane);
					his.refresh();
				} catch (SQLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
	        	try {
					con.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	    });
		dialog.show();
    }
    @FXML
    public void deleteHistory() throws SQLException {
    	int index = his.getSelectionModel().getSelectedIndex();
    	history.remove(index);
    	con=pds.getConnection();//getting the connection from the pool
    	Statement s = con.createStatement();
    	String sql = "DELETE FROM SEARCH_HISTORY WHERE ID = "+uid+" AND SEARCH_ID ="+(index+1);
    	s.executeUpdate(sql);
    	System.out.println(sql);
    	con.close();
    }
    @FXML
    public void Like() throws SQLException {
    	int songid = selected.getID();
    	con=pds.getConnection();//getting the connection from the pool
    	if(check(selected)) {//delete the item from list
    		Statement s = con.createStatement();
    		String sql = "DELETE FROM LISTS WHERE ID = "+uid+" AND SONG_ID ="+songid+";";
    		s.executeUpdate(sql);
    		System.out.println(sql);
    		like.setGraphic(ul);
    	}
    	else {//add it to the lsit
    		Statement s = con.createStatement();
    		String sql = "INSERT INTO LISTS VALUES(NULL,NULL,"+uid+","+songid+");";
    		s.executeUpdate(sql);
    		System.out.println(sql);
    		like.setGraphic(l);
    	}
    	con.close();
    }
    public boolean check(Music m) throws SQLException {//checks the corrent song to see if it is liked
    	con=pds.getConnection();//getting the connection from the pool
    	Statement s = con.createStatement();
    	String sql ="SELECT Song_id FROM LISTS WHERE id ="+uid+";";
    	ResultSet rs = s.executeQuery(sql);
    	System.out.println(sql);
    	while(rs.next()) {
    		if(rs.getInt(1)==m.getID())//if the song is in the list
    			return true;
    	}
    	con.close();
    	return false;
    }
    @FXML
    public void ShowLike() throws SQLException {
    	home();
    	data.clear();
    	table.refresh();
    	con=pds.getConnection();//getting the connection from the pool
    	Statement s = con.createStatement();
    	String sql = "SELECT SONG_ID FROM LISTS WHERE ID = "+uid+";";
    	ResultSet rs = s.executeQuery(sql);
    	System.out.println(sql);
    	while(rs.next()) {
    		int id = rs.getInt(1);
    		s =con.createStatement();
    		sql = "SELECT Song_id,SONG_NAME, SINGER_NAME, ALBUM_NAME FROM SONGS JOIN SONGS_ALBUMS_SINGERS USING(SONG_ID) JOIN SINGERS "
    				+ "USING(SINGER_ID) JOIN ALBUMS USING(ALBUM_ID) WHERE SONG_ID ="+id+";";
    		ResultSet r = s.executeQuery(sql);
    		System.out.println(sql);
    		while(r.next()) {
        		int s_id = r.getInt(1);
        		String sn = WordUtils.capitalizeFully(r.getString(2));
        		String ss = WordUtils.capitalizeFully(r.getString(3));
        		String sa = WordUtils.capitalizeFully(r.getString(4));
        		table.refresh();
        		data.add(new Music(s_id,sn,ss,sa));
        		table.refresh();
    		}
    	}
    	con.close();
    }
    @FXML
    public void exit() {
    	home();
    	JFXButton Agree = new JFXButton("OK");
		JFXDialogLayout content = new JFXDialogLayout();
		content.setActions(Agree);
		content.setHeading(new Text("Confirmation"));
		content.setBody(new Text("Are you sure you want to exit ?"));
		JFXDialog dialog = new JFXDialog(panel, new Label("error"), JFXDialog.DialogTransition.CENTER);
		dialog.setContent(content);		
		Agree.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	dialog.close();
	        	System.exit(0);
	        	try {
					con.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	    });
		dialog.show();
    }
    @FXML
    public void home() {
    	table.setVisible(true);
    	set.setVisible(false);
    	pane.setVisible(false);
    	pane.toBack();
    	data.clear();
    	table.refresh();
    }
    @FXML
    public void setting() {
    	table.setVisible(false);
    	set.setVisible(true);System.out.println("loged in user:"+u+",uid:"+uid);
    	pane.setVisible(false);
    	pane.toBack();
    }
    @FXML
    public void login() throws IOException {
    	FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainApp.class.getResource("view/login.fxml"));
		StackPane rootLayout = loader.load();
		Scene scene = new Scene(rootLayout);
		MainApp.getPrimaryStage().setScene(scene);
    }
    @FXML
    public void updatePass() throws SQLException, IOException {
    	con=pds.getConnection();//getting the connection from the pool
    	String sql = "SELECT AES_DECRYPT(userpassword,?) FROM USERS WHERE ID = ?";
    	PreparedStatement ps = con.prepareStatement(sql);
    	ps.setString(1, KEY);
    	ps.setInt(2, uid);
    	ResultSet rs = ps.executeQuery();
    	String cpass="";
    	if(rs.next())
    		cpass = rs.getString(1);
    	String old = oldpass.getText();
    	String n = newpass.getText();
    	String c = confirm.getText();
    	if(!cpass.equals(old))
    		Alert("The current password you entered is not correct !",panel);
    	else if(n.length()<6)
    		Alert("New password need to have a minimum of 6 characters !",panel);
    	else if(!n.equals(c))
    		Alert("The new passwords are not idantical !",panel);
    	else if(cpass.equals(n))
    		Alert("The new password can't be the same as the current one !",panel);
    	else {
    		sql = "UPDATE USERS SET userpassword = AES_ENCRYPT(?,?) WHERE id = ?;";
    		ps = con.prepareStatement(sql);
    		ps.setString(1, n);
    		ps.setString(2, KEY);
    		ps.setInt(3,uid);
    		ps.executeUpdate();
    		Alert ("Succuss !","You will be redirected to the login page.",panel);
    		login();
    	}
    	con.close();
    }
    @FXML
    public void updateUsername() throws SQLException {
    	String name = newusername.getText();
    	con=pds.getConnection();//getting the connection from the pool
		Statement stmt = con.createStatement();
		String sql ="Select id FROM USERS WHERE username ='"+name+"';";
		ResultSet rs = stmt.executeQuery(sql);
		System.out.println(sql);
		if(rs.next())
			Alert("Sorry, this username has been taken\n\nYou may try something else",panel);
		else if(name.length()<3)
			Alert("Sorry, this username is too short !",panel);
		else {
			stmt = con.createStatement();
			sql ="UPDATE USERS SET username ='"+name+"'where id = "+uid+";";
    		stmt.executeUpdate(sql);
    		Alert ("Succuss !","",panel);
    		u = name;
    		user.setText(u);
		}
		con.close();
    }
    @FXML
    public void showVolume() {
    	if(vol.isVisible())
    		vol.setVisible(false);
    	else
    		vol.setVisible(true);
    }
    @FXML
    public void updateEmail() throws SQLException {
    	String e = email.getText();
    	Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(e);
    	if(!matcher.find()) {
    		Alert("Please enter a valid email address !",panel);
    	 }
    	else {
    		con=pds.getConnection();//getting the connection from the pool
    		Statement stmt = con.createStatement();
    		String sql ="Select username FROM USERS WHERE email ='"+e+"';";
    		ResultSet rs = stmt.executeQuery(sql);
    		System.out.println(sql);
    		if(rs.next())
    			Alert("The email you entered has been linked to an account !",panel);
    		else {
    			stmt = con.createStatement();
    			sql ="UPDATE USERS SET email ='"+e+"'where id = "+uid+";";
        		stmt.executeUpdate(sql);
        		Alert ("Succuss !","",panel);
    		}
    		con.close();
    	}
    }
    public void Alert(String error,StackPane s) {
    	
    	JFXButton Agree = new JFXButton("OK");
		JFXDialogLayout content = new JFXDialogLayout();
		content.setActions(Agree);
		content.setHeading(new Text("Error!"));
		content.setBody(new Text(error));
		JFXDialog dialog = new JFXDialog(s, new Label("error"), JFXDialog.DialogTransition.CENTER);
		dialog.setContent(content);		
		Agree.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	dialog.close();
	        }
	    });
		dialog.show();
    }
    public void Alert(String title,String message,StackPane s) {
    	JFXButton Agree = new JFXButton("OK");
		JFXDialogLayout content = new JFXDialogLayout();
		content.setActions(Agree);
		content.setHeading(new Text(title));
		content.setBody(new Text(message));
		JFXDialog dialog = new JFXDialog(s, new Label("error"), JFXDialog.DialogTransition.CENTER);
		dialog.setContent(content);		
		Agree.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	dialog.close();
	        }
	    });
		dialog.show();
    }
    private String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
             int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}