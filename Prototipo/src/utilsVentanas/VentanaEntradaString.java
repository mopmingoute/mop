/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * VentanaEntradaString is part of MOP.
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

package utilsVentanas;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

public class VentanaEntradaString {
	
	/**
	 * Lee un String con un texto entrado en una pantalla
	 * @param mensaje
	 * @param ancho
	 * @param alto
	 * @return
	 */
	public static String leerTexto(String mensaje, int ancho, int alto){		
		UIManager.put("OptionPane.minimumSize",new Dimension(ancho, alto));
		String texto = JOptionPane.showInputDialog(mensaje);
		return texto;
	}
	
	public static void main(String[] args){
		String perro = VentanaEntradaString.leerTexto("perro", 100, 100);
		
	}
	


}
