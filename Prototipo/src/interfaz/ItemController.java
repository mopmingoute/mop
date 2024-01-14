/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ItemController is part of MOP.
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
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ItemController {

    @FXML private Label itemLabel;
    @FXML private JFXCheckBox itemCheckBox;
    @FXML private JFXButton itemButton;

    private String nombre;
    private int tipo;

    public ItemController(String nombre, int tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    @FXML
    public void initialize() {
        itemLabel.setText(nombre);
        switch (tipo){
            case 0:
                itemButton.setText("E");
                itemButton.setStyle("-fx-background-color:  #38a5e0");
                break;
            case 1:
                itemButton.setText("S");
                itemButton.setStyle("-fx-background-color:  #e87c2a");
                break;
            case 2:
                itemButton.setText("H");
                itemButton.setStyle("-fx-background-color:  #27468d");
                break;
            case 3:
                itemButton.setText("T");
                itemButton.setStyle("-fx-background-color:  #c91717");
                break;
        }
    }
}
