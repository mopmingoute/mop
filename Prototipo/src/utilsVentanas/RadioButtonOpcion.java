package utilsVentanas;

import javax.swing.*;    
import java.awt.event.*;    

public class RadioButtonOpcion extends JFrame implements ActionListener{
	int result = 0;
	String texto1;
	String texto2;
	JRadioButton rb1,rb2;    
	JButton b;    
	RadioButtonOpcion(String texto1, String texto2){
	this.texto1 = texto1;
	this.texto2 = texto2;
	rb1=new JRadioButton(texto1);    
	rb1.setBounds(100,50,100,30);      
	rb2=new JRadioButton(texto2);    
	rb2.setBounds(100,100,100,30);    
	ButtonGroup bg=new ButtonGroup();    
	bg.add(rb1);bg.add(rb2);    
	b=new JButton("click");    
	b.setBounds(100,150,80,30);    
	b.addActionListener(this);    
	add(rb1);add(rb2);add(b);    
	setSize(300,300);    
	setLayout(null);    
	setVisible(true);    
	}    
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(rb1.isSelected()){    
			result = 1;    
		}    
		if(rb2.isSelected()){    
			result = 2;    
		}    
	}    
	
	
	public int abreRadioButton(String texto1, String texto2) {
		new RadioButtonOpcion(texto1, texto2);
		return result;		
	}
	
	public static void main(String args[]){ 
		String texto1 = "opcion1";
		String texto2 = "opcion2";
		RadioButtonOpcion rbo = new RadioButtonOpcion(texto1, texto2);
		int result = rbo.abreRadioButton("opcion1", "opcion2");
		System.out.println("resultado" + result);
	}	
}
