/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PizarronRedis is part of MOP.
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

package pizarron;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import datatypesSalida.DatosEPPUnEscenario;
import futuro.ClaveDiscreta;
import futuro.InformacionValorPunto;
import logica.CorridaHandler;
import optimizacion.Optimizador;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import utilitarios.Constantes;

public class PizarronRedis extends Pizarron {
	private static PizarronRedis instance;
	private String redisHost = "172.26.161.152";
	private Integer redisPort = 6379;
	private Jedis serv;

	JedisPoolConfig jpc;
	JedisPool jp;

	public static int contador = 0;

	public static PizarronRedis getInstance() {
		if (instance == null)
			instance = new PizarronRedis();
		return instance;
	}

	private PizarronRedis() {
		super();
		try {
			jpc = new JedisPoolConfig();
	//		jp = new JedisPool(jpc, redisHost, redisPort, 600000);
//			this.serv = new Jedis(redisHost, redisPort,100000000 );
			this.serv = jp.getResource();
			this.serv.connect();
			this.serv.select(Constantes.CODIGO_BASE_REDIS);
			this.serv.disconnect();

		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	public Set<String> obtenerServidores() { // Devuelve un conjunto de Strings que corresponden a los nombres de los
												// servidores corriendo el Servicio Windows Redis
		return serv.keys("maquina:*");
	}

	public void setCantServidores(String valor) {
		serv.set("cantServidores", "0");
		serv.get("cantServidores");
	}

	private static String toString(Serializable o) throws IOException {
		Optimizador.prof.iniciarContador("piz-TOSTRING");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();

		String retorno = Base64.getEncoder().encodeToString(baos.toByteArray());
		Optimizador.prof.pausarContador("piz-TOSTRING");

		return retorno;

	}

	private static Object fromString(String s) throws IOException, ClassNotFoundException {
		Optimizador.prof.iniciarContador("piz-FROMSTRING");
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		Optimizador.prof.pausarContador("piz-FROMSTRING");
		return o;
	}

	@Override
	public void cargarCorrida(String corrida, String ruta) {
		System.out.println("Ruta entrando a Redis: " + ruta);

		serv.lpush("nueva", ruta);
		serv.lpush("operaciones", "-1");
		serv.lpush("finalizarOptimizacion", "-1");
		serv.lpush("pasoActual", "-1");
		serv.lpush("finalizarSimulacion", "-1");
	}

	public Jedis getServ() {
		return serv;
	}

	public void setServ(Jedis serv) {
		this.serv = serv;
	}

	@Override
	public boolean hayNuevaCorrida() {
		boolean retorno = serv.lindex("nueva", 0) != null;
		return retorno;
	}

	@Override
	public String dameRutaNueva() {
		String rutaParalela = serv.lindex("nueva", 0);
		System.out.println(rutaParalela);
		String ruta = rutaParalela.replace("/", "\\");

		return ruta;
	}

	@Override
	public int dameOperacion() {
		String op = serv.lindex("operaciones", 0);
		if (op == null)
			return -1;
		return Integer.parseInt(op);
	}

	@Override
	public void optimizar() {
		serv.lset("operaciones", 0, Integer.toString(Constantes.OPTIMIZAR));
		serv.lset("finalizarOptimizacion", 0, "false");
	}

	@Override
	public int obtenercantServidores() {
		// Optimizador.prof.iniciarContador("piz-obtenercantServidores");
		String cM = serv.get("cantServidores");
		while (cM == null)
			cM = serv.get("cantServidores");

		// Optimizador.prof.pausarContador("piz-obtenercantServidores");
		return Integer.parseInt(cM);
	}

	@Override
	public void registrarMaquina() {
		serv.incr("cantServidores");
	}

	@Override
	public void escribirPasoActual(int paso) {
		// Optimizador.prof.iniciarContador("piz-EscribirPaso");
		serv.lset("pasoActual", 0, Integer.toString(paso));
		// Optimizador.prof.pausarContador("piz-EscribirPaso");
	}

	@Override
	public void cargarPaquetesAResolver(int paso, ArrayList<Paquete> paquetes) {
		Optimizador.prof.iniciarContador("piz-CargarPaquetesAResolver");
		for (Paquete p : paquetes) {
			try {
				serv.lpush("paquetesAResolver", PizarronRedis.toString(p));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Optimizador.prof.pausarContador("piz-CargarPaquetesAResolver");
	}

	@Override
	public Paquete obtenerPaqueteAResolver(int paso) {
		Optimizador.prof.iniciarContador("piz-obtenerPaquetesAResolver");
		try {
			String s = serv.lpop("paquetesAResolver");
			if (s == null)
				return null;
			Paquete p = (Paquete) PizarronRedis.fromString(s);
			if (p.getPaso() != paso) {
				serv.lpush("paquetesAResolver", s);
				return null;
			}
			// System.out.println("PAQUETE OBTENIDO: " + p.getEstadoFin());
			Optimizador.prof.pausarContador("piz-obtenerPaquetesAResolver");
			return p;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public void pasarPaqueteAEnResolucion(Paquete paquete) {
		// Optimizador.prof.iniciarContador("piz-CargarPaquetesEnResolucion");
		try {
			serv.lpush("paquetesEnResolucion", PizarronRedis.toString(paquete));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Optimizador.prof.pausarContador("piz-CargarPaquetesEnResolucion");
	}

	@Override
	public ArrayList<Paquete> obtenerPaquetesEnResolucion(int paso) {
		Optimizador.prof.iniciarContador("piz-obtenerPaquetesEnResolucion");
		ArrayList<Paquete> res = new ArrayList<Paquete>();
		ArrayList<Paquete> res2 = new ArrayList<Paquete>();
		String s = serv.lpop("paquetesEnResolucion");
		while (s != null) {
			try {
				Paquete p = (Paquete) PizarronRedis.fromString(s);
				if (p.getPaso() == paso)
					res.add(p);
				else {
					res2.add(p);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			s = serv.lpop("paquetesEnResolucion");
		}
		for (Paquete p2 : res2) {
			try {
				serv.lpush("paquetesEnResolucion", PizarronRedis.toString(p2));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Optimizador.prof.pausarContador("piz-obtenerPaquetesEnResolucion");
		return res;

	}

	@Override
	public ArrayList<Paquete> obtenerPaquetesResueltos(int paso) {
		Optimizador.prof.iniciarContador("piz-obtenerPaquetesResueltos");
		ArrayList<Paquete> res = new ArrayList<Paquete>();
		ArrayList<Paquete> res2 = new ArrayList<Paquete>();
		String s = serv.lpop("paquetesResueltos");
		while (s != null) {
			try {
				Paquete p = (Paquete) PizarronRedis.fromString(s);
				if (p.getPaso() == paso)
					res.add(p);
				else {
					res2.add(p);
				}
			} catch (Exception e) {
				System.out.println("El string que falla es: " + s);
				e.printStackTrace();
			}
			s = serv.lpop("paquetesResueltos");
		}
		for (Paquete p2 : res2) {
			try {
				serv.lpush("paquetesResueltos", PizarronRedis.toString(p2));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Optimizador.prof.pausarContador("piz-obtenerPaquetesResueltos");
		return res;

	}

	@Override
	public int obtenerPasoOptim() {
		String paso = serv.lindex("pasoActual", 0);
		if (paso == null)
			return -1;
		return Integer.parseInt(paso);
	}

	@Override
	public void pasarPaqueteAResuelto(Paquete paquete) {
		// Optimizador.prof.iniciarContador("piz-pasarPaqueteAResuelto");
		try {
			String sPaquete = PizarronRedis.toString(paquete);
			System.out.println("La PC: " + paquete.getNroMaquina() + "manda el paquete resuelto: " + sPaquete);
			serv.lpush("paquetesResueltos", sPaquete);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Optimizador.prof.pausarContador("piz-pasarPaqueteAResuelto");
	}

	@Override
	public void pasarPaqueteEscenarioAResuelto(PaqueteEscenarios paquete) {
		try {
			serv.lpush("paquetesEscenariosResueltos", PizarronRedis.toString(paquete));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Optimizador.prof.pausarContador("piz-pasarPaqueteAResuelto");
	}

	@Override
	public void finalizarOptimizacion() {
		serv.lset("finalizarOptimizacion", 0, "true");
		serv.lset("operaciones", 0, Integer.toString(Constantes.ESPERANDO_OPERACION));
	}

	@Override
	public boolean hayQueFinalizarOptimizacion() {
		String finalizar = serv.lindex("finalizarOptimizacion", 0);
		if (finalizar == null)
			return false;

		return finalizar.equalsIgnoreCase("true");
	}

	@Override
	public void simular() {
		serv.lset("operaciones", 0, Integer.toString(Constantes.SIMULAR));
		serv.lset("finalizarSimulacion", 0, "false");
	}

	@Override
	public boolean hayQueFinalizarSimulacion() {
		String finalizar = serv.lindex("finalizarSimulacion", 0);
		if (finalizar == null)
			return false;
		return finalizar.equalsIgnoreCase("true");
	}

	@Override
	public void finalizarSimulacion() {
		serv.lset("finalizarSimulacion", 0, "true");
		serv.lset("operaciones", 0, Integer.toString(Constantes.CERRARSERVIDOR));

	}

	@Override
	public int[] devuelveCodigoControlesDEOpt(String corrida, int paso, ClaveDiscreta sInit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cargaCodigoControlesDEOpt(String corrida, int paso, int[] sInit, int[] codigoOpt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void imprimirListaAResolver() {
		List<String> lista = serv.lrange("paquetesAResolver", 0, -1);

		for (String s : lista) {
			try {
				Paquete p = (Paquete) PizarronRedis.fromString(s);
				// System.out.print(p.getEstadoIni() + " - ");
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void imprimirListaResueltos() {
		List<String> lista = serv.lrange("paquetesResueltos", 0, -1);

		for (String s : lista) {
			try {
				Paquete p = (Paquete) PizarronRedis.fromString(s);
				// System.out.print(p.getEstadoIni() + " - ");
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void flushDB() {
		serv.flushAll();
	}

	public void levantarServidores() {
		serv.publish("MOPPARALELISMO", "all-correr_LEVANTAR");
		serv.publish("MOPPARALELISMO", "all-correr_LEVANTAR");
	}

	public void matarServidores() {
		serv.publish("MOPPARALELISMO", "all-detener_procesos");
		setCantServidores("0");
	}

	@Override
	public void cargaTablaAuxiliar(int paso, Hashtable<ClaveDiscreta, InformacionValorPunto> tabla) {
		try {
			if (tabla != null) {
				if (tabla.size() > 0) {
					if (serv != null) {
						try {
							System.out.println("TAMAÑO TABLA AUXILIAR: " + tabla.size() + " PASO " + paso);
							serv.lpush("TAux" + Integer.toString(paso), toString(tabla));

						} catch (Exception e) {
							// TODO Auto-generated catch block

							System.out.println("TAMAÑO TABLA AUXILIAR: " + tabla.size() + " PASO " + paso);
							System.out.println(tabla.elements().toString());
							e.printStackTrace();
							System.exit(0);
						}
					} else
						System.exit(0);
				} else
					System.exit(0);
			} else
				System.exit(0);
		} catch (Exception e) {
			System.exit(0);
		}

	}

	@Override
	public void cargaTabla(int paso, Hashtable<ClaveDiscreta, InformacionValorPunto> tabla) {
		try {
			serv.set("T" + Integer.toString(paso), toString(tabla));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void cargaTablaControles(int paso, Hashtable<ClaveDiscreta, int[]> aCargar) {

		try {

			if (serv != null) {
				try {
					System.out.println("TAMAÑO TABLA AUXILIAR: " + aCargar.size() + " PASO " + paso);
					serv.lpush("TCDE" + Integer.toString(paso), toString(aCargar));

				} catch (Exception e) {
					// TODO Auto-generated catch block

					System.out.println("TAMAÑO TABLA AUXILIAR: " + aCargar.size() + " PASO " + paso);
					System.out.println(aCargar.elements().toString());
					e.printStackTrace();
					System.exit(0);
				}
			} else
				System.exit(0);
		} catch (Exception e) {
			System.exit(0);
		}

	}

	@Override
	public Hashtable<ClaveDiscreta, int[]> devuelveTablaControles(int paso, int cantPaquetes, int cantEstados) {

		Hashtable<ClaveDiscreta, int[]> retorno = new Hashtable<ClaveDiscreta, int[]>();
		int contarFragmentos = 0;
		boolean termine = false;
		while (!termine) {
			String fragmento = serv.lpop("TCDE" + Integer.toString(paso));
			if (fragmento != null) {
				Hashtable<ClaveDiscreta, int[]> frag;
				contarFragmentos++;

				try {
					frag = (Hashtable<ClaveDiscreta, int[]>) fromString(fragmento);
					// System.out.println("TAMAÑO FRAGMENTO: " + frag.size());
					retorno.putAll(frag);
					// System.out.println("frag " + contarFragmentos);
					// System.out.println("KEYS SIZE " + retorno.keySet().size());
					termine = retorno.keySet().size() == cantEstados;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		// System.out.println("FRAGMENTOS PASO " + paso + ": " + contarFragmentos);
		return retorno;
	}

	
	@Override
	public Hashtable<ClaveDiscreta, int[]> devuelveTablaControlesDE(int paso) {

		Hashtable<ClaveDiscreta, int[]> retorno;
		try {
			retorno = (Hashtable<ClaveDiscreta, int[]>) fromString(
					serv.lpop("TCDE" + Integer.toString(paso)));
			return retorno;
		} catch (ClassNotFoundException | IOException e) {
			retorno = devuelveTablaControlesDE(paso);
			e.printStackTrace();
		}
		return retorno;		
		
	}

	
	@Override
	public void guardarEscenario(int numero, DatosEPPUnEscenario esc) {
		try {
			serv.set("Esc" + Integer.toString(numero), toString(esc));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public DatosEPPUnEscenario levantarEscenario(int numero) {
		DatosEPPUnEscenario retorno = null;
		try {
			retorno = (DatosEPPUnEscenario) fromString(serv.get("Esc" + Integer.toString(numero)));
			System.out.println("Escenario " + numero);
			serv.del("Esc" + Integer.toString(numero));
			return retorno;
		} catch (ClassNotFoundException | IOException e) {
			// retorno = levantarEscenario(numero);
			e.printStackTrace();

		}
		System.out.println("Escenario " + numero);
		return retorno;
	}

	@Override
	public Hashtable<ClaveDiscreta, InformacionValorPunto> devuelveTablaAuxiliar(int paso, int cantPaquetes,
			int cantEstados) {

		Hashtable<ClaveDiscreta, InformacionValorPunto> retorno = new Hashtable<ClaveDiscreta, InformacionValorPunto>();
		int contarFragmentos = 0;
		boolean termine = false;
		while (!termine) {
			String fragmento = serv.lpop("TAux" + Integer.toString(paso));
			if (fragmento != null) {
				Hashtable<ClaveDiscreta, InformacionValorPunto> frag;
				contarFragmentos++;

				try {
					frag = (Hashtable<ClaveDiscreta, InformacionValorPunto>) fromString(fragmento);
					// System.out.println("TAMAÑO FRAGMENTO: " + frag.size());
					retorno.putAll(frag);
					// System.out.println("frag " + contarFragmentos);
					// System.out.println("KEYS SIZE " + retorno.keySet().size());
					termine = retorno.keySet().size() == cantEstados;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		// System.out.println("FRAGMENTOS PASO " + paso + ": " + contarFragmentos);
		return retorno;
	}

	@Override
	public Hashtable<ClaveDiscreta, InformacionValorPunto> devuelveTabla(int paso) {
		Hashtable<ClaveDiscreta, InformacionValorPunto> retorno;
		try {
			retorno = (Hashtable<ClaveDiscreta, InformacionValorPunto>) fromString(
					serv.get("T" + Integer.toString(paso)));
			return retorno;
		} catch (ClassNotFoundException | IOException e) {
			retorno = devuelveTabla(paso);
			e.printStackTrace();
		}
		return retorno;
	}

	@Override
	public void cargarPaquetesEscenarioAResolver(ArrayList<PaqueteEscenarios> paquetes) {
		Optimizador.prof.iniciarContador("piz-CargarPaquetesEscAResolver");
		for (PaqueteEscenarios p : paquetes) {
			try {
				serv.lpush("paquetesEscenariosAResolver", PizarronRedis.toString(p));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Optimizador.prof.pausarContador("piz-CargarPaquetesEscAResolver");
	}

	@Override
	public ArrayList<PaqueteEscenarios> obtenerPaquetesEscenariosResueltos() {
		Optimizador.prof.iniciarContador("piz-obtenerPaquetesEscResueltos");

		ArrayList<PaqueteEscenarios> res = new ArrayList<PaqueteEscenarios>();

		String s = serv.lpop("paquetesEscenariosResueltos");
		while (s != null) {
			try {
				PaqueteEscenarios p = (PaqueteEscenarios) PizarronRedis.fromString(s);
				res.add(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
			s = serv.lpop("paquetesEscenariosResueltos");
		}

		Optimizador.prof.pausarContador("piz-obtenerPaquetesEscResueltos");
		return res;
	}

	@Override
	public PaqueteEscenarios obtenerPaqueteEscenariosAResolver() {
		Optimizador.prof.iniciarContador("piz-obtenerPaquetesEscAResolver");
		try {
			String s = serv.lpop("paquetesEscenariosAResolver");
			if (s == null) {
				if (serv.lrange("paquetesEscenariosAResolver", 0, -1).size() > 0)
					System.out.println("DIO NULL Y HAY MÁS DE CERO!!!!");
				return null;
			}
			PaqueteEscenarios p = (PaqueteEscenarios) PizarronRedis.fromString(s);

			Optimizador.prof.pausarContador("piz-obtenerPaquetesEscAResolver");
			return p;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void cerrarPool() {
		this.jp.close();

	}

//	public static void main(String[] args) {    
//		//PizarronRedis pp = new PizarronRedis();
//		pp.devuelveTablaAuxiliar(7367, 1, 1);
//	}

}
