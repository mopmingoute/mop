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

package interfaz;

import java.awt.*;
import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utilitarios.Constantes;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        String pp = System.getProperty("user.dir")+"\\resources\\logo.png";
//    	 //creating the image object
//        InputStream stream = new FileInputStream(pp);
//        Image imageBig = new Image(stream);
//        //Creating the image view
//        ImageView imageView = new ImageView();
//        //Setting image to the image view
//        imageView.setImage(imageBig);
//        Image img = imageView.getImage();
//        if (img != null) {
//            double w = 0;
//            double h = 0;
//
//            double ratioX = imageView.getFitWidth() / img.getWidth();
//            double ratioY = imageView.getFitHeight() / img.getHeight();
//
//            double reducCoeff = 0;
//            if(ratioX >= ratioY) {
//                reducCoeff = ratioY;
//            } else {
//                reducCoeff = ratioX;
//            }
//
//            w = img.getWidth() * reducCoeff;
//            h = img.getHeight() * reducCoeff;
//
//            imageView.setX((imageView.getFitWidth() - w) / 2);
//            imageView.setY((imageView.getFitHeight() - h) / 2);
//
//        }
//        
//        
//        //Setting the Scene object
//        Group root2 = new Group(imageView);
//        Scene scene = new Scene(root2, imageView.getFitWidth(), imageView.getFitHeight());
//
//        primaryStage.setScene(scene);
//        primaryStage.show();
    //	Thread.sleep(3000);
    	
        Parent root = FXMLLoader.load(getClass().getResource("Manejador.fxml"));
        primaryStage.setTitle(" MOP" + Constantes.VERSION_ET);
   
        
//        System.out.println(pp);
        File file = new File(pp);
        Image image = new Image(file.toURI().toString());        		
        primaryStage.getIcons().add(image);
//        Scene rootScene = new Scene(root, 1000, 800);


        Scene rootScene;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //Se consulta la resolucion de pantalla si es menor a la necesaria se abre con scrollpane
        //if(screenSize.height >= 1080 && screenSize.width >= 1920 ){
        //    rootScene = new Scene(root, 1920, 1080);
        // }else {

            ScrollPane sp = new ScrollPane();
            sp.setContent(root);
        rootScene = new Scene(sp, 1920, 1080);

        // }

        rootScene.getRoot().requestFocus();
        primaryStage.setMaximized(true);
        primaryStage.setScene(rootScene);
        primaryStage.show();
    }


    public static void main(String[] args) {  
        launch(args);
    }
}
