

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Main is part of MOP.
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

package interfazEstimadoresPE;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utilitarios.Constantes;

import java.io.File;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        String pp = System.getProperty("user.dir")+"\\resources\\logo.png";

//        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("EstimadorPE.fxml")));
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("EstimadorBase.fxml")));
        primaryStage.setTitle(" MOP" + Constantes.VERSION_ET);

        System.out.println(pp);
        File file = new File(pp);
        Image image = new Image(file.toURI().toString());        		
        primaryStage.getIcons().add(image);

        Scene rootScene = new Scene(root, 820, 130);
        rootScene.getRoot().requestFocus();
        primaryStage.setScene(rootScene);
        primaryStage.show();
    }


    public static void main(String[] args) {  
        launch(args);
    }
}
