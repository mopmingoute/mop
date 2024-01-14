/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BibliotecaController is part of MOP.
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
import com.jfoenix.controls.JFXToggleButton;
import database.MongoDBHandler;
import datatypes.DatosCorrida;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BibliotecaController extends GeneralidadesController {

    @FXML private GridPane gridPane;
    @FXML private GridPane gridPaneBibliotecas;
    @FXML private ScrollPane scrollPaneBibliotecas;
    @FXML private ScrollPane scrollPaneParticipantes;
    @FXML private JFXButton refreshBtn;



    private ArrayList<HashMap<String, String>> listParticipantes = new ArrayList<>();
    private ArrayList<String> listaBibliotecas = new ArrayList<>();
    private HashMap<String,Boolean> bibliotecasActivas = new HashMap<>();
    private HashMap<String,JFXToggleButton> bibliotecasActivasToggles = new HashMap<>();

    private DatosCorrida datosCorrida;
    private Manejador manejador;


    public BibliotecaController(DatosCorrida datosCorrida, Manejador manejador) throws NoSuchAlgorithmException {
        this.datosCorrida = datosCorrida;
        this.manejador = manejador;
    }

    @FXML
    public void initialize(){
        listaBibliotecas = MongoDBHandler.getInstance().getBibliotecas(Manejador.user);
        listaBibliotecas.forEach(b -> bibliotecasActivas.put(b,true));
        listParticipantes = MongoDBHandler.getInstance().getParticipantes(listaBibliotecas);
        popularBibliotecas();
        popularListaParticipantes();

        refreshBtn.setOnAction(action -> refresh());
//        fixBlurryText();
    }

    private void popularListaParticipantes() {
        int row = 1;
        for(HashMap<String,String> participanteMap : listParticipantes) {
            gridPane.getRowConstraints().add(new RowConstraints(40));
            Label nombre = new Label(participanteMap.get("nombre"));
            nombre.styleProperty().set("-fx-font-size: 15px;");
            gridPane.add(nombre, 0, row);
            Label tipo = new Label(participanteMap.get("tipo"));
            tipo.styleProperty().set("-fx-font-size: 15px;");
            gridPane.add(tipo, 1, row);
            Label biblioteca = new Label(participanteMap.get("biblioteca"));
            biblioteca.styleProperty().set("-fx-font-size: 15px;");
            gridPane.add(biblioteca, 2, row);
            JFXButton addParticipante = new JFXButton("Agregar Participante");
            addParticipante.setMinHeight(30);
            addParticipante.styleProperty().setValue("-fx-background-color:  #27864d; -fx-text-fill: WHITE; -fx-font-weight: bold; -fx-font-size: 15px;");
            Tooltip ttAddParticipante = new Tooltip(Text.TT_BIBLIOTECA_ADD_PARTICIPANTE);
            ttAddParticipante.setFont(new Font(12.0));
            addParticipante.setTooltip(ttAddParticipante);
            addParticipante.setOnAction(action -> {
                try {
                    switch (participanteMap.get("tipo")) {
                        case Text.TIPO_EOLICO_TEXT:
                            datosCorrida.getEolicos().getEolicos().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getEolico(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_SOLAR_TEXT:
                            datosCorrida.getFotovoltaicos().getFotovoltaicos().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getFotovoltaico(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_TERMICO_TEXT:
                            datosCorrida.getTermicos().getTermicos().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getTermico(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_HIDRAULICO_TEXT:
                            datosCorrida.getHidraulicos().getHidraulicos().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getHidraulico(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_ACUMULADOR_TEXT:
                            datosCorrida.getAcumuladores().getAcumuladores().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getAcumulador(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_IMPOEXPO_TEXT:
                            datosCorrida.getImpoExpos().getImpoExpos().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getImpoExpo(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_DEMANDA_TEXT:
                            datosCorrida.getDemandas().getDemandas().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getDemanda(participanteMap.get("nombre"), participanteMap.get("biblioteca")));
                            break;
                        case Text.TIPO_FALLA_TEXT:
                            datosCorrida.getFallas().getFallas().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getFalla(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_COMBUSTIBLE_TEXT:
                            datosCorrida.getCombustibles().getCombustibles().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getCombustible(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_IMPACTO_TEXT:
                            datosCorrida.getImpactos().getImpactos().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getImpactoAmbiental(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                        case Text.TIPO_CONTRATO_ENERGIA_TEXT:
                            datosCorrida.getContratosEnergia().getContratosEnergia().put(participanteMap.get("nombre"), MongoDBHandler.getInstance().getContratoEnergia(participanteMap.get("nombre"), participanteMap.get("biblioteca"), datosCorrida));
                            break;
                    }
                    manejador.refresh();
                    setLabelMessageTemporal( Text.MSG_CONF_AGREGAR_PARTICIIPANTE_CORRIDA, TipoInfo.FEEDBACK);
                }catch (Exception e){
                    setLabelMessageTemporal(Text.MSG_ERR_AGREGAR_PARTICIIPANTE_CORRIDA , TipoInfo.ERROR);
                    e.printStackTrace();

                }
            });
            gridPane.add(addParticipante, 3, row);
            row++;
        }
    }

    private void popularBibliotecas(){
        int row = 0;
        for(String biblioteca : listaBibliotecas){
            gridPaneBibliotecas.getRowConstraints().add(new RowConstraints(40));
            Label nombre = new Label(biblioteca);
            nombre.styleProperty().set("-fx-font-size: 15px; -fx-font-weight: bold;");
            gridPaneBibliotecas.add(nombre, 0, row);
            GridPane.setMargin(nombre, new Insets(0,0,0,35));
            JFXToggleButton toggleButton = new JFXToggleButton();
            toggleButton.setToggleColor(Paint.valueOf("27864d"));
            toggleButton.setToggleLineColor(Paint.valueOf("b4dabd"));
            toggleButton.setSelected(bibliotecasActivas.get(biblioteca));
            toggleButton.setOnAction(evt -> {
                bibliotecasActivas.put(biblioteca, !bibliotecasActivas.get(biblioteca));
                refreshParticipantes();
            });
            bibliotecasActivasToggles.put(biblioteca, toggleButton);
            toggleButton.setOnMouseClicked(evt -> {
                if(evt.getButton() == MouseButton.SECONDARY){
                    boolean applyOthers = !toggleButton.isSelected();
                    bibliotecasActivasToggles.keySet().stream().filter(Predicate.not(Predicate.isEqual(biblioteca))).forEach(k -> {
                        bibliotecasActivasToggles.get(k).setSelected(applyOthers);
                        bibliotecasActivas.put(k,applyOthers);
                    });
                    refreshParticipantes();
                }
            });
            gridPaneBibliotecas.add(toggleButton, 1, row);
            row++;
        }
    }

    private void refresh() {
        refreshBibliotecas();
        refreshParticipantes();
//        fixBlurryText();
    }

    private void refreshBibliotecas(){
        gridPaneBibliotecas.getChildren().clear();
        gridPaneBibliotecas.getRowConstraints().clear();
        listaBibliotecas = MongoDBHandler.getInstance().getBibliotecas(Manejador.user);
        listaBibliotecas.forEach(b -> bibliotecasActivas.putIfAbsent(b, true));
        bibliotecasActivas.keySet().removeIf(Predicate.not(listaBibliotecas::contains));
        popularBibliotecas();
//        refreshParticipantes();
    }

    private void refreshParticipantes(){
        gridPane.getChildren().clear();
        gridPane.getRowConstraints().clear();
        listParticipantes = MongoDBHandler.getInstance().getParticipantes((ArrayList<String>) bibliotecasActivas.keySet().stream().filter(val -> bibliotecasActivas.get(val)).collect(Collectors.toList()));
        popularListaParticipantes();
    }

    public void fixBlurryText() {
        StackPane stackPaneB = (StackPane) scrollPaneBibliotecas.lookup("ScrollPane .viewport");
        stackPaneB.setCache(false);
        StackPane stackPaneP = (StackPane) scrollPaneParticipantes.lookup("ScrollPane .viewport");
        stackPaneP.setCache(false);
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }
}
