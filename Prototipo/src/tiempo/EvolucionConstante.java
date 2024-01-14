/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EvolucionConstante is part of MOP.
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

/**
 * Clase que representa una evolución constante en el tiempo (siempre el mismo valor)
 * @author ut602614
 *
 * @param <T> Tipo de dato del valor de la evolución
 */

public class EvolucionConstante<T> extends Evolucion<T> implements Serializable{
	
	public EvolucionConstante(T val, SentidoTiempo st) {
		super(st);
		this.setValor(val);

	}
	
	
	@Override
	public T getValor(long instante) {
		// TODO Auto-generated method stub
		return valor;
	}
	
	
	@Override
	public void inicializarParaSimulacion() {
		// DELIBERADAMENTE VACóO
		
	}

	@Override
	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(valor == null) { errores.add("Evolucion Constante: valor vacio"); }
		return errores;
	}

	@Override
	public ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err) {

		if(min instanceof Double){
			Double mi = (Double) min;
			Double ma = (Double) max;
			Double va = (Double) valor;
			if(va > ma || va < mi) { err.add("EvolucionPorInstantes: fuera de rango");}

		}

		return err;
	}
	@Override
	public ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err) {
		if(min instanceof Integer){
			Integer mi = (Integer) min;
			Integer ma = (Integer) max;
			Integer va = (Integer) valor;
			if(va > ma || va < mi) { err.add("EvolucionPorInstantes: fuera de rango");}

		}
		return err;
	}


}

