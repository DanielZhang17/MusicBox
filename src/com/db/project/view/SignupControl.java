package com.db.project.view;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.db.project.MainApp;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SignupControl {
    @FXML
    private JFXSpinner wait;
    @FXML
    private Label Error;
    @FXML
    private JFXTextField user;
    @FXML
    private JFXPasswordField pass;
    @FXML
    private JFXPasswordField pass1;
    @FXML
    private JFXTextField email;
    @FXML
    private JFXCheckBox agree;
    @FXML
    private JFXButton Submit;
    @FXML
    private StackPane root;
    private MainApp mainApp;
    private Connection con;
    protected final String KEY= "d4f6d0b1ec8b699f23caf8e767e19c76489bd024dad0a5e2281a9a5955391898";
    private String u;
	private String p;
	private String p1;
	private String e;
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    @FXML
    private void initialize() throws SQLException {
    	this.setMainApp(mainApp);
    }
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
	
	@FXML
	public void back() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainApp.class.getResource("view/login.fxml"));
		StackPane rootLayout = loader.load();
		Scene scene = new Scene(rootLayout);
		MainApp.getPrimaryStage().setScene(scene);
	}

    @FXML
    private void handleKey(KeyEvent a) throws SQLException{
    	if(a.getCode().equals(KeyCode.ENTER)) 
    		check();
    	if(!Submit.isDisabled())
    		submit();
    }
    @FXML
	private void submit() throws SQLException {
    	check();//double check
    	Error.setText("");
    	Error.setAlignment(Pos.CENTER);
    	//Error checking
    	if(!agree.isSelected()) {
    		Error.setText("You must read and agree to the agreement !");
    		Error.setVisible(true);
    		System.out.println(Error.getText());
    	}
    	if(!p.equals(p1)) {
    		Error.setText("The passwords does not match !");
    		Error.setVisible(true);
    		System.out.println(Error.getText());
    	}
    	else if(!(p.length()>=6)) {
    		Error.setText("The password needs to be at least 6 characters long !");
    		Error.setVisible(true);
    		System.out.println(Error.getText());
    	}
    	else {
	    	Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(e);
	    	if(!matcher.find()) {
	    		Error.setText("Please enter the correct email address !");
	    		Error.setVisible(true);
	    		System.out.println(Error.getText());
	    	 }
	    	  //Start to check if the user name is already taken
	    	else {
	    		ArrayList<String> listu = new ArrayList<String>();
	    		ArrayList<String> liste = new ArrayList<String>();
	    		con=DriverManager.getConnection("jdbc:mysql://projects.anranz.xyz:3306/Project?&useSSL=false","Project","2141");
	    		Statement s = con.createStatement();
	    		ResultSet rs = s.executeQuery("SELECT username,email FROM USERS");
	    		while(rs.next()) {
	    			listu.add(rs.getString(1));
	    			liste.add(rs.getString(2));
	    		}
	    		System.out.println("Start checking if the name have been taken...");
	    		if(listu.contains(u)) {
	    			Error.setText("Sorry, the username has been taken.");
	    			Error.setVisible(true);
	    		}
	    		else if(liste.contains(e)) {
	    			Error.setText("Sorry, the email address has already linked to an account.");
	    			Error.setVisible(true);
	    		}
	    		else{//Create User if everything passed the check
	    			PreparedStatement ps=con.prepareStatement("INSERT INTO USERS (username,userpassword,email) VALUES (?, aes_encrypt(?,?),?)");
	    			ps.setString(1, u);
	    			ps.setString(2, p);
	    			ps.setString(3, KEY);
	    			ps.setString(4, e);
	    			wait.setVisible(true);
	    			ps.execute();
	    			System.out.println("user created");
	    			JFXDialogLayout content = new JFXDialogLayout();
	    			content.setHeading(new Text("Success !"));
	    			content.setBody(new Text("Your account is all set!\n\nYou will be redirected to the login page."));
	    			JFXDialog dialog = new JFXDialog(root, new Label("Hello"), JFXDialog.DialogTransition.CENTER);
	    			dialog.setContent(content);
	    			dialog.show();
	    			
	    			 Timeline idlestage = new Timeline(new KeyFrame(Duration.seconds(2.5),new EventHandler<ActionEvent>()
	     		    {
	     		        @Override
	     		        public void handle( ActionEvent event )
	     		        {
	     		        	dialog.close();
	     		        	try {
								back();
								wait.setVisible(false);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	     		        }
	     		    } ) );
	     		    idlestage.setCycleCount(1);
	    		    idlestage.play();
	    		}
	    	}
    	}
	}
    @FXML
	private void check() {
    	u=user.getText();
    	p=pass.getText();
    	p1=pass1.getText();
    	e=email.getText();
    	if(agree.isSelected()&&!u.isEmpty()&&!p.isEmpty()&&!p1.isEmpty()&&!e.isEmpty()) 
    		Submit.setDisable(false);
    	else
    		Submit.setDisable(true);
    }
    @FXML
    private void agreement() {
		
		JFXButton Agree = new JFXButton("Accept");
		JFXButton decline = new JFXButton("Decline");
		JFXDialogLayout content = new JFXDialogLayout();
		content.setActions(Agree,decline);
		content.setHeading(new Text("End User Agreement"));
		content.setBody(new Text("This app is for demo purpose only.\nAll data are acquired online.\n"
				+ "The data you entered above is collected but not discloased.\n Your search history will be obtained and analyized\n"
				+ "\nBy accepting the above rules, you declear that you have read \nand accept the rules."));
		JFXDialog dialog = new JFXDialog(root, new Label("End User Agreement"), JFXDialog.DialogTransition.CENTER);
		dialog.setContent(content);		
		decline.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	dialog.close();
	        	System.exit(0);
	        }
	    });
		Agree.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent e) {
	        	dialog.close();
	        	agree.selectedProperty().set(true);
	        	check();
	        }
	    });
		dialog.show();
    }
}
