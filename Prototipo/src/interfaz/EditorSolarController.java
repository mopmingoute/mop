/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorSolarController is part of MOP.
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
import datatypes.DatosCorrida;
import datatypes.DatosFotovoltaicoCorrida;
import datatypes.DatosVariableAleatoria;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;
import tiempo.Evolucion;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class EditorSolarController extends GeneralidadesController {
    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;
    @FXML private JFXTextField inputCantModInst;
    @FXML private JFXButton inputCantModInstEV;
    @FXML private JFXTextField inputPotMin;
    @FXML private JFXButton inputPotMinEV;
    @FXML private JFXComboBox<String> inputPotMinUnidad;
    @FXML private JFXTextField inputPotMax;
    @FXML private JFXButton inputPotMaxEV;
    @FXML private JFXComboBox<String> inputPotMaxUnidad;
    @FXML private JFXComboBox<String> inputFactorProcOpt;
    @FXML private JFXComboBox<String> inputFactorProcSim;
    @FXML private JFXComboBox<String> inputFactorNombre;
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
    private DatosFotovoltaicoCorrida datosFotovoltaicoCorrida;
    private boolean edicion;
    private ListaSolaresController listaSolaresController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorSolarController(DatosCorrida datosCorrida, ListaSolaresController listaSolaresController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        this.listaSolaresController = listaSolaresController;
        copiadoPortapapeles = false;
    }

    public EditorSolarController(DatosCorrida datosCorrida, DatosFotovoltaicoCorrida datosFotovoltaicoCorrida, ListaSolaresController listaSolaresController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosFotovoltaicoCorrida = datosFotovoltaicoCorrida;
        edicion = true;
        this.listaSolaresController = listaSolaresController;
        this.copiadoPortapapeles = copiadoPortapapeles;
    }

    @FXML
    public void initialize(){

        Manejador.popularListaNombresVA(datosCorrida, inputFactorProcSim, inputFactorProcOpt, inputFactorNombre);

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getSelectionModel().selectFirst();

        inputPotMinUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMinUnidad.getSelectionModel().selectFirst();
        inputPotMaxUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMaxUnidad.getSelectionModel().selectFirst();

        inputTMedioArregloUnidad.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidad.getSelectionModel().selectFirst();

        if(edicion){
            unloadData();
        }else{
            evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);
            evForField(null, "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null,inputPotMin);
            evForField(null, "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);
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
            if(editoVariables) {
            ArrayList<String> errores = controlDatosCompletos();
            if(errores.size() == 0 || errores == null) {
                String nombreNuevo = inputNombre.getText().trim();
                String nombreViejo = (edicion) ? datosFotovoltaicoCorrida.getNombre().trim():"";
                if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getFotovoltaicos().getOrdenCargaXML(), edicion)){
                    setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                } else if(nombreNuevo.contains(" ")){
                    setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                } else {
                    loadData();
                    listaSolaresController.refresh();
                    listaSolaresController.actualizarLineaDeEntrada();
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
                if(inputNombre.getText().trim().equals("Fotovoltaico clonado")){
                    setLabelMessageTemporal("Cambie el nombre del eólico clonado", TipoInfo.FEEDBACK);
                }else{
                    ((Stage)inputCancelar.getScene().getWindow()).close();
                }
            }


        });
        inputCancelar.setOnAction(actionEvent -> {
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
            ArrayList<String> bloquesConError = controlDatosCompletos();
            if(bloquesConError.size() == 0 || bloquesConError == null) {
                 Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_SOLAR_TEXT, datosFotovoltaicoCorrida, datosCorrida, this, inputNombre.getText());
            }else{
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });

        //Control de cambios:

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputFactorProcSim.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputFactorProcOpt.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
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

    private void cerrarVentana() {
        if(copiadoPortapapeles){
            listaSolaresController.borrarSolar(datosFotovoltaicoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaSolaresController.unloadData();//Cambia color participantes incompletos
            listaSolaresController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    };

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

    /**
     *  Método para cargar los datos en el DatosFotovoltaicoCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-SOLAR");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();

        Evolucion<Integer> cantModInst = null;
        if(UtilStrings.esNumeroEntero(inputCantModInst.getText())) { cantModInst = EVController.loadEVsegunTipo(inputCantModInst, Text.EV_NUM_INT,  evsPorNombre,"cantModInst" );}

        Evolucion<Double> potMin = null;
        if(UtilStrings.esNumeroDouble(inputPotMin.getText())) { potMin = EVController.loadEVsegunTipo(inputPotMin, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMin" );}

        Evolucion<Double> potMax = null;
        if(UtilStrings.esNumeroDouble(inputPotMax.getText())) { potMax = EVController.loadEVsegunTipo(inputPotMax, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMax" );}

        DatosVariableAleatoria factor = new DatosVariableAleatoria(inputFactorProcOpt.getValue(), inputFactorProcSim.getValue(), inputFactorNombre.getValue());

        Integer cantModIni = null;
        if(UtilStrings.esNumeroEntero(inputCantModIni.getText())) {cantModIni = Integer.parseInt(inputCantModIni.getText());}

        Evolucion<Double> dispMedia = null;
        if(UtilStrings.esNumeroDouble(inputDispMedia.getText())){ dispMedia = EVController.loadEVsegunTipo(inputDispMedia, Text.EV_NUM_DOUBLE,  evsPorNombre,"dispMedia" ); }

        Evolucion<Double> tMedioArreglo = null;
        if(UtilStrings.esNumeroDouble(inputTMedioArreglo.getText())){ tMedioArreglo = EVController.loadEVsegunTipo(inputTMedioArreglo, Text.EV_NUM_DOUBLE,  evsPorNombre,"tMedioArreglo" ); }

        Evolucion<Integer> mantProgramado = null;
        if(UtilStrings.esNumeroEntero(inputMantProg.getText())){mantProgramado = EVController.loadEVsegunTipo(inputMantProg, Text.EV_NUM_INT,  evsPorNombre,"mantProg" ); }

        Evolucion<Double> costoFijo = null;
        if(UtilStrings.esNumeroDouble(inputCostoFijo.getText())) {costoFijo = EVController.loadEVsegunTipo(inputCostoFijo, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoFijo" ); }

        Evolucion<Double> costoVariable =  null;
        if(UtilStrings.esNumeroDouble(inputCostoVariable.getText())) { costoVariable = EVController.loadEVsegunTipo(inputCostoVariable, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoVariable" ); }

        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        if(edicion){
            String nombreViejo = datosFotovoltaicoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getFotovoltaicos().getFotovoltaicos().remove(nombreViejo);
                datosCorrida.getFotovoltaicos().getFotovoltaicos().put(nombre,datosFotovoltaicoCorrida);

                int ind = datosCorrida.getFotovoltaicos().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getFotovoltaicos().getOrdenCargaXML().set(ind,nombre);

                ind = datosCorrida.getFotovoltaicos().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getFotovoltaicos().getListaUtilizados().set(ind,nombre);

            }
            datosFotovoltaicoCorrida.setNombre(nombre);
            datosFotovoltaicoCorrida.setPropietario("UTE");
            datosFotovoltaicoCorrida.setBarra(barra);
            datosFotovoltaicoCorrida.setCantModInst(cantModInst);
            datosFotovoltaicoCorrida.setPotMin(potMin);
            datosFotovoltaicoCorrida.setPotMax(potMax);
            datosFotovoltaicoCorrida.setFactor(factor);
            datosFotovoltaicoCorrida.setCantModIni(cantModIni);
            datosFotovoltaicoCorrida.setDispMedia(dispMedia);
            datosFotovoltaicoCorrida.settMedioArreglo(tMedioArreglo);
            datosFotovoltaicoCorrida.setMantProgramado(mantProgramado);
            datosFotovoltaicoCorrida.setCostoFijo(costoFijo);
            datosFotovoltaicoCorrida.setCostoVariable(costoVariable);
            datosFotovoltaicoCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosFotovoltaicoCorrida = new DatosFotovoltaicoCorrida(nombre, "UTE", barra, cantModInst, potMin, potMax, factor, cantModIni, dispMedia, tMedioArreglo,
                    salidaDetallada, mantProgramado, costoFijo, costoVariable);
            datosCorrida.getFotovoltaicos().getListaUtilizados().add(nombre);
            datosCorrida.getFotovoltaicos().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getFotovoltaicos().getFotovoltaicos().put(nombre, datosFotovoltaicoCorrida);

    }

    /**
     * Método para obtener los datos del DatosFotovoltaicoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-SOLAR");
        long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        inputNombre.setText(datosFotovoltaicoCorrida.getNombre());
//        inputBarra.getSelectionModel().select(datosFotovoltaicoCorrida.getBarra());
        inputBarra.getSelectionModel().select(Text.BARRA_1);//TODO: uninodal hardcodeado

        if(datosFotovoltaicoCorrida.getCantModInst() != null){   inputCantModInst.setText(datosFotovoltaicoCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);

        if(datosFotovoltaicoCorrida.getPotMin() != null){ inputPotMin.setText(datosFotovoltaicoCorrida.getPotMin().getValor(instanteActual).toString());  }
        evForField(datosFotovoltaicoCorrida.getPotMin(), "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);

        if(datosFotovoltaicoCorrida.getPotMax() != null ){ inputPotMax.setText(datosFotovoltaicoCorrida.getPotMax().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.getPotMax(), "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);

        if(datosFotovoltaicoCorrida.getFactor() != null ) {
            if(datosFotovoltaicoCorrida.getFactor().getProcOptimizacion() != null  ){
                inputFactorProcOpt.getSelectionModel().select(datosFotovoltaicoCorrida.getFactor().getProcOptimizacion());
            }
            if( datosFotovoltaicoCorrida.getFactor().getProcSimulacion() != null ){
                inputFactorProcSim.getSelectionModel().select(datosFotovoltaicoCorrida.getFactor().getProcSimulacion());
            }
            if(datosFotovoltaicoCorrida.getFactor().getProcOptimizacion() != null && datosFotovoltaicoCorrida.getFactor().getProcSimulacion() != null ){
                inputFactorNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datosFotovoltaicoCorrida.getFactor().getProcOptimizacion()),
                    datosCorrida.getProcesosEstocasticos().get(datosFotovoltaicoCorrida.getFactor().getProcSimulacion())));
            }
        }

        inputFactorNombre.getSelectionModel().select(datosFotovoltaicoCorrida.getFactor().getNombre());

        if(datosFotovoltaicoCorrida.getCantModIni() != null ){ inputCantModIni.setText(datosFotovoltaicoCorrida.getCantModIni().toString()); }

        if(datosFotovoltaicoCorrida.getDispMedia() != null){ inputDispMedia.setText(datosFotovoltaicoCorrida.getDispMedia().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);

        if(datosFotovoltaicoCorrida.gettMedioArreglo() != null ){ inputTMedioArreglo.setText(datosFotovoltaicoCorrida.gettMedioArreglo().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);

        if(datosFotovoltaicoCorrida.getMantProgramado() != null){ inputMantProg.setText(datosFotovoltaicoCorrida.getMantProgramado().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);

        if(datosFotovoltaicoCorrida.getCostoFijo() != null ){ inputCostoFijo.setText(datosFotovoltaicoCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosFotovoltaicoCorrida.getCostoVariable() != null ){ inputCostoVariable.setText(datosFotovoltaicoCorrida.getCostoVariable().getValor(instanteActual).toString()); }
        evForField(datosFotovoltaicoCorrida.getCostoVariable(), "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);

        inputSalidaDetallada.setSelected(datosFotovoltaicoCorrida.isSalDetallada());
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if( inputNombre.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar. Nombre vacío.");
        if( inputBarra.getValue() == null) errores.add("Editor Solar: " + inputNombre.getText() + " Barra vacío.") ;
        if( inputCantModInst.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Cantidad módulos instalados vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantModInst.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Cantidad módulos instalados no es entero.");

        if( inputPotMin.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Potencia mínima vacío.");
        if( !UtilStrings.esNumeroDouble(inputPotMin.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Potencia mínima no es double.");
        if( inputPotMax.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Potencia máxima vacío.");
        if( !UtilStrings.esNumeroDouble(inputPotMax.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Potencia máxima no es double.");

        if( inputFactorProcSim.getValue() == null) errores.add("Editor Solar: " + inputNombre.getText() + " Factor proceso simulación vacío.") ;
        if( inputFactorProcOpt.getValue() == null) errores.add("Editor Solar: " + inputNombre.getText() + " Factor proceso optimizacion vacío.") ;
        if( inputFactorNombre.getValue() == null) errores.add("Editor Solar: " + inputNombre.getText() + "  Factor nombre vacío.") ;

        if( inputCantModIni.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Cantidad módulos inicial vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantModIni.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Cantidad módulos inicial no es entero.");

        if( inputTMedioArreglo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Tiempo medio de arreglo vacío.");
        if( !UtilStrings.esNumeroDouble(inputTMedioArreglo.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Tiempo medio de arreglo no es double.");

        if( inputMantProg.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Mantenimiento programado vacío.");
        if( !UtilStrings.esNumeroEntero(inputMantProg.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Mantenimiento programado no es entero.");

        if( inputDispMedia.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Disponibilidad media vacío.");
        if( !controlDisponibilidadMedia(inputDispMedia)) errores.add("Editor Solar: " + inputNombre.getText() + " Disponibilidad media no es double entre 0 y 1.");

        if( inputCostoFijo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Costo fijo vacío.");
        if( !UtilStrings.esNumeroDouble(inputCostoFijo.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Costo fijo no es double.");

        if( inputCostoVariable.getText().trim().equalsIgnoreCase("") )errores.add("Editor Solar: " + inputNombre.getText() +" Costo variable vacío.");
        if( !UtilStrings.esNumeroDouble(inputCostoVariable.getText().trim())) errores.add("Editor Solar: " + inputNombre.getText() + " Costo variable no es double.");

        return errores;
    }
}
