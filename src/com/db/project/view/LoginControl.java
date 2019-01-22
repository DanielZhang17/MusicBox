package com.db.project.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.db.project.MainApp;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class LoginControl{
    @FXML
    private JFXSpinner wait;
    @FXML
    private Label error;
    @FXML
    private JFXTextField user;
    @FXML
    private JFXPasswordField pass;
    @FXML
    private StackPane root;
    protected final String KEY= "d4f6d0b1ec8b699f23caf8e767e19c76489bd024dad0a5e2281a9a5955391898";
    private Connection con;
    // Reference to the main application.
    private MainApp mainApp;
    public  String u="User";
    public  int uid=0;
    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public LoginControl() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     * @throws SQLException 
     */
    @FXML
    private void initialize() throws SQLException {
    	this.setMainApp(mainApp);

    }
    @FXML
    private void handleKey(KeyEvent a){
    	if(a.getCode().equals(KeyCode.ENTER))
    		Login();
    }
    @FXML
    private void Login() {
    	try{
        	//SQL Connections starts here
    		con=DriverManager.getConnection("jdbc:mysql://projects.anranz.xyz:3306/Project?&useSSL=false","Project","2141");
    		PreparedStatement stmt;
    		String ps = "SELECT * FROM USERS WHERE username = ? AND AES_DECRYPT(userpassword,?) = ?";
    		u =  user.getText();
    		stmt = con.prepareStatement(ps);
    		stmt.setString(1, u);
    		stmt.setString(2, KEY);
    		stmt.setString(3, pass.getText());
    		wait.setVisible(true);
    		ResultSet rs=stmt.executeQuery();
    		if(!rs.next()) {
    			error.setVisible(true);
    			user.clear();
    			pass.clear();
    			wait.setVisible(false);
    			
    		}
    		else {
    			uid =Integer.parseInt(rs.getString(1));
    			u = user.getText();
    			user.clear();
    			pass.clear();
    			error.setVisible(false);
    			JFXDialogLayout content = new JFXDialogLayout();
    			//content.setHeading(new Text("Login Successful!"));
    			//content.setBody(new Text("Welcome,"+u+"! The main app will start in a few seconds.\n\nPlease stand by ..."));
    			Text a = new Text("Loading...");
    			a.setFont(new Font(18));
    			a.setTranslateX(60);
    			content.setBody(wait,a);
    			wait.setScaleX(1.35);
    			wait.setScaleY(1.35);
    			wait.setTranslateX(-160);
    			JFXDialog dialog = new JFXDialog(root, new Label("Hello"), JFXDialog.DialogTransition.CENTER);
    			dialog.setMaxHeight(100);
    			dialog.setContent(content);
    			dialog.show();
    			
    		    Timeline idlestage = new Timeline(new KeyFrame(Duration.seconds(2),new EventHandler<ActionEvent>()
    		    {
    		        @Override
    		        public void handle( ActionEvent event )
    		        {
    		        	dialog.close();
    		        	wait.setVisible(false);
    		        	try {
    		        		String sql = "UPDATE USERS SET USER_LOGIN = NOW() WHERE ID = "+uid;
    		        		Statement s = con.createStatement();
    		        		s.executeUpdate(sql);
							client();
						} catch (IOException | SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    		        }
    		    } ) );
    		    idlestage.setCycleCount(1);
    		    idlestage.play();
    		}
    		//con.close();  
    		}catch(Exception e){ System.out.println(e);
    	}  
    }
    @FXML
    public void forgot() {
    	System.out.println("Redirect to browser");
    	String url = "https://projects.anranz.xyz/reset";
    	 if(Desktop.isDesktopSupported()){
             Desktop desktop = Desktop.getDesktop();
             try {
                 desktop.browse(new URI(url));
             } catch (IOException | URISyntaxException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }else{
             Runtime runtime = Runtime.getRuntime();
             try {
                 runtime.exec("xdg-open " + url);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
    }
    //load to signup page
    @FXML
    public void signup() throws IOException {
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(MainApp.class.getResource("view/signup.fxml"));
    	StackPane rootLayout = loader.load();
    	Scene scene = new Scene(rootLayout);
    	MainApp.getPrimaryStage().setScene(scene);
    }
    //reload to Client
	@FXML
	public void client() throws IOException, SQLException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainApp.class.getResource("view/Client.fxml"));
		StackPane rootLayout = loader.load();
		ClientControl controller = loader.<ClientControl>getController();
		controller.setInfo(u,uid);
		Scene scene = new Scene(rootLayout);
		MainApp.getPrimaryStage().setScene(scene);
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