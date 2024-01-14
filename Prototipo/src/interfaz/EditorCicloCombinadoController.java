/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorCicloCombinadoController is part of MOP.
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
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logica.CorridaHandler;
import javafx.stage.StageStyle;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.*;

public class EditorCicloCombinadoController extends GeneralidadesController {

    @FXML private GridPane gridPane;
    @FXML private GridPane gridPaneTG;
    @FXML private GridPane gridPaneCV;
    @FXML private AnchorPane anchorPane;

    @FXML private JFXComboBox<String> inputCompCicloComb;
    @FXML private JFXButton inputCompCicloCombEV;

    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;

    @FXML private JFXTextField inputCostoArranqueCicloAbierto;
    @FXML private JFXButton inputCostoArranqueCicloAbiertoEV;
    @FXML private JFXTextField inputCostoArranqueCicloCombinado;
    @FXML private JFXButton inputCostoArranqueCicloCombinadoEV;

    @FXML private JFXTextField inputPotMax1CV;
    @FXML private JFXComboBox<String> inputPotMax1CVUnidad;
    @FXML private JFXButton inputPotMax1CVEV;

    @FXML private JFXComboBox<String> inputListaComb1;
    @FXML private JFXComboBox<String> inputListaBarraComb1;
    @FXML private JFXButton btnAddCombustible;
    @FXML private JFXButton btnRemoveCombustible;

    @FXML private JFXButton btnRemovePosiblesArranques;
    @FXML private JFXButton btnAddPosiblesArranques;
    @FXML private JFXButton btnRemovePotRampaArranque;
    @FXML private JFXButton btnAddPotRampaArranque;
    @FXML private JFXButton btnRemovePosiblesParadas;
    @FXML private JFXButton btnAddPosiblesParadas;

    @FXML private JFXTextField inputPosiblesArranques1;
    @FXML private JFXTextField inputPotenciaRampaArranque1;
    @FXML private JFXTextField inputPosiblesParadas1;

    private HashMap<Integer, JFXTextField> textFieldsArranques = new HashMap<>();
    private HashMap<Integer, JFXTextField> textFieldsPotArranques = new HashMap<>();
    private HashMap<Integer, JFXTextField> textFieldsParadas = new HashMap<>();

    private Integer cantPosiblesArranques = 1;
    private Integer cantPotenciaRampaArranque = 1;
    private Integer cantPosiblesParadas = 1;
    private Integer cantCombustibles = 1;

    @FXML private Label labelPosiblesArranques;

    private HashMap<Integer, Node> combosCombustibles = new HashMap<>();
    private HashMap<String, ArrayList<String>> combustiblesYBarras = new HashMap<>();
    private ArrayList<String> combustiblesSeleccionados = new ArrayList<>();

    @FXML private JFXCheckBox inputSalidaDetallada;


    //TURBINA A GAS - TG

    @FXML private JFXTextField inputCantModInstTG;
    @FXML private JFXButton inputCantModInstTGEV;


    @FXML private JFXTextField inputPotMinTG;
    @FXML private JFXButton inputPotMinTGEV;
    @FXML private JFXComboBox<String> inputPotMinUnidadTG;
    @FXML private JFXTextField inputPotMaxTG;
    @FXML private JFXButton inputPotMaxTGEV;
    @FXML private JFXComboBox<String> inputPotMaxUnidadTG;

    @FXML private JFXTextField inputRendPotMinTG;
    @FXML private JFXTextField inputRendPotMin2TG;
    @FXML private JFXTextField inputRendPotMin3TG;
    @FXML private JFXButton inputRendPotMinTGEV;
    @FXML private JFXButton inputRendPotMin2TGEV;
    @FXML private JFXButton inputRendPotMin3TGEV;
    @FXML private JFXTextField inputRendPotMaxTG;
    @FXML private JFXTextField inputRendPotMax2TG;
    @FXML private JFXTextField inputRendPotMax3TG;
    @FXML private JFXButton inputRendPotMaxTGEV;
    @FXML private JFXButton inputRendPotMax2TGEV;
    @FXML private JFXButton inputRendPotMax3TGEV;


    @FXML private JFXTextField inputCantModIniTG;
    @FXML private JFXTextField inputDispMediaTG;
    @FXML private JFXButton inputDispMediaTGEV;
    @FXML private JFXTextField inputTMedioArregloTG;
    @FXML private JFXButton inputTMedioArregloTGEV;
    @FXML private JFXComboBox<String> inputTMedioArregloUnidadTG;

    @FXML private JFXTextField inputMantProgTG;
    @FXML private JFXButton inputMantProgTGEV;
    @FXML private JFXTextField inputCostoFijoTG;
    @FXML private JFXButton inputCostoFijoTGEV;
    @FXML private JFXTextField inputCostoVariableTG;
    @FXML private JFXButton inputCostoVariableTGEV;



    //CICLOS DE VAPOR - CV

    @FXML private JFXTextField inputCantModInstCV;
    @FXML private JFXButton inputCantModInstCVEV;

    @FXML private JFXTextField inputPotMinCV;
    @FXML private JFXButton inputPotMinCVEV;
    @FXML private JFXComboBox<String> inputPotMinUnidadCV;
    @FXML private JFXTextField inputPotMaxCV;
    @FXML private JFXButton inputPotMaxCVEV;
    @FXML private JFXComboBox<String> inputPotMaxUnidadCV;

    @FXML private JFXTextField inputRendPotMinCV;
    @FXML private JFXTextField inputRendPotMin2CV;
    @FXML private JFXTextField inputRendPotMin3CV;
    @FXML private JFXButton inputRendPotMinCVEV;
    @FXML private JFXButton inputRendPotMin2CVEV;
    @FXML private JFXButton inputRendPotMin3CVEV;
    @FXML private JFXTextField inputRendPotMaxCV;
    @FXML private JFXTextField inputRendPotMax2CV;
    @FXML private JFXTextField inputRendPotMax3CV;
    @FXML private JFXButton inputRendPotMaxCVEV;
    @FXML private JFXButton inputRendPotMax2CVEV;
    @FXML private JFXButton inputRendPotMax3CVEV;

    @FXML private JFXTextField inputCantModIniCV;
    @FXML private JFXTextField inputDispMediaCV;
    @FXML private JFXButton inputDispMediaCVEV;
    @FXML private JFXTextField inputTMedioArregloCV;
    @FXML private JFXButton inputTMedioArregloCVEV;
    @FXML private JFXComboBox<String> inputTMedioArregloUnidadCV;

    @FXML private JFXTextField inputMantProgCV;
    @FXML private JFXButton inputMantProgCVEV;
    @FXML private JFXTextField inputCostoFijoCV;
    @FXML private JFXButton inputCostoFijoCVEV;
    @FXML private JFXTextField inputCostoVariableCV;
    @FXML private JFXButton inputCostoVariableCVEV;




    @FXML private JFXButton inputAceptar;
    @FXML private JFXButton inputCancelar;
    @FXML private JFXButton btnGuardarEnBiblioteca;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private DatosCorrida datosCorrida;
    private DatosCicloCombinadoCorrida datosCicloCombinadoCorrida;
    private boolean edicion;
    private ListaCiclosCombinadosController listaCiclosCombinadosController;
    private boolean copiadoPortapapeles;
    private boolean editoVariables;



    public EditorCicloCombinadoController(DatosCorrida datosCorrida, ListaCiclosCombinadosController listaCiclosCombinadosController) {
        this.datosCorrida = datosCorrida;
        edicion = false;
        copiadoPortapapeles = false;
        this.listaCiclosCombinadosController = listaCiclosCombinadosController;
        for(DatosCombustibleCorrida datosCombustibleCorrida : datosCorrida.getCombustibles().getCombustibles().values()){
            ArrayList<String> barrasComb = new ArrayList<>();
            for(DatosBarraCombCorrida datosBarraCombCorrida : datosCombustibleCorrida.getRed().getBarras()){
                barrasComb.add(datosBarraCombCorrida.getNombre());
            }
            combustiblesYBarras.put(datosCombustibleCorrida.getNombre(), barrasComb);
        }
    }

    public EditorCicloCombinadoController(DatosCorrida datosCorrida, DatosCicloCombinadoCorrida datosCicloCombinadoCorrida, ListaCiclosCombinadosController listaCiclosCombinadosController, boolean copiadoPortapapeles) {
        this.datosCorrida = datosCorrida;
        this.datosCicloCombinadoCorrida = datosCicloCombinadoCorrida;
        this.edicion = true;
        this.copiadoPortapapeles = copiadoPortapapeles;
        this.listaCiclosCombinadosController = listaCiclosCombinadosController;
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
        listaCiclosCombinadosController.getParentController().asignarTooltips(this);

        inputCompCicloComb.getItems().addAll(Text.COMP_CICLO_COMB_VALS);

        inputBarra.getItems().add(Text.BARRA_1);
        inputBarra.getSelectionModel().selectFirst();


        inputListaComb1.getItems().addAll(combustiblesYBarras.keySet());
        combosCombustibles.put(1, inputListaComb1);
        combosCombustibles.put(2, inputListaBarraComb1);
        inputListaComb1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> genComboOnChangeBehaviour(oldValue, newValue));
        inputListaComb1.setOnAction(actionEvent -> {
            inputListaBarraComb1.getItems().clear();
            inputListaBarraComb1.getItems().addAll(combustiblesYBarras.get(inputListaComb1.getValue()));
        });

        //Hadrcodeo de unidades
        inputPotMax1CVUnidad.getItems().add(Text.UNIDAD_MW);
        inputPotMax1CVUnidad.getSelectionModel().selectFirst();
        inputPotMinUnidadTG.getItems().add(Text.UNIDAD_MW);
        inputPotMinUnidadTG.getSelectionModel().selectFirst();
        inputPotMaxUnidadTG.getItems().add(Text.UNIDAD_MW);
        inputPotMaxUnidadTG.getSelectionModel().selectFirst();
        inputPotMinUnidadCV.getItems().add(Text.UNIDAD_MW);
        inputPotMinUnidadCV.getSelectionModel().selectFirst();
        inputPotMaxUnidadCV.getItems().add(Text.UNIDAD_MW);
        inputPotMaxUnidadCV.getSelectionModel().selectFirst();

        inputTMedioArregloUnidadTG.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidadTG.getSelectionModel().selectFirst();
        inputTMedioArregloUnidadCV.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidadCV.getSelectionModel().selectFirst();

        btnRemovePosiblesArranques.setOnAction(actionEvent -> removePosibleArranque());
        btnAddPosiblesArranques.setOnAction(actionEvent -> addPosibleArranque(null));
        btnRemovePotRampaArranque.setOnAction(actionEvent -> removePotRampaArranque());
        btnAddPotRampaArranque.setOnAction(actionEvent -> addPotRampaArranque(null));
        btnRemovePosiblesParadas.setOnAction(actionEvent -> removePosibleParada());
        btnAddPosiblesParadas.setOnAction(actionEvent -> addPosibleParada(null));


        // Combustibles/Barras
        btnAddCombustible.setOnAction(actionEvent -> addCombustible());
        btnRemoveCombustible.setOnAction(actionEvent -> removeCombustible());

        textFieldsArranques.put(1,inputPosiblesArranques1);
        textFieldsPotArranques.put(1,inputPotenciaRampaArranque1);
        textFieldsParadas.put(1,inputPosiblesParadas1);


        if(edicion){
            unloadData();
        }else{

            evForField(null, "potMax1CV", inputPotMax1CVEV, Text.EV_NUM_DOUBLE, null, inputPotMax1CV);
            evForField(null, "costoArranqueCicloAbierto", inputCostoArranqueCicloAbiertoEV, Text.EV_NUM_DOUBLE, null, inputCostoArranqueCicloAbierto);
            evForField(null, "costoArranqueCicloCombinado", inputCostoArranqueCicloCombinadoEV, Text.EV_NUM_DOUBLE, null, inputCostoArranqueCicloCombinado);

            evForField(null, "compCicloComb", inputCompCicloCombEV, Text.EV_VAR, Text.COMP_CICLO_COMB_VALS, inputCompCicloComb);

            evForField(null, "cantModInstTG", inputCantModInstTGEV, Text.EV_NUM_DOUBLE, null, inputCantModInstTG);
            evForField(null, "rendPotMinTG", inputRendPotMinTGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMinTG);
            evForField(null, "rendPotMaxTG", inputRendPotMaxTGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMaxTG);
            evForField(null, "rendPotMin2TG", inputRendPotMin2TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin2TG);
            evForField(null, "rendPotMax2TG", inputRendPotMax2TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax2TG);
            evForField(null, "rendPotMin3TG", inputRendPotMin3TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin3TG);
            evForField(null, "rendPotMax3TG", inputRendPotMax3TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax3TG);
            evForField(null, "potMinTG", inputPotMinTGEV, Text.EV_NUM_DOUBLE, null,inputPotMinTG);
            evForField(null, "potMaxTG", inputPotMaxTGEV, Text.EV_NUM_DOUBLE, null,inputPotMaxTG);
            evForField(null, "dispMediaTG", inputDispMediaTGEV, Text.EV_NUM_DOUBLE, null, inputDispMediaTG);
            evForField(null, "tMedioArregloTG", inputTMedioArregloTGEV, Text.EV_NUM_DOUBLE, null, inputTMedioArregloTG);
            evForField(null, "mantProgTG", inputMantProgTGEV, Text.EV_NUM_INT, null, inputMantProgTG);
            evForField(null, "costoFijoTG", inputCostoFijoTGEV, Text.EV_NUM_DOUBLE, null, inputCostoFijoTG);
            evForField(null, "costoVariableTG", inputCostoVariableTGEV, Text.EV_NUM_DOUBLE, null, inputCostoVariableTG);

            evForField(null, "cantModInstCV", inputCantModInstCVEV, Text.EV_NUM_DOUBLE, null, inputCantModInstCV);
            evForField(null, "potMaxCV", inputPotMaxCVEV, Text.EV_NUM_DOUBLE, null,inputPotMaxCV);
            evForField(null, "potMinCV", inputPotMinCVEV, Text.EV_NUM_DOUBLE, null,inputPotMinCV);
            evForField(null, "dispMediaCV", inputDispMediaCVEV, Text.EV_NUM_DOUBLE, null, inputDispMediaCV);
            evForField(null, "tMedioArregloCV", inputTMedioArregloCVEV, Text.EV_NUM_DOUBLE, null, inputTMedioArregloCV);
            evForField(null, "mantProgCV", inputMantProgCVEV, Text.EV_NUM_INT, null, inputMantProgCV);
            evForField(null, "costoFijoCV", inputCostoFijoCVEV, Text.EV_NUM_DOUBLE, null, inputCostoFijoCV);
            evForField(null, "costoVariableCV", inputCostoVariableCVEV, Text.EV_NUM_DOUBLE, null, inputCostoVariableCV);


        }
        inputDispMediaTG.textProperty().addListener((observable, oldValue, newValue) -> {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (!lastText.equals(newValue)) {
                            controlDisponibilidadMedia(inputDispMediaTG);
                            lastText = newValue;
                        }
                    });
                }
            }, DELAY_MILLISECONDS);
        });
        inputDispMediaCV.textProperty().addListener((observable, oldValue, newValue) -> {
           timer.cancel();
           timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (!lastText.equals(newValue)) {
                            controlDisponibilidadMedia(inputDispMediaCV);
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
                String nombreViejo = (edicion) ? datosCicloCombinadoCorrida.getNombre().trim():"";
                if(evaluarNombreRepetido(nombreViejo, nombreNuevo, datosCorrida.getCcombinados().getOrdenCargaXML(), edicion)){
                    setLabelMessageTemporal("Nombre repetido o inválido", TipoInfo.FEEDBACK);
                } else if(nombreNuevo.contains(" ")){
                    setLabelMessageTemporal("Ingrese nombre sin espacios", TipoInfo.FEEDBACK);
                } else {
                    loadData();
                    listaCiclosCombinadosController.refresh();
                    listaCiclosCombinadosController.actualizarLineaDeEntrada();
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
                if(inputNombre.getText().trim().equals("Ciclo Combinado clonado")){
                    setLabelMessageTemporal("Cambie el nombre del ciclo combinado clonado", TipoInfo.FEEDBACK);
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
                Manejador.guardarEnBiblioteca(btnGuardarEnBiblioteca, Text.TIPO_TERMICO_TEXT, datosCicloCombinadoCorrida, datosCorrida, this,inputNombre.getText());
            }
            else {
                setLabelMessageTemporal("Faltan datos", TipoInfo.FEEDBACK);
            }


        });


        //Control de cambios:

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMax1CV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoArranqueCicloAbierto.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoArranqueCicloCombinado.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputListaComb1.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputListaBarraComb1.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCompCicloComb.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });

        inputCantModInstTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMaxTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMinTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputRendPotMaxTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMax2TG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMax3TG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMinTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMin2TG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMin3TG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputDispMediaTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTMedioArregloTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModIniTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputMantProgTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFijoTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoVariableTG.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputCantModInstCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMaxCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPotMinCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputRendPotMaxCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMax2CV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMax3CV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMinCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMin2CV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputRendPotMin3CV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

        inputDispMediaCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTMedioArregloCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModIniCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputMantProgCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFijoCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoVariableCV.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });

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
            listaCiclosCombinadosController.borrarCicloCombinado(datosCicloCombinadoCorrida.getNombre());
        }
        ((Stage)inputCancelar.getScene().getWindow()).close();
    }
    private void addCombustible(){
        JFXComboBox<String> nuevoComb = new JFXComboBox<>();
        JFXComboBox<String> nuevaBarra = new JFXComboBox<>();
        nuevoComb.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        nuevaBarra.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        nuevoComb.setStyle("-fx-background-color: WHITE; -fx-font-size: 15px;");
        nuevaBarra.setStyle("-fx-background-color: WHITE; -fx-font-size: 15px;");
        nuevoComb.setPromptText("Elija...");
        nuevaBarra.setPromptText("Elija...");
        nuevoComb.setMinWidth(135);
        nuevoComb.setMaxWidth(135);
        nuevaBarra.setMinWidth(135);
        nuevaBarra.setMaxWidth(135);
        gridPane.add(nuevoComb, cantCombustibles +2,14,2,1);
        gridPane.add(nuevaBarra, cantCombustibles +2,15,2,1);
        combosCombustibles.put((((cantCombustibles +2)*10)+5), nuevoComb);
        combosCombustibles.put((((cantCombustibles +2)*10)+6), nuevaBarra);
        cantCombustibles += 2;
        GridPane.setColumnIndex(btnAddCombustible, cantCombustibles + 2);
        GridPane.setColumnIndex(btnRemoveCombustible, cantCombustibles + 2);
        if(cantCombustibles == 3){
            inputRendPotMin2TG.setVisible(true);
            inputRendPotMax2TG.setVisible(true);
            inputRendPotMin2TGEV.setVisible(true);
            inputRendPotMax2TGEV.setVisible(true);
            evForField(null, "rendPotMin2TG", inputRendPotMin2TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin2TG);
            evForField(null, "rendPotMax2TG", inputRendPotMax2TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax2TG);

            inputRendPotMin2CV.setVisible(true);
            inputRendPotMax2CV.setVisible(true);
            inputRendPotMin2CVEV.setVisible(true);
            inputRendPotMax2CVEV.setVisible(true);
            evForField(null, "rendPotMin2CV", inputRendPotMin2CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin2CV);
            evForField(null, "rendPotMax2CV", inputRendPotMax2CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax2CV);


        }
        if(cantCombustibles == 5) {
            btnAddCombustible.setVisible(false);
            inputRendPotMin3TG.setVisible(true);
            inputRendPotMax3TG.setVisible(true);
            inputRendPotMin3TGEV.setVisible(true);
            inputRendPotMax3TGEV.setVisible(true);
            evForField(null, "rendPotMin3TG", inputRendPotMin3TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin3TG);
            evForField(null, "rendPotMax3TG", inputRendPotMax3TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax3TG);

            inputRendPotMin3CV.setVisible(true);
            inputRendPotMax3CV.setVisible(true);
            inputRendPotMin3CVEV.setVisible(true);
            inputRendPotMax3CVEV.setVisible(true);
            evForField(null, "rendPotMin3CV", inputRendPotMin3CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin3CV);
            evForField(null, "rendPotMax3CV", inputRendPotMax3CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax3CV);
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
        cantCombustibles -= 2;
        gridPane.getChildren().remove(combosCombustibles.get((((cantCombustibles +2)*10)+5)));
        gridPane.getChildren().remove(combosCombustibles.get((((cantCombustibles +2)*10)+6)));
        if(cantCombustibles == 1){
            btnRemoveCombustible.setVisible(false);
            inputRendPotMin2TG.setVisible(false);
            inputRendPotMax2TG.setVisible(false);
            inputRendPotMin2TGEV.setVisible(false);
            inputRendPotMax2TGEV.setVisible(false);
            inputRendPotMin3TG.setVisible(false);
            inputRendPotMax3TG.setVisible(false);
            inputRendPotMin3TGEV.setVisible(false);
            inputRendPotMax3TGEV.setVisible(false);

            inputRendPotMin2CV.setVisible(false);
            inputRendPotMax2CV.setVisible(false);
            inputRendPotMin2CVEV.setVisible(false);
            inputRendPotMax2CVEV.setVisible(false);
            inputRendPotMin3CV.setVisible(false);
            inputRendPotMax3CV.setVisible(false);
            inputRendPotMin3CVEV.setVisible(false);
            inputRendPotMax3CVEV.setVisible(false);

        }
        if(cantCombustibles == 3){
            inputRendPotMin3TG.setVisible(false);
            inputRendPotMax3TG.setVisible(false);
            inputRendPotMin3TGEV.setVisible(false);
            inputRendPotMax3TGEV.setVisible(false);

            inputRendPotMin3CV.setVisible(false);
            inputRendPotMax3CV.setVisible(false);
            inputRendPotMin3CVEV.setVisible(false);
            inputRendPotMax3CVEV.setVisible(false);

        }
        gridPane.setColumnIndex(btnAddCombustible, cantCombustibles + 2);
        gridPane.setColumnIndex(btnRemoveCombustible, cantCombustibles + 2);
        btnAddCombustible.setVisible(true);

        if(((ComboBox<String>)combosCombustibles.get(((cantCombustibles +2)*10)+5)).getValue() != null) {
            combustiblesSeleccionados.remove(((ComboBox<String>)combosCombustibles.get(((cantCombustibles +2)*10)+5)).getValue());
            updateGenList(((ComboBox<String>)combosCombustibles.get(((cantCombustibles +2)*10)+5)).getValue(), null);
        }
    }

    private void addPosibleArranque(Pair<Double, Double> pa) {
        if(cantPosiblesArranques < 7) {
            cantPosiblesArranques++;
            JFXTextField nvoPA = new JFXTextField();
            nvoPA.textProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
            nvoPA.setFocusColor(Paint.valueOf("#27864d"));
            nvoPA.setStyle("-fx-background-color: WHITE; -fx-font-size: 15px;");
            nvoPA.setMinWidth(65);
            nvoPA.setMaxWidth(65);
            if (pa != null) {
                nvoPA.setText(pa.first + ";" + pa.second);
            }
            gridPane.add(nvoPA, cantPosiblesArranques, 18);
            gridPane.setColumnIndex(btnRemovePosiblesArranques, cantPosiblesArranques + 1);
            gridPane.setColumnIndex(btnAddPosiblesArranques, cantPosiblesArranques + 1);
            textFieldsArranques.put(cantPosiblesArranques, nvoPA);
        }



    }

    private void removePosibleArranque(){
        if(cantPosiblesArranques > 1) {
            gridPane.getChildren().remove(textFieldsArranques.get(cantPosiblesArranques));
            gridPane.setColumnIndex(btnRemovePosiblesArranques, cantPosiblesArranques);
            gridPane.setColumnIndex(btnAddPosiblesArranques, cantPosiblesArranques );
            cantPosiblesArranques--;
        }
    }

    private void addPotRampaArranque(Double pot) {
        if(cantPotenciaRampaArranque < 7) {
            cantPotenciaRampaArranque++;
            JFXTextField nvoRA = new JFXTextField();
            nvoRA.textProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
            nvoRA.setFocusColor(Paint.valueOf("#27864d"));
            nvoRA.setStyle("-fx-background-color: WHITE; -fx-font-size: 15px;");
            nvoRA.setMinWidth(65);
            nvoRA.setMaxWidth(65);
            if (pot != null) {
                nvoRA.setText(pot.toString());
            }
            gridPane.add(nvoRA, cantPotenciaRampaArranque, 19);
            gridPane.setColumnIndex(btnRemovePotRampaArranque, cantPotenciaRampaArranque + 1);
            gridPane.setColumnIndex(btnAddPotRampaArranque, cantPotenciaRampaArranque + 1);
            textFieldsPotArranques.put(cantPotenciaRampaArranque, nvoRA);
        }
    }
    private void removePotRampaArranque(){
        if(cantPotenciaRampaArranque > 1) {
            gridPane.getChildren().remove(textFieldsPotArranques.get(cantPotenciaRampaArranque));
            gridPane.setColumnIndex(btnRemovePotRampaArranque, cantPotenciaRampaArranque);
            gridPane.setColumnIndex(btnAddPotRampaArranque, cantPotenciaRampaArranque );
            cantPotenciaRampaArranque--;
        }
    }

    private void addPosibleParada(Double pp) {

        if(cantPosiblesParadas < 6) {
            cantPosiblesParadas++;
            JFXTextField nvoPP = new JFXTextField();
            nvoPP.textProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
            nvoPP.setFocusColor(Paint.valueOf("#27864d"));
            nvoPP.setStyle("-fx-background-color: WHITE; -fx-font-size: 15px;");
            nvoPP.setMinWidth(65);
            nvoPP.setMaxWidth(65);
            if (pp != null) {
                nvoPP.setText(pp.toString());
            }
            gridPane.add(nvoPP, cantPosiblesParadas, 20);
            gridPane.setColumnIndex(btnRemovePosiblesParadas, cantPosiblesParadas + 1);
            gridPane.setColumnIndex(btnAddPosiblesParadas, cantPosiblesParadas + 1);
            textFieldsParadas.put(cantPosiblesParadas, nvoPP);
        }
    }
    private void removePosibleParada(){
        if(cantPosiblesParadas > 1) {
            gridPane.getChildren().remove(textFieldsParadas.get(cantPosiblesParadas));
            gridPane.setColumnIndex(btnRemovePosiblesParadas, cantPosiblesParadas);
            gridPane.setColumnIndex(btnAddPosiblesParadas, cantPosiblesParadas );
            cantPosiblesParadas--;
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
            listaCiclosCombinadosController.unloadData();//Cambia color participantes incompletos
            listaCiclosCombinadosController.refresh();
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

    private void cargarArranque(String dato, List<Pair<Double, Double>> posArr) {
        if(UtilStrings.esParDeNumeroEntero(dato)){
            String[] par = dato.split(";");
            posArr.add(new Pair<>(Double.parseDouble(par[0]), Double.parseDouble(par[1])));
        }

    }

    private void cargarRampaArranque(String text, List<Double> potRampaArranque) {
        if(UtilStrings.esNumeroDouble(text)){
            potRampaArranque.add( Double.parseDouble(text)) ;
        }
    }

    private void cargarParadas(String text, List<Double> posiblesParadas) {
        if(UtilStrings.esNumeroDouble(text)){
            posiblesParadas.add( Double.parseDouble(text)) ;
        }
    }
    /**
     *  Método para cargar los datos en el DatosTermicoCorrida
     */
    public void loadData(){

        System.out.println("LOAD DATA-CICLO COMBINADO");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();

        Evolucion<Double> potMax1CV = null;
        if(UtilStrings.esNumeroDouble(inputPotMax1CV.getText())) { potMax1CV = EVController.loadEVsegunTipo(inputPotMax1CV, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMax1CV" );}

        Evolucion<Double> costoArranqueCA = null;
        if(UtilStrings.esNumeroDouble(inputCostoArranqueCicloAbierto.getText())) { costoArranqueCA = EVController.loadEVsegunTipo(inputCostoArranqueCicloAbierto, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoArranqueCicloAbierto" );}

        Evolucion<Double> costoArranqueCC = null;
        if(UtilStrings.esNumeroDouble(inputCostoArranqueCicloCombinado.getText())) { costoArranqueCC = EVController.loadEVsegunTipo(inputCostoArranqueCicloCombinado, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoArranqueCicloAbierto" );}

        ArrayList<String> listaCombustibles = new ArrayList<>();
        Hashtable<String,String> combustiblesBarras = new Hashtable<>();
        Hashtable<String,Evolucion<Double>> rendimientosPotMinTG = new Hashtable<>();
        Hashtable<String,Evolucion<Double>> rendimientosPotMaxTG = new Hashtable<>();

        Hashtable<String,Evolucion<Double>> rendimientosPotMinCV = new Hashtable<>();
        Hashtable<String,Evolucion<Double>> rendimientosPotMaxCV = new Hashtable<>();

        if(((JFXComboBox<String>) combosCombustibles.get(1)).getValue() != null) {
            String combustible = (String) ((JFXComboBox) combosCombustibles.get(1)).getValue();
            String barraComb = (String) ((JFXComboBox) combosCombustibles.get(2)).getValue();

            Evolucion<Double> rendPotMinTG = EVController.loadEVsegunTipo(inputRendPotMinTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMinTG" );
            Evolucion<Double> rendPotMaxTG = EVController.loadEVsegunTipo(inputRendPotMaxTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMaxTG" );
            rendimientosPotMinTG.put(combustible,rendPotMinTG);
            rendimientosPotMaxTG.put(combustible,rendPotMaxTG);

            Evolucion<Double> rendPotMinCV = EVController.loadEVsegunTipo(inputRendPotMinCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMinCV" );
            Evolucion<Double> rendPotMaxCV = EVController.loadEVsegunTipo(inputRendPotMaxCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMaxCV" );
            rendimientosPotMinCV.put(combustible,rendPotMinCV);
            rendimientosPotMaxCV.put(combustible,rendPotMaxCV);

            listaCombustibles.add(combustible);
            combustiblesBarras.put(combustible, barraComb);
            //TODO falta controlar que el campo este con datos antes de cargarlo a la corrida (para lista de combustibles y rendimientos)
            for (int i = 1; i < cantCombustibles; i += 2) {
                if(i==1) {
                    String combustible2 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 5)).getValue();
                    String barraComb2 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 6)).getValue();
                    listaCombustibles.add(combustible2);
                    combustiblesBarras.put(combustible2, barraComb2);

                    Evolucion<Double> rendPotMin2TG = EVController.loadEVsegunTipo(inputRendPotMin2TG, Text.EV_NUM_DOUBLE, evsPorNombre, "rendPotMin2TG");
                    Evolucion<Double> rendPotMax2TG = EVController.loadEVsegunTipo(inputRendPotMax2TG, Text.EV_NUM_DOUBLE, evsPorNombre, "rendPotMax2TG");
                    rendimientosPotMinTG.put(combustible2, rendPotMin2TG);
                    rendimientosPotMaxTG.put(combustible2, rendPotMax2TG);

                    Evolucion<Double> rendPotMin2CV = EVController.loadEVsegunTipo(inputRendPotMin2CV, Text.EV_NUM_DOUBLE, evsPorNombre, "rendPotMin2CV");
                    Evolucion<Double> rendPotMax2CV = EVController.loadEVsegunTipo(inputRendPotMax2CV, Text.EV_NUM_DOUBLE, evsPorNombre, "rendPotMax2CV");
                    rendimientosPotMinCV.put(combustible2, rendPotMin2CV);
                    rendimientosPotMaxCV.put(combustible2, rendPotMax2CV);

                }
                if(i == 3) {

                    String combustible3 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 5)).getValue();
                    String barraComb3 = (String) ((JFXComboBox) combosCombustibles.get(((i + 2) * 10) + 6)).getValue();
                    listaCombustibles.add(combustible3);
                    combustiblesBarras.put(combustible3, barraComb3);

                    Evolucion<Double> rendPotMin3 = EVController.loadEVsegunTipo(inputRendPotMin3TG, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMin3TG" );
                    Evolucion<Double> rendPotMax3 = EVController.loadEVsegunTipo(inputRendPotMax3TG, Text.EV_NUM_DOUBLE,  evsPorNombre,"rendPotMax3TG" );
                    rendimientosPotMinTG.put(combustible3, rendPotMin3);
                    rendimientosPotMaxTG.put(combustible3, rendPotMax3);

                }
            }

        }


        List<Pair<Double, Double>> posiblesArr = new ArrayList<>();
        textFieldsArranques.forEach((k,v) -> cargarArranque(v.getText(), posiblesArr));
        List<Double> potRampaArranque = new ArrayList<>();
        textFieldsPotArranques.forEach((k,v) -> cargarRampaArranque(v.getText(), potRampaArranque));
        List<Double> posiblesParadas = new ArrayList<>();
        textFieldsParadas.forEach((k,v) -> cargarParadas(v.getText() , posiblesParadas));//posiblesParadas.add( Double.parseDouble(v.getText()))) ;

        boolean salidaDetallada = inputSalidaDetallada.isSelected();

        Hashtable<String,Evolucion<String>> valoresComportamientosCicloComb = new Hashtable<>();
        if(inputCompCicloComb.getValue() != null){
            Evolucion<String> compMinimosTecnicos = EVController.loadEVsegunTipo(inputCompCicloComb, Text.EV_VAR,  evsPorNombre,"compCicloComb" );
            valoresComportamientosCicloComb.put(Constantes.COMPCC, compMinimosTecnicos);
        }

        //Datos TURBINA A GAS

        Evolucion<Integer> cantModInstTG = null;
        if(UtilStrings.esNumeroEntero(inputCantModInstTG.getText())) { cantModInstTG = EVController.loadEVsegunTipo(inputCantModInstTG, Text.EV_NUM_INT,  evsPorNombre,"cantModInstTG" );}

        Evolucion<Double> potMinTG = null;
        if(UtilStrings.esNumeroDouble(inputPotMinTG.getText())){ potMinTG = EVController.loadEVsegunTipo(inputPotMinTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMinTG" );}

        Evolucion<Double> potMaxTG = null;
        if(UtilStrings.esNumeroDouble(inputPotMaxTG.getText())){ potMaxTG = EVController.loadEVsegunTipo(inputPotMaxTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMaxTG" ); }

        Integer cantModIniTG = 0;
        if(UtilStrings.esNumeroEntero(inputCantModIniTG.getText())) {cantModIniTG = Integer.parseInt(inputCantModIniTG.getText());}

        Evolucion<Double> dispMediaTG = null;
        if(UtilStrings.esNumeroDouble(inputDispMediaTG.getText())){ dispMediaTG = EVController.loadEVsegunTipo(inputDispMediaTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"dispMediaTG" );}

        Evolucion<Double> tMedioArregloTG = null;
        if(UtilStrings.esNumeroDouble(inputTMedioArregloTG.getText())){ tMedioArregloTG = EVController.loadEVsegunTipo(inputTMedioArregloTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"tMedioArregloTG" ); }

        Evolucion<Integer> mantProgramadoTG = null;
        if(UtilStrings.esNumeroEntero(inputMantProgTG.getText())) { mantProgramadoTG = EVController.loadEVsegunTipo(inputMantProgTG, Text.EV_NUM_INT,  evsPorNombre,"mantProgTG" ); }

        Evolucion<Double> costoFijoTG = null;
        if(UtilStrings.esNumeroDouble(inputCostoFijoTG.getText())){ costoFijoTG = EVController.loadEVsegunTipo(inputCostoFijoTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoFijoTG" ); }

        Evolucion<Double> costoVariableTG = null;
        if(UtilStrings.esNumeroDouble(inputCostoVariableTG.getText())){ costoVariableTG = EVController.loadEVsegunTipo(inputCostoVariableTG, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoVariableTG" ); }

        //Datos CICLO A VAPOR

        Evolucion<Integer> cantModInstCV = null;
        if(UtilStrings.esNumeroEntero(inputCantModInstCV.getText())) { cantModInstCV = EVController.loadEVsegunTipo(inputCantModInstCV, Text.EV_NUM_INT,  evsPorNombre,"cantModInstCV" );}

        Evolucion<Double> potMinCV = null;
        if(UtilStrings.esNumeroDouble(inputPotMinCV.getText())){  potMinCV = EVController.loadEVsegunTipo(inputPotMinCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMinCV" ); }

        Evolucion<Double> potMaxCV = null;
        if(UtilStrings.esNumeroDouble(inputPotMaxCV.getText())){ potMaxCV = EVController.loadEVsegunTipo(inputPotMaxCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"potMaxCV" ); }

        Integer cantModIniCV = 0;
        if(UtilStrings.esNumeroEntero(inputCantModIniCV.getText())){  cantModIniCV = Integer.parseInt(inputCantModIniCV.getText()); }

        Evolucion<Double> dispMediaCV = null;
        if(UtilStrings.esNumeroDouble(inputDispMediaCV.getText())){ dispMediaCV = EVController.loadEVsegunTipo(inputDispMediaCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"dispMediaCV" ); }

        Evolucion<Double> tMedioArregloCV = null;
        if(UtilStrings.esNumeroDouble(inputTMedioArregloCV.getText())){ tMedioArregloCV = EVController.loadEVsegunTipo(inputTMedioArregloCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"tMedioArregloCV" ); }


        Evolucion<Integer> mantProgramadoCV = null;
        if(UtilStrings.esNumeroEntero(inputMantProgCV.getText())){ mantProgramadoCV = EVController.loadEVsegunTipo(inputMantProgCV, Text.EV_NUM_INT,  evsPorNombre,"mantProgCV" ); }

        Evolucion<Double> costoFijoCV = null;
        if(UtilStrings.esNumeroDouble(inputCostoFijoCV.getText())){ costoFijoCV = EVController.loadEVsegunTipo(inputCostoFijoCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoFijoCV" ); }

        Evolucion<Double> costoVariableCV = null;
        if(UtilStrings.esNumeroDouble(inputCostoVariableCV.getText())){ costoVariableCV = EVController.loadEVsegunTipo(inputCostoVariableCV, Text.EV_NUM_DOUBLE,  evsPorNombre,"costoVariableCV" ); }

        if(edicion){
            String nombreViejo = datosCicloCombinadoCorrida.getNombre();
            if(!nombre.equalsIgnoreCase(nombreViejo)){
                datosCorrida.getCcombinados().getCcombinados().remove(nombreViejo);
                int pos = datosCorrida.getCcombinados().getOrdenCargaXML().indexOf(nombreViejo);
                datosCorrida.getCcombinados().getOrdenCargaXML().set(pos, nombre);

                pos = datosCorrida.getCcombinados().getListaUtilizados().indexOf(nombreViejo);
                datosCorrida.getCcombinados().getListaUtilizados().set(pos, nombre);
            }

        } else{
            datosCicloCombinadoCorrida = new DatosCicloCombinadoCorrida(null,null, null);
            datosCorrida.getCcombinados().getListaUtilizados().add(nombre);
            datosCorrida.getCcombinados().getOrdenCargaXML().add(nombre);
        }



        DatosCicloCombParte turbinaGas = new DatosCicloCombParte( cantModInstTG, potMinTG, potMaxTG, rendimientosPotMaxTG,rendimientosPotMinTG,
                cantModIniTG, dispMediaTG,tMedioArregloTG,mantProgramadoTG, costoFijoTG,costoVariableTG);

        DatosCicloCombParte cicloVapor =new DatosCicloCombParte( cantModInstCV, potMinCV, potMaxCV, rendimientosPotMaxCV,rendimientosPotMinCV,
                cantModIniCV, dispMediaCV,tMedioArregloCV,mantProgramadoCV, costoFijoCV,costoVariableCV);

        //ACÁ VA PROPIETARIO COMO PARÁTMETRO, PERO PONEMOS UNOS NULL
        
        DatosTermicoCorrida dtg = new DatosTermicoCorrida("TurbinasAGas-" + nombre,null, turbinaGas);
        DatosTermicoCorrida dcc = new DatosTermicoCorrida("CiclosDeVapor-" + nombre,null, cicloVapor);


        datosCicloCombinadoCorrida = new DatosCicloCombinadoCorrida(dtg,dcc,potMax1CV);
        datosCicloCombinadoCorrida.setNombre(nombre);
        datosCicloCombinadoCorrida.setPropietario("UTE");

        datosCicloCombinadoCorrida.setBarra(barra);

        datosCicloCombinadoCorrida.setCostoArranque1TGCicloAbierto(costoArranqueCA);
        datosCicloCombinadoCorrida.setCostoArranque1TGCicloCombinado(costoArranqueCC);

        datosCicloCombinadoCorrida.setListaCombustibles(listaCombustibles);
        datosCicloCombinadoCorrida.setBarrasCombustible(combustiblesBarras);

        datosCicloCombinadoCorrida.setPosiblesArranques(posiblesArr);
        datosCicloCombinadoCorrida.setPotRampaArranque(potRampaArranque);
        datosCicloCombinadoCorrida.setPosiblesParadas(posiblesParadas);

        datosCicloCombinadoCorrida.setValoresComportamientos(valoresComportamientosCicloComb);
        datosCicloCombinadoCorrida.setSalDetallada(salidaDetallada);

        datosCorrida.getCcombinados().getCcombinados().put(nombre, datosCicloCombinadoCorrida);

    }




    /**
     * Método para obtener los datos del DatosTermicoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-CICLO COMBINADO");
        long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        if(datosCicloCombinadoCorrida.getValoresComportamientos().get(Constantes.COMPCC) != null){
        inputCompCicloComb.setValue(datosCicloCombinadoCorrida.getValoresComportamientos().get(Constantes.COMPCC).getValor(instanteActual));}
        evForField(datosCicloCombinadoCorrida.getValoresComportamientos().get(Constantes.COMPCC), "compCicloComb", inputCompCicloCombEV, Text.EV_VAR, Text.COMP_CICLO_COMB_VALS, inputCompCicloComb);

        inputNombre.setText(datosCicloCombinadoCorrida.getNombre());
        inputBarra.getSelectionModel().select(Text.BARRA_1);//uninodal hardcodeado

        if(datosCicloCombinadoCorrida.getPotMax1CV() != null){
        inputPotMax1CV.setText(datosCicloCombinadoCorrida.getPotMax1CV().getValor(instanteActual).toString());}
        evForField(null, "potMax1CV", inputPotMax1CVEV, Text.EV_NUM_DOUBLE, null, inputPotMax1CV);

        if(datosCicloCombinadoCorrida.getCostoArranque1TGCicloAbierto() != null){
        inputCostoArranqueCicloAbierto.setText(datosCicloCombinadoCorrida.getCostoArranque1TGCicloAbierto().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getCostoArranque1TGCicloAbierto(), "costoArranqueCicloAbierto", inputCostoArranqueCicloAbiertoEV, Text.EV_NUM_DOUBLE, null, inputCostoArranqueCicloAbierto);

        if(datosCicloCombinadoCorrida.getCostoArranque1TGCicloCombinado() != null){
        inputCostoArranqueCicloCombinado.setText(datosCicloCombinadoCorrida.getCostoArranque1TGCicloCombinado().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getCostoArranque1TGCicloCombinado(), "costoArranqueCicloCombinado", inputCostoArranqueCicloCombinadoEV, Text.EV_NUM_DOUBLE, null, inputCostoArranqueCicloCombinado);


        int i = 0;
        int j = 1;
        for(HashMap.Entry<String, String> entry : datosCicloCombinadoCorrida.getBarrasCombustible().entrySet()) {
            if(i==0){
                inputListaComb1.getSelectionModel().select(entry.getKey());
                inputListaBarraComb1.getItems().addAll(combustiblesYBarras.get(entry.getKey()));
                inputListaBarraComb1.getSelectionModel().select(entry.getValue());
            }else{
                addCombustible();
                ((ComboBox)combosCombustibles.get(((i+2)*10)+5)).getSelectionModel().select(entry.getKey());
                ((ComboBox)combosCombustibles.get(((i+2)*10)+6)).getItems().addAll(combustiblesYBarras.get(entry.getKey()));
                ((ComboBox)combosCombustibles.get(((i+2)*10)+6)).getSelectionModel().select(entry.getValue());
                i++;
            }

            if(j==1 && datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().size() > 0){
                inputRendPotMinTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString());
                inputRendPotMaxTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());
                evForField(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().get(entry.getKey()), "rendPotMinTG", inputRendPotMinTGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMinTG);
                evForField(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMax().get(entry.getKey()), "rendPotMaxTG", inputRendPotMaxTGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMaxTG);

                inputRendPotMinCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString());
                inputRendPotMaxCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());
                evForField(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMin().get(entry.getKey()), "rendPotMinCV", inputRendPotMinCVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMinCV);
                evForField(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMax().get(entry.getKey()), "rendPotMaxCV", inputRendPotMaxCVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMaxCV);


            }else if(j==2){
                inputRendPotMin2TG.setText(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString());
                inputRendPotMax2TG.setText(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());
                evForField(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().get(entry.getKey()), "rendPotMin2TG", inputRendPotMin2TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin2TG);
                evForField(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMax().get(entry.getKey()), "rendPotMax2TG", inputRendPotMax2TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax2TG);
                inputRendPotMin2TG.setVisible(true);
                inputRendPotMax2TG.setVisible(true);

                inputRendPotMin2CV.setText(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString());
                inputRendPotMax2CV.setText(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());
                evForField(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMin().get(entry.getKey()), "rendPotMin2CV", inputRendPotMin2CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin2CV);
                evForField(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMax().get(entry.getKey()), "rendPotMax2CV", inputRendPotMax2CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax2CV);
                inputRendPotMin2CV.setVisible(true);
                inputRendPotMax2CV.setVisible(true);


            }else if(j==3){
                inputRendPotMin3TG.setText(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString());
                inputRendPotMax3TG.setText(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());
                evForField(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMin().get(entry.getKey()), "rendPotMin3TG", inputRendPotMin3TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin3TG);
                evForField(datosCicloCombinadoCorrida.getDatosTGs().getRendimientosPotMax().get(entry.getKey()), "rendPotMax3TG", inputRendPotMax3TGEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax3TG);
                inputRendPotMin3TG.setVisible(true);
                inputRendPotMax3TG.setVisible(true);

                inputRendPotMin3CV.setText(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMin().get(entry.getKey()).getValor(instanteActual).toString());
                inputRendPotMax3CV.setText(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMax().get(entry.getKey()).getValor(instanteActual).toString());
                evForField(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMin().get(entry.getKey()), "rendPotMin3CV", inputRendPotMin3CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMin3CV);
                evForField(datosCicloCombinadoCorrida.getDatosCCs().getRendimientosPotMax().get(entry.getKey()), "rendPotMax3CV", inputRendPotMax3CVEV, Text.EV_NUM_DOUBLE, null,inputRendPotMax3CV);
                inputRendPotMin3CV.setVisible(true);
                inputRendPotMax3CV.setVisible(true);
            }
            i++;
            j++;
        }



        int pos = 0;
        for( Pair<Double, Double>  entry: datosCicloCombinadoCorrida.getPosiblesArranques()){
            if(pos==0){  inputPosiblesArranques1.setText(entry.first + ";" + entry.second); }
            else{ addPosibleArranque(entry); }
            pos++;
        }

        pos = 0;
        for( Double entry: datosCicloCombinadoCorrida.getPotRampaArranque()){
            if(pos==0)  {  inputPotenciaRampaArranque1.setText(entry.toString());  }
            else{ addPotRampaArranque(entry); }
            pos++;
        }

        pos = 0;
        for( Double entry: datosCicloCombinadoCorrida.getPosiblesParadas()){
            if(pos==0){   inputPosiblesParadas1.setText(entry.toString());   }
            else{ addPosibleParada(entry); }
            pos++;
        }


        inputSalidaDetallada.setSelected(datosCicloCombinadoCorrida.isSalDetallada());


        //DATOS TURBINA A GAS

        if(datosCicloCombinadoCorrida.getDatosTGs().getCantModInst() != null){
        inputCantModInstTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getCantModInst().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getCantModInst(), "cantModInstTG", inputCantModInstTGEV, Text.EV_NUM_DOUBLE, null, inputCantModInstTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().getPotMin() != null){
        inputPotMinTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getPotMin().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getPotMin(), "potMinTG", inputPotMinTGEV, Text.EV_NUM_DOUBLE, null,inputPotMinTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().getPotMax() != null){
        inputPotMaxTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getPotMax().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getPotMax(), "potMaxTG", inputPotMaxTGEV, Text.EV_NUM_DOUBLE, null,inputPotMaxTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().getCantModIni() != null){
        inputCantModIniTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getCantModIni().toString());}

        if(datosCicloCombinadoCorrida.getDatosTGs().getDispMedia() != null){
        inputDispMediaTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getDispMedia().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getDispMedia(), "dispMediaTG", inputDispMediaTGEV, Text.EV_NUM_DOUBLE, null, inputDispMediaTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().gettMedioArreglo() != null){
        inputTMedioArregloTG.setText(datosCicloCombinadoCorrida.getDatosTGs().gettMedioArreglo().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().gettMedioArreglo(), "tMedioArregloTG", inputTMedioArregloTGEV, Text.EV_NUM_DOUBLE, null, inputTMedioArregloTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().getMantProgramado() != null){
        inputMantProgTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getMantProgramado().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getMantProgramado(), "mantProgTG", inputMantProgTGEV, Text.EV_NUM_INT, null, inputMantProgTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().getCostoFijo() != null){
        inputCostoFijoTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getCostoFijo().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getCostoFijo(), "costoFijoTG", inputCostoFijoTGEV, Text.EV_NUM_DOUBLE, null, inputCostoFijoTG);

        if(datosCicloCombinadoCorrida.getDatosTGs().getCostoVariable() != null){
        inputCostoVariableTG.setText(datosCicloCombinadoCorrida.getDatosTGs().getCostoVariable().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosTGs().getCostoVariable(), "costoVariableTG", inputCostoVariableTGEV, Text.EV_NUM_DOUBLE, null, inputCostoVariableTG);


        //DATOS CICLO DE VAPOR

        if(datosCicloCombinadoCorrida.getDatosCCs().getCantModInst() != null){
        inputCantModInstCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getCantModInst().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getCantModInst(), "cantModInstCV", inputCantModInstCVEV, Text.EV_NUM_DOUBLE, null, inputCantModInstCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().getPotMax() != null){
        inputPotMaxCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getPotMax().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getPotMax(), "potMaxCV", inputPotMaxCVEV, Text.EV_NUM_DOUBLE, null,inputPotMaxCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().getPotMin() != null){
        inputPotMinCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getPotMin().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getPotMin(), "potMinCV", inputPotMinCVEV, Text.EV_NUM_DOUBLE, null,inputPotMinCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().getCantModIni() != null){
        inputCantModIniCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getCantModIni().toString());}

        if(datosCicloCombinadoCorrida.getDatosCCs().getDispMedia() != null){
        inputDispMediaCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getDispMedia().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getDispMedia(), "dispMediaCV", inputDispMediaCVEV, Text.EV_NUM_DOUBLE, null, inputDispMediaCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().gettMedioArreglo() != null){
        inputTMedioArregloCV.setText(datosCicloCombinadoCorrida.getDatosCCs().gettMedioArreglo().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().gettMedioArreglo(), "tMedioArregloCV", inputTMedioArregloCVEV, Text.EV_NUM_DOUBLE, null, inputTMedioArregloCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().getMantProgramado() != null){
        inputMantProgCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getMantProgramado().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getMantProgramado(), "mantProgCV", inputMantProgCVEV, Text.EV_NUM_INT, null, inputMantProgCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().getCostoFijo() != null){
        inputCostoFijoCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getCostoFijo().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getCostoFijo(), "costoFijoCV", inputCostoFijoCVEV, Text.EV_NUM_DOUBLE, null, inputCostoFijoCV);

        if(datosCicloCombinadoCorrida.getDatosCCs().getCostoVariable() != null){
        inputCostoVariableCV.setText(datosCicloCombinadoCorrida.getDatosCCs().getCostoVariable().getValor(instanteActual).toString());}
        evForField(datosCicloCombinadoCorrida.getDatosCCs().getCostoVariable(), "costoVariableCV", inputCostoVariableCVEV, Text.EV_NUM_DOUBLE, null, inputCostoVariableCV);



    }

    private ArrayList<String> controlDatosCompletos(){

        ArrayList<String> errores = new ArrayList<>();

        if(inputCompCicloComb.getValue() == null){ errores.add( "Editor CicloCombinado: " + inputNombre.getText() +" Comportamientos generales vacío.");}
        if(inputNombre.getText().trim().equals("")) { errores.add( "Editor CicloCombinado.  Nombre vacío"); }
        if( inputBarra.getValue() == null) { errores.add("Editor CicloCombinado: " + inputNombre.getText() + " Barra vacío.") ; }
        if(inputPotMax1CV.getText().trim().equals("")) { errores.add("Editor CicloCombinado: " + inputNombre.getText() + " PotMax1CV vacío.") ; }
        if(!UtilStrings.esNumeroDouble(inputPotMax1CV.getText())) { errores.add("Editor CicloCombinado: " + inputNombre.getText() + " PotMax1CV no es dato numérico.") ; }
        if(inputCostoArranqueCicloAbierto.getText().trim().equals("")) errores.add("Editor CicloCombinado: " + inputNombre.getText() + " Costo Arranque CA vacío.") ;
        if(!UtilStrings.esNumeroDouble( inputCostoArranqueCicloAbierto.getText())) errores.add("Editor CicloCombinado: " + inputNombre.getText() + " Costo Arranque CA no es dato numérico.") ;

        if(inputCostoArranqueCicloCombinado.getText().trim().equals("")) errores.add("Editor CicloCombinado: " + inputNombre.getText() + " Costo arranque CC vacío.") ;
        if(!UtilStrings.esNumeroDouble( inputCostoArranqueCicloCombinado.getText())) errores.add("Editor CicloCombinado: " + inputNombre.getText() + " Costo arranque CC no es dato numérico.") ;

        if(inputListaComb1.getValue() == null) errores.add("Editor CicloCombinado: " + inputNombre.getText() + "  Combustible vacío.") ;
        if(inputListaBarraComb1.getValue() == null) errores.add("Editor CicloCombinado: " + inputNombre.getText() + "  Barra Combustible vacío.") ;


        textFieldsArranques.forEach((k,v) ->  {
            if(!UtilStrings.esParDeNumeroEntero(v.getText())){
                errores.add( "Editor Ciclo Combinado "+ inputNombre.getText() + ":  Posibles arranques debe ser par de numeros separados por punto y coma, por ejemplo: 21;8");
            }
        });

        textFieldsPotArranques.forEach((k,v) ->  {
            if(!UtilStrings.esNumeroDouble(v.getText())){
                errores.add( "Editor Ciclo Combinado "+ inputNombre.getText() + ":  Potencia rampa arranque no es dato numérico");
            }
        });

        textFieldsParadas.forEach((k,v) ->  {
            if(!UtilStrings.esNumeroDouble(v.getText())){
                errores.add( "Editor Ciclo Combinado  "+ inputNombre.getText() + " Posibles paradas no es dato numérico");
            }
        });

        //DATOS TURBINA A GAS

        if(inputCantModInstTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Cantidad modulos instalados vacío.") ;
        if(!UtilStrings.esNumeroEntero(inputCantModInstTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Cantidad modulos instalados no es dato numérico.") ;

        if(inputPotMinTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Potencia mínima vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputPotMinTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Potencia mínima no es dato numérico.") ;
        if(inputPotMaxTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Potencia máxima vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputPotMaxTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Potencia máxima no es dato numérico.") ;

        if(inputCantModIniTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Cantidad modulos inicial vacío.") ;
        if(!UtilStrings.esNumeroEntero(inputCantModIniTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Cantidad inicial instalados no es dato numérico.") ;

        if(inputDispMediaTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Disponibilidad media vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputDispMediaTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Disponibilidad media no es dato numérico.") ;
        if( !controlDisponibilidadMedia(inputDispMediaTG)) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Disponibilidad media no es double entre 0 y 1.");

        if(inputTMedioArregloTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Tiempo medio de arreglo vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputTMedioArregloTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Tiempo medio de arreglo no es dato numérico.") ;

        if(inputMantProgTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Mantenimiento programado vacío.") ;
        if(!UtilStrings.esNumeroEntero(inputMantProgTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Mantenimiento programado no es dato numérico.") ;

        if(inputCostoFijoTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Costo fijo vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputCostoFijoTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Costo fijo no es dato numérico.") ;

        if(inputCostoVariableTG.getText().trim().equals("")) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Costo variable vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputCostoVariableTG.getText())) errores.add("Editor CicloCombinado - TG: " + inputNombre.getText() + "  Costo variable no es dato numérico.") ;



        //DATOS CICLO DE VAPOR

        if(inputCantModInstCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Cantidad modulos instalados vacío.") ;
        if(!UtilStrings.esNumeroEntero(inputCantModInstCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Cantidad modulos instalados no es dato numérico.") ;

        if(inputPotMinCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Potencia mínima vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputPotMinCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Potencia mínima no es dato numérico.") ;
        if(inputPotMaxCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Potencia máxima vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputPotMaxCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Potencia máxima no es dato numérico.") ;

        if(inputCantModIniCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Cantidad modulos inicial vacío.") ;
        if(!UtilStrings.esNumeroEntero(inputCantModIniCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Cantidad inicial instalados no es dato numérico.") ;

        if(inputDispMediaCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Disponibilidad media vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputDispMediaCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Disponibilidad no es dato numérico.") ;
        if( !controlDisponibilidadMedia(inputDispMediaCV)) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Disponibilidad media no es double entre 0 y 1.");

        if(inputTMedioArregloCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Tiempo medio de arreglo vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputTMedioArregloCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Tiempo medio de arreglo no es dato numérico.") ;

        if(inputMantProgCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Mantenimiento programado vacío.") ;
        if(!UtilStrings.esNumeroEntero(inputMantProgCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Mantenimiento programado no es dato numérico.") ;

        if(inputCostoFijoCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Costo fijo vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputCostoFijoCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Costo fijo no es dato numérico.") ;

        if(inputCostoVariableCV.getText().trim().equals("")) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Costo variable vacío.") ;
        if(!UtilStrings.esNumeroDouble(inputCostoVariableCV.getText())) errores.add("Editor CicloCombinado - CV: " + inputNombre.getText() + "  Costo variable no es dato numérico.") ;

        return errores;
    }
}
