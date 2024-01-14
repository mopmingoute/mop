/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EscritorProbLpSolve is part of MOP.
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

package cp_persistencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosVariableControl;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

public class EscritorProbLpSolve extends EscritorProbParaSolver{
	
		
	public EscritorProbLpSolve(String dirSalida, String nomArchSinExt, DatosEntradaProblemaLineal prob) {
		super(dirSalida, nomArchSinExt, prob);
	}
	

	@Override
	public void escribeProb() {	
		String archSalida = dirSalida + "/lpsolve-" + nomArchSinExt + ".lp";
		StringBuilder sb = new StringBuilder();
		cargaObj(sb);
		cargaRests(sb);
		cargaVars(sb);
		DirectoriosYArchivos.siExisteElimina(archSalida);
		DirectoriosYArchivos.agregaTexto(archSalida, sb.toString());	
	}
		
	public void cargaObj(StringBuilder sb) {
		sb.append("min: ");
		DatosObjetivo obj = prob.getObjetivo();		
		sb.append(obj.getTerminoIndependiente() + " ");
		Hashtable<String, Double> terminos = obj.getTerminos();		
		ArrayList<String> lter = new ArrayList<>(terminos.keySet());
		Collections.sort(lter);
		int i=0;
		for(String s: lter) {
			double coef = terminos.get(s);
			if(coef>0) {
				sb.append(" + ");
				sb.append(coef + " ");
				sb.append(s);
			}else {
				sb.append(" - " + (-coef) + " ");
				sb.append(s);
			}
			if(i<lter.size()-1) {
				sb.append(" ");
			}else {
				sb.append(";");
			}
			i++;	
//			if(i%10 == 0) sb.append("\n");
		}
		sb.append("\n");
	}
	
	public void cargaRests(StringBuilder sb) {
		ArrayList<DatosRestriccion> rests = prob.getRestricciones();
		Collections.sort(rests);
		for(DatosRestriccion dr: rests) {
			sb.append(dr.creaSalidaLpSolve());
			sb.append("\n");
		}
	}
	
	public void cargaVars(StringBuilder sb) {
		ArrayList<DatosVariableControl> vars = prob.getVariables();
		Collections.sort(vars);
		ArrayList<String> binarias = new ArrayList<String>();
		ArrayList<String> enteras = new ArrayList<String>();
		ArrayList<String> libres = new ArrayList<String>();
		for(DatosVariableControl dv: vars) {
			String rs = dv.creaCotasLpSolve();
			if(rs!=null) sb.append(rs + "\n");
			if(dv.getTipo()==Constantes.VCBINARIA) {
				binarias.add(dv.getNombre());
			}else if(dv.getTipo()==Constantes.VCENTERA) {
				enteras.add(dv.getNombre());
			}else if(dv.getTipo()==Constantes.VCLIBRE) {
				libres.add(dv.getNombre());
			}
		}
		if(binarias.size()>0) {		
			sb.append("bin ");
			int iv=0;
			for(String nv: binarias) {
				sb.append(nv);
				if(iv<binarias.size()-1) sb.append(", ");
				iv++;
			}
			sb.append(";\n");
		}
		if(enteras.size()>0) {		
			sb.append("int ");
			int iv=0;
			for(String nv: enteras) {
				sb.append(nv);
				if(iv<enteras.size()-1) sb.append(", ");
				iv++;
			}
			sb.append(";\n");
		}
		if(libres.size()>0) {		
			sb.append("free ");
			int iv=0;
			for(String nv: libres) {
				sb.append(nv);
				if(iv<libres.size()-1) sb.append(", ");
				iv++;
			}
			sb.append(";\n");
		}
	}

}
