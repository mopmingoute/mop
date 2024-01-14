/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LParticipante is part of MOP.
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

package presentacion;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;

public class LParticipante extends JFrame {

	private JPanel contentPane;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LParticipante frame = new LParticipante();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();				
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LParticipante() {
		setTitle("Nuevo Participante");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 271, 210);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNombre = new JLabel("Nombre:");
		lblNombre.setBounds(55, 50, 46, 14);
		contentPane.add(lblNombre);
		
		JLabel lblNewLabel = new JLabel("Tipo:");
		lblNewLabel.setBounds(55, 92, 46, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblDatosDelParticipante = new JLabel("Datos del Participante");
		lblDatosDelParticipante.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblDatosDelParticipante.setBounds(66, 11, 179, 14);
		contentPane.add(lblDatosDelParticipante);
		
		JComboBox tipoPart = new JComboBox();
		tipoPart.setModel(new DefaultComboBoxModel(new String[] {"Generador", "Demanda", "Falla", "Impacto"}));
		tipoPart.setBounds(111, 89, 112, 20);
		contentPane.add(tipoPart);
		
		textField = new JTextField();
		textField.setBounds(111, 47, 86, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JButton btnContinuar = new JButton("Continuar");
		btnContinuar.setBounds(93, 128, 89, 23);
		contentPane.add(btnContinuar);
	}
}
