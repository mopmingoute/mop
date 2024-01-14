/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorCentralAcumulacionController is part of MOP.
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
import database.MongoDBHandler;
import datatypes.DatosCorrida;
import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosVariableEstado;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.*;

import static utilitarios.Constantes.COMPPASO;
import static utilitarios.Constantes.TERVARENTERASYVARESTADO;

public class EditorCentralAcumulacionController extends GeneralidadesController {
    @FXML private VBox vBoxVE;
    @FXML private JFXComboBox<String> inputCompPaso;
    @FXML private JFXButton inputCompPasoEV;

    @FXML private GridPane gridPane;

    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;
    @FXML private JFXTextField inputCantModInst;
    @FXML private JFXButton inputCantModInstEV;
    @FXML private JFXTextField inputFactorUso;
    @FXML private JFXButton inputFactorUsoEV;
    @FXML private JFXTextField inputPotMin;
    @FXML private JFXButton inputPotMinEV;
    @FXML private JFXComboBox<String> inputPotMinUnidad;
    @FXML private JFXTextField inputPotMax;
    @FXML private JFXButton inputPotMaxEV;
    @FXML private JFXComboBox<String> inputPotMaxUnidad;
    @FXML private JFXTextField inputRendInyectado;
    @FXML private JFXButton inputRendInyectadoEV;
    @FXML private JFXTextField inputRendAlmacenado;
    @FXML private JFXButton inputRendAlmacenadoEV;
    @FXML private JFXTextField inputPotAlmacenadaMin;
    @FXML private JFXButton inputPotAlmacenadaMinEV;
    @FXML private JFXComboBox<String> inputPotAlmacenadaMinUnidad;
    @FXML private JFXTextField inputPotAlmacenadaMax;
    @FXML private JFXButton inputPotAlmacenadaMaxEV;
    @FXML private JFXComboBox<String> inputPotAlmacenadaMaxUnidad;
    @FXML private JFXTextField inputEnergAlmacenadaMax;
    @FXML private JFXButton inputEnergAlmacenadaMaxEV;
    @FXML private JFXComboBox<String> inputEnergAlmacenadaMaxUnidad;
    @FXML private JFXTextField inputEnergIniPaso;
    @FXML private JFXButton inputEnergIniPasoEV;
    @FXML private JFXCheckBox inputHayPotObligatoria;
    @FXML private JFXButton inputHayPotObligatoriaEV;
    @FXML private JFXTextField inputCostoFallaPotObligatoria;
    @FXML private JFXButton inputCostoFallaPotObligatoriaEV;
    @FXML private JFXComboBox<String> inputCostoFallaPotObligatoriaUnidad;
    //pot obligatoria por poste
    private Hashtable<Integer, JFXTextField> inputsPotObligatoria = new Hashtable<>();

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

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;


    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private DatosCorrida datosCorrida;
    private DatosAcumuladorCorrida datosAcumuladorCorrida;
    private boolean edicion;
    private VariableDeEstadoController variableDeEstadoController;
    private ListaCentralesAcumulacionController listaCentralesAcumulacionController;
    private Integer cantPostes;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;


    public EditorCentralAcumulacionController(DatosCorrida datosCorrida, ListaCentralesAcumulacionController listaCentralesAcumulacionController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaCentralesAcumulacionController = listaCentralesAcumulacionController;
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
    }

    public EditorCentralAcumulacionController(DatosCorrida datosCorrida, DatosAcumuladorCorrida datosAcumuladorCorrida, ListaCentralesAcumulacionController listaCentralesAcumulacionController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosAcumuladorCorrida = datosAcumuladorCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaCentralesAcumulacionController = listaCentralesAcumulacionController;
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
    }

    public void initialize(){

        inputCompPaso.getItems().addAll(Text.COMP_PASO_VALS);
//        inputCompPaso.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.ACUCIERRAPASO, Constantes.ACUMULTIPASO, Constantes.ACUBALANCECRONOLOGICO)));

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getSelectionModel().selectFirst();

        inputPotMinUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMinUnidad.getSelectionModel().selectFirst();
        inputPotMaxUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMaxUnidad.getSelectionModel().selectFirst();

        inputTMedioArregloUnidad.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidad.getSelectionModel().selectFirst();

        inputPotAlmacenadaMinUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotAlmacenadaMinUnidad.getSelectionModel().selectFirst();

        inputPotAlmacenadaMaxUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotAlmacenadaMaxUnidad.getSelectionModel().selectFirst();

        inputEnergAlmacenadaMaxUnidad.getItems().add(Text.UNIDAD_MWH);
        inputEnergAlmacenadaMaxUnidad.getSelectionModel().selectFirst();

        inputCostoFallaPotObligatoriaUnidad.getItems().add(Text.UNIDAD_USD_MWH);
        inputCostoFallaPotObligatoriaUnidad.getSelectionModel().selectFirst();

//        if(comboCompPaso.getValue().equals("multiPaso")){
            try{
                if(edicion && datosAcumuladorCorrida.getVarsEstado() != null){//TODO: unidad
                    System.out.println("---->"+datosAcumuladorCorrida.getVarsEstado());
                    variableDeEstadoController = new VariableDeEstadoController(Text.VE_ENERGIA_ACUMULADA_LABEL, Text.VE_ENERGIA_ACUMULADA, 0, datosAcumuladorCorrida.getVarsEstado().get(Text.VE_ENERGIA_ACUMULADA), "???");
                }else {
                    variableDeEstadoController = new VariableDeEstadoController(Text.VE_ENERGIA_ACUMULADA_LABEL,Text.VE_ENERGIA_ACUMULADA, 0, "???");
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("VariableDeEstado.fxml"));
                loader.setController(variableDeEstadoController);
                AnchorPane newLoadedPane = loader.load();
                vBoxVE.getChildren().add(newLoadedPane);
            }catch(Exception e){
                e.printStackTrace();
            }
//        }


        renderInputsPotObligatoria();


        if(edicion){
            unloadData();
        }else{
            evForField(null, "compPaso", inputCompPasoEV, Text.EV_VAR, Text.COMP_PASO_VALS, inputCompPaso);
            evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);
            evForField(null, "factorUso", inputFactorUsoEV, Text.EV_NUM_DOUBLE, null, inputFactorUso);
            evForField(null, "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);
            evForField(null, "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);
            evForField(null, "rendInyectado", inputRendInyectadoEV, Text.EV_NUM_DOUBLE, null, inputRendInyectado);
            evForField(null, "rendAlmacenado", inputRendAlmacenadoEV, Text.EV_NUM_DOUBLE, null,inputRendAlmacenado);
            evForField(null, "potAlmacenadaMin", inputPotAlmacenadaMinEV, Text.EV_NUM_DOUBLE, null, inputPotAlmacenadaMin);
            evForField(null, "potAlmacenadaMax", inputPotAlmacenadaMaxEV, Text.EV_NUM_DOUBLE, null, inputPotAlmacenadaMax);
            evForField(null, "energAlmacMax", inputEnergAlmacenadaMaxEV, Text.EV_NUM_DOUBLE, null, inputEnergAlmacenadaMax);
            evForField(null, "energIniPaso", inputEnergIniPasoEV, Text.EV_NUM_DOUBLE, null, inputEnergIniPaso);
            evForField(null, "hayPotObligatoria", inputHayPotObligatoriaEV, Text.EV_BOOL, null, inputHayPotObligatoria);
            evForField(null, "costoFallaPotObligatoria", inputCostoFallaPotObligatoriaEV, Text.EV_NUM_DOUBLE, null, inputCostoFallaPotObligatoria);
            evForField(null, "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);
            evForField(null, "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);
            evForField(null, "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);
            evForField(null, "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);
            evForField(null, "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);
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
        inputAceptar.setOnAction(actionEvent -> {
            controlHayCambiosEnVarableEstado();
            if(editoVariables) {
                ArrayList<String> errores = controlDatosCompletos();
                if(errores.size() == 0 || errores == null) {
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosAcumuladorCorrida.getNombre().trim():"";
                    if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getAcumuladores().getOrdenCargaXML(), edicion)){
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaCentralesAcumulacionController.refresh();
                        listaCentralesAcumulacionController.actualizarLineaDeEntrada();
                        ((Stage) inputAceptar.getScene().getWindow()).close();
                        editoVariables = false;
                    }
                } else {
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
                if(inputNombre.getText().trim().equals("Acumulador clonado")){
                    setLabelMessageTemporal("Cambie el nombre del acumulador clonado", TipoInfo.FEEDBACK);
                }else{
                    ((Stage)inputCancelar.getScene().getWindow()).close();
                }
            }
        });
        inputCancelar.setOnAction(actionEvent -> {
            controlHayCambiosEnVarableEstado();
            if(editoVariables){
                mostrarMensajeConfirmacion(Text.MSG_CONF_CERRAR_SIN_GUARDAR,2 );
            }
            else{
                editoVariables = false;
                cerrarVentana();
            }
        });

        btnGuardarEnBiblioteca.setDisable(Manejador.user == null);
        btnGuardarEnBiblioteca.setOnAction(actionEvent -> Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_ACUMULADOR_TEXT, datosAcumuladorCorrida, datosCorrida, this, inputNombre.getText()));

        //Control de cambios:

        inputCompPaso.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputFactorUso.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendInyectado.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendAlmacenado.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotAlmacenadaMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotAlmacenadaMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputEnergAlmacenadaMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputEnergIniPaso.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputHayPotObligatoria.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFallaPotObligatoria.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });


        inputCantModIni.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDispMedia.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTMedioArreglo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputMantProg.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFijo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoVariable.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        controlHayCambiosEnEvoluciones();

        editoVariables = false;


    }

    private void controlHayCambiosEnEvoluciones() {
        for (String clave : evsPorNombre.keySet()) {
            if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
                editoVariables = true;
            }
        }

    }

    private void controlHayCambiosEnVarableEstado() {
        if(variableDeEstadoController.isEditoVariables()) {editoVariables = true;}
    }

    private  void cerrarVentana(){
        if(copiadoPortapapeles){
            listaCentralesAcumulacionController.borrarAcumulador(datosAcumuladorCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }


    private void renderInputsPotObligatoria(){
        for(int i=0;i<cantPostes;i++){
            JFXTextField input = new JFXTextField();
            input.textProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
            input.setMinWidth(40);
            input.setPrefWidth(40);
            input.setMaxWidth(40);
            GridPane.setMargin(input, new Insets(0, 0, 0, 5));
            int row = i < 12 ? 12 : 13;
            int col = (i % 12)/2 + 1;
            System.out.println(i + "->(" + row + "," + col + ")");
            gridPane.add(input, col, row);
            if(i % 2 != 0){
                GridPane.setHalignment(input, HPos.RIGHT);
                GridPane.setMargin(input, new Insets(0, 5, 0, 0));
            }
            inputsPotObligatoria.put(i, input);
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
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

        if(codigoMensaje == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaCentralesAcumulacionController.unloadData();//Cambia color participantes incompletos
            listaCentralesAcumulacionController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigoMensaje == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }
    }
    /**
     *  Método para cargar los datos en el DatosAcumuladorCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-CENTRAL ACUMULACIÓN");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();
        Evolucion<Integer> cantModInst = EVController.loadEVsegunTipo(inputCantModInst, Text.EV_NUM_INT, evsPorNombre, "cantModInst");
        Evolucion<Double> potMin = EVController.loadEVsegunTipo(inputPotMin, Text.EV_NUM_DOUBLE, evsPorNombre, "potMin");
//        inputPotMinUnidad
        Evolucion<Double> potMax = EVController.loadEVsegunTipo(inputPotMax, Text.EV_NUM_DOUBLE, evsPorNombre, "potMax");
//        inputPotMaxUnidad;
        Evolucion<Double> potAlmacenadaMin = EVController.loadEVsegunTipo(inputPotAlmacenadaMin, Text.EV_NUM_DOUBLE, evsPorNombre, "potAlmacenadaMin");
//        inputPotAlmacenadaMinUnidad
        Evolucion<Double> potAlmacenadaMax = EVController.loadEVsegunTipo(inputPotAlmacenadaMax, Text.EV_NUM_DOUBLE, evsPorNombre, "potAlmacenadaMax");
//        inputPotAlmacenadaMaxUnidad
        Evolucion<Double> rendIny = EVController.loadEVsegunTipo(inputRendInyectado, Text.EV_NUM_DOUBLE, evsPorNombre, "rendInyectado");
        Evolucion<Double> rendAlmac = EVController.loadEVsegunTipo(inputRendAlmacenado, Text.EV_NUM_DOUBLE, evsPorNombre, "rendAlmacenado");
        Evolucion<Double> factorUso = EVController.loadEVsegunTipo(inputFactorUso, Text.EV_NUM_DOUBLE, evsPorNombre, "factorUso");

        Integer cantModIni = 0;
        if (UtilStrings.esNumeroEntero(inputCantModIni.getText())) { Integer.parseInt(inputCantModIni.getText()); }

        Evolucion<Double> dispMedia = EVController.loadEVsegunTipo(inputDispMedia, Text.EV_NUM_DOUBLE, evsPorNombre, "dispMedia");
        Evolucion<Double> tMedioArreglo = EVController.loadEVsegunTipo(inputTMedioArreglo, Text.EV_NUM_DOUBLE, evsPorNombre, "tMedioArreglo");
//        inputTMedioArregloUnidad;
        Evolucion<Integer> mantProgramado = EVController.loadEVsegunTipo(inputMantProg, Text.EV_NUM_INT, evsPorNombre, "mantProg");
        Evolucion<Double> costoFijo = EVController.loadEVsegunTipo(inputCostoFijo, Text.EV_NUM_DOUBLE, evsPorNombre, "costoFijo");
        Evolucion<Double> costoVariable = EVController.loadEVsegunTipo(inputCostoVariable, Text.EV_NUM_DOUBLE, evsPorNombre, "costoVariable");
        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        Evolucion<Boolean> hayPotObligatoria = EVController.loadEVsegunTipo(inputHayPotObligatoria, Text.EV_BOOL, evsPorNombre, "hayPotObligatoria");
        Evolucion<Double> costoFallaPotOblig = EVController.loadEVsegunTipo(inputCostoFallaPotObligatoria, Text.EV_NUM_DOUBLE, evsPorNombre, "costoFallaPotObligatoria");

//        inputCostoFallaPotObligatoriaUnidad
        double[] potOblig = new double[cantPostes];
        for(Integer key : inputsPotObligatoria.keySet()){
            if(UtilStrings.esNumeroDouble(inputsPotObligatoria.get(key).getText())) {
                potOblig[key] = Double.parseDouble(inputsPotObligatoria.get(key).getText()); }
        }
//        Hashtable<String, DatosVariableEstado> varsEstado;
        Evolucion<Double> energAlmacMax = EVController.loadEVsegunTipo(inputEnergAlmacenadaMax, Text.EV_NUM_DOUBLE, evsPorNombre, "energAlmacMax");
//        inputEnergAlmacenadaMaxUnidad
        Evolucion<Double> energIniPaso = EVController.loadEVsegunTipo(inputEnergIniPaso, Text.EV_NUM_DOUBLE, evsPorNombre, "energIniPaso");
        Hashtable<String,Evolucion<String>> valoresComportamientos = new Hashtable<>();
        Evolucion<String> compPaso = EVController.loadEVsegunTipo(inputCompPaso, Text.EV_VAR, evsPorNombre, "compPaso");
        valoresComportamientos.put(COMPPASO, compPaso);

        Hashtable<String,DatosVariableEstado> varsEstado = new Hashtable<>();
//        if(inputCompPaso.getValue().equalsIgnoreCase(Constantes.ACUMULTIPASO)) {
        // TODO: 06/08/2021 VEs?
//            DatosVariableEstado datosVariableEstado = variableDeEstadoController.getDatosVariableEstado();
//            varsEstado.put(datosVariableEstado.getNombre(), datosVariableEstado);
//        }

        if(edicion){
            String nombreViejo = datosAcumuladorCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getAcumuladores().getAcumuladores().remove(nombreViejo);
                int pos = datosCorrida.getAcumuladores().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getAcumuladores().getOrdenCargaXML().set(pos, nombre);
                pos = datosCorrida.getAcumuladores().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getAcumuladores().getListaUtilizados().set(pos, nombre);

            }
            datosAcumuladorCorrida.setNombre(nombre);
            datosAcumuladorCorrida.setBarra(barra);
            datosAcumuladorCorrida.setCantModInst(cantModInst);
            datosAcumuladorCorrida.setPotMin(potMin);
            datosAcumuladorCorrida.setPotMax(potMax);
            datosAcumuladorCorrida.setPotAlmacenadaMin(potAlmacenadaMin);
            datosAcumuladorCorrida.setPotAlmacenadaMax(potAlmacenadaMax);
            datosAcumuladorCorrida.setRendIny(rendIny);
            datosAcumuladorCorrida.setRendAlmac(rendAlmac);
            datosAcumuladorCorrida.setFactorUso(factorUso);
            datosAcumuladorCorrida.setCantModIni(cantModIni);
            datosAcumuladorCorrida.setDispMedia(dispMedia);
            datosAcumuladorCorrida.settMedioArreglo(tMedioArreglo);
            datosAcumuladorCorrida.setMantProgramado(mantProgramado);
            datosAcumuladorCorrida.setCostoFijo(costoFijo);
            datosAcumuladorCorrida.setCostoVariable(costoVariable);
            datosAcumuladorCorrida.setSalDetallada(salidaDetallada);
            datosAcumuladorCorrida.setHayPotObligatoria(hayPotObligatoria);
            datosAcumuladorCorrida.setCostoFallaPotOblig(costoFallaPotOblig);
            datosAcumuladorCorrida.setPotOblig(potOblig);
            datosAcumuladorCorrida.setEnergAlmacMax(energAlmacMax);
            datosAcumuladorCorrida.setEnergIniPaso(energIniPaso);
            datosAcumuladorCorrida.setValoresComportamientos(valoresComportamientos);
            datosAcumuladorCorrida.setVarsEstado(varsEstado);
        }else {
            datosAcumuladorCorrida = new DatosAcumuladorCorrida(nombre, null, barra, cantModInst, potMin, potMax, potAlmacenadaMin,
                                                                potAlmacenadaMax, energAlmacMax, rendIny, rendAlmac, cantModIni,
                                                                dispMedia, tMedioArreglo, salidaDetallada, mantProgramado,
                                                                costoFijo, costoVariable, varsEstado, factorUso, energIniPaso,
                                                                salidaDetallada, hayPotObligatoria, costoFallaPotOblig, potOblig);
            datosAcumuladorCorrida.setValoresComportamientos(valoresComportamientos);
            datosCorrida.getAcumuladores().getListaUtilizados().add(nombre);
            datosCorrida.getAcumuladores().getOrdenCargaXML().add(nombre);

        }
        datosCorrida.getAcumuladores().getAcumuladores().put(nombre, datosAcumuladorCorrida);
    }

    /**
     * Método para obtener los datos del DatosAcumuladorCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-CENTRAL ACUMULACIÓN");
        inputNombre.setText(datosAcumuladorCorrida.getNombre());
//        inputBarra.getSelectionModel().select(datosAcumuladorCorrida.getBarra());
        inputBarra.getSelectionModel().select(Text.BARRA_1);//uninodal hardcodeado
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        if(datosAcumuladorCorrida.getCantModInst() != null ) { inputCantModInst.setText(datosAcumuladorCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);

        if(datosAcumuladorCorrida.getPotMin() != null) { inputPotMin.setText(datosAcumuladorCorrida.getPotMin().getValor(instanteActual).toString());}
        evForField(datosAcumuladorCorrida.getPotMin(), "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);
//        inputPotMinUnidad.set;
        if(datosAcumuladorCorrida.getPotMax() != null) { inputPotMax.setText(datosAcumuladorCorrida.getPotMax().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getPotMax(), "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);
//        inputPotMaxUnidad.set;
        if(datosAcumuladorCorrida.getPotAlmacenadaMin() != null) { inputPotAlmacenadaMin.setText(datosAcumuladorCorrida.getPotAlmacenadaMin().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getPotAlmacenadaMin(), "potAlmacenadaMin", inputPotAlmacenadaMinEV, Text.EV_NUM_DOUBLE, null, inputPotAlmacenadaMin);
//        inputPotAlmacenadaMinUnidad
        if(datosAcumuladorCorrida.getPotAlmacenadaMax() != null) { inputPotAlmacenadaMax.setText(datosAcumuladorCorrida.getPotAlmacenadaMax().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getPotAlmacenadaMax(), "potAlmacenadaMax", inputPotAlmacenadaMaxEV, Text.EV_NUM_DOUBLE, null, inputPotAlmacenadaMax);
//        inputPotAlmacenadaMaxUnidad
        if(datosAcumuladorCorrida.getRendIny() != null) {  inputRendInyectado.setText(datosAcumuladorCorrida.getRendIny().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getRendIny(), "rendInyectado", inputRendInyectadoEV, Text.EV_NUM_DOUBLE, null, inputRendInyectado);

        if(datosAcumuladorCorrida.getRendAlmac() != null) { inputRendAlmacenado.setText(datosAcumuladorCorrida.getRendAlmac().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getRendAlmac(), "rendAlmacenado", inputRendAlmacenadoEV, Text.EV_NUM_DOUBLE, null, inputRendAlmacenado);

        if(datosAcumuladorCorrida.getFactorUso() != null) { inputFactorUso.setText(datosAcumuladorCorrida.getFactorUso().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getFactorUso()
                , "factorUso", inputFactorUsoEV, Text.EV_NUM_DOUBLE, null, inputFactorUso);

        inputCantModIni.setText(datosAcumuladorCorrida.getCantModIni().toString());

        if(datosAcumuladorCorrida.getDispMedia() != null) { inputDispMedia.setText(datosAcumuladorCorrida.getDispMedia().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);

        if(datosAcumuladorCorrida.gettMedioArreglo() != null) {  inputTMedioArreglo.setText(datosAcumuladorCorrida.gettMedioArreglo().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);
//        inputTMedioArregloUnidad.set;
        if(datosAcumuladorCorrida.getMantProgramado() != null) {  inputMantProg.setText(datosAcumuladorCorrida.getMantProgramado().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);

        if(datosAcumuladorCorrida.getCostoFijo() != null) {  inputCostoFijo.setText(datosAcumuladorCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosAcumuladorCorrida.getCostoVariable() != null) {  inputCostoVariable.setText(datosAcumuladorCorrida.getCostoVariable().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getCostoVariable(), "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);

        inputSalidaDetallada.setSelected(datosAcumuladorCorrida.isSalDetallada());

        if(datosAcumuladorCorrida.getHayPotObligatoria() != null) {  inputHayPotObligatoria.setSelected(datosAcumuladorCorrida.getHayPotObligatoria().getValor(instanteActual)); }
        evForField(datosAcumuladorCorrida.getHayPotObligatoria(), "hayPotObligatoria", inputHayPotObligatoriaEV, Text.EV_BOOL, null, inputHayPotObligatoria);

        if(datosAcumuladorCorrida.getCostoFallaPotOblig() != null) {  inputCostoFallaPotObligatoria.setText(datosAcumuladorCorrida.getCostoFallaPotOblig().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getCostoFallaPotOblig(), "costoFallaPotObligatoria", inputCostoFallaPotObligatoriaEV, Text.EV_NUM_DOUBLE, null, inputCostoFallaPotObligatoria);

        double[] potOblig = datosAcumuladorCorrida.getPotOblig();
        for(Integer key : inputsPotObligatoria.keySet()){
            inputsPotObligatoria.get(key).setText(String.valueOf(potOblig[key]));
        }

        if(datosAcumuladorCorrida.getEnergAlmacMax() != null) { inputEnergAlmacenadaMax.setText(datosAcumuladorCorrida.getEnergAlmacMax().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getEnergAlmacMax(), "energAlmacMax", inputEnergAlmacenadaMaxEV, Text.EV_NUM_DOUBLE, null, inputEnergAlmacenadaMax);
//        inputEnergAlmacenadaMaxUnidad
        if(datosAcumuladorCorrida.getEnergIniPaso() != null) { inputEnergIniPaso.setText(datosAcumuladorCorrida.getEnergIniPaso().getValor(instanteActual).toString()); }
        evForField(datosAcumuladorCorrida.getEnergIniPaso(), "energIniPaso", inputEnergIniPasoEV, Text.EV_NUM_DOUBLE, null, inputEnergIniPaso);

        if(datosAcumuladorCorrida.getValoresComportamientos().get(COMPPASO) != null) { inputCompPaso.getSelectionModel().select(datosAcumuladorCorrida.getValoresComportamientos().get(COMPPASO).getValor(instanteActual)); }
        evForField(datosAcumuladorCorrida.getValoresComportamientos().get(COMPPASO), "compPaso", inputCompPasoEV, Text.EV_VAR, Text.COMP_PASO_VALS, inputCompPaso);
    }
    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> errores = new ArrayList<>();

        if( inputCompPaso.getValue()== null ) { errores.add("Editor Central Acumulación: "+ inputNombre.getText() + " CompPaso vacío."); }
        if( inputNombre.getText().trim().equalsIgnoreCase("") )errores.add("Editor Central Acumulación. Nombre vacío.");
        if( inputBarra.getValue() == null) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Barra vacío.") ;
        if( !UtilStrings.esNumeroDouble(inputFactorUso.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Factor Uso no es double.");
        if( !UtilStrings.esNumeroEntero(inputCantModInst.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Cantidad módulos instalados no es entero.");
        if( !UtilStrings.esNumeroDouble(inputPotMin.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Potencia mínima no es double.");
        if( !UtilStrings.esNumeroDouble(inputPotMax.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Potencia máxima no es double.");

        if( !UtilStrings.esNumeroDouble(inputRendInyectado.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Rend Inyectado no es double.");
        if( !UtilStrings.esNumeroDouble(inputRendAlmacenado.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Rend Almacenado no es double.");

        if( !UtilStrings.esNumeroDouble(inputPotAlmacenadaMin.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Potencia almacenada Min no es double.");
        if( !UtilStrings.esNumeroDouble(inputPotAlmacenadaMax.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Potencia almacenada Max no es double.");
        if( !UtilStrings.esNumeroDouble(inputEnergAlmacenadaMax.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Energia almacenada Max no es double.");
        if( !UtilStrings.esNumeroDouble(inputEnergIniPaso.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Energía Ini Paso no es double.");
        if( !UtilStrings.esNumeroDouble(inputCostoFallaPotObligatoria.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Costo falla potencia obligatoria no es double.");

        inputsPotObligatoria.forEach((k,v) ->  {
            if(!UtilStrings.esNumeroDouble(v.getText())){
                errores.add( "Editor Central Acumulación:  "+ inputNombre.getText() + " Potencia obligaotira no es dato numérico");
            }
        });

        if( !UtilStrings.esNumeroEntero(inputCantModIni.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Cantidad módulos inicial no es entero.");
        if( !UtilStrings.esNumeroDouble(inputTMedioArreglo.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Tiempo medio de arreglo no es double.");
        if( !UtilStrings.esNumeroEntero(inputMantProg.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Mantenimiento programado no es entero.");
        if( !UtilStrings.esNumeroDouble(inputDispMedia.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Disponibilidad media no es double.");
        if( !controlDisponibilidadMedia(inputDispMedia)) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Disponibilidad media no es double entre 0 y 1.");
        if( !UtilStrings.esNumeroDouble(inputMantProg.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Mant Programado no es double.");
        if( !UtilStrings.esNumeroDouble(inputCostoFijo.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Costo fijo no es double.");
        if( !UtilStrings.esNumeroDouble(inputCostoVariable.getText().trim())) errores.add("Editor Central Acumulación: " + inputNombre.getText() + " Costo variable no es double.");

        if(inputCompPaso.getValue() != null && inputCompPaso.getValue().equals(Constantes.ACUMULTIPASO) && variableDeEstadoController.controlDatosCompletos().size() > 0 )  errores.add("Editor Central Acumulación. Variables De Estado vacía.");
        return errores;
    }


}
