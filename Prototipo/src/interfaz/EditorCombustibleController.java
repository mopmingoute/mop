/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorCombustibleController is part of MOP.
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
import datatypes.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.*;

public class EditorCombustibleController extends GeneralidadesController {

    @FXML private VBox vBoxCanioSimple;
    @FXML private JFXButton btnAddCanioSimple;
    @FXML private JFXButton btnRemoveCanioSimple;
    @FXML private Label labelCantCanios;
    @FXML private VBox vBoxDucto;
    @FXML private JFXButton btnAddDucto;
    @FXML private JFXButton btnRemoveDucto;
    @FXML private Label labelCantDuctos;
    @FXML private VBox vBoxBarra;
    @FXML private JFXButton btnAddBarra;
    @FXML private JFXButton btnRemoveBarra;
    @FXML private Label labelCantBarras;

    private Integer cantBarras = 0;
    private Integer cantDuctos = 0;
    private Integer cantCanios = 0;

    private HashMap<Integer, EditorBarraController> barrasControllers = new HashMap<>();
    private HashMap<Integer, EditorDuctoController> ductosControllers = new HashMap<>();
    private HashMap<Integer, EditorCanioSimpleController> caniosControllers = new HashMap<>();

    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputUnidad;
    @FXML private JFXTextField inputPciPorUnidad;
    @FXML private JFXComboBox<String> inputPciPorUnidadUnidad;
    @FXML private JFXTextField inputDensidad;
    @FXML private JFXComboBox<String> inputDensidadUnidad;
    @FXML private JFXCheckBox inputSalidaDetallada;
    @FXML private JFXComboBox<String> inputCompUsoRed;
    @FXML private JFXButton inputCompUsoRedEV;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;

    @FXML private JFXTabPane tabPane;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    public DatosCorrida datosCorrida;
    private DatosCombustibleCorrida datosCombustibleCorrida;
    private HashMap<Integer, DatosBarraCombCorrida> barras = new HashMap<>();
    private HashMap<Integer, DatosDuctoCombCorrida> ductos = new HashMap<>();
    private HashMap<Integer, DatosContratoCombustibleCorrida> contratos = new HashMap<>();
    private boolean edicion;
    private ListaCombustiblesController listaCombustiblesController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorCombustibleController(DatosCorrida datosCorrida, ListaCombustiblesController listaCombustiblesController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaCombustiblesController = listaCombustiblesController;
    }

    public EditorCombustibleController(DatosCorrida datosCorrida, DatosCombustibleCorrida datosCombustibleCorrida, ListaCombustiblesController listaCombustiblesController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosCombustibleCorrida = datosCombustibleCorrida;
        for(int i=0;i<datosCombustibleCorrida.getRed().getBarras().size();i++){
            this.barras.put(i, datosCombustibleCorrida.getRed().getBarras().get(i));
        }
        for(int i=0;i<datosCombustibleCorrida.getRed().getDuctos().size();i++){
            this.ductos.put(i, datosCombustibleCorrida.getRed().getDuctos().get(i));
        }
        for(int i=0;i<datosCombustibleCorrida.getRed().getContratos().size();i++){
            this.contratos.put(i, datosCombustibleCorrida.getRed().getContratos().get(i));
        }
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaCombustiblesController = listaCombustiblesController;
    }

    @FXML
    public void initialize(){

        inputUnidad.getItems().addAll(new ArrayList<>(Arrays.asList(Text.UNIDAD_M3)));
        inputUnidad.getSelectionModel().selectFirst();

//        inputCompUsoRed.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.UNINODAL)));
        inputCompUsoRed.getItems().addAll(Text.COMP_USO_RED_VALS);
        inputCompUsoRed.getSelectionModel().selectFirst();

        inputPciPorUnidadUnidad.getItems().add(Text.UNIDAD_MWH_M3);
        inputPciPorUnidadUnidad.getSelectionModel().selectFirst();

        inputDensidadUnidad.getItems().add(Text.UNIDAD_KG_M3);
        inputDensidadUnidad.getSelectionModel().selectFirst();

        btnAddCanioSimple.setOnAction(actionEvent -> addCanioSimple(-1));
        btnRemoveCanioSimple.setOnAction(actionEvent -> removeCanioSimple());

        btnAddDucto.setOnAction(actionEvent -> addDucto(-1));
        btnRemoveDucto.setOnAction(actionEvent -> removeDucto());

        btnAddBarra.setOnAction(actionEvent -> addBarra(-1));
        btnRemoveBarra.setOnAction(actionEvent -> removeBarra());

        if(edicion){
            unloadData();
            for(int barraIndex : barras.keySet()){
                addBarra(barraIndex);
            }
            for(int ductoIndex : ductos.keySet()){
                addDucto(ductoIndex);
            }
            for(int canioIndex : contratos.keySet()){
                addCanioSimple(canioIndex);
            }
        }else{
            addBarra(-1);
            addDucto(-1);
            addCanioSimple(-1);
            evForField(null, "compUsoRed", inputCompUsoRedEV, Text.EV_VAR, Text.COMP_USO_RED_VALS, inputCompUsoRed);
        }
        inputAceptar.setOnAction(actionEvent -> {
            verificacionCambiosEnControllers();
            if(editoVariables) {
            ArrayList<String> errores = controlDatosCompletos();
            if(errores.size() == 0 || errores == null) {
                String nombreNuevo = inputNombre.getText().trim();
                String nombreViejo = (edicion) ? datosCombustibleCorrida.getNombre().trim():"";
                if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                    setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                } else if(nombreNuevo.contains(" ")){
                    setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                } else {
                    loadData();
                    listaCombustiblesController.refresh();
                    listaCombustiblesController.actualizarLineaDeEntrada();
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
                if(inputNombre.getText().trim().equals("Combustible clonado")){
                    setLabelMessageTemporal("Cambie el nombre del combustible clonado", TipoInfo.FEEDBACK);
                }else{
                    ((Stage)inputCancelar.getScene().getWindow()).close();
                }
            }
        });
        inputCancelar.setOnAction(actionEvent -> {
            verificacionCambiosEnControllers();
            if(editoVariables){
                mostrarMensajeConfirmacion(Text.MSG_CONF_CERRAR_SIN_GUARDAR,2 );
            }
            else{
                editoVariables = false;
                cerrarVentana();
            }
        });


        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> {
            if(oldTab.getText().equalsIgnoreCase("Barras")){
                for(EditorDuctoController editorDuctoController : ductosControllers.values()){
                    editorDuctoController.updateBarras();
                }
                for(EditorCanioSimpleController editorCanioSimpleController : caniosControllers.values()){
                    editorCanioSimpleController.updateBarras();
                }
            }
        });

        btnGuardarEnBiblioteca.setDisable(Manejador.user == null);
        btnGuardarEnBiblioteca.setOnAction(actionEvent -> {
            ArrayList<String> bloquesConError = controlDatosCompletos();
            if(bloquesConError.size() == 0 || bloquesConError == null) {
            //if(controlDatosCompletos()) {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_COMBUSTIBLE_TEXT, datosCombustibleCorrida, datosCorrida, this, inputNombre.getText());
            }else{
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }
        });

        //Control de cambios:

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPciPorUnidad.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDensidad.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPciPorUnidadUnidad.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputUnidad.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCompUsoRed.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
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

    private boolean verificacionCambiosEnControllers(){
        for(EditorBarraController b: barrasControllers.values()){
            if(b.isEditoVariables()) editoVariables = true;
        }
        for(EditorDuctoController d: ductosControllers.values()){
            if(d.isEditoVariables()) editoVariables = true;
        }
        for(EditorCanioSimpleController c: caniosControllers.values()){
            if(c.isEditoVariables()) editoVariables = true;
        }
        return editoVariables;
    }

    private  void cerrarVentana(){
        if(copiadoPortapapeles){
            listaCombustiblesController.borrarCombustible(datosCombustibleCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }
    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaCombustiblesController.unloadData();
            listaCombustiblesController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    }

    private void addCanioSimple(int index){
        try{
            EditorCanioSimpleController controller;
            if(index >= 0){
                controller = new EditorCanioSimpleController(this, contratos.get(index));
            }else{
                controller = new EditorCanioSimpleController(this);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditorCanioSimple.fxml"));
            loader.setController(controller);
            AnchorPane newLoadedPane = loader.load();
            vBoxCanioSimple.getChildren().add(0,newLoadedPane);
            cantCanios++;
            labelCantCanios.setText("Contratos(s): " + cantCanios);
            caniosControllers.put(index, controller);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void removeCanioSimple(){
        if(vBoxCanioSimple.getChildrenUnmodifiable().size() > 0) {
            vBoxCanioSimple.getChildren().remove(0);
            cantCanios--;
            labelCantCanios.setText("Contratos(s): " + cantCanios);
        }
    }

    private void addDucto(int index){
        try{
            EditorDuctoController controller;
            if(index >= 0){
                controller = new EditorDuctoController(this, ductos.get(index));
            }else{
                controller = new EditorDuctoController(this);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditorDucto.fxml"));
            loader.setController(controller);
            AnchorPane newLoadedPane = loader.load();
            vBoxDucto.getChildren().add(0,newLoadedPane);
            cantDuctos++;
            labelCantDuctos.setText("Ducto(s): " + cantDuctos);
//            ductosControllers.put(index, controller);
            ductosControllers.put(cantDuctos-1, controller);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void removeDucto(){
        if(vBoxDucto.getChildrenUnmodifiable().size() > 0) {
            vBoxDucto.getChildren().remove(0);
            cantDuctos--;
            labelCantDuctos.setText("Ducto(s): " + cantDuctos);
        }
    }

    private void addBarra(int index){
        try{
            EditorBarraController controller;
            if(index >= 0){
                controller = new EditorBarraController(barras.get(index));
            }else{
                controller = new EditorBarraController();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditorBarra.fxml"));
            loader.setController(controller);
            AnchorPane newLoadedPane = loader.load();
            vBoxBarra.getChildren().add(0, newLoadedPane);
            cantBarras++;
            labelCantBarras.setText("Barra(s): " + cantBarras);
//            barrasControllers.put(index, controller);
            barrasControllers.put(cantBarras-1, controller);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void removeBarra(){
        if(vBoxBarra.getChildrenUnmodifiable().size() > 0) {
            vBoxBarra.getChildren().remove(0);
            cantBarras--;
            labelCantBarras.setText("Barra(s): " + cantBarras);
        }
    }


    public ArrayList<String> getListaBarras(){
        ArrayList<String> res = new ArrayList<>();
        for(EditorBarraController editorBarraController : barrasControllers.values()){
            res.add(editorBarraController.getDatosBarraCombCorrida().getNombre());
        }
        return res;
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
     *  Método para cargar los datos en el DatosCombustibleCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-COMBUSTIBLE");
        String nombre = inputNombre.getText();
        String unidad = inputUnidad.getValue();
        double pciPorUnidad = 0;
        if(UtilStrings.esNumeroDouble(inputPciPorUnidad.getText())) { pciPorUnidad = Double.parseDouble(inputPciPorUnidad.getText()); }
//        inputPciPorUnidadUnidad
        double densidad = 0;
        if (UtilStrings.esNumeroDouble(inputDensidad.getText())) { densidad = Double.parseDouble(inputDensidad.getText()); }
//        inputDensidadUnidad
        DatosRedCombustibleCorrida red = new DatosRedCombustibleCorrida();
        red.setNombre(nombre);
        ArrayList<DatosBarraCombCorrida> barras = new ArrayList<>();
        for(EditorBarraController editorBarra : barrasControllers.values()) {
           barras.add(editorBarra.getDatosBarraCombCorrida());
        }
        ArrayList<DatosDuctoCombCorrida> ductos = new ArrayList<>();
        for(EditorDuctoController editorDucto : ductosControllers.values()){
            ductos.add(editorDucto.getDatosDuctoCombCorrida());
        }
//        ArrayList<DatosTanqueCombustibleCorrida> tanques;
        ArrayList<DatosContratoCombustibleCorrida> contratos = new ArrayList<>();
        for(EditorCanioSimpleController editorCanio : caniosControllers.values()){
            contratos.add(editorCanio.getDatosContratoCombustibleCorrida());
        }
        Hashtable<String, Evolucion<String>> valoresComportamiento = new Hashtable<>();
        Evolucion<String> usoRed = EVController.loadEVsegunTipo(inputCompUsoRed, Text.EV_VAR, evsPorNombre, "compUsoRed");
        valoresComportamiento.put(Constantes.COMPRED, usoRed);
        red.setValoresComportamiento(valoresComportamiento);
        red.setBarras(barras);
        red.setDuctos(ductos);
        red.setContratos(contratos);
        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        if(edicion){
            String nombreViejo = datosCombustibleCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getCombustibles().getCombustibles().remove(nombreViejo);
                int pos = datosCorrida.getCombustibles().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getCombustibles().getOrdenCargaXML().set(pos, nombre);
                pos = datosCorrida.getCombustibles().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getCombustibles().getListaUtilizados().set(pos, nombre);
            }
            datosCombustibleCorrida.setNombre(nombre);
            datosCombustibleCorrida.setUnidad(unidad);
            datosCombustibleCorrida.setPciPorUnidad(pciPorUnidad);
            datosCombustibleCorrida.setDensidad(densidad);
            datosCombustibleCorrida.setSalDetallada(salidaDetallada);
            datosCombustibleCorrida.setRed(red);
        }else {
            datosCombustibleCorrida = new DatosCombustibleCorrida(nombre, unidad, pciPorUnidad, densidad, salidaDetallada);
            datosCombustibleCorrida.setRed(red);
            datosCorrida.getCombustibles().getListaUtilizados().add(nombre);
            datosCorrida.getCombustibles().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getCombustibles().getCombustibles().put(nombre, datosCombustibleCorrida);
    }

    /**
     * Método para obtener los datos del DatosCombustibleCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-COMBUSTIBLE");
        inputNombre.setText(datosCombustibleCorrida.getNombre());
//        inputUnidad.getSelectionModel().select(datosCombustibleCorrida.getUnidad());//TODO: generalizar a otras unidades
        inputPciPorUnidad.setText(String.valueOf(datosCombustibleCorrida.getPciPorUnidad()));
//        inputPciPorUnidadUnidad
        inputDensidad.setText(String.valueOf(datosCombustibleCorrida.getDensidad()));
//        inputDensidadUnidad
        inputSalidaDetallada.setSelected(datosCombustibleCorrida.isSalDetallada());
        evForField(datosCombustibleCorrida.getRed().getValoresComportamiento().get(Constantes.COMPRED), "compUsoRed", inputCompUsoRedEV, Text.EV_VAR, Text.COMP_USO_RED_VALS, inputCompUsoRed);
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();
        if( inputNombre.getText().trim().equalsIgnoreCase("") ) errores.add("Editor Combustible. Nombre vacío.");
        if( inputPciPorUnidad.getText().trim().equalsIgnoreCase("") ) errores.add("Editor Combustible:  " + inputNombre.getText() +" PCI vacío.");
        if( inputDensidad.getText().trim().equalsIgnoreCase("") ) errores.add("Editor Combustible:  " + inputNombre.getText() +" Densidad vacío.");
        if( inputCompUsoRed.getValue() == null ) errores.add("Editor Combustible:  " + inputNombre.getText() +" Uso red vacío.");


        for (Map.Entry<Integer, EditorBarraController> entry : barrasControllers.entrySet()) {
            if(entry.getValue().getDatosBarraCombCorrida().getNombre().trim().equals("")){
                errores.add("Editor Combustible:  " + inputNombre.getText() +" Nombre barra vacío.");
            }
        }
        for (Map.Entry<Integer, EditorDuctoController> entry : ductosControllers.entrySet()) {
            if(entry.getValue().getDatosDuctoCombCorrida().getNombre().trim().equals("")){
                errores.add("Editor Combustible:  " + inputNombre.getText() +" Nombre ducto vacío.");
            }
            errores.addAll(entry.getValue().controlDatosCompletos());

        }
        for (Map.Entry<Integer, EditorCanioSimpleController> entry : caniosControllers.entrySet()) {
            if(entry.getValue().getDatosContratoCombustibleCorrida().getNombre().trim().equals("")){
                errores.add("Editor Combustible:  " + inputNombre.getText() +" Nombre contrato/canio vacío.");
            }
            errores.addAll(entry.getValue().controlDatosCompletos());

        }

        return errores;
    }

}
