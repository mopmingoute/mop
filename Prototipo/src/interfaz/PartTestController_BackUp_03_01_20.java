/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PartTestController_BackUp_03_01_20 is part of MOP.
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartTestController_BackUp_03_01_20 {
    //Scroll pane test
    @FXML private ScrollPane scrollPaneParque;
    @FXML private VBox vBoxParque;
    @FXML private HBox hBoxParque;
    @FXML private AnchorPane anchorPaneParque;
    @FXML private SplitPane splitPaneParqueVista;
    @FXML private SplitPane splitPaneBibliotecaParque;
    @FXML private JFXButton closeVista;
    @FXML private AnchorPane editorPane;

    @FXML private JFXButton part3LeftRightButton;
    @FXML private JFXButton part3UpDownButtonLeft;
    @FXML private JFXButton part3UpDownButtonRight;
    @FXML private SplitPane part3SplitPaneLeftRight;
    @FXML private SplitPane part3SplitPaneUpDown;
    @FXML private Label leftRightButtonLabel;
    @FXML private AnchorPane leftPane;
    @FXML private AnchorPane topPane;
    @FXML private AnchorPane bottomPane;
    @FXML private AnchorPane middlePane;
    @FXML private VBox vBoxBloques;
    @FXML private GridPane gridPaneBloque;
    @FXML private AnchorPane anchorPaneBloque;
    @FXML private JFXTextField textFieldCantPostes;
    @FXML private VBox vBoxParque1;
    @FXML private VBox vBoxParque2;
    @FXML private VBox vBoxParque3;
    @FXML private GridPane testGridPane;
    @FXML private AnchorPane testAnchorPane;
    @FXML private JFXCheckBox testCheckBoxVisible;
    @FXML private Button testDrawerButton;
    @FXML private JFXDrawer testDrawer;
    @FXML private JFXListView testListView;
    @FXML private TableView tableViewPE;
//    @FXML private JFXTreeTableView<ProcesoEstocastico> tableViewPEJFX;
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton comporDefectoDoneBtn;
    @FXML private AnchorPane testCompoPorDefecto;

    @FXML private JFXButton btnNewEol;
    @FXML private JFXButton btnNewSol;
    @FXML private JFXButton btnNewTerm;
    @FXML private JFXButton btnNewHidro;
    @FXML private JFXButton btnNewAcum;
    @FXML private JFXButton btnNewDemanda;
    @FXML private JFXButton btnNewFalla;
    @FXML private JFXButton btnNewImpoExpo;
    @FXML private JFXButton btnNewRedComb;
    @FXML private JFXButton btnNewRedElec;

    @FXML private AnchorPane nuevoPartAnchorRow00;
    @FXML private AnchorPane nuevoPartAnchorRow01;
    @FXML private AnchorPane nuevoPartAnchorRow02;
    @FXML private AnchorPane nuevoPartAnchorRow03;
    @FXML private AnchorPane nuevoPartAnchorRow04;
    @FXML private AnchorPane nuevoPartAnchorRow05;
    @FXML private AnchorPane nuevoPartAnchorRow06;
    @FXML private AnchorPane nuevoPartAnchorRow07;
    @FXML private AnchorPane nuevoPartAnchorRow08;
    @FXML private AnchorPane nuevoPartAnchorRow09;


    private boolean leftRightOpened = true;
    private boolean topMiddleOpened = true;
    private boolean bottomMiddleOpened = true;

    private boolean updating = false;

    private Integer cantPostes = 0;
    private Map<Integer, JFXTextField> durPostes = new HashMap<Integer, JFXTextField>();
    @FXML private JFXButton nuevoBloqueButton;
    private List<TitledPane> bloques = new ArrayList<>();


    //DATA TYPE TEST
    public DatosCorrida datosCorrida = new DatosCorrida();


    @FXML
    public void initialize() {
        //scroll test
//        scrollPaneParque.prefViewportHeightProperty().bind(Bindings.min(vBoxParque.heightProperty(), Bindings.max(Bindings.subtract(anchorPaneParque.heightProperty(), 60), 16)));
//        scrollPaneParque.prefViewportHeightProperty().bind(Bindings.min(hBoxParque.heightProperty(), Bindings.max(Bindings.subtract(anchorPaneParque.heightProperty(), 60), 16)));
//        hBoxParque.prefHeightProperty().bind(Bindings.min());
//        splitPaneParqueVista.setDividerPosition();
//        try {
//            AnchorPane newLoadedPane =  FXMLLoader.load(getClass().getResource("editor.fxml"));
//            editorPane.getChildren().add(newLoadedPane);
////            newLoadedPane..autosize();
//        }catch(Exception e) {
//            e.printStackTrace();
//        }

        closeVista.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                System.out.println(splitPaneParqueVista.getDividerPositions()[0]);
                System.out.println(splitPaneBibliotecaParque.getDividerPositions()[0]);
                if(splitPaneParqueVista.getDividerPositions()[0] > 0.11){
                    splitPaneParqueVista.setDividerPosition(0, .05);
                    splitPaneBibliotecaParque.setDividerPosition(0, .05);
                }else{
                    splitPaneParqueVista.setDividerPosition(0, .7);
                    splitPaneBibliotecaParque.setDividerPosition(0, 0.1564);
                }
            }
        });


        leftPane = (AnchorPane) part3SplitPaneLeftRight.getItems().get(0);

        part3LeftRightButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
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
            }
        });

        topPane = (AnchorPane) part3SplitPaneUpDown.getItems().get(0);

        part3UpDownButtonLeft.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                middlePane.setM
                if(topMiddleOpened) {
                    if(!bottomMiddleOpened) {
                        part3SplitPaneUpDown.getItems().add(2, bottomPane);
                        bottomMiddleOpened = true;
                        part3UpDownButtonRight.setText("▲");
                    }
                    part3SplitPaneUpDown.getItems().remove(topPane);
                    part3UpDownButtonLeft.setText("---");
                }else{
                    part3SplitPaneUpDown.setDividerPosition(0, .495);
                    part3SplitPaneUpDown.setDividerPosition(1, .495);

                    part3SplitPaneUpDown.getItems().add(0, topPane);
                    part3UpDownButtonLeft.setText("▲");
                }
                topMiddleOpened = !topMiddleOpened;
            }
        });

        bottomPane = (AnchorPane) part3SplitPaneUpDown.getItems().get(2);

        part3UpDownButtonRight.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(bottomMiddleOpened) {
                    part3SplitPaneUpDown.getItems().remove(bottomPane);
                    part3UpDownButtonRight.setText("---");
                    if(!topMiddleOpened) {
                        part3SplitPaneUpDown.getItems().add(0, topPane);
                        part3UpDownButtonLeft.setText("▲");
                        topMiddleOpened = true;
                    }
                    hideVisible(testCheckBoxVisible.isSelected(), true);
                }else{
                    part3SplitPaneUpDown.setDividerPosition(0, .495);
                    part3SplitPaneUpDown.setDividerPosition(1, .495);
                    part3SplitPaneUpDown.getItems().add(2, bottomPane);
                    part3UpDownButtonRight.setText("▲");
                    hideVisible(testCheckBoxVisible.isSelected(), false);
                }
                bottomMiddleOpened = !bottomMiddleOpened;
            }
        });

        //divider synch poc
//        part3SplitPaneUpDown.getDividers().get(0).positionProperty().addListener((obs, oldPos, newPos) -> {
//            if(updating) return;
////            if(newPos.doubleValue() > .7) {
////                part3SplitPaneUpDown.getDividers().get(0).setPosition(.7);
////                part3SplitPaneUpDown.getDividers().get(1).setPosition(1);
////                return;
////            }
//            updating = true;
//            middlePane.setMaxHeight(Region.USE_COMPUTED_SIZE);
//            part3SplitPaneUpDown.getDividers().get(1).setPosition(newPos.doubleValue() /*+ .015*/);
//            middlePane.setMaxHeight(40);
//            updating = false;
//        });
//
//        part3SplitPaneUpDown.getDividers().get(1).positionProperty().addListener((obs, oldPos, newPos) -> {
//            if(updating) return;
////            if(newPos.doubleValue() > .7) {
////                part3SplitPaneUpDown.getDividers().get(0).setPosition(.7);
////                part3SplitPaneUpDown.getDividers().get(1).setPosition(1);
////                return;
////            }
//            updating = true;
//            middlePane.setMaxHeight(Region.USE_COMPUTED_SIZE);
//            part3SplitPaneUpDown.getDividers().get(0).setPosition(newPos.doubleValue() /*- .015*/);
//            middlePane.setMaxHeight(40);
//            updating = false;
//        });

//        bottomPane.setMaxHeight(920);
//        topPane.setMaxHeight(920);

//        AtomicBoolean topUpdate = new AtomicBoolean(false);
//        AtomicBoolean bottomUpdate = new AtomicBoolean(false);
//
//        bottomPane.heightProperty().addListener((obs, oldVal, newVal) -> {
//            System.out.println("--BOTTOM--");
//            System.out.println("oldValue: " + oldVal);
//            System.out.println("newValue: " + newVal);
//            System.out.println("bottom_height: " + bottomPane.getHeight());
//            System.out.println("top_height: " + topPane.getHeight());
//            System.out.println("new_top_height: " + (topPane.getHeight() + ((Double) oldVal-(Double) newVal)));
//            System.out.println("====================================");
//            if(!topUpdate.get()) {
//                topUpdate.set(true);
//                topPane.setMinHeight(topPane.getHeight() + ((Double) oldVal - (Double) newVal));
//                topUpdate.set(false);
//            }
////            topPane.setMinHeight(921-(Double)newVal);
//        });
//        topPane.heightProperty().addListener((obs, oldVal, newVal) -> {
//            System.out.println("--TOP--");
//            System.out.println("oldValue: " + oldVal);
//            System.out.println("newValue: " + newVal);
//            System.out.println("top_height: " + topPane.getHeight());
//            System.out.println("bottom_height: " + bottomPane.getHeight());
//            System.out.println("new_bottom_height: " + (bottomPane.getHeight() + ((Double) oldVal-(Double) newVal)));
//            System.out.println("====================================");
//            if(!topUpdate.get()) {
//                topUpdate.set(true);
//                bottomPane.setMinHeight(bottomPane.getHeight() + ((Double) oldVal - (Double) newVal));
//                topUpdate.set(false);
//            }
////            bottomPane.setMinHeight(921-(Double)newVal);
//        });
//        TitledPane tp = new TitledPane();
//        tp.setContent(new AnchorPane());
//        tp.setText("asd");
//        vBoxBloques.getChildren().add(tp);

        //Dynamic Postes
//        textFieldCantPostes.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                Integer nuevaCantPostes = Integer.valueOf(textFieldCantPostes.getText());
//                double cantRows = Math.ceil(nuevaCantPostes / 7.0);
//                System.out.println("cantRows: "+nuevaCantPostes / 7);
//                System.out.println("CANT_ROWS=>"+cantRows);
//                anchorPaneBloque.setPrefHeight(288+40*(cantRows-1));
//                for(int i=cantPostes;i<nuevaCantPostes;i++){
////                    if(i >= cantPostes) {
//                        int col = (i % 7) + 1;
//                        int row = (i / 7) + 7;
////                        System.out.println("col:" + ((i % 7) + 1) + " fila:" + (7 + i / 7));
//                        System.out.println("col:" + col + " fila:" + row);
//                        JFXTextField newField = new JFXTextField();
//                        newField.setMaxWidth(65);
//                        newField.setPrefWidth(65);
//                        durPostes.put(10*col+row,newField);
////                        gridPaneBloque.add(newField, (i % 7) + 1, (i / 7) + 7);
//                        gridPaneBloque.add(newField, col, row);
//                        GridPane.setMargin(newField, new Insets(10, 0, 0, 10));
////                    }
//                }
////                if(nuevaCantPostes < cantPostes) {
//                    for(int i=cantPostes;i>=nuevaCantPostes;i--){
//                        int col = (i % 7) + 1;
//                        int row = (i / 7) + 7;
//                        gridPaneBloque.getChildren().remove(durPostes.get(10*col+row));
//                    }
////                }
//                cantPostes = nuevaCantPostes;
//            }
//        });

        addBloque();

        nuevoBloqueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
//                try {
////                    TitledPane newLoadedPane =  FXMLLoader.load(getClass().getResource("BloqueLineaDeTiempo.fxml"));
//
//                    BloqueLineaDeTiempoController bloqueController = new BloqueLineaDeTiempoController(vBoxBloques.getChildren().size() > 0);
//
//                    FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueLineaDeTiempo.fxml"));
//                    loader.setController(bloqueController);
//
////                    Pane mainPane = (Pane) loader.load();
//                    TitledPane newLoadedPane = loader.load();
//
//                    newLoadedPane.setText("Bloque " + (vBoxBloques.getChildrenUnmodifiable().size()+1));
//
//                    vBoxBloques.getChildren().add(newLoadedPane);
////                    bloques.add(newLoadedPane);
//                }catch(Exception e) {
//                    e.printStackTrace();
//                }
                addBloque();
            }
        });

        for(Node node: testGridPane.getChildren()){
            if(node instanceof JFXButton) {
//                node.setOnMouseEntered(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent mouseEvent) {
//                        node.setDisable(false);
//                    }
//                });
//                node.setOnMouseExited(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent mouseEvent) {
//                        node.setDisable(true);
//                    }
//                });
            }
        }

        testAnchorPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                testAnchorPane.getChildren().get(0).setDisable(false);
            }
        });

        testAnchorPane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                testAnchorPane.getChildren().get(0).setDisable(true);
            }
        });

        for(Node node: testGridPane.getChildren()){
            if(node instanceof AnchorPane) {
                node.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        ((AnchorPane)node).getChildren().get(0).setDisable(false);
                    }
                });

                node.setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        ((AnchorPane)node).getChildren().get(0).setDisable(true);
                    }
                });
            }
        }


        testCheckBoxVisible.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(testCheckBoxVisible.isSelected()){

                }
            }
        });

//        JFXBadge badge = new JFXBadge(testCheckBoxVisible);
//        badge.
//        GlyphF
//        FontAwesome.Glyph.ARROW_LEFT;

        //TEST DRAWER
        try {
            AnchorPane drawerContent = FXMLLoader.load(getClass().getResource("drawer.fxml"));
            testDrawerButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if(testDrawer.isOpened()) {
                        testDrawer.close();
                    }else {
                        testDrawer.open();
                    }
                }
            });
            testDrawer.setSidePane(drawerContent);
        }catch (Exception e){
            e.printStackTrace();
        }

        //TEST LIST VIEW
        testListView.getItems().add("Eolo_1");
        testListView.getItems().add("Eolo_2");
        testListView.getItems().add("Eolo_3");
        testListView.getItems().add("Solar_1");
        testListView.getItems().add("Solar_2");
        testListView.getItems().add("Hidro_1");
        testListView.getItems().add("Hidro_2");
        testListView.getItems().add("Hidro_3");
        testListView.getItems().add("Hidro_4");
        testListView.getItems().add("Térmico_1");
        testListView.getItems().add("Térmico_2");

        testListView.setCellFactory(param -> new Cell());


        //PE table view
        TableColumn<String,ProcesoEstocastico> column1 = new TableColumn<>("Nombre");
        column1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        column1.setPrefWidth(299);

        TableColumn<String, ProcesoEstocastico> column2 = new TableColumn<>("Tipo");
        column2.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        column2.setPrefWidth(129);


        tableViewPE.getColumns().add(column1);
        tableViewPE.getColumns().add(column2);

        tableViewPE.getItems().add(new ProcesoEstocastico("Histórico Aportes","Hisórico"));
        tableViewPE.getItems().add(new ProcesoEstocastico("Markov Semanal","Markov"));
        tableViewPE.getItems().add(new ProcesoEstocastico("Demanda Poste Semana","Por Escenarios"));
        tableViewPE.getItems().add(new ProcesoEstocastico("Energía Eolo Sol Poste Semana","Por Escenarios"));


        //JFX
//        JFXTreeTableColumn<ProcesoEstocastico, String> nombreCol = new JFXTreeTableColumn<>("Nombre");
//        nombreCol.setPrefWidth(299);
//        nombreCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ProcesoEstocastico, String>, ObservableValue<String>>() {
//            @Override
//            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ProcesoEstocastico, String> procesoEstocasticoStringCellDataFeatures) {
//                return procesoEstocasticoStringCellDataFeatures.getValue().getValue().nombre;
//            }
//        });
//        JFXTreeTableColumn<ProcesoEstocastico, String> tipoCol = new JFXTreeTableColumn<>("Tipo");
//        tipoCol.setPrefWidth(129);
//        tipoCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<ProcesoEstocastico, String>, ObservableValue<String>>() {
//            @Override
//            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ProcesoEstocastico, String> procesoEstocasticoStringCellDataFeatures) {
//                return procesoEstocasticoStringCellDataFeatures.getValue().getValue().tipo;
//            }
//        });
//
//        ObservableList<ProcesoEstocastico> pes = FXCollections.observableArrayList();
//        pes.add(new ProcesoEstocastico("Histórico Aportes","Hisórico"));
//        pes.add(new ProcesoEstocastico("Markov Semanal","Markov"));
//        pes.add(new ProcesoEstocastico("Demanda Poste Semana","Por Escenarios"));
//        pes.add(new ProcesoEstocastico("Energía Eolo Sol Poste Semana","Por Escenarios"));
////        tableViewPEJFX..getItems().add(new ProcesoEstocastico("Histórico Aportes","Hisórico"));
////        tableViewPEJFX.getItems().add(new ProcesoEstocastico("Markov Semanal","Markov"));
////        tableViewPEJFX.getItems().add(new ProcesoEstocastico("Demanda Poste Semana","Por Escenarios"));
////        tableViewPEJFX.getItems().add(new ProcesoEstocastico("Energía Eolo Sol Poste Semana","Por Escenarios"));
//
//        final TreeItem<ProcesoEstocastico> root = new RecursiveTreeItem<ProcesoEstocastico>(pes, RecursiveTreeObject::getChildren);
//        tableViewPEJFX.setRoot(root);
//        tableViewPEJFX.setShowRoot(false);
//        tableViewPEJFX.getColumns().setAll(nombreCol,tipoCol);

        //TEST COMP POR DEFECTO
        compPorDefectoBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                testCompoPorDefecto.setVisible(true);
            }
        });
        comporDefectoDoneBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                testCompoPorDefecto.setVisible(false);
            }
        });



        //NUEVO PARTICIPANTE

        //EOLICO
        btnNewEol.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorEolico.fxml", "Editor Eólico", 850, 820);
            }
        });
        toogleAddButton(nuevoPartAnchorRow00, btnNewEol);

        //SOLAR
        btnNewSol.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorSolar.fxml", "Editor Solar", 850, 820);
            }
        });
        toogleAddButton(nuevoPartAnchorRow01, btnNewSol);

        //TÉRMICO
        btnNewTerm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorTermico.fxml", "Editor Térmico", 856, 920);
            }
        });
        toogleAddButton(nuevoPartAnchorRow02, btnNewTerm);

        //HIDRÁULICO
        btnNewHidro.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorHidraulico.fxml", "Editor Hidráulico", 1840, 934);
            }
        });
        toogleAddButton(nuevoPartAnchorRow03, btnNewHidro);

        //CENTRAL ACUMULACIÓN
        btnNewAcum.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorCentralAcumulacion.fxml", "Editor Central Acumulación", 862, 1015);
            }
        });
        toogleAddButton(nuevoPartAnchorRow04, btnNewAcum);

        //DEMANDA
        btnNewDemanda.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorDemanda.fxml", "Editor Demanda", 850, 378);
            }
        });
        toogleAddButton(nuevoPartAnchorRow05, btnNewDemanda);

        //FALLA
        btnNewFalla.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorFalla.fxml", "Editor Falla", 889, 930);
            }
        });
        toogleAddButton(nuevoPartAnchorRow06, btnNewFalla);

        //IMPO/EXPO
        btnNewImpoExpo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorImpoExpo.fxml", "Editor Impo/Expo", 1768, 711);
            }
        });
        toogleAddButton(nuevoPartAnchorRow07, btnNewImpoExpo);

        //RED COMBUSTIBLE
        btnNewRedComb.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorCombustible.fxml", "Editor Red Combustible", 1337, 870);
            }
        });
        toogleAddButton(nuevoPartAnchorRow08, btnNewRedComb);

        //RED ELÉCTRICA
        btnNewRedElec.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openEditor("EditorRedElectrica.fxml", "Editor Red Eléctrica", 889, 1100);
            }
        });
        toogleAddButton(nuevoPartAnchorRow09, btnNewRedElec);







    }

    private void openEditor(String editor, String title, Integer width, Integer height){
        try {
            Parent root1 = FXMLLoader.load(getClass().getResource(editor));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            Scene firstScene = new Scene(root1, width,height);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void toogleAddButton(AnchorPane anchor, JFXButton btn) {
        anchor.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                btn.setDisable(false);
            }
        });
        anchor.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                btn.setDisable(true);
            }
        });
        btn.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                btn.setDisable(false);
            }
        });
        btn.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                btn.setDisable(true);
            }
        });
    }


    private void addBloque(){
        try {
            BloqueLineaDeTiempoController bloqueController = new BloqueLineaDeTiempoController(null, vBoxBloques.getChildren().size() > 0, vBoxBloques.getChildrenUnmodifiable().size()+1);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BloqueLineaDeTiempo.fxml"));
            loader.setController(bloqueController);
            TitledPane newLoadedPane = loader.load();
            newLoadedPane.setText("Bloque " + (vBoxBloques.getChildrenUnmodifiable().size()+1));
            vBoxBloques.getChildren().add(newLoadedPane);
        }catch(Exception e) {
            e.printStackTrace();
        }
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

    static class Cell extends ListCell<String>{
        HBox hbox = new HBox();
        Pane pane = new Pane();
        Label nombre = new Label("");
        JFXCheckBox detalleCheckBox = new JFXCheckBox();
        GridPane gridPane = new GridPane();

        public Cell(){
            super();
//            hbox.setMinHeight(40);
//            nombre.setStyle("-fx-font-size: 15px");
//            nombre.setMinHeight(40);
//            pane.setMinHeight(40);
//            detalleCheckBox.setMinHeight(40);
//            detalleCheckBox.setAlignment(Pos.CENTER_RIGHT);
//            hbox.getChildren().addAll(nombre,pane,detalleCheckBox);
////            hbox.getChildren().addAll(nombre,pane,detalleCheckBox);
//
////            gridPane.add(detalleCheckBox,3,0);
//            gridPane.setVgap(40);
//            gridPane.add(new Label("ASD"),0,0);
//            gridPane.add(nombre,1,0);
//            gridPane.add(detalleCheckBox,2,0);
////            gridPane.setAlignment(Pos.CENTER_RIGHT);
//            gridPane.setMinWidth(615);
//            gridPane.setPrefWidth(615);
//            gridPane.setGridLinesVisible(true);
////            System.out.println(gridPane.getWidth());
//            GridPane.setHalignment(detalleCheckBox, HPos.RIGHT);
////            GridPane.get
        }

        public void updateItem(String name, boolean empty) {
            super.updateItem(name,empty);
            setText(null);
            setGraphic(null);
            if(name != null && !empty){
                try{
                    int tipo;
                    if(name.contains("Eol")){
                        tipo = 0;
                    }else if(name.contains("Sol")){
                        tipo = 1;
                    }else if(name.contains("Hidro")){
                        tipo = 2;
                    }else{
                        tipo = 3;
                    }
                    ItemController itemController = new ItemController(name,tipo);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("item.fxml"));
                    loader.setController(itemController);
                    AnchorPane newItem = loader.load();
//                    nombre.setText(name);
//                setGraphic(hbox);
                    setGraphic(newItem);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    @FXML
    private void openEditor() {

        try {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("newWindow.fxml"));
//            Parent root1 = (Parent) fxmlLoader.load();
//            Parent root1 = FXMLLoader.load(getClass().getResource("newWindow.fxml"));
            Parent root1 = FXMLLoader.load(getClass().getResource("EditorTermico.fxml"));
//            Parent root1 = FXMLLoader.load(getClass().getResource("EditorTermico_2.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Editor Térmico");
            Scene firstScene = new Scene(root1, 850,920);
//            //Editor 2
//            Scene firstScene = new Scene(root1, 1200,760);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void openEditorHidro() {

        try {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("newWindow.fxml"));
//            Parent root1 = (Parent) fxmlLoader.load();
            Parent root1 = FXMLLoader.load(getClass().getResource("EditorHidraulico.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Editor Hidráulico");
            Scene firstScene = new Scene(root1, 1700,890);
//            //Editor 2
//            Scene firstScene = new Scene(root1, 1200,760);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    public class ProcesoEstocastico extends RecursiveTreeObject<ProcesoEstocastico> {
//        public StringProperty nombre;
//        public StringProperty tipo;
//

//        public ProcesoEstocastico(String nombre, String tipo){
//            this.nombre = new SimpleStringProperty(nombre);
//            this.tipo = new SimpleStringProperty(tipo);
//        }
//
//        public StringProperty getNombre() {
//            return nombre;
//        }
//
//        public StringProperty getTipo() {
//            return tipo;
//        }
//
//        public void setNombre(StringProperty nombre) {
//            this.nombre = nombre;
//        }
//
//        public void setTipo(StringProperty tipo) {
//            this.tipo = tipo;
//        }
//    }

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
