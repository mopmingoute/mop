/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Manejador is part of MOP.
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
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import logica.CorridaHandler;
import org.controlsfx.control.PopOver;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import persistencia.CargadorXML;
import persistencia.EscritorXML;
import presentacion.PresentacionHandler;
import tiempo.LineaTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LectorPropiedades;
import utilitarios.UtilStrings;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class Manejador extends GeneralidadesController {



    @FXML private MenuItem menuNuevaCorrida;
    @FXML private MenuItem menuCargarCorrida;
    @FXML private MenuItem menuGuardarCorrida;
    @FXML private MenuItem menuGuardarComoCorrida;
    @FXML private MenuItem menuSalir;
    @FXML private MenuItem menuAcercaDelMop;
    @FXML private JFXTabPane mainTabPane;
    private String savePath = "";

    @FXML private JFXButton part3LeftRightButton;
    @FXML private JFXButton part3UpDownButtonLeft;
    @FXML private JFXButton part3UpDownButtonRight;
    @FXML private SplitPane part3SplitPaneLeftRight;
    @FXML private SplitPane part3SplitPaneUpDown;
    @FXML private Label leftRightButtonLabel;
    @FXML private AnchorPane leftPane;
    @FXML private AnchorPane topPane;
    @FXML private AnchorPane bottomPane;
    @FXML private AnchorPane bottomPaneContent;
    @FXML private ScrollPane bottomPanelScrollPane;
    @FXML private AnchorPane middlePane;
//    @FXML private VBox vBoxBloques;
    @FXML private GridPane gridPaneBloque;
    @FXML private AnchorPane anchorPaneBloque;
    @FXML private JFXTextField textFieldCantPostes;
    @FXML private VBox vBoxParque1;
    @FXML private VBox vBoxParque2;
    @FXML private VBox vBoxParque3;
//    private VBox[] vBoxParques = new VBox[]{vBoxParque1,vBoxParque2,vBoxParque3};
    private VBox[] vBoxParques = new VBox[3];
    @FXML private GridPane testGridPane;
    @FXML private AnchorPane testAnchorPane;
    @FXML private JFXCheckBox testCheckBoxVisible;
    @FXML private Button testDrawerButton;
    @FXML private JFXDrawer testDrawer;
    @FXML private JFXListView testListView;

    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton comporDefectoDoneBtn;
    @FXML private AnchorPane testCompoPorDefecto;

    @FXML private JFXButton btnNewEol;
    @FXML private JFXButton btnNewSol;
    @FXML private JFXButton btnNewTerm;
    @FXML private JFXButton btnNewCicloComb;
    @FXML private JFXButton btnNewHidro;
    @FXML private JFXButton btnNewAcum;
    @FXML private JFXButton btnNewDemanda;
    @FXML private JFXButton btnNewFalla;
    @FXML private JFXButton btnNewImpoExpo;
    @FXML private JFXButton btnNewRedComb;
    @FXML private JFXButton btnNewRedElec;
    @FXML private JFXButton btnNewImpacto;
    @FXML private JFXButton btnNewContrato;

    String nombreParticipanteClipboard;
    String tipoParticipanteClipboard;

    @FXML private JFXButton btnNewEolClipboard;
    @FXML private JFXButton btnNewSolClipboard;
    @FXML private JFXButton btnNewTermClipboard;
    @FXML private JFXButton btnNewHidroClipboard;
    @FXML private JFXButton btnNewCicloCombClipboard;
    @FXML private JFXButton btnNewAcumClipboard;
    @FXML private JFXButton btnNewDemandaClipboard;
    @FXML private JFXButton btnNewFallaClipboard;
    @FXML private JFXButton btnNewImpoExpoClipboard;
    @FXML private JFXButton btnNewRedCombClipboard;
    @FXML private JFXButton btnNewRedElecClipboard;
    @FXML private JFXButton btnNewImpactoClipboard;
    @FXML private JFXButton btnNewContratoClipboard;

    @FXML private AnchorPane nuevoPartAnchorRowEolico;
    @FXML private AnchorPane nuevoPartAnchorRowSolar;
    @FXML private AnchorPane nuevoPartAnchorRowTermico;
    @FXML private AnchorPane nuevoPartAnchorRowCicloComb;
    @FXML private AnchorPane nuevoPartAnchorRowHidraulico;
    @FXML private AnchorPane nuevoPartAnchorRowCentralAcumulacion;
    @FXML private AnchorPane nuevoPartAnchorRowDemanda;
    @FXML private AnchorPane nuevoPartAnchorRowFalla;
    @FXML private AnchorPane nuevoPartAnchorRowImpoExpo;
    @FXML private AnchorPane nuevoPartAnchorRowCombustible;
    @FXML private AnchorPane nuevoPartAnchorRowRedElectrica;
    @FXML private AnchorPane nuevoPartAnchorRowImpacto;
    @FXML private AnchorPane nuevoPartAnchorRowContrato;



    private boolean leftRightOpened = true;
    private boolean topMiddleOpened = true;
    private boolean bottomMiddleOpened = true;

    private boolean updating = false;

    private Integer cantPostes = 0;
    private Map<Integer, JFXTextField> durPostes = new HashMap<Integer, JFXTextField>();
    @FXML private JFXButton nuevoBloqueButton;
    private List<BloqueLineaDeTiempoController> bloqueControllerList = new ArrayList<>();
    private List<TitledPane> bloques = new ArrayList<>();


    //DATA TYPE CORRIDA
    public DatosCorrida datosCorrida = new DatosCorrida();

    //INPUTS DATA PARAM GENERALES




    //INPUTS DATA PROCESOS ESTOCASTICOS
    @FXML private AnchorPane cargaProcesosEstocasticosPane;
    private CargaProcesosEstocasticosController cargaProcesosEstocasticosPaneController;
    @FXML private AnchorPane estimacionProcesosEstocasticosPane;
    private EstimadorBaseController estimadorBaseController;



    private Hashtable<String,Boolean> procesosEstocasticosEnuso = new Hashtable<>();



    // REPORTES
    @FXML private AnchorPane anchorPaneReportes;
    @FXML private AnchorPane reportesMenuPane;
    @FXML private AnchorPane reportesContentPane;
    @FXML private AnchorPane reportesInputPane;


    // NUEVA LINEA DE TIEMPO
    @FXML private AnchorPane nuevaLineaDeTiempoPane;
    private LineaDeTiempoSimpleController lineaDeTiempoSimpleController;
    private static boolean nuevaLineaDeTiempoActiva = true;
    private LineaDeTiempoDetalladaController lineaDeTiempoDetalladaController;

    public void setLineaDeTiempoDetalladaControllerSugerenciaCorreccion(String lineaDeTiempoDetalladaControllerSugerenciaCorreccion) {
        this.lineaDeTiempoDetalladaControllerSugerenciaCorreccion = lineaDeTiempoDetalladaControllerSugerenciaCorreccion;
    }

    private String lineaDeTiempoDetalladaControllerSugerenciaCorreccion = "";



    // SALIDA
    @FXML private AnchorPane salidaPane;
    private SalidaController salidaController;

    // OPTIMIZACION Y SIMULACION
    @FXML private AnchorPane optimizacionSimulacionPane;
    private OptimizacionSimulacionController optimizacionSimulacionController;

    // BIBLIOTECA
    @FXML private AnchorPane bibliotecaPane;



    public PresentacionHandler ph;
    private String rutaXMLCarga;


    // LOGIN
    @FXML private JFXButton btnLogin;
    @FXML private AnchorPane loginPane;
    boolean loginVisible = false;
    public static String user;


    private ListaEolicosController listaEolicosController;
    private ListaSolaresController listaSolaresController;
    private ListaTermicosController listaTermicosController;
    private ListaCiclosCombinadosController listaCiclosCombinadosController;
    private ListaHidraulicosController listaHidraulicosController;
    private ListaCentralesAcumulacionController listaCentralesAcumulacionController;
    private ListaDemandasController listaDemandasController;
    private ListaFallasController listaFallasController;
    private ListaImpoExpoController listaImpoExpoController;
    private ListaCombustiblesController listaCombustiblesController;
    private ListaImpactosController listaImpactosController;
    private ListaContratosController listaContratosController;
    private ListaRedElectricaController listaRedElectricaController;

    private LineaDeEntradaController lineaDeEntradaController;
    private ReportesContentController reportesContentController;
    private ReportesInputPanelController reportesInputPanelController;

    private BibliotecaController bibliotecaController;

    private Properties toolTips;


    @FXML
    public void initialize() {

        toolTips =  loadToolTipsProperties();


        /////////
//        MongoDBHandler.getInstance().getPEList();
//        ArrayList<String> bibliotecas = new ArrayList<>(Arrays.asList("biblioteca_1","biblioteca_2","biblioteca_3"));
//        ArrayList<String> bibliotecas = new ArrayList<>(Arrays.asList("biblioteca_2"));
//        MongoDBHandler.getInstance().getParticipantes(bibliotecas);
        /////////




        menuNuevaCorrida.setOnAction(actionEvent -> nuevaCorrida());

        menuCargarCorrida.setOnAction(actionEvent -> cargarCorrida());

        menuGuardarCorrida.setOnAction(actionEvent -> guardarCorrida(false, true));

        menuGuardarComoCorrida.setOnAction(actionEvent -> guardarCorrida(true, true));

        menuSalir.setOnAction(actionEvent -> salir());

        menuAcercaDelMop.setOnAction(actionEvent -> linkManuales());

        vBoxParques[0] = vBoxParque1;
        vBoxParques[1] = vBoxParque2;
        vBoxParques[2] = vBoxParque3;


        //LINEA DE ENTRADA
        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeTiempo2.fxml"));
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeTiempo3.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeEntrada.fxml"));
            lineaDeEntradaController = new LineaDeEntradaController();
            loader.setController(lineaDeEntradaController);
//            bottomPanelScrollPane.setContent(loader.load());
            AnchorPane aP = loader.load();
            bottomPaneContent.getChildren().add(aP);
            AnchorPane.setTopAnchor(aP, 0.0);
            AnchorPane.setLeftAnchor(aP, 0.0);
            AnchorPane.setRightAnchor(aP, 0.0);
            AnchorPane.setBottomAnchor(aP, 0.0);
        }catch (Exception e){
            e.printStackTrace();
        }


        //SPLIT SCRREN FUNC
        leftPane = (AnchorPane) part3SplitPaneLeftRight.getItems().get(0);

        part3LeftRightButton.setOnAction(actionEvent -> {
            if(leftRightOpened){
//                    part3SplitPaneLeftRight.setDividerPosition(0, -1);
//                    part3SplitPaneLeftRight.setDividerPosition(0, -1);
                leftRightButtonLabel.setRotate(90);
                part3SplitPaneLeftRight.getItems().remove(leftPane);
                vBoxParque1.setMinWidth(617);
                vBoxParque2.setMinWidth(617);
                vBoxParque3.setMinWidth(617);
            }else{
                part3SplitPaneLeftRight.setDividerPosition(0, .17);
                part3SplitPaneLeftRight.setDividerPosition(1, 0);
                leftRightButtonLabel.setRotate(270);
                part3SplitPaneLeftRight.getItems().add(0, leftPane);
                vBoxParque1.setMinWidth(512);
                vBoxParque2.setMinWidth(512);
                vBoxParque3.setMinWidth(512);
            }
            leftRightOpened = !leftRightOpened;
        });

        topPane = (AnchorPane) part3SplitPaneUpDown.getItems().get(0);

        part3UpDownButtonLeft.setOnAction(actionEvent -> {
            if(topMiddleOpened) {
                lineaDeEntradaController.uncapHeight();
                if(!bottomMiddleOpened) {
                    part3SplitPaneUpDown.getItems().add(2, bottomPane);
                    bottomMiddleOpened = true;
                    part3UpDownButtonRight.setText("▲");
                }
                part3SplitPaneUpDown.getItems().remove(topPane);
                part3UpDownButtonLeft.setText("---");
            }else{
                lineaDeEntradaController.capHeight();
                part3SplitPaneUpDown.setDividerPosition(0, .495);
                part3SplitPaneUpDown.setDividerPosition(1, .495);

                part3SplitPaneUpDown.getItems().add(0, topPane);
                part3UpDownButtonLeft.setText("▲");
            }
            topMiddleOpened = !topMiddleOpened;
        });

        bottomPane = (AnchorPane) part3SplitPaneUpDown.getItems().get(2);

        part3UpDownButtonRight.setOnAction(actionEvent -> {
            if(bottomMiddleOpened) {
                lineaDeEntradaController.uncapHeight();
                part3SplitPaneUpDown.getItems().remove(bottomPane);
                part3UpDownButtonRight.setText("---");
                if(!topMiddleOpened) {
                    part3SplitPaneUpDown.getItems().add(0, topPane);
                    part3UpDownButtonLeft.setText("▲");
                    topMiddleOpened = true;
                }
//                    hideVisible(testCheckBoxVisible.isSelected(), true);
            }else{
                lineaDeEntradaController.capHeight();
                part3SplitPaneUpDown.setDividerPosition(0, .495);
                part3SplitPaneUpDown.setDividerPosition(1, .495);
                part3SplitPaneUpDown.getItems().add(2, bottomPane);
                part3UpDownButtonRight.setText("▲");
//                    hideVisible(testCheckBoxVisible.isSelected(), false);
            }
            bottomMiddleOpened = !bottomMiddleOpened;
        });



        try {

            cargaProcesosEstocasticosPane.getChildren().clear();
            cargaProcesosEstocasticosPaneController = new CargaProcesosEstocasticosController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CargaProcesosEstocasticos.fxml"));
            loader.setController(cargaProcesosEstocasticosPaneController);
            AnchorPane aP = loader.load();
            cargaProcesosEstocasticosPane.getChildren().add(aP);
            //cargaProcesosEstocasticosPane.unloadData();


            AnchorPane.setTopAnchor(aP, 0.0);
            AnchorPane.setLeftAnchor(aP, 0.0);
            AnchorPane.setRightAnchor(aP, 0.0);
            AnchorPane.setBottomAnchor(aP, 0.0);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            estimacionProcesosEstocasticosPane.getChildren().clear();
            estimadorBaseController = new EstimadorBaseController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EstimadorBase.fxml"));
            loader.setController(estimadorBaseController);
            AnchorPane aP = loader.load();
            estimacionProcesosEstocasticosPane.getChildren().add(aP);
            //cargaProcesosEstocasticosPane.unloadData();
        }catch (Exception e){
            e.printStackTrace();
        }


        //NUEVOS PARTICIPANTES

        //EOLICO
        btnNewEol.setOnAction(actionEvent -> {

            openEditor("EditorEolico.fxml", "Editor Eólico", Text.EOLICO_SIZE[0], Text.EOLICO_SIZE[1], new EditorEolicoController(datosCorrida, listaEolicosController));
        });
        toogleAddButton(nuevoPartAnchorRowEolico, btnNewEol);
//        toogleAddButton(nuevoPartAnchorRow00, btnNewEolClipboard);

        //SOLAR
        btnNewSol.setOnAction(actionEvent -> openEditor("EditorSolar.fxml", "Editor Solar", Text.SOLAR_SIZE[0], Text.SOLAR_SIZE[1], new EditorSolarController(datosCorrida, listaSolaresController)));
        toogleAddButton(nuevoPartAnchorRowSolar, btnNewSol);

        //TERMICO
        btnNewTerm.setOnAction(actionEvent -> openEditor("EditorTermico.fxml", "Editor Térrmico", Text.TERMICO_SIZE[0], Text.TERMICO_SIZE[1], new EditorTermicoController(datosCorrida, listaTermicosController)));
        toogleAddButton(nuevoPartAnchorRowTermico, btnNewTerm);

        //CICLO COMBINADO
        btnNewCicloComb.setOnAction(actionEvent -> openEditor("EditorCicloCombinado.fxml", "Editor Ciclo Combinado", Text.CICLO_COMBINADO_SIZE[0], Text.CICLO_COMBINADO_SIZE[1], new EditorCicloCombinadoController(datosCorrida, listaCiclosCombinadosController)));
        toogleAddButton(nuevoPartAnchorRowCicloComb, btnNewCicloComb);

        //HIDRAULICO
        btnNewHidro.setOnAction(actionEvent -> openEditor("EditorHidraulico.fxml", "Editor Hidráulico", Text.HIDRAULICO_SIZE[0], Text.HIDRAULICO_SIZE[1], new EditorHidraulicoController(datosCorrida, listaHidraulicosController)));
        toogleAddButton(nuevoPartAnchorRowHidraulico, btnNewHidro);

        //CENTRAL ACUMULACION
        btnNewAcum.setOnAction(actionEvent -> openEditor("EditorCentralAcumulacion.fxml", "Editor Central Acumulación", Text.ACUMULADOR_SIZE[0], Text.ACUMULADOR_SIZE[1], new EditorCentralAcumulacionController(datosCorrida, listaCentralesAcumulacionController)));
        toogleAddButton(nuevoPartAnchorRowCentralAcumulacion, btnNewAcum);

        //DEMANDA
        btnNewDemanda.setOnAction(actionEvent -> openEditor("EditorDemanda.fxml", "Editor Demanda", Text.DEMANDA_SIZE [0], Text.DEMANDA_SIZE[1], new EditorDemandaController(datosCorrida, listaDemandasController)));
        toogleAddButton(nuevoPartAnchorRowDemanda, btnNewDemanda);

        //FALLA
        btnNewFalla.setOnAction(actionEvent -> openEditor("EditorFalla.fxml", "Editor Falla", Text.FALLA_SIZE[0], Text.FALLA_SIZE[1], new EditorFallaController(datosCorrida, listaFallasController)));
        toogleAddButton(nuevoPartAnchorRowFalla, btnNewFalla);

        //IMPO/EXPO
        btnNewImpoExpo.setOnAction(actionEvent -> openEditor("EditorImpoExpo.fxml", "Editor Impo/Expo", Text.IMPOEXPO_SIZE[0], Text.IMPOEXPO_SIZE[1], new EditorImpoExpoController(datosCorrida, listaImpoExpoController)));
        toogleAddButton(nuevoPartAnchorRowImpoExpo, btnNewImpoExpo);

        //RED COMBUSTIBLE
        btnNewRedComb.setOnAction(actionEvent -> openEditor("EditorCombustible.fxml", "Editor Combustible", 1337, 870, new EditorCombustibleController(datosCorrida, listaCombustiblesController)));
        toogleAddButton(nuevoPartAnchorRowCombustible, btnNewRedComb);

        //RED ELECTRICA
      //  btnNewRedElec.setOnAction(actionEvent -> openEditor("EditorRedElectrica.fxml", "Editor Red Eléctrica", 889, 860, new EditorRedElectricaController(datosCorrida, listaRedElectricaController)));
       // btnNewRedElec.setOnAction(actionEvent -> openEditor("EditorRedElectrica.fxml", "Editor Red Eléctrica", 889, 860, null));
        //toogleAddButton(nuevoPartAnchorRowRedElectrica, btnNewRedElec);
        btnNewRedElec.setDisable(true);

        //IMPACTOS
        btnNewImpacto.setOnAction(actionEvent -> openEditor("EditorImpacto.fxml", "Editor Impacto Ambiental", Text.IMPACTO_SIZE[0], Text.IMPACTO_SIZE[1], new EditorImpactoController(datosCorrida, listaImpactosController)));
        toogleAddButton(nuevoPartAnchorRowImpacto, btnNewImpacto);

        //CONTRATOS
        btnNewContrato.setOnAction(actionEvent -> openEditor("EditorContrato.fxml", "Editor Contrato Energía", Text.CONTRATO_ENERGIA_SIZE[0], Text.CONTRATO_ENERGIA_SIZE[1], new EditorContratoController(datosCorrida, listaContratosController)));
        toogleAddButton(nuevoPartAnchorRowContrato, btnNewContrato);


        // CREACION DE PARTICIPANTES DESDE EL PORTAPAPELES
        btnNewEolClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_EOLICO_TEXT);
                tipoParticipanteClipboard = Text.TIPO_EOLICO_TEXT;
                if(datosCorrida.getEolicos().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Eólico con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_EOLICO_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_EOLICO_TEXT);
                }
            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                    e.printStackTrace();
                    mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
                }
        });
        btnNewSolClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_SOLAR_TEXT);
                tipoParticipanteClipboard = Text.TIPO_SOLAR_TEXT;
                if(datosCorrida.getFotovoltaicos().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Solar con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_SOLAR_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_SOLAR_TEXT);
                           }
            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewTermClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_TERMICO_TEXT);
                tipoParticipanteClipboard = Text.TIPO_TERMICO_TEXT;
                if(datosCorrida.getTermicos().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Térmico con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_TERMICO_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_TERMICO_TEXT);
                }

            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewHidroClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_HIDRAULICO_TEXT);
                tipoParticipanteClipboard = Text.TIPO_HIDRAULICO_TEXT;
                if(datosCorrida.getHidraulicos().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Hidráulico con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_HIDRAULICO_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_HIDRAULICO_TEXT);
                }

                  } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewCicloCombClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_CICLO_COMBINADO_TEXT);
                tipoParticipanteClipboard = Text.TIPO_CICLO_COMBINADO_TEXT;
                if(datosCorrida.getCcombinados().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe Ciclo Combinado con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_CICLO_COMBINADO_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_CICLO_COMBINADO_TEXT);
                }

            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewAcumClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_ACUMULADOR_TEXT);
                tipoParticipanteClipboard = Text.TIPO_ACUMULADOR_TEXT;
                if(datosCorrida.getAcumuladores().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Acumulador con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_ACUMULADOR_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_ACUMULADOR_TEXT);
                }

             } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewDemandaClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_DEMANDA_TEXT);
                tipoParticipanteClipboard = Text.TIPO_DEMANDA_TEXT;
                if(datosCorrida.getDemandas().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe una Demanda con ese nombre ¿desea crearla y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_DEMANDA_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_DEMANDA_TEXT);
                }

              } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewFallaClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_FALLA_TEXT);
                tipoParticipanteClipboard = Text.TIPO_FALLA_TEXT;
                if(datosCorrida.getFallas().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe una Falla con ese nombre ¿desea crearla y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_FALLA_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_FALLA_TEXT);
                }

            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewImpoExpoClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_IMPOEXPO_TEXT);
                tipoParticipanteClipboard = Text.TIPO_IMPOEXPO_TEXT;
                if(datosCorrida.getImpoExpos().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe una ImpoExpo con ese nombre ¿desea crearla y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_IMPOEXPO_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_IMPOEXPO_TEXT);
                }


            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewRedCombClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_COMBUSTIBLE_TEXT);
                tipoParticipanteClipboard = Text.TIPO_COMBUSTIBLE_TEXT;
                if(datosCorrida.getCombustibles().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Combustible con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_COMBUSTIBLE_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_COMBUSTIBLE_TEXT);
                }


            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewRedElecClipboard.setOnAction(actionEvent -> {
            /*try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_RED);
                tipoParticipanteClipboard = Text.TIPO_RED_TEXT;
                if(datosCorrida.getRed().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Ya existe una Red con ese nombre ¿desea guardarla y corregir el nombre?", 2);
                } else {
                    String nombre = getParticipanteDelPortaPapeles("red");
                    DatosRedElectricaCorrida copiado = datosCorrida.getRed();
                    datosCorrida.getRedes().getRedes().put(nombre, copiado);
                    datosCorrida.getRed().getListaUtilizados().add(nombre);
                    datosCorrida.getRed().getOrdenCargaXML().add(nombre);
                    openEditor("EditorRedElectrica.fxml", "Editor Red Eléctrica", 889, 860, null);
                }


            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }*/
        });
        btnNewRedElecClipboard.setDisable(true);
        btnNewImpactoClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_IMPACTO_TEXT);
                tipoParticipanteClipboard = Text.TIPO_IMPACTO_TEXT;
                if(datosCorrida.getImpactos().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Impacto con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_IMPACTO_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_IMPACTO_TEXT);
                }

            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });
        btnNewContratoClipboard.setOnAction(actionEvent -> {
            try {
                nombreParticipanteClipboard = getNombreDelParticipanteDelPortaPapeles(Text.TIPO_CONTRATO_ENERGIA_TEXT);
                tipoParticipanteClipboard = Text.TIPO_CONTRATO_ENERGIA_TEXT;
                if(datosCorrida.getContratosEnergia().getOrdenCargaXML().contains(nombreParticipanteClipboard)){
                    mostrarMensajeConfirmacion("Existe un Contrato con ese nombre ¿desea crearlo y corregir nombre?", 2);
                } else {
                    getParticipanteDelPortaPapeles(Text.TIPO_CONTRATO_ENERGIA_TEXT, getDocumentoDelPortaPapeles());
                    agregarParticipanteCopiado(Text.TIPO_CONTRATO_ENERGIA_TEXT);
                }

            } catch (IOException | UnsupportedFlavorException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
                mostrarMensajeAviso("Error al leer el participante del portapapeles", this);
            }
        });



        /////////CHART_TEST/////////
        try {
            FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("ReportesContent.fxml"));
//            ReportesContentController contentController = new ReportesContentController(ph.devolverReporte(new DatosEspecificacionReporte(DatosEspecificacionReporte.REP_RES_ENER, true)));
            reportesContentController = new ReportesContentController();
            contentLoader.setController(reportesContentController);
            AnchorPane contentPane = contentLoader.load();
            reportesContentPane.getChildren().add(contentPane);

            FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("ReportesMenu.fxml"));
            ReportesMenuController menuController = new ReportesMenuController(reportesContentController);
            menuLoader.setController(menuController);
            AnchorPane menuPane = menuLoader.load();
            reportesMenuPane.getChildren().add(menuPane);

            FXMLLoader inputLoader = new FXMLLoader(getClass().getResource("ReportesInputPanel.fxml"));
//            ReportesInputPanelController inputPanelController = new ReportesInputPanelController(reportesContentController);
            reportesInputPanelController = new ReportesInputPanelController(reportesContentController);
            inputLoader.setController(reportesInputPanelController);
            AnchorPane inputPane = inputLoader.load();
            reportesInputPane.getChildren().add(inputPane);
        }catch (Exception e){
            e.printStackTrace();
        }
        /////////CHART_TEST/////////



        // LOGIN
        btnLogin.setOnAction(actionEvent -> {
            setLoginContent();
            outsideClick();
            loginVisible = !loginVisible;
            loginPane.setVisible(loginVisible);
        });
        inicializar();


    }







    public void setLoginContent(){
        String contentURL = user == null ? "LoginPage.fxml" : "UserMenu.fxml";
        loginPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(contentURL));
            loader.setController(user == null ? new LoginPageController(btnLogin) : new UserMenuController(btnLogin));
            loginPane.getChildren().add(loader.load());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }

    private void outsideClick(){
        btnLogin.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            if((evt.getPickResult().getIntersectedNode() != btnLogin) && (evt.getPickResult().getIntersectedNode() != loginPane) && !getAllNodes(loginPane).contains(evt.getPickResult().getIntersectedNode()) && loginVisible) {
                loginPane.setVisible(false);
                loginVisible = false;
            }
        });
    }

    private void inicializar(){

        ph = PresentacionHandler.getInstance();

        popularLineaDeTiempo();
        popularOptimizacionSimulacion();

    }

    private void cargarBiblioteca(){
        /////////BIBLIOTECA/////////
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Biblioteca.fxml"));
            bibliotecaController = new BibliotecaController(datosCorrida, this);
            loader.setController(bibliotecaController);
            bibliotecaPane.getChildren().add(loader.load());
        }catch (Exception e){
            e.printStackTrace();
        }
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> bibliotecaController.fixBlurryText());
        /////////BIBLIOTECA/////////
    }

    public static void guardarEnBiblioteca(JFXButton btnGuardarEnBiblioteca, String tipo, Object dataObject, DatosCorrida datosCorrida, Object controller, String nombre){
        try {
            FXMLLoader loader = new FXMLLoader(Manejador.class.getResource("GuardarEnBiblioteca.fxml"));
            loader.setController(new GuardarEnBibliotecaController( tipo, dataObject, datosCorrida, controller, nombre));
            AnchorPane newLoadedPane = loader.load();
            PopOver popOver = new PopOver(newLoadedPane);
            popOver.setTitle("Guardar En Biblioteca");
            popOver.show(btnGuardarEnBiblioteca);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void openEditor(String editor, String title, Integer width, Integer height, Object controller){

        try {
            if(datosCorrida.getNombre() == null ){
                mostrarMensajeAviso(Text.MSG_ERR_CREAR_PARTICIPANTE_SIN_CORRIDA,this);
            } else{
                //            Parent root1 = FXMLLoader.load(getClass().getResource(editor));
                FXMLLoader loader = new FXMLLoader(getClass().getResource(editor));
                loader.setController(controller);
                Parent root1 = loader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle(title);

                //ICONO
                String pp = System.getProperty("user.dir")+"\\resources\\logo.png";
                File file = new File(pp);
                Image image = new Image(file.toURI().toString());
                stage.getIcons().add(image);

                //Scroll
                Scene firstScene;
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                boolean aplicaScroll = screenSize.height<  1080 && screenSize.width < 1920;
                if(!aplicaScroll){
                    firstScene = new Scene(root1, width,height);
                }
                else{
                    ScrollPane sp = new ScrollPane();
                    sp.setContent(root1);
                    firstScene = new Scene(sp, width,height);
                }

                firstScene.getRoot().requestFocus();
                stage.setScene(firstScene);
                stage.show();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void toogleAddButton(AnchorPane anchor, JFXButton btn) {
/*        anchor.setOnMouseEntered(mouseEvent -> btn.setDisable(false));
        anchor.setOnMouseExited(mouseEvent -> btn.setDisable(true));
        btn.setOnMouseEntered(mouseEvent -> btn.setDisable(false));
        btn.setOnMouseExited(mouseEvent -> btn.setDisable(true));*/
    }

    private void hideVisible(boolean modo, boolean showHide){
        for(Node node: testGridPane.getChildren()){
            if(node instanceof JFXCheckBox) {
                if(modo) {
                    node.setDisable(showHide);
                }else{
                    node.setVisible(!showHide);
                }
            }
            if(node instanceof Label) {
                if(((Label) node).getText().equals("Visible?") || ((Label) node).getText().equals("Todos")){
                    if(modo) {
                        node.setDisable(showHide);
                    }else{
                        node.setVisible(!showHide);
                    }
                }
            }
        }
    }

    private void inicializarControllersParticipantes() {
        listaEolicosController = new ListaEolicosController(datosCorrida,this);
        listaSolaresController = new ListaSolaresController(datosCorrida,this);
        listaTermicosController = new ListaTermicosController(datosCorrida,this);
        listaCiclosCombinadosController = new ListaCiclosCombinadosController(datosCorrida,this);
        listaHidraulicosController = new ListaHidraulicosController(datosCorrida,this);
        listaCentralesAcumulacionController = new ListaCentralesAcumulacionController(datosCorrida,this);
        listaDemandasController = new ListaDemandasController(datosCorrida,this);
        listaFallasController = new ListaFallasController(datosCorrida,this);
        listaImpoExpoController = new ListaImpoExpoController(datosCorrida,this);
        listaCombustiblesController = new ListaCombustiblesController(datosCorrida,this);
        listaImpactosController = new ListaImpactosController(datosCorrida,this);
        listaContratosController = new ListaContratosController(datosCorrida,this);
        listaRedElectricaController = new ListaRedElectricaController(datosCorrida, this);

        popularParque();
    }

    private void popularParque(){
        int parqueActual = 0;

        if(datosCorrida.getEolicos().getEolicos().size() > 0){
            cargarTipoParticipante("ListaEolicos.fxml", listaEolicosController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getFotovoltaicos().getFotovoltaicos().size() > 0){
            cargarTipoParticipante("ListaSolares.fxml", listaSolaresController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getTermicos().getTermicos().size() > 0){
            cargarTipoParticipante("ListaTermicos.fxml", listaTermicosController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getCcombinados().getCcombinados().size() > 0){
            cargarTipoParticipante("ListaCiclosCombinados.fxml", listaCiclosCombinadosController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getHidraulicos().getHidraulicos().size() > 0){
            cargarTipoParticipante("ListaHidraulicos.fxml", listaHidraulicosController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getAcumuladores().getAcumuladores().size() > 0){
            cargarTipoParticipante("ListaCentralesAcumulacion.fxml", listaCentralesAcumulacionController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getDemandas().getDemandas().size() > 0){
            cargarTipoParticipante("ListaDemandas.fxml", listaDemandasController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getFallas().getFallas().size() > 0){
            cargarTipoParticipante("ListaFallas.fxml", listaFallasController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getImpoExpos().getImpoExpos().size() > 0){
            cargarTipoParticipante("ListaImpoExpo.fxml", listaImpoExpoController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getCombustibles().getCombustibles().size() > 0){
            cargarTipoParticipante("ListaCombustibles.fxml", listaCombustiblesController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getImpactos().getImpactos().size() > 0){
            cargarTipoParticipante("ListaImpactos.fxml", listaImpactosController, parqueActual);
            parqueActual++;
        }
        if(datosCorrida.getContratosEnergia().getContratosEnergia().size() > 0){
            cargarTipoParticipante("ListaContratos.fxml", listaContratosController, parqueActual);
            parqueActual++;
        }
        /*if(datosCorrida.getRed().getBarras().size() > 0){  ///ACA PREGUNTO POR LA LISTA DE BARRAS EN LUGAR DE LA LISTA DE REDES PORQUE LA RED ES UNICA, CONFIRMAR ESTO
            cargarTipoParticipante("ListaRedElectrica.fxml", listaRedElectricaController, parqueActual);
            parqueActual++;
        }*/

    }

    private void cargarTipoParticipante(String fxml, Object controller, int parqueActual){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setController(controller);
            TitledPane tipoParticipantes = loader.load();
            vBoxParques[parqueActual % 3].getChildren().add(tipoParticipantes);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void popularSalida(){


        //NUEVA LINEA DE TIEMPO
        try {
            salidaPane.getChildren().clear();
            salidaController = new SalidaController(this, datosCorrida);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Salida.fxml"));
            loader.setController(salidaController);
            AnchorPane newLoadedPane = loader.load();
            salidaPane.getChildren().add(newLoadedPane);
            salidaController.unloadData();

        }catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void refresh(){
        //parque
        vBoxParque1.getChildren().clear();
        vBoxParque2.getChildren().clear();
        vBoxParque3.getChildren().clear();
        popularParque();

        //salida
        salidaController.clean(); //clearListaParticipantesSalida();
        popularSalida();
    }

    /***
     *  Retorna  las Variables Aleatorias que tienen en comun los dos procesos estocasticos
     * @param PE1
     * @param PE2
     * @return
     */
    public static ArrayList<String> getListaNombresVA(DatosProcesoEstocastico PE1, DatosProcesoEstocastico PE2){
        ArrayList<String> res = new ArrayList<>();
        if(PE1 != null) {
            for (String va : PE1.getNombresVa()) {
                if (PE2 != null) {
                    if (PE2.getNombresVa().contains(va)) {
                        res.add(va);
                    }
                }
            }
        }
        return res;
    }
    public static ArrayList<String>  getListaNombresPE(DatosProcesoEstocastico PE1,  Hashtable<String, DatosProcesoEstocastico>  procesosEstocasticos ){
        ArrayList<String> listaPE = new ArrayList<>();
        Set<String> keys = procesosEstocasticos.keySet();
        for(String key: keys){
            DatosProcesoEstocastico PE2 = procesosEstocasticos.get(key);
            for(String va : PE2.getNombresVa()){
                if(PE1.getNombresVa().contains(va)){
                    listaPE.add(PE2.getNombre());
                    break;
                }
            }
        }

        return listaPE;
    }
    public static void popularListaNombresVA(DatosCorrida datosCorrida, JFXComboBox<String> inputVAProcSim, JFXComboBox<String> inputVAProcOpt, JFXComboBox<String> inputVANombre){
        inputVAProcSim.getItems().addAll(datosCorrida.getProcesosEstocasticos().keySet());
        inputVAProcOpt.getItems().addAll(datosCorrida.getProcesosEstocasticos().keySet());
        inputVAProcSim.setOnAction(actionEvent -> {
            if(inputVAProcSim.getValue() != null) {
                String procSim = inputVAProcSim.getValue();
                String procOpt = "";
                if (inputVAProcOpt.getValue() != null) {
                    procOpt = inputVAProcOpt.getValue();
                    inputVANombre.getItems().clear();
                    inputVANombre.getItems().addAll(getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(procSim), datosCorrida.getProcesosEstocasticos().get(procOpt)));
                }
                ArrayList<String> listaPE = getListaNombresPE(datosCorrida.getProcesosEstocasticos().get(procSim), datosCorrida.getProcesosEstocasticos());

                if (!(procOpt != "" && listaPE.contains(procOpt))) {
                    inputVAProcOpt.getItems().clear();
                    inputVAProcOpt.getItems().addAll(listaPE);
                }
            }
        });
        inputVAProcOpt.setOnAction(actionEvent -> {
            if(inputVAProcOpt.getValue() != null) {
                String procOpt = inputVAProcOpt.getValue();
                String procSim = "";
                if (inputVAProcSim.getValue() != null) {
                    procSim = inputVAProcSim.getValue();
                    inputVANombre.getItems().clear();
                    inputVANombre.getItems().addAll(getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(procSim), datosCorrida.getProcesosEstocasticos().get(procOpt)));
                }
                ArrayList<String> listaPE = getListaNombresPE(datosCorrida.getProcesosEstocasticos().get(procOpt), datosCorrida.getProcesosEstocasticos());

                if (!(procSim != "" && listaPE.contains(procSim))) {
                    inputVAProcSim.getItems().clear();
                    inputVAProcSim.getItems().addAll(listaPE);
                }
            }
        });
    }

    public ArrayList<String> controlProcesosEstocasticosNoSeUsan(){
        ArrayList<String> noUsados = new ArrayList<>();
        ArrayList<String> peUsados = controlProcesosEstocasticosEnUso();
        //Control de no usados
        for(DatosProcesoEstocastico procE : datosCorrida.getProcesosEstocasticos().values()){
            if(!peUsados.contains(procE.getNombre())){
                noUsados.add(procE.getNombre());
            }

        }
        return noUsados;
    }
    public ArrayList<String> controlProcesosEstocasticosEnUso(){
        ArrayList<String> peUsados = null;
        procesosEstocasticosEnuso.clear();
        String procesoEst = "";

        for (DatosEolicoCorrida eolico:  this.datosCorrida.getEolicos().getEolicos().values()){
            procesoEst = eolico.getFactor().getProcOptimizacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);
            }


            procesoEst = eolico.getFactor().getProcSimulacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);
            }
        }

        for (DatosFotovoltaicoCorrida solar:  this.datosCorrida.getFotovoltaicos().getFotovoltaicos().values()){
            procesoEst = solar.getFactor().getProcOptimizacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);

            }

            procesoEst = solar.getFactor().getProcSimulacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);

            }
        }

        for (DatosHidraulicoCorrida hidraulico:  this.datosCorrida.getHidraulicos().getHidraulicos().values()){
            procesoEst = hidraulico.getAporte().getProcOptimizacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);

            }

            procesoEst = hidraulico.getAporte().getProcSimulacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);

            }
        }

        for (DatosImpoExpoCorrida impoExpo:  this.datosCorrida.getImpoExpos().getImpoExpos().values()){

            if(impoExpo.getTipoImpoExpo().equals(Constantes.IEALEATPRPOT)){
                for (DatosVariableAleatoria va :impoExpo.getDatPrecio()) {
                    procesoEst = va.getProcSimulacion();
                    if(!procesosEstocasticosEnuso.contains(procesoEst)){
                        procesosEstocasticosEnuso.put(procesoEst, true);

                    }

                    procesoEst = va.getProcOptimizacion();
                    if(!procesosEstocasticosEnuso.contains(procesoEst)){
                        procesosEstocasticosEnuso.put(procesoEst, true);

                    }
                }

                for (DatosVariableAleatoria va :impoExpo.getDatPotencia()) {
                    procesoEst = va.getProcSimulacion();
                    if(!procesosEstocasticosEnuso.contains(procesoEst)){
                        procesosEstocasticosEnuso.put(procesoEst, true);

                    }

                    procesoEst = va.getProcOptimizacion();
                    if(!procesosEstocasticosEnuso.contains(procesoEst)){
                        procesosEstocasticosEnuso.put(procesoEst, true);

                    }
                }
            }
            if(impoExpo.getTipoImpoExpo().equals(Constantes.IEALEATFORMUL)){
                procesoEst = impoExpo.getDatCMg().getProcOptimizacion();
                if(!procesosEstocasticosEnuso.contains(procesoEst)){
                    procesosEstocasticosEnuso.put(procesoEst, true);

                }

                procesoEst = impoExpo.getDatCMg().getProcSimulacion();
                procesosEstocasticosEnuso.put(procesoEst, true);

            }

        }

        for (DatosDemandaCorrida demanda:  this.datosCorrida.getDemandas().getDemandas().values()){
            procesoEst = demanda.getPotActiva().getProcOptimizacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);

            }

            procesoEst = demanda.getPotActiva().getProcSimulacion();
            if(!procesosEstocasticosEnuso.contains(procesoEst)){
                procesosEstocasticosEnuso.put(procesoEst, true);

            }

            peUsados = new ArrayList<>(procesosEstocasticosEnuso.keySet());


        }



        return peUsados;
    }
    public void popularLineaDeTiempo(){

        try {
            nuevaLineaDeTiempoPane.getChildren().clear();
            lineaDeTiempoSimpleController = new LineaDeTiempoSimpleController(this, datosCorrida);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeTiempoSimple.fxml"));
            loader.setController(lineaDeTiempoSimpleController);
            AnchorPane newLoadedPane = loader.load();
            nuevaLineaDeTiempoPane.getChildren().add(newLoadedPane);
            lineaDeTiempoSimpleController.unloadData();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void popularLineaDeTiempoDetallada(){
        //LINEA DE TIEMPO DETALLADA
        try {
            lineaDeTiempoDetalladaController = new LineaDeTiempoDetalladaController(this, datosCorrida);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeTiempoDetallada.fxml"));
            loader.setController(lineaDeTiempoDetalladaController);
            AnchorPane newLoadedPane = loader.load();
            if(!lineaDeTiempoDetalladaControllerSugerenciaCorreccion.trim().equals("")){
                mostrarMensajeAviso(lineaDeTiempoDetalladaControllerSugerenciaCorreccion, this);
            }
            lineaDeTiempoDetalladaControllerSugerenciaCorreccion = "";
            nuevaLineaDeTiempoPane.getChildren().add(newLoadedPane);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    private void popularOptimizacionSimulacion(){
        //NUEVA LINEA DE TIEMPO
        try {
            optimizacionSimulacionPane.getChildren().clear();
            optimizacionSimulacionController = new OptimizacionSimulacionController(this, datosCorrida);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("OptimizacionSimulacion.fxml"));
            loader.setController(optimizacionSimulacionController);
            AnchorPane newLoadedPane = loader.load();
            optimizacionSimulacionPane.getChildren().add(newLoadedPane);
            optimizacionSimulacionController.unloadData();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void setNuevaLineaDeTiempoActiva(boolean value){
        nuevaLineaDeTiempoActiva = value;
        popularLineaDeTiempoDetallada();
    }
    private void loadData() {
        optimizacionSimulacionController.loadData();
        if(!nuevaLineaDeTiempoActiva) {
            lineaDeTiempoDetalladaController.loadData();
        }else {
            lineaDeTiempoSimpleController.loadData();
        }
       this.salidaController.loadData();

        //DATA PARTICIPATES
        if(datosCorrida.getEolicos().getEolicos().size() > 0){ listaEolicosController.loadData(); }
        if(datosCorrida.getFotovoltaicos().getFotovoltaicos().size() > 0){ listaSolaresController.loadData(); }
        if(datosCorrida.getTermicos().getTermicos().size() > 0){ listaTermicosController.loadData(); }
        if(datosCorrida.getCcombinados().getCcombinados().size() > 0){ listaCiclosCombinadosController.loadData(); }
        if(datosCorrida.getHidraulicos().getHidraulicos().size() > 0){ listaHidraulicosController.loadData(); }
        if(datosCorrida.getAcumuladores().getAcumuladores().size() > 0){ listaCentralesAcumulacionController.loadData(); }
        if(datosCorrida.getDemandas().getDemandas().size() > 0){ listaDemandasController.loadData(); }
        if(datosCorrida.getFallas().getFallas().size() > 0){ listaFallasController.loadData(); }
        if(datosCorrida.getImpoExpos().getImpoExpos().size() > 0){ listaImpoExpoController.loadData(); }
        if(datosCorrida.getCombustibles().getCombustibles().size() > 0){ listaCombustiblesController.loadData(); }
        if(datosCorrida.getImpactos().getImpactos().size() > 0){ listaImpactosController.loadData(); }
        if(datosCorrida.getContratosEnergia().getContratosEnergia().size() > 0){ listaContratosController.loadData(); }
        if(datosCorrida.getRed().getBarras().size() > 0) { listaRedElectricaController.loadData(); }

    }
    private void unloadData() {

        //evento al cerrar el mop
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.setOnHidden(event -> {
            salir();
        });

        System.out.println("UNLOAD DATA-CORRIDA");
        popularSalida();
        inicializarControllersParticipantes();
        popularLineaDeTiempo();
        cargaProcesosEstocasticosPaneController.popularPEs();
        cargarBiblioteca();
        optimizacionSimulacionController.setDatosCorrida(datosCorrida);
        optimizacionSimulacionController.unloadData();
    }
    private void nuevaCorrida(){
        System.out.println("NUEVA CORRIDA");
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CrearNuevaCorrida.fxml"));
            CrearNuevaCorridaController crearNuevaCorridaController = new CrearNuevaCorridaController(this);
            loader.setController(crearNuevaCorridaController);
            Parent root1 = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            BoxBlur bb = new BoxBlur();
            bb.setWidth(3);
            bb.setHeight(3);
            bb.setIterations(3);

            mainTabPane.setEffect(bb);

            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void clearBlur(){
        mainTabPane.setEffect(null);
    }
    public void crearNuevaCorrida(String nombre) {
        clean();
        datosCorrida = new DatosCorrida();
        datosCorrida.setNombre(nombre);
        clean();
        rutaXMLCarga = ".\\resources\\baseParaIniciarPruebasBASE.xml";
        datosCorrida = ph.cargarCorrida(rutaXMLCarga, false, false);
        datosCorrida.setNombre(nombre);
        setTitle();
        unloadData();
        mainTabPane.setEffect(null);

    }
    private void setTitle(){
        Stage stage = (Stage)mainTabPane.getScene().getWindow();
        stage.setTitle(" MOP" + Constantes.VERSION_ET + " - " + savePath);

    }
    private void cargarCorrida(){
        System.out.println("CARGAR CORRIDA");
        FileChooser fileChooser = new FileChooser();
    	String  ruta_xml = null;
		LectorPropiedades lprop = new LectorPropiedades(".\\resources\\mop.conf");
		try {
			ruta_xml= lprop.getProp("rutaEntradas");					
		} catch (IOException e) {
			e.printStackTrace();
		}
        fileChooser.setInitialDirectory(new File(ruta_xml));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(mainTabPane.getScene().getWindow());
        if(selectedFile != null) {
            clean();

            Stage stage = new Stage();

            Task<Void> cargaTask = new Task<>() {
                @Override
                public Void call() {
                    updateProgress(0,1);
                    rutaXMLCarga = selectedFile.getAbsolutePath();
                    savePath = rutaXMLCarga;
                    datosCorrida = ph.cargarCorrida(rutaXMLCarga, false, false);
                    return null;
                }
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource("CargandoCorrida.fxml"));
            CargandoCorridaController cargandoCorridaController = new CargandoCorridaController(this, cargaTask);
            loader.setController(cargandoCorridaController);
            Parent root1 = null;
            try {
                root1 = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            BoxBlur bb = new BoxBlur();
            bb.setWidth(3);
            bb.setHeight(3);
            bb.setIterations(3);

            mainTabPane.setEffect(bb);

            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();

            cargaTask.setOnSucceeded(event -> {
                setTitle();

                if(datosCorrida == null){
                    mostrarMensajeAviso("No se pudo cargar la corrida. Revisar errores en la consola.", this);
                    stage.close();
                    mainTabPane.setEffect(null);
                }
                else{
                    lineaDeEntradaController.popularListaDatosMaquinaLT(datosCorrida);
                    lineaDeEntradaController.render(LineaDeEntradaController.ESCALA_INCIAL, datosCorrida);
                    if(!datosCorrida.getInicioCorrida().equalsIgnoreCase(datosCorrida.getLineaTiempo().getTiempoInicial())){
                        mostrarMensajeAviso("El inicio de la corrida no coincide con el inicio de la linea de tiempo", this);
                    }
                    unloadData();
                    stage.close();
                    mainTabPane.setEffect(null);
                    cargaProcesosEstocasticosPaneController.cargarCarpetasDisponibles();
                    mostrarMensajeAviso("La corrida se cargó correctamente", this);
                }


            });

            cargaTask.setOnCancelled(event -> {
               clean();
            });

            Thread thread = new Thread(cargaTask);
            thread.setDaemon(true);
            thread.start();



        }
    }
    /***
     * Retorna true si la informacion en datosCorrida esta completa
     * @return
     */
    public ArrayList<String> controlDatosCompletos(){
       ArrayList<String> errores = new ArrayList<>();
        if(datosCorrida.getNombre()!= null) {
            errores = datosCorrida.getLineaTiempo().controlDatosCompletos();

            if(errores.size() == 0){
                loadData();
            }
            errores.addAll(datosCorrida.controlDatosCompletos());
        }
        return errores;
    }
    private void guardarCorrida(boolean guardarComo, boolean muestraMsjeConf){
        if(datosCorrida.getNombre()!= null) {
             ArrayList<String> erroresLineaDeTiempo = datosCorrida.getLineaTiempo().controlDatosCompletos();

            if(erroresLineaDeTiempo.size() == 0){
                loadData();
            }
            ArrayList errores = datosCorrida.controlDatosCompletos();
            errores.addAll(erroresLineaDeTiempo);
            if (errores.size() == 0) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("."));
                if (savePath.equalsIgnoreCase("") || guardarComo) {
                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
                    File selectedFile = fileChooser.showSaveDialog(mainTabPane.getScene().getWindow());
                    savePath = selectedFile.getAbsolutePath();
                }
                EscritorXML eml = new EscritorXML(getLineaTiempo(datosCorrida));

                if(eml.guardarCorrida(datosCorrida, savePath)){
                    if(muestraMsjeConf) { mostrarMensajeAviso("La corrida se guardó correctamente", this); }
                    setTitle();
                }else{
                    mostrarMensajeAviso("No se pudo guardar la corrida", this);
                }
            } else {
                //Se muestra mensaje de que hay datos incompletos o erroneos y no se deja guardar la corrida
                mostrarMensajeDatosIncompletos(errores, "No se puede guardar la corrida. Hay datos incompletos o erroneos");

            }
        } else {
            mostrarMensajeAviso("No existe una corrida para guardar", this);
        }
    }
    private void salir(){
        if(datosCorrida.getNombre()!=null){
            mostrarMensajeConfirmacion(Text.MSG_CONF_GUARDAR_AL_SALIR, 1);}
        else {
            System.exit(0);
        }
    }
    public void mostrarMensajeDatosIncompletos(ArrayList errores, String textoMsje) {

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MostrarMensaje.fxml"));

            MostrarMensajeController ventanaMsje = new MostrarMensajeController(textoMsje , this, 0);

            loader.setController(ventanaMsje);
            Parent root1 = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            BoxBlur bb = new BoxBlur();
            bb.setWidth(3);
            bb.setHeight(3);
            bb.setIterations(3);

            this.getMainPane().setEffect(bb);

            if(errores != null){
                String direc = datosCorrida.getRutaSals() + "/listaErrores.txt";
                File file = new File(direc);

                DirectoriosYArchivos.grabaTexto(direc, UtilStrings.arrayStringAtexto(errores, "\n"));
                ventanaMsje.agregarHiperlink("Ver errores", file);
            }


            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void linkManuales(){
        try {
            Desktop.getDesktop().open(new File( "resources/ManualMOP.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void setVisibleParticipate(String participante, Boolean visible){
        lineaDeEntradaController.setVisibleParticipate(participante, visible);
    }
    private void clean(){
        //clear parques
        vBoxParque1.getChildren().clear();
        vBoxParque2.getChildren().clear();
        vBoxParque3.getChildren().clear();

        cargaProcesosEstocasticosPaneController.clean();

        if(nuevaLineaDeTiempoActiva){
            if(lineaDeTiempoSimpleController != null) {
                lineaDeTiempoSimpleController.clean();
            }
        }else{
            if(lineaDeTiempoDetalladaController != null) {
                lineaDeTiempoDetalladaController.clear();
            }
        }

        if(salidaController != null){  salidaController.clean();  }

        optimizacionSimulacionController.clean();
        lineaDeEntradaController.clean();

    }
    public static LineaTiempo getLineaTiempo(DatosCorrida datosCorrida){//TODO: Probar
        Date tIni = Date.from((LocalDateTime.parse(datosCorrida.getInicioCorrida(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"))).atZone( ZoneId.systemDefault()).toInstant());
        Date tFin = Date.from((LocalDateTime.parse(datosCorrida.getFinCorrida(), DateTimeFormatter.ofPattern("dd MM yyyy HH:mm:ss"))).atZone( ZoneId.systemDefault()).toInstant());
        GregorianCalendar calIni = new GregorianCalendar();
        GregorianCalendar calFin = new GregorianCalendar();
        calIni.setTime(tIni);
        calFin.setTime(tFin);
        return new LineaTiempo(datosCorrida.getLineaTiempo(), calIni, calFin);
    }
    public boolean puedoGenerarNuevaLineaDeTiempo(){
        boolean ret = lineaDeTiempoSimpleController.generarDatosLT(datosCorrida.getLineaTiempo());
        nuevaLineaDeTiempoActiva = ret;
        return ret;
    }
    public boolean generateXML(String path) throws ParseException {
        loadData();
        EscritorXML eml = new EscritorXML(getLineaTiempo(datosCorrida));
        boolean guardo = eml.guardarCorrida(datosCorrida, path);
        return guardo;

    }
    public String getNombreDelParticipanteDelPortaPapeles(String tipo) throws IOException, UnsupportedFlavorException, ParserConfigurationException, SAXException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        String result = (String) clipboard.getData(DataFlavor.stringFlavor);
        Element node =  DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(result.getBytes()))
                .getDocumentElement();

        return  node.getElementsByTagName("nombre").item(0).getTextContent();

    }
    public void getParticipanteDelPortaPapelesCambiaNombre(String tipo) throws IOException, UnsupportedFlavorException, ParserConfigurationException, SAXException {
        //Creo un datosCorridaLocal para que agregue en forma provisoria el participante que luego se agregara a la corrida
        //De lo contrario sobre escribiria el que ya existe y se perderian datos del que ya esta en la corrida con el mismo nombre
        DatosCorrida datosCorridaLocal = new DatosCorrida();
        datosCorridaLocal.setLineaTiempo(datosCorrida.getLineaTiempo());

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        String result = (String) clipboard.getData(DataFlavor.stringFlavor);

        Element node =  DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(result.getBytes()))
                .getDocumentElement();

        CargadorXML cargador = new CargadorXML();

        NodeList nodoNombre = node.getElementsByTagName("nombre");

        // Verificar si se encontró algún elemento con el nombre especificado
        if (nodoNombre.getLength() > 0) {
            Element elemento = (Element) nodoNombre.item(0);
            String nom = elemento.getTextContent();
            elemento.setTextContent(nom + "copiadoportapapeles");
            nombreParticipanteClipboard = nom + "copiadoportapapeles";
            getParticipanteDelPortaPapeles(tipoParticipanteClipboard, node);
            agregarParticipanteCopiado(tipoParticipanteClipboard);

        } else {
            System.out.println("No se encontró ningún elemento con el nombre: " + "nodo");
        }

    }
    private void agregarParticipanteCopiado(String tipo) {
        if(tipo.equalsIgnoreCase(Text.TIPO_EOLICO_TEXT)) {
            DatosEolicoCorrida eo = datosCorrida.getEolicos().getEolicos().get(nombreParticipanteClipboard);
            datosCorrida.getEolicos().getEolicos().put(nombreParticipanteClipboard, eo);
            datosCorrida.getEolicos().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getEolicos().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorEolico.fxml", "EditorEolico - " + nombreParticipanteClipboard, Text.EOLICO_SIZE[0],  Text.EOLICO_SIZE[1], new EditorEolicoController(datosCorrida, eo,listaEolicosController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_SOLAR_TEXT)){
            DatosFotovoltaicoCorrida fv = datosCorrida.getFotovoltaicos().getFotovoltaicos().get(nombreParticipanteClipboard);
            datosCorrida.getFotovoltaicos().getFotovoltaicos().put(nombreParticipanteClipboard, fv);
            datosCorrida.getFotovoltaicos().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getFotovoltaicos().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorSolar.fxml", "EditorSolar - " + nombreParticipanteClipboard, Text.SOLAR_SIZE[0], Text.SOLAR_SIZE[1], new EditorSolarController(datosCorrida, fv,listaSolaresController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_TERMICO_TEXT)){
            DatosTermicoCorrida ter = datosCorrida.getTermicos().getTermicos().get(nombreParticipanteClipboard);
            datosCorrida.getTermicos().getTermicos().put(nombreParticipanteClipboard, ter);
            datosCorrida.getTermicos().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getTermicos().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorTermico.fxml", "EditorTermico - " + nombreParticipanteClipboard, Text.TERMICO_SIZE[0], Text.TERMICO_SIZE[1], new EditorTermicoController(datosCorrida, ter,listaTermicosController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_CICLO_COMBINADO_TEXT)){
            DatosCicloCombinadoCorrida cc = datosCorrida.getCcombinados().getCcombinados().get(nombreParticipanteClipboard);
            datosCorrida.getCcombinados().getCcombinados().put(nombreParticipanteClipboard, cc);
            datosCorrida.getCcombinados().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getCcombinados().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorCicloCombinado.fxml", "Editor Ciclo Combinado - " + nombreParticipanteClipboard, Text.CICLO_COMBINADO_SIZE[0], Text.CICLO_COMBINADO_SIZE[1], new EditorCicloCombinadoController(datosCorrida, cc, listaCiclosCombinadosController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_HIDRAULICO_TEXT)){
            DatosHidraulicoCorrida hid = datosCorrida.getHidraulicos().getHidraulicos().get(nombreParticipanteClipboard);
            datosCorrida.getHidraulicos().getHidraulicos().put(nombreParticipanteClipboard, hid);
            datosCorrida.getHidraulicos().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getHidraulicos().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorHidraulico.fxml","EditorHidraulico - " + nombreParticipanteClipboard, Text.HIDRAULICO_SIZE[0], Text.HIDRAULICO_SIZE[1], new EditorHidraulicoController(datosCorrida, hid,listaHidraulicosController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_ACUMULADOR_TEXT)){
            DatosAcumuladorCorrida acu = datosCorrida.getAcumuladores().getAcumuladores().get(nombreParticipanteClipboard);
            datosCorrida.getAcumuladores().getAcumuladores().put(nombreParticipanteClipboard, acu);
            datosCorrida.getAcumuladores().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getAcumuladores().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorCentralAcumulacion.fxml", "EditorCentralAcumulacion - " + nombreParticipanteClipboard, Text.ACUMULADOR_SIZE[0], Text.ACUMULADOR_SIZE[1],new EditorCentralAcumulacionController(datosCorrida, acu,listaCentralesAcumulacionController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_DEMANDA_TEXT)){
            DatosDemandaCorrida dem = datosCorrida.getDemandas().getDemandas().get(nombreParticipanteClipboard);
            datosCorrida.getDemandas().getDemandas().put(nombreParticipanteClipboard, dem);
            datosCorrida.getDemandas().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getDemandas().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorDemanda.fxml", "EditorDemanda - " + nombreParticipanteClipboard, Text.DEMANDA_SIZE[0], Text.DEMANDA_SIZE[1], new EditorDemandaController(datosCorrida, dem,listaDemandasController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_FALLA_TEXT)){
            DatosFallaEscalonadaCorrida falla = datosCorrida.getFallas().getFallas().get(nombreParticipanteClipboard);
            datosCorrida.getFallas().getFallas().put(nombreParticipanteClipboard, falla);
            datosCorrida.getFallas().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getFallas().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorFalla.fxml", "EditorFalla - " + nombreParticipanteClipboard, Text.FALLA_SIZE[0], Text.FALLA_SIZE[1],  new EditorFallaController(datosCorrida, falla,listaFallasController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_IMPOEXPO_TEXT)){
            DatosImpoExpoCorrida impoe = datosCorrida.getImpoExpos().getImpoExpos().get(nombreParticipanteClipboard);
            datosCorrida.getImpoExpos().getImpoExpos().put(nombreParticipanteClipboard, impoe);
            datosCorrida.getImpoExpos().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getImpoExpos().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorImpoExpo.fxml", "EditorImpoExpo - " + nombreParticipanteClipboard, Text.IMPOEXPO_SIZE[0], Text.IMPOEXPO_SIZE[1], new EditorImpoExpoController(datosCorrida, impoe,listaImpoExpoController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_COMBUSTIBLE_TEXT)){
            DatosCombustibleCorrida comb = datosCorrida.getCombustibles().getCombustibles().get(nombreParticipanteClipboard);
            datosCorrida.getCombustibles().getCombustibles().put(nombreParticipanteClipboard, comb);
            datosCorrida.getCombustibles().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getCombustibles().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorCombustible.fxml", "EditorCombustible - " + nombreParticipanteClipboard, Text.COMBUSTIBLE_SIZE[0], Text.COMBUSTIBLE_SIZE[1], new EditorCombustibleController(datosCorrida, comb,listaCombustiblesController, true));

        }else if(tipo.equalsIgnoreCase("red")){
//            new CargadorXML().cargarCombustible(datosCorrida, node, datosCorrida.getLineaTiempo());

        }else if(tipo.equalsIgnoreCase(Text.TIPO_IMPACTO_TEXT)){
            DatosImpactoCorrida imp = datosCorrida.getImpactos().getImpactos().get(nombreParticipanteClipboard);
            datosCorrida.getImpactos().getImpactos().put(nombreParticipanteClipboard, imp);
            datosCorrida.getImpactos().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getImpactos().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorImpacto.fxml", "EditorImpacto - " + nombreParticipanteClipboard, Text.IMPACTO_SIZE[0], Text.IMPACTO_SIZE[1], new EditorImpactoController(datosCorrida, imp,listaImpactosController, true));

        }else if(tipo.equalsIgnoreCase(Text.TIPO_CONTRATO_ENERGIA_TEXT)){
            DatosContratoEnergiaCorrida cont = datosCorrida.getContratosEnergia().getContratosEnergia().get(nombreParticipanteClipboard);
            datosCorrida.getContratosEnergia().getContratosEnergia().put(nombreParticipanteClipboard, cont);
            datosCorrida.getContratosEnergia().getListaUtilizados().add(nombreParticipanteClipboard);
            datosCorrida.getContratosEnergia().getOrdenCargaXML().add(nombreParticipanteClipboard);
            openEditor("EditorContrato.fxml", "EditorContrato - " + nombreParticipanteClipboard, Text.CONTRATO_ENERGIA_SIZE[0], Text.CONTRATO_ENERGIA_SIZE[1], new EditorContratoController(datosCorrida, cont,listaContratosController, true));

        }
        refresh();
    }
    private  Element getDocumentoDelPortaPapeles() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        Element node = null;
        try{
            String result = (String) clipboard.getData(DataFlavor.stringFlavor);
//        System.out.println("String from Clipboard: " + result);

            String nombre = "";
             node =  DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(result.getBytes()))
                    .getDocumentElement();
        }catch (Exception e){
            e.printStackTrace();
        }

        return node;
    }
    public String getParticipanteDelPortaPapeles(String tipo, Element node) throws IOException, UnsupportedFlavorException, ParserConfigurationException, SAXException {
        String nombre = "";

        if(tipo.equalsIgnoreCase(Text.TIPO_EOLICO_TEXT)) {
            nombre = new CargadorXML().cargarEolico(datosCorrida, node, true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_SOLAR_TEXT)){
            nombre = new CargadorXML().cargarFotovoltaico(datosCorrida, node, true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_TERMICO_TEXT)){
            nombre = new CargadorXML().cargarTermico(datosCorrida, node, true, true).getNombre();
        }else if(tipo.equalsIgnoreCase(Text.TIPO_CICLO_COMBINADO_TEXT)){
            nombre = new CargadorXML().cargarCicloCombinado(datosCorrida, node, true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_HIDRAULICO_TEXT)){
            nombre = new CargadorXML().cargarHidraulico(datosCorrida, node, datosCorrida.getLineaTiempo(), true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_ACUMULADOR_TEXT)){
            nombre = new CargadorXML().cargarAcumulador(datosCorrida, node, datosCorrida.getLineaTiempo(), true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_DEMANDA_TEXT)){
            nombre = new CargadorXML().cargarDemanda(datosCorrida, node, true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_FALLA_TEXT)){
            nombre = new CargadorXML().cargarFalla(datosCorrida, node, true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_IMPOEXPO_TEXT)){
            nombre = new CargadorXML().cargarImpoExpo(datosCorrida, node, datosCorrida.getLineaTiempo(), true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_COMBUSTIBLE_TEXT)){
            nombre = new CargadorXML().cargarCombustible(datosCorrida, node, datosCorrida.getLineaTiempo(), true);
        }else if(tipo.equalsIgnoreCase("red")){
//            new CargadorXML().cargarCombustible(datosCorrida, node, datosCorrida.getLineaTiempo());
        }else if(tipo.equalsIgnoreCase(Text.TIPO_IMPACTO_TEXT)){
            nombre = new CargadorXML().cargarImpacto(datosCorrida, node, true);
        }else if(tipo.equalsIgnoreCase(Text.TIPO_CONTRATO_ENERGIA_TEXT)){
            nombre = new CargadorXML().cargarContratoEnergia(datosCorrida, node, true);
        }
        refresh();
        return nombre;
    }
    public JFXTabPane getMainPane(){
        return this.mainTabPane;
    }
    public void actualizarRenderizarReportes(){
        //reportesContentController.actualizar(ph.devolverReporte(new DatosEspecificacionReporte(DatosEspecificacionReporte.REP_RES_ENER, true)));
        //reportesInputPanelController.render(datosCorrida);

    }

    /***
     * Carga archivo que contiene los textos para los tooltips
     * @return
     */
    private Properties loadToolTipsProperties() {
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream("resources/Tooltips.properties")) {
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("No se pudo cargar archivo Tooltips.propeties");
        }
        return prop;
    }

    public void asignarTooltips(Object controlador) {

        Class<?> clase = controlador.getClass();
        String nombreControlador = clase.getName();
        String prefijo = "";
        if (nombreControlador.endsWith("Controller")) {
            prefijo = nombreControlador.substring(0, nombreControlador.length() - 10);
        }

        String[] partes = prefijo.split("\\.");

        // Comprobar si la cadena contiene un punto y obtener la parte final
        if (partes.length > 1) {
            prefijo = partes[partes.length - 1];
        }


/*        if (prefijo.startsWith("interfaz.")) {
            prefijo = prefijo.substring(9);
        }*/

        for (java.lang.reflect.Field campo : clase.getDeclaredFields()) {
            if (campo.isAnnotationPresent(FXML.class)) {
                campo.setAccessible(true);
                try {
                    Object valor = campo.get(controlador);
                    if (valor instanceof Node) {
                        Node v = (Node)valor;
                        String fxId = v.getId();
                        agregarTooltips(v, prefijo + "." + fxId);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void agregarTooltips(Node componente, String fxId) {
        if (fxId != null) {
            String tooltipText =  toolTips.getProperty(fxId);
            if (tooltipText != null) {
                Font fuenteTooltip = new Font(14.0);
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setFont(fuenteTooltip);
                if(componente.getClass().getName().equals(JFXButton.class.getName())){
                    ((JFXButton) componente).setTooltip(tooltip);
                } else if(componente.getClass().getName().equals(Label.class.getName())){
                    ((Label) componente).setTooltip(tooltip);
                }

            }
        }
    }
    public void actualizarLineaEntrada() {

        lineaDeEntradaController.popularListaDatosMaquinaLT(datosCorrida);
        lineaDeEntradaController.render(LineaDeEntradaController.ESCALA_INCIAL, datosCorrida);
    }
    @Override
    public void aceptoMensaje(int codigoMensaje) {
        if(codigoMensaje == 1){
            System.exit(0);
        }
    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje)  {

        if(codigoMensaje == 1){
            //Confirma guarda corrida antes de salir
            if(confirmacion){
                guardarCorrida(true, false);
                if (CorridaHandler.getInstance().isParalelo()){
                 //   PizarronRedis pp = new PizarronRedis();
                    //  pp.matarServidores();
                }
                mostrarMensajeAviso("La corrida se guardó correctamente", this, 1);
            }else{
                System.exit(0);
            }
        }else if(codigoMensaje == 2){
            //Confirma guarda crear desde el clipboard con nombre repetido
            if(confirmacion){
                DatosCorrida nombre = null;
                try {
                    getParticipanteDelPortaPapelesCambiaNombre(tipoParticipanteClipboard);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedFlavorException e) {
                    throw new RuntimeException(e);
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public Properties getToolTips() {  return toolTips; }
    public void setToolTips(Properties toolTips) { this.toolTips = toolTips; }
    public Hashtable<String, Boolean> getProcesosEstocasticosEnuso() {     return procesosEstocasticosEnuso;   }

    public void setProcesosEstocasticosEnuso(Hashtable<String, Boolean> procesosEstocasticosEnuso) {     this.procesosEstocasticosEnuso = procesosEstocasticosEnuso; }

    public void borrarProcesoEstocastico(String nombrePE) {
        this.datosCorrida.getProcesosEstocasticos().remove(nombrePE);
    }

    public class ProcesoEstocastico {
        public String nombre;
        public String tipo;

        public ProcesoEstocastico(String nombre, String tipo){
            this.nombre = nombre;
            this.tipo = tipo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTipo() {
            return tipo;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }
}
