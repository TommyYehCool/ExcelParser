package com.dreamer.excelextractor.ui.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.dreamer.common.constant.UiConstant;
import com.dreamer.excelextractor.ui.controller.ExcelExtractorUiViewController;

public class ExcelExtractorUiMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
	FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ExcelExtractorUiView.fxml"));

	Parent root = (Parent) loader.load();
	
	ExcelExtractorUiViewController controller 
		= (ExcelExtractorUiViewController) loader.getController();

	controller.setStage(stage);

	setUserAgentStylesheet(STYLESHEET_CASPIAN);

	Scene scene = new Scene(root);
	stage.setTitle(UiConstant.EXCEL_EXTRACOTR_UI_NAME);
	stage.setScene(scene);
	stage.show();
    }

    public static void main(String[] args) {
	launch(args);
    }
}
