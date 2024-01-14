/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProblemaHandler is part of MOP.
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

package problema;

import java.util.ArrayList;

import datatypesProblema.DatosEntradaProblemaLineal;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;
import utilitarios.Constantes;

/**
 * Clase encargada de manejar lo relativo al problema de despacho
 * 
 * @author ut602614
 *
 */
public class ProblemaHandler { 

	private static ProblemaHandler instance;
	private int resolvedorPrincipal;
	private ResolvedorLineal resolvedor;
	private ResolvedorLineal resolvedorAlternativo;
//	private ResolvedorLineal resolvedorPrincipalInicial;
	private DatosEntradaProblemaLineal ultimoProblema;
	private DatosEntradaProblemaLineal problemaActual;

	private ProblemaHandler() {
		resolvedorPrincipal = Constantes.RESOLVEDOR_PRINCIPAL ;
		if (resolvedorPrincipal == Constantes.RES_GLPK) {
			resolvedor = new ResolvedorGLPK_anterior();
		//	setResolvedorPrincipalInicial(resolvedor);
			resolvedorAlternativo = new ResolvedorLPSolve();
		} else if ((resolvedorPrincipal == Constantes.RES_LP_SOLVE)) {
			resolvedor = new ResolvedorLPSolve();
			//setResolvedorPrincipalInicial(resolvedor);
			resolvedorAlternativo = new ResolvedorGLPK_anterior();
		} else if(resolvedorPrincipal == Constantes.RES_XPRESS){
			resolvedor = new ResolvedorXpress();
			//setResolvedorPrincipalInicial(resolvedor);
			// Seteo como secundario el GLPK porque es el que más me gusta
			resolvedorAlternativo = new ResolvedorGLPK();
		}

		ultimoProblema = null;
		problemaActual = null;
	}

	/** Función del singleton que devuelve siempre la misma instancia */
	public static ProblemaHandler getInstance() {
		if (instance == null)
			instance = new ProblemaHandler();

		return instance;
	}
	public static void deleteInstance() {
		instance = null;			
	}

	public DatosSalidaProblemaLineal resolver(DatosEntradaProblemaLineal entrada, int escenario, int paso,
			String dirInfactible, String dirSalidasLP) {

		ultimoProblema = problemaActual;
		problemaActual = entrada;
		DatosEntradaProblemaLineal actualizado = null;
		if (ultimoProblema != null) {
			actualizado = ultimoProblema.diferencias(problemaActual);	
		}

		return resolvedor.resolver(entrada, actualizado, escenario, paso, dirInfactible, dirSalidasLP);
	}

	public ResolvedorLineal getResolvedorAlternativo() {
		return resolvedorAlternativo;
	}

	public void setResolvedorAlternativo(ResolvedorLineal resolvedorAlternativo) {
		this.resolvedorAlternativo = resolvedorAlternativo;
	}

	public void cambiarResolvedor() {
		ResolvedorLineal aux = resolvedorAlternativo;
		resolvedorAlternativo = resolvedor;
		resolvedor = aux;
	}

	public void resetearResolvedor() {
		if (resolvedorPrincipal == Constantes.RES_LP_SOLVE) {
			if (resolvedor instanceof ResolvedorGLPK_anterior) {
				cambiarResolvedor();
			};			
		} else if (resolvedorPrincipal == Constantes.RES_GLPK) {
			if (resolvedor instanceof ResolvedorLPSolve) {
				cambiarResolvedor();
			};			
		}
	}


}




