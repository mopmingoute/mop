/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorContratoController is part of MOP.
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
import database.MongoDBHandler;
import datatypes.DatosCorrida;
import datatypes.DatosContratoEnergiaCorrida;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;


public class EditorContratoController extends GeneralidadesController {
    @FXML private GridPane gridPane;
    @FXML private JFXTextField inputNombre;
    @FXML private JFXDatePicker inputFechaInicial;
    @FXML private JFXTextField inputCantAnios;
    @FXML private JFXTextField inputEnergiaInicial;
    @FXML private JFXComboBox<String> inputEnergiaInicialUnidad;
    @FXML private JFXTextField inputEnergiaBase;
    @FXML private JFXButton inputEnergiaBaseEV;
    @FXML private JFXComboBox<String> inputEnergiaBaseUnidad;
    @FXML private JFXTextField inputPrecioBase;
    @FXML private JFXButton inputPrecioBaseEV;
    @FXML private JFXComboBox<String> inputPrecioBaseUnidad;
    @FXML private JFXTextField inputCotaInferior;
    @FXML private JFXButton inputCotaInferiorEV;
    @FXML private JFXComboBox<String> inputCotaInferiorUnidad;
    @FXML private JFXTextField inputCotaSuperior;
    @FXML private JFXButton inputCotaSuperiorEV;
    @FXML private JFXComboBox<String> inputCotaSuperiorUnidad;
    @FXML private JFXComboBox<String> inputTipoContrato;
    //participantes involucrados
    @FXML private JFXComboBox<String> inputPartInv1;
    @FXML private JFXButton btnAddPartInv;
    @FXML private JFXButton btnRemovePartInv;

    @FXML private JFXCheckBox inputSalidaDetallada;
    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;



    private Integer cantParticipantesInvolucrados = 1;
    private HashMap<Integer, Node> combosParticipantesInvolucrados = new HashMap<>();
    private ArrayList<String> participantesDisponibles = new ArrayList<>();
    private ArrayList<String> participantesSeleccionados = new ArrayList<>();

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private DatosCorrida datosCorrida;
    private DatosContratoEnergiaCorrida datosContratoEnergiaCorrida;
    private boolean edicion;
    private ListaContratosController listaContratosController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorContratoController(DatosCorrida datosCorrida, ListaContratosController listaContratosController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        this.copiadoPortapapeles = false;
        this.listaContratosController = listaContratosController;
    }

    public EditorContratoController(DatosCorrida datosCorrida, DatosContratoEnergiaCorrida datosContratoEnergiaCorrida, ListaContratosController listaContratosController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosContratoEnergiaCorrida = datosContratoEnergiaCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaContratosController = listaContratosController;
    }

    @FXML
    public void initialize(){

        inputTipoContrato.getItems().addAll(Constantes.LIM_ENERGIA_ANUAL);
        inputTipoContrato.getSelectionModel().selectFirst();

        inputEnergiaInicialUnidad.getItems().add(Text.UNIDAD_GWH);
        inputEnergiaInicialUnidad.getSelectionModel().selectFirst();
        inputEnergiaBaseUnidad.getItems().add(Text.UNIDAD_GWH);// TODO: 06/08/2020 unidad?
        inputEnergiaBaseUnidad.getSelectionModel().selectFirst();
        inputPrecioBaseUnidad.getItems().add(Text.UNIDAD_USD_MWH);
        inputPrecioBaseUnidad.getSelectionModel().selectFirst();
        inputCotaInferiorUnidad.getItems().add(Text.UNIDAD_USD_MWH);
        inputCotaInferiorUnidad.getSelectionModel().selectFirst();
        inputCotaSuperiorUnidad.getItems().add(Text.UNIDAD_USD_MWH);
        inputCotaSuperiorUnidad.getSelectionModel().selectFirst();



//        participantesDisponibles.addAll(datosCorrida.getHidraulicos().getHidraulicos().keySet());
//        participantesDisponibles.addAll(datosCorrida.getImpoExpos().getImpoExpos().keySet());
        participantesDisponibles.addAll(datosCorrida.getListaParticipantes());
        if(edicion){
            participantesDisponibles.remove(datosContratoEnergiaCorrida.getNombre());
        }

        inputPartInv1.getItems().addAll(participantesDisponibles);
        combosParticipantesInvolucrados.put(1, inputPartInv1);
        inputPartInv1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));

        btnAddPartInv.setOnAction(actionEvent -> {
            JFXComboBox<String> newGen = new JFXComboBox<>();
            newGen.setPromptText("Elija...");
            newGen.setMinWidth(180);
            newGen.setMaxWidth(180);
            gridPane.add(newGen, 2*cantParticipantesInvolucrados+1,14, 2, 1);
            combosParticipantesInvolucrados.put(2*cantParticipantesInvolucrados+1, newGen);
            cantParticipantesInvolucrados++;
            GridPane.setColumnIndex(btnAddPartInv, 2*cantParticipantesInvolucrados+1);
            GridPane.setColumnIndex(btnRemovePartInv, 2*cantParticipantesInvolucrados+1);
            if(cantParticipantesInvolucrados == 3) {// TODO: 28/08/2020 max cant ?
                btnAddPartInv.setVisible(false);
                GridPane.setHalignment(btnRemovePartInv, HPos.LEFT);
            }
            if(cantParticipantesInvolucrados == 2) {
                btnRemovePartInv.setVisible(true);
            }
            for(String nombreGen : participantesDisponibles){
                if(!participantesSeleccionados.contains(nombreGen)){
                    newGen.getItems().add(nombreGen);
                }
            }
            newGen.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));
        });

        btnRemovePartInv.setOnAction(actionEvent -> {
            cantParticipantesInvolucrados--;
            gridPane.getChildren().remove(combosParticipantesInvolucrados.get(2*cantParticipantesInvolucrados+1));
            GridPane.setColumnIndex(btnAddPartInv, 2*cantParticipantesInvolucrados+1);
            GridPane.setColumnIndex(btnRemovePartInv, 2*cantParticipantesInvolucrados+1);
            if(cantParticipantesInvolucrados == 1) {
                btnRemovePartInv.setVisible(false);
            }
            if(cantParticipantesInvolucrados == 2) {
                btnAddPartInv.setVisible(true);
                GridPane.setHalignment(btnRemovePartInv, HPos.RIGHT);
            }
            if(((ComboBox<String>) combosParticipantesInvolucrados.get(2*cantParticipantesInvolucrados+1)).getValue() != null) {
                participantesSeleccionados.remove(((ComboBox<String>) combosParticipantesInvolucrados.get(2 * cantParticipantesInvolucrados + 1)).getValue());
                updateGenList(((ComboBox<String>) combosParticipantesInvolucrados.get(2*cantParticipantesInvolucrados+1)).getValue(), null);
            }
        });



        if(edicion){
            unloadData();
        }else{
            evForField(null, "energiaBase", inputEnergiaBaseEV, Text.EV_NUM_DOUBLE, null, inputEnergiaBase);
            evForField(null, "precioBase", inputPrecioBaseEV, Text.EV_NUM_DOUBLE, null, inputPrecioBase);
            evForField(null, "cotaInferior", inputCotaInferiorEV, Text.EV_NUM_DOUBLE, null, inputCotaInferior);
            evForField(null, "cotaSuperior", inputCotaSuperiorEV, Text.EV_NUM_DOUBLE, null, inputCotaSuperior);
        }
        inputAceptar.setOnAction(actionEvent -> {
            if(editoVariables) {
                ArrayList<String> errores = controlDatosCompletos();
                if(errores.size() == 0 || errores == null) {
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosContratoEnergiaCorrida.getNombre().trim():"";
                    if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaContratosController.refresh();
                        listaContratosController.actualizarLineaDeEntrada();
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
                if(inputNombre.getText().trim().equals("Contrato clonado")){
                    setLabelMessageTemporal("Cambie el nombre del contrato clonado", TipoInfo.FEEDBACK);
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
            ArrayList<String> errores = controlDatosCompletos();
            if(errores.size() == 0 || errores == null) {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_CONTRATO_ENERGIA_TEXT, datosContratoEnergiaCorrida, datosCorrida, this, inputNombre.getText());
            }
            else {
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }

        });


        //Control de cambios:

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputFechaInicial.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;   });
        inputCantAnios.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputEnergiaInicial.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputEnergiaBase.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPrecioBase.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCotaInferior.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCotaSuperior.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTipoContrato.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPartInv1.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

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
            listaContratosController.borrarContrato(datosContratoEnergiaCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }
    private void genComboOnChangeBehaviour(String oldValue, String newValue){
        if(oldValue != null){
            participantesSeleccionados.remove(oldValue);
        }
        participantesSeleccionados.add(newValue);
        updateGenList(oldValue, newValue);
    }

    private void updateGenList(String oldValue, String newValue){
        for(Node combo : combosParticipantesInvolucrados.values()){
            if(oldValue != null && !oldValue.isEmpty()){
                if(!((ComboBox<String>)combo).getItems().contains(oldValue)){
                    ((ComboBox<String>)combo).getItems().add(oldValue);
                }
            }
            if(newValue != null) {
                if ((((ComboBox<String>) combo).getValue() == null) || !((ComboBox<String>) combo).getValue().equalsIgnoreCase(newValue)) {
                    ((ComboBox<String>) combo).getItems().remove(newValue);
                }
            }
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

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //Eligio No corregir errores o datos incompletos
            loadData();
            listaContratosController.unloadData();
            listaContratosController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    }

    /**
     *  Método para cargar los datos en el DatosContratoEnergiaCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-CONTRATO");
        String nombre = inputNombre.getText();
//        String fechaInicial = inputFechaInicial.getEditor().getText();

        String fechaInicial = null;
        if(inputFechaInicial.getValue() != null) { fechaInicial = inputFechaInicial.getValue().format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00";}

        int cantAnios = 0;
        if(UtilStrings.esNumeroEntero(inputCantAnios.getText())) { cantAnios = Integer.parseInt(inputCantAnios.getText()); }

        double energiaInicial = 0;
        if(UtilStrings.esNumeroDouble(inputEnergiaInicial.getText())) { energiaInicial = Double.parseDouble(  inputEnergiaInicial.getText()); }

        Evolucion<Double> energiaBase = null;
        if(UtilStrings.esNumeroDouble(inputEnergiaBase.getText())){ energiaBase = EVController.loadEVsegunTipo(inputEnergiaBase, Text.EV_NUM_DOUBLE, evsPorNombre, "energiaBase"); }

        Evolucion<Double> precioBase  = null;
        if(UtilStrings.esNumeroDouble(inputPrecioBase.getText())) {precioBase  =  EVController.loadEVsegunTipo(inputPrecioBase, Text.EV_NUM_DOUBLE, evsPorNombre, "precioBase"); }

        Evolucion<Double> cotaInferior = null;
        if(UtilStrings.esNumeroDouble(inputCotaInferior.getText())) {cotaInferior = EVController.loadEVsegunTipo(inputCotaInferior, Text.EV_NUM_DOUBLE, evsPorNombre, "cotaInferior"); }

        Evolucion<Double> cotaSuperior = null;
        if(UtilStrings.esNumeroDouble(inputCotaSuperior.getText())) { cotaSuperior = EVController.loadEVsegunTipo(inputCotaSuperior, Text.EV_NUM_DOUBLE, evsPorNombre, "cotaSuperior"); }

        String tipoContrato = "";
        if (!inputTipoContrato.getValue().trim().equals("")) { tipoContrato = inputTipoContrato.getValue(); }
        //participantes involucrados
        ArrayList<String> participantesInvolucrados = new ArrayList<>();
        for(Integer combo_key : combosParticipantesInvolucrados.keySet()){
            if(combo_key > 0){
                if(((ComboBox<String>)combosParticipantesInvolucrados.get(combo_key)).getValue() != null) {
                    participantesInvolucrados.add(((ComboBox<String>) combosParticipantesInvolucrados.get(combo_key)).getValue());
                }
            }
        }
        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        if(edicion){
            String nombreViejo = datosContratoEnergiaCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getContratosEnergia().getContratosEnergia().remove(nombreViejo);
                int ind = datosCorrida.getContratosEnergia().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getContratosEnergia().getOrdenCargaXML().set(ind,nombre);

                ind = datosCorrida.getContratosEnergia().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getContratosEnergia().getListaUtilizados().set(ind,nombre);
            }
            datosContratoEnergiaCorrida.setNombre(nombre);
            datosContratoEnergiaCorrida.setFechaInicial(fechaInicial);
            datosContratoEnergiaCorrida.setCantAnios(cantAnios);
            datosContratoEnergiaCorrida.setEnergiaInicial(energiaInicial);
            datosContratoEnergiaCorrida.setEnergiaBase(energiaBase);
            datosContratoEnergiaCorrida.setPrecioBase(precioBase);
            datosContratoEnergiaCorrida.setCotaInf(cotaInferior);
            datosContratoEnergiaCorrida.setCotaSup(cotaSuperior);
            datosContratoEnergiaCorrida.setTipo(tipoContrato);
            datosContratoEnergiaCorrida.setInvolucrados(participantesInvolucrados);
            datosContratoEnergiaCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosContratoEnergiaCorrida = new DatosContratoEnergiaCorrida(nombre,participantesInvolucrados,precioBase,energiaBase,fechaInicial,cantAnios,
                                                                          energiaInicial,tipoContrato,cotaInferior,cotaSuperior,salidaDetallada);
        }
        datosCorrida.getContratosEnergia().getContratosEnergia().put(nombre, datosContratoEnergiaCorrida);
    }

    /**
     * Método para obtener los datos del DatosContratoEnergiaCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
    	long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        System.out.println("UNLOAD DATA-CONTRATO");
        inputNombre.setText(datosContratoEnergiaCorrida.getNombre());
        
        if(datosContratoEnergiaCorrida.getFechaInicial() != null) {
        inputFechaInicial.setValue(LocalDateTime.parse(datosContratoEnergiaCorrida.getFechaInicial(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss")).toLocalDate());}

        if(datosContratoEnergiaCorrida.getCantAnios() > 0 ) {
        inputCantAnios.setText(String.valueOf(datosContratoEnergiaCorrida.getCantAnios())); }

        if(datosContratoEnergiaCorrida.getEnergiaInicial() > 0 ){
        inputEnergiaInicial.setText(String.valueOf(datosContratoEnergiaCorrida.getEnergiaInicial())); }

        if(datosContratoEnergiaCorrida.getEnergiaBase() != null){
        inputEnergiaBase.setText(String.valueOf(datosContratoEnergiaCorrida.getEnergiaBase().getValor(instanteActual)));}
        evForField(datosContratoEnergiaCorrida.getEnergiaBase(), "energiaBase", inputEnergiaBaseEV, Text.EV_NUM_DOUBLE, null, inputEnergiaBase);

        if(datosContratoEnergiaCorrida.getPrecioBase() != null){
        inputPrecioBase.setText(String.valueOf(datosContratoEnergiaCorrida.getPrecioBase().getValor(instanteActual)));}
        evForField(datosContratoEnergiaCorrida.getPrecioBase(), "precioBase", inputPrecioBaseEV, Text.EV_NUM_DOUBLE, null, inputPrecioBase);

        if(datosContratoEnergiaCorrida.getCotaInf() != null){
        inputCotaInferior.setText(String.valueOf(datosContratoEnergiaCorrida.getCotaInf().getValor(instanteActual)));}
        evForField(datosContratoEnergiaCorrida.getCotaInf(), "cotaInferior", inputCotaInferiorEV, Text.EV_NUM_DOUBLE, null, inputCotaInferior);

        if(datosContratoEnergiaCorrida.getCotaSup() != null){
        inputCotaSuperior.setText(String.valueOf(datosContratoEnergiaCorrida.getCotaSup().getValor(instanteActual)));}
        evForField(datosContratoEnergiaCorrida.getCotaSup(), "cotaSuperior", inputCotaSuperiorEV, Text.EV_NUM_DOUBLE, null, inputCotaSuperior);


        if(datosContratoEnergiaCorrida.getTipo() != null){
        inputTipoContrato.getSelectionModel().select(datosContratoEnergiaCorrida.getTipo()); }
        //participantes involucrados
        for(int i=0;i<datosContratoEnergiaCorrida.getInvolucrados().size();i++){
            if(i==0){
                inputPartInv1.getSelectionModel().select(datosContratoEnergiaCorrida.getInvolucrados().get(i));
            }else if(i < 3){//TODO: maximo harcodeado
                btnAddPartInv.fire();
                ((ComboBox<String>)combosParticipantesInvolucrados.get(2*(cantParticipantesInvolucrados-1)+1)).getSelectionModel().select(datosContratoEnergiaCorrida.getInvolucrados().get(i));
            }
        }
        inputSalidaDetallada.setSelected(datosContratoEnergiaCorrida.isSalDetallada());
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();

        Boolean ret;
        if((inputNombre.getText().isEmpty()) || inputNombre.getText().trim().equalsIgnoreCase("")) errores.add("Editor Contrato.  Nombre vacío.");
        if(inputCantAnios.getText().trim().equals("")) errores.add("Editor Contrato: " + inputNombre.getText() +"  Cantidad años vacio");
        if(!UtilStrings.esNumeroEntero(inputCantAnios.getText())) errores.add("Editor Contrato: " + inputNombre.getText() +"  Cantidad años no es dato numérico");
        if(inputFechaInicial.getValue() == null) errores.add("Editor Contrato: " + inputNombre.getText() +"  Fecha inical vacia");
        if(inputEnergiaInicial.getText().isEmpty() || inputEnergiaInicial.getText().equalsIgnoreCase("") ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Energia inical vacia");
        if(!UtilStrings.esNumeroDouble(inputEnergiaInicial.getText() )) errores.add("Editor Contrato: " + inputNombre.getText() +"  Energia inical no es dato numérico");
        if(inputEnergiaBase.getText().isEmpty() || inputEnergiaBase.getText().equalsIgnoreCase("") ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Energía base vacia");
        if(!UtilStrings.esNumeroDouble(inputEnergiaBase.getText()) ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Energía base no es dato numérico");
        if(inputPrecioBase.getText().isEmpty() || inputPrecioBase.getText().equalsIgnoreCase("") ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Precio base vacia");
        if(!UtilStrings.esNumeroDouble(inputPrecioBase.getText()) ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Precio base no es dato numérico");
        if(inputCotaInferior.getText().isEmpty() || inputCotaInferior.getText().equalsIgnoreCase("") ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Cota inferior vacia");
        if(!UtilStrings.esNumeroDouble(inputCotaInferior.getText()) ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Cota inferior no es dato numérico");
        if(inputCotaSuperior.getText().isEmpty() || inputCotaSuperior.getText().equalsIgnoreCase("") ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Cota superior vacia");
        if(!UtilStrings.esNumeroDouble(inputCotaSuperior.getText()) ) errores.add("Editor Contrato: " + inputNombre.getText() +"  Cota superior no es dato numérico");
        if(inputTipoContrato.getValue().isEmpty() ) errores.add("Editor Contrato: " + inputNombre.getText() +" Tipo de contrato vacio");
        if(inputPartInv1.getValue() == null || inputPartInv1.getValue().isEmpty())  errores.add("Editor Contrato: " + inputNombre.getText() +"  Participantes involucrados vacio");


        return errores;
    }
}
