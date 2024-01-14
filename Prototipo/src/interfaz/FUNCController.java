/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FUNCController is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package interfaz;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import datatypes.DatosPolinomio;
import datatypes.Pair;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utilitarios.UtilStrings;

import java.util.*;

public class FUNCController extends GeneralidadesController{
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> comboBoxTipo;
    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    private String tipoFUNC = Text.FUNC_POLI;

    private HashMap<Integer, Node> poliElements = new HashMap<>();
    private HashMap<Integer, Node> poliElementsLocal = new HashMap<>();

    private HashMap<Integer, Node> poliConCotasElements = new HashMap<>();
    private HashMap<Integer, Node> poliConCotasElementsLocal = new HashMap<>();
    private HashMap<Integer, Node> poliMultiElements = new HashMap<>();
    private HashMap<Integer, Node> porRangosElements = new HashMap<>();
    private HashMap<Integer, Node> porRangosElementsLocal = new HashMap<>();

    private Integer poliCant = 4;
    private Integer poliConCotasCant = 4;
    private Integer poliMultiCant = 2;
    private Integer porRangosCant = 3;

    private DatosPolinomio datosPolinomio;
    private boolean edicion;
    private FUNCController fueraRangoController;
    private Hashtable<String,FUNCController> polsControllers = new Hashtable<>();
    private ArrayList<RangoFunc> listaRangoFuncs = new ArrayList<>();
    private ArrayList<RangoFunc> listaRangoFuncsLocal = new ArrayList<>();
    private boolean unloaded = false;


    @FXML private TitledPane tiledPane;

    String titulo;

    public FUNCController(DatosPolinomio datosPolinomio, String titulo){
        this.datosPolinomio = datosPolinomio;
        edicion = true;
        tipoFUNC = datosPolinomio.getTipo();
        this.titulo = titulo;

    }

    public FUNCController(DatosPolinomio datosPolinomio){
        this.datosPolinomio = datosPolinomio;
        edicion = true;
        tipoFUNC = datosPolinomio.getTipo();
        this.titulo = titulo;
    }

    public FUNCController(String titulo){
        edicion = false;
        this.titulo = titulo;
    }

    @FXML
    public void initialize(){

        System.out.println("FUNC Initialize");

        tiledPane.setText(titulo);
        comboBoxTipo.getItems().addAll(new ArrayList<>(Arrays.asList(Text.FUNC_POLI_LABEL, Text.FUNC_POLI_CON_COTAS_LABEL, Text.FUNC_POLI_MULTI_LABEL, Text.FUNC_POR_RANGOS_LABEL)));
        comboBoxTipo.getSelectionModel().selectFirst();
        comboBoxTipo.valueProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
        comboBoxTipo.setOnAction(actionEvent -> {
            switch (comboBoxTipo.getValue()){
                case Text.FUNC_POLI_LABEL:
                    clearPoliConCotas();
                    clearPoliMulti();
                    clearPoliPorRangos();
                    renderPoli();
                    break;
                case Text.FUNC_POLI_CON_COTAS_LABEL:
                    clearPoli();
                    clearPoliMulti();
                    clearPoliPorRangos();
                    renderPoliConCotas();
                    break;
                case Text.FUNC_POLI_MULTI_LABEL:
                    clearPoli();
                    clearPoliConCotas();
                    clearPoliPorRangos();
                    renderPoliMulti();
                    break;
                case Text.FUNC_POR_RANGOS_LABEL:
                    clearPoli();
                    clearPoliConCotas();
                    clearPoliMulti();
                    renderPoliPorRangos();
                    break;
            }
        });

        if(edicion){
            unloadData();
        }else{
            renderPoli();
        }


        inputCancelar.setOnAction(actionEvent -> ((Stage)inputCancelar.getScene().getWindow()).close());

        inputAceptar.setOnAction(actionEvent -> {
            if(controlDatosCompletos()) {
                loadData();
                ((Stage) inputCancelar.getScene().getWindow()).close();
            }else{
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });

        this.datosPolinomio.setEditoValores(false);
    }

    //POLI
    private void renderPoli() {
        //Etiqueta Grado
        Label gradoLabel = new Label("Grado:");
        gradoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(gradoLabel,0,2);
        poliElements.put(0, gradoLabel);

        //Input Grado
        JFXTextField gradoTxtField = new JFXTextField();
        gradoTxtField.setText(String.valueOf(poliCant));
        gradoTxtField.setMinWidth(54);
        gradoTxtField.setMaxWidth(54);
        gridPane.add(gradoTxtField, 1, 2, 2, 1);
        poliElements.put(1, gradoTxtField);

        gradoTxtField.setOnAction(actionEvent -> renderPoliCoefInputs(poliCant, Integer.parseInt(gradoTxtField.getText())));
        gradoTxtField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if(UtilStrings.esNumeroEntero(gradoTxtField.getText())){
                    renderPoliCoefInputs(poliCant, Integer.parseInt(gradoTxtField.getText()));
                }
            }
        });
        renderPoliCoefInputs(0, poliCant);
        tipoFUNC = Text.FUNC_POLI;
    }

    private void renderPoliCoefInputs(Integer start, Integer largo) {
        for(int i=start;i<largo;i++){
            JFXTextField inputLargo = new JFXTextField();
            inputLargo.setMinWidth(54);
            inputLargo.setMaxWidth(54);
            int col = i % 6;
            int row = (i / 6) + 4;
            gridPane.add(inputLargo, col, row);
            GridPane.setMargin(inputLargo, new Insets(9, 0, 0, 0));
            poliElements.put(10*col+row, inputLargo);
            if(poliElementsLocal != null && poliElementsLocal.size() > i){
                inputLargo.setText(((JFXTextField)poliElementsLocal.get(10*col+row)).getText());
            }
        }

        for(int i=largo; i<=poliCant; i++){
            int col = i % 6;
            int row = (i / 6) + 4;
            gridPane.getChildren().remove(poliElements.get(10*col+row));
            poliElements.remove(10*col+row);
        }
        poliCant = largo;
    }

    private void clearPoli() {
        for(Node n: poliElements.values()){
            gridPane.getChildren().remove(n);
        }
        poliElements.clear();
    }

    //POLI CON COTAS
    private void renderPoliConCotas() {
        //Etiqueta XMIN
        Label xminLabel = new Label("X mín:");
        xminLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(xminLabel,0,2);
        poliConCotasElements.put(0, xminLabel);

        //Input XMIN
        JFXTextField xminTextField = new JFXTextField();
//        xminTextField.setText(String.valueOf(poliConCotasCant));
        xminTextField.setMinWidth(54);
        xminTextField.setMaxWidth(54);
        gridPane.add(xminTextField, 1, 2, 2, 1);
//        GridPane.setHalignment(xminTextField, HPos.CENTER);
//        GridPane.setMargin(xminTextField, new Insets(9, 0, 0, 0));
        poliConCotasElements.put(1, xminTextField);

        //Etiqueta XMAX
        Label xmaxLabel = new Label("X máx:");
        xmaxLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(xmaxLabel,3,2);
        poliConCotasElements.put(2, xmaxLabel);

        //Input XMAX
        JFXTextField xmaxTextField = new JFXTextField();
//        xmaxTextField.setText(String.valueOf(poliConCotasCant));
        xmaxTextField.setMinWidth(54);
        xmaxTextField.setMaxWidth(54);
        gridPane.add(xmaxTextField, 4, 2, 2, 1);
//        GridPane.setHalignment(xmaxTextField, HPos.CENTER);
//        GridPane.setMargin(xmaxTextField, new Insets(9, 0, 0, 0));
        poliConCotasElements.put(3, xmaxTextField);

        //Etiqueta VALMIN
        Label valminLabel = new Label("Y mín:");
        valminLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(valminLabel,0,3);
        poliConCotasElements.put(4, valminLabel);

        //Input VALMIN
        JFXTextField valminTextField = new JFXTextField();
//        valminTextField.setText(String.valueOf(poliConCotasCant));
        valminTextField.setMinWidth(54);
        valminTextField.setMaxWidth(54);
        gridPane.add(valminTextField, 1, 3, 2, 1);
//        GridPane.setHalignment(valminTextField, HPos.CENTER);
//        GridPane.setMargin(valminTextField, new Insets(9, 0, 0, 0));
        poliConCotasElements.put(5, valminTextField);

        //Etiqueta VALMAX
        Label valmaxLabel = new Label("Y máx:");
        valmaxLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(valmaxLabel,3,3);
        poliConCotasElements.put(6, valmaxLabel);

        //Input VALMAX
        JFXTextField valmaxTextField = new JFXTextField();
//        valmaxTextField.setText(String.valueOf(poliConCotasCant));
        valmaxTextField.setMinWidth(54);
        valmaxTextField.setMaxWidth(54);
        gridPane.add(valmaxTextField, 4, 3, 2, 1);
//        GridPane.setHalignment(valmaxTextField, HPos.CENTER);
//        GridPane.setMargin(valmaxTextField, new Insets(9, 0, 0, 0));
        poliConCotasElements.put(7, valmaxTextField);

        //Etiqueta Grado
        Label gradoLabel = new Label("Grado:");
        gradoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(gradoLabel,0,5);
        poliConCotasElements.put(8, gradoLabel);

        //Input Grado
        JFXTextField gradoTxtField = new JFXTextField();
        gradoTxtField.setText(String.valueOf(poliConCotasCant));
        gradoTxtField.setMinWidth(54);
        gradoTxtField.setMaxWidth(54);
        gridPane.add(gradoTxtField, 1, 5, 2, 1);
//        GridPane.setHalignment(gradoTxtField, HPos.CENTER);
//        GridPane.setMargin(gradoTxtField, new Insets(9, 0, 0, 0));
        poliConCotasElements.put(9, gradoTxtField);

        gradoTxtField.setOnAction(actionEvent -> renderPoliConCotasCoefInputs(poliConCotasCant, Integer.parseInt(gradoTxtField.getText())));

        gradoTxtField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if(UtilStrings.esNumeroEntero(gradoTxtField.getText())){
                    renderPoliConCotasCoefInputs(poliConCotasCant, Integer.parseInt(gradoTxtField.getText()));
                }

            }
        });
        renderPoliConCotasCoefInputs(0, poliConCotasCant);

        tipoFUNC = Text.FUNC_POLI_CON_COTAS;
    }

    private void renderPoliConCotasCoefInputs(Integer start, Integer largo) {
        for(int i=start;i<largo;i++){
            JFXTextField inputLargo = new JFXTextField();
            inputLargo.setMinWidth(54);
            inputLargo.setMaxWidth(54);
            int col = i % 6;
            int row = (i / 6) + 6;
            gridPane.add(inputLargo, col, row);
            GridPane.setMargin(inputLargo, new Insets(9, 0, 0, 0));
            poliConCotasElements.put(10*(col+1)+row, inputLargo);
            if(poliConCotasElementsLocal != null && poliConCotasElementsLocal.size() > i ){
                inputLargo.setText( ((JFXTextField)poliConCotasElementsLocal.get(10*(col+1)+row)).getText());
            }
        }

        for(int i=largo; i<=poliConCotasCant; i++){
            int col = i % 6;
            int row = (i / 6) + 6;
            gridPane.getChildren().remove(poliConCotasElements.get(10*(col+1)+row));
            poliConCotasElements.remove(10*(col+1)+row);
        }
        poliConCotasCant = largo;
    }

    private void clearPoliConCotas() {
        for(Node n: poliConCotasElements.values()){
            gridPane.getChildren().remove(n);
        }
        poliConCotasElements.clear();
    }

    //POLI MULTI
    private void renderPoliMulti() {

        //Etiqueta QErogado
        Label varQErogado = new Label(Text.FUNC_VAR_Q_EROGADO + ":");
        varQErogado.setStyle("-fx-font-size: 15px;");
        gridPane.add(varQErogado,0,2,2,1);
        poliMultiElements.put(0, varQErogado);

        //Input F QErogado
        JFXButton funcQErogadoBtn = new JFXButton("F");
        funcQErogadoBtn.setButtonType(JFXButton.ButtonType.RAISED);
        funcQErogadoBtn.setRipplerFill(Paint.valueOf("WHITE"));
        funcQErogadoBtn.setStyle("-fx-font-size: 15px; -fx-background-color:  #2db149;");
        funcQErogadoBtn.setTextFill(Paint.valueOf("WHITE"));
        funcQErogadoBtn.setAlignment(Pos.BOTTOM_LEFT);
        gridPane.add(funcQErogadoBtn, 2, 2);
        poliMultiElements.put(1, funcQErogadoBtn);
        if(!edicion){
            FUNCController controllerQErogado = new FUNCController(titulo + ": QErogado");
            polsControllers.put(Text.FUNC_VAR_Q_EROGADO, controllerQErogado);
            setFuncBtnAction(funcQErogadoBtn, controllerQErogado);
        }

        //Etiqueta CotaAguasAbajo
        Label varCotaAguasAbajo = new Label(Text.FUNC_VAR_COTA_AGUAS_ABAJO + ":");
        varCotaAguasAbajo.setStyle("-fx-font-size: 15px;");
        gridPane.add(varCotaAguasAbajo,0,3,2,1);
        poliMultiElements.put(2, varCotaAguasAbajo);

        //Input F CotaAguasAbajo
        JFXButton funcCotaAguasAbajoBtn = new JFXButton("F");
        funcCotaAguasAbajoBtn.setButtonType(JFXButton.ButtonType.RAISED);
        funcCotaAguasAbajoBtn.setRipplerFill(Paint.valueOf("WHITE"));
        funcCotaAguasAbajoBtn.setStyle("-fx-font-size: 15px; -fx-background-color:  #2db149;");
        funcCotaAguasAbajoBtn.setTextFill(Paint.valueOf("WHITE"));
        funcCotaAguasAbajoBtn.setAlignment(Pos.BOTTOM_LEFT);
        gridPane.add(funcCotaAguasAbajoBtn, 2, 3);
        poliMultiElements.put(3, funcCotaAguasAbajoBtn);
        if(!edicion){
            FUNCController controllerCotaAguasAbajo = new FUNCController(titulo + ": C/A. Abajo");
            polsControllers.put(Text.FUNC_VAR_COTA_AGUAS_ABAJO, controllerCotaAguasAbajo);
            setFuncBtnAction(funcCotaAguasAbajoBtn, controllerCotaAguasAbajo);
        }

        tipoFUNC = Text.FUNC_POLI_MULTI;
    }

    private void renderPoliMultiPoliInputs(Integer start, Integer largo) {
        for(int i=start;i<largo;i++){
            Label poliLabel = new Label("Poli "+ (i+1) + ":");
            poliLabel.setStyle("-fx-font-size: 15px;");
            JFXButton funcBtn = new JFXButton("F");
            funcBtn.setButtonType(JFXButton.ButtonType.RAISED);
            funcBtn.setRipplerFill(Paint.valueOf("WHITE"));
            funcBtn.setStyle("-fx-font-size: 15px; -fx-background-color:  #27864d;");
            funcBtn.setTextFill(Paint.valueOf("WHITE"));
            funcBtn.setAlignment(Pos.BOTTOM_LEFT);
//            int col = i % 6;
//            int row = (i / 6) + 4;
            gridPane.add(poliLabel, 0, 4+i);
            gridPane.add(funcBtn, 1, 4+i);
            GridPane.setMargin(poliLabel, new Insets(9, 0, 0, 0));
            GridPane.setMargin(funcBtn, new Insets(9, 0, 0, 0));
            poliMultiElements.put((i+1)*10+1, poliLabel);
            poliMultiElements.put((i+1)*10+2, funcBtn);
        }

        for(int i=largo; i<=poliMultiCant; i++){
//            int col = i % 6;
//            int row = (i / 6) + 4;
            gridPane.getChildren().remove(poliMultiElements.get((i+1)*10+1));
            gridPane.getChildren().remove(poliMultiElements.get((i+1)*10+2));
            poliMultiElements.remove((i+1)*10+1);
            poliMultiElements.remove((i+1)*10+2);
        }
        poliMultiCant = largo;
    }

    private void clearPoliMulti() {
        for(Node n: poliMultiElements.values()){
            gridPane.getChildren().remove(n);
        }
        poliMultiElements.clear();
    }

    //POR RANGOS
    private void renderPoliPorRangos() {
        //Etiqueta Cantidad Rangos
        Label cantRangosLabel = new Label("Cantidad de Rangos:");
        cantRangosLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(cantRangosLabel,0,2,3,1);
        porRangosElements.put(0, cantRangosLabel);

        //Input Cantidad Rangos
        JFXTextField cantRangosTxtField = new JFXTextField();
        cantRangosTxtField.textProperty().addListener((observable, oldValue, newValue) -> {  if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }   });
        cantRangosTxtField.setText(String.valueOf(porRangosCant));
        cantRangosTxtField.setMinWidth(54);
        cantRangosTxtField.setMaxWidth(54);
        gridPane.add(cantRangosTxtField, 3, 2, 2, 1);
//        GridPane.setHalignment(cantRangosTxtField, HPos.CENTER);
//        GridPane.setMargin(cantRangosTxtField, new Insets(9, 0, 0, 0));
        porRangosElements.put(1, cantRangosTxtField);

        cantRangosTxtField.setOnAction(actionEvent -> renderRangos(porRangosCant, Integer.parseInt(cantRangosTxtField.getText())));
        cantRangosTxtField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if(UtilStrings.esNumeroEntero(cantRangosTxtField.getText())){
                    renderRangos(porRangosCant, Integer.parseInt(cantRangosTxtField.getText()));
                }
            }
        });
        renderRangos(0, porRangosCant);

        //Etiqueta Fuera de Rango
        Label fueraDeRangoLabel = new Label("Fuera de Rango:");
        fueraDeRangoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(fueraDeRangoLabel,0,3,3,1);
        porRangosElements.put(2, fueraDeRangoLabel);

        //Input Fuera de Rango
        JFXButton funcBtn = new JFXButton("F");
        funcBtn.setButtonType(JFXButton.ButtonType.RAISED);
        funcBtn.setRipplerFill(Paint.valueOf("WHITE"));
        funcBtn.setStyle("-fx-font-size: 15px; -fx-background-color:  #2db149;");
        funcBtn.setTextFill(Paint.valueOf("WHITE"));
        funcBtn.setAlignment(Pos.BOTTOM_LEFT);
        gridPane.add(funcBtn,3,3,1,1);
        GridPane.setMargin(funcBtn, new Insets(9, 0, 0, 0));
        porRangosElements.put(3, funcBtn);

        //TODO: controlar que se cree una sola vez
        if(!edicion) {
            fueraRangoController = new FUNCController(titulo + ": F/Rango");
            setFuncBtnAction(funcBtn, fueraRangoController);
        }


        //Etiqueta Rangos
        Label rangosLabel = new Label("Rangos:");
        rangosLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(rangosLabel,0,4,2,1);
        porRangosElements.put(4, rangosLabel);

        tipoFUNC = Text.FUNC_POR_RANGOS;
    }

    private void renderRangos(Integer start, Integer largo){

        // Version con subclase
        for(int i=start;i<largo;i++){
            RangoFunc rangoFunc = new RangoFunc();
            System.out.println("NEW RANGO FUNC");
            listaRangoFuncs.add(rangoFunc);
            gridPane.add(rangoFunc.getStart(), 0, 5+i);
            gridPane.add(rangoFunc.getEnd(), 1, 5+i);
            gridPane.add(rangoFunc.getFuncBtn(), 2, 5+i);
            GridPane.setMargin(rangoFunc.getStart(), new Insets(9, 0, 0, 0));
            GridPane.setMargin(rangoFunc.getEnd(), new Insets(9, 0, 0, 0));
            GridPane.setMargin(rangoFunc.getFuncBtn(), new Insets(9, 0, 0, 0));
            porRangosElements.put((i+1)*10+1, rangoFunc.getStart());
            porRangosElements.put((i+1)*10+2, rangoFunc.getEnd());
            porRangosElements.put((i+1)*10+3, rangoFunc.getFuncBtn());

            int nextStart = (i+2)*10+1;
            rangoFunc.getEnd().setOnAction(actionEvent -> {
                if(porRangosElements.containsKey(nextStart)){
                    ((JFXTextField)porRangosElements.get(nextStart)).setText(rangoFunc.getEnd().getText());
                }
            });

            int previousEnd = (i)*10+2;
            rangoFunc.getStart().setOnAction(actionEvent -> {
                if(previousEnd > 12 && porRangosElements.containsKey(previousEnd)){
                    ((JFXTextField)porRangosElements.get(previousEnd)).setText(rangoFunc.getStart().getText());
                }
            });

            if(porRangosElementsLocal != null && porRangosElementsLocal.size() > i){

                rangoFunc.getStart().setText(((JFXTextField)porRangosElementsLocal.get((i + 1) * 10 + 1)).getText());
                rangoFunc.getEnd().setText(((JFXTextField)porRangosElementsLocal.get((i + 1) * 10 + 2)).getText());
                rangoFunc.setFuncController(listaRangoFuncsLocal.get(i).getFuncController());

            }
        }

        for(int i=largo; i<porRangosCant; i++){

            gridPane.getChildren().remove(porRangosElements.get((i+1)*10+1));
            gridPane.getChildren().remove(porRangosElements.get((i+1)*10+2));
            gridPane.getChildren().remove(porRangosElements.get((i+1)*10+3));
            porRangosElements.remove((i+1)*10+1);
            porRangosElements.remove((i+1)*10+2);
            porRangosElements.remove((i+1)*10+3);
            //TODO: remove rangoFunc de la lista
            listaRangoFuncs.remove(listaRangoFuncs.size()-1);
        }
        porRangosCant = largo;
    }

    private void clearPoliPorRangos() {
        for(Node n: porRangosElements.values()){
            gridPane.getChildren().remove(n);
        }
        porRangosElements.clear();
        listaRangoFuncs.clear();
    }


    public DatosPolinomio getDatosPolinomio(){
        if(!edicion || unloaded) {
            loadData();
        }
        return datosPolinomio;
    }

    public DatosPolinomio getDatosPolinomioSinLoadData(){
        return datosPolinomio;
    }


    public void setDatosPolinomio(DatosPolinomio dp){

         datosPolinomio = dp;
    }

    public static void setFuncBtnAction(JFXButton funcBtn, FUNCController funcController){
        funcBtn.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println("NEW FUNC WINDOW");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("FUNC.fxml"));
                    loader.setController(funcController);
                    Parent root1 = loader.load();
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("FUNC");
                    Scene firstScene = new Scene(root1, 359, 456);
                    firstScene.getRoot().requestFocus();
                    stage.setScene(firstScene);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }

    private static class RangoFunc{
        private JFXTextField start;
        private JFXTextField end;
        private JFXButton funcBtn;
        private FUNCController funcController;
        private DatosPolinomio datosPolinomio;

        public RangoFunc(){
            start = new JFXTextField();
            end = new JFXTextField();
            funcBtn = new JFXButton("F");
            start.setMinWidth(54);
            start.setMaxWidth(54);
            end.setMinWidth(54);
            end.setMaxWidth(54);
            funcBtn.setButtonType(JFXButton.ButtonType.RAISED);
            funcBtn.setRipplerFill(Paint.valueOf("WHITE"));
            funcBtn.setStyle("-fx-font-size: 15px; -fx-background-color: #2db149;");
            funcBtn.setTextFill(Paint.valueOf("WHITE"));
            funcBtn.setAlignment(Pos.BOTTOM_LEFT);
            funcController = new FUNCController("Rango");
            setFuncBtnAction(funcBtn, funcController);
            start.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
            end.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });

        }

        public JFXTextField getStart() {
            return start;
        }
        public JFXTextField getEnd() {
            return end;
        }
        public JFXButton getFuncBtn() {
            return funcBtn;
        }
        public FUNCController getFuncController() {
            return funcController;
        }
        public void setFuncController(FUNCController funcController) {
            this.funcController = funcController;
            setFuncBtnAction(funcBtn, funcController);
        }

        public void setDatosPolinomio(DatosPolinomio datosPolinomio) {
            this.datosPolinomio = datosPolinomio;
        }

        public void setStartVal(Double val){
            start.setText(String.valueOf(val));
        }

        public void setEndVal(Double val){
            end.setText(String.valueOf(val));
        }

    }


    public boolean polinomioCompleto(DatosPolinomio pPolinomio){
        boolean ret = true;

        if(pPolinomio != null){
            switch (pPolinomio.getTipo()) {

                case Text.FUNC_POLI:
                    ret = pPolinomio.getCoefs().length>0;
                    break;

                case Text.FUNC_POLI_CON_COTAS:
                    ret = pPolinomio.getCoefs().length>0;
                    ret = ret && pPolinomio.getXmax() != null;
                    ret = ret && pPolinomio.getXmin() != null;
                    ret = ret && pPolinomio.getValmin() != null;
                    ret = ret && pPolinomio.getValmax() != null;
                    break;

                case Text.FUNC_POLI_MULTI:
                    for(DatosPolinomio p: datosPolinomio.getPols().values()){
                        ret = this.polinomioCompleto(p);
                    }

                case Text.FUNC_POR_RANGOS:

                    this.polinomioCompleto(pPolinomio.getFueraRango());
                    for(DatosPolinomio p: datosPolinomio.getPols().values()){
                        ret = this.polinomioCompleto(p);
                    }
                    for(Pair<Double,Double> pair: pPolinomio.getRangos()){
                        ret = ret && pair.first != null && pair.second != null;
                    }

                    break;
            }
        } else {
            ret = false;
        }

        return ret;
    }


    /**
     *  Método para cargar los datos en el DatosPolinomio
     */
    private void loadData(){
        System.out.println("LOAD DATA-FUNC");

        if(!edicion){
            datosPolinomio = new DatosPolinomio();
        }

        if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POLI)){
            double[] coefs = new double[poliCant];
            for(int i=0;i<poliCant;i++){
                int col = i % 6;
                int row = (i / 6) + 4;
                coefs[i] = Double.parseDouble(((JFXTextField)poliElements.get(10*col+row)).getText());
            }

            datosPolinomio.setTipo(tipoFUNC);
            datosPolinomio.setCoefs(coefs);
            //TODO: set var ?
        }else if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POLI_CON_COTAS)){
            double xmin = Double.parseDouble(((JFXTextField)poliConCotasElements.get(1)).getText());
            double xmax = Double.parseDouble(((JFXTextField)poliConCotasElements.get(3)).getText());
            double ymin = Double.parseDouble(((JFXTextField)poliConCotasElements.get(5)).getText());
            double ymax = Double.parseDouble(((JFXTextField)poliConCotasElements.get(7)).getText());
            double[] coefs = new double[poliConCotasCant];
            for(int i=0;i<poliConCotasCant;i++){
                int col = i % 6;
                int row = (i / 6) + 6;
                coefs[i] = Double.parseDouble(((JFXTextField)poliConCotasElements.get(10*(col+1)+row)).getText());
            }

            datosPolinomio.setTipo(tipoFUNC);
            datosPolinomio.setXmin(xmin);
            datosPolinomio.setXmax(xmax);
            datosPolinomio.setValmin(ymin);
            datosPolinomio.setValmax(ymax);
            datosPolinomio.setCoefs(coefs);
        }else if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POLI_MULTI)){
            Hashtable<String,DatosPolinomio> pols = new Hashtable<>();
            for(HashMap.Entry<String, FUNCController> entry : polsControllers.entrySet()){
                pols.put(entry.getKey(), entry.getValue().getDatosPolinomio());
            }

            datosPolinomio.setTipo(tipoFUNC);
            datosPolinomio.setPols(pols);
        }else if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POR_RANGOS)){
            ArrayList<Pair<Double, Double>> rangos = new ArrayList<>();
            ArrayList<DatosPolinomio> polsRangos = new ArrayList<>();
            DatosPolinomio fueraRango = fueraRangoController.getDatosPolinomio();
            for(int i=0;i<porRangosCant;i++){
                JFXTextField inputRangoStart = (JFXTextField)porRangosElements.get((i+1)*10+1);
                JFXTextField inputRangoEnd = (JFXTextField)porRangosElements.get((i+1)*10+2);
                Pair<Double, Double> rango = new Pair<>(Double.parseDouble(inputRangoStart.getText()), Double.parseDouble(inputRangoEnd.getText()));
                rangos.add(rango);
                polsRangos.add(listaRangoFuncs.get(i).getFuncController().getDatosPolinomio());
            }

            datosPolinomio.setTipo(tipoFUNC);
            datosPolinomio.setFueraRango(fueraRango);
            datosPolinomio.setRangos(rangos);
            datosPolinomio.setPolsrangos(polsRangos);
        }
        edicion = true;
    }

    /**
     * Método para obtener los datos del DatosPolinomio y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-FUNC");
        System.out.println(">>>"+tipoFUNC);
        if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POLI)){

            clearPoli();

            comboBoxTipo.getSelectionModel().select(Text.FUNC_POLI_LABEL);
            double[] coefs = datosPolinomio.getCoefs();
            poliCant = coefs.length;
            renderPoli();
            for(int i=0;i<poliCant;i++){
                int col = i % 6;
                int row = (i / 6) + 4;
                JFXTextField coefInput = (JFXTextField) poliElements.get(10*col+row);
                coefInput.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
                coefInput.setText(String.valueOf(coefs[i]));
            }
            poliElementsLocal = new HashMap<>(poliElements);
        }else if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POLI_CON_COTAS)){

            clearPoliConCotas();

            comboBoxTipo.getSelectionModel().select(Text.FUNC_POLI_CON_COTAS_LABEL);
            double[] coefs = datosPolinomio.getCoefs();
            poliConCotasCant = coefs.length;
            renderPoliConCotas();
            JFXTextField inputXmin = (JFXTextField)poliConCotasElements.get(1);
            JFXTextField inputXmax = (JFXTextField)poliConCotasElements.get(3);
            JFXTextField inputYmin = (JFXTextField)poliConCotasElements.get(5);
            JFXTextField inputYmax = (JFXTextField)poliConCotasElements.get(7);
            inputXmin.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
            inputXmax.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
            inputYmin.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
            inputYmax.textProperty().addListener((observable, oldValue, newValue) -> {   if(datosPolinomio != null){ if(datosPolinomio != null){ datosPolinomio.setEditoValores(true); }  }     });
            inputXmin.setText(String.valueOf(datosPolinomio.getXmin()));
            inputXmax.setText(String.valueOf(datosPolinomio.getXmax()));
            inputYmin.setText(String.valueOf(datosPolinomio.getValmin()));
            inputYmax.setText(String.valueOf(datosPolinomio.getValmax()));
            for(int i=0;i<poliConCotasCant;i++){
                int col = i % 6;
                int row = (i / 6) + 6;
                JFXTextField coefInput = (JFXTextField)poliConCotasElements.get(10*(col+1)+row);
                coefInput.setText(String.valueOf(coefs[i]));
            }
            poliConCotasElementsLocal = new HashMap<>(poliConCotasElements);
        }else if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POLI_MULTI)){

            clearPoliMulti();

            comboBoxTipo.getSelectionModel().select(Text.FUNC_POLI_MULTI_LABEL);
            renderPoliMulti();
            FUNCController controllerQErogado = new FUNCController(datosPolinomio.getPols().get(Text.FUNC_VAR_Q_EROGADO),titulo + ": QErogado");
            polsControllers.put(Text.FUNC_VAR_Q_EROGADO, controllerQErogado);
            FUNCController controllerCotaAguasAbajo = new FUNCController(datosPolinomio.getPols().get(Text.FUNC_VAR_COTA_AGUAS_ABAJO),titulo + ": C/A. Abajo");
            polsControllers.put(Text.FUNC_VAR_COTA_AGUAS_ABAJO, controllerCotaAguasAbajo);
            setFuncBtnAction((JFXButton)poliMultiElements.get(1), controllerQErogado);
            setFuncBtnAction((JFXButton)poliMultiElements.get(3), controllerCotaAguasAbajo);

        }else if(tipoFUNC.equalsIgnoreCase(Text.FUNC_POR_RANGOS)){
            //TODO: nombre var rango

            clearPoliPorRangos();

            comboBoxTipo.getSelectionModel().select(Text.FUNC_POR_RANGOS_LABEL);
            ArrayList<Pair<Double, Double>> rangos = datosPolinomio.getRangos();
            ArrayList<DatosPolinomio> polsRangos = datosPolinomio.getPolsrangos();
            porRangosCant = rangos.size();
            renderPoliPorRangos();
            fueraRangoController = new FUNCController(datosPolinomio.getFueraRango(), titulo + ": F/Rango" );
            setFuncBtnAction((JFXButton)porRangosElements.get(3), fueraRangoController);
            for(int i=0;i<listaRangoFuncs.size();i++){
                RangoFunc rangoFunc = listaRangoFuncs.get(i);
                rangoFunc.setStartVal(rangos.get(i).first);
                rangoFunc.setEndVal(rangos.get(i).second);
                FUNCController controller = new FUNCController(polsRangos.get(i), titulo + ": Rango");
                rangoFunc.setDatosPolinomio(polsRangos.get(i));
                rangoFunc.setFuncController(controller);
            }
            porRangosElementsLocal = new HashMap<>(porRangosElements);
            listaRangoFuncsLocal = new ArrayList<>(listaRangoFuncs);

        }
        unloaded = true;
    }

    public boolean controlDatosCompletos(){
        boolean ret = true;
        try {
            switch (tipoFUNC) {
                case Text.FUNC_POLI:
                    ret = ret && !((JFXTextField ) gridPane.getChildren().get(4)).getText().trim().equals("");
                    for (int i = 0; i < poliCant; i++) {
                        int col = i % 6;
                        int row = (i / 6) + 4;
                        ret = ret && !((JFXTextField) poliElements.get(10 * col + row)).getText().trim().equals("") ;
                    }
                    break;

                case Text.FUNC_POLI_CON_COTAS:
                    ret = poliConCotasCant > 0;

                    ret = ret && !((JFXTextField) poliConCotasElements.get(1)).getText().trim().equals("");
                    ret = ret && !((JFXTextField) poliConCotasElements.get(3)).getText().trim().equals("");
                    ret = ret && !((JFXTextField) poliConCotasElements.get(5)).getText().trim().equals("");
                    ret = ret && !((JFXTextField) poliConCotasElements.get(7)).getText().trim().equals("");
                    for (int i = 0; i < poliConCotasCant; i++) {
                        int col = i % 6;
                        int row = (i / 6) + 6;
                        ret = ret && !((JFXTextField) poliConCotasElements.get(10 * (col + 1) + row)).getText().trim().equals("");
                    }
                    break;

                case Text.FUNC_POLI_MULTI:

                    for(FUNCController p : polsControllers.values()){
                        ret = ret && polinomioCompleto(this.datosPolinomio);
                    }
                    break;

                case Text.FUNC_POR_RANGOS:
                    ret = porRangosCant > 0;
                    ret = ret && (polinomioCompleto(fueraRangoController.getDatosPolinomio()));

                    for (int i = 0; i < listaRangoFuncs.size(); i++) {
                        RangoFunc rangoFunc = listaRangoFuncs.get(i);
                        ret = ret && polinomioCompleto(rangoFunc.funcController.getDatosPolinomio());
                        ret = ret && !rangoFunc.getStart().getText().trim().trim().equals("")  ;
                        ret = ret && !rangoFunc.getEnd().getText().trim().equals("");

                    }

                    break;
            }
        }catch (NullPointerException e){
            return false;
        }
        return ret;
    }


}
