/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorFallaController is part of MOP.
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.*;

import static utilitarios.Constantes.COMPFALLA;

public class EditorFallaController extends GeneralidadesController {

    @FXML private JFXComboBox<String> inputCompFalla;
    @FXML private JFXButton inputCompFallaEV;
    @FXML private JFXTextField inputNombre;
    @FXML private GridPane gridPane;
    @FXML private JFXTextField inputEscalon1;

    @FXML private JFXComboBox<String> inputEscPorcientoUnidad1;
    @FXML private JFXComboBox<String> inputEscPorcientoUnidad2;
    @FXML private JFXComboBox<String> inputDemanda;
    @FXML private JFXCheckBox inputSalidaDetallada;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;
    @FXML private JFXButton btnGuardarEnBiblioteca;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    @FXML private VBox vBoxVE;
    //@FXML private VBox vBoxVCDE;

    private DatosCorrida datosCorrida;
    private DatosFallaEscalonadaCorrida datosFallaCorrida;
    private boolean edicion;
    private VariableDeEstadoFallaController variableDeEstadoCantEscController;
    //private VariableDeEstadoController variableDeEstadoCantPerController;
    //private VariableDeControlDEController variableDeControlDEController;
    private ListaFallasController listaFallasController;



    private Integer cantEscalonesPorciento = 1;
    private HashMap<Integer, JFXTextField> textFieldsEscalonesPorciento = new HashMap<>();

    @FXML private JFXButton btnRemoveEscalonPorciento;
    @FXML private JFXButton btnAddEscalonPorciento;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorFallaController(DatosCorrida datosCorrida, ListaFallasController listaFallasController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaFallasController = listaFallasController;
    }

    public EditorFallaController(DatosCorrida datosCorrida, DatosFallaEscalonadaCorrida datosFallaCorrida, ListaFallasController listaFallasController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosFallaCorrida = datosFallaCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaFallasController = listaFallasController;
    }

    public void initialize(){

        inputCompFalla.getItems().addAll(Text.COMP_FALLA);
        inputCompFalla.setOnAction(actionEvent -> {
            cambioComportamientoFalla();
        });

//        inputDemanda.getItems().addAll(datosCorrida.getDemandas().getListaUtilizados());
        inputDemanda.getItems().addAll(datosCorrida.getDemandas().getDemandas().keySet());

        inputEscPorcientoUnidad1.getItems().add(Text.UNIDAD_POR_CIENTO);
        inputEscPorcientoUnidad1.getSelectionModel().selectFirst();

        inputEscPorcientoUnidad2.getItems().add(Text.UNIDAD_USD_MWH);
        inputEscPorcientoUnidad2.getSelectionModel().selectFirst();

        btnRemoveEscalonPorciento.setOnAction(actionEvent -> removeEscalonPorciento());
        btnAddEscalonPorciento.setOnAction(actionEvent -> addEscalonPorciento(null));


        try{
            if(edicion){
                variableDeEstadoCantEscController = new VariableDeEstadoFallaController(Text.VE_CANT_ESC_FORZ_LABEL, Text.VE_CANT_ESC_FORZ, 0, datosFallaCorrida.getVarsEstado().get(Text.VE_CANT_ESC_FORZ),
                        datosFallaCorrida.getCantEscProgram(), datosFallaCorrida.getVarsControlDE().get("cantEscAForzar").getPeriodo(), Text.UNIDAD_PERIODOS);
            }else {
                variableDeEstadoCantEscController = new VariableDeEstadoFallaController(Text.VE_CANT_ESC_FORZ_LABEL, Text.VE_CANT_ESC_FORZ, 0, Text.UNIDAD_PERIODOS);
            }
            FXMLLoader loader1 = new FXMLLoader(getClass().getResource("VariableDeEstadoFalla.fxml"));
            loader1.setController(variableDeEstadoCantEscController);
            AnchorPane newLoadedPane1 = loader1.load();
            vBoxVE.getChildren().add(newLoadedPane1);

            /*if(edicion) {
                variableDeEstadoCantPerController = new VariableDeEstadoController(Text.VE_PER_FORZ_RESTANTES_LABEL, Text.VE_PER_FORZ_RESTANTES, 0, datosFallaCorrida.getVarsEstado().get(Text.VE_PER_FORZ_RESTANTES), Text.UNIDAD_PERIODOS);
            }else{
                variableDeEstadoCantPerController = new VariableDeEstadoController(Text.VE_PER_FORZ_RESTANTES_LABEL, Text.VE_PER_FORZ_RESTANTES, 0, Text.UNIDAD_PERIODOS);
            }
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("VariableDeEstado.fxml"));
            loader2.setController(variableDeEstadoCantPerController);
            AnchorPane newLoadedPane2 = loader2.load();
            vBoxVE.getChildren().add(newLoadedPane2);*/
        }catch(Exception e){
            e.printStackTrace();
        }
//        }
 /*
        try{
          if(edicion) {
                variableDeControlDEController = new VariableDeControlDEController(Text.VCDE_CANT_A_FORZ_LABEL, Text.VCDE_CANT_A_FORZ, datosFallaCorrida.getVarsControlDE().get(Text.VCDE_CANT_A_FORZ), datosFallaCorrida.getCantEscProgram());
            }else{
                variableDeControlDEController = new VariableDeControlDEController(Text.VCDE_CANT_A_FORZ_LABEL, Text.VCDE_CANT_A_FORZ, cantInputs);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VariableDeControlDE.fxml"));
            //loader.setController(variableDeControlDEController);
            AnchorPane newLoadedPane = loader.load();
            vBoxVCDE.getChildren().add(newLoadedPane);
        }catch (Exception e){
            e.printStackTrace();
        }
        */


        inputAceptar.setOnAction(actionEvent -> {
            if(editoVariables) {
            ArrayList<String> errores = controlDatosCompletos();
            if(errores.size() == 0 || errores == null) {
                String nombreNuevo = inputNombre.getText().trim();
                String nombreViejo = (edicion) ? datosFallaCorrida.getNombre().trim():"";
                if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                    setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                } else if(nombreNuevo.contains(" ")){
                    setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                } else {
                    loadData();
                    listaFallasController.refresh();
                    listaFallasController.actualizarLineaDeEntrada();
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
                if(inputNombre.getText().trim().equals("Falla clonada")){
                    setLabelMessageTemporal("Cambie el nombre de la falla clonada", TipoInfo.FEEDBACK);
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



        textFieldsEscalonesPorciento.put(1,inputEscalon1);

        if(edicion){
            unloadData();
        }else{
            evForField(null, "compFalla", inputCompFallaEV, Text.EV_VAR, Text.COMP_FALLA, inputCompFalla);
        }

        btnGuardarEnBiblioteca.setDisable(Manejador.user == null);
        btnGuardarEnBiblioteca.setOnAction(actionEvent -> {
            if (controlDatosCompletos().size() == 0) {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_FALLA_TEXT, datosFallaCorrida, datosCorrida, this, inputNombre.getText());
            } else {
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });



        //Control de cambios:

        inputCompFalla.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputEscalon1.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDemanda.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        controlHayCambiosEnEvoluciones();

        editoVariables = false;
        cambioComportamientoFalla();
    }

    private void cambioComportamientoFalla() {
        Boolean desHabilitar = false;
        if(inputCompFalla.getValue() != null && inputCompFalla.getValue().trim().equalsIgnoreCase(Constantes.FALLASINESTADO)){
            desHabilitar = true;
        }
        vBoxVE.setDisable(desHabilitar);
        //vBoxVCDE.setDisable(desHabilitar);

    }

    private void controlHayCambiosEnEvoluciones() {
        for (String clave : evsPorNombre.keySet()) {
            if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
                editoVariables = true;
            }
        }

    }

    private void controlHayCambiosEnVariablesEstadoVariablesControl() {

        if(variableDeEstadoCantEscController.isEditoVariables()) { editoVariables = true; }
        //if(variableDeEstadoCantPerController.isEditoVariables()) { editoVariables = true; }
       // if(variableDeControlDEController.isEditoVariables()) { editoVariables = true; }

    }



    private void cerrarVentana() {
        if(copiadoPortapapeles){
            listaFallasController.borrarFalla(datosFallaCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }



    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaFallasController.unloadData();
            listaFallasController.refresh();
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

    private void addEscalonPorciento(Pair<Double, Double> pa) {
        if(cantEscalonesPorciento < 6) {
            cantEscalonesPorciento++;
            JFXTextField nvoPA = new JFXTextField();
            nvoPA.setFocusColor(Paint.valueOf("#27864d"));
            nvoPA.setStyle("-fx-background-color: WHITE; -fx-font-size: 15px;");
            nvoPA.setMinWidth(85);
            nvoPA.setMaxWidth(85);
            GridPane.setMargin(nvoPA, new Insets(0,0,0,5));

            if (pa != null) {
                nvoPA.setText(pa.first + ";" + pa.second);
            }
            gridPane.add(nvoPA, cantEscalonesPorciento, 7);
            gridPane.setColumnIndex(btnRemoveEscalonPorciento, cantEscalonesPorciento + 1);
            gridPane.setColumnIndex(btnAddEscalonPorciento, cantEscalonesPorciento + 1);
            textFieldsEscalonesPorciento.put(cantEscalonesPorciento, nvoPA);
        }
    }

    private void removeEscalonPorciento(){
        if(cantEscalonesPorciento > 1) {
            gridPane.getChildren().remove(textFieldsEscalonesPorciento.get(cantEscalonesPorciento));
            gridPane.setColumnIndex(btnAddEscalonPorciento, cantEscalonesPorciento);
            gridPane.setColumnIndex(btnRemoveEscalonPorciento, cantEscalonesPorciento );
            cantEscalonesPorciento--;
        }
    }

    private void cargarEscalonPorciento(String dato, List<Pair<Double, Double>> posArr) {
        String[] par = dato.split(";");
        if(UtilStrings.esNumeroDouble(par[0])  && UtilStrings.esNumeroDouble(par[1]) ){
            posArr.add(new Pair<>(Double.parseDouble(par[0]), Double.parseDouble(par[1])));
        }

    }

    /**
     *  Método para cargar los datos en el DatosFallaEscalonadaCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-FALLA");

        Evolucion<String> compFalla = EVController.loadEVsegunTipo(inputCompFalla, Text.EV_VAR, evsPorNombre,"compFalla");
        Hashtable<String, Evolucion<String>> valsComps = new Hashtable<>();
        valsComps.put(COMPFALLA, compFalla);

        String nombre = inputNombre.getText();
        String demanda = inputDemanda.getValue();


//        ArrayList<Pair<Double, Double>> escalones; //ESCALONES POR CIENTO
        boolean salidaDetallada = inputSalidaDetallada.isSelected();


        Double[] costoControl = new Double[variableDeEstadoCantEscController.getCantEscProgram()];
        for(int i =0; i<costoControl.length; i++){
            costoControl[i] = 0.0;
        }

        ArrayList<Pair<Double, Double>> escalonesPorciento = new ArrayList<> ();
        textFieldsEscalonesPorciento.forEach((k,v) ->  cargarEscalonPorciento( v.getText(),escalonesPorciento));



        DatosVariableEstado datosVariableEstadoCantEsc = variableDeEstadoCantEscController.getDatosVariableEstado();
        //DatosVariableEstado datosVariableEstadoCantPer = variableDeEstadoCantPerController.getDatosVariableEstado();
        Hashtable<String,DatosVariableEstado> varsEstado = new Hashtable<>();
        varsEstado.put(datosVariableEstadoCantEsc.getNombre(), datosVariableEstadoCantEsc);
        //varsEstado.put(datosVariableEstadoCantPer.getNombre(), datosVariableEstadoCantPer);
        Hashtable<String, DatosVariableControlDE> varsControlDE = new Hashtable<>();
        DatosVariableControlDE datosVariableControlDE = new DatosVariableControlDE("cantEscAForzar");
        datosVariableControlDE.setPeriodo(variableDeEstadoCantEscController.getFrecuenciaControlPerminDesp());
        Evolucion<Double[]> costoDeControl = new EvolucionConstante<>(costoControl, datosCorrida.getLineaTiempo().getSentido());
        datosVariableControlDE.setCostoDeControl(costoDeControl);
        varsControlDE.put(datosVariableControlDE.getNombre(), datosVariableControlDE);
        int cantEscProgram = variableDeEstadoCantEscController.getCantEscProgram();

        if(edicion){
            String nombreViejo = datosFallaCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getFallas().getFallas().remove(nombreViejo);
                int pos = datosCorrida.getFallas().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getFallas().getOrdenCargaXML().set(pos, nombre);
                pos = datosCorrida.getFallas().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getFallas().getListaUtilizados().set(pos, nombre);
            }
            datosFallaCorrida.setNombre(nombre);
            datosFallaCorrida.setDemanda(demanda);
            datosFallaCorrida.setCantEscProgram(cantEscProgram);
        //   datosFallaCorrida.setDurMinForzSeg(durMinForzSeg);
            datosFallaCorrida.setCompFalla(compFalla);
            datosFallaCorrida.setValsComps(valsComps);
            datosFallaCorrida.setVarsEstado(varsEstado);
            datosFallaCorrida.setVarsControlDE(varsControlDE);
            datosFallaCorrida.setSalDetallada(salidaDetallada);
            datosFallaCorrida.setEscalones(escalonesPorciento);
        }else {
            datosFallaCorrida = new DatosFallaEscalonadaCorrida(nombre, compFalla, valsComps, demanda,  escalonesPorciento, cantEscProgram,
                                                                null, varsEstado, varsControlDE, salidaDetallada);
            datosCorrida.getFallas().getListaUtilizados().add(nombre);
            datosCorrida.getFallas().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getFallas().getFallas().put(nombre, datosFallaCorrida);

    }

    /**
     * Método para obtener los datos del DatosFallaEscalonadaCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-FALLA");
        //inputCompFalla.getSelectionModel().select(datosFallaCorrida.getCompFalla().getValor());
        inputCompFalla.getSelectionModel().select(datosFallaCorrida.getValsComps().get(COMPFALLA).getValor(CorridaHandler.getInstance().dameInstanteActual()));
        evForField(datosFallaCorrida.getValsComps().get(COMPFALLA), "compFalla", inputCompFallaEV, Text.EV_VAR, Text.COMP_FALLA, inputCompFalla);

        inputNombre.setText(datosFallaCorrida.getNombre());


        int pos = 0;
        for( Pair<Double, Double>  entry: datosFallaCorrida.getEscalones()){
            if(pos==0){  inputEscalon1.setText(entry.first + ";" + entry.second); }
            else{ addEscalonPorciento(entry); }
            pos++;
        }

        inputDemanda.getSelectionModel().select(datosFallaCorrida.getDemanda());
        inputSalidaDetallada.setSelected(datosFallaCorrida.isSalDetallada());
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if(inputCompFalla.getValue() == null){ errores.add( "Editor Falla: " + inputNombre.getText() +" Comportamientos generales vacío.");}
        if(inputNombre.getText().trim().equals("")) { errores.add( "Editor Falla.  Nombre vacío"); }


        double suma = 0;
        for (JFXTextField v : textFieldsEscalonesPorciento.values()) {
            if(!UtilStrings.esParDeNumeroEntero(v.getText())){
                errores.add( "Editor Falla.  "+ inputNombre.getText() + " Escalones porciento no es dato numérico");
            } else {
                String[] par = v.getText().split(";");
                suma += Double.parseDouble(par[0]);
            }

        }
        if(suma != 100){
            errores.add( "Editor Falla.  "+ inputNombre.getText() + " La suma de Escalones porciento no es 100");
        }

        if (inputDemanda.getValue() == null ) { errores.add( "Editor Falla.  "+ inputNombre.getText() +" Demanda vacía"); }

        errores.addAll(variableDeEstadoCantEscController.controlDatosCompletos());
       // errores.addAll(variableDeEstadoCantPerController.controlDatosCompletos());
        //errores.addAll(variableDeControlDEController.controlDatosCompletos());

        if(inputDemanda.getValue() == null){ errores.add( "Editor Falla: " + inputNombre.getText() +" Demanda  vacía.");}

        //TODO: hay que controlar que la suma de los escalnes porciento de 100
        //Te diría que a lo sumo 8 escalones (exagerando) es lo que se puede agregar.
        // No depende de otra variable, lo único necesario es que la suma de los primeros componentes de todos los escalones sea 100.

        return errores;
    }

}
