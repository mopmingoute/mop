/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorEolicoController is part of MOP.
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
import datatypes.DatosEolicoCorrida;
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

public class EditorEolicoController extends GeneralidadesController {
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
    private DatosEolicoCorrida datosEolicoCorrida;
    private boolean edicion;
    private ListaEolicosController listaEolicosController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorEolicoController(DatosCorrida datosCorrida, ListaEolicosController listaEolicosController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        this.listaEolicosController = listaEolicosController;
        copiadoPortapapeles = false;
    }

    public EditorEolicoController(DatosCorrida datosCorrida, DatosEolicoCorrida datosEolicoCorrida, ListaEolicosController listaEolicosController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosEolicoCorrida = datosEolicoCorrida;
        edicion = true;
        this.listaEolicosController = listaEolicosController;
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
            evForField(null, "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);
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
                if (errores.size() == 0 || errores == null) {
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosEolicoCorrida.getNombre().trim() : "";
                    if (evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getEolicos().getOrdenCargaXML(), edicion)) {
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaEolicosController.refresh();
                        listaEolicosController.actualizarLineaDeEntrada();
                        ((Stage) inputAceptar.getScene().getWindow()).close();
                        editoVariables = false;
                    }
                } else {
                    if (inputNombre.getText().trim().equals("")) {
                        setLabelMessageTemporal("Falta el nombre", TipoInfo.FEEDBACK);
                    } else {
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
                if(inputNombre.getText().trim().equals("Eolico clonado")){
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
            if(controlDatosCompletos().size() == 0) {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_EOLICO_TEXT, datosEolicoCorrida, datosCorrida, this, inputNombre.getText());
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
            listaEolicosController.borrarEolico(datosEolicoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaEolicosController.unloadData();//Cambia color participantes incompletos
            listaEolicosController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
             editoVariables = false;
             cerrarVentana();
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




    /**
     *  Método para cargar los datos en el DatosEolicoCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-EOLICO");
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
            String nombreViejo = datosEolicoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getEolicos().getEolicos().remove(nombreViejo);
                int ind = datosCorrida.getEolicos().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getEolicos().getOrdenCargaXML().set(ind,nombre);

                ind = datosCorrida.getEolicos().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getEolicos().getListaUtilizados().set(ind,nombre);

            }
            datosEolicoCorrida.setNombre(nombre);
            datosEolicoCorrida.setPropietario("UTE");
            datosEolicoCorrida.setBarra(barra);
            datosEolicoCorrida.setCantModInst(cantModInst);
            datosEolicoCorrida.setPotMin(potMin);
            datosEolicoCorrida.setPotMax(potMax);
            datosEolicoCorrida.setFactor(factor);
            datosEolicoCorrida.setCantModIni(cantModIni);
            datosEolicoCorrida.setDispMedia(dispMedia);
            datosEolicoCorrida.settMedioArreglo(tMedioArreglo);
            datosEolicoCorrida.setMantProgramado(mantProgramado);
            datosEolicoCorrida.setCostoFijo(costoFijo);
            datosEolicoCorrida.setCostoVariable(costoVariable);
            datosEolicoCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosEolicoCorrida = new DatosEolicoCorrida(nombre,"UTE",barra, cantModInst, potMin, potMax, factor, cantModIni, dispMedia, tMedioArreglo,
                    salidaDetallada, mantProgramado, costoFijo, costoVariable);
            datosCorrida.getEolicos().getListaUtilizados().add(nombre);
            datosCorrida.getEolicos().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getEolicos().getEolicos().put(nombre, datosEolicoCorrida);
    }

    /**
     * Método para obtener los datos del DatosEolicoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-EOLICO");
        inputNombre.setText(datosEolicoCorrida.getNombre());
//        inputBarra.getSelectionModel().select(datosEolicoCorrida.getBarra());
        inputBarra.getSelectionModel().select(Text.BARRA_1);//TODO: uninodal hardcodeado
        
        long instanteActual =CorridaHandler.getInstance().dameInstanteActual();

        if(datosEolicoCorrida.getCantModInst() != null){   inputCantModInst.setText(datosEolicoCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);

        if(datosEolicoCorrida.getPotMin() != null){ inputPotMin.setText(datosEolicoCorrida.getPotMin().getValor(instanteActual).toString());  }
        evForField(datosEolicoCorrida.getPotMin(), "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null, inputPotMin);

        if(datosEolicoCorrida.getPotMax() != null ){ inputPotMax.setText(datosEolicoCorrida.getPotMax().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.getPotMax(), "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null, inputPotMax);

        if(datosEolicoCorrida.getFactor() != null ) {
            if(datosEolicoCorrida.getFactor().getProcOptimizacion() != null  ){
                inputFactorProcOpt.getSelectionModel().select(datosEolicoCorrida.getFactor().getProcOptimizacion());
            }
            if( datosEolicoCorrida.getFactor().getProcSimulacion() != null ){
                inputFactorProcSim.getSelectionModel().select(datosEolicoCorrida.getFactor().getProcSimulacion());
            }
            if(datosEolicoCorrida.getFactor().getProcOptimizacion() != null && datosEolicoCorrida.getFactor().getProcSimulacion() != null ){
                inputFactorNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datosEolicoCorrida.getFactor().getProcOptimizacion()),
                        datosCorrida.getProcesosEstocasticos().get(datosEolicoCorrida.getFactor().getProcSimulacion())));
            }
        }

        inputFactorNombre.getSelectionModel().select(datosEolicoCorrida.getFactor().getNombre());

        if(datosEolicoCorrida.getCantModIni() != null ){ inputCantModIni.setText(datosEolicoCorrida.getCantModIni().toString()); }

        if(datosEolicoCorrida.getDispMedia() != null){ inputDispMedia.setText(datosEolicoCorrida.getDispMedia().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);

        if(datosEolicoCorrida.gettMedioArreglo() != null ){ inputTMedioArreglo.setText(datosEolicoCorrida.gettMedioArreglo().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);

        if(datosEolicoCorrida.getMantProgramado() != null){ inputMantProg.setText(datosEolicoCorrida.getMantProgramado().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);

        if(datosEolicoCorrida.getCostoFijo() != null ){ inputCostoFijo.setText(datosEolicoCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosEolicoCorrida.getCostoVariable() != null ){ inputCostoVariable.setText(datosEolicoCorrida.getCostoVariable().getValor(instanteActual).toString()); }
        evForField(datosEolicoCorrida.getCostoVariable(), "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);

        inputSalidaDetallada.setSelected(datosEolicoCorrida.isSalDetallada());

    }
    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if( inputNombre.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico. Nombre vacío.");
        if( inputBarra.getValue() == null) errores.add("Editor Eólico: " + inputNombre.getText() + " Barra vacío.") ;

        if( inputCantModInst.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Cantidad módulos instalados vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantModInst.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Cantidad módulos instalados no es entero.");

        if( inputPotMin.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Potencia mínima vacío.");
        if( !UtilStrings.esNumeroDouble(inputPotMin.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Potencia mínima no es double.");
        if( inputPotMax.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Potencia máxima vacío.");
        if( !UtilStrings.esNumeroDouble(inputPotMax.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Potencia máxima no es double.");

        if( inputFactorProcSim.getValue() == null) errores.add("Editor Eólico: " + inputNombre.getText() + " Factor proceso simulación vacío.") ;
        if( inputFactorProcOpt.getValue() == null) errores.add("Editor Eólico: " + inputNombre.getText() + " Factor proceso optimizacion vacío.") ;
        if( inputFactorNombre.getValue() == null) errores.add("Editor Eólico: " + inputNombre.getText() + "  Factor nombre vacío.") ;


        if( inputCantModIni.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Cantidad módulos inicial vacío.");
        if( !UtilStrings.esNumeroEntero(inputCantModIni.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Cantidad módulos inicial no es entero.");

        if( inputTMedioArreglo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Tiempo medio de arreglo vacío.");
        if( !UtilStrings.esNumeroDouble(inputTMedioArreglo.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Tiempo medio de arreglo no es double.");

        if( inputMantProg.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Mantenimiento programado vacío.");
        if( !UtilStrings.esNumeroEntero(inputMantProg.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Mantenimiento programado no es entero.");

        if( inputDispMedia.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Disponibilidad media vacío.");
        if( !controlDisponibilidadMedia(inputDispMedia)) errores.add("Editor Eólico: " + inputNombre.getText() + " Disponibilidad media no es double entre 0 y 1.");

        if( inputCostoFijo.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Costo fijo vacío.");
        if( !UtilStrings.esNumeroDouble(inputCostoFijo.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Costo fijo no es double.");

        if( inputCostoVariable.getText().trim().equalsIgnoreCase("") )errores.add("Editor Eólico: " + inputNombre.getText() +" Costo variable vacío.");
        if( !UtilStrings.esNumeroDouble(inputCostoVariable.getText().trim())) errores.add("Editor Eólico: " + inputNombre.getText() + " Costo variable no es double.");




        return errores;
    }
}
