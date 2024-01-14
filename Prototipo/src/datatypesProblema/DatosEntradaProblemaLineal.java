/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosEntradaProblemaLineal is part of MOP.
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

package datatypesProblema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import utilitarios.DirectoriosYArchivos;

/**
 * Datatype que representa los datos de entrata relativos a un problema lineal
 * 
 * @author ut602614
 *
 */

public class DatosEntradaProblemaLineal {
	private DatosObjetivo objetivo;
	/** Objetivo del problema lineal */
	private Hashtable<String, DatosVariableControl> vars;
	private Hashtable<String, DatosRestriccion> restrs;

	public DatosEntradaProblemaLineal() {
		objetivo = new DatosObjetivo();
		vars = new Hashtable<String, DatosVariableControl>();
		restrs = new Hashtable<String, DatosRestriccion>();
	}

	public ArrayList<DatosRestriccion> getRestricciones() {
		ArrayList<DatosRestriccion> ret = new ArrayList<DatosRestriccion>(restrs.values());
		return ret;
	}


	public DatosObjetivo getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(DatosObjetivo objetivo) {
		this.objetivo = objetivo;
	}

	public void agregarRestricciones(Hashtable<String, DatosRestriccion> hashtable) {
		Set<String> set;
		String clave;
		set = hashtable.keySet();
		Iterator<String> itr = set.iterator();
		while (itr.hasNext()) {
			clave = itr.next();
			DatosRestriccion currRestr = hashtable.get(clave);
			if (restrs.containsKey(clave)) {
				currRestr.contribuir(restrs.get(clave));
				restrs.remove(clave);
			}
			restrs.put(clave, currRestr);
		}
	}

	public void contribuirObjetivo(DatosObjetivo contribucionObjetivo) {
		this.objetivo.contribuir(contribucionObjetivo);
	}

	public ArrayList<DatosVariableControl> getVariables() {
		ArrayList<DatosVariableControl> ret = new ArrayList<DatosVariableControl>(vars.values());
		return ret;
	}

	/*
	 * public void setVariables(ArrayList<DatosVariableControl> variables) {
	 * this.variables = variables; }
	 */
    public void agregarVariables(ArrayList<DatosVariableControl> nuevas) {
        for (int i = 0; i < nuevas.size(); i++) {
               DatosVariableControl vc = nuevas.get(i);
               vars.put(vc.getNombre(), vc);
        }
    }


	public void imprimir() {
		objetivo.imprimir();
		ArrayList<DatosRestriccion> restricciones = this.getRestricciones();
		for (DatosRestriccion dr : restricciones) {
			dr.imprimir();
		}
		imprimirVariables();

	}

	public void imprimir(String rutaArchivo) {
		DirectoriosYArchivos.siExisteElimina(rutaArchivo);
		objetivo.guardar(rutaArchivo);
		ArrayList<DatosRestriccion> restricciones = this.getRestricciones();
		for (DatosRestriccion dr : restricciones) {
			dr.guardar(rutaArchivo);
		}
		System.out.println("Termina impresión de objetivo y restricciones en entrada");
		imprimirVariables(rutaArchivo);
	}
	
	
	
	public void imprimirEntradaPLNueva(String rutaArchivo) {
		StringBuilder sb = new StringBuilder();
		sb.append(objetivo.creaSalida());
		ArrayList<DatosRestriccion> restricciones = this.getRestricciones();
		int r=0;
		sb.append("\n");
		for (DatosRestriccion dr : restricciones) {
			sb.append(dr.creaSalida());
			sb.append("\n");
			r++;
		}
		ArrayList<DatosVariableControl> variables = this.getVariables();
		for (DatosVariableControl dvc : variables) {
			sb.append(dvc.creaSalida());
			sb.append("\n");
		}
		DirectoriosYArchivos.siExisteElimina(rutaArchivo);
		DirectoriosYArchivos.agregaTexto(rutaArchivo, sb.toString());
		
		System.out.println("Termina impresión de archivo de entradas del PL en directorio entradas");
	}	
		
		

	public void imprimirVariables() {
		ArrayList<DatosVariableControl> variables = this.getVariables();
		for (DatosVariableControl dvc : variables) {
			dvc.imprimir();
		}
	}

	public void imprimirVariables(String rutaArchivo) {
		ArrayList<DatosVariableControl> variables = this.getVariables();
		for (DatosVariableControl dvc : variables) {
			dvc.guardar(rutaArchivo);
		}
	}

	public void guardar(String ruta) {
		objetivo.guardar(ruta);
		ArrayList<DatosRestriccion> restricciones = this.getRestricciones();
		for (DatosRestriccion dr : restricciones) {
			dr.guardar(ruta);
		}
		guardarVariables(ruta);

	}

	public void guardarVariables(String ruta) {
		ArrayList<DatosVariableControl> variables = this.getVariables();
		for (DatosVariableControl dvc : variables) {
			dvc.guardar(ruta);
		}
	}

	public DatosEntradaProblemaLineal diferencias(DatosEntradaProblemaLineal depl) {

		boolean problemaNuevo = false;

		DatosEntradaProblemaLineal ret = new DatosEntradaProblemaLineal();

		ret.objetivo = objetivo.diferencias(depl.objetivo);

		HashSet<String> misResKeys = new HashSet<String>(restrs.keySet());
		HashSet<String> difResKeys = new HashSet<String>(depl.restrs.keySet());
		misResKeys.addAll(difResKeys);

		Iterator<String> itrK = misResKeys.iterator();
		while (itrK.hasNext() && !problemaNuevo) {
			String currKey = itrK.next();
			DatosRestriccion miRes = restrs.getOrDefault(currKey, null);
			DatosRestriccion deplRes = depl.restrs.getOrDefault(currKey, null);
			if (miRes == null || deplRes == null) {
				problemaNuevo = true;
			} else {
				DatosRestriccion difRes = miRes.diferencias(deplRes);
				if (difRes != null) {
					ret.restrs.put(currKey, difRes);
				}
			}
		}

		HashSet<String> misVarKeys = new HashSet<String>(vars.keySet());
		HashSet<String> difVarKeys = new HashSet<String>(depl.vars.keySet());
		misVarKeys.addAll(difVarKeys);

		Iterator<String> itvK = misVarKeys.iterator();
		while (itvK.hasNext() && !problemaNuevo) {
			String currKey = itvK.next();
			DatosVariableControl miVar = vars.getOrDefault(currKey, null);
			DatosVariableControl deplVar = depl.vars.getOrDefault(currKey, null);
			if (miVar == null || deplVar == null) {
				problemaNuevo = true;
			} else {
				if (miVar.cambioVar(deplVar)) {
					ret.vars.put(currKey, deplVar);
				}
			}

		}  

		if (problemaNuevo) {
			ret = null;
		}

		return ret;
	}

	public boolean isEmpty() {
		return objetivo == null && restrs.size() == 0 && vars.size() == 0;
	}

}
