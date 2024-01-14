/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorHidraulicoController is part of MOP.
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
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import datatypes.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;

import org.controlsfx.control.PopOver;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.Polinomio;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EditorHidraulicoController extends GeneralidadesController {

    @FXML private JFXButton btnAddGen;
    @FXML private JFXButton btnRemoveGen;
    @FXML private GridPane gridPane;
    @FXML private VBox vBoxVE;

    private Integer cantGenAguasArriba = 1;
    private HashMap<Integer, Node> combosGeneradores = new HashMap<>();


    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;
    @FXML private JFXTextField inputCantModInst;
    @FXML private JFXButton inputCantModInstEV;
    @FXML private JFXTextField inputFactorCompartir;
    @FXML private JFXButton inputFactorCompartirEV;
    //generadores aguas arriba/aguas abajo
    @FXML private JFXComboBox<String> inputGenAguasArriba1;
    @FXML private JFXComboBox<String> inputGenAguasAbajo1;
    private ArrayList<String> generadoresDisponibles = new ArrayList<>();
    private ArrayList<String> generadoresSeleccionados = new ArrayList<>();
    @FXML private JFXTextField inputPotMin;
    @FXML private JFXButton inputPotMinEV;
    @FXML private JFXComboBox<String> inputPotMinUnidad;
    @FXML private JFXTextField inputPotMax;
    @FXML private JFXComboBox<String> inputPotMaxUnidad;
    @FXML private JFXButton inputPotMaxEV;
    @FXML private JFXTextField inputRendPotMin;
    @FXML private JFXButton inputRendPotMinEV;
    @FXML private JFXTextField inputRendPotMax;
    @FXML private JFXButton inputRendPotMaxEV;
    @FXML private JFXTextField inputCantModIni;
    @FXML private JFXTextField inputDispMedia;
    @FXML private JFXButton inputDispMediaEV;
    @FXML private JFXTextField inputTMedioArreglo;
    @FXML private JFXButton inputTMedioArregloEV;
    @FXML private JFXComboBox<String> inputTMedioArregloUnidad;
    @FXML private JFXTextField inputMantProg;
    @FXML private JFXButton inputMantProgEV;
    @FXML private JFXTextField inputCostoFijo;
    @FXML private JFXButton inputCostoFijoEV;
    @FXML private JFXTextField inputCostoVariable;
    @FXML private JFXButton inputCostoVariableEV;
    @FXML private JFXCheckBox inputSalidaDetallada;
    @FXML private JFXTextField inputEpsilonCaudalErogado;
    @FXML private JFXComboBox<String> inputEpsilonCaudalErogadoUnidad;
    @FXML private JFXTextField inputFuncionesPQ;
    //FcotaAguasAbajo - F
    @FXML private JFXButton inputFCotaAguasAbajo;
    private FUNCController fCotaAguasAbajoController;
    @FXML private JFXTextField inputVolumenFijo;
    @FXML private JFXButton inputVolumenFijoEV;
    @FXML private JFXButton inputVolumenFijoSwapCoVo;
    @FXML private Label labelVolumenFijo;
    @FXML private JFXTextField inputQTur1Max;
    @FXML private JFXButton inputQTur1MaxEV;
    @FXML private JFXTextField inputSaltoMinimo;
    //fCoVo - F
    @FXML private JFXButton inputFCoVo;
    private FUNCController fCoVoController;
    //fVoCo - F
    @FXML private JFXButton inputFVoCo;
    private FUNCController fVoCoController;
    //Coef evap - EV
    @FXML private JFXTextField inputCoefEvaporacion;
    @FXML private JFXButton inputCoefEvaporacionEV;
    //fEvaporacion - EV
    @FXML private JFXButton inputFEvaporacion;
    private FUNCController fEvaporacionController;
    //fFiltracion - EV
    @FXML private JFXButton inputFFiltracion;
    private FUNCController fFiltracionController;
    //fQEroMin - EV
    @FXML private JFXButton inputFQEroMin;
    private FUNCController fQEroMinController;
    //fQVerMax - EV
    @FXML private JFXButton inputFQVerMax;
    @FXML private JFXComboBox<String> inputFQverMaxUnidad;
    private FUNCController fQVerMaxController;
    @FXML private JFXTextField inputCotaInunAguasAbajo;
    @FXML private JFXTextField inputCotaInunAguasArriba;
    @FXML private JFXComboBox<String> inputAporteUnidad;
    @FXML private JFXComboBox<String> inputAporteProcOpt;
    @FXML private JFXComboBox<String> inputAporteProcSim;
    @FXML private JFXComboBox<String> inputAporteNombre;
    @FXML private JFXCheckBox inputHayReservaEstrategica;
    @FXML private JFXTextField inputVolReservaEstrategica;
    @FXML private JFXButton inputVolReservaEstrategicaEV;
    @FXML private JFXButton inputVolReservaEstrategicaSwapCoVo1;
    @FXML private Label labelVolReservaEstrategica;
    @FXML private JFXTextField inputValorMinReserva;
    @FXML private JFXButton inputValorMinReservaEV;
    @FXML private JFXCheckBox inputValorAplicaEnOpt;
    @FXML private JFXCheckBox inputTieneCaudalMinEco;
    @FXML private JFXButton inputTieneCaudalMinEcoEV;
    private HashMap<Integer, JFXTextField> inputsCaudalMinEcol = new HashMap<>();
    @FXML private JFXComboBox<String> inputCaudalMinEcoUnidad;
    @FXML private JFXButton inputCaudalMinEcoEV;
    @FXML private JFXTextField inputPenalizacionFaltanteCaudal;
    @FXML private JFXComboBox<String> inputPenalizacionFaltanteCaudalUnidad;
    @FXML private JFXButton inputPenalizacionFaltanteCaudalEV;
    @FXML private JFXCheckBox inputVertimientoConstante;
//    @FXML private JFXButton inputVertimientoConstanteEV;
    @FXML private JFXCheckBox inputHayVolumenObjetivoVertimiento;
//    @FXML private JFXButton inputHayVolumenObjetivoVertimientoEV;
    @FXML private JFXTextField inputVolumenObjetivoVertimiento;
    @FXML private JFXComboBox<String> inputVolumenObjetivoVertimientoUnidad;
    @FXML private JFXButton inputVolumenObjetivoVertimientoEV;

    @FXML private JFXCheckBox inputHayControlCotaMinima;
    @FXML private JFXTextField inputVolumenControlMinimo;
    @FXML private JFXButton inputVolumenControlMinimoEV;

    @FXML private JFXTextField inputPenalidadControlMinimo;
    @FXML private JFXButton inputPenalidadControlMinimoEV;

    @FXML private JFXCheckBox inputHayControlCotaMaxima;
    @FXML private JFXTextField inputVolumenControlMaximo;
    @FXML private JFXButton inputVolumenControlMaximoEV;
    @FXML private JFXTextField inputPenalidadControlMaximo;
    @FXML private JFXButton inputPenalidadControlMaximoEV;

    //UNIDADES
    @FXML private JFXComboBox<String> inputVolFijoUnidad;
    @FXML private JFXComboBox<String> inputCaudalMaxTurbUnidad;
    @FXML private JFXComboBox<String> inputSaltoMinimoUnidad;
    @FXML private JFXComboBox<String> inputCotaAguasAbajoUnidad;
    @FXML private JFXComboBox<String> inputCotaAguasArribaUnidad;
    @FXML private JFXComboBox<String> inputFErogMinUnidad;
    @FXML private JFXComboBox<String> inputFCotaVolumenUnidad;
    @FXML private JFXComboBox<String> inputFVolumenCotaUnidad;
    @FXML private JFXComboBox<String> inputFEvaporacionUnidad;
    @FXML private JFXComboBox<String> inputFFiltracionUnidad;
    @FXML private JFXComboBox<String> inputFCotaAguasAbajoUnidad;
    @FXML private JFXComboBox<String> inputVolControlMinUnidad;
    @FXML private JFXComboBox<String> inputVolControlMaxUnidad;
    @FXML private JFXComboBox<String> inputVolReservaEstrategicaUnidad;
    @FXML private JFXComboBox<String> inputValMinReservaUnidad;




    //valoresComportamiento
    @FXML private JFXComboBox<String> inputComportamientoLago;
    @FXML private JFXButton inputComportamientoLagoEV;
    @FXML private JFXComboBox<String> inputComportamientoCoefEnergetico;
    @FXML private JFXButton inputComportamientoCoefEnergeticoEV;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();
    private HashMap<String, Boolean> esVol = new HashMap<>();
    private HashMap<Double, Double> cotaByVol = new HashMap<>();

    private DatosCorrida datosCorrida;
    private DatosHidraulicoCorrida datosHidraulicoCorrida;
    private boolean edicion;
    private VariableDeEstadoController variableDeEstadoController;
    private ListaHidraulicosController listaHidraulicosController;
    private Integer cantPostes;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;


    public EditorHidraulicoController(DatosCorrida datosCorrida, ListaHidraulicosController listaHidraulicosController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        this.copiadoPortapapeles = false;
        this.listaHidraulicosController = listaHidraulicosController;
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
    }

    public EditorHidraulicoController(DatosCorrida datosCorrida, DatosHidraulicoCorrida datosHidraulicoCorrida, ListaHidraulicosController listaHidraulicosController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosHidraulicoCorrida = datosHidraulicoCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaHidraulicosController = listaHidraulicosController;
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
    }

    public void initialize(){

        listaHidraulicosController.getParentController().asignarTooltips(this);

        Manejador.popularListaNombresVA(datosCorrida, inputAporteProcSim, inputAporteProcOpt, inputAporteNombre);

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getItems().add(Text.BARRA_2);
        inputBarra.getSelectionModel().selectFirst();

        inputComportamientoLago.getItems().addAll(Text.COMP_LAGO_VALS);
        inputComportamientoCoefEnergetico.getItems().addAll(Text.COMP_COEF_ENER_VALS);

        inputPotMinUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMinUnidad.getSelectionModel().selectFirst();
        inputPotMaxUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMaxUnidad.getSelectionModel().selectFirst();

        inputTMedioArregloUnidad.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidad.getSelectionModel().selectFirst();

        inputEpsilonCaudalErogadoUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputEpsilonCaudalErogadoUnidad.getSelectionModel().selectFirst();

        inputAporteUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputAporteUnidad.getSelectionModel().selectFirst();

        inputCaudalMinEcoUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputCaudalMinEcoUnidad.getSelectionModel().selectFirst();

        inputPenalizacionFaltanteCaudalUnidad.getItems().add(Text.UNIDAD_USD_HM3);
        inputPenalizacionFaltanteCaudalUnidad.getSelectionModel().selectFirst();

        inputFQverMaxUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputFQverMaxUnidad.getSelectionModel().selectFirst();

        inputVolumenObjetivoVertimientoUnidad.getItems().add(Text.UNIDAD_HM3);
        inputVolumenObjetivoVertimientoUnidad.getSelectionModel().selectFirst();
       ////
        inputVolFijoUnidad.getItems().add(Text.UNIDAD_HM3);
        inputVolFijoUnidad.getSelectionModel().selectFirst();

        inputCaudalMaxTurbUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputCaudalMaxTurbUnidad.getSelectionModel().selectFirst();

        inputSaltoMinimoUnidad.getItems().add(Text.UNIDAD_M);
        inputSaltoMinimoUnidad.getSelectionModel().selectFirst();

        inputCotaAguasAbajoUnidad.getItems().add(Text.UNIDAD_M);
        inputCotaAguasAbajoUnidad.getSelectionModel().selectFirst();

        inputCotaAguasArribaUnidad.getItems().add(Text.UNIDAD_M);
        inputCotaAguasArribaUnidad.getSelectionModel().selectFirst();

        inputFErogMinUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputFErogMinUnidad.getSelectionModel().selectFirst();

        inputFCotaVolumenUnidad.getItems().add(Text.UNIDAD_HM3);
        inputFCotaVolumenUnidad.getSelectionModel().selectFirst();

        inputFVolumenCotaUnidad.getItems().add(Text.UNIDAD_M);
        inputFVolumenCotaUnidad.getSelectionModel().selectFirst();

        inputFEvaporacionUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputFEvaporacionUnidad.getSelectionModel().selectFirst();

        inputFFiltracionUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputFFiltracionUnidad.getSelectionModel().selectFirst();

        inputFCotaAguasAbajoUnidad.getItems().add(Text.UNIDAD_M);
        inputFCotaAguasAbajoUnidad.getSelectionModel().selectFirst();

        inputVolControlMinUnidad.getItems().add(Text.UNIDAD_HM3);
        inputVolControlMinUnidad.getSelectionModel().selectFirst();

        inputVolControlMaxUnidad.getItems().add(Text.UNIDAD_HM3);
        inputVolControlMaxUnidad.getSelectionModel().selectFirst();

        inputVolReservaEstrategicaUnidad.getItems().add(Text.UNIDAD_HM3);
        inputVolReservaEstrategicaUnidad.getSelectionModel().selectFirst();

        inputValMinReservaUnidad.getItems().add(Text.UNIDAD_HM3);
        inputValMinReservaUnidad.getSelectionModel().selectFirst();
        ////

        generadoresDisponibles.addAll(datosCorrida.getHidraulicos().getHidraulicos().keySet());
        if(edicion){
            generadoresDisponibles.remove(datosHidraulicoCorrida.getNombre());
        }
        generadoresDisponibles.add("Elija...");

        inputGenAguasArriba1.getItems().addAll(generadoresDisponibles);
        combosGeneradores.put(1, inputGenAguasArriba1);
        inputGenAguasArriba1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));

        inputGenAguasAbajo1.getItems().addAll(generadoresDisponibles);
        combosGeneradores.put(-1, inputGenAguasAbajo1);
        inputGenAguasAbajo1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));


        btnAddGen.setOnAction(actionEvent -> {
            JFXComboBox<String> newGen = new JFXComboBox<>();
            newGen.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
            newGen.setPromptText("Elija...");
            newGen.setMinWidth(180);
            newGen.setMaxWidth(180);
            gridPane.add(newGen, 2*cantGenAguasArriba+1,5, 2, 1);
            combosGeneradores.put(2*cantGenAguasArriba+1, newGen);
            cantGenAguasArriba++;
            GridPane.setColumnIndex(btnAddGen, 2*cantGenAguasArriba+1);
            GridPane.setColumnIndex(btnRemoveGen, 2*cantGenAguasArriba+1);
            if(cantGenAguasArriba == 3) {
                btnAddGen.setVisible(false);
                GridPane.setHalignment(btnRemoveGen, HPos.LEFT);
            }
            if(cantGenAguasArriba == 2) {
                btnRemoveGen.setVisible(true);
            }
            for(String nombreGen : generadoresDisponibles){
                if(!generadoresSeleccionados.contains(nombreGen)){
                    newGen.getItems().add(nombreGen);
                }
            }
            newGen.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));
        });

        btnRemoveGen.setOnAction(actionEvent -> {
            cantGenAguasArriba--;
            gridPane.getChildren().remove(combosGeneradores.get(2*cantGenAguasArriba+1));
            GridPane.setColumnIndex(btnAddGen, 2*cantGenAguasArriba+1);
            GridPane.setColumnIndex(btnRemoveGen, 2*cantGenAguasArriba+1);
            if(cantGenAguasArriba == 1) {
                btnRemoveGen.setVisible(false);
            }
            if(cantGenAguasArriba == 2) {
                btnAddGen.setVisible(true);
                GridPane.setHalignment(btnRemoveGen, HPos.RIGHT);
            }
            if(((ComboBox<String>)combosGeneradores.get(2*cantGenAguasArriba+1)).getValue() != null) {
                generadoresSeleccionados.remove(((ComboBox<String>) combosGeneradores.get(2 * cantGenAguasArriba + 1)).getValue());
                updateGenList(((ComboBox<String>)combosGeneradores.get(2*cantGenAguasArriba+1)).getValue(), null);
            }
        });


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EV.fxml"));
            AnchorPane newLoadedPane;
            newLoadedPane = loader.load();

            PopOver popOver = new PopOver(newLoadedPane);
            popOver.setTitle("EV");

            inputTieneCaudalMinEcoEV.setOnAction(actionEvent -> {
                popOver.show(inputTieneCaudalMinEcoEV);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


//        renderInputsCaudalMinEcol();


        esVol.put("volFijo", true);
        inputVolumenFijoSwapCoVo.setOnAction(actionEvent -> swapCotaVolumen("volFijo", inputVolumenFijo, labelVolumenFijo, "Volumen Fijo", "Cota Fija", inputVolFijoUnidad));
        esVol.put("volReservaEstrategica", true);
        inputVolReservaEstrategicaSwapCoVo1.setOnAction(actionEvent -> swapCotaVolumen("volReservaEstrategica", inputVolReservaEstrategica, labelVolReservaEstrategica, "Volumen Reserva Estrategica:", "Cota Reserva Estrategica:", inputVolReservaEstrategicaUnidad));

        esVol.put("estadoInicial", true);

        if(edicion){
            unloadData();
        }else{
            fCotaAguasAbajoController = new FUNCController("Cota Aguas Abajo");
            fCoVoController = new FUNCController("Cota Volumen");
            fVoCoController = new FUNCController("Volumen Cota");
            fEvaporacionController = new FUNCController("Evaporación");
            fFiltracionController = new FUNCController("Filtración");
            fQEroMinController = new FUNCController("QErogado minimo");
            fQVerMaxController = new FUNCController("Vertimento máximo");
            FUNCController.setFuncBtnAction(inputFCotaAguasAbajo, fCotaAguasAbajoController);
            FUNCController.setFuncBtnAction(inputFCoVo, fCoVoController);
            FUNCController.setFuncBtnAction(inputFVoCo, fVoCoController);
            FUNCController.setFuncBtnAction(inputFEvaporacion, fEvaporacionController);
            FUNCController.setFuncBtnAction(inputFFiltracion, fFiltracionController);
            FUNCController.setFuncBtnAction(inputFQEroMin, fQEroMinController);
            FUNCController.setFuncBtnAction(inputFQVerMax, fQVerMaxController);
            evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);
            evForField(null, "comportamientoLago", inputComportamientoLagoEV, Text.EV_VAR, Text.COMP_LAGO_VALS, inputComportamientoLago);
            evForField(null, "tieneCaudalMinEco", inputTieneCaudalMinEcoEV, Text.EV_BOOL, null, inputTieneCaudalMinEco);
            evForField(null, "volReservaEstrategica", inputVolReservaEstrategicaEV, Text.EV_NUM_DOUBLE, null, inputVolReservaEstrategica);
            evForField(null, "factorCompartir", inputFactorCompartirEV, Text.EV_NUM_DOUBLE, null, inputFactorCompartir);
            evForField(null, "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);
            evForField(null, "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);
            evForField(null, "rendPotMin", inputRendPotMinEV, Text.EV_NUM_DOUBLE, null, inputRendPotMin);
            evForField(null, "rendPotMax", inputRendPotMaxEV, Text.EV_NUM_DOUBLE, null, inputRendPotMax);
            evForField(null, "qTur1Max", inputQTur1MaxEV, Text.EV_NUM_DOUBLE, null, inputQTur1Max);
            evForField(null, "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);
            evForField(null, "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);
            evForField(null, "volFijo", inputVolumenFijoEV, Text.EV_NUM_DOUBLE, null, inputVolumenFijo);
            evForField(null, "coefEvaporacion", inputCoefEvaporacionEV, Text.EV_NUM_DOUBLE, null, inputCoefEvaporacion);
            evForField(null, "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);
            evForField(null, "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);
            evForField(null, "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);
            evForField(null, "penalizacionFaltanteCaudal", inputPenalizacionFaltanteCaudalEV, Text.EV_NUM_DOUBLE, null, inputPenalizacionFaltanteCaudal);
            evForField(null, "valorMinReserva", inputValorMinReservaEV, Text.EV_NUM_DOUBLE, null, inputValorMinReserva);
            evForField(null, "comportamientoCoefEner", inputComportamientoCoefEnergeticoEV, Text.EV_VAR, Text.COMP_COEF_ENER_VALS, inputComportamientoCoefEnergetico);
            //evForField(null, "caudalMinEco", inputCaudalMinEcoEV, Text.EV_LISTA_NUM, null, null);
            evForField(null, "volumenObjetivoVertimiento", inputVolumenObjetivoVertimientoEV, Text.EV_NUM_DOUBLE, null, inputVolumenObjetivoVertimiento);

            evForField(null, "volumenControlMinimo", inputVolumenControlMinimoEV, Text.EV_NUM_DOUBLE, null, inputVolumenControlMinimo);
            evForField(null, "penalidadControlMinimo", inputPenalidadControlMinimoEV, Text.EV_NUM_DOUBLE, null, inputPenalidadControlMinimo);
            evForField(null, "volumenControlMaximo", inputVolumenControlMaximoEV, Text.EV_NUM_DOUBLE, null, inputVolumenControlMaximo);
            evForField(null, "penalidadControlMaximo", inputPenalidadControlMaximoEV, Text.EV_NUM_DOUBLE, null, inputPenalidadControlMaximo);


        }

        inputDispMedia.textProperty().addListener((observable, oldValue, newValue) -> {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (!lastText.equals(newValue)) {
                            controlDisponibilidadMedia(inputDispMedia);
                            lastText = newValue;
                        }
                    });
                }
            }, DELAY_MILLISECONDS);
        });


        // V.E.
//        if(!inputComportamientoLago.getValue().equalsIgnoreCase(Constantes.HIDROSINLAGO)){
            try{
                if(edicion && datosHidraulicoCorrida.getVarsEstado() != null && !datosHidraulicoCorrida.getVarsEstado().isEmpty()){
                    variableDeEstadoController = new VariableDeEstadoController(Text.VE_VOLUMEN_DEL_LAGO_LABEL, Text.VE_VOLUMEN_DEL_LAGO, 21, datosHidraulicoCorrida.getVarsEstado().get(Text.VE_VOLUMEN_DEL_LAGO), Text.UNIDAD_HM3, this);
                }else {
                    variableDeEstadoController = new VariableDeEstadoController(Text.VE_VOLUMEN_DEL_LAGO_LABEL, Text.VE_VOLUMEN_DEL_LAGO, 21, Text.UNIDAD_HM3, this);
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("VariableDeEstado.fxml"));
                loader.setController(variableDeEstadoController);
                AnchorPane newLoadedPane = loader.load();
                vBoxVE.getChildren().add(newLoadedPane);
            }catch(Exception e){
                e.printStackTrace();
            }
//        }
        inputComportamientoLago.setOnAction(actionEvent -> {
            cambioComportamientoLago();
        });
        inputComportamientoCoefEnergetico.setOnAction(actionEvent -> {
            cambioComportamientoCoefEnergetico();
        });


        inputAceptar.setOnAction(actionEvent -> {
            controlHayCambiosEnFunciones();
            controlHayCambiosEnVE();
            if(editoVariables) {
                ArrayList<String> errores = controlDatosCompletos();
                if(errores.size() == 0 || errores == null) {

                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosHidraulicoCorrida.getNombre().trim():"";
                    if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaHidraulicosController.refresh();
                        listaHidraulicosController.actualizarLineaDeEntrada();
                        ((Stage) inputAceptar.getScene().getWindow()).close();
                        editoVariables = false;
                    }
                } else{
                    if(inputNombre.getText().trim().equals("")){
                        setLabelMessageTemporal("Falta el nombre", TipoInfo.FEEDBACK);
                    }else {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("MensajeConfirmacion.fxml"));
                            MensajeConfirmacionController mcc = new MensajeConfirmacionController(this, Text.MSG_ASK_DATOS_INCOMPLETOS_PARTICIPANTE, 1, errores);

                            loader.setController(mcc);
                            Parent root1 = loader.load();
                            Stage stage = new Stage();
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.initStyle(StageStyle.UNDECORATED);

                            Scene firstScene = new Scene(root1, 600, 200);
                            firstScene.getRoot().requestFocus();
                            stage.setScene(firstScene);
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                if(inputNombre.getText().trim().equals("Hidraulico clonado")){
                    setLabelMessageTemporal("Cambie el nombre del hidráulico clonado", TipoInfo.FEEDBACK);
                }else{
                    ((Stage)inputCancelar.getScene().getWindow()).close();
                }
            }

        });
        inputCancelar.setOnAction(actionEvent -> {
            controlHayCambiosEnFunciones();
            controlHayCambiosEnVE();
            if(editoVariables){
                mostrarMensajeConfirmacion(Text.MSG_CONF_CERRAR_SIN_GUARDAR,2 );
            }
            else{
                editoVariables = false;
                cerrarVentana();
            }
        });

        btnGuardarEnBiblioteca.setDisable(Manejador.user == null);
        btnGuardarEnBiblioteca.setOnAction(actionEvent -> {
            if(controlDatosCompletos().size() == 0) {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_HIDRAULICO_TEXT, datosHidraulicoCorrida, datosCorrida, this, inputNombre.getText());
            }else{
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });

        inputHayControlCotaMinima.setOnAction(actionEvent -> {
            cambioControlCotaMinima();
        });

        inputHayControlCotaMaxima.setOnAction(actionEvent -> {
            cambioControlCotaMaxima();
        });

        inputHayReservaEstrategica.setOnAction(actionEvent -> {
            cambioControlHayReservaEstrategica();
        });

        //Control de cambios:
        inputComportamientoCoefEnergetico.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputComportamientoLago.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputFactorCompartir.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputGenAguasAbajo1.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputGenAguasArriba1.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModIni.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDispMedia.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTMedioArreglo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputMantProg.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFijo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoVariable.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputEpsilonCaudalErogado.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputFuncionesPQ.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputVolumenFijo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputQTur1Max.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputSaltoMinimo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCoefEvaporacion.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputCotaInunAguasAbajo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCotaInunAguasArriba.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputAporteProcSim.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputAporteProcOpt.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputAporteNombre.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputHayReservaEstrategica.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputVolReservaEstrategica.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputValorMinReserva.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputValorAplicaEnOpt.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputVertimientoConstante.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputHayVolumenObjetivoVertimiento.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputVolumenObjetivoVertimiento.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputHayControlCotaMinima.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputHayControlCotaMaxima.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputVolumenControlMinimo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputVolumenControlMaximo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPenalidadControlMinimo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPenalidadControlMaximo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });


        editoVariables = (variableDeEstadoController.isEditoVariables()) ? true : editoVariables;
        controlHayCambiosEnEvoluciones();


        editoVariables = false;
    }

    private void cambioComportamientoLago() {
        Boolean desHabilitar = false;
        if(inputComportamientoLago.getValue() != null && inputComportamientoLago.getValue().trim().equalsIgnoreCase(Constantes.HIDROSINLAGO)){
            desHabilitar = true;
        }

        inputHayReservaEstrategica.setDisable(desHabilitar);
        inputVolReservaEstrategica.setDisable(desHabilitar);
        inputVolReservaEstrategicaEV.setDisable(desHabilitar);
        inputValorMinReserva.setDisable(desHabilitar);
        inputValorMinReservaEV.setDisable(desHabilitar);
        inputValorAplicaEnOpt.setDisable(desHabilitar);
        inputHayControlCotaMaxima.setDisable(desHabilitar);
        inputHayControlCotaMinima.setDisable(desHabilitar);
        inputVolumenControlMaximo.setDisable(desHabilitar);
        inputVolumenControlMaximoEV.setDisable(desHabilitar);
        inputVolumenControlMinimo.setDisable(desHabilitar);
        inputVolumenControlMinimoEV.setDisable(desHabilitar);
        inputPenalidadControlMaximo.setDisable(desHabilitar);
        inputPenalidadControlMaximoEV.setDisable(desHabilitar);
        inputPenalidadControlMinimo.setDisable(desHabilitar);
        inputPenalidadControlMinimoEV.setDisable(desHabilitar);
        inputHayVolumenObjetivoVertimiento.setDisable(desHabilitar);
        inputVolumenObjetivoVertimiento.setDisable(desHabilitar);
        inputVolumenObjetivoVertimientoEV.setDisable(desHabilitar);
        inputVolumenObjetivoVertimientoUnidad.setDisable(desHabilitar);
        inputVolControlMinUnidad.setDisable(desHabilitar);
        inputVolControlMaxUnidad.setDisable(desHabilitar);

        vBoxVE.setDisable(desHabilitar);
        if(!desHabilitar){
            cambioControlCotaMinima();
            cambioControlCotaMaxima();
            cambioControlHayReservaEstrategica();
        }



    }

    private void cambioComportamientoCoefEnergetico(){
        Boolean desHabilitar = true;
        if(inputComportamientoCoefEnergetico.getValue() != null && inputComportamientoCoefEnergetico.getValue().trim().equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)){
            desHabilitar = false;
        }
        inputFuncionesPQ.setDisable(desHabilitar);
        inputVertimientoConstante.setDisable(desHabilitar);

    }
    private void cambioControlCotaMinima(){
        boolean controla = inputHayControlCotaMinima.isSelected();
        inputVolumenControlMinimo.setDisable(!controla);
        inputVolumenControlMinimoEV.setDisable(!controla);
        inputPenalidadControlMinimo.setDisable(!controla);
        inputPenalidadControlMinimoEV.setDisable(!controla);
        inputVolControlMinUnidad.setDisable(!controla);
    }
    private void cambioControlCotaMaxima(){
        boolean controla = inputHayControlCotaMaxima.isSelected();
        inputVolumenControlMaximo.setDisable(!controla);
        inputVolumenControlMaximoEV.setDisable(!controla);
        inputPenalidadControlMaximo.setDisable(!controla);
        inputPenalidadControlMaximoEV.setDisable(!controla);
        inputVolControlMaxUnidad.setDisable(!controla);
    }

    private void cambioControlHayReservaEstrategica(){
        boolean controla = inputHayReservaEstrategica.isSelected();
        inputVolReservaEstrategica.setDisable(!controla);
        inputValorMinReserva.setDisable(!controla);
        inputValorMinReservaEV.setDisable(!controla);
        inputVolReservaEstrategicaUnidad.setDisable(!controla);
        inputValMinReservaUnidad.setDisable(!controla);
    }


    private void controlHayCambiosEnEvoluciones() {
        for (String clave : evsPorNombre.keySet()) {
            if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
                editoVariables = true;
            }
        }

    }

    private void controlHayCambiosEnFunciones() {
        if(fCotaAguasAbajoController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
        if(fCoVoController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
        if(fVoCoController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
        if(fEvaporacionController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
        if(fFiltracionController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
        if(fQEroMinController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
        if(fQVerMaxController.getDatosPolinomio().isEditoValores()){    editoVariables = true;  }
    }

    private void controlHayCambiosEnVE(){
        editoVariables = (variableDeEstadoController.isEditoVariables()) ? true : editoVariables;

    }

    private void cerrarVentana() {
        if(copiadoPortapapeles){
            listaHidraulicosController.borrarHidraulico(datosHidraulicoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaHidraulicosController.unloadData();//Cambia color participantes incompletos
            listaHidraulicosController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    };
    private void agregarTextoFuncion(JFXButton componente, String textoTooltip){
        Tooltip t = new Tooltip(textoTooltip);
        t.setFont(new Font(12.0));
        componente.setTooltip(t);

    }

    private void genComboOnChangeBehaviour(String oldValue, String newValue){
        if(oldValue != null){
            generadoresSeleccionados.remove(oldValue);
        }
        generadoresSeleccionados.add(newValue);
        updateGenList(oldValue, newValue);
    }

    private void updateGenList(String oldValue, String newValue){
        for(Node combo : combosGeneradores.values()){
            if(oldValue != null && !oldValue.isEmpty()){
                if(!((ComboBox<String>)combo).getItems().contains(oldValue)){
                    ((ComboBox<String>)combo).getItems().add(oldValue);
                }
            }
            if(newValue != null) {
                if ((((ComboBox<String>) combo).getValue() == null) || !((ComboBox<String>) combo).getValue().equalsIgnoreCase(newValue)) {
                    if(!newValue.equalsIgnoreCase("Elija...")){
                        ((ComboBox<String>) combo).getItems().remove(newValue);   }

                }
            }
        }
    }

    private void renderInputsCaudalMinEcol(){
        for(int i=0;i<cantPostes;i++){
            JFXTextField input = new JFXTextField();
            input.setMinWidth(85);
            input.setPrefWidth(85);
            input.setMaxWidth(85);
            int row = i > 5 ? 18 : 17;
            int col = (i % 7) + 10;
            gridPane.add(input,col, row);
            inputsCaudalMinEcol.put(i, input);
            GridPane.setMargin(input, new Insets(0, 0, 0, 5));
        }
        if(cantPostes > 6) {
            GridPane.setColumnIndex(inputCaudalMinEcoUnidad,16);
            GridPane.setColumnIndex(inputCaudalMinEcoEV, 17);
        }else{
            GridPane.setColumnIndex(inputCaudalMinEcoUnidad, cantPostes+10);
            GridPane.setColumnIndex(inputCaudalMinEcoEV, cantPostes+11);
        }
    }


    private void evForField(Evolucion ev, String key, JFXButton evBtn, String tipoData, ArrayList<String> vars, Node componenteAsociado){
        ArrayList<Evolucion> listaConUnaEv = new ArrayList<>();
        if(ev != null){
            listaConUnaEv.add(ev);
            EVController evController = new EVController(listaConUnaEv, datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);

        }else{
            EVController evController = new EVController(datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);
        }
    }

    public void swapCotaVolumen(String campo, JFXTextField textField, Label label, String textVol, String textCota, JFXComboBox unidad) {
        // TODO: 15/02/2022 checkear si estan definidas
    	long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        try {
            Polinomio polCoVo = new Polinomio(datosHidraulicoCorrida.getfCoVo().getValor(instanteActual));
            Polinomio polVoCo = new Polinomio(datosHidraulicoCorrida.getfVoCo().getValor(instanteActual));

            System.out.println("ANTES: " + textField.getText() + " " + esVol.get(campo));
            if (esVol.get(campo)) {
                if (cotaByVol.containsKey(Double.parseDouble(textField.getText()))) {
                    textField.setText(String.valueOf(cotaByVol.get(Double.parseDouble(textField.getText()))));
                } else {
                    double vol = Double.parseDouble(textField.getText());
                    double cota = polCoVo.dameValor(vol);
                    textField.setText(String.valueOf(cota));
                    cotaByVol.put(vol, cota);
                }
//            labelVolumenFijo.setText("Cota Fija");
                label.setText(textCota);
                unidad.getItems().clear();
                unidad.getItems().add(Text.UNIDAD_M);
                unidad.getSelectionModel().selectFirst();

            } else {
                if (cotaByVol.containsValue(Double.parseDouble(textField.getText()))) {
                    Set<Double> vol = cotaByVol.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue() == Double.parseDouble(textField.getText()))
                            .map(HashMap.Entry::getKey)
                            .collect(Collectors.toSet());
                    textField.setText(String.valueOf(vol.stream().findFirst().get()));
                } else {
                    double vol = polVoCo.dameValor(Double.parseDouble(textField.getText()));
                    double cota = Double.parseDouble(textField.getText());
                    textField.setText(String.valueOf(vol));
                    cotaByVol.put(vol, cota);
                }
//            labelVolumenFijo.setText("Volumen Fijo");
                label.setText(textVol);
                unidad.getItems().clear();
                unidad.getItems().add(Text.UNIDAD_HM3);
                unidad.getSelectionModel().selectFirst();

            }
            esVol.put(campo, !esVol.get(campo));
            System.out.println("DESPUES: " + textField.getText() + " " + esVol.get(campo));
        }catch (Exception e){
            if (esVol.get(campo)) {
                label.setText(textCota);
                System.out.println("ANTES: " + esVol.get(campo));
            }else{
                label.setText(textVol);
                System.out.println("ANTES: " + esVol.get(campo));
            }
            esVol.put(campo, !esVol.get(campo));
            System.out.println("DESPUES: " + esVol.get(campo));
        }
    }

    /**
     *  Método para cargar los datos en el DatosHidraulicoCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-HIDRÁULICO");


        Evolucion<String> compLago = EVController.loadEVsegunTipo(inputComportamientoLago, Text.EV_VAR,  evsPorNombre,"comportamientoLago" );
        Evolucion<String> compCoefEnergetico = EVController.loadEVsegunTipo(inputComportamientoCoefEnergetico, Text.EV_VAR,  evsPorNombre,"comportamientoCoefEner" );
        Hashtable<String,Evolucion<String>> valoresComportamientos = new Hashtable<>();
        valoresComportamientos.put(Constantes.COMPLAGO, compLago);
        valoresComportamientos.put(Constantes.COMPCOEFENERGETICO, compCoefEnergetico);

        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();


        Evolucion<Integer> cantModInst = null;
        if(UtilStrings.esNumeroEntero(inputCantModInst.getText())) { cantModInst = EVController.loadEVsegunTipo(inputCantModInst, Text.EV_NUM_INT,  evsPorNombre,"cantModInst" ); }

        Evolucion<Double> factorCompartir = null;
        if(UtilStrings.esNumeroDouble(inputFactorCompartir.getText())) { factorCompartir = EVController.loadEVsegunTipo(inputFactorCompartir, Text.EV_NUM_DOUBLE,  evsPorNombre,"factorCompartir" ); }

        ArrayList<String> hidraulicosAguasArriba = new ArrayList<>();
        for(Integer combo_key : combosGeneradores.keySet()){
            if(combo_key > 0){
                if(((ComboBox<String>)combosGeneradores.get(combo_key)).getValue() != null) {
                    String hidraulico = ((ComboBox<String>)combosGeneradores.get(combo_key)).getValue();
                    if(!hidraulico.equalsIgnoreCase("Elija...")) {
                        hidraulicosAguasArriba.add(((ComboBox<String>)combosGeneradores.get(combo_key)).getValue()); }
                }
            }
        }

        String hidraulicoAguasAbajo = "";
        String hidraulico = ((ComboBox<String>)combosGeneradores.get(-1)).getValue();
        if( ((ComboBox<String>)combosGeneradores.get(-1)).getValue() != null  && !hidraulico.equalsIgnoreCase("Elija...")) {   hidraulicoAguasAbajo = hidraulico; }

        Evolucion<Double> potMin = null;
        if(UtilStrings.esNumeroDouble(inputPotMin.getText())) { potMin = EVController.loadEVsegunTipo(inputPotMin, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMin" ); }

        Evolucion<Double> potMax = null;
        if(UtilStrings.esNumeroDouble(inputPotMax.getText())) { potMax = EVController.loadEVsegunTipo(inputPotMax, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMax" );}

        Evolucion<Double> rendPotMin = null;
        if(UtilStrings.esNumeroDouble(inputRendPotMin.getText())) { rendPotMin = EVController.loadEVsegunTipo(inputRendPotMin, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMin" );}

        Evolucion<Double> rendPotMax = null;
        if(UtilStrings.esNumeroDouble(inputRendPotMax.getText())) { rendPotMax = EVController.loadEVsegunTipo(inputRendPotMax, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMax" );}


        Integer cantModIni = 0;
        if (UtilStrings.esNumeroEntero(inputCantModIni.getText().trim())) { cantModIni = Integer.parseInt(inputCantModIni.getText().trim()); }

        Evolucion<Double> dispMedia = null;
        if(UtilStrings.esNumeroDouble(inputDispMedia.getText())) { dispMedia = EVController.loadEVsegunTipo(inputDispMedia, Text.EV_NUM_DOUBLE,  evsPorNombre,"dispMedia" );}

        Evolucion<Double> tMedioArreglo = null;
        if(UtilStrings.esNumeroDouble(inputTMedioArreglo.getText())) { tMedioArreglo = EVController.loadEVsegunTipo(inputTMedioArreglo, Text.EV_NUM_DOUBLE,  evsPorNombre,"tMedioArreglo" );}

        Evolucion<Integer> mantProgramado = null;
        if(UtilStrings.esNumeroEntero(inputMantProg.getText())) { mantProgramado = EVController.loadEVsegunTipo(inputMantProg, Text.EV_NUM_INT,  evsPorNombre,"mantProg" );}

        Evolucion<Double> costoFijo = null;
        if(UtilStrings.esNumeroDouble(inputCostoFijo.getText())) { costoFijo = EVController.loadEVsegunTipo(inputCostoFijo, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoFijo" );}

        Evolucion<Double> costoVariable =  null;
        if(UtilStrings.esNumeroDouble(inputCostoVariable.getText())) { costoVariable = EVController.loadEVsegunTipo(inputCostoVariable, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoVariable" ); }

        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        double epsilonCaudalErogadoIteracion = 0;
        if(UtilStrings.esNumeroDouble(inputEpsilonCaudalErogado.getText())) { epsilonCaudalErogadoIteracion = Double.parseDouble(inputEpsilonCaudalErogado.getText()); }
        String rutaPQ = inputFuncionesPQ.getText();


        Evolucion<Double> volFijo = null;
        if(UtilStrings.esNumeroDouble(inputVolumenFijo.getText())) {
            if(inputVolFijoUnidad.getValue().equals(Text.UNIDAD_M)) {
                swapCotaVolumen("volFijo", inputVolumenFijo, labelVolumenFijo, "Volumen Fijo", "Cota Fija", inputVolFijoUnidad); }
            volFijo =  EVController.loadEVsegunTipo(inputVolumenFijo, Text.EV_NUM_DOUBLE,  evsPorNombre,"volumenFijo" );
        }
        //fCoefEnerg
        Evolucion<Double> qTur1Max = null;
        if(UtilStrings.esNumeroDouble(inputQTur1Max.getText())) { qTur1Max = EVController.loadEVsegunTipo(inputQTur1Max, Text.EV_NUM_DOUBLE,  evsPorNombre,"qTur1Max" );}

        double saltoMin = 0;
        if(UtilStrings.esNumeroDouble(inputSaltoMinimo.getText())){ saltoMin = Double.parseDouble(inputSaltoMinimo.getText());}

        Evolucion<Double> coefEvaporacion = null;
        if(UtilStrings.esNumeroDouble(inputCoefEvaporacion.getText())) { coefEvaporacion = EVController.loadEVsegunTipo(inputCoefEvaporacion, Text.EV_NUM_DOUBLE,  evsPorNombre,"coefEvaporacion" );}

        Evolucion<DatosPolinomio> fCoAA = null;
        if(fCotaAguasAbajoController.getDatosPolinomioSinLoadData() != null ) { fCoAA = new EvolucionConstante<>(fCotaAguasAbajoController.getDatosPolinomio(), new SentidoTiempo(1)); }

        Evolucion<DatosPolinomio> fCoVo =  null;
        if(fCoVoController.getDatosPolinomioSinLoadData() != null) { fCoVo = new EvolucionConstante<>(fCoVoController.getDatosPolinomio(), new SentidoTiempo(1)); }

        Evolucion<DatosPolinomio> fVoCo =  null;
        if(fVoCoController.getDatosPolinomioSinLoadData() != null ) { fVoCo =  new EvolucionConstante<>(fVoCoController.getDatosPolinomio(), new SentidoTiempo(1)); }

        Evolucion<DatosPolinomio> fQEroMin =  null;
        if(fQEroMinController.getDatosPolinomioSinLoadData() != null) { fQEroMin = new EvolucionConstante<>(fQEroMinController.getDatosPolinomio(), new SentidoTiempo(1)); }

        Evolucion<DatosPolinomio> fEvaporacion =  null;
        if(fEvaporacionController.getDatosPolinomioSinLoadData() != null ) { fEvaporacion = new EvolucionConstante<>(fEvaporacionController.getDatosPolinomio(), new SentidoTiempo(1)); }

        Evolucion<DatosPolinomio> fFiltracion =  null;
        if(fFiltracionController.getDatosPolinomioSinLoadData() != null) { fFiltracion =  new EvolucionConstante<>(fFiltracionController.getDatosPolinomio(), new SentidoTiempo(1)); }

        Evolucion<DatosPolinomio> fQVerMax =  null;
        if(fQVerMaxController.getDatosPolinomioSinLoadData() != null ) { fQVerMax = new EvolucionConstante<>(fQVerMaxController.getDatosPolinomio(), new SentidoTiempo(1)); }

        double cotaInundacionAguasArriba = 0;
        if(UtilStrings.esNumeroDouble(inputCotaInunAguasArriba.getText())) { cotaInundacionAguasArriba = Double.parseDouble(inputCotaInunAguasArriba.getText()); }

        double cotaInundacionAguasAbajo = 0;
        if(UtilStrings.esNumeroDouble(inputCotaInunAguasAbajo.getText())) { cotaInundacionAguasAbajo = Double.parseDouble(inputCotaInunAguasAbajo.getText()); }

        DatosVariableAleatoria aporte = new DatosVariableAleatoria(inputAporteProcOpt.getValue(), inputAporteProcSim.getValue(), inputAporteNombre.getValue());

        boolean hayReservaEstrategica = inputHayReservaEstrategica.isSelected();
        Evolucion<Double> volReservaEstrategica = null;
        if(UtilStrings.esNumeroDouble(inputVolReservaEstrategica.getText())) {
            if(inputVolReservaEstrategicaUnidad.getValue().equals(Text.UNIDAD_M)){
                swapCotaVolumen("volReservaEstrategica", inputVolReservaEstrategica, labelVolReservaEstrategica, "Volumen Reserva Estrategica:", "Cota Reserva Estrategica:", inputVolReservaEstrategicaUnidad);
            }
            volReservaEstrategica = EVController.loadEVsegunTipo(inputVolReservaEstrategica, Text.EV_NUM_DOUBLE,  evsPorNombre,"volReservaEstrategica" );
        }
        Evolucion<Double> valorMinReserva = null;
        if(UtilStrings.esNumeroDouble(inputValorMinReserva.getText())) { valorMinReserva = EVController.loadEVsegunTipo(inputValorMinReserva, Text.EV_NUM_DOUBLE,  evsPorNombre,"valorMinReserva" ); }
        boolean valorAplicaOpt = inputValorAplicaEnOpt.isSelected();

        boolean vertimientoConstante = inputVertimientoConstante.isSelected();
        boolean hayVolumenObjetivoVertimiento = inputHayVolumenObjetivoVertimiento.isSelected();
        Evolucion<Double> volumenObjetivoVertimiento = null;
        if(UtilStrings.esNumeroDouble(inputVolumenObjetivoVertimiento.getText())) { volumenObjetivoVertimiento = EVController.loadEVsegunTipo(inputVolumenObjetivoVertimiento, Text.EV_NUM_DOUBLE,  evsPorNombre,"volumenObjetivoVertimiento" ); }

        boolean hayControlCotaMinima = inputHayControlCotaMinima.isSelected();
        Evolucion<Double> volumenControlMinimo = null;
        if(UtilStrings.esNumeroDouble(inputVolumenControlMinimo.getText())) { volumenControlMinimo = EVController.loadEVsegunTipo(inputVolumenControlMinimo, Text.EV_NUM_DOUBLE,  evsPorNombre,"volumenControlMinimo" ); }
        Evolucion<Double> penalidadControlMinimo = null;
        if(UtilStrings.esNumeroDouble(inputPenalidadControlMinimo.getText())) { penalidadControlMinimo = EVController.loadEVsegunTipo(inputPenalidadControlMinimo, Text.EV_NUM_DOUBLE,  evsPorNombre,"penalidadControlMinimo" ); }
        boolean hayControlCotaMaxima = inputHayControlCotaMaxima.isSelected();
        Evolucion<Double> volumenControlMaximo = null;
        if(UtilStrings.esNumeroDouble(inputVolumenControlMaximo.getText())) { volumenControlMaximo = EVController.loadEVsegunTipo(inputVolumenControlMaximo, Text.EV_NUM_DOUBLE,  evsPorNombre,"volumenControlMaximo" ); }
        Evolucion<Double> penalidadControlMaximo = null;
        if(UtilStrings.esNumeroDouble(inputPenalidadControlMaximo.getText())) { penalidadControlMaximo = EVController.loadEVsegunTipo(inputPenalidadControlMaximo, Text.EV_NUM_DOUBLE,  evsPorNombre,"penalidadControlMaximo" ); }

        Hashtable<String, DatosVariableEstado> varsEstado = new Hashtable<>();
        //if(inputComportamientoLago.getValue() != null && !inputComportamientoLago.getValue().equals(Constantes.HIDROSINLAGO)) {
            if(variableDeEstadoController.getDatosVariableEstado() != null) {
                DatosVariableEstado datosVariableEstado = variableDeEstadoController.getDatosVariableEstado();

                varsEstado.put(datosVariableEstado.getNombre(), datosVariableEstado);
            }
       // }


        if(edicion){
            String nombreViejo = datosHidraulicoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getHidraulicos().getHidraulicos().remove(nombreViejo);
                int ind = datosCorrida.getHidraulicos().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getHidraulicos().getOrdenCargaXML().set(ind,nombre);

                ind = datosCorrida.getHidraulicos().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getHidraulicos().getListaUtilizados().set(ind,nombre);

            }
            datosHidraulicoCorrida.setNombre(nombre);
            datosHidraulicoCorrida.setPropietario("UTE"); //Se hardcodea hasta habilitar campo
            datosHidraulicoCorrida.setBarra(barra);
            datosHidraulicoCorrida.setRutaPQ(rutaPQ);
            datosHidraulicoCorrida.setCantModInst(cantModInst);
            datosHidraulicoCorrida.setFactorCompartir(factorCompartir);
            datosHidraulicoCorrida.setHidraulicosAguasArriba(hidraulicosAguasArriba);
            datosHidraulicoCorrida.setHidraulicoAguasAbajo(hidraulicoAguasAbajo);
            datosHidraulicoCorrida.setPotMin(potMin);
            datosHidraulicoCorrida.setPotMax(potMax);
            datosHidraulicoCorrida.setRendPotMin(rendPotMin);
            datosHidraulicoCorrida.setRendPotMax(rendPotMax);
            datosHidraulicoCorrida.setqTur1Max(qTur1Max);
            datosHidraulicoCorrida.setAporte(aporte);
            datosHidraulicoCorrida.setCantModIni(cantModIni);
            datosHidraulicoCorrida.setDispMedia(dispMedia);
            datosHidraulicoCorrida.settMedioArreglo(tMedioArreglo);
            datosHidraulicoCorrida.setMantProgramado(mantProgramado);
            datosHidraulicoCorrida.setCostoFijo(costoFijo);
            datosHidraulicoCorrida.setCostoVariable(costoVariable);
            datosHidraulicoCorrida.setSalDetallada(salidaDetallada);
            datosHidraulicoCorrida.setVolFijo(volFijo);
            //fCoefEnerg
            datosHidraulicoCorrida.setfCoAA(fCoAA);
            datosHidraulicoCorrida.setSaltoMin(saltoMin);
            datosHidraulicoCorrida.setCotaInundacionAguasArriba(cotaInundacionAguasArriba);
            datosHidraulicoCorrida.setCotaInundacionAguasAbajo(cotaInundacionAguasAbajo);
            datosHidraulicoCorrida.setfQEroMin(fQEroMin);
            datosHidraulicoCorrida.setfCoVo(fCoVo);
            datosHidraulicoCorrida.setfVoCo(fVoCo);
            datosHidraulicoCorrida.setfEvaporacion(fEvaporacion);
            datosHidraulicoCorrida.setCoefEvaporacion(coefEvaporacion);
            datosHidraulicoCorrida.setfFiltracion(fFiltracion);
            datosHidraulicoCorrida.setfQVerM(fQVerMax);
            datosHidraulicoCorrida.setValoresComportamientos(valoresComportamientos);
//            if(!inputComportamientoLago.getValue().equals(Constantes.HIDROSINLAGO)) {
                datosHidraulicoCorrida.setVarsEstado(varsEstado);
//            }
            datosHidraulicoCorrida.setEpsilonCaudalErogadoIteracion(epsilonCaudalErogadoIteracion);
            datosHidraulicoCorrida.setVolReservaEstrategica(volReservaEstrategica);
            datosHidraulicoCorrida.setValorMinReserva(valorMinReserva);
            datosHidraulicoCorrida.setValorAplicaOptim(valorAplicaOpt);
            datosHidraulicoCorrida.setHayReservaEstrategica(hayReservaEstrategica);
            datosHidraulicoCorrida.setVertimientoConstante(vertimientoConstante);
            datosHidraulicoCorrida.setHayVolObjVert(hayVolumenObjetivoVertimiento);
            datosHidraulicoCorrida.setVolObjVert(volumenObjetivoVertimiento);
            datosHidraulicoCorrida.setHayControldeCotasMinimas(hayControlCotaMinima);
            datosHidraulicoCorrida.setVolumenControlMinimo(volumenControlMinimo);
            datosHidraulicoCorrida.setPenalidadControlMinimo(penalidadControlMinimo);

            datosHidraulicoCorrida.setHayControldeCotasMaximas(hayControlCotaMaxima);
            datosHidraulicoCorrida.setVolumenControlMaximo(volumenControlMaximo);
            datosHidraulicoCorrida.setPenalidadControlMaximo(penalidadControlMaximo);

        }else {
        	/**
        	 * TODO:AGREGAR VALORES DE COTAS DE CONTROL (ESTÁN NULOS)
        	 */
            datosHidraulicoCorrida = new DatosHidraulicoCorrida(nombre,null, barra, rutaPQ, cantModInst, factorCompartir, hidraulicosAguasArriba, hidraulicoAguasAbajo, potMin, potMax,
                                                                rendPotMin, rendPotMax, volFijo, qTur1Max, aporte, cantModIni, dispMedia, tMedioArreglo,
                                                                null, fCoAA, saltoMin, cotaInundacionAguasAbajo, cotaInundacionAguasArriba, fQEroMin, fCoVo,
                                                                fVoCo, fEvaporacion, coefEvaporacion, fFiltracion, fQVerMax, varsEstado, epsilonCaudalErogadoIteracion, salidaDetallada,
                                                                mantProgramado, costoFijo, costoVariable,
                                                                volReservaEstrategica, valorMinReserva, valorAplicaOpt, hayReservaEstrategica,
                                                                vertimientoConstante, hayVolumenObjetivoVertimiento, volumenObjetivoVertimiento,hayControlCotaMinima,volumenControlMinimo, penalidadControlMinimo, hayControlCotaMaxima,volumenControlMaximo, penalidadControlMaximo);
            datosHidraulicoCorrida.setValoresComportamientos(valoresComportamientos);
            datosCorrida.getHidraulicos().getListaUtilizados().add(nombre);
            datosCorrida.getHidraulicos().getOrdenCargaXML().add(nombre);

        }
        datosCorrida.getHidraulicos().getHidraulicos().put(nombre, datosHidraulicoCorrida);
    }

    /**
     * Método para obtener los datos del DatosHidraulicoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-HIDRÁULICO");
        inputNombre.setText(datosHidraulicoCorrida.getNombre());
        inputBarra.getSelectionModel().select(datosHidraulicoCorrida.getBarra());
//        inputBarra.getSelectionModel().select(Text.BARRA_1);//TODO: uninodal hardcodeado

        inputFuncionesPQ.setText(datosHidraulicoCorrida.getRutaPQ());

        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        
        
        if(datosHidraulicoCorrida.getCantModInst() != null ) { inputCantModInst.setText(datosHidraulicoCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);

        if(datosHidraulicoCorrida.getFactorCompartir() != null) { inputFactorCompartir.setText(datosHidraulicoCorrida.getFactorCompartir().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getFactorCompartir(), "factorCompartir", inputFactorCompartirEV, Text.EV_NUM_DOUBLE, null, inputFactorCompartir);

        for(int i=0;i<datosHidraulicoCorrida.getHidraulicosAguasArriba().size();i++){
            if(i==0){
                inputGenAguasArriba1.getSelectionModel().select(datosHidraulicoCorrida.getHidraulicosAguasArriba().get(i));
            }else if(i < 3){//TODO: maximo harcodeado
                btnAddGen.fire();
                ((ComboBox<String>)combosGeneradores.get(2*(cantGenAguasArriba-1)+1)).getSelectionModel().select(datosHidraulicoCorrida.getHidraulicosAguasArriba().get(i));
            }
        }
        inputGenAguasAbajo1.getSelectionModel().select(datosHidraulicoCorrida.getHidraulicoAguasAbajo());

        if(datosHidraulicoCorrida.getPotMin() != null) { inputPotMin.setText(datosHidraulicoCorrida.getPotMin().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getPotMin(), "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);

        if(datosHidraulicoCorrida.getPotMax() != null) { inputPotMax.setText(datosHidraulicoCorrida.getPotMax().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getPotMax(), "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);

        if(datosHidraulicoCorrida.getRendPotMin() != null) { inputRendPotMin.setText(datosHidraulicoCorrida.getRendPotMin().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getRendPotMin(), "rendPotMin", inputRendPotMinEV, Text.EV_NUM_DOUBLE, null, inputRendPotMin);

        if(datosHidraulicoCorrida.getRendPotMax() != null ) { inputRendPotMax.setText(datosHidraulicoCorrida.getRendPotMax().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getRendPotMax(), "rendPotMax", inputRendPotMaxEV, Text.EV_NUM_DOUBLE, null, inputRendPotMax);

        if(datosHidraulicoCorrida.getqTur1Max() != null) { inputQTur1Max.setText(datosHidraulicoCorrida.getqTur1Max().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getqTur1Max(), "qTur1Max", inputQTur1MaxEV, Text.EV_NUM_DOUBLE, null, inputQTur1Max);

        if(datosHidraulicoCorrida.getAporte() != null) {
            inputAporteProcOpt.getSelectionModel().select(datosHidraulicoCorrida.getAporte().getProcOptimizacion());
            inputAporteProcSim.getSelectionModel().select(datosHidraulicoCorrida.getAporte().getProcSimulacion());
            if(datosHidraulicoCorrida.getAporte().getProcOptimizacion() != null && datosHidraulicoCorrida.getAporte().getProcSimulacion() != null){
                inputAporteNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datosHidraulicoCorrida.getAporte().getProcOptimizacion()),
                        datosCorrida.getProcesosEstocasticos().get(datosHidraulicoCorrida.getAporte().getProcSimulacion())));
                inputAporteNombre.getSelectionModel().select(datosHidraulicoCorrida.getAporte().getNombre());
                }

        }

        inputCantModIni.setText(datosHidraulicoCorrida.getCantModIni().toString());

        if(datosHidraulicoCorrida.getDispMedia() != null) { inputDispMedia.setText(datosHidraulicoCorrida.getDispMedia().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);

        if(datosHidraulicoCorrida.gettMedioArreglo() != null ) { inputTMedioArreglo.setText(datosHidraulicoCorrida.gettMedioArreglo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);

        if(datosHidraulicoCorrida.getMantProgramado() != null ) {inputMantProg.setText(datosHidraulicoCorrida.getMantProgramado().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);

        if(datosHidraulicoCorrida.getCostoFijo() != null ) {inputCostoFijo.setText(datosHidraulicoCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosHidraulicoCorrida.getCostoVariable() != null ) {inputCostoVariable.setText(datosHidraulicoCorrida.getCostoVariable().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getCostoVariable(), "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);

        inputSalidaDetallada.setSelected(datosHidraulicoCorrida.isSalDetallada());

        if(datosHidraulicoCorrida.getVolFijo() != null ) { inputVolumenFijo.setText(datosHidraulicoCorrida.getVolFijo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getVolFijo(), "volFijo", inputVolumenFijoEV, Text.EV_NUM_DOUBLE, null, inputVolumenFijo);

        //fCoefEnerg
        if (datosHidraulicoCorrida.getfCoAA() != null) {
            fCotaAguasAbajoController = new FUNCController(datosHidraulicoCorrida.getfCoAA().getValor(instanteActual), "Cota Aguas Abajo");

            agregarTextoFuncion(inputFCotaAguasAbajo,datosHidraulicoCorrida.getfCoAA().getValor(instanteActual).toString());
        } else { fCotaAguasAbajoController = new FUNCController("Cota Aguas Abajo"); }
        FUNCController.setFuncBtnAction(inputFCotaAguasAbajo, fCotaAguasAbajoController);


        inputSaltoMinimo.setText(datosHidraulicoCorrida.getSaltoMin().toString());
        inputCotaInunAguasArriba.setText(datosHidraulicoCorrida.getCotaInundacionAguasArriba().toString());
        inputCotaInunAguasAbajo.setText(datosHidraulicoCorrida.getCotaInundacionAguasAbajo().toString());

        if(datosHidraulicoCorrida.getfQEroMin() != null ) {
            fQEroMinController = new FUNCController(datosHidraulicoCorrida.getfQEroMin().getValor(instanteActual), "QErogado minimo");
            agregarTextoFuncion(inputFQEroMin,datosHidraulicoCorrida.getfQEroMin().getValor(instanteActual).toString());
        } else { fQEroMinController = new FUNCController("QErogado minimo"); }
        FUNCController.setFuncBtnAction(inputFQEroMin, fQEroMinController);

        if(datosHidraulicoCorrida.getfCoVo() != null ) {
            fCoVoController = new FUNCController(datosHidraulicoCorrida.getfCoVo().getValor(instanteActual), "Cota Volumen");
            agregarTextoFuncion(inputFCoVo,datosHidraulicoCorrida.getfCoVo().getValor(instanteActual).toString());
        } else { fCoVoController = new FUNCController("Cota Volumen"); }
        FUNCController.setFuncBtnAction(inputFCoVo, fCoVoController);


        if(datosHidraulicoCorrida.getfVoCo() != null ) {
            fVoCoController = new FUNCController(datosHidraulicoCorrida.getfVoCo().getValor(instanteActual), "Volumen Cota");
            agregarTextoFuncion(inputFVoCo,datosHidraulicoCorrida.getfVoCo().getValor(instanteActual).toString());
        } else { fVoCoController = new FUNCController("Volumen Cota"); }
        FUNCController.setFuncBtnAction(inputFVoCo, fVoCoController);

        if(datosHidraulicoCorrida.getfEvaporacion() != null ) {
            fEvaporacionController = new FUNCController(datosHidraulicoCorrida.getfEvaporacion().getValor(instanteActual), "Evaporación");
            agregarTextoFuncion(inputFEvaporacion,datosHidraulicoCorrida.getfEvaporacion().getValor(instanteActual).toString());
        } else { fEvaporacionController = new FUNCController("Evaporación"); }
        FUNCController.setFuncBtnAction(inputFEvaporacion, fEvaporacionController);

        if(datosHidraulicoCorrida.getfFiltracion() != null ) {
            fFiltracionController = new FUNCController(datosHidraulicoCorrida.getfFiltracion().getValor(instanteActual), "Filtración");
            agregarTextoFuncion(inputFFiltracion,datosHidraulicoCorrida.getfFiltracion().getValor(instanteActual).toString());
        } else { fFiltracionController = new FUNCController("Filtración"); }
        FUNCController.setFuncBtnAction(inputFFiltracion, fFiltracionController);

        if(datosHidraulicoCorrida.getfQVerM() != null ) {
            fQVerMaxController = new FUNCController(datosHidraulicoCorrida.getfQVerM().getValor(instanteActual), "Vertimento máximo");
            agregarTextoFuncion(inputFQVerMax,datosHidraulicoCorrida.getfQVerM().getValor(instanteActual).toString());
        } else { fQVerMaxController = new FUNCController("Vertimento máximo"); }
        FUNCController.setFuncBtnAction(inputFQVerMax, fQVerMaxController);

        //coefEvaporacion
        if(datosHidraulicoCorrida.getCoefEvaporacion() != null ) { inputCoefEvaporacion.setText(datosHidraulicoCorrida.getCoefEvaporacion().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getCoefEvaporacion(), "coefEvaporacion", inputCoefEvaporacionEV, Text.EV_NUM_DOUBLE, null, inputCoefEvaporacion);

        if(datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPLAGO) != null ) {  inputComportamientoLago.getSelectionModel().select(datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPLAGO).getValor(instanteActual)); }
        evForField(datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPLAGO), "comportamientoLago", inputComportamientoLagoEV, Text.EV_VAR, Text.COMP_LAGO_VALS, inputComportamientoLago);

        if(datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPCOEFENERGETICO) != null ) {  inputComportamientoCoefEnergetico.getSelectionModel().select(datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPCOEFENERGETICO).getValor(instanteActual)); }
        evForField(datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPCOEFENERGETICO), "comportamientoCoefEner", inputComportamientoCoefEnergeticoEV, Text.EV_VAR, Text.COMP_COEF_ENER_VALS, inputComportamientoCoefEnergetico);

        inputEpsilonCaudalErogado.setText(datosHidraulicoCorrida.getEpsilonCaudalErogadoIteracion().toString());

        if(datosHidraulicoCorrida.getVolReservaEstrategica() != null ) { inputVolReservaEstrategica.setText(datosHidraulicoCorrida.getVolReservaEstrategica().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getVolReservaEstrategica(), "volReservaEstrategica", inputVolReservaEstrategicaEV, Text.EV_NUM_DOUBLE, null, inputVolReservaEstrategica);

        if(datosHidraulicoCorrida.getValorMinReserva() != null ) { inputValorMinReserva.setText(datosHidraulicoCorrida.getValorMinReserva().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getValorMinReserva(), "valorMinReserva", inputValorMinReservaEV, Text.EV_NUM_DOUBLE, null, inputValorMinReserva);

        inputValorAplicaEnOpt.setSelected(datosHidraulicoCorrida.isValorAplicaOptim());
        inputHayReservaEstrategica.setSelected(datosHidraulicoCorrida.isHayReservaEstrategica());
        inputVertimientoConstante.setSelected(datosHidraulicoCorrida.isVertimientoConstante());
        inputHayVolumenObjetivoVertimiento.setSelected(datosHidraulicoCorrida.isHayVolObjVert());

        if(datosHidraulicoCorrida.getVolObjVert() != null ) { inputVolumenObjetivoVertimiento.setText(datosHidraulicoCorrida.getVolObjVert().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getVolObjVert(), "volumenObjetivoVertimiento", inputVolumenObjetivoVertimientoEV, Text.EV_NUM_DOUBLE, null, inputVolumenObjetivoVertimiento);

        if(datosHidraulicoCorrida.getfCoVo() != null) {
            Polinomio polCoVo = new Polinomio(datosHidraulicoCorrida.getfCoVo().getValor(instanteActual));
            cotaByVol.put(datosHidraulicoCorrida.getVolFijo().getValor(instanteActual), polCoVo.dameValor(datosHidraulicoCorrida.getVolFijo().getValor(instanteActual)));
        }

        inputHayControlCotaMinima.setSelected(datosHidraulicoCorrida.isHayControldeCotasMinimas());

        if(datosHidraulicoCorrida.getVolumenControlMinimo() != null ) { inputVolumenControlMinimo.setText(datosHidraulicoCorrida.getVolumenControlMinimo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getVolumenControlMinimo(), "volumenControlMinimo", inputVolumenControlMinimoEV, Text.EV_NUM_DOUBLE, null, inputVolumenControlMinimo);

        if(datosHidraulicoCorrida.getPenalidadControlMinimo() != null ) { inputPenalidadControlMinimo.setText(datosHidraulicoCorrida.getPenalidadControlMinimo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getPenalidadControlMinimo(), "penalidadControlMinimo", inputPenalidadControlMinimoEV, Text.EV_NUM_DOUBLE, null, inputPenalidadControlMinimo);

        inputHayControlCotaMaxima.setSelected(datosHidraulicoCorrida.isHayControldeCotasMaximas());

        if(datosHidraulicoCorrida.getVolumenControlMaximo() != null ) { inputVolumenControlMaximo.setText(datosHidraulicoCorrida.getVolumenControlMaximo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getVolumenControlMaximo(), "volumenControlMaximo", inputVolumenControlMaximoEV, Text.EV_NUM_DOUBLE, null, inputVolumenControlMaximo);

        if(datosHidraulicoCorrida.getPenalidadControlMaximo() != null ) { inputPenalidadControlMaximo.setText(datosHidraulicoCorrida.getPenalidadControlMaximo().getValor(instanteActual).toString()); }
        evForField(datosHidraulicoCorrida.getPenalidadControlMaximo(), "penalidadControlMaximo", inputPenalidadControlMaximoEV, Text.EV_NUM_DOUBLE, null, inputPenalidadControlMaximo);


        cambioComportamientoCoefEnergetico();
        cambioControlCotaMaxima();
        cambioControlCotaMinima();
        cambioControlHayReservaEstrategica();
        cambioComportamientoLago();

    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();

        if( inputNombre.getText().trim().equalsIgnoreCase("") ) errores.add("Editor Hidraúlico. Nombre vacío.");

        if(inputComportamientoLago.getValue() == null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Comportamiento Lago vacío.");
        if(inputComportamientoCoefEnergetico.getValue()== null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Comportamiento coef energetico vacío.");
        if(inputBarra.getValue()== null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Barra vacía.");

        if( inputCantModInst.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Cantidad módulos instalados vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantModInst.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Cantidad módulos instalados no es entero.");

        if( inputFactorCompartir.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Factor compartir mínima vacío.");
        if( !UtilStrings.esNumeroDouble(inputFactorCompartir.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Factor compartir no es double.");

        if( inputPotMin.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Potencia mínima vacío.");
        if( !UtilStrings.esNumeroDouble(inputPotMin.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Potencia mínima no es double.");
        if( inputPotMax.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Potencia máxima vacío.");
        if( !UtilStrings.esNumeroDouble(inputPotMax.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Potencia máxima no es double.");

        if( inputRendPotMax.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Rend. Potencia máxima vacío.");
        if( !UtilStrings.esNumeroDouble(inputRendPotMax.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Rend. Potencia máxima no es double.");
        if( inputRendPotMin.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Rend. Potencia mínima vacío.");
        if( !UtilStrings.esNumeroDouble(inputRendPotMin.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Rend. Potencia mínima no es double.");

        // Es responsabilidad del usuario verificar que estos valores esten completos
        //if(inputGenAguasArriba1.getValue() == null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Generador aguas arriba vacío.");
        //if(inputGenAguasAbajo1.getValue() == null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Generador aguas abajo vacío.");

        if( inputCantModIni.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Cantidad módulos inicial vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantModIni.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Cantidad módulos inicial no es entero.");

        if( inputTMedioArreglo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Tiempo medio de arreglo vacío.");
        if( !UtilStrings.esNumeroDouble(inputTMedioArreglo.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Tiempo medio de arreglo no es double.");

        if( inputMantProg.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Mantenimiento programado vacío.");
        if( !UtilStrings.esNumeroEntero(inputMantProg.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Mantenimiento programado no es entero.");

        if( inputDispMedia.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Disponibilidad media vacío.");
        if( !UtilStrings.esNumeroDouble(inputDispMedia.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Disponibilidad media no es double.");
        if( !controlDisponibilidadMedia(inputDispMedia)) errores.add("Editor Eólico: " + inputNombre.getText() + " Disponibilidad media no es double entre 0 y 1.");

        if( inputCostoFijo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Costo fijo vacío.");
        if( !UtilStrings.esNumeroDouble(inputCostoFijo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Costo fijo no es double.");

        if( inputCostoVariable.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Costo variable vacío.");
        if( !UtilStrings.esNumeroDouble(inputCostoVariable.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Costo variable no es double.");

        if( inputEpsilonCaudalErogado.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Epsilon caudal erogado vacío.");
        if( !UtilStrings.esNumeroDouble(inputEpsilonCaudalErogado.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Epsilon caudal erogado no es double.");

        if( inputFuncionesPQ.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Funciones PQ vacío.");

        if( inputVolumenFijo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Volumen fijo vacío.");
        if( !UtilStrings.esNumeroDouble(inputVolumenFijo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Volumen fijo no es double.");

        if( inputQTur1Max.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" qTurMax vacío.");
        if( !UtilStrings.esNumeroDouble(inputQTur1Max.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " qTurMax no es double.");

        if( inputSaltoMinimo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Salto mínimo vacío.");
        if( !UtilStrings.esNumeroDouble(inputSaltoMinimo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Salto mínimo no es double.");


        if( inputCoefEvaporacion.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Coef. evaporación vacío.");
        if( !UtilStrings.esNumeroDouble(inputCoefEvaporacion.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Coef. evaporación no es double.");

        if( inputCotaInunAguasArriba.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Cota aguas arriba vacío.");
        if( !UtilStrings.esNumeroDouble(inputCotaInunAguasArriba.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Cota aguas arriba no es double.");
        if( inputCotaInunAguasAbajo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Cota aguas abajo vacío.");
        if( !UtilStrings.esNumeroDouble(inputCotaInunAguasAbajo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Cota aguas abajo no es double.");

        if(inputAporteProcSim.getValue() == null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Aporte proc Simulación vacío.");
        if(inputAporteProcOpt.getValue() == null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Aporte proc Optimización vacío.");
        if(inputAporteNombre.getValue() == null) errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Aporte nombre vacío.");

        if(inputComportamientoLago.getValue() != null && !inputComportamientoLago.getValue().equalsIgnoreCase(Constantes.HIDROSINLAGO)){
            if( inputValorMinReserva.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Valor reserva minimo vacío.");
            if( !UtilStrings.esNumeroDouble(inputValorMinReserva.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Valor reserva mínimo no es double.");

            if( inputVolReservaEstrategica.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Valor reserva estratégico vacío.");
            if( !UtilStrings.esNumeroDouble(inputVolReservaEstrategica.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Valor reserva estratégico no es double.");

            if( inputVolumenObjetivoVertimiento.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Volumen objetivo vertimiento vacío.");
            if( !UtilStrings.esNumeroDouble(inputVolumenObjetivoVertimiento.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Volumen objetivo vertimiento no es double.");

            if( inputVolumenControlMinimo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Volumen control mínimo vacío.");
            if( !UtilStrings.esNumeroDouble(inputVolumenControlMinimo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Volumen control mínimo no es double.");
            if( inputVolumenControlMaximo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Volumen control máximo vacío.");
            if( !UtilStrings.esNumeroDouble(inputVolumenControlMaximo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Volumen control máximo no es double.");
            if( inputPenalidadControlMaximo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Penalidad control máximo vacío.");
            if( !UtilStrings.esNumeroDouble(inputPenalidadControlMaximo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Penalidad control máximo no es double.");
            if( inputPenalidadControlMinimo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +" Penalidad control mínimo vacío.");
            if( !UtilStrings.esNumeroDouble(inputPenalidadControlMinimo.getText().trim())) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Penalidad control mínimo no es double.");


        }

        if(inputComportamientoCoefEnergetico.getValue() != null && inputComportamientoCoefEnergetico.getValue().equalsIgnoreCase(Constantes.HIDROPOTENCIACAUDAL)){
            if( inputFuncionesPQ.getText().trim().equalsIgnoreCase("") )errores.add("Editor Hidraúlico: " + inputNombre.getText() +"Dir Funciones PQ vacío.");
        }
        if(datosHidraulicoCorrida != null) {
            if (datosHidraulicoCorrida.getfCoAA() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion cota aguas abajo vacía.");
            if (datosHidraulicoCorrida.getfCoVo() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion cota volumen vacía.");
            if (datosHidraulicoCorrida.getfVoCo() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion volumen cota vacía.");
            if (datosHidraulicoCorrida.getfEvaporacion() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion evaporación vacía.");
            if (datosHidraulicoCorrida.getfFiltracion() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion filtración vacía.");
            if (datosHidraulicoCorrida.getfQEroMin() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion erogado mínimo vacía.");
            if (datosHidraulicoCorrida.getfQVerM() == null)
                errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion vertimiento máximo vacía.");
        } else {
            //Es un hidraulico nuevo hay que preguntar a los controladores si se agregaron datos
            if(fCotaAguasAbajoController.controlDatosCompletos())  errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion cota aguas abajo vacía.");
            if(fCoVoController.controlDatosCompletos())  errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion cota volumen vacía.");
            if(fVoCoController.controlDatosCompletos()) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion volumen cota vacía.");
            if(fEvaporacionController.controlDatosCompletos())  errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion evaporación vacía.");
            if(fFiltracionController.controlDatosCompletos())  errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion filtración vacía.");
            if(fQEroMinController.controlDatosCompletos()) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion erogado mínimo vacía.");
            if(fQVerMaxController.controlDatosCompletos()) errores.add("Editor Hidraúlico: " + inputNombre.getText() + " Funcion vertimiento máximo vacía.");
        }

        ArrayList<String> erroresVE = variableDeEstadoController.controlDatosCompletos();

        if((inputComportamientoLago.getValue().equalsIgnoreCase(Constantes.HIDROCONLAGO) || inputComportamientoLago.getValue().equalsIgnoreCase(Constantes.HIDROSINLAGOENOPTIM)) && erroresVE.size() > 0 )
        {
            errores.addAll(erroresVE);
        }

        return errores;
    }
}
