/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ManejadorSqlite is part of MOP.
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

import java.sql.*;
import java.util.ArrayList;

import parque.Corrida;
import parque.Demanda;
import parque.Falla;
import parque.Generador;
import parque.GeneradorHidraulico;
import logica.CorridaHandler;
import datatypes.DatosLocalizadorSalida;
import datatypes.DatosFuenteSalida;
import datatypesProblema.DatosCorridaSalida;
import datatypesProblema.DatosHidroSalida;

/**
 * Clase que maneja la conexión a una base de datos SQLite
 * @author ut602614
 *
 */
public class ManejadorSqlite {
	
	
	
	/**
	 * Se define un atributo que es la conexión a la base de datos
	 */
	private Connection conexion;

	/**
	 * @param datos
	 * @param ruta
	 * @throws Exception
	 */
	
	
	
	/**
	 * Abre la conexión a la base de datos
	 * @throws Exception 
	 */
	public void abreConexionDB(String ruta) throws Exception{		
		//Class.forName("org.sqlite.JDBC");
		//conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta);		
	}	
	
	
	
	/**
	 * Crea todas las tablas de la base de datos de resultados.
	 * Debe ejecutarse antes el mótodo abreConexionDB
	 */
	public void creaTablasDB(String ruta) throws Exception{		
		Statement stat = conexion.createStatement();		
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS Localizadores (id_loc, anio, semana, poste, cronica, id_corrida);");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS Fuentes (id_fuente, nombre);");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS Potencias (id_loc, id_fuente, potencia);");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS Costos (id_loc, id_fuente, costo);");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS Hidros (id_loc, id_fuente, turbinado, vertido);");						
	}	
	
	
	
	public void guardarDespachoDB(DatosCorridaSalida datos) throws Exception {



		PreparedStatement insertaPotencia = conexion.prepareStatement("INSERT INTO Potencias VALUES (?,?,?);");
		PreparedStatement insertaLocalizador = conexion.prepareStatement("INSERT INTO Localizadores VALUES (?,?,?,?,?);");
		PreparedStatement insertaCosto = conexion.prepareStatement("INSERT INTO Costos VALUES (?,?,?);");
		PreparedStatement insertaHidro = conexion.prepareStatement("INSERT INTO Hidros VALUES (?,?,?,?);");



		CorridaHandler ch = CorridaHandler.getInstance();
		Corrida corrida = ch.getCorridaActual();
				
		int id_loc = 0;
		for(DatosLocalizadorSalida dls: datos.getLocalizadores()){
			String nombreBarra = dls.getBarra();
			
			insertaLocalizador.setInt(1, id_loc);
			insertaLocalizador.setInt(4, dls.getPoste());
			insertaLocalizador.addBatch();
			
			
			// quedan muchos campos vacóos
			
			ArrayList<Generador> generadoresBarra = corrida.getGeneradoresBarra(nombreBarra);
			for(Generador g: generadoresBarra){				
				DatosFuenteSalida dfs = dls.getFuentes().get(g.getNombre());
				int id_fuente = ch.getTablaIdFuentes().get(g.getNombre());				
				insertaPotencia.setInt(1, id_loc);				
				insertaPotencia.setInt(2, id_fuente);
				insertaPotencia.setDouble(3, dfs.getPotencia());
				insertaPotencia.addBatch();
				
				if (g instanceof GeneradorHidraulico){
					DatosHidroSalida dhs = dls.getHidros().get(g.getNombre());									
					insertaHidro.setInt(1, id_loc);
					insertaHidro.setInt(2, id_fuente);
					insertaHidro.setDouble(3, dhs.getTurbinado());
					insertaHidro.setDouble(4, dhs.getVertido());
					insertaHidro.addBatch();
				}
				
			}
			
			ArrayList<Demanda> demandasBarra = corrida.getDemandasBarra(nombreBarra);
			for(Demanda d: demandasBarra){				
				DatosFuenteSalida dfs = dls.getFuentes().get(d.getNombre());
				int id_fuente = ch.getTablaIdFuentes().get(d.getNombre());				
				insertaPotencia.setInt(1, id_loc);				
				insertaPotencia.setInt(2, id_fuente);
				insertaPotencia.setDouble(3, dfs.getPotencia());
				insertaPotencia.addBatch();
			}			
			
			
			ArrayList<Falla> fallasBarra = corrida.getFallasBarra(nombreBarra);
			for(Falla d: fallasBarra){				
				DatosFuenteSalida dfs = dls.getFuentes().get(d.getNombre());
				int id_fuente = ch.getTablaIdFuentes().get(d.getNombre());				
				insertaPotencia.setInt(1, id_loc);				
				insertaPotencia.setInt(2, id_fuente);
				insertaPotencia.setDouble(3, dfs.getPotencia());
				insertaPotencia.addBatch();
			}							
			// insertaCosto.addBatch();
																	
			id_loc ++;
		}
		insertaPotencia.executeBatch();
		// insertaCosto.executeBatch();
		insertaLocalizador.executeBatch();
		insertaHidro.executeBatch();		
		conexion.close();

	}

	
	/**
	 * Cierra la conexión a la base de datos
	 * @throws SQLException 
	 */
	public void cierraConexionDB() throws SQLException{
		
		this.conexion.close();
		
	}
	
	
	/**
	 * Carga la tabla Fuentes de la base de datos de resultados
	 * 
	 */
	public void cargaFuentesDB(String ruta) throws Exception{
		
		
		
		
		
	}
	
	

}
