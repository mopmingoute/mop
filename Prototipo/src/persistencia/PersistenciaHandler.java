/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PersistenciaHandler is part of MOP.
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

import java.util.GregorianCalendar;

import datatypes.DatosCorrida;
import datatypes.DatosPostizacion;
import datatypesProblema.DatosCorridaSalida;


/**
 * Clase que maneja el acceso a memoria de disco
 * @author ut602614
 *
 */
public class PersistenciaHandler {
	private static PersistenciaHandler instance;
	
	private CargadorXML loader;
	private ManejadorSqlite manejadorSql;
	
	private PersistenciaHandler() {
		loader = new CargadorXML();
		manejadorSql=new ManejadorSqlite();
	}
	
	
	/**Función del singleton que devuelve siempre la misma instancia*/	
	public static PersistenciaHandler getInstance()
	{
		if (instance  == null)
			instance = new PersistenciaHandler();
	
		return instance;
	}
	
	public static void deleteInstance() {
		instance = null;			
	}
	
	public DatosCorrida cargarCorrida(String ruta){	
		return loader.cargarCorrida(ruta);
		
	}
	
	public void guardarDespachoDB(DatosCorridaSalida datos) throws Exception {
		this.manejadorSql.guardarDespachoDB(datos);
	}


	public ManejadorSqlite getManejadorSql() {
		return manejadorSql;
	}


	public void setManejadorSql(ManejadorSqlite manejadorSql) {
		this.manejadorSql = manejadorSql;
	}


	public DatosPostizacion leerPostizacionExterna(String ruta, GregorianCalendar inicioCorrida) {
		DatosPostizacion resultado = CargadorPostizacion.devuelveDatosPostizacion(ruta, inicioCorrida);
	
		return resultado;
	}
	
	
}
