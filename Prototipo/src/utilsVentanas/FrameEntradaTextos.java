/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * FrameEntradaTextos is part of MOP.
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

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class FrameEntradaTextos extends JFrame implements ActionListener{
	
	ArrayList<String> textos;
	JTextField texto0;
	JTextField texto1;
	JButton button;
	
	public FrameEntradaTextos(String tituloFrame, ArrayList<String> mensajes, int ancho, int alto){
		
		textos = new ArrayList<String>();		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout (3,3));
		JLabel label0 = new JLabel(mensajes.get(0));
		texto0 = new JTextField(10);
//		texto1 = new JTextField(10);
		JLabel label1 = new JLabel(mensajes.get(1));
		texto1 = new JTextField(10);		
		button = new JButton();
		button.setText("Entrar datos");
		button.setBounds(400, 400, 200, 100);
		button.addActionListener(this);	
		panel.add(label0);
		panel.add(texto0);
		panel.add(label1);
//		panel.add(texto1);	
		panel.add(button);
		this.add(panel);
		this.setSize(ancho, alto);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);	
		
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==button) {
			textos.add(texto0.getText());
			System.out.println("paso por action listener");
			System.out.println(textos.get(0));
			System.out.println(texto0.getText());
		}
	}

//	public void windowClosing(WindowEvent e) {
//        dispose();
//       
//	}
	
	
	public ArrayList<String> devuelveTextos(){
		return this.getTextos();
	}
	



	public ArrayList<String> getTextos() {
		return textos;
	}


	public void setTextos(ArrayList<String> textos) {
		this.textos = textos;
	}


	public static void main(String[] args) {
		ArrayList<String> mensajes = new ArrayList<String>();
		mensajes.add("Entrar el primer mensaje mensaje1");
		mensajes.add("Entrar el segundo texto de blblblblblal");
		FrameEntradaTextos fet = new FrameEntradaTextos("Esta es una ventana", mensajes, 600, 100);
	}

}
