/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EstimadorBaseController is part of MOP.
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
import com.jfoenix.controls.JFXComboBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;


public class EstimadorBaseController {
    @FXML private VBox vBox;
    @FXML private VBox vBox2;
    @FXML private JFXComboBox<String> inputTipo;

    @FXML private JFXButton infoTipoProceso;

    private Manejador parentController;

    public EstimadorBaseController(Manejador parentController){
        this.parentController = parentController;
    }
    @FXML
    public void initialize(){
        parentController.asignarTooltips(this);
        inputTipo.getItems().addAll(Text.TIPOS_ESTIMADORES);
        inputTipo.setOnAction(actionEvent -> loadEstimador(inputTipo.getValue()));
    }

    private void loadEstimador(String estimador){
        String estimadorFxml = null;
        Object controller = null;
        switch(estimador) {
            case Text.TIPO_ESTIMADOR_MARKOV:
                estimadorFxml = "EstimadorPE.fxml";
                controller = new EstimadorPEController(parentController);
                break;
            case Text.TIPO_ESTIMADOR_VAR_EN_V_NORM:
                estimadorFxml = "EstimadorVARenVNorm.fxml";
                controller = new EstimadorVARenVNormController(parentController);
                break;
            case Text.TIPO_ESTIMADOR_BOOSTRAP_DISCRETO:
                estimadorFxml = "EstimadorBootstrap.fxml";
                controller = new EstimadorBoostrapController(parentController);
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(estimadorFxml));
            loader.setController(controller);
            AnchorPane estimadorPane = loader.load();
            vBox2.getChildren().clear();
            vBox2.getChildren().add(estimadorPane);
            vBox2.getScene().getWindow().setHeight(vBox.getScene().getWindow().getHeight() + ((GridPane)estimadorPane.getChildren().get(0)).getRowConstraints().stream().map(RowConstraints::getMinHeight).reduce(Double::sum).get());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
