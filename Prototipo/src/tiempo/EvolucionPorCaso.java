/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EvolucionPorCaso is part of MOP.
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

package tiempo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Subclase de una evolución que para cada valor de una clave nombreCaso devuelve 
 * el resultado en el instante de una evolución asociada a esa clave,
 * 
 * @author ut469262
 *
 * @param <T>
 */
public class EvolucionPorCaso<T> extends Evolucion<T> implements Serializable {
	
	
	private String tipoDato;  // nombre del tipo de datos de this, por ejemplo "modulos-TG"  o "Pot-Solar"
	private Hashtable<String, String>   casosCorrientes;   // Los carga el estudio para cada corrida
	private Hashtable<String, Evolucion<T>> evoluciones;
	private ArrayList<String> nombresCasos;   // Los valores que puede tomar el caso
	
	
	
	
	public EvolucionPorCaso(Hashtable<String, Evolucion<T>> evols, SentidoTiempo st) {
		
		super(st);
		this.evoluciones = evols;
		
	}
		
	
    public EvolucionPorCaso(String tipoDato, Hashtable<String, Evolucion<T>> evols, SentidoTiempo st, ArrayList<String> nombresCasos) {          
        super(st);
        this.tipoDato = tipoDato;
        this.evoluciones = evols;
        this.nombresCasos = nombresCasos;       
        casosCorrientes = new Hashtable<String, String>();
   }

	

	@Override
	public T getValor(long instante) {

		String casoCorriente = casosCorrientes.get(tipoDato);
		Evolucion<T> evol = evoluciones.get(casoCorriente);
		return evol.getValor(instante);		
		
	}

	
	
	@Override
	public void inicializarParaSimulacion() {
		// TODO Auto-generated method stub
		
	}

	public Hashtable<String, Evolucion<T>> getEvoluciones() {
		return evoluciones;
	}

	public void setEvoluciones(Hashtable<String, Evolucion<T>> evoluciones) {
		this.evoluciones = evoluciones;
	}

	public ArrayList<String> getNombresCasos() {
		return nombresCasos;
	}

	public void setNombresCasos(ArrayList<String> nombresCasos) {
		this.nombresCasos = nombresCasos;
	}



	public String getTipoDato() {
		return tipoDato;
	}



	public void setTipoDato(String tipoDato) {
		this.tipoDato = tipoDato;
	}



	public Hashtable<String, String> getCasosCorrientes() {
		return casosCorrientes;
	}



	public void setCasosCorrientes(Hashtable<String, String> casosCorrientes) {
		this.casosCorrientes = casosCorrientes;
	}



	@Override
	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(tipoDato.trim().equals("")) { errores.add("EvolucionPorCaso: tipoDato vacío."); }

		if(casosCorrientes == null) { errores.add("EvolucionPorCaso: casosCorrientes vacío."); }
		else {
			casosCorrientes.forEach((k, v) -> {
				if (v.trim().equals("")) {
					errores.add("EvolucionPorCaso: casosCorrientes vacío.");
				}
			});
		}
		if(evoluciones == null) { errores.add("EvolucionPorCaso: evoluciones vacío."); }
		else {
			evoluciones.forEach((k, v) -> {
				if (v.controlDatosCompletos().size() > 0) {
					errores.add("EvolucionPorCaso: evoluciones vacío.");
				}
			});
		}
		if(nombresCasos == null) { errores.add("EvolucionPorCaso: nombresCasos vacío."); }
		else {
			nombresCasos.forEach((v) -> {
				if (v.trim().equals("")) {
					errores.add("EvolucionPorCaso: nombresCasos vacío.");
				}
			});
		}
		return errores;
	}

	@Override
	public ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err) {
		//TODO control Rango en Evolucion Por caso
		return null;
	}

	@Override
	public ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err) {
		//TODO control Rango en Evolucion Por caso
		return null;
	}


}
