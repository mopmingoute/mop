/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorImpoExpoController is part of MOP.
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
import datatypes.DatosImpoExpoCorrida;
import datatypes.DatosPolinomio;
import datatypes.DatosVariableAleatoria;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EditorImpoExpoController extends GeneralidadesController {
    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;
    @FXML private JFXComboBox<String> inputPais;
    @FXML private JFXComboBox<String> inputTipoImpoExpo;
    @FXML private JFXTextField inputCostoFijo;
    @FXML private JFXButton inputCostoFijoEV;
    @FXML private JFXTextField inputCantModInst;
    @FXML private JFXButton inputCantModInstEV;

    @FXML private JFXRadioButton inputOpCompra;
    @FXML private JFXRadioButton inputOpVenta;
    @FXML private JFXTextField inputMinimoTecnico;
    @FXML private JFXButton inputMinimoTecnicoEV;
    @FXML private JFXCheckBox inputHayMinimoTecnico;

    @FXML private JFXComboBox<String> inputCmgProcOpt;
    @FXML private JFXComboBox<String> inputCmgProcSim;
    @FXML private JFXComboBox<String> inputCmgNombre;
    @FXML private JFXTextField inputCmgFactorEscalamiento;
    @FXML private JFXButton inputCmgFactorEscalamientoEV;

    @FXML private JFXCheckBox inputSalidaDetallada;
    @FXML private JFXButton btnRemoveBloque;
    @FXML private JFXButton btnAddBloque;



    private HashMap<Integer, JFXTextField> inputsPrecioEvol;
    private HashMap<Integer, JFXTextField> inputsPotenciaEvol;
    private HashMap<Integer, JFXTextField> inputsDispEvol;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;

    @FXML private VBox vBoxBloques;

    private int cantBloques = 0;
    private ArrayList<BloqueImpoExpoController> bloqueImpoExpoControllers = new ArrayList<>();

    private DatosCorrida datosCorrida;
    private DatosImpoExpoCorrida datosImpoExpoCorrida;
    private boolean edicion;
    private ListaImpoExpoController listaImpoExpoController;
    private String tipoImpoExpo;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();
    private boolean copiadoPortapapeles;
    private boolean editoVariables;

    public EditorImpoExpoController(DatosCorrida datosCorrida, ListaImpoExpoController listaImpoExpoController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaImpoExpoController = listaImpoExpoController;
        tipoImpoExpo = Constantes.IEEVOL;
    }

    public EditorImpoExpoController(DatosCorrida datosCorrida, DatosImpoExpoCorrida datosImpoExpoCorrida, ListaImpoExpoController listaImpoExpoController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosImpoExpoCorrida = datosImpoExpoCorrida;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaImpoExpoController = listaImpoExpoController;
        tipoImpoExpo = datosImpoExpoCorrida.getTipoImpoExpo();
        cantBloques = datosImpoExpoCorrida.getCantBloques();
    }

    @FXML
    public void initialize(){

        Manejador.popularListaNombresVA(datosCorrida, inputCmgProcSim, inputCmgProcOpt, inputCmgNombre);

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getSelectionModel().selectFirst();

        inputTipoImpoExpo.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.IEEVOL, Constantes.IEALEATFORMUL, Constantes.IEALEATPRPOT)));

        inputPais.getItems().addAll(Text.PAISES);


        inputAceptar.setOnAction(actionEvent -> {
            controlHayCambiosEnBloques();
            controlHayCambiosEnEvoluciones();
            if(editoVariables) {
                ArrayList errores = controlDatosCompletos();
                if(errores.size() == 0 || errores == null ){
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosImpoExpoCorrida.getNombre().trim():"";
                    if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getHidraulicos().getOrdenCargaXML(), edicion)){
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaImpoExpoController.refresh();
                        listaImpoExpoController.actualizarLineaDeEntrada();
                        ((Stage) inputAceptar.getScene().getWindow()).close();
                        editoVariables = false;
                    }
                }else {
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
                if(inputNombre.getText().trim().equals("ImpoExpo clonado")){
                    setLabelMessageTemporal("Cambie el nombre de la ImpoExpo clonada", TipoInfo.FEEDBACK);
                }else{
                    ((Stage)inputCancelar.getScene().getWindow()).close();
                }
                }

        });
        inputCancelar.setOnAction(actionEvent -> {
            controlHayCambiosEnBloques();
            controlHayCambiosEnEvoluciones();
            if(editoVariables){
                mostrarMensajeConfirmacion(Text.MSG_CONF_CERRAR_SIN_GUARDAR,2 );
            }
            else{
                editoVariables = false;
                cerrarVentana();
            }
        });

        btnRemoveBloque.setOnAction( actionEvent -> {
            int oldCantBloques = cantBloques;
            cantBloques --;
            logicaBloques(oldCantBloques, cantBloques);
        });

        btnAddBloque.setOnAction( actionEvent -> {
            int oldCantBloques = cantBloques;
            cantBloques ++;
            logicaBloques(oldCantBloques, cantBloques);
        });


        //TODO: resetear lista data
        inputTipoImpoExpo.setOnAction(actionEvent -> {
            String newTipoImpoExpo = inputTipoImpoExpo.getValue();
            if(!newTipoImpoExpo.equals(tipoImpoExpo)){
                tipoImpoExpo = newTipoImpoExpo;
                for(BloqueImpoExpoController bloqueImpoExpoController : bloqueImpoExpoControllers){
                    bloqueImpoExpoController.disableInputs(tipoImpoExpo);
                }
                disableCmg(newTipoImpoExpo);
            }
        });

        disableCmg(tipoImpoExpo);

        btnGuardarEnBiblioteca.setDisable(Manejador.user == null);
        btnGuardarEnBiblioteca.setOnAction(actionEvent -> {
            ArrayList<String> errores = controlDatosCompletos();
            if (errores.size() > 0) {
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            } else {
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_IMPOEXPO_TEXT, datosImpoExpoCorrida, datosCorrida, this, inputNombre.getText());
            }
        });
        inputHayMinimoTecnico.setOnAction(actionEvent -> {
            if(inputHayMinimoTecnico.isSelected()){
                inputMinimoTecnico.setDisable(false);
                inputMinimoTecnicoEV.setDisable(false);
            }else{
                inputMinimoTecnico.setDisable(true);
                inputMinimoTecnicoEV.setDisable(true);
            }
        });

        if(edicion){
            unloadData();
        }else{
            evForField(null, "minimoTecnico", inputMinimoTecnicoEV, Text.EV_NUM_INT, null, inputMinimoTecnico);
            evForField(null, "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);
            evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);
            evForField(null, "factorEscalamiento", inputCmgFactorEscalamientoEV, Text.EV_NUM_DOUBLE, null, inputCmgFactorEscalamiento);
            cantBloques = 1;
            logicaBloques(0,1);
            inputHayMinimoTecnico.setSelected(true);
            inputTipoImpoExpo.getSelectionModel().select(Constantes.IEEVOL);


        }

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPais.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTipoImpoExpo.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputOpCompra.selectedProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputHayMinimoTecnico.selectedProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFijo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputMinimoTecnico.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputCmgProcSim.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCmgProcOpt.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCmgNombre.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCmgFactorEscalamiento.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
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

    private void controlHayCambiosEnBloques() {
        for(int i = 0; i < bloqueImpoExpoControllers.size() ; i++) {
            if(bloqueImpoExpoControllers.get(i).isEditoVariables()){
                editoVariables = true;
            }
        }
    }



    private void cerrarVentana() {
        if(copiadoPortapapeles){
            listaImpoExpoController.borrarImpoExpo(datosImpoExpoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaImpoExpoController.unloadData();
            listaImpoExpoController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    }
    private void disableCmg(String tipo){
        if(!tipo.equals(Constantes.IEALEATFORMUL)){
            inputCmgNombre.setDisable(true);
            inputCmgProcSim.setDisable(true);
            inputCmgProcOpt.setDisable(true);
            inputCmgFactorEscalamiento.setDisable(true);
            inputCmgFactorEscalamientoEV.setDisable(true);

        }else{
            inputCmgNombre.setDisable(false);
            inputCmgProcSim.setDisable(false);
            inputCmgProcOpt.setDisable(false);
            inputCmgFactorEscalamiento.setDisable(false);
            inputCmgFactorEscalamientoEV.setDisable(false);
        }
    }

    private void logicaBloques(int oldCantBloques, int newCantBloques){
        if(newCantBloques > oldCantBloques) {
            for (int i = oldCantBloques;i < newCantBloques;i++) {
                try {
                    BloqueImpoExpoController bloqueController = new BloqueImpoExpoController(datosCorrida, tipoImpoExpo);
                    bloqueImpoExpoControllers.add(bloqueController);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueImpoExpo.fxml"));
                    loader.setController(bloqueController);
                    AnchorPane newLoadedPane = loader.load();
                    vBoxBloques.getChildren().add(newLoadedPane);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            for(int i = oldCantBloques; i > newCantBloques; i--){
                vBoxBloques.getChildren().remove(vBoxBloques.getChildren().size()-1);
                bloqueImpoExpoControllers.remove(bloqueImpoExpoControllers.size()-1);
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

    /**
     *  Método para cargar los datos en el DatosImpoExpoCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-IMPO/EXPO");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();
        String pais = inputPais.getValue();
        String tipoImpoExpo = inputTipoImpoExpo.getValue();
        String opCompraVenta = inputOpCompra.isSelected() ? Constantes.PROVCOMPRA : Constantes.PROVVENTA;
        Evolucion<Double> costoFijo = EVController.loadEVsegunTipo(inputCostoFijo, Text.EV_NUM_DOUBLE, evsPorNombre, "costoFijo");
        Evolucion<Integer> cantModIns = EVController.loadEVsegunTipo(inputCantModInst, Text.EV_NUM_INT, evsPorNombre, "cantModInst");
        boolean hayMinimoTecnico = inputHayMinimoTecnico.isSelected();
        Evolucion<Double> minimoTecnico = null;
        if(hayMinimoTecnico){
            minimoTecnico = EVController.loadEVsegunTipo(inputMinimoTecnico, Text.EV_NUM_DOUBLE, evsPorNombre, "minimoTecnico");
        }
        boolean salidaDetallada = inputSalidaDetallada.isSelected();
        DatosVariableAleatoria datCmg = new DatosVariableAleatoria(inputCmgProcOpt.getValue(), inputCmgProcSim.getValue(), inputCmgNombre.getValue());
        DatosVariableAleatoria datUniforme = new DatosVariableAleatoria(Text.datosVarAleatUniforme,Text.datosVarAleatUniforme,Text.nommbreVarAleatUniforme);
        Evolucion<Double> factorEscalamiento = EVController.loadEVsegunTipo(inputCmgFactorEscalamiento, Text.EV_NUM_DOUBLE, evsPorNombre, "factorEscalamiento");



        if(edicion){
            String nombreViejo = datosImpoExpoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getImpoExpos().getImpoExpos().remove(nombreViejo);
                int pos = datosCorrida.getImpoExpos().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getImpoExpos().getOrdenCargaXML().set(pos, nombre);
                pos = datosCorrida.getImpoExpos().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getImpoExpos().getListaUtilizados().set(pos, nombre);

            }
            datosImpoExpoCorrida.setNombre(nombre);
            datosImpoExpoCorrida.setBarra(barra);
            datosImpoExpoCorrida.setPais(pais);
            datosImpoExpoCorrida.setTipoImpoExpo(tipoImpoExpo);
            datosImpoExpoCorrida.setOperacionCompraVenta(opCompraVenta);
            datosImpoExpoCorrida.setCostoFijo(costoFijo);
            datosImpoExpoCorrida.setCantModInst(cantModIns);
            datosImpoExpoCorrida.setCantBloques(cantBloques);
            datosImpoExpoCorrida.setSalDetallada(salidaDetallada);
            datosImpoExpoCorrida.setFactorEscalamiento(factorEscalamiento);
            datosImpoExpoCorrida.setDatUniforme(datUniforme);
        }else {
            datosImpoExpoCorrida = new DatosImpoExpoCorrida(nombre, null,cantModIns,barra, pais, tipoImpoExpo, opCompraVenta, costoFijo,
                                                            cantBloques, salidaDetallada, hayMinimoTecnico, minimoTecnico, datCmg, factorEscalamiento, datUniforme);
            datosCorrida.getImpoExpos().getListaUtilizados().add(nombre);
            datosCorrida.getImpoExpos().getOrdenCargaXML().add(nombre);
        }


        if(tipoImpoExpo != null && tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {
            datosImpoExpoCorrida.setDatCMg(datCmg);
        }
        //Datos bloques
        //Se reinician las listas sino al abrir y cerrar re carga lo que ya habia.
        datosImpoExpoCorrida.setPotEvol(new ArrayList<>());
        datosImpoExpoCorrida.setPreEvol(new ArrayList<>());
        datosImpoExpoCorrida.setDispEvol(new ArrayList<>());
        datosImpoExpoCorrida.setDatPotencia(new ArrayList<>());
        datosImpoExpoCorrida.setDatPrecio(new ArrayList<>());
        datosImpoExpoCorrida.setPoliPot(new ArrayList<>());
        datosImpoExpoCorrida.setPoliDisp(new ArrayList<>());
        datosImpoExpoCorrida.setPoliPre(new ArrayList<>());

        for(BloqueImpoExpoController bloqueImpoExpoController : bloqueImpoExpoControllers){
            bloqueImpoExpoController.loadData(tipoImpoExpo, datosImpoExpoCorrida);
        }
        datosCorrida.getImpoExpos().getImpoExpos().put(nombre, datosImpoExpoCorrida);
    }

    /**
     * Método para obtener los datos del DatosImpoExpoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-IMPO/EXPO");

        inputNombre.setText(datosImpoExpoCorrida.getNombre());
//        inputBarra.getSelectionModel().select(datosImpoExpoCorrida.getBarra());
        inputBarra.getSelectionModel().select(Text.BARRA_1);//uninodal hardcodeado
        inputPais.getSelectionModel().select(datosImpoExpoCorrida.getPais());
        inputTipoImpoExpo.getSelectionModel().select(datosImpoExpoCorrida.getTipoImpoExpo());
        inputOpCompra.setSelected(datosImpoExpoCorrida.getOperacionCompraVenta().equals(Constantes.PROVCOMPRA));
        inputOpVenta.setSelected(datosImpoExpoCorrida.getOperacionCompraVenta().equals(Constantes.PROVVENTA));
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        if(datosImpoExpoCorrida.getCantModInst() != null) {
        inputCantModInst.setText(datosImpoExpoCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosImpoExpoCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);

        if(datosImpoExpoCorrida.getCostoFijo() != null) {
        inputCostoFijo.setText(datosImpoExpoCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosImpoExpoCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosImpoExpoCorrida.getMinTec() != null) {
        inputMinimoTecnico.setText(datosImpoExpoCorrida.getMinTec().getValor(instanteActual).toString()); }
        evForField(datosImpoExpoCorrida.getMinTec(), "minimoTecnico", inputMinimoTecnicoEV, Text.EV_NUM_INT, null, inputMinimoTecnico);

        inputHayMinimoTecnico.setSelected(datosImpoExpoCorrida.isHayMinTec());
        inputSalidaDetallada.setSelected(datosImpoExpoCorrida.isSalDetallada());
        if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)){
            inputCmgProcOpt.getSelectionModel().select(datosImpoExpoCorrida.getDatCMg().getProcOptimizacion());
            inputCmgProcSim.getSelectionModel().select(datosImpoExpoCorrida.getDatCMg().getProcSimulacion());
            if(datosImpoExpoCorrida.getDatCMg().getProcOptimizacion() != null && datosImpoExpoCorrida.getDatCMg().getProcSimulacion() != null ){
                inputCmgNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datosImpoExpoCorrida.getDatCMg().getProcOptimizacion()),
                        datosCorrida.getProcesosEstocasticos().get(datosImpoExpoCorrida.getDatCMg().getProcSimulacion())));
                inputCmgNombre.getSelectionModel().select(datosImpoExpoCorrida.getDatCMg().getNombre());
            }

        }

        if(datosImpoExpoCorrida.getFactorEscalamiento() != null) {
        inputCmgFactorEscalamiento.setText(datosImpoExpoCorrida.getFactorEscalamiento().getValor(instanteActual).toString()); }
        evForField(datosImpoExpoCorrida.getFactorEscalamiento(), "factorEscalamiento", inputCmgFactorEscalamientoEV, Text.EV_NUM_DOUBLE, null, inputCmgFactorEscalamiento);
        disableCmg(datosImpoExpoCorrida.getTipoImpoExpo());
        try{
            if(tipoImpoExpo.equals(Constantes.IEEVOL)) {
                for(int i=0;i<cantBloques;i++){
                    BloqueImpoExpoController bloqueController = new BloqueImpoExpoController(datosCorrida,
                                                                                             datosImpoExpoCorrida.getPotEvol().get(i),
                                                                                             datosImpoExpoCorrida.getPreEvol().get(i),
                                                                                             datosImpoExpoCorrida.getDispEvol().get(i));
                    bloqueImpoExpoControllers.add(bloqueController);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueImpoExpo.fxml"));
                    loader.setController(bloqueController);
                    AnchorPane newLoadedPane = loader.load();
                    vBoxBloques.getChildren().add(newLoadedPane);
                }
            }else if(tipoImpoExpo.equals(Constantes.IEALEATPRPOT)) {
                for(int i=0;i<cantBloques;i++){
                    BloqueImpoExpoController bloqueController = new BloqueImpoExpoController(datosCorrida,
                                                                                             datosImpoExpoCorrida.getDatPrecio().get(i),
                                                                                             datosImpoExpoCorrida.getDatPotencia().get(i));
                    bloqueImpoExpoControllers.add(bloqueController);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueImpoExpo.fxml"));
                    loader.setController(bloqueController);
                    AnchorPane newLoadedPane = loader.load();
                    vBoxBloques.getChildren().add(newLoadedPane);
                }
            }else{
                for(int i=0;i<cantBloques;i++){
                    BloqueImpoExpoController bloqueController = new BloqueImpoExpoController(datosCorrida,
                                                                                             datosImpoExpoCorrida.getPoliPot().get(i),
                                                                                             datosImpoExpoCorrida.getPoliPre().get(i),
                                                                                             datosImpoExpoCorrida.getPoliDisp().get(i),
                                                                                             true);
                    bloqueImpoExpoControllers.add(bloqueController);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueImpoExpo.fxml"));
                    loader.setController(bloqueController);
                    AnchorPane newLoadedPane = loader.load();
                    vBoxBloques.getChildren().add(newLoadedPane);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> ret = new ArrayList<>();

        if (inputNombre.getText().trim().equalsIgnoreCase("")){ ret.add("ImpoExpo: Nombre vacío"); }
        if (inputBarra.getValue() == null){ ret.add("ImpoExpo: Barra vacío"); }

        if (inputPais.getValue() == null){ ret.add("ImpoExpo: País vacío"); }
        if (inputTipoImpoExpo.getValue() == null){ ret.add("ImpoExpo: Tipo Impo Expo vacío"); }

        if( !UtilStrings.esNumeroDouble(inputCostoFijo.getText().trim())) {ret.add("ImpoExpo: Costo Fijo no es decimal"); }
        if(inputHayMinimoTecnico.isSelected()){
            if( !UtilStrings.esNumeroDouble(inputMinimoTecnico.getText().trim())) { ret.add("ImpoExpo: Mínimo técnico no es decimal"); }
        }

        if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {

            if (inputCmgProcSim.getValue() == null){ ret.add("ImpoExpo: Cmg Proceso Simulación"); }
            if (inputCmgProcOpt.getValue() == null){ ret.add("ImpoExpo: Cmg Proceso Optimización"); }
            if (inputCmgNombre.getValue() == null){ ret.add("ImpoExpo: Cmg Nombre proceso "); }
            if (inputCmgFactorEscalamiento.getText().trim().equalsIgnoreCase("")){ ret.add("ImpoExpo: Cmg Factor Escalamiento"); }

        }

        for(BloqueImpoExpoController  b: bloqueImpoExpoControllers){
            ArrayList<String> err = b.controlDatosCompletos();
            if(err.size() > 0 ){ ret.add( "Bloque incompleto"); }
        }

        return ret;
    }
}
