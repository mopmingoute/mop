/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorDemandaController is part of MOP.
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
import datatypes.DatosDemandaCorrida;
import datatypes.DatosVariableAleatoria;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;

public class EditorDemandaController extends GeneralidadesController {
    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;
    @FXML private JFXComboBox<String> inputPotActivaUnidad;
    @FXML private JFXComboBox<String> inputPotActivaProcOpt;
    @FXML private JFXComboBox<String> inputPotActivaProcSim;
    @FXML private JFXComboBox<String> inputPotActivaNombre;
    @FXML private JFXCheckBox inputSalidaDetallada;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;


    private DatosCorrida datosCorrida;
    private DatosDemandaCorrida datosDemandaCorrida;
    private boolean edicion;
    private ListaDemandasController listaDemandasController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorDemandaController(DatosCorrida datosCorrida, ListaDemandasController listaDemandasController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        this.copiadoPortapapeles = false;
        this.listaDemandasController = listaDemandasController;
    }

    public EditorDemandaController(DatosCorrida datosCorrida, DatosDemandaCorrida datosDemandaCorrida, ListaDemandasController listaDemandasController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosDemandaCorrida = datosDemandaCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaDemandasController = listaDemandasController;
    }

    @FXML
    public void initialize(){

        Manejador.popularListaNombresVA(datosCorrida, inputPotActivaProcSim, inputPotActivaProcOpt, inputPotActivaNombre);

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getSelectionModel().selectFirst();

        inputPotActivaUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotActivaUnidad.getSelectionModel().selectFirst();

        if(edicion){
            unloadData();
        }
        inputAceptar.setOnAction(actionEvent -> {
            if(editoVariables) {
                ArrayList<String> errores = controlDatosCompletos();
                if(errores.size() == 0 || errores == null) {
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosDemandaCorrida.getNombre().trim():"";
                    if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaDemandasController.refresh();
                        listaDemandasController.actualizarLineaDeEntrada();
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
            }else {
                if(inputNombre.getText().trim().equals("Demanda clonada")){
                    setLabelMessageTemporal("Cambie el nombre de la demanda clonada", TipoInfo.FEEDBACK);
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
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_DEMANDA_TEXT, datosDemandaCorrida, datosCorrida, this, inputNombre.getText());
            }else{
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });

        //Control de cambios:

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotActivaProcSim.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotActivaProcOpt.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotActivaNombre.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        controlHayCambiosEnEvoluciones();

        editoVariables = false;
    }

    private void controlHayCambiosEnEvoluciones() {
        /*for (String clave : evsPorNombre.keySet()) {
             if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
                editoVariables = true;
            }
        }*/

    }



    private void cerrarVentana() {
        if(copiadoPortapapeles){
            listaDemandasController.borrarDemanda(datosDemandaCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaDemandasController.unloadData();//Cambia color participantes incompletos
            listaDemandasController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    };

    /**
     *  Método para cargar los datos en el DatosDemandaCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-DEMANDA");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();

        DatosVariableAleatoria potActiva = null;
        String potActivaPSim = inputPotActivaProcSim.getValue();
        String potActivaPOpt = inputPotActivaProcOpt.getValue();
        String potActivaNom = inputPotActivaNombre.getValue();
        potActiva = new DatosVariableAleatoria( potActivaPOpt, potActivaPSim , potActivaNom);

        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        if(edicion){
            String nombreViejo = datosDemandaCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getDemandas().getDemandas().remove(nombreViejo);
                int pos = datosCorrida.getDemandas().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getDemandas().getListaUtilizados().set(pos, nombre);

                pos = datosCorrida.getDemandas().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getDemandas().getOrdenCargaXML().set(pos, nombre);
            }
            datosDemandaCorrida.setNombre(nombre);
            datosDemandaCorrida.setBarra(barra);
            datosDemandaCorrida.setPotActiva(potActiva);
            datosDemandaCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosDemandaCorrida = new DatosDemandaCorrida(nombre, barra, potActiva, salidaDetallada);
            datosCorrida.getDemandas().getListaUtilizados().add(nombre);
            datosCorrida.getDemandas().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getDemandas().getDemandas().put(nombre, datosDemandaCorrida);
    }

    /**
     * Método para obtener los datos del DatosDemandaCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-DEMANDA");
        inputNombre.setText(datosDemandaCorrida.getNombre());
        inputBarra.getSelectionModel().select(Text.BARRA_1);//uninodal hardcodeado

        if(datosDemandaCorrida.getPotActiva().getProcOptimizacion() != null) { inputPotActivaProcOpt.getSelectionModel().select(datosDemandaCorrida.getPotActiva().getProcOptimizacion()); }
        if(datosDemandaCorrida.getPotActiva().getProcSimulacion() != null) { inputPotActivaProcSim.getSelectionModel().select(datosDemandaCorrida.getPotActiva().getProcSimulacion()); }
        if(datosDemandaCorrida.getPotActiva().getProcOptimizacion() != null && datosDemandaCorrida.getPotActiva().getProcSimulacion() != null ) {
            inputPotActivaNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datosDemandaCorrida.getPotActiva().getProcOptimizacion()),
                    datosCorrida.getProcesosEstocasticos().get(datosDemandaCorrida.getPotActiva().getProcSimulacion())));
        }
        if(datosDemandaCorrida.getPotActiva().getNombre() != null) { inputPotActivaNombre.getSelectionModel().select(datosDemandaCorrida.getPotActiva().getNombre()); }
        inputSalidaDetallada.setSelected(datosDemandaCorrida.isSalDetallada());
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if( inputNombre.getText().trim().equalsIgnoreCase("") )errores.add("Editor Demanda. Nombre vacío.");
        if( inputBarra.getValue() == null) errores.add("Editor Demanda: " + inputNombre.getText() + " Barra vacío.") ;
        if( inputPotActivaProcOpt.getValue() == null) errores.add("Editor Demanda: " + inputNombre.getText() + " Pot Activa - Proceso simulación vacío.") ;
        if( inputPotActivaProcSim.getValue() == null) errores.add("Editor Demanda: " + inputNombre.getText() + " Pot Activa - Proceso optimizacion vacío.") ;
        if( inputPotActivaNombre.getValue() == null) errores.add("Editor Demanda: " + inputNombre.getText() + " Pot Activa - Nombre vacío.") ;
        return errores;
    }
}
