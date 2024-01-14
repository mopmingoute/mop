/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * RedCompDesp is part of MOP.
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

package compdespacho;


import java.util.Iterator;
import java.util.Set;
import parque.Barra;
import parque.Rama;
import parque.RedElectrica;

/**
 * Clase encargada de modelar el comportamiento de la red elóctrica en el problema de despacho
 * @author ut602614
 *
 */
public class RedCompDesp extends CompDespacho{
	
	private String compRed;
	private boolean uninodal; 
	private RedElectrica red;
	
	public RedCompDesp(RedElectrica redElectrica) {
		this.red = redElectrica;
	}

	public String getCompRed() {
		return compRed;
	}

	public void setCompRed(String compRed) {
		this.compRed = compRed;
	}

	@Override
	public void crearVariablesControl() {
		if (!uninodal) {
			Set<String> claves = this.red.getBarras().keySet();
			Iterator<String> it = claves.iterator();
			
			String clave; 
			
			while (it.hasNext()) {
			    	clave = it.next();
			    	Barra barra = this.red.getBarras().get(clave);
			    	barra.getCompDesp().crearVariablesControl();    		    	
			    	
			}
			
			claves = this.red.getRamas().keySet();
			it = claves.iterator();		 
			
			while (it.hasNext()) {
			    	clave = it.next();
			    	Rama rama = this.red.getRamas().get(clave);
			    	rama.getComportamiento().crearVariablesControl(); 
			}			
		} else {
			this.red.getBarraUnica().getCompDesp().crearVariablesControl();			
		}		
	}

	@Override
	public void cargarRestricciones() {
		if (!uninodal) {
			Set<String> claves = this.red.getBarras().keySet();
			Iterator<String> it = claves.iterator();
			
			String clave; 
			
			while (it.hasNext()) {
			    	clave = it.next();
			    	Barra barra = this.red.getBarras().get(clave);
			    	barra.getCompDesp().cargarRestricciones();    		    	
			    	
			}
			
			claves = this.red.getBarras().keySet();
			it = claves.iterator();		 
			
			while (it.hasNext()) {
			    	clave = it.next();
			    	Rama rama = this.red.getRamas().get(clave);
			    	rama.getComportamiento().cargarRestricciones();    		    	
			    	
			}
			
		} else {
			this.red.getBarraUnica().getCompDesp().cargarRestricciones();
						
		}				
	}

	@Override
	public void contribuirObjetivo() {
		if (!uninodal) {
			Set<String> claves = this.red.getBarras().keySet();
			Iterator<String> it = claves.iterator();
			
			String clave; 
			
			while (it.hasNext()) {
			    	clave = it.next();
			    	Barra barra = this.red.getBarras().get(clave);
			    	barra.getCompDesp().contribuirObjetivo();    		    	
			    	
			}
			
			claves = this.red.getBarras().keySet();
			it = claves.iterator();		 
			
			while (it.hasNext()) {
			    	clave = it.next();
			    	Rama rama = this.red.getRamas().get(clave);
			    	rama.getComportamiento().contribuirObjetivo();    		    	
			    	
			}			
		} else {
			this.red.getBarraUnica().getCompDesp().contribuirObjetivo();
		}		
	}


	public boolean isUninodal() {
		return uninodal;
	}

	public void setUninodal(boolean uninodal) {
		this.uninodal = uninodal;
	}

	public RedElectrica getRed() {
		return red;
	}

	public void setRed(RedElectrica red) {
		this.red = red;
	}


}
