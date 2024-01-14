/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * MongoDBHandler is part of MOP.
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

package database;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;

import com.mongodb.internal.HexUtils;
import datatypes.*;
import com.mongodb.client.model.*;
import datatypesProcEstocasticos.DatosProcesoEstocastico;
import datatypesTiempo.DatosLineaTiempo;
import interfaz.Text;
import logica.CorridaHandler;

import org.bson.Document;
import persistencia.CargadorXML;
import persistencia.EscritorXML;
import tiempo.*;
import utilitarios.Constantes;
import utilitarios.Utilitarios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.connection.SocketSettings.builder;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MongoDBHandler {

	private static MongoDBHandler mongoDBHandler;
	private final MongoDatabase mopDB;

	public MongoClientSettings mongoOptions() {
		return MongoClientSettings.builder()
				.applyToSocketSettings(
						builder -> builder.applySettings(builder().connectTimeout(5000, MILLISECONDS).build()))
				.applyToClusterSettings(builder -> builder.serverSelectionTimeout(5000, MILLISECONDS).build()).build();

	}

	private MongoDBHandler() {
		// MongoClient mongoClient = MongoClients.create();

		// pc146014 - 172.16.8.59 // Nombre - IP pc donde se aloja la BD

		MongoClient mongoClient = MongoClients.create("mongodb://pc146014.corp.ute.com.uy:27017");

		// Se intento cambiar: Timed out after 30000 ms while waiting to connect.
		// Toma configuracion de otro lado
		/*
		 * MongoClientSettings settings = MongoClientSettings.builder()
		 * .retryWrites(true) .retryReads(true) .applyConnectionString(new
		 * ConnectionString("mongodb://pc146014.corp.ute.com.uy:27017"))
		 * .applyToSocketSettings(builder -> { builder.readTimeout(30000,
		 * TimeUnit.MILLISECONDS).connectTimeout(5000, TimeUnit.MILLISECONDS);})
		 * .build(); MongoClient mongoClient = MongoClients.create(settings);
		 */

		mopDB = mongoClient.getDatabase("mop");

////        ConnectionString connectionString = new ConnectionString(System.getProperty("mongodb.uri"));
//        ConnectionString connectionString = new ConnectionString("mongodb://127.0.0.1");
//        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
//        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
//        MongoClientSettings clientSettings = MongoClientSettings.builder()
//                .applyConnectionString(connectionString)
//                .codecRegistry(codecRegistry)
//                .build();

//        MongoClient mongoClient = MongoClients.create(clientSettings);
//        mopDB = mongoClient.getDatabase("mop");
//        MongoCollection<DatosProcesoEstocastico> listaPE = mopDB.getCollection("")
	}

	public static MongoDBHandler getInstance() {
		if (mongoDBHandler == null) {
			mongoDBHandler = new MongoDBHandler();
		}
		return mongoDBHandler;
	}

	// USERS
	public void addUser(String user, String pass, boolean isAdmin) throws NoSuchAlgorithmException {
		final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
		final byte[] hashbytes = digest.digest(pass.getBytes(StandardCharsets.UTF_8));
		String sha3Pass = HexUtils.toHex(hashbytes);
		MongoCollection<Document> usersColl = mopDB.getCollection("users");
		Document doc = new Document();
		doc.append("user", user);
		doc.append("pass", sha3Pass);
		if (isAdmin) {
			doc.append("bibliotecas", List.of("all"));
		} else {
			doc.append("bibliotecas", List.of(user));
		}
		usersColl.insertOne(doc);
	}

	public Boolean authUser(String user, String pass) throws NoSuchAlgorithmException {
		final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
		final byte[] hashbytes = digest.digest(pass.getBytes(StandardCharsets.UTF_8));
		String sha3Pass = HexUtils.toHex(hashbytes);
		MongoCollection<Document> usersColl = mopDB.getCollection("users");
		return usersColl.find(new Document().append("user", user).append("pass", sha3Pass)).iterator().hasNext();
	}

	public ArrayList<String> getBibliotecas(String user) {
		ArrayList<String> res = new ArrayList<>();
		MongoCollection<Document> usersColl = mopDB.getCollection("users");
		for (Document doc : usersColl.find(eq("user", user))) {
			res = (ArrayList<String>) doc.getList("bibliotecas", String.class);
			break;
		}
		if (res.contains("all")) {
			MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
			res.clear();
			participantesColl.distinct("biblioteca", String.class).map(res::add);
			for (String s : participantesColl.distinct("biblioteca", String.class)) {
				res.add(s);
			}
			if (res.isEmpty()) {
				res.add(user);
			}
		}
		return res;
	}

	// PE
	public ArrayList<DatosProcesoEstocastico> getPEList() {
		ArrayList<DatosProcesoEstocastico> res = new ArrayList<>();
		MongoCollection<Document> PEList = mopDB.getCollection("procesosEstocasticos");
//        MongoCollection<DatosProcesoEstocastico> PEList = mopDB.getCollection("procesosEstocasticos", DatosProcesoEstocastico.class);
		for (Document doc : PEList.find()) {
//        for(DatosProcesoEstocastico doc : PEList.find()){
			System.out.println(doc.entrySet());
//            System.out.println(doc);
			res.add(new DatosProcesoEstocastico((String) doc.get("nombre"), (String) doc.get("tipo"),
					(String) doc.get("tipoSoporte"), (String) doc.get("ruta"), (Boolean) doc.get("discretoExhaustivo"),
					(Boolean) doc.get("muestreado"), null, new Hashtable<>()));
		}
		System.out.println(res);
		return res;
	}

	// Participantes
	public ArrayList<HashMap<String, String>> getParticipantes(ArrayList<String> bibliotecas) {
		ArrayList<HashMap<String, String>> res = new ArrayList<>();
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(Filters.in("biblioteca", bibliotecas))) {
			HashMap<String, String> participanteMap = new HashMap<>();
			participanteMap.put("nombre", (String) doc.get("nombre"));
			participanteMap.put("tipo", (String) doc.get("tipo"));
			participanteMap.put("biblioteca", (String) doc.get("biblioteca"));
			res.add(participanteMap);
		}
//        System.out.println(res);
		return res;
	}

	public boolean existeParticipante(String nombre, String tipo, String biblioteca) {

		boolean ret = true;
		MongoCollection<Document> collection = mopDB.getCollection("participantes");

		Document criteria = new Document();
		criteria.append("nombre", nombre);
		criteria.append("tipo", tipo);
		criteria.append("biblioteca", biblioteca);

		Document doc = collection.find(criteria).first();

		if (doc == null) {
			ret = false;
		}

		return ret;
	}

	public boolean eliminarParticipante(String nombre, String tipo, String biblioteca) {

		boolean ret = true;
		MongoCollection<Document> collection = mopDB.getCollection("participantes");

		Document criteria = new Document();
		criteria.append("nombre", nombre);
		criteria.append("tipo", tipo);
		criteria.append("biblioteca", biblioteca);

		Document doc = collection.find(criteria).first();

		if (doc == null) {
			ret = false;
		} else {
			// TODO Aca se debe hacer la eliminacion logica y agregar el cambio al historial
			collection.deleteOne(doc);
		}

		return ret;
	}

	// EOLICOS
	public DatosEolicoCorrida getEolico(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosEolicoCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_EOLICO_TEXT), eq("biblioteca", biblioteca)))) {
//            res = new DatosEolicoCorrida((String) doc.get("nombre"),
//                    (String) doc.get("barra"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("cantModInst"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    getVariableAleatoria((Document)doc.get("factor")),
//                    (Integer) ((Document)doc.get("dispModulos")).get("cantModIni"),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("dispMedia"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("tMedioArreglo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Boolean) doc.get("salidaDetallada"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("mantProgramado"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoVariable"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()));
//            break;
		}
		return res;
	}

	private void actualizarParticipante(Document doc, String nombre, String tipo, String biblioteca) {
		// TODO: falta guardar el participante en el historial con fecha y usuario.
		// Mejora pro: en el historial solo poner los campos que se actualizaron, en
		// lugar del participante entero.

		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document queryObj = new Document();
		queryObj.append("nombre", nombre);
		queryObj.append("tipo", tipo);
		queryObj.append("biblioteca", biblioteca);

		participantesColl.replaceOne(queryObj, doc);
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ?
	public void setEolico(DatosEolicoCorrida datosEolicoCorrida, String biblioteca, LineaTiempo lt, boolean edita) {
		System.out.println("Insertando Participante Eólico en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("nombre", datosEolicoCorrida.getNombre());
		doc.append("tipo", Text.TIPO_EOLICO_TEXT);
		doc.append("barra", datosEolicoCorrida.getBarra());
		doc.append("cantModInst", getDocEvolucion(datosEolicoCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("potMin", getDocEvolucion(datosEolicoCorrida.getPotMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMax", getDocEvolucion(datosEolicoCorrida.getPotMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("factor", getDocVariableAleatoria(datosEolicoCorrida.getFactor()));
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosEolicoCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosEolicoCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosEolicoCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("dispModulos", docDispModulos);
		doc.append("salidaDetallada", datosEolicoCorrida.isSalDetallada());
		doc.append("mantProgramado",
				getDocEvolucion(datosEolicoCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("costoFijo", getDocEvolucion(datosEolicoCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("costoVariable", getDocEvolucion(datosEolicoCorrida.getCostoVariable(), Text.TIPO_DATO_DOUBLE, lt));

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosEolicoCorrida.getNombre(), Text.TIPO_EOLICO_TEXT, biblioteca);

		} else {
			participantesColl.insertOne(doc);
		}

		System.out.println("Participante Eólico insertado Correctamente.");
	}

	// SOLARES
	public DatosFotovoltaicoCorrida getFotovoltaico(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosFotovoltaicoCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_SOLAR_TEXT), eq("biblioteca", biblioteca)))) {
//            res = new DatosFotovoltaicoCorrida((String) doc.get("nombre"),
//                    (String) doc.get("barra"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("cantModInst"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    getVariableAleatoria((Document)doc.get("factor")),
//                    (Integer) ((Document)doc.get("dispModulos")).get("cantModIni"),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("dispMedia"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("tMedioArreglo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Boolean) doc.get("salidaDetallada"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("mantProgramado"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoVariable"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()));
			break;
		}
		return res;
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ?
	public void setFotovoltaico(DatosFotovoltaicoCorrida datosFotovoltaicoCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		System.out.println("Insertando Participante Solar en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("nombre", datosFotovoltaicoCorrida.getNombre());
		doc.append("tipo", Text.TIPO_SOLAR_TEXT);
		doc.append("barra", datosFotovoltaicoCorrida.getBarra());
		doc.append("cantModInst",
				getDocEvolucion(datosFotovoltaicoCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("potMin", getDocEvolucion(datosFotovoltaicoCorrida.getPotMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMax", getDocEvolucion(datosFotovoltaicoCorrida.getPotMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("factor", getDocVariableAleatoria(datosFotovoltaicoCorrida.getFactor()));
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosFotovoltaicoCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosFotovoltaicoCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosFotovoltaicoCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("dispModulos", docDispModulos);
		doc.append("salidaDetallada", datosFotovoltaicoCorrida.isSalDetallada());
		doc.append("mantProgramado",
				getDocEvolucion(datosFotovoltaicoCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("costoFijo", getDocEvolucion(datosFotovoltaicoCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("costoVariable",
				getDocEvolucion(datosFotovoltaicoCorrida.getCostoVariable(), Text.TIPO_DATO_DOUBLE, lt));

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosFotovoltaicoCorrida.getNombre(), Text.TIPO_SOLAR_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Solar isertado Correctamente.");
	}

	// TÉRMICOS
	public DatosTermicoCorrida getTermico(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosTermicoCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_TERMICO_TEXT), eq("biblioteca", biblioteca)))) {
//            res = new DatosTermicoCorrida(
//                    (String) doc.get("nombre"),
//                    (String) doc.get("barra"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("cantModInst"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (ArrayList<String>) doc.getList("listaCombustibles", Document.class).stream().map(document -> (String)document.get("nombre")).collect(Collectors.toList()),
//                    fromMap(doc.getList("listaCombustibles", Document.class).stream().collect(Collectors.toMap(document -> (String) document.get("nombre"), document -> (String) document.get("barra")))),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//
////HashTable<String, Evolucion<Double>>       TIPO DATO QUE DEBO MANDAR
//// getEvolucion(Document doc, String tipoDato, String tiempoInicial, DatosLineaTiempo dlt) {   METODO OBTENER EV DOCUMENTO
//                    fromMap(doc.getList("rendPotMax", Document.class).stream().collect(Collectors.toMap(document -> (String) document.get("nombre"), document -> (Evolucion<Double>) getEvolucion((Document) document.get("rendimientoMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())))),
//                    fromMap(doc.getList("rendPotMin", Document.class).stream().collect(Collectors.toMap(document -> (String) document.get("nombre"), document -> (Evolucion<Double>) getEvolucion((Document) document.get("rendimientoMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())))),
//
//                    (String) doc.get("flexibilidadMin"),
//                    (Integer) ((Document)doc.get("dispModulos")).get("cantModIni"),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("dispMedia"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("tMedioArreglo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Boolean) doc.get("salidaDetallada"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("mantProgramado"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoVariable"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()));

			Hashtable<String, Evolucion<String>> compGenMap = new Hashtable<>();
			((Document) doc.get("compsGenerales")).forEach((key, value) -> compGenMap.put(key,
					(Evolucion<String>) getEvolucion((Document) value, Text.TIPO_DATO_STRING,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())));
			res.setValoresComportamientos(compGenMap);
			break;
		}
		return res;
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ?
	public void setTermico(DatosTermicoCorrida datosTermicoCorrida, String biblioteca, LineaTiempo lt, boolean edita) {
		System.out.println("Insertando Participante Térmico en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_TERMICO_TEXT);
		Document docCompGenerales = new Document();
		for (var entry : datosTermicoCorrida.getValoresComportamientos().entrySet()) {
			docCompGenerales.append(entry.getKey(), getDocEvolucion(entry.getValue(), Text.TIPO_DATO_STRING, lt));
		}
		doc.append("compsGenerales", docCompGenerales);
		doc.append("nombre", datosTermicoCorrida.getNombre());
		doc.append("barra", datosTermicoCorrida.getBarra());
		doc.append("cantModInst", getDocEvolucion(datosTermicoCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		ArrayList<Document> listaCombustibles = new ArrayList<>();
		for (var entry : datosTermicoCorrida.getCombustiblesBarras().entrySet()) {
			Document docCombustibleBarra = new Document();
			docCombustibleBarra.append("nombre", entry.getKey());
			docCombustibleBarra.append("barra", entry.getValue());
			listaCombustibles.add(docCombustibleBarra);
		}
		doc.append("listaCombustibles", listaCombustibles);
		doc.append("potMin", getDocEvolucion(datosTermicoCorrida.getPotMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMax", getDocEvolucion(datosTermicoCorrida.getPotMax(), Text.TIPO_DATO_DOUBLE, lt));

		ArrayList<Document> listaRendimientosMinimos = new ArrayList<>();
//        for(var entry : datosTermicoCorrida.getRendimientosPotMin().entrySet()){
//            Document docCombustibleRendMin = new Document();
//            docCombustibleRendMin.append("nombre", entry.getKey());
//            docCombustibleRendMin.append("rendimientoMin", getDocEvolucion(entry.getValue(), Text.TIPO_DATO_DOUBLE, lt));
//            listaRendimientosMinimos.add(docCombustibleRendMin);
//        }
//        doc.append("rendPotMin", listaRendimientosMinimos);
//
//        ArrayList<Document> listaRendimientosMaximos = new ArrayList<>();
//        for(var entry : datosTermicoCorrida.getRendimientosPotMax().entrySet()){
//            Document docCombustibleRendMax = new Document();
//            docCombustibleRendMax.append("nombre", entry.getKey());
//            docCombustibleRendMax.append("rendimientoMax", getDocEvolucion(entry.getValue(), Text.TIPO_DATO_DOUBLE, lt));
//            listaRendimientosMaximos.add(docCombustibleRendMax);
//        }
//        doc.append("rendPotMax", listaRendimientosMaximos);

		doc.append("flexibilidadMin", datosTermicoCorrida.getFlexibilidadMin());
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosTermicoCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosTermicoCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosTermicoCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("dispModulos", docDispModulos);
		doc.append("salidaDetallada", datosTermicoCorrida.isSalDetallada());
		doc.append("mantProgramado",
				getDocEvolucion(datosTermicoCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("costoFijo", getDocEvolucion(datosTermicoCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("costoVariable", getDocEvolucion(datosTermicoCorrida.getCostoVariable(), Text.TIPO_DATO_DOUBLE, lt));

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosTermicoCorrida.getNombre(), Text.TIPO_TERMICO_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Térmico insertado Correctamente.");
	}

	// HIDRÁULICOS
	public DatosHidraulicoCorrida getHidraulico(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosHidraulicoCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(Filters.and(eq("nombre", nombre),
				eq("tipo", Text.TIPO_HIDRAULICO_TEXT), eq("biblioteca", biblioteca)))) {
//            res = new DatosHidraulicoCorrida(
//                        (String) doc.get("nombre"),
//                        (String) doc.get("barra"),
//                        (String) doc.get("rutaPQ"),
//                        (Evolucion<Integer>) getEvolucion((Document)doc.get("cantModInst"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document) doc.get("factorCompartir"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (ArrayList<String>) doc.get("generadoresAguasArriba"),
//                        (String) doc.get("generadorAguasAbajo"),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("potMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("potMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("rendPotMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("rendPotMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("volumenFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("qTur1Max"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        getVariableAleatoria((Document) doc.get("aporte")),
//                        (Integer) ((Document)doc.get("dispModulos")).get("cantModIni"),
//                        (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("dispMedia"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("tMedioArreglo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
////                    Evolucion<DatosPolinomio> fCoefEnerg,
//                        null,
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fCotaAguasAbajo")), new SentidoTiempo(1)),
//                        (Double) doc.get("saltoMinimo") ,
//                        (Double) doc.get("cotaInundacionAguasAbajo") ,
//                        (Double) doc.get("cotaInundacionAguasArriba") ,
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fQEroMin")), new SentidoTiempo(1)),
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fCoVo")), new SentidoTiempo(1)),
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fVoCo")), new SentidoTiempo(1)),
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fEvaporacion")), new SentidoTiempo(1)),
//                        (Evolucion<Double>) getEvolucion((Document) doc.get("coefEvaporacion"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fFiltracion")), new SentidoTiempo(1)),
//                        new EvolucionConstante<>(getFuncion((Document) doc.get("fQVerMax")), new SentidoTiempo(1)),
//                        fromMap(doc.getList("variablesEstado", Document.class).stream()
//                                .collect(Collectors.toMap(document -> getVariableEstado(document, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()).getNombre(),
//                                                          document -> getVariableEstado(document, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())))),
//                        (Double) doc.get("epsilonCaudalErogadoIteracion"),
//                        (Boolean) doc.get("salidaDetallada"),
//                        (Evolucion<Integer>) getEvolucion((Document)doc.get("mantProgramado"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("costoFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("costoVariable"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("volumenReservaEstrategica"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("valorMinReserva"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (boolean) doc.get("valorAplicaEnOptim"),
//                        (boolean) doc.get("hayReservaEstrategica"),
//                        (boolean) doc.get("vetimientoConstante"),
//                        (boolean) doc.get("hayVolumenObjetivoVertimiento"),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("volumenObjetivoVertimiento"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (boolean) doc.get("hayControldeCotasMinimas"),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("volumenControlMinimo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("penalidadControlMinimo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (boolean) doc.get("hayControldeCotasMaximas"),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("volumenControlMaximo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                        (Evolucion<Double>) getEvolucion((Document)doc.get("penalidadControlMaximo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()));

			Hashtable<String, Evolucion<String>> compGenMap = new Hashtable<>();
			((Document) doc.get("compsGenerales")).forEach((key, value) -> compGenMap.put(key,
					(Evolucion<String>) getEvolucion((Document) value, Text.TIPO_DATO_STRING,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())));
			res.setValoresComportamientos(compGenMap);

			break;
		}
		return res;
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ?
	public void setHidraulico(DatosHidraulicoCorrida datosHidraulicoCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		System.out.println("Insertando Participante Hidráulico en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_HIDRAULICO_TEXT);
		Document docCompGenerales = new Document();
		for (var entry : datosHidraulicoCorrida.getValoresComportamientos().entrySet()) {
			docCompGenerales.append(entry.getKey(), getDocEvolucion(entry.getValue(), Text.TIPO_DATO_STRING, lt));
		}
		doc.append("compsGenerales", docCompGenerales);
		doc.append("nombre", datosHidraulicoCorrida.getNombre());
		doc.append("barra", datosHidraulicoCorrida.getBarra());
		doc.append("cantModInst", getDocEvolucion(datosHidraulicoCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("factorCompartir",
				getDocEvolucion(datosHidraulicoCorrida.getFactorCompartir(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMin", getDocEvolucion(datosHidraulicoCorrida.getPotMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMax", getDocEvolucion(datosHidraulicoCorrida.getPotMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("rendPotMin", getDocEvolucion(datosHidraulicoCorrida.getRendPotMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("rendPotMax", getDocEvolucion(datosHidraulicoCorrida.getRendPotMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("generadoresAguasArriba", datosHidraulicoCorrida.getHidraulicosAguasArriba());
		doc.append("generadorAguasAbajo", datosHidraulicoCorrida.getHidraulicoAguasAbajo());
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosHidraulicoCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosHidraulicoCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosHidraulicoCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("dispModulos", docDispModulos);
		doc.append("salidaDetallada", datosHidraulicoCorrida.isSalDetallada());
		doc.append("mantProgramado",
				getDocEvolucion(datosHidraulicoCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("costoFijo", getDocEvolucion(datosHidraulicoCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("costoVariable",
				getDocEvolucion(datosHidraulicoCorrida.getCostoVariable(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("epsilonCaudalErogadoIteracion", datosHidraulicoCorrida.getEpsilonCaudalErogadoIteracion());
		doc.append("rutaPQ", datosHidraulicoCorrida.getRutaPQ());
//        Hashtable<Pair<Double,Double> , ArrayList<Recta>> funcionesPQ
		doc.append("fCotaAguasAbajo", getDocFuncion(datosHidraulicoCorrida.getfCoAA().getValor(instanteActual)));
		doc.append("volumenFijo", getDocEvolucion(datosHidraulicoCorrida.getVolFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("qTur1Max", getDocEvolucion(datosHidraulicoCorrida.getqTur1Max(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("saltoMinimo", datosHidraulicoCorrida.getSaltoMin());
		doc.append("fCoVo", getDocFuncion(datosHidraulicoCorrida.getfCoVo().getValor(instanteActual)));
		doc.append("fVoCo", getDocFuncion(datosHidraulicoCorrida.getfVoCo().getValor(instanteActual)));
		doc.append("coefEvaporacion",
				getDocEvolucion(datosHidraulicoCorrida.getCoefEvaporacion(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("fEvaporacion", getDocFuncion(datosHidraulicoCorrida.getfEvaporacion().getValor(instanteActual)));
		doc.append("fFiltracion", getDocFuncion(datosHidraulicoCorrida.getfFiltracion().getValor(instanteActual)));
		doc.append("cotaInundacionAguasAbajo", datosHidraulicoCorrida.getCotaInundacionAguasAbajo());
		doc.append("cotaInundacionAguasArriba", datosHidraulicoCorrida.getCotaInundacionAguasArriba());
		doc.append("fQEroMin", getDocFuncion(datosHidraulicoCorrida.getfQEroMin().getValor(instanteActual)));
		doc.append("fQVerMax", getDocFuncion(datosHidraulicoCorrida.getfQVerM().getValor(instanteActual)));
		doc.append("aporte", getDocVariableAleatoria(datosHidraulicoCorrida.getAporte()));
		doc.append("hayReservaEstrategica", datosHidraulicoCorrida.isHayReservaEstrategica());
		doc.append("volumenReservaEstrategica",
				getDocEvolucion(datosHidraulicoCorrida.getVolReservaEstrategica(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("valorMinReserva",
				getDocEvolucion(datosHidraulicoCorrida.getValorMinReserva(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("valorAplicaEnOptim", datosHidraulicoCorrida.isValorAplicaOptim());
		doc.append("vetimientoConstante", datosHidraulicoCorrida.isVertimientoConstante());
		doc.append("hayVolumenObjetivoVertimiento", datosHidraulicoCorrida.isHayVolObjVert());
		doc.append("volumenObjetivoVertimiento",
				getDocEvolucion(datosHidraulicoCorrida.getVolObjVert(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("variablesEstado", datosHidraulicoCorrida.getVarsEstado().values().stream()
				.map(val -> getDocVariableEstado(val, lt)).collect(Collectors.toList()));
		doc.append("hayControldeCotasMinimas", datosHidraulicoCorrida.isHayControldeCotasMinimas());
		doc.append("volumenControlMinimo",
				getDocEvolucion(datosHidraulicoCorrida.getVolumenControlMinimo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("penalidadControlMinimo",
				getDocEvolucion(datosHidraulicoCorrida.getPenalidadControlMinimo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("hayControldeCotasMaximas", datosHidraulicoCorrida.isHayControldeCotasMaximas());
		doc.append("volumenControlMaximo",
				getDocEvolucion(datosHidraulicoCorrida.getVolumenControlMaximo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("penalidadControlMaximo",
				getDocEvolucion(datosHidraulicoCorrida.getPenalidadControlMaximo(), Text.TIPO_DATO_DOUBLE, lt));

//        DatosPolinomio fRendimiento
//        Evolucion<DatosPolinomio> fCoefEnerg
//        Evolucion<Boolean> tieneCaudalMinEcol
//        Evolucion<ArrayList<Double>> caudalMinEcol
//        Evolucion<Double> penalizacionFaltanteCaudal

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosHidraulicoCorrida.getNombre(), Text.TIPO_HIDRAULICO_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Hidráulico isertado Correctamente.");
	}

	// CENTRALES ACUMULACIÓN // TODO: 02/08/2021 VE?
	public DatosAcumuladorCorrida getAcumulador(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosAcumuladorCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(Filters.and(eq("nombre", nombre),
				eq("tipo", Text.TIPO_ACUMULADOR_TEXT), eq("biblioteca", biblioteca)))) {
//            res = new DatosAcumuladorCorrida(
//                    (String) doc.get("nombre"),
//                    (String) doc.get("barra"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("cantModInst"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potAlmacMin"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("potAlmacMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("energAlmacMax"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("rendIny"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("rendAlmac"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Integer) ((Document)doc.get("dispModulos")).get("cantModIni"),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("dispMedia"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document) ((Document)doc.get("dispModulos")).get("tMedioArreglo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Boolean) doc.get("salidaDetallada"),
//                    (Evolucion<Integer>) getEvolucion((Document)doc.get("mantProgramado"), Text.TIPO_DATO_ENTERO, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoVariable"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
////            Hashtable<String,DatosVariableEstado> varsEstado,
//                    null,
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("factorUso"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("energIniPaso"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Boolean) doc.get("salidaDetallada"),
//                    (Evolucion<Boolean>) getEvolucion((Document)doc.get("hayPotObligatoria"), Text.TIPO_DATO_BOOLEAN, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoFallaPObligatoria"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    fromList(doc.getList("potObligatoria", Double.class)));

			Hashtable<String, Evolucion<String>> compGenMap = new Hashtable<>();
			((Document) doc.get("compsGenerales")).forEach((key, value) -> compGenMap.put(key,
					(Evolucion<String>) getEvolucion((Document) value, Text.TIPO_DATO_STRING,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())));
			res.setValoresComportamientos(compGenMap);
			break;
		}
		return res;
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ? VE?
	public void setAcumulador(DatosAcumuladorCorrida datosAcumuladorCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		System.out.println("Insertando Participante Acumulador en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_ACUMULADOR_TEXT);
		Document docCompGenerales = new Document();
		for (var entry : datosAcumuladorCorrida.getValoresComportamientos().entrySet()) {
			docCompGenerales.append(entry.getKey(), getDocEvolucion(entry.getValue(), Text.TIPO_DATO_STRING, lt));
		}
		doc.append("compsGenerales", docCompGenerales);
		doc.append("nombre", datosAcumuladorCorrida.getNombre());
		doc.append("barra", datosAcumuladorCorrida.getBarra());
		doc.append("cantModInst", getDocEvolucion(datosAcumuladorCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("factorUso", getDocEvolucion(datosAcumuladorCorrida.getFactorUso(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMin", getDocEvolucion(datosAcumuladorCorrida.getPotMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potMax", getDocEvolucion(datosAcumuladorCorrida.getPotMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potAlmacMin",
				getDocEvolucion(datosAcumuladorCorrida.getPotAlmacenadaMin(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potAlmacMax",
				getDocEvolucion(datosAcumuladorCorrida.getPotAlmacenadaMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("energAlmacMax",
				getDocEvolucion(datosAcumuladorCorrida.getEnergAlmacMax(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("rendIny", getDocEvolucion(datosAcumuladorCorrida.getRendIny(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("rendAlmac", getDocEvolucion(datosAcumuladorCorrida.getRendAlmac(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("energIniPaso",
				getDocEvolucion(datosAcumuladorCorrida.getEnergIniPaso(), Text.TIPO_DATO_DOUBLE, lt));
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosAcumuladorCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosAcumuladorCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosAcumuladorCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("dispModulos", docDispModulos);
		doc.append("salidaDetallada", datosAcumuladorCorrida.isSalDetallada());
		doc.append("mantProgramado",
				getDocEvolucion(datosAcumuladorCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		doc.append("costoFijo", getDocEvolucion(datosAcumuladorCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("costoVariable",
				getDocEvolucion(datosAcumuladorCorrida.getCostoVariable(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("hayPotObligatoria",
				getDocEvolucion(datosAcumuladorCorrida.getHayPotObligatoria(), Text.TIPO_DATO_BOOLEAN, lt));
		doc.append("costoFallaPObligatoria",
				getDocEvolucion(datosAcumuladorCorrida.getCostoFallaPotOblig(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("potObligatoria",
				Arrays.stream(datosAcumuladorCorrida.getPotOblig()).boxed().collect(Collectors.toList()));
//        Hashtable<String, DatosVariableEstado> varsEstado                           

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosAcumuladorCorrida.getNombre(), Text.TIPO_ACUMULADOR_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Acumulador isertado Correctamente.");
	}

	// DEMANDAS
	public DatosDemandaCorrida getDemanda(String nombre, String biblioteca) {
		DatosDemandaCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_DEMANDA_TEXT), eq("biblioteca", biblioteca)))) {
			res = new DatosDemandaCorrida((String) doc.get("nombre"), (String) doc.get("barra"),
					getVariableAleatoria((Document) doc.get("potActiva")), (Boolean) doc.get("salidaDetallada"));
			break;
		}
		return res;
	}

	public void setDemanda(DatosDemandaCorrida datosDemandaCorrida, String biblioteca, boolean edita) {
		System.out.println("Insertando Participante Demanda en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_DEMANDA_TEXT);
		doc.append("nombre", datosDemandaCorrida.getNombre());
		doc.append("barra", datosDemandaCorrida.getBarra());
		doc.append("potActiva", getDocVariableAleatoria(datosDemandaCorrida.getPotActiva()));
		doc.append("salidaDetallada", datosDemandaCorrida.isSalDetallada());

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosDemandaCorrida.getNombre(), Text.TIPO_DEMANDA_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Demanda isertado Correctamente.");
	}

	// FALLAS
	public DatosFallaEscalonadaCorrida getFalla(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosFallaEscalonadaCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_FALLA_TEXT), eq("biblioteca", biblioteca)))) {
			List<Integer> litDurForz = doc.getList("durMinForzamientos", Integer.class);
			int[] durForz = new int[litDurForz.size()];
			for (int i = 0; i < durForz.length; i++)
				durForz[i] = litDurForz.get(i);
			res = new DatosFallaEscalonadaCorrida(doc.getString("nombre"), null,
					fromMap(((Document) doc.get("compsGenerales")).entrySet().stream()
							.collect(Collectors.toMap(entry -> entry.getKey(),
									entry -> (Evolucion<String>) getEvolucion((Document) entry.getValue(),
											Text.TIPO_DATO_STRING, datosCorrida.getLineaTiempo().getTiempoInicial(),
											datosCorrida.getLineaTiempo())))),
					doc.getString("demanda"),
					(ArrayList<Pair<Double, Double>>) doc.getList("escalonesPorciento", String.class).stream()
							.map(str -> new Pair<>(Double.valueOf(str.split(";")[0].substring(2)),
									Double.valueOf(str.split(";")[1].split("\\)")[0])))
							.collect(Collectors.toList()),
					doc.getInteger("cantEscProgram"), durForz,
					fromMap(doc.getList("variablesEstado", Document.class).stream()
							.map(document -> getVariableEstado(document,
									datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()))
							.collect(Collectors.toMap(DatosVariableEstado::getNombre, Function.identity()))),
					fromMap(doc.getList("variablesControlDE", Document.class).stream()
							.map(document -> getVariableDcontrolDE(document,
									datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()))
							.collect(Collectors.toMap(DatosVariableControlDE::getNombre, Function.identity()))),
					doc.getBoolean("salidaDetallada"));
			break;
		}
		return res;
	}

	// TODO: 07/05/2021 compFalla ?
	public void setFalla(DatosFallaEscalonadaCorrida datosFallaEscalonadaCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		System.out.println("Insertando Participante Falla en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_FALLA_TEXT);
		Document docCompGenerales = new Document();
		for (var entry : datosFallaEscalonadaCorrida.getValsComps().entrySet()) {
			docCompGenerales.append(entry.getKey(), getDocEvolucion(entry.getValue(), Text.TIPO_DATO_STRING, lt));
		}
		doc.append("compsGenerales", docCompGenerales);
		doc.append("nombre", datosFallaEscalonadaCorrida.getNombre());
//        Evolucion<String> compFalla;
		doc.append("demanda", datosFallaEscalonadaCorrida.getDemanda());
		doc.append("escalonesPorciento", datosFallaEscalonadaCorrida.getEscalones().stream()
				.map(par -> "(" + par.first + ";" + par.second + ")").collect(Collectors.toList()));
		doc.append("cantEscProgram", datosFallaEscalonadaCorrida.getCantEscProgram());
		/*doc.append("durMinForzamientos", Arrays.stream(datosFallaEscalonadaCorrida.getDurMinForzSeg()).boxed()
				.map(val -> val / Constantes.SEGUNDOSXDIA).collect(Collectors.toList()));*/
		doc.append("variablesEstado", datosFallaEscalonadaCorrida.getVarsEstado().values().stream()
				.map(val -> getDocVariableEstado(val, lt)).collect(Collectors.toList()));
		doc.append("variablesControlDE", datosFallaEscalonadaCorrida.getVarsControlDE().values().stream()
				.map(val -> getDocVariableControlDE(val, lt)).collect(Collectors.toList()));
		doc.append("salidaDetallada", datosFallaEscalonadaCorrida.isSalDetallada());

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosFallaEscalonadaCorrida.getNombre(), Text.TIPO_FALLA_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Falla isertado Correctamente.");
	}

	// IMPO/EXPO
	public DatosImpoExpoCorrida getImpoExpo(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosImpoExpoCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_IMPOEXPO_TEXT), eq("biblioteca", biblioteca)))) {
//            res = new DatosImpoExpoCorrida(
//                    doc.getString("nombre"),
//                    doc.getString("barra"),
//                    doc.getString("pais"),
//                    doc.getString("tipoImpoExpo"),
//                    doc.getString("opCompraVenta"),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("costoFijo"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    doc.getInteger("cantBloques"),
//                    doc.getBoolean("salidaDetallada"),
//                    false,null,
//                    getVariableAleatoria((Document)doc.get("cmg")),
//                    (Evolucion<Double>) getEvolucion((Document)doc.get("factorEscalamiento"), Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
//                    getVariableAleatoria((Document)doc.get("uniforme")));
//            ArrayList<Evolucion<ArrayList<Double>>>  potEvol = (ArrayList<Evolucion<ArrayList<Double>>>) doc.getList("potEvol", Document.class).stream().map(val -> (Evolucion<ArrayList<Double>>)getEvolucion(val, Text.TIPO_DATO_LISTA_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())).collect(Collectors.toList());
//            ArrayList<Evolucion<ArrayList<Double>>>  preEvol = (ArrayList<Evolucion<ArrayList<Double>>>) doc.getList("preEvol", Document.class).stream().map(val -> (Evolucion<ArrayList<Double>>)getEvolucion(val, Text.TIPO_DATO_LISTA_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())).collect(Collectors.toList());
//            ArrayList<Evolucion<ArrayList<Double>>>  dispEvol = (ArrayList<Evolucion<ArrayList<Double>>>) doc.getList("dispEvol", Document.class).stream().map(val -> (Evolucion<ArrayList<Double>>)getEvolucion(val, Text.TIPO_DATO_LISTA_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())).collect(Collectors.toList());
//            ArrayList<DatosVariableAleatoria> datPrecio = (ArrayList<DatosVariableAleatoria>) doc.getList("datPrecio", Document.class).stream().map(this::getVariableAleatoria).collect(Collectors.toList());
//            ArrayList<DatosVariableAleatoria> datPotencia = (ArrayList<DatosVariableAleatoria>) doc.getList("datPotencia", Document.class).stream().map(this::getVariableAleatoria).collect(Collectors.toList());
//            ArrayList<Evolucion<DatosPolinomio>> poliPot = (ArrayList<Evolucion<DatosPolinomio>>) doc.getList("poliPot", Document.class).stream().map(val -> (Evolucion<DatosPolinomio>) new EvolucionConstante<>(getFuncion(val), new SentidoTiempo(1))).collect(Collectors.toList());
//            ArrayList<Evolucion<DatosPolinomio>> poliPre = (ArrayList<Evolucion<DatosPolinomio>>) doc.getList("poliPre", Document.class).stream().map(val -> (Evolucion<DatosPolinomio>) new EvolucionConstante<>(getFuncion(val), new SentidoTiempo(1))).collect(Collectors.toList());
//            ArrayList<Evolucion<DatosPolinomio>> poliDisp = (ArrayList<Evolucion<DatosPolinomio>>) doc.getList("poliDisp", Document.class).stream().map(val -> (Evolucion<DatosPolinomio>) new EvolucionConstante<>(getFuncion(val), new SentidoTiempo(1))).collect(Collectors.toList());

//            for(int i=0;i< potEvol.size();i++){
//                res.cargarBloque(datPrecio.get(i), datPotencia.get(i), poliPre.get(i),poliPot.get(i), poliDisp.get(i), potEvol.get(i), preEvol.get(i), dispEvol.get(i));
//            }
			break;
		}
		return res;
	}

	public void setImpoExpo(DatosImpoExpoCorrida datosImpoExpoCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		System.out.println("Insertando Participante Impo/Expo en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_IMPOEXPO_TEXT);
		doc.append("nombre", datosImpoExpoCorrida.getNombre());
		doc.append("barra", datosImpoExpoCorrida.getBarra());
		doc.append("pais", datosImpoExpoCorrida.getPais());
		doc.append("tipoImpoExpo", datosImpoExpoCorrida.getTipoImpoExpo());
		doc.append("opCompraVenta", datosImpoExpoCorrida.getOperacionCompraVenta());
		doc.append("costoFijo", getDocEvolucion(datosImpoExpoCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("cantBloques", datosImpoExpoCorrida.getCantBloques());
		doc.append("salidaDetallada", datosImpoExpoCorrida.isSalDetallada());
		doc.append("potEvol", datosImpoExpoCorrida.getPotEvol().stream()
				.map(val -> getDocEvolucion(val, Text.TIPO_DATO_LISTA_DOUBLE, lt)).collect(Collectors.toList()));
		doc.append("preEvol", datosImpoExpoCorrida.getPreEvol().stream()
				.map(val -> getDocEvolucion(val, Text.TIPO_DATO_LISTA_DOUBLE, lt)).collect(Collectors.toList()));
		doc.append("dispEvol", datosImpoExpoCorrida.getDispEvol().stream()
				.map(val -> getDocEvolucion(val, Text.TIPO_DATO_LISTA_DOUBLE, lt)).collect(Collectors.toList()));
		doc.append("datPrecio", datosImpoExpoCorrida.getDatPrecio().stream().map(this::getDocVariableAleatoria)
				.collect(Collectors.toList()));
		doc.append("datPotencia", datosImpoExpoCorrida.getDatPotencia().stream().map(this::getDocVariableAleatoria)
				.collect(Collectors.toList()));
		doc.append("poliPot", datosImpoExpoCorrida.getPoliPot().stream()
				.map(val -> getDocFuncion(val.getValor(instanteActual))).collect(Collectors.toList()));
		doc.append("poliPre", datosImpoExpoCorrida.getPoliPre().stream()
				.map(val -> getDocFuncion(val.getValor(instanteActual))).collect(Collectors.toList()));
		doc.append("poliDisp", datosImpoExpoCorrida.getPoliDisp().stream()
				.map(val -> getDocFuncion(val.getValor(instanteActual))).collect(Collectors.toList()));
		doc.append("cmg", getDocVariableAleatoria(datosImpoExpoCorrida.getDatCMg()));
		doc.append("factorEscalamiento",
				getDocEvolucion(datosImpoExpoCorrida.getFactorEscalamiento(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("uniforme", getDocVariableAleatoria(datosImpoExpoCorrida.getDatUniforme()));

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosImpoExpoCorrida.getNombre(), Text.TIPO_IMPOEXPO_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Impo/Expo isertado Correctamente.");
	}

	// COMBUSTIBLES
	public DatosCombustibleCorrida getCombustible(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosCombustibleCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(Filters.and(eq("nombre", nombre),
				eq("tipo", Text.TIPO_COMBUSTIBLE_TEXT), eq("biblioteca", biblioteca)))) {
			res = new DatosCombustibleCorrida(doc.getString("nombre"), doc.getString("unidad"), doc.getDouble("pci"),
					doc.getDouble("densidad"), doc.getBoolean("salidaDetallada"));
			res.setRed(getRed((Document) doc.get("red"), datosCorrida));
			break;
		}
		return res;
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ?
	public void setCombustible(DatosCombustibleCorrida datosCombustibleCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		System.out.println("Insertando Participante Combustible en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_COMBUSTIBLE_TEXT);
		doc.append("nombre", datosCombustibleCorrida.getNombre());
		doc.append("unidad", datosCombustibleCorrida.getUnidad());
		doc.append("pci", datosCombustibleCorrida.getPciPorUnidad());
		doc.append("densidad", datosCombustibleCorrida.getDensidad());
		doc.append("salidaDetallada", datosCombustibleCorrida.isSalDetallada());
		doc.append("red", getDocRed(datosCombustibleCorrida.getRed(), lt));

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosCombustibleCorrida.getNombre(), Text.TIPO_COMBUSTIBLE_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Combustible isertado Correctamente.");
	}

	public DatosRedCombustibleCorrida getRed(Document doc, DatosCorrida datosCorrida) {
		DatosRedCombustibleCorrida res = new DatosRedCombustibleCorrida();
		res.setNombre(doc.getString("nombre"));
//            Hashtable<String,Evolucion<String>> valoresComportamiento;
//            ArrayList<String> barrasUtilizadas;
//            ArrayList<String> ramasUtilizadas;
//            ArrayList<String> tanquesUtilizados;
//            ArrayList<String> contratosUtilizados;
		res.setBarras((ArrayList<DatosBarraCombCorrida>) doc.getList("listaBarrasComb", Document.class).stream()
				.map(this::getBarraCombustible).collect(Collectors.toList()));
		res.setDuctos((ArrayList<DatosDuctoCombCorrida>) doc.getList("listaDuctosComb", Document.class).stream()
				.map(val -> getDuctoCombustible(val, datosCorrida)).collect(Collectors.toList()));
//            ArrayList<DatosTanqueCombustibleCorrida> tanques;
		res.setContratos((ArrayList<DatosContratoCombustibleCorrida>) doc
				.getList("listaContratosCombustibleCanioSimple", Document.class).stream()
				.map(val -> getContratoCombustible(val, datosCorrida)).collect(Collectors.toList()));

		Hashtable<String, Evolucion<String>> compGenMap = new Hashtable<>();
		((Document) doc.get("compsGenerales")).forEach((key, value) -> compGenMap.put(key,
				(Evolucion<String>) getEvolucion((Document) value, Text.TIPO_DATO_STRING,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo())));
		res.setValoresComportamiento(compGenMap);
		return res;
	}

	public Document getDocRed(DatosRedCombustibleCorrida datosRedCoombustibleCorrida, LineaTiempo lt) {
		Document doc = new Document();
		Document docCompGenerales = new Document();
		for (var entry : datosRedCoombustibleCorrida.getValoresComportamiento().entrySet()) {
			docCompGenerales.append(entry.getKey(), getDocEvolucion(entry.getValue(), Text.TIPO_DATO_STRING, lt));
		}
		doc.append("compsGenerales", docCompGenerales);
//        doc.append("nombre", datosRedCoombustibleCorrida.getNombre());
//        ArrayList<String> barrasUtilizadas;
//        ArrayList<String> ramasUtilizadas;
//        ArrayList<String> tanquesUtilizados;
//        ArrayList<String> contratosUtilizados;
		doc.append("listaBarrasComb", datosRedCoombustibleCorrida.getBarras().stream().map(this::getDocBarraCombustible)
				.collect(Collectors.toList()));
		doc.append("listaDuctosComb", datosRedCoombustibleCorrida.getDuctos().stream()
				.map(val -> getDocDuctoCombustible(val, lt)).collect(Collectors.toList()));
//        ArrayList<DatosTanqueCombustibleCorrida> tanques;
//        ArrayList<DatosContratoCombustibleCorrida> contratos;
		doc.append("listaContratosCombustibleCanioSimple", datosRedCoombustibleCorrida.getContratos().stream()
				.map(val -> getDocContratoCombustible(val, lt)).collect(Collectors.toList()));
		return doc;
	}

	private DatosBarraCombCorrida getBarraCombustible(Document doc) {
		return new DatosBarraCombCorrida(doc.get("barraComb", Document.class).getString("nombre"));
	}

	private Document getDocBarraCombustible(DatosBarraCombCorrida datosBarraCombCorrida) {
		Document doc = new Document();
		Document docBC = new Document();
		docBC.append("nombre", datosBarraCombCorrida.getNombre());
		doc.append("barraComb", docBC);
		return doc;
	}

	private DatosDuctoCombCorrida getDuctoCombustible(Document doc, DatosCorrida datosCorrida) {
		Document docDC = doc.get("ductoComb", Document.class);
		return new DatosDuctoCombCorrida(docDC.getString("nombre"),
				(Evolucion<Integer>) getEvolucion(docDC.get("cantModInst", Document.class), Text.TIPO_DATO_ENTERO,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				docDC.getString("barra1"), docDC.getString("barra2"),
				(Evolucion<Double>) getEvolucion(docDC.get("capacidad12", Document.class), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion(docDC.get("capacidad21", Document.class), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion(docDC.get("perdidas12", Document.class), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion(docDC.get("perdidas21", Document.class), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Integer) ((Document) docDC.get("dispModulos")).get("cantModIni"),
				(Evolucion<Double>) getEvolucion((Document) ((Document) docDC.get("dispModulos")).get("dispMedia"),
						Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(),
						datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion(
						(Document) ((Document) docDC.get("dispModulos")).get("tMedioArreglo"), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				docDC.getBoolean("salidaDetallada"),
				(Evolucion<Integer>) getEvolucion((Document) docDC.get("mantProgramado"), Text.TIPO_DATO_ENTERO,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion((Document) docDC.get("costoFijo"), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()));
	}

	private Document getDocDuctoCombustible(DatosDuctoCombCorrida datosDuctoCombCorrida, LineaTiempo lt) {
		Document doc = new Document();
		Document docDC = new Document();
		docDC.append("nombre", datosDuctoCombCorrida.getNombre());
		docDC.append("cantModInst", getDocEvolucion(datosDuctoCombCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosDuctoCombCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosDuctoCombCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosDuctoCombCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		docDC.append("dispModulos", docDispModulos);
		docDC.append("mantProgramado",
				getDocEvolucion(datosDuctoCombCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		docDC.append("costoFijo", getDocEvolucion(datosDuctoCombCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		docDC.append("barra1", datosDuctoCombCorrida.getBarra1());
		docDC.append("barra2", datosDuctoCombCorrida.getBarra2());
		docDC.append("capacidad12", getDocEvolucion(datosDuctoCombCorrida.getCapacidad12(), Text.TIPO_DATO_DOUBLE, lt));
		docDC.append("capacidad21", getDocEvolucion(datosDuctoCombCorrida.getCapacidad21(), Text.TIPO_DATO_DOUBLE, lt));
		docDC.append("perdidas12", getDocEvolucion(datosDuctoCombCorrida.getPerdidas12(), Text.TIPO_DATO_DOUBLE, lt));
		docDC.append("perdidas21", getDocEvolucion(datosDuctoCombCorrida.getPerdidas21(), Text.TIPO_DATO_DOUBLE, lt));
		docDC.append("salidaDetallada", datosDuctoCombCorrida.isSalDetallada());
		doc.append("ductoComb", docDC);
		return doc;
	}

	private DatosContratoCombustibleCorrida getContratoCombustible(Document doc, DatosCorrida datosCorrida) {
		Document docCC = doc.get("contratoCanioSimple", Document.class);
		return new DatosContratoCombustibleCorrida(docCC.getString("nombre"), docCC.getString("barra"),
				docCC.getString("combustible"),
				(Evolucion<Integer>) getEvolucion(docCC.get("cantModInst", Document.class), Text.TIPO_DATO_ENTERO,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Integer) ((Document) docCC.get("dispModulos")).get("cantModIni"),
				(Evolucion<Double>) getEvolucion((Document) ((Document) docCC.get("dispModulos")).get("dispMedia"),
						Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(),
						datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion((Document) ((Document) docCC.get("dispModulos")).get("tMedioArreglo"),
						Text.TIPO_DATO_DOUBLE, datosCorrida.getLineaTiempo().getTiempoInicial(),
						datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion(docCC.get("caudalMax", Document.class), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion(docCC.get("precioComb", Document.class), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				docCC.getBoolean("salidaDetallada"),
				(Evolucion<Integer>) getEvolucion((Document) docCC.get("mantProgramado"), Text.TIPO_DATO_ENTERO,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
				(Evolucion<Double>) getEvolucion((Document) docCC.get("costoFijo"), Text.TIPO_DATO_DOUBLE,
						datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()));
	}

	private Document getDocContratoCombustible(DatosContratoCombustibleCorrida datosContratoCombustibleCorrida,
			LineaTiempo lt) {
		Document doc = new Document();
		Document docCC = new Document();
		docCC.append("nombre", datosContratoCombustibleCorrida.getNombre());
		docCC.append("barra", datosContratoCombustibleCorrida.getBarra());
		docCC.append("combustible", datosContratoCombustibleCorrida.getComb());
		docCC.append("cantModInst",
				getDocEvolucion(datosContratoCombustibleCorrida.getCantModInst(), Text.TIPO_DATO_ENTERO, lt));
		Document docDispModulos = new Document();
		docDispModulos.append("cantModIni", datosContratoCombustibleCorrida.getCantModIni());
		docDispModulos.append("dispMedia",
				getDocEvolucion(datosContratoCombustibleCorrida.getDispMedia(), Text.TIPO_DATO_DOUBLE, lt));
		docDispModulos.append("tMedioArreglo",
				getDocEvolucion(datosContratoCombustibleCorrida.gettMedioArreglo(), Text.TIPO_DATO_DOUBLE, lt));
		docCC.append("dispModulos", docDispModulos);
		docCC.append("mantProgramado",
				getDocEvolucion(datosContratoCombustibleCorrida.getMantProgramado(), Text.TIPO_DATO_ENTERO, lt));
		docCC.append("costoFijo",
				getDocEvolucion(datosContratoCombustibleCorrida.getCostoFijo(), Text.TIPO_DATO_DOUBLE, lt));
		docCC.append("caudalMax",
				getDocEvolucion(datosContratoCombustibleCorrida.getCaudalMax(), Text.TIPO_DATO_DOUBLE, lt));
		docCC.append("precioComb",
				getDocEvolucion(datosContratoCombustibleCorrida.getPrecioComb(), Text.TIPO_DATO_DOUBLE, lt));
		docCC.append("salidaDetallada", datosContratoCombustibleCorrida.isSalDetallada());
		doc.append("contratoCanioSimple", docCC);
		return doc;
	}

	// IMPACTOS AMBIENTALES
	public DatosImpactoCorrida getImpactoAmbiental(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosImpactoCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(
				Filters.and(eq("nombre", nombre), eq("tipo", Text.TIPO_IMPACTO_TEXT), eq("biblioteca", biblioteca)))) {
			res = new DatosImpactoCorrida(doc.getString("nombre"),
					(Evolucion<Boolean>) getEvolucion(doc.get("activo", Document.class), Text.TIPO_DATO_BOOLEAN,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					(Evolucion<Double>) getEvolucion(doc.get("costo", Document.class), Text.TIPO_DATO_DOUBLE,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					(Evolucion<Double>) getEvolucion(doc.get("limite", Document.class), Text.TIPO_DATO_DOUBLE,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					doc.getBoolean("porPoste"),
					(ArrayList<String>) doc.getList("participantesInvolucrados", String.class),
					Text.TIPOS_IMPACTO_BY_STRING.get(doc.getString("tipoImpacto")), doc.getBoolean("porUnidadDeTiempo"),
					doc.getBoolean("salidaDetallada"));
			break;
		}
		return res;
	}

	public void setImpactoAmbiental(DatosImpactoCorrida datosImpactoCorrida, String biblioteca, LineaTiempo lt,
			boolean edita) {
		System.out.println("Insertando Participante Impacto Ambiental en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_IMPACTO_TEXT);
		doc.append("nombre", datosImpactoCorrida.getNombre());
		doc.append("activo", getDocEvolucion(datosImpactoCorrida.getActivo(), Text.TIPO_DATO_BOOLEAN, lt));
		doc.append("costo", getDocEvolucion(datosImpactoCorrida.getCostoUnit(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("limite", getDocEvolucion(datosImpactoCorrida.getLimite(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("porPoste", datosImpactoCorrida.isPorPoste());
		doc.append("participantesInvolucrados", datosImpactoCorrida.getInvolucrados());
		doc.append("tipoImpacto", Text.TIPOS_IMPACTO_BY_INT.get(datosImpactoCorrida.getTipoImpacto()));
		doc.append("porUnidadDeTiempo", datosImpactoCorrida.isPorUnidadTiempo());
		doc.append("salidaDetallada", datosImpactoCorrida.isSalDetallada());

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));

		if (edita) {
			actualizarParticipante(doc, datosImpactoCorrida.getNombre(), Text.TIPO_IMPACTO_TEXT, biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Impacto Ambiental isertado Correctamente.");
	}

	// CONTRATOS ENERGÍA
	public DatosContratoEnergiaCorrida getContratoEnergia(String nombre, String biblioteca, DatosCorrida datosCorrida) {
		DatosContratoEnergiaCorrida res = null;
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		for (Document doc : participantesColl.find(Filters.and(eq("nombre", nombre),
				eq("tipo", Text.TIPO_CONTRATO_ENERGIA_TEXT), eq("biblioteca", biblioteca)))) {
			res = new DatosContratoEnergiaCorrida(doc.getString("nombre"),
					(ArrayList<String>) doc.getList("participantesInvolucrados", String.class),
					(Evolucion<Double>) getEvolucion((Document) doc.get("precioBase"), Text.TIPO_DATO_DOUBLE,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					(Evolucion<Double>) getEvolucion((Document) doc.get("energiaBase"), Text.TIPO_DATO_DOUBLE,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					doc.getString("fechaInicial"), doc.getInteger("cantAnios"), doc.getDouble("energiaInicial"),
					doc.getString("tipoContrato"),
					(Evolucion<Double>) getEvolucion((Document) doc.get("cotaInf"), Text.TIPO_DATO_DOUBLE,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					(Evolucion<Double>) getEvolucion((Document) doc.get("cotaSup"), Text.TIPO_DATO_DOUBLE,
							datosCorrida.getLineaTiempo().getTiempoInicial(), datosCorrida.getLineaTiempo()),
					doc.getBoolean("salidaDetallada"));
			break;
		}
		return res;
	}

	// TODO: 07/05/2021 unidades ? tipo en dispMod ?
	public void setContratoEnergia(DatosContratoEnergiaCorrida datosContratoEnergiaCorrida, String biblioteca,
			LineaTiempo lt, boolean edita) {
		System.out.println("Insertando Participante Contrato Energía en la Base de Datos.......");
		MongoCollection<Document> participantesColl = mopDB.getCollection("participantes");
		Document doc = new Document();
		doc.append("biblioteca", biblioteca);
		doc.append("tipo", Text.TIPO_CONTRATO_ENERGIA_TEXT);
		doc.append("nombre", datosContratoEnergiaCorrida.getNombre());
		doc.append("participantesInvolucrados", datosContratoEnergiaCorrida.getInvolucrados());
		doc.append("precioBase",
				getDocEvolucion(datosContratoEnergiaCorrida.getPrecioBase(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("energiaBase",
				getDocEvolucion(datosContratoEnergiaCorrida.getEnergiaBase(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("fechaInicial", datosContratoEnergiaCorrida.getFechaInicial());
		doc.append("cantAnios", datosContratoEnergiaCorrida.getCantAnios());
		doc.append("energiaInicial", datosContratoEnergiaCorrida.getEnergiaInicial());
		doc.append("cotaInf", getDocEvolucion(datosContratoEnergiaCorrida.getCotaInf(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("cotaSup", getDocEvolucion(datosContratoEnergiaCorrida.getCotaSup(), Text.TIPO_DATO_DOUBLE, lt));
		doc.append("tipoContrato", datosContratoEnergiaCorrida.getTipo());
		doc.append("salidaDetallada", datosContratoEnergiaCorrida.isSalDetallada());

		doc.append("info", Arrays.asList(new Document().append("user", "admin").append("date", LocalDateTime.now())));
		if (edita) {
			actualizarParticipante(doc, datosContratoEnergiaCorrida.getNombre(), Text.TIPO_CONTRATO_ENERGIA_TEXT,
					biblioteca);
		} else {
			participantesColl.insertOne(doc);
		}
		System.out.println("Participante Contrato Energía isertado Correctamente.");
	}

	// evoluciones
	private Evolucion<?> getEvolucion(Document doc, String tipoDato, String tiempoInicial, DatosLineaTiempo dlt) {
		Evolucion<?> res = null;
		Document evDoc = (Document) doc.get("ev");
		String tipoEV = (String) evDoc.get("tipo");
		Object valor = evDoc.get("valor");
		if (tipoEV.equalsIgnoreCase("const")) {
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_STRING)) {
				res = new EvolucionConstante<>((String) valor, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_BOOLEAN)) {
				res = new EvolucionConstante<>((Boolean) valor, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_DOUBLE)) {
				res = new EvolucionConstante<>((Double) valor, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ENTERO)) {
				res = new EvolucionConstante<>((Integer) valor, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_PAR_DOUBLE)) {
				res = new EvolucionConstante<>((ArrayList<Pair<Double, Double>>) valor, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_DOUBLE)) {
				res = new EvolucionConstante<>((ArrayList<Double>) valor, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ARRAY_DOUBLE)) {
				ArrayList<Double> listaValores = (ArrayList<Double>) valor;
				Double[] arrayValores = new Double[listaValores.size()];
				for (int i = 0; i < listaValores.size(); i++)
					arrayValores[i] = listaValores.get(i);
				res = new EvolucionConstante<>(arrayValores, dlt.getSentido());
			}
		} else if (tipoEV.equalsIgnoreCase("porInstantes")) {
			DateFormat df = new SimpleDateFormat("dd MM yyyy"); // Se parsea un string con este formato a
																// GregorianCalendar

			GregorianCalendar fechaInicial = Utilitarios.stringToGregorianCalendar(tiempoInicial, "dd MM yyyy");

			GregorianCalendar fechaReferencia = new GregorianCalendar();

			ArrayList<Pair<String, String>> listaPorInstantes = CargadorXML.generarListaPorInstantes((String) valor,
					",");

			ArrayList<Pair<Long, String>> listaInstantesEnteros = new ArrayList<>();

			for (int i = 0; i < listaPorInstantes.size(); i++) {
				DateFormat dfi = null;
				if (listaPorInstantes.get(i).first.length() <= " dd MM yyyy ".length()) {
					dfi = df;
				} else {
					dfi = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
				}
				try {
					Date d = dfi.parse(listaPorInstantes.get(i).first);
					fechaReferencia.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				long instante = CargadorXML.restarFechas(fechaReferencia, fechaInicial);
				listaInstantesEnteros.add(new Pair<>(instante, listaPorInstantes.get(i).second));
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_STRING)) {
				Hashtable<Long, String> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, p.second);
				}
				res = new EvolucionPorInstantes<>(vals, "", dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_DOUBLE)) {
				Hashtable<Long, Double> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Double.parseDouble(p.second));
				}
				res = new EvolucionPorInstantes<>(vals, 0.0, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ENTERO)) {
				Hashtable<Long, Integer> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Integer.parseInt(p.second));
				}
				res = new EvolucionPorInstantes<>(vals, 0, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_BOOLEAN)) {
				Hashtable<Long, Boolean> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Boolean.parseBoolean(p.second));
				}
				res = new EvolucionPorInstantes<>(vals, false, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_PAR_DOUBLE)) {
				Hashtable<Long, ArrayList<Pair<Double, Double>>> vals = new Hashtable<>();
				int cantLista = 0;
				for (Pair<Long, String> p : listaInstantesEnteros) {
					ArrayList<Double> ad = CargadorXML.generarListaDoubles(p.second, " ");
					vals.put(p.first, CargadorXML.generarListaParesDouble(p.second));
					cantLista = ad.size();
				}
				ArrayList<Pair<Double, Double>> ini = new ArrayList<>();
				for (int i = 0; i < cantLista; i++) {
					ini.add(new Pair<>(0.0, 0.0));
				}
				res = new EvolucionPorInstantes<>(vals, ini, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_DOUBLE)) {
				Hashtable<Long, ArrayList<Double>> vals = new Hashtable<>();
				int cantLista = 0;
				for (Pair<Long, String> p : listaInstantesEnteros) {
					ArrayList<Double> ad = CargadorXML.generarListaDoubles(p.second, " ");
					vals.put(p.first, ad);
					cantLista = ad.size();
				}
				ArrayList<Double> ini = new ArrayList<>();
				for (int i = 0; i < cantLista; i++) {
					ini.add(0.0);
				}
				res = new EvolucionPorInstantes<>(vals, ini, dlt.getSentido());
			}
		} else if (tipoEV.equalsIgnoreCase("periodica")) {
			GregorianCalendar fechaInicial = Utilitarios.stringToGregorianCalendar(tiempoInicial, "dd MM yyyy");

			String defPeriodo = evDoc.getString("definicionPeriodo");
			EvolucionPorInstantes evInst = null;
			ArrayList<Pair<String, String>> listaPorInstantes = CargadorXML.generarListaPorInstantes(defPeriodo, ",");
			ArrayList<Pair<Long, String>> listaInstantesEnteros = new ArrayList<>();
			GregorianCalendar fechaReferencia = new GregorianCalendar();
			DateFormat df = new SimpleDateFormat("dd MM yyyy"); // Se parsea un string con este formato a
																// GregorianCalendar
			for (Pair<String, String> listaPorInstante : listaPorInstantes) {
				DateFormat dfi;
				if (listaPorInstante.first.length() <= " dd MM yyyy ".length()) {
					dfi = df;
				} else {
					dfi = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
				}
				try {
					Date d = dfi.parse(listaPorInstante.first);
					fechaReferencia.setTime(d);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				Long instante = CargadorXML.restarFechas(fechaReferencia, fechaInicial);
				listaInstantesEnteros.add(new Pair<>(instante, listaPorInstante.second));
			}

			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_STRING)) {
				Hashtable<Long, String> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, p.second);
				}
				evInst = new EvolucionPorInstantes<>(vals, "", dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_DOUBLE)) {
				Hashtable<Long, Double> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Double.parseDouble(p.second));
				}
				evInst = new EvolucionPorInstantes<>(vals, 0.0, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ENTERO)) {
				Hashtable<Long, Integer> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Integer.parseInt(p.second));
				}
				evInst = new EvolucionPorInstantes<>(vals, 0, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_BOOLEAN)) {
				Hashtable<Long, Boolean> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, Boolean.parseBoolean(p.second));
				}
				evInst = new EvolucionPorInstantes<>(vals, false, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_PAR_DOUBLE)) {
				Hashtable<Long, ArrayList<Pair<Double, Double>>> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, CargadorXML.generarListaParesDouble(p.second));
				}
				evInst = new EvolucionPorInstantes<>(vals, null, dlt.getSentido());
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_DOUBLE)) {
				Hashtable<Long, ArrayList<Double>> vals = new Hashtable<>();
				for (Pair<Long, String> p : listaInstantesEnteros) {
					vals.put(p.first, CargadorXML.generarListaDoubles(p.second, " "));
				}
				evInst = new EvolucionPorInstantes<>(vals, new ArrayList<>(), dlt.getSentido());
			}

			String periodo = evDoc.getString("periodo");
			int peri = Calendar.YEAR; // por defecto
			if (periodo.equalsIgnoreCase("año"))
				peri = Calendar.YEAR;
			if (periodo.equalsIgnoreCase("mes"))
				peri = Calendar.MONTH;
			if (periodo.equalsIgnoreCase("semana"))
				peri = Calendar.WEEK_OF_YEAR;
			if (periodo.equalsIgnoreCase("dia"))
				peri = Calendar.DAY_OF_YEAR;

			Integer cantPeriodo = evDoc.getInteger("cantPeriodo");

			res = new EvolucionPeriodica<Double>(fechaInicial, evInst, peri, cantPeriodo, dlt.getSentido());
		}
		return res;
	}

	private Document getDocEvolucion(Evolucion<?> evolucion, String tipoDato, LineaTiempo lt) {
		Document doc = new Document();
		Document evDoc = new Document();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		if (evolucion instanceof EvolucionConstante) {
			evDoc.append("tipo", "const");
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ARRAY_DOUBLE)) {
				evDoc.append("valor",
						Arrays.stream((Double[]) evolucion.getValor(instanteActual)).collect(Collectors.toList()));
			} else {
				evDoc.append("valor", evolucion.getValor(instanteActual));
			}
		} else if (evolucion instanceof EvolucionPorInstantes) {
			evDoc.append("tipo", "porInstantes");
			String valor = "";
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_STRING)) {
				EvolucionPorInstantes<String> evs = (EvolucionPorInstantes<String>) evolucion;
				Hashtable<Long, String> valorizador = evs.getValorizador();
				valor = "";

				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {

					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					String val = valorizador.get(p);
					valor += fechaest + ";" + val + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_DOUBLE)) {
				EvolucionPorInstantes<Double> evs = (EvolucionPorInstantes<Double>) evolucion;
				Hashtable<Long, Double> valorizador = evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					Double val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}

			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ENTERO)) {
				EvolucionPorInstantes<Integer> evs = (EvolucionPorInstantes<Integer>) evolucion;
				Hashtable<Long, Integer> valorizador = evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					Integer val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_BOOLEAN)) {
				EvolucionPorInstantes<Boolean> evs = (EvolucionPorInstantes<Boolean>) evolucion;
				Hashtable<Long, Boolean> valorizador = evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					Boolean val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_PAR_DOUBLE)) {
				EvolucionPorInstantes<ArrayList<Pair<Double, Double>>> evs = (EvolucionPorInstantes<ArrayList<Pair<Double, Double>>>) evolucion;
				Hashtable<Long, ArrayList<Pair<Double, Double>>> valorizador = evs.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					ArrayList<Pair<Double, Double>> val = valorizador.get(p);
					valor += fechaest + ";" + Arrays.deepToString(val.toArray()) + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_DOUBLE)) {
				EvolucionPorInstantes<ArrayList<Double>> evs = (EvolucionPorInstantes<ArrayList<Double>>) evolucion;
				Hashtable<Long, ArrayList<Double>> valorizador = evs.getValorizador();
				valor = "";

				boolean primero = true;
				for (Long p : evs.getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					ArrayList<Double> val = valorizador.get(p);
					valor += fechaest + ";" + EscritorXML.arrayDoubleXML(val) + ")";
				}
			}
			evDoc.append("valor", valor);
		} else if (evolucion instanceof EvolucionPeriodica) {
			evDoc.append("tipo", "periodica");
			evDoc.append("periodo", EscritorXML.dameStringPeriodo(((EvolucionPeriodica<?>) evolucion).getPeriodo()));
			evDoc.append("cantPeriodo", ((EvolucionPeriodica<?>) evolucion).getCantPeriodos());
			String valor = "";
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_STRING)) {
				EvolucionPeriodica<String> evs = (EvolucionPeriodica<String>) evolucion;
				Hashtable<Long, String> valorizador = evs.getDefinicionPeriodo().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {

					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					String val = valorizador.get(p);
					valor += fechaest + ";" + val + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_DOUBLE)) {
				EvolucionPeriodica<Double> evs = (EvolucionPeriodica<Double>) evolucion;
				Hashtable<Long, Double> valorizador = evs.getDefinicionPeriodo().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					Double val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}

			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_ENTERO)) {
				EvolucionPeriodica<Integer> evs = (EvolucionPeriodica<Integer>) evolucion;
				Hashtable<Long, Integer> valorizador = evs.getDefinicionPeriodo().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					Integer val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_BOOLEAN)) {
				EvolucionPeriodica<Boolean> evs = (EvolucionPeriodica<Boolean>) evolucion;
				Hashtable<Long, Boolean> valorizador = evs.getDeterminante().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					Boolean val = valorizador.get(p);
					valor += fechaest + ";" + val.toString() + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_PAR_DOUBLE)) {
				EvolucionPeriodica<ArrayList<Pair<Double, Double>>> evs = (EvolucionPeriodica<ArrayList<Pair<Double, Double>>>) evolucion;
				Hashtable<Long, ArrayList<Pair<Double, Double>>> valorizador = evs.getDefinicionPeriodo()
						.getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					ArrayList<Pair<Double, Double>> val = valorizador.get(p);
					valor += fechaest + ";" + Arrays.deepToString(val.toArray()) + ")";
				}
			}
			if (tipoDato.equalsIgnoreCase(Text.TIPO_DATO_LISTA_DOUBLE)) {
				EvolucionPeriodica<ArrayList<Double>> evs = (EvolucionPeriodica<ArrayList<Double>>) evolucion;
				Hashtable<Long, ArrayList<Double>> valorizador = evs.getDefinicionPeriodo().getValorizador();
				valor = "";
				boolean primero = true;
				for (Long p : evs.getDefinicionPeriodo().getInstantesOrdenados()) {
					if (primero) {
						valor += "(";
						primero = false;
					} else {
						valor += ",(";
					}
					String fechaest = EscritorXML.convertirAFecha(p, lt);
					ArrayList<Double> val = valorizador.get(p);
					valor += fechaest + ";" + EscritorXML.arrayDoubleXML(val) + ")";
				}
			}
			evDoc.append("definicionPeriodo", valor);
		}
		doc.append("ev", evDoc);
		return doc;
	}

	// funciones
	private DatosPolinomio getFuncion(Document doc) {
		DatosPolinomio res = new DatosPolinomio();
		Document funcDoc = (Document) doc.get("funcion");
		String tipo = (String) funcDoc.get("tipo");
		res.setTipo(tipo);
		if (tipo.equalsIgnoreCase(Text.FUNC_POLI)) {
			List<Double> coefs = (ArrayList<Double>) funcDoc.get("coeficientes");
			double[] arrCoefs = new double[coefs.size()];
			for (int i = 0; i < arrCoefs.length; i++)
				arrCoefs[i] = coefs.get(i);
			res.setCoefs(arrCoefs);
		} else if (tipo.equalsIgnoreCase(Text.FUNC_POLI_CON_COTAS)) {
			res.setXmin((Double) funcDoc.get("xmin"));
			res.setXmin((Double) funcDoc.get("xmax"));
			res.setXmin((Double) funcDoc.get("valmin"));
			res.setXmin((Double) funcDoc.get("valmax"));
			List<Double> coefs = (ArrayList<Double>) funcDoc.get("coeficientes");
			double[] arrCoefs = new double[coefs.size()];
			for (int i = 0; i < arrCoefs.length; i++)
				arrCoefs[i] = coefs.get(i);
			res.setCoefs(arrCoefs);
		} else if (tipo.equalsIgnoreCase(Text.FUNC_POLI_MULTI)) {
			Hashtable<String, DatosPolinomio> pols = new Hashtable<>();
			for (Document docPol : (ArrayList<Document>) funcDoc.get("pols")) {
				DatosPolinomio datosPol = new DatosPolinomio();
				List<Double> coefs = (ArrayList<Double>) funcDoc.get("coeficientes");
				double[] arrCoefs = new double[coefs.size()];
				for (int i = 0; i < arrCoefs.length; i++)
					arrCoefs[i] = coefs.get(i);
				res.setCoefs(arrCoefs);
				pols.put((String) docPol.get("var"), datosPol);
			}
			res.setPols(pols);
		} else if (tipo.equalsIgnoreCase(Text.FUNC_POR_RANGOS)) {
			res.setFueraRango(getFuncion((Document) funcDoc.get("fueraRango")));
			ArrayList<Pair<Double, Double>> rangos = new ArrayList<>();
			for (String rangoStr : (List<String>) funcDoc.get("rangos")) {
				rangos.add(new Pair<>(Double.valueOf(rangoStr.split(";")[0].substring(1)),
						Double.valueOf(rangoStr.split(";")[1].substring(0, rangoStr.split(";")[1].length() - 2))));
			}
			res.setRangos(rangos);
			res.setPolsrangos((ArrayList<DatosPolinomio>) ((List<Document>) funcDoc.get("polsRangos")).stream()
					.map(this::getFuncion).collect(Collectors.toList()));
		}
		return res;
	}

	private Document getDocFuncion(DatosPolinomio datosPolinomio) {
		Document doc = new Document();
		Document funcDoc = new Document();
		if (datosPolinomio.getTipo().equalsIgnoreCase(Text.FUNC_POLI)) {
			funcDoc.append("tipo", Text.FUNC_POLI);
			List<Double> coefs = new ArrayList<>();
			for (double coef : datosPolinomio.getCoefs())
				coefs.add(coef);
			funcDoc.append("coeficientes", coefs);
		} else if (datosPolinomio.getTipo().equalsIgnoreCase(Text.FUNC_POLI_CON_COTAS)) {
			funcDoc.append("tipo", Text.FUNC_POLI_CON_COTAS);
			funcDoc.append("xmin", datosPolinomio.getXmin());
			funcDoc.append("xmax", datosPolinomio.getXmax());
			funcDoc.append("valmin", datosPolinomio.getValmin());
			funcDoc.append("valmax", datosPolinomio.getValmax());
			List<Double> coefs = new ArrayList<>();
			for (double coef : datosPolinomio.getCoefs())
				coefs.add(coef);
			funcDoc.append("coeficientes", coefs);
		} else if (datosPolinomio.getTipo().equalsIgnoreCase(Text.FUNC_POLI_MULTI)) {
			funcDoc.append("tipo", Text.FUNC_POLI_MULTI);
			List<Document> listPoli = new ArrayList<>();
			for (var entry : datosPolinomio.getPols().entrySet()) {
				Document docPol = new Document();
				docPol.append("var", entry.getKey());
				List<Double> coefs = new ArrayList<>();
				for (double coef : entry.getValue().getCoefs())
					coefs.add(coef);
				docPol.append("coeficientes", coefs);
				listPoli.add(docPol);
			}
			funcDoc.append("pols", listPoli);
		} else if (datosPolinomio.getTipo().equalsIgnoreCase(Text.FUNC_POR_RANGOS)) {
			funcDoc.append("tipo", Text.FUNC_POR_RANGOS);
			funcDoc.append("fueraRango", getDocFuncion(datosPolinomio.getFueraRango()));
			funcDoc.append("rangos", datosPolinomio.getRangos().stream()
					.map(par -> "(" + par.first + ";" + par.second + ")").collect(Collectors.toList()));
			List<Document> listPolRangos = new ArrayList<>();
			for (DatosPolinomio dP : datosPolinomio.getPolsrangos())
				listPolRangos.add(getDocFuncion(dP));
			funcDoc.append("polsRangos", listPolRangos);
		}
		doc.append("funcion", funcDoc);
		return doc;
	}

	// varAleatoria
	private DatosVariableAleatoria getVariableAleatoria(Document doc) {
		Document vaDoc = (Document) doc.get("variableAleat");
		return new DatosVariableAleatoria((String) vaDoc.get("procOptimizacion"), (String) vaDoc.get("procSimulacion"),
				(String) vaDoc.get("nombre"));
	}

	private Document getDocVariableAleatoria(DatosVariableAleatoria datosVariableAleatoria) {
		Document doc = new Document();
		Document vaDoc = new Document();
		vaDoc.append("procOptimizacion", datosVariableAleatoria.getProcOptimizacion());
		vaDoc.append("procSimulacion", datosVariableAleatoria.getProcSimulacion());
		vaDoc.append("nombre", datosVariableAleatoria.getNombre());
		doc.append("variableAleat", vaDoc);
		return doc;
	}

	// varEstado
	private DatosVariableEstado getVariableEstado(Document doc, String tiempoInicial, DatosLineaTiempo dlt) {
		DatosVariableEstado res = new DatosVariableEstado();
		Document docVE = (Document) doc.get("variableEstado");
		res.setNombre((String) docVE.get("nombre"));
		res.setEstadoInicial((Double) docVE.get("estadoInicial"));
		Document docDisc = (Document) docVE.get("discretizacion");
		DatosDiscretizacion dD = new DatosDiscretizacion();
		double discretizacionMin = (Double) docDisc.get("minimo");
		double discretizacionMax = (Double) docDisc.get("maximo");
		int discretizacionCantPuntos = (int) docDisc.get("cantidadPuntos");
		double[] discretizacionParticion = new double[discretizacionCantPuntos];
		double salto = (discretizacionMax - discretizacionMin) / (discretizacionCantPuntos - 1);
		for (int i = 0; i < discretizacionCantPuntos; i++) {
			discretizacionParticion[i] = discretizacionMin + i * salto;
		}
		dD.setMinimo(discretizacionMin);
		dD.setMaximo(discretizacionMax);
		dD.setParticion(discretizacionParticion);
		res.setDiscretizacion(new EvolucionConstante<>(dD, new SentidoTiempo(1)));
		res.setHayValorInferior(docVE.containsKey("valorRecursoInferior"));
		if (docVE.containsKey("valorRecursoInferior"))
			res.setValorRecursoInferior((Evolucion<Double>) getEvolucion((Document) docVE.get("valorRecursoInferior"),
					Text.TIPO_DATO_DOUBLE, tiempoInicial, dlt));
		res.setHayValorSuperior(docVE.containsKey("valorRecursoSuperior"));
		if (docVE.containsKey("valorRecursoSuperior"))
			res.setValorRecursoSuperior((Evolucion<Double>) getEvolucion((Document) docVE.get("valorRecursoSuperior"),
					Text.TIPO_DATO_DOUBLE, tiempoInicial, dlt));
		res.setDiscreta((boolean) docVE.get("discreta"));
		res.setOrdinal((boolean) docVE.get("ordinal"));
		res.setDiscretaIncremental((boolean) docVE.get("discretaIncremental"));
		return res;
	}

	private Document getDocVariableEstado(DatosVariableEstado datosVariableEstado, LineaTiempo lt) {
		Document doc = new Document();
		Document docVE = new Document();
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		docVE.append("nombre", datosVariableEstado.getNombre());
		docVE.append("estadoInicial", datosVariableEstado.getEstadoInicial());
		Document docDisc = new Document();
		docDisc.append("minimo", datosVariableEstado.getDiscretizacion().getValor(instanteActual).getMinimo());
		docDisc.append("maximo", datosVariableEstado.getDiscretizacion().getValor(instanteActual).getMaximo());
		docDisc.append("cantidadPuntos",
				datosVariableEstado.getDiscretizacion().getValor(instanteActual).getParticion().length);
		docVE.append("discretizacion", docDisc);
		if (datosVariableEstado.getValorRecursoInferior() != null)
			docVE.append("valorRecursoInferior",
					getDocEvolucion(datosVariableEstado.getValorRecursoInferior(), Text.TIPO_DATO_DOUBLE, lt));
		if (datosVariableEstado.getValorRecursoSuperior() != null)
			docVE.append("valorRecursoSuperior",
					getDocEvolucion(datosVariableEstado.getValorRecursoSuperior(), Text.TIPO_DATO_DOUBLE, lt));
//        docVE.append("hayValorInferior", datosVariableEstado.isHayValorInferior());
//        docVE.append("hayValorSuperior", datosVariableEstado.isHayValorSuperior());
		docVE.append("discreta", datosVariableEstado.isDiscreta());
		docVE.append("ordinal", datosVariableEstado.isOrdinal());
		docVE.append("discretaIncremental", datosVariableEstado.isDiscretaIncremental());
		doc.append("variableEstado", docVE);
		return doc;
	}

	// varsControlDE
	private DatosVariableControlDE getVariableDcontrolDE(Document doc, String tiempoInicial, DatosLineaTiempo dlt) {
		Document docVCDE = (Document) doc.get("variableControlDE");
		DatosVariableControlDE res = new DatosVariableControlDE();
		res.setNombre(docVCDE.getString("nombre"));
		res.setPeriodo(docVCDE.getInteger("periodo"));
		res.setCostoDeControl((Evolucion<Double[]>) getEvolucion((Document) docVCDE.get("costoDeControl"),
				Text.TIPO_DATO_ARRAY_DOUBLE, tiempoInicial, dlt));
		return res;
	}

	private Document getDocVariableControlDE(DatosVariableControlDE datosVariableControlDE, LineaTiempo lt) {
		Document doc = new Document();
		Document docVCDE = new Document();
		docVCDE.append("nombre", datosVariableControlDE.getNombre());
		docVCDE.append("periodo", datosVariableControlDE.getPeriodo());
		docVCDE.append("costoDeControl",
				getDocEvolucion(datosVariableControlDE.getCostoDeControl(), Text.TIPO_DATO_ARRAY_DOUBLE, lt));
		doc.append("variableControlDE", docVCDE);
		return doc;
	}

	// utilitarios
	private <K, V> Hashtable<K, V> fromMap(Map<K, V> map) {
		Hashtable<K, V> res = new Hashtable<>();
		for (var entry : map.entrySet()) {
			res.put(entry.getKey(), entry.getValue());
		}
		return res;
	}

	private double[] fromList(List<Double> list) {
		double[] res = new double[list.size()];
		for (int i = 0; i < res.length; i++)
			res[i] = list.get(i);
		return res;
	}

	// TEST
//    public void insertData()
}
