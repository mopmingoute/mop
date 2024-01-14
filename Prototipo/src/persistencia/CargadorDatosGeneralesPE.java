/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargadorDatosGeneralesPE is part of MOP.
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

package persistencia;

import java.util.ArrayList;

import datatypesProcEstocasticos.DatosAgregadorLineal;
import datatypesProcEstocasticos.DatosDiscretizacionesVEPE;
import datatypesProcEstocasticos.DatosGeneralesPE;
import datatypesProcEstocasticos.DatosTransformaciones;
import utilitarios.AsistenteLectorEscritorTextos;
import utilitarios.DirectoriosYArchivos;
import utilitarios.LeerDatosArchivo;

public class CargadorDatosGeneralesPE {
	
	
	/**
	 * 
	 * @param ruta es el directorio donde están todos los datos del PE, cuyo nombre
	 * coincide con del proceso estocástico
	 * @return
	 */
	public static DatosGeneralesPE devuelveDatosGeneralesPE(String ruta, String nombreProc){
		
		DatosGeneralesPE dat = new DatosGeneralesPE();
		
		String dirArchivo = ruta + "/datosGenerales.txt";		
        ArrayList<ArrayList<String>> texto;       
        texto = LeerDatosArchivo.getDatos(dirArchivo);	
        dat.setNombre(nombreProc);	
    	AsistenteLectorEscritorTextos lector = new AsistenteLectorEscritorTextos(texto, dirArchivo);    	
    	int i=0;   		    	
    	dat.setNombreEstimacion(lector.cargaPalabra(i, "NOMBRE_ESTIMACION"));
    	i++;
    	dat.setUsoSimulacion(lector.cargaBooleano(i, "USO_SIMULACION"));
    	i++;
    	dat.setUsoOptimizacion(lector.cargaBooleano(i, "USO_OPTIMIZACION"));
    	i++;
    	dat.setNombrePEAsociadoEnOptim(lector.cargaPalabra(i, "PROC_ASOCIADO_EN_OPTIM"));
    	i++;
    	dat.setUsaTransformaciones(lector.cargaBooleano(i, "USA_TRANSFORMACIONES"));
    	i++;    	
    	dat.setNombresVariables(lector.cargaLista(i, "NOMBRES_VARIABLES"));
    	dat.setCantVariables(dat.getNombresVariables().size());
    	i++;
    	dat.setNombresVarsEstado(lector.cargaLista(i, "NOMBRES_VARS_ESTADO"));
    	i++;
    	dat.setUsaVarsEstadoEnOptim(lector.cargaBooleano(i,"USA_VARS_ESTADO_EN_OPTIM"));
    	i++;
    	dat.setDiscretoExhaustivo(lector.cargaBooleano(i, "DISCRETO_EXHAUSTIVO"));
    	i++;
    	dat.setTieneVEContinuas(lector.cargaBooleano(i, "TIENE_VE_CONTINUAS"));
    	i++;
    	lector.verificaEtiqueta(i, "PRIORIDAD_SORTEOS");
    	i++;
    	dat.setNombrePaso(lector.cargaPalabraDeLista(i, "NOMBRE_PASO", utilitarios.Constantes.NOMBRESPASOS));
    	i++;
    	dat.setTieneVAExogenas(lector.cargaBooleano(i, "TIENE_VARS_EXOGENAS"));
    	i++;
    	dat.setNombresVAExogenas(lector.cargaLista(i,"NOMBRES_VAR_EXOGENAS"));
    	i++;
    	dat.setNombresProcesosExogenas(lector.cargaLista(i,"NOMBRES_PROCESOS_EXOGENAS"));   
    	
    	
		DatosTransformaciones datTrans;
		if(dat.isUsaTransformaciones()){
			datTrans = persistencia.CargadorTransformaciones.devuelveDatosTransformaciones(ruta);
			dat.setDatTransformaciones(datTrans);
		}
						
		String archAgregador = ruta + "/agregadorEstados.txt";   // si en el directorio hay un agregador lineal lo lee.
		if(DirectoriosYArchivos.existeArchivo(archAgregador)){
			DatosAgregadorLineal datAgr = persistencia.CargadorAgregadorLineal.devuelveDatosAgregadorLineal(archAgregador, nombreProc, dat.getNombreEstimacion());
			dat.setDatAgregadorEstados(datAgr);
		}
		
		if(dat.isTieneVEContinuas() && dat.isUsoOptimizacion()){
			DatosDiscretizacionesVEPE datDis = persistencia.CargadorDiscretizacionesVEPE.devuelveDatosDiscretizacionesVEPE(ruta);
			dat.setDatDiscVEPE(datDis);
		}
       	return dat;
	}

}
