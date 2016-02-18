package com.dreamer.ui.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import com.dreamer.common.constant.ConfigSetting;
import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.constant.StrConstant;
import com.dreamer.common.util.AccessDbUtil;
import com.dreamer.common.util.ExcelUtil;
import com.dreamer.common.util.GetPhoneNoUtil;
import com.dreamer.common.vo.LandInfo;
import com.dreamer.excelparser.config.DBConfigLoader;
import com.dreamer.excelparser.format.AdditionalResultDef;
import com.dreamer.excelparser.format.CppSheet1AndResultCellDef;
import com.dreamer.excelparser.vo.CompanyInfo;
import com.dreamer.ui.config.UiConfigLoader;
import com.dreamer.ui.enu.RadioButtonStatus;
import com.dreamer.ui.enu.TableColDef;
import com.dreamer.ui.enu.TableColDef.DataType;
import com.dreamer.ui.vo.Counties;
import com.dreamer.ui.vo.Townships;
import com.syscom.safe.util.xml.ElementNotFoundException;
import com.syscom.safe.util.xml.XmlAggregate;

public class QueryLandInfoUiViewController implements Initializable {
    private Logger mLogger = Logger.getLogger(Log4jSetting.QUERY_UI);

    // ------------- Running needed -------------
    private UiConfigLoader mUiConfigLoader = UiConfigLoader.getInstance();
    private AccessDbUtil mAccessDbUtil = AccessDbUtil.getInstance();

    // ------------- FX Components -------------
    @SuppressWarnings("unused")
    private Stage mStage;
    
    @FXML
    private ComboBox<Counties> cmbCounties;
    private final Counties EMPTY_COUNTIES = new Counties("", null);
    
    @FXML
    private ComboBox<Townships> cmbTownships;
    private final Townships EMPTY_TOWNSHIPS = new Townships("", "");

    @FXML 
    private TextField tfdLandUsePartition;
    @FXML 
    private TextField tfdSegment;
    @FXML
    private TextField tfdOwner;
    @FXML
    private TextField tfdAreaBigger;
    @FXML
    private TextField tfdAreaSmaller;
    
    private ToggleGroup rdoGroup;
    @FXML 
    private RadioButton rdoCompany;
    @FXML 
    private RadioButton rdoPersonal;
    @FXML 
    private RadioButton rdoLegalPerson;
    @FXML 
    private RadioButton rdoOthers;
    @FXML 
    private RadioButton rdoAll;

    @FXML
    private Button btnQry;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnQueryCompanyNos;
    
    @FXML 
    private TableView<LandInfo> tvLandInfos;
    private ObservableList<LandInfo> mQryLandInfos; 

    @FXML
    private Label lblMessage;

    // ------------- Constants -------------
    private final int AREA_BIGGER_DEF_VAL = 0;
    private final int AREA_SMALLER_DEF_VAL = 9999999; 
    private final int EXCEL_MAXIMUM_ROWS = 1048576;
    private final int QRY_ALL_START = 0;
    private final int QRY_ALL_END = 9999999;
    
    // ------------- User Input -------------
    private Counties mSelectedCounties;
    private Townships mSelectedTownships;
    private String mLandUsePartition;
    private String mSegment;
    private String mOwner;
    private int mAreaBigger = AREA_BIGGER_DEF_VAL;
    private int mAreaSmaller = AREA_SMALLER_DEF_VAL;
    private RadioButtonStatus mRadioBtnStat;
    private boolean mNeverContact = true;
    
    // ------------- Member Variables -------------
    private boolean mIsForBoss = false;
    private final String mCreateExcelFilePath = System.getProperty("user.home") + "\\Desktop\\結果.xlsx";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
	mUiConfigLoader.init();

	mIsForBoss = mUiConfigLoader.isForBoss();

	initComponents();

	Counties[] allCounties = mUiConfigLoader.getAllCounties();
	fillCountiesCombo(allCounties);

	initComboBoxListener();

	new Thread(() -> {
	    long startTime = System.currentTimeMillis();
	    showInfoMsgAndLog("嘗試連線至資料庫...");
	    changeAllCompsStatByIsFxAppThread(true);

	    boolean initAccessDbUtilSucceed = initAccessDbUtilAndCreateQueryConnection();
	    if (initAccessDbUtilSucceed) {
		showInfoMsgAndLog("連線至資料庫成功，共花了：<" + calSpentTime(startTime) + " 秒>");
		changeAllCompsStatByIsFxAppThread(false);
	    }
	    else {
		showErrorMsg("連線至資料庫失敗");
	    }
	    mLogger.info(StrConstant.SEPRATE_LINE);
	    
	}).start();
    }
    
    public void setStage(Stage stage) {
	this.mStage = stage;
    }
    
    private void initComponents() {
	rdoGroup = new ToggleGroup();
	
	rdoCompany.setToggleGroup(rdoGroup);
	rdoPersonal.setToggleGroup(rdoGroup);
	rdoLegalPerson.setToggleGroup(rdoGroup);
	rdoOthers.setToggleGroup(rdoGroup);
	rdoAll.setToggleGroup(rdoGroup);
	
	rdoCompany.setSelected(true);
	
	btnQueryCompanyNos.setVisible(mIsForBoss);
	
	// ----------- Define Table Column Property -----------
	defineTableColumeCellValueFactory();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void defineTableColumeCellValueFactory() {
	tvLandInfos.setEditable(true);
	
	ObservableList<TableColumn<LandInfo, ?>> columns = tvLandInfos.getColumns();

	for (int tableColIndex = 0; tableColIndex < columns.size(); tableColIndex++) {
	    TableColDef tableColDef = TableColDef.convertByTableIndex(tableColIndex);
	    
	    PropertyValueFactory propValueFactory = null;
	    
	    DataType dataType = tableColDef.getDateType();
	    String mappingVoField = tableColDef.getMappingVoField();
	    switch (dataType) {
        	    case String:
        		propValueFactory = new PropertyValueFactory<LandInfo, String>(mappingVoField);
        		break;

        	    case Integer:
        		propValueFactory = new PropertyValueFactory<LandInfo, Integer>(mappingVoField);
        		break;

        	    case Long:
        		propValueFactory = new PropertyValueFactory<LandInfo, Long>(mappingVoField);
        		break;

        	    case Double:
        		propValueFactory = new PropertyValueFactory<LandInfo, Double>(mappingVoField);
        		break;
	    }

	    // 設定 PropertyValueFactory 到 TableColumn
	    TableColumn column = columns.get(tableColIndex);
	    column.setCellValueFactory(propValueFactory);
	    
	    if (tableColDef.isEditable()) {
		column.setCellFactory(TextFieldTableCell.forTableColumn());
		column.setOnEditCommit(new EventHandler<CellEditEvent<LandInfo, ?>>() {
		    @Override
		    public void handle(CellEditEvent event) {
			processCellEdited(tableColDef, event);
		    }
		});
	    }
	}
    }

    private void processCellEdited(TableColDef tableColDef, CellEditEvent<LandInfo, ?> event) {
	LandInfo landInfo = (LandInfo) event.getTableView().getItems().get(event.getTablePosition().getRow());
	String newValue = (String) event.getNewValue();
	
	mLogger.info("修改前：" + landInfo);
	
	switch (tableColDef) {
        	case OWNER:
        	    landInfo.setOwner(newValue);
        	    break;
        	case COUNTIES:
        	    landInfo.setCounties(newValue);
        	    break;
        	case TOWNSHIPS:
        	    landInfo.setTownships(newValue);
        	    break;
        	case SEGMENT:
        	    landInfo.setSegment(newValue);
        	    break;
        	case LAND_NO:
        	    landInfo.setLandNo(newValue);
        	    break;

        	case LAND_PRICE:
        	    long landPrice = Long.parseLong(newValue);
        	    landInfo.setLandPrice(landPrice);
        	    break;
        	case AREA:
        	    double area = Double.parseDouble(newValue);
        	    landInfo.setArea(area);
        	    break;
        	case LAND_USE_PARTITION:
        	    landInfo.setLandUsePartition(newValue);
        	    break;
        	case NUMBERS_OF_BUILDING:
        	    int numbersOfBuilding = Integer.parseInt(newValue);
	    	    landInfo.setNumbersOfBuilding(numbersOfBuilding);
        	    break;
        	case NATURAL_PERSON:
        	    int naturalPerson = Integer.parseInt(newValue);
        	    landInfo.setNaturalPerson(naturalPerson);
        	    break;
        	
        	case PRIVATE_LEGAL_PERSON:
        	    int privateLegalPerson = Integer.parseInt(newValue);
        	    landInfo.setPrivateLegalPerson(privateLegalPerson);
        	    break;
        	case PHONE_NO:
        	    landInfo.setPhoneNo(newValue);
        	    break;
        	case ADDRESS:
        	    landInfo.setAddress(newValue);
        	    break;
	}
	mLogger.info("修改後：" + landInfo);
	mLogger.info(StrConstant.SEPRATE_LINE);
    }

    private long calSpentTime(long startTime) {
	return (System.currentTimeMillis() - startTime) / 1000;
    }

    private boolean initAccessDbUtilAndCreateQueryConnection() {
	DBConfigLoader.getInstance().init();

	XmlAggregate agrDbService = DBConfigLoader.getInstance().getDbServiceConfig();
	if (agrDbService == null) {
	    mLogger.error("<db-service> not found in " + ConfigSetting.DB_CONFIG_NAME + ", please check...");
	    System.exit(1);
	}
	else {
	    try {
		mAccessDbUtil.init(mLogger, ConfigSetting.DB_CONFIG_NAME, agrDbService);
	    }
	    catch (ElementNotFoundException | ClassNotFoundException e) {
		showExceptionDialog("初始化連線 DB 工具失敗", "可能設定檔有問題", e);
		System.exit(1);
	    }
	    try {
		mAccessDbUtil.createConnection();
	    }
	    catch (SQLException e) {
		showExceptionDialog("嘗試與 DB 建立連線失敗", "發生了 Exception", e);
		return false;
	    }
	}
	return true;
    }

    @FXML
    public void buttonHandler(ActionEvent event) {
	if (event.getSource().equals(btnQry)) {
	    makeQuery();
	}
	else if (event.getSource().equals(btnClear)) {
	    makeClear();
	}
	else if (event.getSource().equals(btnQueryCompanyNos)) {
	    queryCompanyNosFromInternet();
	}
    }
    
    private void initComboBoxListener() {
	cmbCounties.valueProperty().addListener(new ChangeListener<Counties>() {
	    @Override
	    public void changed(ObservableValue<? extends Counties> observable, Counties oldValue, Counties newValue) {
		Townships[] allTownships = null;
		if (newValue != null) {
		    allTownships = newValue.getAllTownships();
		}
		fillTownshipsCombo(allTownships);
	    }
	});
    }

    private void fillCountiesCombo(Counties[] counties) {
	Platform.runLater(() -> {
	    ObservableList<Counties> items = cmbCounties.getItems();
	    items.clear();

	    ArrayList<Counties> comboValues = new ArrayList<Counties>();
	    comboValues.add(EMPTY_COUNTIES);
	    if (counties != null && counties.length != 0) {
		comboValues.addAll(Arrays.asList(counties));
	    }
	    items.addAll(comboValues);
	});
    }

    private void fillTownshipsCombo(Townships[] townships) {
	Platform.runLater(() -> {
	    ObservableList<Townships> items = cmbTownships.getItems();
	    items.clear();

	    if (townships != null) {
		ArrayList<Townships> comboValues = new ArrayList<Townships>();
		comboValues.add(EMPTY_TOWNSHIPS);
		if (townships != null && townships.length != 0) {
		    comboValues.addAll(Arrays.asList(townships));
		}
		items.addAll(comboValues);
	    }
	});
    }

    private void makeQuery() {
	if (checkInput()) {
	    // 開始查詢
	    processQueryAction();
	}
    }
    
    private boolean checkInput() {
	mSelectedCounties = cmbCounties.getValue();
	mSelectedTownships = cmbTownships.getValue();
	mLandUsePartition = tfdLandUsePartition.getText().trim();
	mSegment = tfdSegment.getText().trim();
	mOwner = tfdOwner.getText().trim();
	
	String inputAreaBigger = tfdAreaBigger.getText().trim();
	if (!"".equals(inputAreaBigger)) {
	    try {
		mAreaBigger = Integer.parseInt(inputAreaBigger);
	    } 
	    catch (Exception e) {
		showErrorMsg("請輸入正確數字");
		tfdAreaBigger.requestFocus();
		return false;
	    }
	}
	
	String inputAreaSmaller = tfdAreaSmaller.getText().trim();
	if (!"".equals(inputAreaSmaller)) {
	    try {
		mAreaSmaller = Integer.parseInt(inputAreaSmaller);
	    } 
	    catch (Exception e) {
		showErrorMsg("請輸入正確數字");
		tfdAreaSmaller.requestFocus();
		return false;
	    }
	}
	
	if (rdoCompany.isSelected()) {
	    mRadioBtnStat = RadioButtonStatus.COMPANY;
	}
	else if (rdoPersonal.isSelected()) {
	    mRadioBtnStat = RadioButtonStatus.PERSONAL;
	}
	else if (rdoLegalPerson.isSelected()) {
	    mRadioBtnStat = RadioButtonStatus.LEGAL_PERSON;
	}
	else if (rdoOthers.isSelected()) {
	    mRadioBtnStat = RadioButtonStatus.OTHERS;
	}
	else if (rdoAll.isSelected()) {
	    mRadioBtnStat = RadioButtonStatus.ALL;
	}
	
	boolean selectedCountiesIsNull = (mSelectedCounties == null);
	boolean selectedTownshipsIsNull = (mSelectedTownships == null);
	boolean inputLandUsePartitionIsEmpty = mLandUsePartition.isEmpty();
	boolean inputSegmentIsEmpty = mSegment.isEmpty();
	boolean inputOwnerIsEmpty = mOwner.isEmpty();
	boolean inputAreaBiggerIsEmpty = (mAreaBigger == AREA_BIGGER_DEF_VAL);
	boolean inputAreaSmallerIsEmpty = (mAreaSmaller == AREA_SMALLER_DEF_VAL);
	
	boolean allFieldsIsEmpty 
		= selectedCountiesIsNull && selectedTownshipsIsNull && inputLandUsePartitionIsEmpty && inputSegmentIsEmpty && 
		  inputOwnerIsEmpty && inputAreaBiggerIsEmpty && inputAreaSmallerIsEmpty;
	
	if (allFieldsIsEmpty) {
	    showWarnDialog("資料量過大，請至少輸入一個條件");
	    return false;
	}
	return true;
    }

    /**
     * 查詢
     * 
     * 1. 根據條件查詢 DB
     * 2. 產生 Excel
     */
    private void processQueryAction() {
	final Counties selectedCounties = mSelectedCounties;
	final Townships selectedTownships = mSelectedTownships;
	final String inputLandUsePartition = mLandUsePartition;
	final String inputSegment = mSegment;
	final String inputOwner = mOwner;
	final int areaBigger = mAreaBigger;
	final int areaSmaller = mAreaSmaller;
	final RadioButtonStatus radioBtnStat = mRadioBtnStat;  
	final boolean neverContact = mNeverContact;
	
	new Thread(() -> {
	    	showInfoMsg("查詢中...");
	    
	    	if (mQryLandInfos != null) {
	    	    mQryLandInfos.clear();
	    	}
	    	
	    	changeAllCompsStatByIsFxAppThread(true);

	    	String searchCondsStr 
	    		= getSearchCondsStr(selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
	    			            inputOwner, areaBigger, areaSmaller, radioBtnStat, neverContact);
	    	
	    	long qryStartTime = System.currentTimeMillis();

	    	mLogger.info("根據下列條件查詢資料筆數：" + searchCondsStr);
	    	
	    	final ArrayList<LandInfo> landInfos = 
        		mAccessDbUtil.queryLandInfos(selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        				   	     inputOwner, areaBigger, areaSmaller, radioBtnStat, neverContact, 
        				   	     QRY_ALL_START, QRY_ALL_END);
	    	
	    	int totalDatasSize = landInfos.size();

		if (totalDatasSize != 0 && totalDatasSize < EXCEL_MAXIMUM_ROWS) {
	    	    if (mIsForBoss) {
	    		createExcel(landInfos);
	    	    }
	    	    
	    	    mQryLandInfos = FXCollections.observableArrayList(landInfos);
                	
	    	    setDatasToTable();

	    	    showInfoMsgAndLog("查詢資料並產生 Excel 完成，資料筆數：<" + totalDatasSize + ">，共花了：<" + calSpentTime(qryStartTime) + " 秒>");
	    	}
		else if (totalDatasSize >= EXCEL_MAXIMUM_ROWS) {
		    mQryLandInfos = FXCollections.observableArrayList(landInfos);
            	
	    	    setDatasToTable();
		    
		    showErrorMsgAndLog("查詢資料完成，資料筆數：<" + totalDatasSize + ">，共花了：<" + calSpentTime(qryStartTime) + " 秒>，但資料量過大，不產生 Excel");
		}

		changeAllCompsStatByIsFxAppThread(false);
		
		mLogger.info(StrConstant.SEPRATE_LINE);
	    	
	}).start();
    }

    private String getSearchCondsStr(
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment, 
	    String inputOwner, int areaBigger, int areaSmaller, 
	    RadioButtonStatus radioBtnStat, boolean neverContact) {

	StringBuilder buffer = new StringBuilder();

	buffer.append("[");
	if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
	    buffer.append("Counties=").append(selectedCounties).append(",");
	}
	if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
	    buffer.append("Townships=").append(selectedTownships).append(",");
	}
	if (!inputLandUsePartition.isEmpty()) {
	    buffer.append("LandUsePartition=").append(inputLandUsePartition).append(",");
	}
	if (!inputSegment.isEmpty()) {
	    buffer.append("Segment=").append(inputSegment).append(",");
	}
	if (!inputOwner.isEmpty()) {
	    buffer.append("Owner=").append(inputOwner).append(",");
	}
	buffer.append("AreaBigger=").append(areaBigger).append(",");
	buffer.append("AreaSmaller=").append(areaSmaller).append(",");
	buffer.append("RadioButtonStatus=").append(radioBtnStat).append(",");
	buffer.append("NeverContact=").append(mNeverContact);
	buffer.append("]");
	      
	return buffer.toString();
    }

    private void setDatasToTable() {
	Platform.runLater(() -> {
	    tvLandInfos.setItems(mQryLandInfos);
	});
    }

    private void makeClear() {
	mLogger.info("開始清除資料...");
	
	// ---------- UI ---------- 
	cmbCounties.setValue(EMPTY_COUNTIES);
	cmbTownships.setValue(EMPTY_TOWNSHIPS);
	tfdLandUsePartition.clear();
	tfdSegment.clear();
	
	tfdOwner.clear();
	tfdAreaBigger.clear();
	tfdAreaSmaller.clear();
	
	rdoCompany.setSelected(true);
	
	if (mQryLandInfos != null) {
	    mQryLandInfos.clear();
	}
	
	lblMessage.setText("");
	
	// ---------- Variables ----------
	mSelectedCounties = null;
	mSelectedTownships = null;
	mLandUsePartition = null;
	mOwner = null;
	mAreaBigger = AREA_BIGGER_DEF_VAL;
	mAreaSmaller = AREA_SMALLER_DEF_VAL;
	mRadioBtnStat = null;
	mNeverContact = true;
	
	changeAllCompsStatByIsFxAppThread(false);
	
	mLogger.info("清除資料完成");
	mLogger.info(StrConstant.SEPRATE_LINE);
    }

    private void createExcel(ArrayList<LandInfo> landInfos) {
	mLogger.info("開始產生 Excel 至 " + mCreateExcelFilePath);
	
	int totalDatas = landInfos.size();
	
	// 宣告 Workbook
	SXSSFWorkbook workbook = new SXSSFWorkbook();

	// 產生 sheet
	Sheet sheet = workbook.createSheet("data");

	// freeze the first row
	sheet.createFreezePane(0, 1);

	// 產生 Title Cell Style
	XSSFCellStyle titleCellStyle = ExcelUtil.getTitleColCellStyle(workbook);

	// 取得所有 title 欄位
	CppSheet1AndResultCellDef[] resultCells = CppSheet1AndResultCellDef.getResultColsIncludeHidden();
	AdditionalResultDef[] additionalResultCells = AdditionalResultDef.values();

	// 定義 rowIndex
	int rowIndex = 0;

	// create title
	Row titleRow = sheet.createRow(rowIndex++);
	for (CppSheet1AndResultCellDef title : resultCells) {
	    int cellIndex = title.getResultWithHiddenCellIndex();

	    Cell cell = titleRow.createCell(cellIndex);
	    cell.setCellValue(title.getTitleName());

	    // set Title Cell Style
	    cell.setCellStyle(titleCellStyle);
	    
	    // set the Column Width
	    sheet.setColumnWidth(cellIndex, title.getColumnWidth() * 256);
	}
	
	// create additional title
	for (AdditionalResultDef additionalTitle : additionalResultCells) {
	    int cellIndex = additionalTitle.getResultWithHiddenCellIndex();

	    Cell cell = titleRow.createCell(cellIndex);
	    cell.setCellValue(additionalTitle.getTitleName());

	    // set Title Cell Style
	    cell.setCellStyle(titleCellStyle);
	    
	    // set the Column Width
	    sheet.setColumnWidth(cellIndex, additionalTitle.getColumnWidth() * 256);
	}

	for (int dataIndex = 1; dataIndex <= totalDatas; dataIndex += 1) {
	    int dataAtListIndex = dataIndex - 1;
	    LandInfo landInfo = landInfos.get(dataAtListIndex);

	    Row dataRow = sheet.createRow(rowIndex++);

	    // 根據 CppSheet1AndResultCellDef 定義欄位來塞入 Data Cell
	    for (CppSheet1AndResultCellDef resultCell : resultCells) {
		int cellIndex = resultCell.getResultWithHiddenCellIndex();

		Cell cell = dataRow.createCell(cellIndex);

		switch (resultCell) {
			case OWNER_LIST:
			    cell.setCellValue(landInfo.getOwner());
			    break;

			case COUNTIES:
			    cell.setCellValue(landInfo.getCounties());
			    break;

			case TOWNSHIPS:
			    cell.setCellValue(landInfo.getTownships());
			    break;

			case SEGMENT:
			    cell.setCellValue(landInfo.getSegment());
			    break;

			case LAND_NO:
			    cell.setCellValue(landInfo.getLandNo());
			    break;

			case LAND_PRICE:
			    cell.setCellValue(landInfo.getLandPrice());
			    break;

			case AREA:
			    cell.setCellValue(landInfo.getArea());
			    break;

			case LAND_USED_PARTITON:
			    cell.setCellValue(landInfo.getLandUsePartition());
			    break;

			case NUMBERS_OF_LAND_BUILDING:
			    cell.setCellValue(landInfo.getNumbersOfBuilding());
			    break;

			case PRIVATE_LEGAL_PERSON:
			    cell.setCellValue(landInfo.getPrivateLegalPerson());
			    break;

			case NATURAL_PERSON:
			    cell.setCellValue(landInfo.getNaturalPerson());
			    break;
		}
	    }
		
	    // 這邊可以將電話、地址產出到 Excel
	    int addressValIndex = AdditionalResultDef.ADDRESS.getResultWithHiddenCellIndex();
	    Cell addressCell = dataRow.createCell(addressValIndex);
	    addressCell.setCellValue(landInfo.getAddress());
		
	    int phoneNoValIndex = AdditionalResultDef.PHONE_NO.getResultWithHiddenCellIndex();
	    Cell phoneCell = dataRow.createCell(phoneNoValIndex);
	    phoneCell.setCellValue(landInfo.getPhoneNo());
	}
	
	// 將檔案寫出
	mLogger.info("產生 " + mCreateExcelFilePath + " 中...");

	File resultFile = new File(mCreateExcelFilePath);

	FileOutputStream fos = null;
	try {
	    fos = new FileOutputStream(resultFile);

	    workbook.write(fos);
		
	    mLogger.info("產生 " + mCreateExcelFilePath + " 完成");
	    mLogger.info(StrConstant.SEPRATE_LINE);
	} 
	catch (IOException e) {
	    showExceptionDialog("產生" + mCreateExcelFilePath + "失敗", "發生 IOException", e);
	} 
	finally {
	    if (fos != null) {
		try {
		    fos.close();
		    fos = null;
		} 
		catch (IOException e) {
		    showExceptionDialog("產生" + mCreateExcelFilePath + "失敗，關閉  IO 錯誤", "發生 IOException", e);
		}
	    }
	    if (workbook != null) {
		try {
		    workbook.close();
		    workbook = null;
		} 
		catch (IOException e) {
		    showExceptionDialog("產生" + mCreateExcelFilePath + "失敗，關閉  IO 錯誤", "發生 IOException", e);
		}
	    }
	}
    }
    
    private void queryCompanyNosFromInternet() {
	new Thread(() -> {
	    showInfoMsgAndLog("開始查詢電話並將結果更新至資料庫...");
	    
	    if (mQryLandInfos != null) {
		mQryLandInfos.clear();
	    }
	    
	    changeAllCompsStatByIsFxAppThread(true);
	    
	    ArrayList<CompanyInfo> distinctCompanies = mAccessDbUtil.queryDistinctCompanies();
	    
	    showInfoMsgAndLog("資料庫共有公司：" + distinctCompanies.size() + " 家，開始去網路上查找...");
	    
	    long startTime = System.currentTimeMillis();
	    
	    HashSet<CompanyInfo> foundResults = GetPhoneNoUtil.getPhoneNo(mLogger, distinctCompanies.toArray(new CompanyInfo[0]));
	    
	    ArrayList<CompanyInfo> successFoundCompanies = new ArrayList<CompanyInfo>();
		
	    for (CompanyInfo foundResult : foundResults) {
		boolean foundNo = !foundResult.getPhoneNo().trim().isEmpty();
		if (foundNo) {
		    successFoundCompanies.add(foundResult);
		}
	    }
	    
	    if (successFoundCompanies.size() != 0) {
		showInfoMsgAndLog("查詢完畢，成功找到資料共：<" + successFoundCompanies.size() + ">，共花了：<" + calSpentTime(startTime) + " 秒>，3 秒後將開始更新資料庫資料...");
    	    
    	    	try {
    	    	    Thread.sleep(3000);
    	    	} catch (Exception e) {
    	    	    e.printStackTrace();
    	    	}
    	    
    	    	startTime = System.currentTimeMillis();
    	    
    	    	showInfoMsgAndLog("開始更新資料庫...");
    	    
    	    	mAccessDbUtil.updateCompanyNoAndAddress(successFoundCompanies);
    	    
    	    	showInfoMsgAndLog("更新至資料庫成功，共花了：<" + calSpentTime(startTime) + " 秒>");
	    }
	    else {
		showInfoMsgAndLog("查詢完畢");
	    }
	    
	    changeAllCompsStatByIsFxAppThread(false);
	    
	    mLogger.info(StrConstant.SEPRATE_LINE);
	    
	}).start();
    }
    
    private void showExceptionDialog(String headerText, String contentText, Exception ex) {
	boolean isFxAppThread = Platform.isFxApplicationThread();
	if (isFxAppThread) {
	    showExceptionDialogByIsFxThread(headerText, contentText, ex);
	}
	else {
	    Platform.runLater(() -> {
		showExceptionDialogByIsFxThread(headerText, contentText, ex);
	    });
	}
    }

    private void showExceptionDialogByIsFxThread(String headerText, String contentText, Exception ex) {
	// 這種都 log 下來
	mLogger.error(ex);
	
	Alert alert = new Alert(AlertType.ERROR);
	alert.setTitle("發生錯誤");
	alert.setHeaderText(headerText);
	alert.setContentText(contentText);
	
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	ex.printStackTrace(pw);
	String exceptionText = sw.toString();
	
	Label label = new Label("The exception stacktrace was:");

	TextArea textArea = new TextArea(exceptionText);
	textArea.setEditable(false);
	textArea.setWrapText(true);

	textArea.setMaxWidth(Double.MAX_VALUE);
	textArea.setMaxHeight(Double.MAX_VALUE);
	GridPane.setVgrow(textArea, Priority.ALWAYS);
	GridPane.setHgrow(textArea, Priority.ALWAYS);

	GridPane expContent = new GridPane();
	expContent.setMaxWidth(Double.MAX_VALUE);
	expContent.add(label, 0, 0);
	expContent.add(textArea, 0, 1);

	// Set expandable Exception into the dialog pane.
	alert.getDialogPane().setExpandableContent(expContent);

	alert.showAndWait();
    }

    @SuppressWarnings("unused")
    private boolean showConfirmDialog(String headerText, String contentText) {
	Alert alert = new Alert(AlertType.CONFIRMATION);
	alert.setTitle("請確認");
	alert.setHeaderText(headerText);
	alert.setContentText(contentText);
	
	Optional<ButtonType> result = alert.showAndWait();
	if (result.get() == ButtonType.OK){
	    return true;
	} 
	else {
	    return false;
	}
    }

    private void showWarnDialog(String msg) {
	boolean isFxAppThread = Platform.isFxApplicationThread();
	if (isFxAppThread) {
	    showWarnDialogByIsFxThread(msg);
	}
	else {
	    Platform.runLater(() -> {
		showWarnDialogByIsFxThread(msg);
	    });
	} 
    }
    
    private void showWarnDialogByIsFxThread(String msg) {
	Alert alert = new Alert(AlertType.WARNING);
	alert.setTitle("警告");
	alert.setHeaderText(msg);
	alert.showAndWait();
    }

    private void changeAllCompsStatByIsFxAppThread(boolean disable) {
	boolean isFxAppThread = Platform.isFxApplicationThread();
	if (isFxAppThread) {
	    changeAllCompsStat(disable);
	}
	else {
	    Platform.runLater(() -> {
		changeAllCompsStat(disable);
	    });
	}
    }
    
    private void changeAllCompsStat(boolean disable) {
	cmbCounties.setDisable(disable);
	cmbTownships.setDisable(disable);
	tfdLandUsePartition.setDisable(disable);
	tfdSegment.setDisable(disable);

	tfdOwner.setDisable(disable);
	tfdAreaBigger.setDisable(disable);
	tfdAreaSmaller.setDisable(disable);
	
	rdoCompany.setDisable(disable);
	rdoLegalPerson.setDisable(disable);
	rdoOthers.setDisable(disable);
	rdoPersonal.setDisable(disable);
	rdoAll.setDisable(disable);
	
	btnQry.setDisable(disable);
	btnClear.setDisable(disable);
	btnQueryCompanyNos.setDisable(disable);
    }
    
    private void showInfoMsg(String infoMsg) {
	showMsg(false, infoMsg, false);
    }

    private void showInfoMsgAndLog(String infoMsg) {
	showMsg(false, infoMsg, true);
    }

    private void showErrorMsg(String errorMsg) {
	showMsg(true, errorMsg, false);
    }
    
    private void showErrorMsgAndLog(String errorMsg) {
	showMsg(true, errorMsg, true);
    }

    private void showMsg(boolean isError, String msg, boolean needLog) {
	boolean isFxAppThread = Platform.isFxApplicationThread();
	if (isFxAppThread) {
	    lblMessage.setText(msg);
	    lblMessage.setTextFill(isError ? Color.RED : Color.BLACK);
	}
	else if (!isFxAppThread) {
	    Platform.runLater(() -> {
		lblMessage.setText(msg);
		lblMessage.setTextFill(isError ? Color.RED : Color.BLACK);
	    });
	}
	if (needLog) {
	    if (isError) {
		mLogger.error(msg);
	    }
	    else {
		mLogger.info(msg);
	    }
	}
    }
}