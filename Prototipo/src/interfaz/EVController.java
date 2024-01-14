/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EVController is part of MOP.
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

import com.jfoenix.controls.*;
import datatypes.DatosCorrida;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.*;
import logica.CorridaHandler;

import org.controlsfx.control.PopOver;
import tiempo.*;
import utilitarios.DirectoriosYArchivos;
import utilitarios.UtilStrings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class EVController extends GeneralidadesController {
    @FXML private JFXComboBox<String> comboBoxTipo;
    @FXML private JFXComboBox<String> comboBoxData;
    @FXML private Label labelTipoData;
    @FXML private GridPane gridPane;
    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;
    @FXML private JFXButton inputImport;
    @FXML private JFXButton inputExport;
    @FXML private TitledPane tiledPane;

    String titulo;


    private HashMap<String, ArrayList<String>> dataByEVType = new HashMap<>();
    private HashMap<Integer, Node> constElements = new HashMap<>();
    private HashMap<Integer, Node> porInstantesElements = new HashMap<>();
    private HashMap<Integer, ArrayList<Node>> porInstantesDynamicElements = new HashMap<>();
    private HashMap<Integer, ArrayList<Node>> porInstantesDynamicElementsLocal = new HashMap<>();
    private HashMap<Integer, Node> periodicaElements = new HashMap<>();
    private HashMap<Integer, ArrayList<Node>> periodicaDynamicElements = new HashMap<>();
    private HashMap<Integer, ArrayList<Node>> periodicaDynamicElementsLocal = new HashMap<>();
    private int periodicaRows = 2;
    private int porInstantesRows = 2;
    private int largoListaNum = 8;
    private int constListNumLargo = 4;
    private int constListParNumLargo = 4;
    private String tipoEV = Text.EV_CONST;
    private String tipoData;
    private ArrayList<String> vars;
    private boolean edicion;
    private Evolucion ev;
    private DatosCorrida datosCorrida;
    private LineaTiempo lt;
    private Node componenteAsociado;
    private boolean cerroSinAceptar;



    public EVController(DatosCorrida datosCorrida, String tipoData, ArrayList<String> vars,  Node componenteAsociado, String titulo) {
        edicion = false;
        this.datosCorrida = datosCorrida;
        lt = Manejador.getLineaTiempo(datosCorrida);
        this.tipoData = tipoData;
        this.vars = vars;
        this.componenteAsociado = componenteAsociado;
        this.titulo = titulo;
        habilitarComponenteAsociado(null);
    }

    public <T> EVController(ArrayList<Evolucion> ev, DatosCorrida datosCorrida, String tipoData, ArrayList<String> vars, Node componenteAsociado, String titulo) {
        edicion = true;
        this.ev = ev.get(0);
        this.datosCorrida = datosCorrida;
        lt = Manejador.getLineaTiempo(datosCorrida);
        this.tipoData = tipoData;
        this.vars = vars;
        this.componenteAsociado = componenteAsociado;
        this.titulo = titulo;
        habilitarComponenteAsociado(ev.get(0));
    }


    public Evolucion getEv() {     return ev;    }
    public void setEv(Evolucion ev) {     this.ev = ev;  }
    @FXML
    public void initialize(){
        cerroSinAceptar = true;
        tiledPane.setText(titulo);

        comboBoxTipo.getItems().addAll(Text.EV_CONST, Text.EV_POR_INSTANTES, Text.EV_PERIODICA);
        comboBoxTipo.setOnAction(actionEvent -> comboBoxTipoAction());
        comboBoxTipo.getSelectionModel().selectFirst();

        labelTipoData.setText(tipoData.toUpperCase());

        inputCancelar.setOnAction(actionEvent -> {
            cerroSinAceptar = true;
            inputCancelar.getScene().getWindow().hide();
        });

        inputAceptar.setOnAction(actionEvent -> {
            cerroSinAceptar = false;
            ArrayList<String> errores = controlDatosCompletos();
            if(errores.size() >0){
                setLabelMessageTemporal(errores.get(0), TipoInfo.FEEDBACK);
            }else {
                Evolucion evEditada = loadData();
                habilitarComponenteAsociado(evEditada);
                inputCancelar.getScene().getWindow().hide();
            }
        });

        inputImport.setOnAction(actionEvent -> importEV());
        inputExport.setOnAction(actionEvent -> exportEV());

        if(edicion){
            unloadData();
        }else{
            generateConst(tipoData);
        }

        agregarControlDeCambios();

    }

    /***
     * A los componentes de la interfaz se le agrega un listener para capturar si hubo cambios en los datos
     */
    private void agregarControlDeCambios() {

        for (Integer clave : constElements.keySet()) {
            if(constElements.get(clave) instanceof JFXTextField){
                ((JFXTextField) constElements.get(clave)).textProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }
                });

            }else if(constElements.get(clave) instanceof JFXComboBox){
                ((JFXComboBox) constElements.get(clave)).valueProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
            }
        }


/*        for (Integer clave : porInstantesElements.keySet()) {
            if(porInstantesElements.get(clave) instanceof JFXTextField){
                ((JFXTextField) porInstantesElements.get(clave)).textProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }
                });

            }else if(porInstantesElements.get(clave) instanceof JFXComboBox){
                ((JFXComboBox) porInstantesElements.get(clave)).valueProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
            }
        }

       */

    }


    /***
     *  Tratamiento datos tipo EV:
     *  Si el Text field del campo asociado esta habilitado se asume son de tipo constante -> SE USA LA INFORMACION DEL TEXT FIELD DEL CAMPO (se contruye new EV)
     *  sino se asume que la evolucion no es constante y se usa la informacion de la evolucion asociada al campo que esta en la lista ya cargada.
     */
    public static <T> Evolucion<T> loadEVsegunTipo(Node inputCampo, String tipoDato, HashMap<String, EVController> listaEv, String etiquetaEV){
        Evolucion<?> retorno = null;

        if(inputCampo.isDisable()){
            retorno = listaEv.get(etiquetaEV).loadData();
        }else {
            switch (tipoDato) {
                case Text.EV_NUM_INT:
                    String datoInt = ((JFXTextField)inputCampo).getText();
                    if(UtilStrings.esNumeroEntero(datoInt)) {
                        Integer valorInt = Integer.parseInt(datoInt);
                        retorno = new EvolucionConstante<>(valorInt, new SentidoTiempo(1));
                    }
                    break;
                case Text.EV_NUM_DOUBLE:
                    String datoDouble = ((JFXTextField)inputCampo).getText();
                    if(UtilStrings.esNumeroDouble(datoDouble)) {
                        Double valorDouble = Double.parseDouble(datoDouble);
                        retorno = new EvolucionConstante<>(valorDouble, new SentidoTiempo(1));
                    }
                    break;
                case Text.EV_VAR:
                    String datoLista = ((JFXComboBox<String>) inputCampo).getValue();
                    retorno = new EvolucionConstante<>(datoLista,new SentidoTiempo(1));
                    break;
                case Text.EV_BOOL:
                    Boolean datoBoolean = ((JFXCheckBox) inputCampo).isSelected();
                    retorno = new EvolucionConstante<>(datoBoolean,new SentidoTiempo(1));
                    break;



            }
        }
        return (Evolucion<T>)retorno;
    }

    private void  comboBoxTipoAction(){

        inputImport.setDisable(!comboBoxTipo.getValue().equalsIgnoreCase(Text.EV_POR_INSTANTES));
        inputExport.setDisable(!comboBoxTipo.getValue().equalsIgnoreCase(Text.EV_POR_INSTANTES));

        if(tipoEV.equalsIgnoreCase(Text.EV_POR_INSTANTES) && comboBoxTipo.getValue().equalsIgnoreCase(Text.EV_PERIODICA)){
            swapPorInstantesPorPeriodica();
            tipoEV = comboBoxTipo.getValue();
        }else if(tipoEV.equalsIgnoreCase(Text.EV_PERIODICA) && comboBoxTipo.getValue().equalsIgnoreCase(Text.EV_POR_INSTANTES)) {
            swapPeriodicaPorPorInstantes();
            tipoEV = comboBoxTipo.getValue();
        }else{
            tipoEV = comboBoxTipo.getValue();
            generate(tipoEV);
        }
    }

    private void generate(String tipoEV) {
        switch (tipoEV){
            case Text.EV_CONST:
                clearConstElements();
                clearPorInstantesElements();
                clearPeriodicaElements();
                generateConstContent(tipoData);
                break;
            case Text.EV_POR_INSTANTES:
                clearConstElements();
                clearPorInstantesElements();
                clearPeriodicaElements();
                generatePorInstantesContent();
                break;
            case Text.EV_PERIODICA:
                clearConstElements();
                clearPorInstantesElements();
                clearPeriodicaElements();
                generatePeriodicaContent();
                break;
        }
    }


    // CONST
    private void generateConstContent(String tipoData){
        if(tipoData != null) {
            generateConst(tipoData);
        }
    }

    private void generateConst(String tipoData){
        switch (tipoData){
            case Text.EV_NUM_INT:
            case Text.EV_NUM_DOUBLE:
                renderConstNumSinUnidad();
                break;
            case Text.EV_BOOL:
                renderConstBool();
                break;
            case Text.EV_CONST_FUNC:
                renderConstFunc();
                break;
            case Text.EV_VAR:
                renderConstVar();
                break;
            case Text.EV_CONST_LISTA_NUM:
                renderConstListaNum();
                break;
            case Text.EV_CONST_RANGOS:
                renderConstRangos();
                break;
            case Text.EV_CONST_DISCRETIZACION:
                renderConstDiscretizacion();
                break;
            case Text.EV_CONST_LISTA_PAR_NUM:
                renderConstListaParNum();
                break;
        }
    }

    private void renderConstNumSinUnidad(){
        //Etiqueta Valor
        Label valorLabel = new Label("Valor:");
        valorLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(valorLabel,0,3);
        constElements.put(0, valorLabel);

        //Input Valor
        JFXTextField valorTxtField = new JFXTextField();
        valorTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }
        });
        valorTxtField.setMinWidth(50);
        valorTxtField.setMaxWidth(50);
        gridPane.add(valorTxtField, 1, 3, 2, 1);
        constElements.put(1, valorTxtField);
    }

    private void renderConstNumConUnidad(){
        //Etiqueta Valor
        Label valorLabel = new Label("Valor:");
        valorLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(valorLabel,0,3);
        constElements.put(0, valorLabel);

        //Input Valor
        JFXTextField valorTxtField = new JFXTextField();
        valorTxtField.setMinWidth(50);
        valorTxtField.setMaxWidth(50);
        gridPane.add(valorTxtField, 1, 3, 2, 1);
//        GridPane.setHalignment(valorTxtField, HPos.CENTER);
//        GridPane.setMargin(valorTxtField, new Insets(9, 0, 0, 0));
        constElements.put(1, valorTxtField);

        //Etiqueta Unidad
        Label unidadLabel = new Label("Unidad:");
        unidadLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(unidadLabel,3,3);
        constElements.put(2, unidadLabel);

        //TODO: agregar valores
        //Input Unidad
        JFXComboBox undiadComboBox = new JFXComboBox();
        undiadComboBox.setMinWidth(90);
        undiadComboBox.setMaxWidth(90);
        gridPane.add(undiadComboBox, 4, 3, 2, 1);
//        GridPane.setHalignment(undiadComboBox, HPos.CENTER);
//        GridPane.setMargin(undiadComboBox, new Insets(9, 0, 0, 0));
        constElements.put(3, undiadComboBox);
    }

    private void renderConstBool(){
        ToggleGroup conBoolGroup = new ToggleGroup();

        //TRUE Input
        JFXRadioButton trueRadioBtn = new JFXRadioButton("True");
        trueRadioBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {    if(ev != null){  ev.setEditoValores(true);   }   });
        gridPane.add(trueRadioBtn,0,3, 2,1);
        constElements.put(0, trueRadioBtn);
        trueRadioBtn.setSelected(true);
        trueRadioBtn.setToggleGroup(conBoolGroup);
        GridPane.setMargin(trueRadioBtn, new Insets(0, 0, 0, 10));

        //FALSE Input
        JFXRadioButton falseRadioBtn = new JFXRadioButton("False");
        gridPane.add(falseRadioBtn,2,3);
        constElements.put(1, falseRadioBtn);
        falseRadioBtn.setToggleGroup(conBoolGroup);
    }

    //TODO
    private void renderConstFunc(){}

    private void renderConstVar(){
        //Etiqueta Var
        Label varLabel = new Label("Var:");
        varLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(varLabel, 0, 3);
        constElements.put(0, varLabel);

        //Input Var
        JFXComboBox<String> varComboBox = new JFXComboBox<>();
        varComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        varComboBox.setMinWidth(120);
        varComboBox.setMaxWidth(120);
        varComboBox.getItems().addAll(vars);
        gridPane.add(varComboBox, 1, 3);
        constElements.put(1, varComboBox);
    }

    /***
     * Renderiza la ventana para ev constante de una lista de numeros
     */
    private void renderConstListaNum(){
    	long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        //Etiqueta Largo
        Label largoLabel = new Label("Largo:");
        largoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(largoLabel, 0, 3);
        constElements.put(0, largoLabel);

        //Input Largo
        JFXTextField inputLargo = new JFXTextField();
        inputLargo.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   } });
        if(ev != null) { largoListaNum = ((ArrayList)ev.getValor(instanteActual)).size(); }
        inputLargo.setText(String.valueOf(largoListaNum));
        inputLargo.setMinWidth(50);
        inputLargo.setMaxWidth(50);
        gridPane.add(inputLargo, 1, 3, 2, 1);
        constElements.put(1, inputLargo);
        if(ev!= null) largoListaNum = ((ArrayList)ev.getValor(instanteActual)).size();
        inputLargo.setOnAction(actionEvent -> renderListNum(largoListaNum, Integer.parseInt(inputLargo.getText())));
        Tooltip t = new Tooltip("Debe presionar enter para actualizar cantidad de componentes");
        t.setFont(new Font(12.0));
        inputLargo.setTooltip(t);
        renderListNum(0, largoListaNum);

    }

    private void renderListNum(int start, int largo) {
        for(int i=start;i<largo;i++){
            JFXTextField inputValor = new JFXTextField();
            inputValor.setMinWidth(50);
            inputValor.setMaxWidth(50);
            int col = i % 6;
            int row = (i / 6) + 5;
            gridPane.add(inputValor, col, row);
            GridPane.setMargin(inputValor, new Insets(9, 0, 0, 0));
            constElements.put(10*col+row, inputValor);
        }

//        for(int i=largo; i<=constListNumLargo; i++){
        for(int i=largo; i<=largoListaNum; i++){
            int col = i % 6;
            int row = (i / 6) + 5;
            gridPane.getChildren().remove(constElements.get(10*col+row));
            constElements.remove(10*col+row);
        }
//        constListNumLargo = largo;
        largoListaNum = largo;
    }

    private void renderConstListaParNum(){
        //Etiqueta Largo
        Label largoLabel = new Label("Largo:");
        largoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(largoLabel, 0, 3);
        constElements.put(0, largoLabel);

        //Input Largo
        JFXTextField inputLargo = new JFXTextField();
        inputLargo.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        inputLargo.setText(String.valueOf(constListParNumLargo));
        inputLargo.setMinWidth(50);
        inputLargo.setMaxWidth(50);
        gridPane.add(inputLargo, 1, 3, 2, 1);
        constElements.put(1, inputLargo);
        inputLargo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                renderListaParNum(constListParNumLargo, Integer.parseInt(inputLargo.getText()));
            }
        });

        renderListaParNum(0, constListParNumLargo);

    }

    private void renderListaParNum(int start, int largo) {
        for(int i=start;i<largo;i++){
            JFXTextField inputValor1 = new JFXTextField();
            inputValor1.setMinWidth(50);
            inputValor1.setMaxWidth(50);
            JFXTextField inputValor2 = new JFXTextField();
            inputValor2.setMinWidth(50);
            inputValor2.setMaxWidth(50);
            gridPane.add(inputValor1, 1, 4+i);
            gridPane.add(inputValor2, 2, 4+i);
            GridPane.setMargin(inputValor1, new Insets(9, 0, 0, 0));
            GridPane.setMargin(inputValor2, new Insets(9, 0, 0, 0));
            constElements.put(110+4+i, inputValor1);
            constElements.put(120+4+i, inputValor2);
        }

        for(int i=largo; i<=constListParNumLargo; i++){
            gridPane.getChildren().remove(constElements.get(110+4+i));
            gridPane.getChildren().remove(constElements.get(120+4+i));
            constElements.remove(110+4+i);
            constElements.remove(120+4+i);
        }
        constListParNumLargo = largo;
    }

    //TODO
    private void renderConstRangos(){}

    private void renderConstDiscretizacion(){
        //Etiqueta tipo
        Label tipoLabel = new Label("Tipo:");
        tipoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(tipoLabel, 0, 3);
        constElements.put(0, tipoLabel);

        //TODO: agregar valores
        //Input tipo
        JFXComboBox inputTipo = new JFXComboBox();
        inputTipo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        inputTipo.setMinWidth(90);
        inputTipo.setMaxWidth(90);
        gridPane.add(inputTipo, 1, 3,2,1);
        constElements.put(1, inputTipo);

        //Etiqueta min
        Label minLabel = new Label("Mínimo:");
        minLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(minLabel, 0, 4);
        constElements.put(2, minLabel);

        //Input min
        JFXTextField inputMin = new JFXTextField();
        inputMin.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        inputMin.setMinWidth(50);
        inputMin.setMaxWidth(50);
        gridPane.add(inputMin, 1, 4, 2, 1);
        GridPane.setHalignment(inputMin, HPos.CENTER);
        constElements.put(3, inputMin);

        //Etiqueta max
        Label maxLabel = new Label("Máximo:");
        maxLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(maxLabel, 3, 4);
        constElements.put(4, maxLabel);

        //Input max
        JFXTextField inputMax = new JFXTextField();
        inputMax.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        inputMax.setMinWidth(50);
        inputMax.setMaxWidth(50);
        gridPane.add(inputMax, 4, 4, 2, 1);
        GridPane.setHalignment(inputMax, HPos.CENTER);
        constElements.put(5, inputMax);

        //Etiqueta cantPuntos
        Label cantPuntosLabel = new Label("Cantidad de Puntos:");
        cantPuntosLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(cantPuntosLabel, 0, 5, 3,1);
        constElements.put(6, cantPuntosLabel);

        //Input cantPuntos
        JFXTextField inputCantPuntos = new JFXTextField();
        inputCantPuntos.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        inputCantPuntos.setMinWidth(50);
        inputCantPuntos.setMaxWidth(50);
        gridPane.add(inputCantPuntos, 2, 5, 2, 1);
        GridPane.setHalignment(inputCantPuntos, HPos.CENTER);
        constElements.put(7, inputCantPuntos);
    }

    private void clearConstElements(){

        for(Node n: constElements.values()){
            gridPane.getChildren().remove(n);
        }
        constElements.clear();
    }

    // POR INSTANTES
    private void generatePorInstantesContent(){
        //STATIC
        JFXTextField cantInstantesTxtField = renderPorInstantesStaticElements();

        //DYNAMIC
        renderPorInstantesDynamicElements(0, Integer.parseInt(cantInstantesTxtField.getText()), null);
    }

    private JFXTextField renderPorInstantesStaticElements(){
        //Etiqueta Instantes
        Label cantInstantesLabel = new Label("Instantes:");
        cantInstantesLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(cantInstantesLabel, 0, 3, 2,1);
        porInstantesElements.put(0, cantInstantesLabel);

        //Input Instantes
        JFXTextField cantInstantesTxtField = new JFXTextField();
        cantInstantesTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        cantInstantesTxtField.setText(String.valueOf(porInstantesRows));
        cantInstantesTxtField.setMinWidth(50);
        cantInstantesTxtField.setMaxWidth(50);
        gridPane.add(cantInstantesTxtField, 1, 3, 2, 1);
        GridPane.setHalignment(cantInstantesTxtField, HPos.CENTER);
        cantInstantesTxtField.setOnAction(actionEvent -> renderPorInstantesDynamicElements(porInstantesRows, Integer.parseInt(cantInstantesTxtField.getText()), null));
        cantInstantesTxtField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if( UtilStrings.esNumeroEntero(cantInstantesTxtField.getText())) {
                    int c = Integer.parseInt(cantInstantesTxtField.getText());
                    renderPorInstantesDynamicElements(porInstantesRows, Integer.parseInt(cantInstantesTxtField.getText()), null);
                }
            }
        });
        porInstantesElements.put(1, cantInstantesTxtField);

        //Etiqueta Valor
        Label valorLabel = new Label("Valor:");
        valorLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(valorLabel, 3, 4, 2,1);
        GridPane.setHalignment(valorLabel, HPos.CENTER);
        porInstantesElements.put(3, valorLabel);

        //Largo para los tipos de datos que lo necesitan
        if(tipoData.equalsIgnoreCase(Text.EV_LISTA_NUM)){
            //Etiqueta Largo
            Label largoLabel = new Label("Largo:");
            largoLabel.setStyle("-fx-font-size: 15px;");
            gridPane.add(largoLabel, 3, 3);
            porInstantesElements.put(4, largoLabel);

            //Input Largo
            JFXTextField largoTxtField = new JFXTextField();
            largoTxtField.setText(String.valueOf(largoListaNum));
            gridPane.add(largoTxtField, 4, 3);
//            largoTxtField.setOnAction(actionEvent -> renderPorInstantesDynamicElements(porInstantesRows, Integer.parseInt(largoTxtField.getText()), null));
            porInstantesElements.put(5, largoTxtField);
        }

        return cantInstantesTxtField;
    }

    private void renderPorInstantesDynamicElements(int start, int cantInstancias, HashMap<Integer, ArrayList<Node>> rows){
        for(int i=start+1;i<=cantInstancias;i++) {
            JFXDatePicker rangoIni = new JFXDatePicker();
            rangoIni.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(ev != null){  ev.setEditoValores(true);   }   });
            rangoIni.setDefaultColor(Paint.valueOf("#27864d"));
            rangoIni.setShowWeekNumbers(true);
            rangoIni.setMinWidth(120);
            rangoIni.setMaxWidth(120);
            gridPane.add(rangoIni, 0, 4+i, 3, 1);
            GridPane.setHalignment(rangoIni, HPos.CENTER);
            GridPane.setMargin(rangoIni, new Insets(9, 0, 0, 0));
            if(rows != null){
                rangoIni.setValue(((JFXDatePicker)rows.get(i).get(0)).getValue());
            }
            if(tipoData.equalsIgnoreCase(Text.EV_NUM_INT) || tipoData.equalsIgnoreCase(Text.EV_NUM_DOUBLE)){
                JFXTextField valorTxtField = new JFXTextField();
                valorTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
                valorTxtField.setMinWidth(50);
                valorTxtField.setMaxWidth(50);
                gridPane.add(valorTxtField, 3, 4 + i, 2, 1);
                GridPane.setHalignment(valorTxtField, HPos.CENTER);
                GridPane.setMargin(valorTxtField, new Insets(9, 0, 0, 0));
                if(rows != null){
                    valorTxtField.setText(((JFXTextField)rows.get(i).get(1)).getText());
                }
                else if(porInstantesDynamicElementsLocal != null && porInstantesDynamicElementsLocal.size() >= i){
                    rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                    valorTxtField.setText(((JFXTextField)porInstantesDynamicElementsLocal.get(i).get(1)).getText());
                }

                porInstantesDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,valorTxtField)));
            }else if(tipoData.equalsIgnoreCase(Text.EV_VAR)){
                JFXComboBox<String> valorComboBox = new JFXComboBox<>();
                valorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
                valorComboBox.setMinWidth(120);
                valorComboBox.setMaxWidth(120);
                valorComboBox.getItems().addAll(vars);
                gridPane.add(valorComboBox, 3, 4 + i, 2, 1);
                GridPane.setMargin(valorComboBox, new Insets(9, 0, 0, 0));
                if(rows != null){
                    valorComboBox.getSelectionModel().select(((JFXComboBox<String>)rows.get(i).get(1)).getValue());
                }else if(porInstantesDynamicElementsLocal != null && porInstantesDynamicElementsLocal.size() >= i){
                    rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                    valorComboBox.getSelectionModel().select(((JFXComboBox<String>)porInstantesDynamicElementsLocal.get(i).get(1)).getValue());
                }
                porInstantesDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,valorComboBox)));
            }else if(tipoData.equalsIgnoreCase(Text.EV_BOOL)){
                JFXCheckBox valorCheckBox = new JFXCheckBox();
                valorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
                gridPane.add(valorCheckBox, 3, 4 + i, 2, 1);
                GridPane.setHalignment(valorCheckBox, HPos.CENTER);
                GridPane.setMargin(valorCheckBox, new Insets(9, 0, 0, 0));
                if(rows != null){
                    valorCheckBox.setSelected(((JFXCheckBox)rows.get(i).get(1)).isSelected());
                }else if(porInstantesDynamicElementsLocal != null && porInstantesDynamicElementsLocal.size() >= i){
                    rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                    valorCheckBox.setSelected(((JFXCheckBox)porInstantesDynamicElementsLocal.get(i).get(1)).isSelected());
                }
                porInstantesDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,valorCheckBox)));
            }else if(tipoData.equalsIgnoreCase(Text.EV_LISTA_NUM)){
                ArrayList<Node> row = new ArrayList<>();
                row.add(rangoIni);
                for(int j=0; j<largoListaNum; j++){
                    JFXTextField valorTxtField = new JFXTextField();
                    valorTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if(ev != null){  ev.setEditoValores(true);   }   });
                    valorTxtField.setMinWidth(50);
                    valorTxtField.setMaxWidth(50);
                    gridPane.add(valorTxtField, 3 + j, 4 + i);
                    GridPane.setMargin(valorTxtField, new Insets(9, 0, 0, 0));
                    GridPane.setMargin(valorTxtField, new Insets(9, 1, 0, 1));
                    if(rows != null){
                        valorTxtField.setText(((JFXTextField)rows.get(i).get(1+j)).getText());
                    }else if(porInstantesDynamicElementsLocal != null && porInstantesDynamicElementsLocal.size() >= i){
                        rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                        valorTxtField.setText(((JFXTextField)porInstantesDynamicElementsLocal.get(i).get(1+j)).getText());
                    }
                    row.add(valorTxtField);
                }
                porInstantesDynamicElements.put(i, row);
            }
        }
        if(rows == null) {
            for (int i = cantInstancias + 1; i <= porInstantesRows; i++) {
                gridPane.getChildren().removeAll(porInstantesDynamicElements.get(i));
                porInstantesDynamicElements.remove(i);
            }
            porInstantesRows = cantInstancias;
        }
    }

    private void clearPorInstantesElements(){
        for(Node n: porInstantesElements.values()){
            gridPane.getChildren().remove(n);
        }
        porInstantesElements.clear();
        for(ArrayList<Node> l: porInstantesDynamicElements.values()){
            gridPane.getChildren().removeAll(l);
        }
        porInstantesDynamicElements.clear();
    }

    // PERIODICA
    private void generatePeriodicaContent(){
        //STATIC
        JFXTextField cantInstantesTxtField = renderPeriodicaStaticElements();

        //DYNAMIC
        renderPeriodicaDynamicElements(0, Integer.parseInt(cantInstantesTxtField.getText()), null);
    }

    private JFXTextField renderPeriodicaStaticElements(){
        //Etiqueta Periodo
        Label periodoLabel = new Label("Período:");
        periodoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(periodoLabel, 0, 3);
        periodicaElements.put(0, periodoLabel);

        //Input Periodo
        JFXComboBox<String> periodoComboBox = new JFXComboBox<>();
        periodoComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        periodoComboBox.setMinWidth(90);
        periodoComboBox.setMaxWidth(90);
        periodoComboBox.getItems().addAll(Text.PERIODO_ANIO, Text.PERIODO_MES, Text.PERIODO_SEMANA, Text.PERIODO_DIA);
        gridPane.add(periodoComboBox, 1, 3);
        periodicaElements.put(1, periodoComboBox);

        //Etiqueta CantPeriodo
        Label cantPeriodoLabel = new Label("Cantidad Período:");
        cantPeriodoLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(cantPeriodoLabel, 3, 3);
        periodicaElements.put(2, cantPeriodoLabel);

        //Input CantPeriodo
        JFXTextField inputCantPeriodo = new JFXTextField();
        inputCantPeriodo.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        inputCantPeriodo.setMinWidth(50);
        inputCantPeriodo.setMaxWidth(50);
        gridPane.add(inputCantPeriodo, 4, 3, 2, 1);
        periodicaElements.put(3, inputCantPeriodo);

        //Etiqueta Instantes
        Label cantInstantesLabel = new Label("Instantes:");
        cantInstantesLabel.setStyle("-fx-font-size: 15px;");
        gridPane.add(cantInstantesLabel, 0, 4, 2,1);
        periodicaElements.put(4, cantInstantesLabel);

        //Input Instantes
        JFXTextField cantInstantesTxtField = new JFXTextField();
        cantInstantesTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(ev != null){  ev.setEditoValores(true);   }   });
        cantInstantesTxtField.setText(String.valueOf(periodicaRows));
        gridPane.add(cantInstantesTxtField, 1, 4, 2, 1);
        cantInstantesTxtField.setMinWidth(50);
        cantInstantesTxtField.setMaxWidth(50);
        GridPane.setHalignment(cantInstantesTxtField, HPos.CENTER);
        cantInstantesTxtField.setOnAction(actionEvent -> renderPeriodicaDynamicElements(periodicaRows, Integer.parseInt(cantInstantesTxtField.getText()), null));
        cantInstantesTxtField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {

                if( UtilStrings.esNumeroEntero(cantInstantesTxtField.getText())) {
                    int c = Integer.parseInt(cantInstantesTxtField.getText());

                    renderPeriodicaDynamicElements(periodicaRows, Integer.parseInt(cantInstantesTxtField.getText()), null);
                }
            }
        });
        Tooltip t = new Tooltip("Debe presionar enter para actualizar cantidad de componentes");
        t.setFont(new Font(12.0));
        cantInstantesTxtField.setTooltip(t);
        periodicaElements.put(5, cantInstantesTxtField);

        //Largo para los tipos de datos que lo necesitan
        if(tipoData.equalsIgnoreCase(Text.EV_LISTA_NUM)){
            //Etiqueta Largo
            Label largoLabel = new Label("Largo:");
            largoLabel.setStyle("-fx-font-size: 15px;");
            gridPane.add(largoLabel, 3, 4);
            periodicaElements.put(4, largoLabel);

            //Input Largo
            JFXTextField largoTxtField = new JFXTextField();
            largoTxtField.setText(String.valueOf(largoListaNum));
            gridPane.add(largoTxtField, 4, 4);
//            largoTxtField.setOnAction(actionEvent -> renderPeriodicaDynamicElements(periodicaRows, Integer.parseInt(largoTxtField.getText()), null));
            periodicaElements.put(5, largoTxtField);
        }

        return cantInstantesTxtField;
    }

    private void renderPeriodicaDynamicElements(int start, int cantInstancias, HashMap<Integer, ArrayList<Node>> rows){
//        for(int i=porInstantesRows+1;i<=cantInstancias;i++) {
        for(int i=start+1;i<=cantInstancias;i++) {
            JFXDatePicker rangoIni = new JFXDatePicker();
            rangoIni.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(ev != null){  ev.setEditoValores(true);   }   });
            rangoIni.setDefaultColor(Paint.valueOf("#27864d"));
            rangoIni.setShowWeekNumbers(true);
            rangoIni.setMinWidth(120);
            rangoIni.setMaxWidth(120);
            gridPane.add(rangoIni, 0, 5 + i, 3, 1);
            GridPane.setHalignment(rangoIni, HPos.CENTER);
            GridPane.setMargin(rangoIni, new Insets(9, 0, 0, 0));
            if(rows != null){
                rangoIni.setValue(((JFXDatePicker)rows.get(i).get(0)).getValue());
            }

//            if(periodicaTipoData.equalsIgnoreCase(Text.EV_PERIODICA_NUM)) {
            if(tipoData.equalsIgnoreCase(Text.EV_NUM_INT) || tipoData.equalsIgnoreCase(Text.EV_NUM_DOUBLE)) {
                JFXTextField valorTxtField = new JFXTextField();
                valorTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
                valorTxtField.setMinWidth(50);
                valorTxtField.setMaxWidth(50);
                gridPane.add(valorTxtField, 3, 5 + i, 2, 1);
                GridPane.setHalignment(valorTxtField, HPos.CENTER);
                GridPane.setMargin(valorTxtField, new Insets(9, 0, 0, 0));
//                porInstantesDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,rangoFin,valorTxtField)));

                if(rows != null){
                    valorTxtField.setText(((JFXTextField)rows.get(i).get(1)).getText());
                }
                else if(periodicaDynamicElementsLocal != null && periodicaDynamicElementsLocal.size() >= i){
                    rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                    valorTxtField.setText(((JFXTextField)porInstantesDynamicElementsLocal.get(i).get(1)).getText());
                }

                periodicaDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,valorTxtField)));
//            }else if(periodicaTipoData.equalsIgnoreCase(Text.EV_PERIODICA_VAR)){
            }else if(tipoData.equalsIgnoreCase(Text.EV_VAR)){
                JFXComboBox<String> valorComboBox = new JFXComboBox<>();
                valorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
                valorComboBox.setMinWidth(120);
                valorComboBox.setMaxWidth(120);
                valorComboBox.getItems().addAll(vars);
                gridPane.add(valorComboBox, 3, 5 + i, 2, 1);
//                GridPane.setHalignment(valorComboBox, HPos.CENTER);
                GridPane.setMargin(valorComboBox, new Insets(9, 0, 0, 0));
//                periodicaDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,rangoFin,valorComboBox)));

                if(rows != null){
                    valorComboBox.getSelectionModel().select(((JFXComboBox<String>)rows.get(i).get(1)).getValue());
                }else if(periodicaDynamicElementsLocal != null && periodicaDynamicElementsLocal.size() >= i){
                    rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                    valorComboBox.getSelectionModel().select(((JFXComboBox<String>)porInstantesDynamicElementsLocal.get(i).get(1)).getValue());
                }

                periodicaDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,valorComboBox)));
//            }else if(periodicaTipoData.equalsIgnoreCase(Text.EV_PERIODICA_BOOL)){
            }else if(tipoData.equalsIgnoreCase(Text.EV_BOOL)){
                JFXCheckBox valorCheckBox = new JFXCheckBox();
                valorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(ev != null){  ev.setEditoValores(true);   }   });
                gridPane.add(valorCheckBox, 3, 5 + i, 2, 1);
                GridPane.setHalignment(valorCheckBox, HPos.CENTER);
                GridPane.setMargin(valorCheckBox, new Insets(9, 0, 0, 0));
//                porInstantesDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,rangoFin,valorCheckBox)));

                if(rows != null){
                    valorCheckBox.setSelected(((JFXCheckBox)rows.get(i).get(1)).isSelected());
                }else if(periodicaDynamicElementsLocal != null && periodicaDynamicElementsLocal.size() >= i){
                    rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                    valorCheckBox.setSelected(((JFXCheckBox)porInstantesDynamicElementsLocal.get(i).get(1)).isSelected());
                }

                periodicaDynamicElements.put(i, new ArrayList<>(Arrays.asList(rangoIni,valorCheckBox)));
            }else if(tipoData.equalsIgnoreCase(Text.EV_LISTA_NUM)){
                ArrayList<Node> row = new ArrayList<>();
                row.add(rangoIni);
                for(int j=0; j<largoListaNum; j++){
                    JFXTextField valorTxtField = new JFXTextField();
                    valorTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if(ev != null){  ev.setEditoValores(true);   }   });
                    valorTxtField.setMinWidth(50);
                    valorTxtField.setMaxWidth(50);
                    gridPane.add(valorTxtField, 3 + j, 5 + i);
//                    GridPane.setHalignment(valorTxtField, HPos.CENTER);
                    GridPane.setMargin(valorTxtField, new Insets(9, 0, 0, 0));
                    GridPane.setMargin(valorTxtField, new Insets(9, 1, 0, 1));

                    if(rows != null){
                        valorTxtField.setText(((JFXTextField)rows.get(i).get(1+j)).getText());
                    }else if(periodicaDynamicElementsLocal != null && periodicaDynamicElementsLocal.size() >= i){
                        rangoIni.setValue(((JFXDatePicker)porInstantesDynamicElementsLocal.get(i).get(0)).getValue());
                        valorTxtField.setText(((JFXTextField)porInstantesDynamicElementsLocal.get(i).get(1+j)).getText());
                    }

                    row.add(valorTxtField);
                }
                periodicaDynamicElements.put(i, row);
            }
        }
        if(rows == null){
            for(int i=cantInstancias+1; i<=periodicaRows; i++){
                gridPane.getChildren().removeAll(periodicaDynamicElements.get(i));
                periodicaDynamicElements.remove(i);
            }
            periodicaRows = cantInstancias;
        }
    }

    private void clearPeriodicaElements(){
        for(Node n: periodicaElements.values()){
            gridPane.getChildren().remove(n);
        }
        periodicaElements.clear();
        for(ArrayList<Node> l: periodicaDynamicElements.values()){
            gridPane.getChildren().removeAll(l);
        }
        periodicaDynamicElements.clear();
    }

    private void swapPorInstantesPorPeriodica(){
        periodicaRows = porInstantesDynamicElements.size();
        renderPeriodicaStaticElements();
        renderPeriodicaDynamicElements(0, porInstantesDynamicElements.size(), porInstantesDynamicElements);
        clearPorInstantesElements();
    }

    private void swapPeriodicaPorPorInstantes(){
        porInstantesRows = periodicaDynamicElements.size();
        renderPorInstantesStaticElements();
        renderPorInstantesDynamicElements(0, periodicaDynamicElements.size(), periodicaDynamicElements);
        clearPeriodicaElements();
    }



    private String dameStringPeriodo(int periodo) {
        if (periodo == Calendar.YEAR)
            return Text.PERIODO_ANIO;
        if (periodo == Calendar.MONTH)
            return Text.PERIODO_MES;
        if (periodo == Calendar.WEEK_OF_YEAR)
            return Text.PERIODO_SEMANA;
        if (periodo == Calendar.DAY_OF_YEAR)
            return Text.PERIODO_DIA;
        return null;
    }

    private int intPeriodo(String periodo){
        switch(periodo){
            case Text.PERIODO_ANIO:
                return Calendar.YEAR;
            case Text.PERIODO_MES:
                return Calendar.MONTH;
            case Text.PERIODO_SEMANA:
                return Calendar.WEEK_OF_YEAR;
            case Text.PERIODO_DIA:
                return Calendar.DAY_OF_YEAR;
        }
        return -1;
    }


    public void setEVBtnAction(JFXButton btn, EVController controller){
        try {
            FXMLLoader loader = new FXMLLoader(EVController.class.getResource("EV.fxml"));
            loader.setController(controller);
            AnchorPane newLoadedPane = loader.load();

            PopOver popOver = new PopOver(newLoadedPane);

            btn.setOnAction(actionEvent -> {
                if(!componenteAsociado.isDisable()) {
                    switch (tipoData){
                        case Text.EV_NUM_INT:
                        case Text.EV_NUM_DOUBLE:
                            //((JFXTextField)constElements.get(1)).setText(((JFXTextField)componenteAsociado).getText());
                            JFXTextField tf = (JFXTextField)constElements.get(1);
                            tf.setText(((JFXTextField)componenteAsociado).getText());
                            break;
                        case Text.EV_BOOL:
                            if(constElements.size()>0) {
                                boolean estaSeleccionado = ((JFXCheckBox) componenteAsociado).isSelected();
                                ((JFXRadioButton) constElements.get(0)).setSelected(estaSeleccionado);
                                ((JFXRadioButton) constElements.get(1)).setSelected(!estaSeleccionado);
                            }
                            break;
                        case Text.EV_VAR:
                            if( ((JFXComboBox) componenteAsociado).getValue() != null) {
                                String valorCBox = ((JFXComboBox) componenteAsociado).getValue().toString();
                                ((JFXComboBox) constElements.get(1)).setValue(valorCBox);
                            }
                            break;
                        case Text.EV_LISTA_NUM:
                            //No se muestra componente asociado para evoluciones de lista de numeros
                            break;
                    }
                }
                popOver.setOnHidden(hiddenEvent -> {
                    if(cerroSinAceptar){
                        //Se saca de la interfaz lo que cambio (sino cuando vuelve a abrir permanecen los cambios aunque haya cerrado sin aceptar)
                        unloadData();
                    }
                });
                popOver.show(btn);

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void importEV() {
        List<List<String>> records = new ArrayList<>();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(gridPane.getScene().getWindow());

        // Ejemplo formato:
/*
        01 01 2022,6

        01 01 2023,7

        01 01 2024,8

        01 01 2039,4

        01 01 2044,2

        01 01 2045,10

         */
        try(BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            while((line = br.readLine()) != null){
                String[] values = line.split(Text.EV_IMP_EXP_SEPARADOR);
                records.add(Arrays.asList(values));
            }
            System.out.println("TIPO_DATA: " + tipoData);
            System.out.println(records);

            SentidoTiempo st = new SentidoTiempo(1);
            switch(tipoData){
                case Text.EV_NUM_INT:
                    Hashtable<Long, Integer> valorizadorInt = new Hashtable<>();
                    for(List<String> record : records){
                        GregorianCalendar calendar = new GregorianCalendar();
                        String[] stringFecha = record.get(0).split(" ");
                        calendar.set(Integer.parseInt(stringFecha[2]), Integer.parseInt(stringFecha[1]) + 1, Integer.parseInt(stringFecha[0]));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorInt.put(instante, Integer.parseInt(record.get(1)));
                    }
                    ev = new EvolucionPorInstantes<>(valorizadorInt, Integer.parseInt(records.get(0).get(1)), st);
                    unloadData();
                    break;
                case Text.EV_NUM_DOUBLE:
                    Hashtable<Long, Double> valorizadorDouble = new Hashtable<>();
                    for(List<String> record : records){
                        GregorianCalendar calendar = new GregorianCalendar();
                        String[] stringFecha = record.get(0).split(" ");
                        calendar.set(Integer.parseInt(stringFecha[2]), Integer.parseInt(stringFecha[1]) + 1, Integer.parseInt(stringFecha[0]));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorDouble.put(instante, Double.parseDouble(record.get(1)));
                    }
                    ev = new EvolucionPorInstantes<>(valorizadorDouble, Double.parseDouble(records.get(0).get(1)), st);
                    unloadData();
                    break;
                case Text.EV_BOOL:
                    Hashtable<Long, Boolean> valorizadorBool = new Hashtable<>();
                    for(List<String> record : records){
                        GregorianCalendar calendar = new GregorianCalendar();
                        String[] stringFecha = record.get(0).split(" ");
                        calendar.set(Integer.parseInt(stringFecha[2]), Integer.parseInt(stringFecha[1]) + 1, Integer.parseInt(stringFecha[0]));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorBool.put(instante, record.get(1).equalsIgnoreCase("true"));
                    }
                    ev = new EvolucionPorInstantes<>(valorizadorBool, records.get(0).get(1).equalsIgnoreCase("true"), st);
                    unloadData();
                    break;
                case Text.EV_VAR:
                    Hashtable<Long, String> valorizadorVar = new Hashtable<>();
                    for(List<String> record : records){
                        GregorianCalendar calendar = new GregorianCalendar();
                        String[] stringFecha = record.get(0).split(" ");
                        calendar.set(Integer.parseInt(stringFecha[2]), Integer.parseInt(stringFecha[1]) + 1, Integer.parseInt(stringFecha[0]));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorVar.put(instante, record.get(1));
                    }
                    ev = new EvolucionPorInstantes<>(valorizadorVar, records.get(0).get(1), st);
                    unloadData();
                    break;
                case Text.EV_LISTA_NUM:
                    ArrayList<Double> valInicial = new ArrayList<>();
                    Hashtable<Long, ArrayList<Double>> valorizadorListaNum = new Hashtable<>();
                    for(List<String> record : records){
                        GregorianCalendar calendar = new GregorianCalendar();
                        String[] stringFecha = record.get(0).split(" ");
                        calendar.set(Integer.parseInt(stringFecha[2]), Integer.parseInt(stringFecha[1]) + 1, Integer.parseInt(stringFecha[0]));
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Long instante = lt.dameInstante(calendar);
                        ArrayList<Double> rowVals = new ArrayList<>();
                        for(String s : record.get(1).split(" ")){
                            rowVals.add(Double.parseDouble(s));
                            if(valInicial.isEmpty()){
                                valInicial.add(Double.parseDouble(s));
                            }
                        }
                        valorizadorListaNum.put(instante, rowVals);
                    }
                    ev = new EvolucionPorInstantes<>(valorizadorListaNum, valInicial, st);
                    unloadData();
                    break;
            }

        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void exportEV() {

        // Ejemplo formato:
/*
        01 01 2022,6

        01 01 2023,7

        01 01 2024,8

        01 01 2039,4

        01 01 2044,2

        01 01 2045,10

         */

        String dirArch;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar un directorio");
        java.io.File selectedDirectory = directoryChooser.showDialog(inputExport.getScene().getWindow());
        dirArch = selectedDirectory.getAbsolutePath() + "\\Evolucion.txt";
        if (selectedDirectory != null) {
            System.out.println("Directorio seleccionado: " + selectedDirectory.getAbsolutePath());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
            if (ev instanceof EvolucionPorInstantes) {
                String salida = "";
                ArrayList<Long> instantesOrdenados = ((EvolucionPorInstantes) ev).getInstantesOrdenados();

                switch (tipoData) {
                    case Text.EV_NUM_INT:
                        Hashtable<Long, Integer> valorizadorInt = (Hashtable<Long, Integer>) ((EvolucionPorInstantes<?>) ev).getValorizador();
                        for (Long inst : instantesOrdenados) {

                            String fechaFormateada = dateFormat.format(lt.dameTiempoParaEscibirEvolucion(inst).getTime());
                            salida += fechaFormateada + Text.EV_IMP_EXP_SEPARADOR + " " + valorizadorInt.get(inst).toString() + "\n";

                        }
                        break;
                    case Text.EV_NUM_DOUBLE:
                        Hashtable<Long, Double> valorizadorDouble = (Hashtable<Long, Double>) ((EvolucionPorInstantes<?>) ev).getValorizador();
                        for (Long inst : instantesOrdenados) {
                            String fechaFormateada = dateFormat.format(lt.dameTiempoParaEscibirEvolucion(inst).getTime());
                            salida += fechaFormateada + Text.EV_IMP_EXP_SEPARADOR + " ";
                            salida += valorizadorDouble.get(inst).toString() + "\n";

                        }
                        break;
                    case Text.EV_BOOL:
                        Hashtable<Long, Boolean> valorizadorBool = (Hashtable<Long, Boolean>) ((EvolucionPorInstantes<?>) ev).getValorizador();
                        for (Long inst : instantesOrdenados) {
                            String fechaFormateada = dateFormat.format(lt.dameTiempoParaEscibirEvolucion(inst).getTime());
                            salida += fechaFormateada + Text.EV_IMP_EXP_SEPARADOR + " " + valorizadorBool.get(inst).toString() + "\n";

                        }
                        break;
                    case Text.EV_VAR:
                        Hashtable<Long, String> valorizadorVar = (Hashtable<Long, String>) ((EvolucionPorInstantes<?>) ev).getValorizador();
                        for (Long inst : instantesOrdenados) {
                            String fechaFormateada = dateFormat.format(lt.dameTiempoParaEscibirEvolucion(inst).getTime());
                            salida += fechaFormateada + Text.EV_IMP_EXP_SEPARADOR + " " + valorizadorVar.get(inst).toString() + "\n";

                        }
                        break;
                    case Text.EV_LISTA_NUM:
                        ArrayList<Double> valInicial = new ArrayList<>();
                        Hashtable<Long, ArrayList<Double>> valorizadorListaNum = new Hashtable<>();
                        for (Long inst : instantesOrdenados) {
                            String fechaFormateada = dateFormat.format(lt.dameTiempoParaEscibirEvolucion(inst).getTime());
                            salida += fechaFormateada + Text.EV_IMP_EXP_SEPARADOR + " " + valorizadorListaNum.get(inst).toString() + "\n";

                        }
                }

                DirectoriosYArchivos.grabaTexto(dirArch, salida);
                mostrarMensajeAvisoSinBlur("Se grabó la Evolucion en " + dirArch);

                }

            } else {
                System.out.println("Ningún directorio seleccionado.");
            }

    }

    private void habilitarComponenteAsociado(Evolucion evEditada) {
    	long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        boolean esConstante = true;
        if(evEditada != null){
            esConstante = evEditada.getClass().toString().equalsIgnoreCase(EvolucionConstante.class.toString());
        }
        componenteAsociado.setDisable(!esConstante);
        String textoAsignar = "";
        boolean boolAsignar = false;
        if(evEditada != null) {
            textoAsignar = ((evEditada.getValor(instanteActual) !=null) ? evEditada.getValor(instanteActual).toString(): "");

            switch (tipoData){
                case Text.EV_NUM_INT:
                case Text.EV_NUM_DOUBLE:
                    ((JFXTextField) componenteAsociado).setText(textoAsignar);
                    break;
                case Text.EV_BOOL:
                    boolAsignar = !textoAsignar.equals("false");
                    ((JFXCheckBox) componenteAsociado).setSelected(boolAsignar);
                    break;
                case Text.EV_VAR:
                    ((JFXComboBox) componenteAsociado).setValue(evEditada.getValor(instanteActual));
                    break;
            }
        }


    }

    public <T> Evolucion<T> loadData(){
    	long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        System.out.println("LOAD DATA-EV");
        SentidoTiempo st = new SentidoTiempo(1);
        if(tipoEV.equalsIgnoreCase(Text.EV_CONST)){
//            switch (constTipoData){
            switch (tipoData){
                case Text.EV_NUM_INT:
                    return (Evolucion<T>) new EvolucionConstante<>(Integer.parseInt(((JFXTextField)constElements.get(1)).getText()), st);
                case Text.EV_NUM_DOUBLE:
                    return (Evolucion<T>) new EvolucionConstante<>(Double.parseDouble(((JFXTextField)constElements.get(1)).getText()), st);
                case Text.EV_CONST_BOOL:
                    return (Evolucion<T>) new EvolucionConstante<>(((JFXRadioButton)constElements.get(0)).isSelected(), st);
                case Text.EV_VAR:
                    return (Evolucion<T>) new EvolucionConstante<>(((JFXComboBox)constElements.get(1)).getValue(), st);
                case Text.EV_CONST_FUNC:

                    break;
                case Text.EV_LISTA_NUM:
                    ArrayList<Double> res = new ArrayList<>();
                    for(int i=0;i<largoListaNum;i++){
                        int col = i % 6;
                        int row = (i / 6) + 5;
                        if(!((JFXTextField)constElements.get(10*col+row)).getText().isEmpty()){//TODO: largo lista num
                            res.add(Double.parseDouble(((JFXTextField) constElements.get(10*col+row)).getText()));
                        }
                    }
                    return (Evolucion<T>) new EvolucionConstante<>(res, st);
                case Text.EV_CONST_RANGOS:

                    break;
                case Text.EV_CONST_DISCRETIZACION:

                    break;
                case Text.EV_CONST_LISTA_PAR_NUM:

                    break;
            }
        }else if(tipoEV.equalsIgnoreCase(Text.EV_POR_INSTANTES)){
//            switch(porInstantesTipoData){
            switch(tipoData){
//                case Text.EV_POR_INSTANTES_NUMERICO:
                case Text.EV_NUM_INT:
                    Hashtable<Long, Integer> valorizadorInt = new Hashtable<>();
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorInt.put(instante, Integer.parseInt(((JFXTextField)row.get(1)).getText()));
                    }
                    return (Evolucion<T>) new EvolucionPorInstantes<>(valorizadorInt, Integer.parseInt(((JFXTextField)(porInstantesDynamicElements.get(1).get(1))).getText()), st);
                case Text.EV_NUM_DOUBLE:
                    Hashtable<Long, Double> valorizadorDouble = new Hashtable<>();
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorDouble.put(instante, Double.parseDouble(((JFXTextField)row.get(1)).getText()));
                    }
                    return (Evolucion<T>) new EvolucionPorInstantes<>(valorizadorDouble, Double.parseDouble(((JFXTextField)(porInstantesDynamicElements.get(1).get(1))).getText()), st);
//                case Text.EV_POR_INSTANTES_BOOL:
                case Text.EV_BOOL:
                    Hashtable<Long, Boolean> valorizadorBool = new Hashtable<>();
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorBool.put(instante, ((JFXCheckBox)row.get(1)).isSelected());
                    }
                    return (Evolucion<T>) new EvolucionPorInstantes<>(valorizadorBool, ((JFXCheckBox)(porInstantesDynamicElements.get(1).get(1))).isSelected(), st);
                case Text.EV_VAR:
                    Hashtable<Long, String> valorizadorVar = new Hashtable<>();
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorVar.put(instante, ((JFXComboBox<String>)row.get(1)).getValue());
                    }
                    return (Evolucion<T>) new EvolucionPorInstantes<>(valorizadorVar, ((JFXComboBox<String>)(porInstantesDynamicElements.get(1).get(1))).getValue(), st);
//                case Text.EV_CONST_FUNC:
//
//                    break;
                case Text.EV_LISTA_NUM:
                    ArrayList<Double> valInicial = new ArrayList<>();
                    Hashtable<Long, ArrayList<Double>> valorizadorListaNum = new Hashtable<>();
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        ArrayList<Double> rowVals = new ArrayList<>();
                        for(int i=0;i<largoListaNum;i++){
                            if(!((JFXTextField)row.get(i+1)).getText().isEmpty()){//TODO: largo lista num
                                rowVals.add(Double.parseDouble(((JFXTextField) row.get(i + 1)).getText()));
                                if (i == 0) {
                                    valInicial = rowVals;
                                }
                            }
                        }
                        valorizadorListaNum.put(instante, rowVals);
                    }
                    return (Evolucion<T>) new EvolucionPorInstantes<>(valorizadorListaNum, valInicial, st);
//                case Text.EV_CONST_RANGOS:
//
//                    break;
//                case Text.EV_CONST_DISCRETIZACION:
//
//                    break;
//                case Text.EV_CONST_LISTA_PAR_NUM:
//
//                    break;
            }
        }else if(tipoEV.equalsIgnoreCase(Text.EV_PERIODICA)){
//            switch(periodicaTipoData){
            switch(tipoData){
//                case Text.EV_PERIODICA_NUM:
                case Text.EV_NUM_INT:
                    int periodoInt = intPeriodo(((JFXComboBox<String>)periodicaElements.get(1)).getValue());
                    int cantPeriodosInt = Integer.parseInt(((JFXTextField)periodicaElements.get(3)).getText());
                    Hashtable<Long, Integer> valorizadorInt = new Hashtable<>();
                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorInt.put(instante, Integer.valueOf(((JFXTextField)row.get(1)).getText()));
                    }
                    EvolucionPorInstantes definicionPeriodoInt = new EvolucionPorInstantes<>(valorizadorInt, Integer.valueOf(((JFXTextField)(periodicaDynamicElements.get(1).get(1))).getText()), st);

                    return (Evolucion<T>) new EvolucionPeriodica(lt.getTiempoInicial(), definicionPeriodoInt, periodoInt, cantPeriodosInt, st);
                case Text.EV_NUM_DOUBLE:
                    int periodoDouble = intPeriodo(((JFXComboBox<String>)periodicaElements.get(1)).getValue());
                    int cantPeriodosDouble = Integer.parseInt(((JFXTextField)periodicaElements.get(3)).getText());
                    Hashtable<Long, Double> valorizadorDouble = new Hashtable<>();
                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorDouble.put(instante, Double.valueOf(((JFXTextField)row.get(1)).getText()));
                    }
                    EvolucionPorInstantes definicionPeriodoDouble = new EvolucionPorInstantes<>(valorizadorDouble, Double.valueOf(((JFXTextField)(periodicaDynamicElements.get(1).get(1))).getText()), st);

                    return (Evolucion<T>) new EvolucionPeriodica(lt.getTiempoInicial(), definicionPeriodoDouble, periodoDouble, cantPeriodosDouble, st);
//                case Text.EV_PERIODICA_VAR:
                case Text.EV_VAR:
                    int periodoVar = intPeriodo(((JFXComboBox<String>)periodicaElements.get(1)).getValue());
                    int cantPeriodosVar = Integer.parseInt(((JFXTextField)periodicaElements.get(3)).getText());
                    Hashtable<Long, String> valorizadorVar = new Hashtable<>();
                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorVar.put(instante, ((JFXComboBox<String>)row.get(1)).getValue());
                    }
                    EvolucionPorInstantes definicionPeriodoVar = new EvolucionPorInstantes<>(valorizadorVar, ((JFXComboBox<String>)(periodicaDynamicElements.get(1).get(1))).getValue(), st);

                    return (Evolucion<T>) new EvolucionPeriodica(lt.getTiempoInicial(), definicionPeriodoVar, periodoVar, cantPeriodosVar, st);
//                case Text.EV_PERIODICA_BOOL:
                case Text.EV_BOOL:
                    int periodoBool = intPeriodo(((JFXComboBox<String>)periodicaElements.get(1)).getValue());
                    int cantPeriodosBool = Integer.parseInt(((JFXTextField)periodicaElements.get(3)).getText());
                    Hashtable<Long, Boolean> valorizadorBool = new Hashtable<>();
                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        valorizadorBool.put(instante, ((JFXCheckBox)row.get(1)).isSelected());
                    }
                    EvolucionPorInstantes definicionPeriodoBool = new EvolucionPorInstantes<>(valorizadorBool, ((JFXCheckBox)(periodicaDynamicElements.get(1).get(1))).isSelected(), st);

                    return (Evolucion<T>) new EvolucionPeriodica(lt.getTiempoInicial(), definicionPeriodoBool, periodoBool, cantPeriodosBool, st);
//                case Text.EV_CONST_FUNC:
//
//                    break;
//                case Text.EV_CONST_VAR:
//
//                    break;
                case Text.EV_LISTA_NUM:
                    int periodoListaNum = intPeriodo(((JFXComboBox<String>)periodicaElements.get(1)).getValue());
                    int cantPeriodosListaNum = Integer.parseInt(((JFXTextField)periodicaElements.get(3)).getText());
                    ArrayList<Double> valInicial = new ArrayList<>();
                    Hashtable<Long, ArrayList<Double>> valorizadorListaNum = new Hashtable<>();
                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        GregorianCalendar calendar = new GregorianCalendar();
                        Date utilDate = Date.from(((JFXDatePicker)row.get(0)).getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        calendar.setTime(utilDate);
                        Long instante = lt.dameInstante(calendar);
                        ArrayList<Double> rowVals = new ArrayList<>();
                        for(int i=0;i<largoListaNum;i++){
                            if(!((JFXTextField)row.get(i+1)).getText().isEmpty()){//TODO: largo lista num
                                rowVals.add(Double.parseDouble(((JFXTextField) row.get(i + 1)).getText()));
                                if (i == 0) {
                                    valInicial = rowVals;
                                }
                            }
                        }
                        valorizadorListaNum.put(instante, rowVals);
                    }
                    EvolucionPorInstantes definicionPeriodoListaNum = new EvolucionPorInstantes<>(valorizadorListaNum, valInicial, st);

                    return (Evolucion<T>) new EvolucionPeriodica(lt.getTiempoInicial(), definicionPeriodoListaNum, periodoListaNum, cantPeriodosListaNum, st);
//                case Text.EV_CONST_RANGOS:
//
//                    break;
//                case Text.EV_CONST_DISCRETIZACION:
//
//                    break;
//                case Text.EV_CONST_LISTA_PAR_NUM:
//
//                    break;

            }
        }

        return null;
    }

    private void unloadData() {
    	long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        System.out.println("UNLOAD DATA-EV");
        if(ev == null) { return; }
        if(ev instanceof EvolucionConstante){
            comboBoxTipo.getSelectionModel().select(Text.EV_CONST);
            comboBoxTipoAction();
            if(ev.getValor(instanteActual) instanceof Boolean){
                ((JFXRadioButton)constElements.get(0)).setSelected((Boolean)ev.getValor(instanteActual));
                ((JFXRadioButton)constElements.get(1)).setSelected(!(Boolean)ev.getValor(instanteActual));
            }else if(ev.getValor(instanteActual) instanceof Integer){
                ((JFXTextField)constElements.get(1)).setText(String.valueOf(ev.getValor(instanteActual)));
            }else if(ev.getValor(instanteActual) instanceof Double){
                ((JFXTextField)constElements.get(1)).setText(String.valueOf(ev.getValor(instanteActual)));
            }else if(ev.getValor(instanteActual) instanceof String){
                ((JFXComboBox<String>)constElements.get(1)).getSelectionModel().select(String.valueOf(ev.getValor(instanteActual)));
            }else if(ev.getValor(instanteActual) instanceof ArrayList){
                largoListaNum = ((ArrayList)ev.getValor(instanteActual)).size();
                for(int i=0;i<((ArrayList<String>)ev.getValor(instanteActual)).size();i++){
                    int col = i % 6;
                    int row = (i / 6) + 5;
                    ((JFXTextField)constElements.get(10*col+row)).setText(String.valueOf(((ArrayList<String>)ev.getValor(instanteActual)).get(i)));
                }
            }else if(tipoData.equalsIgnoreCase(Text.EV_LISTA_NUM)){

            }
        }else if(ev instanceof EvolucionPorInstantes){
            comboBoxTipo.getSelectionModel().select(Text.EV_POR_INSTANTES);
            if(ev.getValor(instanteActual) instanceof Boolean){
                ArrayList<Long> instantesOrdenados = ((EvolucionPorInstantes)ev).getInstantesOrdenados();
                Hashtable<Long, Boolean> valorizador = ((EvolucionPorInstantes)ev).getValorizador();
                porInstantesRows = valorizador.size();
                comboBoxTipoAction();
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)porInstantesDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXCheckBox)porInstantesDynamicElements.get(i).get(1)).setSelected(valorizador.get(instantesOrdenados.get(i-1)));
                }
            }else if(ev.getValor(instanteActual) instanceof Integer){
                ArrayList<Long> instantesOrdenados = ((EvolucionPorInstantes)ev).getInstantesOrdenados();
                Hashtable<Long, Integer> valorizador = ((EvolucionPorInstantes)ev).getValorizador();
                porInstantesRows = valorizador.size();
                comboBoxTipoAction();
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)porInstantesDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXTextField)porInstantesDynamicElements.get(i).get(1)).setText(String.valueOf(valorizador.get(instantesOrdenados.get(i-1))));
                }
            }else if(ev.getValor(instanteActual) instanceof Double){
                ArrayList<Long> instantesOrdenados = ((EvolucionPorInstantes)ev).getInstantesOrdenados();
                Hashtable<Long, Double> valorizador = ((EvolucionPorInstantes)ev).getValorizador();
                porInstantesRows = valorizador.size();
                comboBoxTipoAction();
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)porInstantesDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXTextField)porInstantesDynamicElements.get(i).get(1)).setText(String.valueOf(valorizador.get(instantesOrdenados.get(i-1))));
                }
            }else if(ev.getValor(instanteActual) instanceof String){
                ArrayList<Long> instantesOrdenados = ((EvolucionPorInstantes)ev).getInstantesOrdenados();
                Hashtable<Long, String> valorizador = ((EvolucionPorInstantes)ev).getValorizador();

                porInstantesRows = valorizador.size();
                comboBoxTipoAction();
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)porInstantesDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXComboBox<String>)porInstantesDynamicElements.get(i).get(1)).getSelectionModel().select(valorizador.get(instantesOrdenados.get(i-1)));
                }
            }else if(ev.getValor(instanteActual) instanceof ArrayList){
                largoListaNum = ((ArrayList)ev.getValor(instanteActual)).size();
                ArrayList<Long> instantesOrdenados = ((EvolucionPorInstantes)ev).getInstantesOrdenados();
                Hashtable<Long, ArrayList<Double>> valorizador = ((EvolucionPorInstantes)ev).getValorizador();

                porInstantesRows = valorizador.size();
                comboBoxTipoAction();
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)porInstantesDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    for(int j=0;j<valorizador.get(instantesOrdenados.get(i-1)).size();j++){
                        ((JFXTextField)porInstantesDynamicElements.get(i).get(1+j)).setText(String.valueOf(valorizador.get(instantesOrdenados.get(i-1)).get(j)));
                    }
                }

            }
            porInstantesDynamicElementsLocal = new HashMap<>(porInstantesDynamicElements);
        }else if(ev instanceof EvolucionPeriodica){
            comboBoxTipo.getSelectionModel().select(Text.EV_PERIODICA);
            if(ev.getValor(instanteActual) instanceof Boolean){
                ArrayList<Long> instantesOrdenados = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getInstantesOrdenados();
                Hashtable<Long, Boolean> valorizador = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getValorizador();
                periodicaRows = valorizador.size();
                comboBoxTipoAction();
                ((JFXComboBox)periodicaElements.get(1)).getSelectionModel().select(dameStringPeriodo(((EvolucionPeriodica)ev).getPeriodo()));
                ((JFXTextField)periodicaElements.get(3)).setText(String.valueOf(((EvolucionPeriodica)ev).getCantPeriodos()));
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)periodicaDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXCheckBox)periodicaDynamicElements.get(i).get(1)).setSelected(valorizador.get(instantesOrdenados.get(i-1)));
                }
            }else if(ev.getValor(instanteActual) instanceof Integer){
                ArrayList<Long> instantesOrdenados = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getInstantesOrdenados();
                Hashtable<Long, Integer> valorizador = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getValorizador();

                periodicaRows = valorizador.size();
                comboBoxTipoAction();
                ((JFXComboBox)periodicaElements.get(1)).getSelectionModel().select(dameStringPeriodo(((EvolucionPeriodica)ev).getPeriodo()));
                ((JFXTextField)periodicaElements.get(3)).setText(String.valueOf(((EvolucionPeriodica)ev).getCantPeriodos()));
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)periodicaDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXTextField)periodicaDynamicElements.get(i).get(1)).setText(String.valueOf(valorizador.get(instantesOrdenados.get(i-1))));
                }
            }else if(ev.getValor(instanteActual) instanceof Double){
                ArrayList<Long> instantesOrdenados = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getInstantesOrdenados();
                Hashtable<Long, Double> valorizador = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getValorizador();

                periodicaRows = valorizador.size();
                comboBoxTipoAction();
                ((JFXComboBox)periodicaElements.get(1)).getSelectionModel().select(dameStringPeriodo(((EvolucionPeriodica)ev).getPeriodo()));
                ((JFXTextField)periodicaElements.get(3)).setText(String.valueOf(((EvolucionPeriodica)ev).getCantPeriodos()));
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)periodicaDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXTextField)periodicaDynamicElements.get(i).get(1)).setText(String.valueOf(valorizador.get(instantesOrdenados.get(i-1))));
                }
            }else if(ev.getValor(instanteActual) instanceof String){
                ArrayList<Long> instantesOrdenados = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getInstantesOrdenados();
                Hashtable<Long, String> valorizador = (((EvolucionPeriodica)ev).getDefinicionPeriodo()).getValorizador();

                periodicaRows = valorizador.size();
                comboBoxTipoAction();
                ((JFXComboBox)periodicaElements.get(1)).getSelectionModel().select(dameStringPeriodo(((EvolucionPeriodica)ev).getPeriodo()));
                ((JFXTextField)periodicaElements.get(3)).setText(String.valueOf(((EvolucionPeriodica)ev).getCantPeriodos()));
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)periodicaDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    ((JFXComboBox<String>)periodicaDynamicElements.get(i).get(1)).getSelectionModel().select(valorizador.get(instantesOrdenados.get(i-1)));
                }
            }else if(ev.getValor(instanteActual) instanceof ArrayList){
                largoListaNum = ((ArrayList)ev.getValor(instanteActual)).size();
                ArrayList<Long> instantesOrdenados = ((EvolucionPeriodica)ev).getDefinicionPeriodo().getInstantesOrdenados();
                Hashtable<Long, ArrayList<Double>> valorizador = ((EvolucionPeriodica)ev).getDefinicionPeriodo().getValorizador();

                periodicaRows = valorizador.size();
                comboBoxTipoAction();
                ((JFXComboBox)periodicaElements.get(1)).getSelectionModel().select(dameStringPeriodo(((EvolucionPeriodica)ev).getPeriodo()));
                ((JFXTextField)periodicaElements.get(3)).setText(String.valueOf(((EvolucionPeriodica)ev).getCantPeriodos()));
                for(int i=1;i<=instantesOrdenados.size();i++){
                    GregorianCalendar fechaInstante = lt.dameTiempoParaEscibirEvolucion(instantesOrdenados.get(i-1));
                    ((JFXDatePicker)periodicaDynamicElements.get(i).get(0)).setValue(LocalDate.of(fechaInstante.get(Calendar.YEAR), fechaInstante.get(Calendar.MONTH)+1, fechaInstante.get(Calendar.DAY_OF_MONTH)));
                    for(int j=0;j<valorizador.get(instantesOrdenados.get(i-1)).size();j++){
                        ((JFXTextField)periodicaDynamicElements.get(i).get(1+j)).setText(String.valueOf(valorizador.get(instantesOrdenados.get(i-1)).get(j)));
                    }
                }
            }
            periodicaDynamicElementsLocal = new HashMap<>(periodicaDynamicElements);
        }
        ev.setEditoValores(false);
    }

    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> errores = new ArrayList<>();

        if(tipoEV.equalsIgnoreCase(Text.EV_CONST)){
//            switch (constTipoData){
            switch (tipoData){
                case Text.EV_NUM_INT:
                    if(((JFXTextField)constElements.get(1)).getText().trim().equalsIgnoreCase("")){ errores.add("Datos incompleltos" ); }
                    else if(!UtilStrings.esNumeroEntero(((JFXTextField)constElements.get(1)).getText())){errores.add("No es nro entero"); }
                    break;
                case Text.EV_NUM_DOUBLE:
                    if(((JFXTextField)constElements.get(1)).getText().trim().equalsIgnoreCase("")){ errores.add("Datos incompleltos" ); }
                    else if( !UtilStrings.esNumeroDouble(((JFXTextField)constElements.get(1)).getText())){errores.add("No es nro doble"); }
                    break;
                case Text.EV_CONST_BOOL:
                    //TODO por ahora no hay controles para hacer, siempre viene un valor por defecto, puede generarse la evolucion sin ningun valor
                    break;
                case Text.EV_VAR:
                    if(((JFXComboBox) constElements.get(1)).getValue() == null){ errores.add("Datos incompleltos");}
                    break;
                case Text.EV_CONST_FUNC:
                    break;
                case Text.EV_LISTA_NUM:
                    for(int i=0;i<largoListaNum;i++){
                        int col = i % 6;
                        int row = (i / 6) + 5;
                        if(((JFXTextField)constElements.get(10*col+row)).getText().isEmpty()){//TODO: largo lista num
                            errores.add("Datos incompletos");
                        }
                    }
                    break;
                case Text.EV_CONST_RANGOS:
                    break;
                case Text.EV_CONST_DISCRETIZACION:
                    break;
                case Text.EV_CONST_LISTA_PAR_NUM:
                    break;
            }
        }
        else if(tipoEV.equalsIgnoreCase(Text.EV_POR_INSTANTES)){
            switch(tipoData){
                case Text.EV_NUM_INT:
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        if(!UtilStrings.esNumeroEntero(((JFXTextField)row.get(1)).getText().trim())){errores.add("No es nro entero");};
                    }
                    break;
                case Text.EV_NUM_DOUBLE:
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        if(!UtilStrings.esNumeroDouble(((JFXTextField)row.get(1)).getText().trim())){errores.add("No es nro doble");};
                    }
                    break;
                case Text.EV_BOOL:  //Ejemplo Centrales Acumulacion
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                    }
                    break;
                case Text.EV_VAR:
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        if(((JFXComboBox<String>) row.get(1)).getValue() == null){ errores.add("Datos incompletos"); }
                    }
                    break;
                case Text.EV_LISTA_NUM:
                    for(Integer key : porInstantesDynamicElements.keySet()){
                        ArrayList<Node> row = porInstantesDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        for(int i=0;i<largoListaNum;i++){
                            if(((JFXTextField)row.get(i+1)).getText().isEmpty()){//TODO: largo lista num
                                errores.add("Datos incompletos");
                            }
                        }
                    }
                    break;
            }
        }
        else if(tipoEV.equalsIgnoreCase(Text.EV_PERIODICA)){
            switch(tipoData){
                case Text.EV_NUM_INT:
                    if((((JFXComboBox<String>)periodicaElements.get(1)).getValue()) == null){ errores.add("Falta periodo");} ;
                    if( !UtilStrings.esNumeroEntero(((JFXTextField)periodicaElements.get(3)).getText().trim())){ errores.add("Falta cant. períodos"); }

                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        if(!UtilStrings.esNumeroEntero(((JFXTextField)row.get(1)).getText().trim())){errores.add("No es nro entero");};
                    }
                    break;
                case Text.EV_NUM_DOUBLE:
                    if((((JFXComboBox<String>)periodicaElements.get(1)).getValue()) == null){ errores.add("Falta periodo");} ;
                    if( !UtilStrings.esNumeroEntero(((JFXTextField)periodicaElements.get(3)).getText().trim())){ errores.add("Falta cant. períodos"); }

                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        if( !UtilStrings.esNumeroDouble(((JFXTextField)row.get(1)).getText().trim())){errores.add("No es double");};
                    }
                    break;
                case Text.EV_VAR:
                    if((((JFXComboBox<String>)periodicaElements.get(1)).getValue()) == null){ errores.add("Falta periodo");} ;
                    if( !UtilStrings.esNumeroEntero(((JFXTextField)periodicaElements.get(3)).getText().trim())){ errores.add("Falta cant. períodos"); }

                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        if(((JFXComboBox<String>) row.get(1)).getValue() != null){ errores.add("Falta valor lista"); };
                    }
                    break;
                case Text.EV_BOOL:
                    if((((JFXComboBox<String>)periodicaElements.get(1)).getValue()) == null){ errores.add("Falta periodo");} ;
                    if( !UtilStrings.esNumeroEntero(((JFXTextField)periodicaElements.get(3)).getText().trim())){ errores.add("Falta cantidad períodos"); }

                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                    }
                    break;
                case Text.EV_LISTA_NUM:
                    if((((JFXComboBox<String>)periodicaElements.get(1)).getValue()) == null){ errores.add("Falta periodo");} ;
                    if( !UtilStrings.esNumeroEntero(((JFXTextField)periodicaElements.get(3)).getText().trim())){ errores.add("Falta cant. períodos"); }
                    Hashtable<Long, ArrayList<Double>> valorizadorListaNum = new Hashtable<>();
                    for(Integer key : periodicaDynamicElements.keySet()){
                        ArrayList<Node> row = periodicaDynamicElements.get(key);
                        if(((JFXDatePicker)row.get(0)).getValue() == null){ errores.add("Falta fecha"); }
                        for(int i=0;i<largoListaNum;i++){
                            if(((JFXTextField)row.get(i+1)).getText().isEmpty()){//TODO: largo lista num
                                errores.add("Falta lista numeros");
                            }
                        }

                    }
                    break;
            }
        }
        return errores;
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {        }
}
