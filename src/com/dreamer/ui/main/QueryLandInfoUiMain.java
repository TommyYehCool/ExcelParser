package com.dreamer.ui.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.dreamer.common.constant.UiConstant;
import com.dreamer.ui.controller.QueryLandInfoUiViewController;

public class QueryLandInfoUiMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
	FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/QueryLandInfoUiView.fxml")); 
	
	Parent root = (Parent) loader.load();

	QueryLandInfoUiViewController contoller
		= (QueryLandInfoUiViewController) loader.getController();
	
	contoller.setStage(stage);

	setUserAgentStylesheet(STYLESHEET_CASPIAN);

	Scene scene = new Scene(root);
	stage.setTitle(UiConstant.QRY_LAND_INFO_UI_NAME);
	stage.setScene(scene);
	stage.show();
    }

    public static void main(String[] args) {
	launch(args);
    }
}