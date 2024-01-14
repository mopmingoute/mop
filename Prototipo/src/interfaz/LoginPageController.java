/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LoginPageController is part of MOP.
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
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import database.MongoDBHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class LoginPageController {

    @FXML private JFXTextField inputUsuario;
    @FXML private JFXPasswordField inputPassword;
    @FXML private JFXButton btnLogin;
    @FXML private JFXButton btnCancelar;
    @FXML private Label labelErrorMsg;

    JFXButton parentBtn;

    public LoginPageController(JFXButton parentBtn){
        this.parentBtn = parentBtn;
    }

    @FXML
    public void initialize() {
        inputUsuario.setOnKeyReleased(actionEvent -> labelErrorMsg.setVisible(false));
        btnLogin.setOnAction(actionEvent -> {
            try {
                login();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
//        btnCancelar.setOnAction(actionEvent -> parentBtn.fire());
    }

    public void login() throws NoSuchAlgorithmException {
        String user = inputUsuario.getText();
        String pass = inputPassword.getText();
        if(!MongoDBHandler.getInstance().authUser(user, pass)){
            labelErrorMsg.setVisible(true);
            inputUsuario.clear();
            inputPassword.clear();
        }else{
            Manejador.user = user;
            parentBtn.setText(user);
            parentBtn.fire();
        }
    }

}
