/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorImpactoController is part of MOP.
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
import datatypes.DatosImpactoCorrida;
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
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class EditorImpactoController extends GeneralidadesController {
    @FXML private GridPane gridPane;
    @FXML private JFXTextField inputNombre;
    @FXML private JFXTextField inputCosto;
    @FXML private JFXButton inputCostoEV;
    @FXML private JFXComboBox<String> inputCostoUnidad;
    @FXML private JFXTextField inputLimite;
    @FXML private JFXButton inputLimiteEV;
    @FXML private JFXComboBox<String> inputLimiteUnidad;
    @FXML private JFXCheckBox inputActivo;
    @FXML private JFXButton inputActivoEV;
    @FXML private JFXCheckBox inputPorPoste;
    @FXML private JFXCheckBox inputPorUnidadDeTiempo;
    @FXML private JFXComboBox<String> inputTipoImpacto;
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
    private DatosImpactoCorrida datosImpactoCorrida;
    private boolean edicion;
    private ListaImpactosController listaImpactosController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorImpactoController(DatosCorrida datosCorrida, ListaImpactosController listaImpactosController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaImpactosController = listaImpactosController;
    }

    public EditorImpactoController(DatosCorrida datosCorrida, DatosImpactoCorrida datosImpactoCorrida, ListaImpactosController listaImpactosController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosImpactoCorrida = datosImpactoCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaImpactosController = listaImpactosController;
    }

    @FXML
    public void initialize(){

        inputTipoImpacto.getItems().addAll(Text.TIPOS_IMPACTOS);
        inputTipoImpacto.getSelectionModel().selectFirst();
        inputCostoUnidad.getItems().add(Text.UNIDAD_USD_HM3);
        inputCostoUnidad.getSelectionModel().selectFirst();
        inputLimiteUnidad.getItems().add(Text.UNIDAD_M3_S);
        inputLimiteUnidad.getSelectionModel().selectFirst();


//        participantesDisponibles.addAll(datosCorrida.getHidraulicos().getHidraulicos().keySet());
//        participantesDisponibles.addAll(datosCorrida.getImpoExpos().getImpoExpos().keySet());
//        participantesDisponibles.addAll(datosCorrida.getImpactos().getImpactos().keySet());
        participantesDisponibles.addAll(datosCorrida.getListaParticipantes());
        if(edicion){
            participantesDisponibles.remove(datosImpactoCorrida.getNombre());
        }

        inputPartInv1.getItems().addAll(participantesDisponibles);
        combosParticipantesInvolucrados.put(1, inputPartInv1);
        inputPartInv1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));

        btnAddPartInv.setOnAction(actionEvent -> {
            JFXComboBox<String> newGen = new JFXComboBox<>();
            newGen.setPromptText("Elija...");
            newGen.setMinWidth(180);
            newGen.setMaxWidth(180);
            gridPane.add(newGen, 2*cantParticipantesInvolucrados+1,11, 2, 1);
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
            evForField(null, "activo", inputActivoEV, Text.EV_BOOL, null, inputActivo);
            evForField(null, "costo", inputCostoEV, Text.EV_NUM_DOUBLE, null, inputCosto);
            evForField(null, "limite", inputLimiteEV, Text.EV_NUM_DOUBLE, null,inputLimite);
        }
        inputAceptar.setOnAction(actionEvent -> {

            if(editoVariables) {
                ArrayList<String> errores = controlDatosCompletos();
                if(errores.size() == 0 || errores == null) {
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosImpactoCorrida.getNombre().trim():"";
                    if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaImpactosController.refresh();
                        listaImpactosController.actualizarLineaDeEntrada();
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
                if(inputNombre.getText().trim().equals("Impacto clonado")){
                    setLabelMessageTemporal("Cambie el nombre del impacto clonado", TipoInfo.FEEDBACK);
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
            if (controlDatosCompletos().size() == 0) {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_IMPACTO_TEXT, datosImpactoCorrida, datosCorrida, this, inputNombre.getText());
            } else {
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });

        //Control de cambios:

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputActivo.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCosto.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputLimite.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPorPoste.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPorUnidadDeTiempo.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTipoImpacto.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
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
            listaImpactosController.borrarImpacto(datosImpactoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
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

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaImpactosController.unloadData();
            listaImpactosController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    };

    /**
     *  Método para cargar los datos en el DatosImpactoCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-IMPACTO");
        String nombre = inputNombre.getText();
        Evolucion<Boolean> activo = EVController.loadEVsegunTipo(inputActivo, Text.EV_BOOL,  evsPorNombre,"activo" );
        Evolucion<Double> costo = EVController.loadEVsegunTipo(inputCosto, Text.EV_NUM_DOUBLE,  evsPorNombre,"costo" );
        Evolucion<Double> limite = EVController.loadEVsegunTipo(inputLimite, Text.EV_NUM_DOUBLE,  evsPorNombre,"limite" );
        boolean porPoste = inputPorPoste.isSelected();
        boolean porUnidadDeTiempo = inputPorUnidadDeTiempo.isSelected();
        int tipoImpacto = Text.TIPOS_IMPACTO_BY_STRING.get(inputTipoImpacto.getValue());
        //participantes involucrados
        ArrayList<String> participantesInvolucrados = new ArrayList<>();
        for(Integer combo_key : combosParticipantesInvolucrados.keySet()){
            if(combo_key > 0){
                if(((ComboBox<String>)combosParticipantesInvolucrados.get(combo_key)).getValue() != null) {
                    participantesInvolucrados.add(((ComboBox<String>)combosParticipantesInvolucrados.get(combo_key)).getValue());
                }
            }
        }
        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        if(edicion){
            String nombreViejo = datosImpactoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getImpactos().getImpactos().remove(nombreViejo);
                int pos = datosCorrida.getImpactos().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getImpactos().getOrdenCargaXML().set(pos, nombre);
                pos = datosCorrida.getImpoExpos().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getImpoExpos().getListaUtilizados().set(pos, nombre);
            }
            datosImpactoCorrida.setNombre(nombre);
            datosImpactoCorrida.setActivo(activo);
            datosImpactoCorrida.setCostoUnit(costo);
            datosImpactoCorrida.setLimite(limite);
            datosImpactoCorrida.setPorPoste(porPoste);
            datosImpactoCorrida.setPorUnidadTiempo(porUnidadDeTiempo);
            datosImpactoCorrida.setTipoImpacto(tipoImpacto);
            datosImpactoCorrida.setInvolucrados(participantesInvolucrados);
            datosImpactoCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosImpactoCorrida = new DatosImpactoCorrida(nombre,activo,costo,limite,porPoste,participantesInvolucrados,tipoImpacto,porUnidadDeTiempo,salidaDetallada);
            datosCorrida.getImpactos().getListaUtilizados().add(nombre);
            datosCorrida.getImpactos().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getImpactos().getImpactos().put(nombre, datosImpactoCorrida);
    }

    /**
     * Método para obtener los datos del DatosImpactoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-IMPACTO");
        inputNombre.setText(datosImpactoCorrida.getNombre());
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        if(datosImpactoCorrida.getActivo() != null) {
        inputActivo.setSelected(datosImpactoCorrida.getActivo().getValor(instanteActual)); }
        evForField(datosImpactoCorrida.getActivo(), "activo", inputActivoEV, Text.EV_BOOL, null, inputActivo);

        if(datosImpactoCorrida.getCostoUnit() != null) {
        inputCosto.setText(datosImpactoCorrida.getCostoUnit().getValor(instanteActual).toString()); }
        evForField(datosImpactoCorrida.getCostoUnit(), "costo", inputCostoEV, Text.EV_NUM_DOUBLE, null,inputCosto);

        if(datosImpactoCorrida.getLimite() != null) {
        inputLimite.setText(datosImpactoCorrida.getLimite().getValor(instanteActual).toString()); }
        evForField(datosImpactoCorrida.getLimite(), "limite", inputLimiteEV, Text.EV_NUM_DOUBLE, null, inputLimite);

        inputPorPoste.setSelected(datosImpactoCorrida.isPorPoste());
        inputPorUnidadDeTiempo.setSelected(datosImpactoCorrida.isPorUnidadTiempo());
        inputTipoImpacto.getSelectionModel().select(Text.TIPOS_IMPACTO_BY_INT.get(datosImpactoCorrida.getTipoImpacto()));

        //participantes involucrados
        for(int i=0;i<datosImpactoCorrida.getInvolucrados().size();i++){
            if(i==0){
                inputPartInv1.getSelectionModel().select(datosImpactoCorrida.getInvolucrados().get(i));
            }else if(i < 3){//TODO: maximo harcodeado
                btnAddPartInv.fire();
                ((ComboBox<String>)combosParticipantesInvolucrados.get(2*(cantParticipantesInvolucrados-1)+1)).getSelectionModel().select(datosImpactoCorrida.getInvolucrados().get(i));
            }
        }
        inputSalidaDetallada.setSelected(datosImpactoCorrida.isSalDetallada());
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();

        if(inputNombre.getText().trim().equals("")) { errores.add( "Editor Impacto.  Nombre vacío"); }

        if(!UtilStrings.esNumeroDouble(inputCosto.getText())) { errores.add("Editor Impacto: " + inputNombre.getText() + " Costo no es dato numérico.") ; }
        if(!UtilStrings.esNumeroDouble(inputLimite.getText())) { errores.add("Editor Impacto: " + inputNombre.getText() + " Límite no es dato numérico.") ; }
        if(inputTipoImpacto.getValue() == null){ errores.add( "Editor CicloCombinado: " + inputNombre.getText() +"  Tipo impacto vacío.");}


        combosParticipantesInvolucrados.forEach((k,v) -> {
            if( ((ComboBox<String>)v).getSelectionModel().isEmpty()){
                errores.add( "Editor CicloCombinado: " + inputNombre.getText() +"  Participante involucrado vacío.");
            }
        });

        return errores;
    }
}
