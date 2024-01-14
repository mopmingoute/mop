/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorTermicoController is part of MOP.
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import logica.CorridaHandler;

import org.controlsfx.control.PopOver;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.*;

import static utilitarios.Constantes.COMPMINTEC;

public class EditorTermicoController extends GeneralidadesController {
    @FXML private JFXButton popOverTest;
    @FXML private GridPane gridPane;
    @FXML private AnchorPane anchorPane;
    @FXML private JFXButton testDialog;
    @FXML private StackPane stackPane;
    @FXML private VBox vBoxVE;
    @FXML private JFXButton btnAddCombustible;
    @FXML private JFXButton btnRemoveCombustible;

    private Integer cantCombustibles = 1;
    private HashMap<Integer, Node> combosCombustibles = new HashMap<>();

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

    @FXML private JFXTextField inputRendPotMin;
    @FXML private JFXTextField inputRendPotMin2;
    @FXML private JFXTextField inputRendPotMin3;

    @FXML private JFXButton inputRendPotMinEV;
    @FXML private JFXButton inputRendPotMinEV2;
    @FXML private JFXButton inputRendPotMinEV3;

    @FXML private JFXTextField inputRendPotMax;
    @FXML private JFXTextField inputRendPotMax2;
    @FXML private JFXTextField inputRendPotMax3;

    @FXML private JFXButton inputRendPotMaxEV;
    @FXML private JFXButton inputRendPotMaxEV2;
    @FXML private JFXButton inputRendPotMaxEV3;

    @FXML private JFXComboBox<String> inputFlexMin;
    @FXML private JFXComboBox<String> inputCompMinTecnicos;
    @FXML private JFXButton inputCompMinTecnicosEV;
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
    @FXML private JFXComboBox<String> inputListaComb1;
    @FXML private JFXComboBox<String> inputListaBarraComb1;

    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;

    @FXML private JFXButton btnGuardarEnBiblioteca;


    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private DatosCorrida datosCorrida;
    private DatosTermicoCorrida datosTermicoCorrida;
    private boolean edicion;
    private ListaTermicosController listaTermicosController;

    private HashMap<String, ArrayList<String>> combustiblesYBarras = new HashMap<>();
    private ArrayList<String> combustiblesSeleccionados = new ArrayList<>();
    private boolean copiadoPortapapeles;
    private boolean editoVariables;


    public EditorTermicoController(DatosCorrida datosCorrida, ListaTermicosController listaTermicosController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaTermicosController = listaTermicosController;
        for(DatosCombustibleCorrida datosCombustibleCorrida : datosCorrida.getCombustibles().getCombustibles().values()){
            ArrayList<String> barrasComb = new ArrayList<>();
            for(DatosBarraCombCorrida datosBarraCombCorrida : datosCombustibleCorrida.getRed().getBarras()){
                barrasComb.add(datosBarraCombCorrida.getNombre());
            }
            combustiblesYBarras.put(datosCombustibleCorrida.getNombre(), barrasComb);
        }
    }

    public EditorTermicoController(DatosCorrida datosCorrida, DatosTermicoCorrida datosTermicoCorrida, ListaTermicosController listaTermicosController, boolean copiadoPortapapeles){
        this.datosCorrida = datosCorrida;
        this.datosTermicoCorrida = datosTermicoCorrida;
        this.listaTermicosController = listaTermicosController;
        edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        for(DatosCombustibleCorrida datosCombustibleCorrida : datosCorrida.getCombustibles().getCombustibles().values()){
            ArrayList<String> barrasComb = new ArrayList<>();
            for(DatosBarraCombCorrida datosBarraCombCorrida : datosCombustibleCorrida.getRed().getBarras()){
                barrasComb.add(datosBarraCombCorrida.getNombre());
            }
            combustiblesYBarras.put(datosCombustibleCorrida.getNombre(), barrasComb);
        }
    }

    @FXML
    public void initialize(){

        inputCompMinTecnicos.getItems().addAll(Text.COMP_MIN_TECNICO_VALS);
//        inputCompMinTecnicos.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.TERSINMINTEC, Constantes.TERMINTECFORZADO, Constantes.TERVARENTERAS, Constantes.TERVARENTERASYVARESTADO, Constantes.TERDOSPASOS)));

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getItems().add(Text.BARRA_2);
        inputBarra.getSelectionModel().selectFirst();

        inputFlexMin.getItems().addAll(new ArrayList<>(Arrays.asList(Text.TERFLEXHORARIO, Text.TERFLEXSEMANAL)));

        inputListaComb1.getItems().addAll(combustiblesYBarras.keySet());
        combosCombustibles.put(1, inputListaComb1);
//        combosCombustibles.put(((cantCombustibles+2)*10)+5, inputListaComb1);
        combosCombustibles.put(2, inputListaBarraComb1);
//        combosCombustibles.put(((cantCombustibles+2)*10)+6, inputListaBarraComb1);
//        cantCombustibles += 2;
        inputListaComb1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));
        inputListaComb1.setOnAction(actionEvent -> {
            inputListaBarraComb1.getItems().clear();
            inputListaBarraComb1.getItems().addAll(combustiblesYBarras.get(inputListaComb1.getValue()));
        });

        inputPotMinUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMinUnidad.getSelectionModel().selectFirst();
        inputPotMaxUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMaxUnidad.getSelectionModel().selectFirst();


        inputTMedioArregloUnidad.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidad.getSelectionModel().selectFirst();

        try {
            for(Node node : gridPane.getChildren()) {
                if(node instanceof JFXButton && ((JFXButton) node).getText().equals("EV")) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("EV.fxml"));
                AnchorPane newLoadedPane = loader.load();
                PopOver popOver = new PopOver(newLoadedPane);
                popOver.setTitle("EVOOOO");
                    ((JFXButton)node).setOnAction(actionEvent -> {
                        popOver.show(node);
                    });
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }


            try{//TODO: no existe VE para los termicos
                VariableDeEstadoController controller = new VariableDeEstadoController("Estado Mínimo Técnico","Estado Mínimo Técnico", 0, "???");//TODO: unidad
                FXMLLoader loader = new FXMLLoader(getClass().getResource("VariableDeEstado.fxml"));
                loader.setController(controller);
                AnchorPane newLoadedPane = loader.load();
                vBoxVE.getChildren().add(newLoadedPane);
            }catch(Exception e){
                e.printStackTrace();
            }
//        }


        // Combustibles/Barras
        btnAddCombustible.setOnAction(actionEvent -> addCombustible());

        btnRemoveCombustible.setOnAction(actionEvent -> removeCombustible());



        if(edicion){
            unloadData();
        }else{
            evForField(null, "compMinTecnico", inputCompMinTecnicosEV, Text.EV_VAR, Text.COMP_MIN_TECNICO_VALS,inputCompMinTecnicos);
            evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null,inputCantModInst);
            evForField(null, "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null,inputPotMin);
            evForField(null, "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null,inputPotMax);
            evForField(null, "rendPotMin", inputRendPotMinEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin);
            evForField(null, "rendPotMax", inputRendPotMaxEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax);
            evForField(null, "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null,inputDispMedia);
            evForField(null, "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null,inputTMedioArreglo);
            evForField(null, "mantProg", inputMantProgEV, Text.EV_NUM_INT, null,inputMantProg);
            evForField(null, "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null,inputCostoFijo);
            evForField(null, "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null,inputCostoVariable);

            evForField(null, "rendPotMax2", inputRendPotMaxEV2, Text.EV_NUM_DOUBLE, null,inputRendPotMax2);
            evForField(null, "rendPotMax3", inputRendPotMaxEV3, Text.EV_NUM_DOUBLE, null,inputRendPotMax3);
            evForField(null, "rendPotMin2", inputRendPotMinEV2, Text.EV_NUM_DOUBLE, null,inputRendPotMin2);
            evForField(null, "rendPotMin3", inputRendPotMinEV3, Text.EV_NUM_DOUBLE, null,inputRendPotMin3);

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
            if (editoVariables) {
                ArrayList<String> errores = controlDatosCompletos();
                if (errores.size() == 0 || errores == null) {
                    String nombreNuevo = inputNombre.getText().trim();
                    String nombreViejo = (edicion) ? datosTermicoCorrida.getNombre().trim() : "";
                    if (evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getTermicos().getOrdenCargaXML(), edicion)) {
                        setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                    } else if(nombreNuevo.contains(" ")){
                        setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                    } else {
                        loadData();
                        listaTermicosController.refresh();
                        listaTermicosController.actualizarLineaDeEntrada();
                        ((Stage) inputAceptar.getScene().getWindow()).close();
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


            }else {
                if(inputNombre.getText().trim().equals("Termico clonado")){
                    setLabelMessageTemporal("Cambie el nombre del termico clonado", TipoInfo.FEEDBACK);
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
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_TERMICO_TEXT, datosTermicoCorrida, datosCorrida, this,inputNombre.getText());
            }
            else {
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }


        });

        //Control de cambios:
        inputCompMinTecnicos.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputListaComb1.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;    });
        inputPotMin.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMax.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMin.textProperty().addListener((observable, oldValue, newValue) -> {     editoVariables = true;    });
        inputRendPotMin2.textProperty().addListener((observable, oldValue, newValue) -> {     editoVariables = true;     });
        inputRendPotMin3.textProperty().addListener((observable, oldValue, newValue) -> {     editoVariables = true;     });
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
            listaTermicosController.borrarTermico(datosTermicoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }

    private void addCombustible(){
        editoVariables = true;
        JFXComboBox<String> nuevoComb = new JFXComboBox<>();
        JFXComboBox<String> nuevaBarra = new JFXComboBox<>();
        nuevoComb.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;  });
        nuevaBarra.valueProperty().addListener((observable, oldValue, newValue) -> {    editoVariables = true;   });
        nuevoComb.setPromptText("Elija...");
        nuevaBarra.setPromptText("Elija...");
        nuevoComb.setMinWidth(180);
        nuevoComb.setMaxWidth(180);
        nuevaBarra.setMinWidth(180);
        nuevaBarra.setMaxWidth(180);
        gridPane.add(nuevoComb, cantCombustibles+2,5,2,1);
        gridPane.add(nuevaBarra, cantCombustibles+2,6,2,1);
        combosCombustibles.put((((cantCombustibles+2)*10)+5), nuevoComb);
        combosCombustibles.put((((cantCombustibles+2)*10)+6), nuevaBarra);
        cantCombustibles += 2;
        GridPane.setColumnIndex(btnAddCombustible, cantCombustibles + 2);
        GridPane.setColumnIndex(btnRemoveCombustible, cantCombustibles + 2);
        if(cantCombustibles == 3){
            inputRendPotMin2.setVisible(true);
            inputRendPotMax2.setVisible(true);
            inputRendPotMinEV2.setVisible(true);
            inputRendPotMaxEV2.setVisible(true);
            evForField(null, "rendPotMin2", inputRendPotMinEV2, Text.EV_NUM_DOUBLE, null,inputRendPotMin2);
            evForField(null, "rendPotMax2", inputRendPotMaxEV2, Text.EV_NUM_DOUBLE, null,inputRendPotMax2);
        }
        if(cantCombustibles == 5) {
            btnAddCombustible.setVisible(false);
            inputRendPotMin3.setVisible(true);
            inputRendPotMax3.setVisible(true);
            inputRendPotMinEV3.setVisible(true);
            inputRendPotMaxEV3.setVisible(true);
            evForField(null, "rendPotMin3", inputRendPotMinEV3, Text.EV_NUM_DOUBLE, null,inputRendPotMin3);
            evForField(null, "rendPotMax3", inputRendPotMaxEV3, Text.EV_NUM_DOUBLE, null,inputRendPotMax3);
        }
        btnRemoveCombustible.setVisible(true);


        for(String nombreComb : combustiblesYBarras.keySet()){
            if(!combustiblesSeleccionados.contains(nombreComb)){
                nuevoComb.getItems().add(nombreComb);
            }
        }
        nuevoComb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));
        nuevoComb.setOnAction(actionEvent -> {
            nuevaBarra.getItems().clear();
            nuevaBarra.getItems().addAll(combustiblesYBarras.get(nuevoComb.getValue()));
        });

    }

    private void removeCombustible() {
        editoVariables = true;
        cantCombustibles -= 2;
        gridPane.getChildren().remove(combosCombustibles.get((((cantCombustibles+2)*10)+5)));
        gridPane.getChildren().remove(combosCombustibles.get((((cantCombustibles+2)*10)+6)));
        if(cantCombustibles == 1){
            btnRemoveCombustible.setVisible(false);
            inputRendPotMin2.setVisible(false);
            inputRendPotMax2.setVisible(false);
            inputRendPotMinEV2.setVisible(false);
            inputRendPotMaxEV2.setVisible(false);
            inputRendPotMin3.setVisible(false);
            inputRendPotMax3.setVisible(false);
            inputRendPotMinEV3.setVisible(false);
            inputRendPotMaxEV3.setVisible(false);

        }
        if(cantCombustibles == 3){
            inputRendPotMin3.setVisible(false);
            inputRendPotMax3.setVisible(false);
            inputRendPotMinEV3.setVisible(false);
            inputRendPotMaxEV3.setVisible(false);

        }
        GridPane.setColumnIndex(btnAddCombustible, cantCombustibles + 2);
        GridPane.setColumnIndex(btnRemoveCombustible, cantCombustibles + 2);
        btnAddCombustible.setVisible(true);

        if(((ComboBox<String>)combosCombustibles.get(((cantCombustibles+2)*10)+5)).getValue() != null) {
            combustiblesSeleccionados.remove(((ComboBox<String>)combosCombustibles.get(((cantCombustibles+2)*10)+5)).getValue());
            updateGenList(((ComboBox<String>)combosCombustibles.get(((cantCombustibles+2)*10)+5)).getValue(), null);
        }
    }

    private void genComboOnChangeBehaviour(String oldValue, String newValue){
        if(oldValue != null){
            combustiblesSeleccionados.remove(oldValue);
        }
        combustiblesSeleccionados.add(newValue);
        updateGenList(oldValue, newValue);
    }

    private void updateGenList(String oldValue, String newValue){
        for(HashMap.Entry<Integer, Node> entry : combosCombustibles.entrySet()) {
            if (entry.getKey() % 2 != 0) {
                if (oldValue != null && !oldValue.isEmpty()) {
                    if (!((ComboBox<String>)entry.getValue()).getItems().contains(oldValue)) {
                        ((ComboBox<String>)entry.getValue()).getItems().add(oldValue);
                    }
                }
                if (newValue != null) {
                    if ((((ComboBox<String>)entry.getValue()).getValue() == null) || !((ComboBox<String>)entry.getValue()).getValue().equalsIgnoreCase(newValue)) {
                        ((ComboBox<String>)entry.getValue()).getItems().remove(newValue);
                    }
                }
            }
        }
    }

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(codigo == 1 && confirmacion){ //No corrige errores o datos incompletos
            loadData();
            listaTermicosController.unloadData();//Cambia color participantes incompletos
            listaTermicosController.refresh();
            ((Stage)inputAceptar.getScene().getWindow()).close();
            editoVariables = false;
        } else if(codigo == 2 && confirmacion){ //Guarda mensajes antes de cerrar
            editoVariables = false;
            cerrarVentana();
        }

    };

    private String loadDatosFlexibilidadMinima() {
        String ret = "";
        if(inputFlexMin.getValue().trim().equals(Text.TERFLEXHORARIO))
        { ret = Constantes.TERFLEXHORARIO; }

        if(inputFlexMin.getValue().trim().equals(Text.TERFLEXSEMANAL))
        { ret = Constantes.TERFLEXSEMANAL; }
        return ret;
    }

    private String unloadDatosFlexibilidadMinima() {
        String ret = "";
        if(datosTermicoCorrida.getFlexibilidadMin().trim().equals(Constantes.TERFLEXHORARIO))
        { ret = Text.TERFLEXHORARIO; }
        else if(datosTermicoCorrida.getFlexibilidadMin().trim().equals(Constantes.TERFLEXSEMANAL))
        { ret = Text.TERFLEXSEMANAL; }
        return ret;
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
     *  Método para cargar los datos en el DatosTermicoCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-TÉRMICO");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();

        ArrayList<String> listaCombustibles = new ArrayList<>();
        Hashtable<String,String> combustiblesBarras = new Hashtable<>();
        Hashtable<String,Evolucion<Double>> rendimientosPotMin = new Hashtable<>();
        Hashtable<String,Evolucion<Double>> rendimientosPotMax = new Hashtable<>();
        System.out.println("cantComb: "+cantCombustibles);
        System.out.println("combosComb: "+combosCombustibles);
//        for(int i=0;i<cantCombustibles; i++){
        if(((JFXComboBox<String>) combosCombustibles.get(1)).getValue() != null) {
            String combustible = (String) ((JFXComboBox) combosCombustibles.get(1)).getValue();
            String barraComb = (String) ((JFXComboBox) combosCombustibles.get(2)).getValue();

            Evolucion<Double> rendPotMin = EVController.loadEVsegunTipo(inputRendPotMin, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMin" );
            Evolucion<Double> rendPotMax = EVController.loadEVsegunTipo(inputRendPotMax, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMax" );
            if (rendPotMin != null) { rendimientosPotMin.put(combustible,rendPotMin); }
            if (rendPotMax != null) { rendimientosPotMax.put(combustible,rendPotMax); }

            listaCombustibles.add(combustible);
            combustiblesBarras.put(combustible, barraComb);
            for (int i = 1; i < cantCombustibles; i += 2) {
                if(i == 1) {
                    String combustible2 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 5)).getValue();
                    String barraComb2 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 6)).getValue();
                    listaCombustibles.add(combustible2);
                    combustiblesBarras.put(combustible2, barraComb2);

                    Evolucion<Double> rendPotMin2 = EVController.loadEVsegunTipo(inputRendPotMin2, Text.EV_NUM_DOUBLE, evsPorNombre, "rendPotMin2");
                    Evolucion<Double> rendPotMax2 = EVController.loadEVsegunTipo(inputRendPotMax2, Text.EV_NUM_DOUBLE, evsPorNombre, "rendPotMax2");
                    rendimientosPotMin.put(combustible2, rendPotMin2);
                    rendimientosPotMax.put(combustible2, rendPotMax2);
                }

                if(i == 3) {

                    String combustible3 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 5)).getValue();
                    String barraComb3 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 6)).getValue();
                    listaCombustibles.add(combustible3);
                    combustiblesBarras.put(combustible3, barraComb3);

                    Evolucion<Double> rendPotMin3 = EVController.loadEVsegunTipo(inputRendPotMin3, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMin3" );
                    Evolucion<Double> rendPotMax3 = EVController.loadEVsegunTipo(inputRendPotMax3, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMax3" );
                    rendimientosPotMin.put(combustible3, rendPotMin3);
                    rendimientosPotMax.put(combustible3, rendPotMax3);
                }
            }
        }

//        Evolucion<Integer> cantModInst = new EvolucionConstante<>(Integer.parseInt(inputCantModInst.getText()),new SentidoTiempo(1));
        Evolucion<Integer> cantModInst = EVController.loadEVsegunTipo(inputCantModInst, Text.EV_NUM_INT,  evsPorNombre,"cantModInst" );
//        Evolucion<Double> potMin = new EvolucionConstante<>(Double.parseDouble(inputPotMin.getText()), new SentidoTiempo(1));
        Evolucion<Double> potMin = EVController.loadEVsegunTipo(inputPotMin, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMin" );
//        inputPotMinUnidad
//        Evolucion<Double> potMax = new EvolucionConstante<>(Double.parseDouble(inputPotMax.getText()), new SentidoTiempo(1));
        Evolucion<Double> potMax = EVController.loadEVsegunTipo(inputPotMax, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMax" );
//        inputPotMaxUnidad;
//        Evolucion<Double> rendPotMin = new EvolucionConstante<>(Double.parseDouble(inputRendPotMin.getText()), new SentidoTiempo(1));
        Evolucion<Double> rendPotMin = EVController.loadEVsegunTipo(inputRendPotMin, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMin" );
//        Evolucion<Double> rendPotMax = new EvolucionConstante<>(Double.parseDouble(inputRendPotMax.getText()), new SentidoTiempo(1));
        Evolucion<Double> rendPotMax = EVController.loadEVsegunTipo(inputRendPotMax, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMax" );
        String flexibilidadMin = loadDatosFlexibilidadMinima();
        Hashtable<String,Evolucion<String>> valoresComportamientos = new Hashtable<>();
        Evolucion<String> compMinimosTecnicos = EVController.loadEVsegunTipo(inputCompMinTecnicos, Text.EV_VAR,  evsPorNombre,"compMinTecnico" );
        valoresComportamientos.put(COMPMINTEC, compMinimosTecnicos);
        Integer cantModIni = 0;
        if(UtilStrings.esNumeroEntero(inputCantModIni.getText())) { cantModIni = Integer.parseInt(inputCantModIni.getText()); }

//        Evolucion<Double> dispMedia = new EvolucionConstante<>(Double.parseDouble(inputDispMedia.getText()), new SentidoTiempo(1));
        Evolucion<Double> dispMedia = EVController.loadEVsegunTipo(inputDispMedia, Text.EV_NUM_DOUBLE,  evsPorNombre,"dispMedia" );
//        Evolucion<Double> tMedioArreglo = new EvolucionConstante<>(Double.parseDouble(inputTMedioArreglo.getText()), new SentidoTiempo(1));
        Evolucion<Double> tMedioArreglo = EVController.loadEVsegunTipo(inputTMedioArreglo, Text.EV_NUM_DOUBLE,  evsPorNombre,"tMedioArreglo" );
//        inputTMedioArregloUnidad;
//        Evolucion<Integer> mantProgramado = new EvolucionConstante<>(Integer.parseInt(inputMantProg.getText()), new SentidoTiempo(1));
        Evolucion<Integer> mantProgramado = EVController.loadEVsegunTipo(inputMantProg, Text.EV_NUM_INT,  evsPorNombre,"mantProg" );
//        Evolucion<Double> costoFijo = new EvolucionConstante<>(Double.parseDouble(inputCostoFijo.getText()), new SentidoTiempo(1));
        Evolucion<Double> costoFijo = EVController.loadEVsegunTipo(inputCostoFijo, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoFijo" );
//        Evolucion<Double> costoVariable = new EvolucionConstante<>(Double.parseDouble(inputCostoVariable.getText()), new SentidoTiempo(1));
        Evolucion<Double> costoVariable = EVController.loadEVsegunTipo(inputCostoVariable, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoVariable" );
        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        if(edicion){
            String nombreViejo = datosTermicoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getTermicos().getTermicos().remove(nombreViejo);
                int i = datosCorrida.getTermicos().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getTermicos().getOrdenCargaXML().set(i, nombre);
                int j = datosCorrida.getTermicos().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getTermicos().getListaUtilizados().set(j, nombre);
            }
            datosTermicoCorrida.setNombre(nombre);
            datosTermicoCorrida.setPropietario("UTE");
            datosTermicoCorrida.setBarra(barra);
            datosTermicoCorrida.setListaCombustibles(listaCombustibles);
            datosTermicoCorrida.setCombustiblesBarras(combustiblesBarras);
            datosTermicoCorrida.setCantModInst(cantModInst);
            datosTermicoCorrida.setPotMin(potMin);
            datosTermicoCorrida.setPotMax(potMax);
         /*   datosTermicoCorrida.setRendPotMin(rendPotMin);
            datosTermicoCorrida.setRendPotMax(rendPotMax);*/
            datosTermicoCorrida.setFlexibilidadMin(flexibilidadMin);
            datosTermicoCorrida.setValoresComportamientos(valoresComportamientos);
            datosTermicoCorrida.setCantModIni(cantModIni);
            datosTermicoCorrida.setDispMedia(dispMedia);
            datosTermicoCorrida.settMedioArreglo(tMedioArreglo);
            datosTermicoCorrida.setMantProgramado(mantProgramado);
            datosTermicoCorrida.setCostoFijo(costoFijo);
            datosTermicoCorrida.setCostoVariable(costoVariable);
            datosTermicoCorrida.setSalDetallada(salidaDetallada);
            datosTermicoCorrida.setRendimientosPotMin(rendimientosPotMin);
            datosTermicoCorrida.setRendimientosPotMax(rendimientosPotMax);
        }else {
            datosTermicoCorrida = new DatosTermicoCorrida(nombre,"UTE", barra, cantModInst, listaCombustibles, combustiblesBarras, potMin, potMax,
                                                          rendimientosPotMax, rendimientosPotMin, flexibilidadMin, cantModIni, dispMedia, tMedioArreglo,
                                                          salidaDetallada, mantProgramado, costoFijo, costoVariable);
            datosTermicoCorrida.setValoresComportamientos(valoresComportamientos);
            datosCorrida.getTermicos().getListaUtilizados().add(nombre);
            datosCorrida.getTermicos().getOrdenCargaXML().add(nombre);
        }
        datosCorrida.getTermicos().getTermicos().put(nombre, datosTermicoCorrida);

    }



    /**
     * Método para obtener los datos del DatosTermicoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-TÉRMICO");
        inputNombre.setText(datosTermicoCorrida.getNombre());
        inputBarra.getSelectionModel().select(datosTermicoCorrida.getBarra());
//        inputBarra.getSelectionModel().select(Text.BARRA_1);//uninodal hardcodeado
        //combustibles/barras
        int i = 0;
        int j = 1;
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        for(HashMap.Entry<String, String> entry : datosTermicoCorrida.getCombustiblesBarras().entrySet()) {
            if(i==0){
                inputListaComb1.getSelectionModel().select(entry.getKey());
                inputListaBarraComb1.getItems().addAll(combustiblesYBarras.get(entry.getKey()));
                inputListaBarraComb1.getSelectionModel().select(entry.getValue());
            }else{
                addCombustible();
                ((JFXComboBox)combosCombustibles.get(((i+2)*10)+5)).getSelectionModel().select(entry.getKey());
                ((JFXComboBox)combosCombustibles.get(((i+2)*10)+6)).getItems().addAll(combustiblesYBarras.get(entry.getKey()));
                ((JFXComboBox)combosCombustibles.get(((i+2)*10)+6)).getSelectionModel().select(entry.getValue());
                i++;
            }

            if(j==1){
                if(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()) != null){
                    inputRendPotMin.setText(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString()); }
                if(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()) != null){
                    inputRendPotMax.setText(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());  }
                evForField(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()), "rendPotMin", inputRendPotMinEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin);
                evForField(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()), "rendPotMax", inputRendPotMaxEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax);

            }else if(j==2){
                if(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()) != null){
                    inputRendPotMin2.setText(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString()); }
                if(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()) != null){
                    inputRendPotMax2.setText(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString()); }
                evForField(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()), "rendPotMin2", inputRendPotMinEV2, Text.EV_NUM_DOUBLE, null,inputRendPotMin2);
                evForField(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()), "rendPotMax2", inputRendPotMaxEV2, Text.EV_NUM_DOUBLE, null,inputRendPotMax2);
                inputRendPotMin2.setVisible(true);
                inputRendPotMax2.setVisible(true);
            }else if(j==3){
                if(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()) != null){
                    inputRendPotMin3.setText(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString()); }
                if(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()) != null){
                    inputRendPotMax3.setText(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString()); }
                evForField(datosTermicoCorrida.getRendimientosPotMin().get(entry.getKey()), "rendPotMin3", inputRendPotMinEV3, Text.EV_NUM_DOUBLE, null,inputRendPotMin3);
                evForField(datosTermicoCorrida.getRendimientosPotMax().get(entry.getKey()), "rendPotMax3", inputRendPotMaxEV3, Text.EV_NUM_DOUBLE, null,inputRendPotMax3);
                inputRendPotMin3.setVisible(true);
                inputRendPotMax3.setVisible(true);
            }
            i++;
            j++;
        }

        if(datosTermicoCorrida.getCantModInst() != null) {
        inputCantModInst.setText(datosTermicoCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null,inputCantModInst);

        if(datosTermicoCorrida.getPotMin() != null){
        inputPotMin.setText(datosTermicoCorrida.getPotMin().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getPotMin(), "potMin", inputPotMinEV, Text.EV_NUM_DOUBLE, null,inputPotMin);

        if(datosTermicoCorrida.getPotMax() != null){
        inputPotMax.setText(datosTermicoCorrida.getPotMax().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getPotMax(), "potMax", inputPotMaxEV, Text.EV_NUM_DOUBLE, null,inputPotMax);

        inputFlexMin.getSelectionModel().select(unloadDatosFlexibilidadMinima());

        if(datosTermicoCorrida.getValoresComportamientos().get(COMPMINTEC) != null) {
        inputCompMinTecnicos.getSelectionModel().select(datosTermicoCorrida.getValoresComportamientos().get(COMPMINTEC).getValor(instanteActual)); }
        evForField(datosTermicoCorrida.getValoresComportamientos().get(COMPMINTEC), "compMinTecnico", inputCompMinTecnicosEV, Text.EV_VAR, Text.COMP_MIN_TECNICO_VALS, inputCompMinTecnicos);

        inputCantModIni.setText(datosTermicoCorrida.getCantModIni().toString());

        if(datosTermicoCorrida.getDispMedia() != null){
        inputDispMedia.setText(datosTermicoCorrida.getDispMedia().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);

        if(datosTermicoCorrida.gettMedioArreglo() != null){
        inputTMedioArreglo.setText(datosTermicoCorrida.gettMedioArreglo().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);

        if(datosTermicoCorrida.getMantProgramado() != null){
        inputMantProg.setText(datosTermicoCorrida.getMantProgramado().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);

        if(datosTermicoCorrida.getCostoFijo() != null){
        inputCostoFijo.setText(datosTermicoCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosTermicoCorrida.getCostoVariable() != null){
        inputCostoVariable.setText(datosTermicoCorrida.getCostoVariable().getValor(instanteActual).toString()); }
        evForField(datosTermicoCorrida.getCostoVariable(), "costoVariable", inputCostoVariableEV, Text.EV_NUM_DOUBLE, null, inputCostoVariable);

        inputSalidaDetallada.setSelected(datosTermicoCorrida.isSalDetallada());


    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();

        if(inputCompMinTecnicos.getValue() == null){ errores.add( "Editor Térmico: " + inputNombre.getText() +" Comportamiento min tecnico vacío.");}
        if(inputNombre.getText().trim().equals("")) { errores.add( "Editor Térmico.  Nombre vacío"); }
        if( inputBarra.getValue() == null) { errores.add("Editor Térmico: " + inputNombre.getText() + " Barra vacío.") ; }

        if(!UtilStrings.esNumeroEntero(inputCantModInst.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Cantidad modulos instalados no es dato numérico.") ;

        if(inputListaComb1.getValue() == null) errores.add("Editor Térmico: " + inputNombre.getText() + "  Combustible vacío.") ;
        if(inputListaBarraComb1.getValue() == null) errores.add("Editor Térmico: " + inputNombre.getText() + "  Barra Combustible vacío.") ;

        if(inputPotMin.getText().trim().equals("")) errores.add("Editor Térmico: " + inputNombre.getText() + "  Potencia mínima vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputPotMin.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Potencia mínima no es dato numérico.") ;
        if(inputPotMax.getText().trim().equals("")) errores.add("Editor Térmico: " + inputNombre.getText() + "  Potencia máxima vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputPotMax.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Potencia máxima no es dato numérico.") ;


        if( inputFlexMin.getValue() == null) { errores.add("Editor Térmico: " + inputNombre.getText() + " Flexibilidad mínima vacío.") ; }



        if(!UtilStrings.esNumeroDouble(inputRendPotMin.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Rend potencia mínima no es dato numérico.") ;
        if(!UtilStrings.esNumeroDouble(inputRendPotMax.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Rend potencia máxima no es dato numérico.") ;

        if(cantCombustibles > 2){
            if(!UtilStrings.esNumeroDouble(inputRendPotMin2.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Rendimiento min combustible 2 no es dato numérico.") ;
            if(!UtilStrings.esNumeroDouble(inputRendPotMax2.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Rendimiento max combustible 2 no es dato numérico.") ;

        }
        if(cantCombustibles > 4) {
            if(!UtilStrings.esNumeroDouble(inputRendPotMin3.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Rendimiento min combustible 3 no es dato numérico.") ;
            if(!UtilStrings.esNumeroDouble(inputRendPotMax3.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Rendimiento max combustible 3 no es dato numérico.") ;
        }
        if(!UtilStrings.esNumeroDouble(inputCantModIni.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Cantidad modulos inicial no es dato numérico.") ;
        if(!UtilStrings.esNumeroDouble(inputTMedioArreglo.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Tiempo medio de arreglo no es dato numérico.") ;
        if(!UtilStrings.esNumeroDouble(inputMantProg.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Mantenimiento programado no es dato numérico.") ;
        if(!UtilStrings.esNumeroDouble(inputDispMedia.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Disponibilidad media no es dato numérico.") ;
        if( !controlDisponibilidadMedia(inputDispMedia)) errores.add("Editor Térmico: " + inputNombre.getText() + " Disponibilidad media no es double entre 0 y 1.");

        if(!UtilStrings.esNumeroDouble(inputCostoFijo.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Costo fijo no es dato numérico.") ;
        if(!UtilStrings.esNumeroDouble(inputCostoVariable.getText())) errores.add("Editor Térmico: " + inputNombre.getText() + "  Costo variable no es dato numérico.") ;

        return errores;
    }
}
