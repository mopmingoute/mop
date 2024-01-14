/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Text is part of MOP.
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

package interfaz;

import javafx.scene.paint.Color;
import utilitarios.Constantes;

import java.awt.*;
import java.util.*;

public class Text {

    public static final Integer TIPO_EOLICO = 1;
    public static final Integer TIPO_SOLAR = 2;
    public static final Integer TIPO_TERMICO = 3;
    public static final Integer TIPO_HIDRAULICO = 4;
    public static final Integer TIPO_ACUMULADOR = 5;
    public static final Integer TIPO_IMPOEXPO = 6;
    public static final Integer TIPO_CICLO_COMBINADO = 7;

    public static final String TIPO_EOLICO_TEXT = "eolico";
    public static final String TIPO_SOLAR_TEXT = "solar";
    public static final String TIPO_TERMICO_TEXT = "termico";
    public static final String TIPO_CICLO_COMBINADO_TEXT = "ciclo_combinado";
    public static final String TIPO_HIDRAULICO_TEXT = "hidraulico";
    public static final String TIPO_ACUMULADOR_TEXT = "acumulador";
    public static final String TIPO_IMPOEXPO_TEXT = "impo_expo";
    public static final String TIPO_DEMANDA_TEXT = "demanda";
    public static final String TIPO_FALLA_TEXT = "falla";
    public static final String TIPO_COMBUSTIBLE_TEXT = "combustible";
    public static final String TIPO_IMPACTO_TEXT = "impacto_ambiental";
    public static final String TIPO_CONTRATO_ENERGIA_TEXT = "contrato_energia";

    public static final String TIPO_DATO_ENTERO = "int";
    public static final String TIPO_DATO_DOUBLE = "double";
    public static final String TIPO_DATO_STRING = "string";
    public static final String TIPO_DATO_BOOLEAN = "boolean";
    public static final String TIPO_DATO_LISTA_PAR_DOUBLE = "listaParesDouble";
    public static final String TIPO_DATO_LISTA_DOUBLE = "listadouble";
    public static final String TIPO_DATO_ARRAY_DOUBLE = "arraydouble";
//    public static final String TIPO_DATO_ENTERO = "int";
//    public static final String TIPO_DATO_ENTERO = "int";


    //COLORES
    public static final String COLOR_EOLICO  = "#38a5e0";//celeste
    public static final String COLOR_SOLAR   = "#f1ca48";//amarillo
    public static final String COLOR_TERMICO = "#c91717";//rojo
    public static final String COLOR_CICLO_COMBINADO = "#e87c2a";//anaranjado
    public static final String COLOR_HIDRAULICO   = "#27468d";//azul
    public static final String COLOR_ACUMULADOR   = "#000000";//todo color
    public static final String COLOR_IMPOEXPO   = "#4d8006";//todo color

    public static final Map<Integer,String> COLORES = Map.of(TIPO_EOLICO,COLOR_EOLICO,TIPO_SOLAR,COLOR_SOLAR,TIPO_TERMICO,COLOR_TERMICO,TIPO_HIDRAULICO,COLOR_HIDRAULICO,TIPO_ACUMULADOR,COLOR_ACUMULADOR, TIPO_IMPOEXPO,COLOR_IMPOEXPO, TIPO_CICLO_COMBINADO, COLOR_CICLO_COMBINADO);

    public static final Map<Integer,String> ALPHA = Map.of(1,"40",2,"80",3,"c0",4,"ff");//25% - 40, 50% - 80, 75%-C0, 100%-FF


    //PARAM GEN
    public static final String VALPOSTIZACION_INTERNA = "Interna";
    public static final String VALPOSTIZACION_EXTERNA = "Externa";


    // EV
    public static final String EV_NUM_INT = "Entero";
    public static final String EV_NUM_DOUBLE = "Decimal";
    public static final String EV_BOOL = "Booleano";
    public static final String EV_VAR = "Variable";
    public static final String EV_LISTA_NUM = "Lista Num";
    //    public static final String EV_BOOL = "Booleano";
//    public static final String EV_BOOL = "Booleano";
//    public static final String EV_BOOL = "Booleano";
    public static final String EV_CONST = "Const";
    public static final String EV_CONST_NUM = "Numérico";
    public static final String EV_CONST_NUM_INT = "Num (entero)";
    public static final String EV_CONST_NUM_DOUBLE = "Num (double)";
    public static final String EV_CONST_NUM_CON_UNIT = "Num c/Unidad";
    public static final String EV_CONST_NUM_SIN_UNIT = "Num s/Unidad";
    public static final String EV_CONST_BOOL = "Booleano";
    public static final String EV_CONST_FUNC = "Función";
    public static final String EV_CONST_VAR = "Variable";
    public static final String EV_CONST_LISTA_NUM = "Lista Num";
    public static final String EV_CONST_RANGOS = "Rangos";
    public static final String EV_CONST_DISCRETIZACION  = "Discretización";
    public static final String EV_CONST_LISTA_PAR_NUM  = "Lista Pares";
    public static final String EV_POR_INSTANTES = "Por Instantes";
    public static final String EV_POR_INSTANTES_NUMERICO = "Numérico";
    public static final String EV_POR_INSTANTES_VARIABLE = "Variable";
    public static final String EV_POR_INSTANTES_BOOL = "Booleano";
    public static final String EV_PERIODICA = "Periódica";
    public static final String EV_PERIODICA_NUM = "Numérico";
    public static final String EV_PERIODICA_VAR = "Variable";
    public static final String EV_PERIODICA_BOOL = "Booleano";
    public static final String PERIODO_ANIO = "Año";
    public static final String PERIODO_MES = "Mes";
    public static final String PERIODO_SEMANA = "Semana";
    public static final String PERIODO_DIA = "Día";


    public static final String EV_IMP_EXP_SEPARADOR = ",";



    // FUNC
    public static final String FUNC_POLI_LABEL = "Poli";
    public static final String FUNC_POLI = "poli";
    public static final String FUNC_POLI_CON_COTAS_LABEL = "Poli Con Cotas";
    public static final String FUNC_POLI_CON_COTAS = "poliConCotas";
    public static final String FUNC_POLI_MULTI_LABEL = "Poli Multi";
    public static final String FUNC_POLI_MULTI = "poliMulti";
    public static final String FUNC_POR_RANGOS_LABEL = "Por Rangos";
    public static final String FUNC_POR_RANGOS = "porRangos";
    public static final String FUNC_VAR_Q_EROGADO = "QErogado";
    public static final String FUNC_VAR_COTA_AGUAS_ABAJO = "CotaAguasAbajo";

    // PARTICIPANTES
    public static final String PARTICIPANTE_EOLICO = "Eólico";
    public static final String PARTICIPANTE_SOLAR  = "Solar";
    public static final String PARTICIPANTE_TERMICO  = "Térmico";
    public static final String PARTICIPANTE_HIDRAULICO  = "Hidráulico";
    public static final String PARTICIPANTE_CENTRAL_ACUMULACION  = "Central Acumulación";
    public static final String PARTICIPANTE_DEMANDA  = "Demanda";
    public static final String PARTICIPANTE_FALLA  = "Falla";
    public static final String PARTICIPANTE_IMPO_EXPO  = "Impo/Expo";
    public static final String PARTICIPANTE_RED_COMBUSTIBLE  = "Red Combustible";
    public static final String PARTICIPANTE_RED_ELECTRICA  = "Red Eléctrica";

    //ATRIBUTOS
    public static final String ATRIBUTOS_POTENCIAS = "potencias";
    public static final String ATRIBUTOS_ENERGIAS = "energias";
    public static final String ATRIBUTOS_COSTOS = "costos";

    public static final String ATRIBUTOS_VOLUMEN_COMBUSTIBLE1 = Constantes.VOLCOM1;
    public static final String ATRIBUTOS_VOLUMEN_COMBUSTIBLE2 = Constantes.VOLCOM2;
    public static final String ATRIBUTOS_ENERGIA_COMBUSTIBLE1 = Constantes.ENERCOM1;
    public static final String ATRIBUTOS_ENERGIA_COMBUSTIBLE2 = Constantes.ENERCOM2;


    public static final String ATRIBUTOS_TURBINADOS = "turbinados";
    public static final String ATRIBUTOS_VERTIDO = "vertido";
    public static final String ATRIBUTOS_COTA_AGUAS_ARRIBA = "cotaAguasArriba";
    public static final String ATRIBUTOS_COEF_ENERGETICO = "coefEnergetico";
    public static final String ATRIBUTOS_TURBINADO_PASO = "turb_paso";
    public static final String ATRIBUTOS_VERTIDO_PASO = "vert_paso";
    public static final String ATRIBUTOS_APORTE = "aporte";
    public static final String ATRIBUTOS_VALLOR_AGUA = "valAgua";
    public static final String ATRIBUTOS_POT_INYEC = "potenciasIny";
    public static final String ATRIBUTOS_POT_ALMAC = "potenciasAlm";
    public static final String ATRIBUTOS_ENERG_INYEC = "energiasIny";
    public static final String ATRIBUTOS_ENERG_ALMAC = "energiasAlm";

    public static final String ATRIBUTOS_COTA_PENALIZADA_INFERIOR = Constantes.COTAPENINF;
    public static final String ATRIBUTOS_VOLUMEN_PENALIZADO_INFERIOR = Constantes.VOLPENINF;
    public static final String ATRIBUTOS_COSTOS_PEN_COTA_INF = Constantes.COSTOPENCCOTASINF;
    public static final String ATRIBUTOS_COTA_PENALIZADA_SUPERIOR =Constantes.COTAPENSUP;
    public static final String ATRIBUTOS_VOLUMEN_PENALIZADO_SUPERIOR = Constantes.VOLPENSUP;
    public static final String ATRIBUTOS_COSTOS_PEN_COTA_SUP = Constantes.COSTOPENCCOTASSUP;


    // Sacar de la clase constantes o del xml para los 6 atributos nuevos

    //PE
    public static final String TIPO_PE_HISTORICO = "historico";
    public static final String TIPO_PE_MARKOV = "markov";
    public static final String TIPO_PE_POR_ESCENARIOS = "porEscenarios";
    public static final String TIPO_PE_POR_CRONICAS = "porCronicas";
    public static final String TIPO_PE_BOOTSTRAP_DISCRETO = "bootstrapDiscreto";
    public static final String TIPO_PE_DEMANDA_ANIO_BASE = "demandaAnioBase";

    //LABELS

    ////EDITORES


//    public static final String

    //HIDRO
    public static final ArrayList<String> COMP_LAGO_VALS = new ArrayList<>(Arrays.asList(Constantes.HIDROCONLAGO, Constantes.HIDROSINLAGO, Constantes.HIDROSINLAGOENOPTIM));
    public static final ArrayList<String> COMP_COEF_ENER_VALS = new ArrayList<>(Arrays.asList(Constantes.HIDROCOEFENERGCONSTANTES, Constantes.HIDROPOTENCIACAUDAL));

    //////EÓLICO

    //TÉRMICO
    public static final ArrayList<String> COMP_MIN_TECNICO_VALS = new ArrayList<>(Arrays.asList(Constantes.TERSINMINTEC, Constantes.TERMINTECFORZADO, Constantes.TERVARENTERAS, Constantes.TERVARENTERASYVARESTADO, Constantes.TERDOSPASOS));


    public static final String TERFLEXHORARIO = "Por poste"; //En utilitarios/Constantes.java  estan las que se usan internamente
    public static final String TERFLEXSEMANAL = "Por paso";  //En utilitarios/Constantes.java  estan las que se usan internamente


    //CICLO COMBINADO
    // se saca temporalmente la lista completa
    //public static final ArrayList<String> COMP_CICLO_COMB_VALS = new ArrayList<>(Arrays.asList(Constantes.TGSMEJORES, Constantes.CCSINESTADO, Constantes.CCCONESTADO, Constantes.CCDETALLADO));
    public static final ArrayList<String> COMP_CICLO_COMB_VALS = new ArrayList<>(Arrays.asList( Constantes.CCSINESTADO));

    //ACUMULADOR
    public static final ArrayList<String> COMP_PASO_VALS = new ArrayList<>(Arrays.asList(Constantes.ACUCIERRAPASO, Constantes.ACUMULTIPASO, Constantes.ACUBALANCECRONOLOGICO));

    //FALLA
    //public static final ArrayList<String> COMP_FALLA = new ArrayList<>(Arrays.asList(Constantes.FALLASINESTADO, Constantes.FALLA_CONESTADO_SINDUR, Constantes.FALLA_CONESTADO_CONDUR));
    public static final ArrayList<String> COMP_FALLA = new ArrayList<>(Arrays.asList(Constantes.FALLASINESTADO, Constantes.FALLA_CONESTADO_SINDUR));

    //IMPO/EXPO
    public static final ArrayList<String> PAISES = new ArrayList<>(Arrays.asList("Argentina", "Brasil", "Biolandia"));
    public static final String datosVarAleatUniforme = "PEDispSimple";
    public static final String nommbreVarAleatUniforme ="UNIFORME";

    //COMBUSTIBLE
    public static final ArrayList<String> COMP_USO_RED_VALS = new ArrayList<>(Arrays.asList(Constantes.UNINODAL));
    public static final String UNIDAD_M3 = "m³";

    //IMPACTOS
    // TODO: 06/08/2020 valores posta
    public static final String STRING_HIDRO_VERTIMIENTO_EXTERNO = "VertimientoExterno";
    public static final String STRING_HIDRO_CAUDAL_ECOLOGICO = "caudalEcologico";
    public static final String STRING_HIDRO_PROD_MAQUINA = "ImpactoProduccionPorCaudal";
    public static final String STRING_HIDRO_INUN_AGUAS_ARRIBA = "InundacionAguasArriba";
    public static final String STRING_HIDRO_INUN_AGUAS_ABAJO = "InundacionAguasAbajo";
    public static final String STRING_TER_EMISIONES_CO2 = "EmisionesCO2";

    public static final ArrayList<String> TIPOS_IMPACTOS = new ArrayList<>(Arrays.asList(STRING_HIDRO_CAUDAL_ECOLOGICO,STRING_HIDRO_VERTIMIENTO_EXTERNO,
                                                                                         STRING_HIDRO_PROD_MAQUINA,STRING_HIDRO_INUN_AGUAS_ARRIBA,
                                                                                         STRING_HIDRO_INUN_AGUAS_ABAJO,STRING_TER_EMISIONES_CO2));

    public static final Map<Integer,String> TIPOS_IMPACTO_BY_INT = Map.of(Constantes.HIDRO_CAUDAL_ECOLOGICO, STRING_HIDRO_CAUDAL_ECOLOGICO,
    																	  Constantes.HIDRO_VERTIMIENTO_EXTERNO, STRING_HIDRO_VERTIMIENTO_EXTERNO,
                                                                          Constantes.HIDRO_PROD_MAQUINA, STRING_HIDRO_PROD_MAQUINA,
                                                                          Constantes.HIDRO_INUN_AGUAS_ARRIBA, STRING_HIDRO_INUN_AGUAS_ARRIBA,
                                                                          Constantes.HIDRO_INUN_AGUAS_ABAJO, STRING_HIDRO_INUN_AGUAS_ABAJO,
                                                                          Constantes.TER_EMISIONES_CO2, STRING_TER_EMISIONES_CO2);

    public static final Map<String,Integer> TIPOS_IMPACTO_BY_STRING = Map.of(STRING_HIDRO_CAUDAL_ECOLOGICO, Constantes.HIDRO_CAUDAL_ECOLOGICO,
    												 						 STRING_HIDRO_VERTIMIENTO_EXTERNO, Constantes.HIDRO_VERTIMIENTO_EXTERNO,
                                                                             STRING_HIDRO_PROD_MAQUINA, Constantes.HIDRO_PROD_MAQUINA,
                                                                             STRING_HIDRO_INUN_AGUAS_ARRIBA, Constantes.HIDRO_INUN_AGUAS_ARRIBA,
                                                                             STRING_HIDRO_INUN_AGUAS_ABAJO, Constantes.HIDRO_INUN_AGUAS_ABAJO,
                                                                             STRING_TER_EMISIONES_CO2, Constantes.TER_EMISIONES_CO2);



    public static final String BARRA_1 = "barraPrueba1";
    public static final String BARRA_2 = "barraPrueba2";

    public static final String UNIDAD_MW = "MW";
    public static final String UNIDAD_SEGUNDOS = "segundos";
    public static final String UNIDAD_HORAS = "horas";
    public static final String UNIDAD_DIAS = "días";
    public static final String UNIDAD_HM3 = "hm³";
    public static final String UNIDAD_PERIODOS = "periodos";
    public static final String TIPO_EQUIESPACIADA = "equiespaciada";
    public static final String UNIDAD_M3_S = "m³/s";
    public static final String UNIDAD_USD_M3 = "USD/m³";
    public static final String UNIDAD_USD_HM3 = "USD/hm³";
    public static final String UNIDAD_MWH = "MWh";
    public static final String UNIDAD_GWH = "GWh";
    public static final String UNIDAD_USD_MWH = "USD/MWh";
    public static final String UNIDAD_POR_CIENTO = "%";
    public static final String UNIDAD_USD = "USD";
    public static final String UNIDAD_MWH_M3 = "MWh/m³";
    public static final String UNIDAD_KG_M3 = "kg/m³";
    public static final String TIPO_EXPONENCIAL = "exponencial";
    public static final String UNIDAD_M3_H = "m³/h";
    public static final String UNIDAD_M = "m";



    public static final String VE_VOLUMEN_DEL_LAGO = "volumen";
    public static final String VE_VOLUMEN_DEL_LAGO_LABEL = "VOLUMEN DEL LAGO";
    public static final String VE_ESTADO_MIN_TEC = "volumen";
    public static final String VE_ESTADO_MIN_TEC_LABEL = "ESTADO MÍNIMO TÉCNICO";
    public static final String VE_ENERGIA_ACUMULADA = "energAcumulada";
    public static final String VE_ENERGIA_ACUMULADA_LABEL = "ENERGÍA ACUMULADA";
    public static final String VE_CANT_ESC_FORZ = "cantEscForzados";
    public static final String VE_CANT_ESC_FORZ_LABEL = "CANTIDAD DE ESCALONES FORZADOS";
    public static final String VE_PER_FORZ_RESTANTES = "perForzadosRestantes";
    public static final String VE_PER_FORZ_RESTANTES_LABEL = "CANTIDAD DE PERÍODOS RESTANTES DE FORZAMIENTO";
    public static final String VCDE_CANT_A_FORZ = "cantEscAForzar";
    public static final String VCDE_CANT_A_FORZ_LABEL = "CANTIDAD DE ESCALONES A FORZAR";


    //MESES
    public static final Map<Integer,String> MONTHS_BY_NUMBER = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(1,  "Enero"),     new AbstractMap.SimpleEntry<>(2,  "Febrero"),
            new AbstractMap.SimpleEntry<>(3,  "Marzo"),     new AbstractMap.SimpleEntry<>(4,  "Abril"),
            new AbstractMap.SimpleEntry<>(5,  "Mayo"),      new AbstractMap.SimpleEntry<>(6,  "Junio"),
            new AbstractMap.SimpleEntry<>(7,  "Julio"),     new AbstractMap.SimpleEntry<>(8,  "Agosto"),
            new AbstractMap.SimpleEntry<>(9,  "Setiembre"), new AbstractMap.SimpleEntry<>(10, "Octubre"),
            new AbstractMap.SimpleEntry<>(11, "Noviembre"), new AbstractMap.SimpleEntry<>(12, "Diciembre")
    );

    //PASOS
    public static final String PASO_NONE = "SELECCIONE_PASO";
    public static final String PASO_HORARIO = "HORARIO";
    public static final String PASO_DIARIO = "DIARIO";
    public static final String PASO_SEMANAL = "SEMANAL";
    public static final ArrayList<String> PASOS_BASE = new ArrayList<>(Arrays.asList(PASO_NONE,PASO_DIARIO,PASO_SEMANAL));
    public static final Map<String,Integer> DEFAULT_CANT_POSTES = Map.of(PASO_HORARIO, 1, PASO_DIARIO, 24, PASO_SEMANAL, 4);


    //ESTIMADORES
    public static final String TIPO_ESTIMADOR_MARKOV = "Markov Discreto";
    public static final String TIPO_ESTIMADOR_VAR_EN_V_NORM = "VAR en Variables Normalizadas";
    public static final String TIPO_ESTIMADOR_BOOSTRAP_DISCRETO = "Bootstrap Discreto";
//    public static final String TIPO_ESTIMADOR_MARKOV = "Markov Discreto";
//    public static final String TIPO_ESTIMADOR_MARKOV = "Markov Discreto";
    public static final ArrayList<String> TIPOS_ESTIMADORES = new ArrayList<>(Arrays.asList(TIPO_ESTIMADOR_MARKOV,TIPO_ESTIMADOR_VAR_EN_V_NORM, TIPO_ESTIMADOR_BOOSTRAP_DISCRETO));

    //TOOLTIPS
    //Se pasaron a un archivo Tooltips.properties ubicado en la carpeta resources

    public static final String EMPTY_TOOLTIP = "Tooltip Vacío";

    public static final String TT_BIBLIOTECA_ADD_PARTICIPANTE = "Agrega el participante a la corrida";
    public static final String TT_BIBLIOTECA_REMOVE_PARTICIPANTE = "Elimina el participante de la biblioteca";


    //MENSAJES

    public static final Boolean MOSTRAR_MENSAJE_AL_LANZAR_CORRIDA = true;   //se usa esta bandera para que no haya otros threads al lanzar la optimizacion y así ver los mensajes de error en la consola

    public static final String MSG_ERR_AGREGAR_PARTICIIPANTE_BIBLIOTECA = "Error al guardar participante. Verifique: datos completos";
    public static final String MSG_CONF_GUARDAR_PARTICIPANTE_BIBLIOTECA = "Participante guardado correctamente";
    public static final String MSG_CONF_ACTUALIZAR_PARTICIPANTE_BIBLIOTECA = "Participante actualizado correctamente";
    public static final String MSG_ASK_ACTUALIZAR_PARTICIPANTE_BIBLIOTECA = "El participante ya existe, ¿quiere actualizarlo?";
    public static final String MSG_ERR_ELIMINAR_PARTICIIPANTE_BIBLIOTECA = "Error al eliminar participante de la biblioteca";

    public static final String MSG_CONF_AGREGAR_PARTICIIPANTE_CORRIDA = "El participante se agregó correctamente a la corrida";
    public static final String MSG_ERR_AGREGAR_PARTICIIPANTE_CORRIDA = "Error al agregar participante a la corrida";

    public static final String MSG_CONF_REPORTES_PDF = "Se exportó el reporte";
    public static final String MSG_ERR_REPORTES_PDF = "Error al exportar el reporte";

    public static final String MSG_ASK_DATOS_INCOMPLETOS_PARTICIPANTE = "El participante tiene datos incompletos o erroneos, ¿cierra sin corregirlos?";

    public static final String MSG_ERR_CREAR_PARTICIPANTE_SIN_CORRIDA = "Para agregar un participante debe existir una corrida.";

    public static final String MSG_CONF_ELIMINAR_PARTICIPANTE = "¿Desea eliminar el participante: ";

    public static final String MSG_CONF_GUARDAR_AL_SALIR = "¿Quiere guardar la corrida antes de salir? ";

    public static final String MSG_CONF_CERRAR_SIN_GUARDAR = "Hay cambios en el participante. ¿Cierra sin guardarlos?";


    //Graficos

    static Color COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO = Color.color(0.9, 0.2, 0.1);



    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    //TODO: no esta detectando la resolucion adecuada, por lo estatica de la clase, ver como inicializar esta variable
    public static final boolean RESOLUCION_ADECUADA = screenSize.height >= 1080 && screenSize.width >= 1920;
    // Medidas ventanas:   width, height
    // Se definen las medidas de las ventans grandes y la basica
    public static final int [] BASIC_SIZE = {1400, 800};
    public static final int[] CICLO_COMBINADO_SIZE_GDE = {1640, 970};
    public static final int[] HIDRAULICO_SIZE_GDE = {1900, 1002};
    public static final int[] COMBUSTIBLE_SIZE_GDE = {1337, 870};
    public static final int[] IMPOEXPO_SIZE_GDE = {1768, 785};
    // Se definen las medidas a usar
    public static final int[] EOLICO_SIZE = {850, 860};
    public static final int[] SOLAR_SIZE =  {850, 860};
    public static final int[] TERMICO_SIZE =  {910,970};
    public static final int[] CICLO_COMBINADO_SIZE =  (RESOLUCION_ADECUADA) ? CICLO_COMBINADO_SIZE_GDE : BASIC_SIZE;
    public static final int[] HIDRAULICO_SIZE =   (RESOLUCION_ADECUADA) ? HIDRAULICO_SIZE_GDE : BASIC_SIZE;
    public static final int[] ACUMULADOR_SIZE =  {862, 1015};
    public static final int[] IMPOEXPO_SIZE =   (RESOLUCION_ADECUADA) ? IMPOEXPO_SIZE_GDE : BASIC_SIZE;
    public static final int[] DEMANDA_SIZE =  {850, 418};
    public static final int[] FALLA_SIZE =  {889, 980};
    public static final int[] COMBUSTIBLE_SIZE = (RESOLUCION_ADECUADA) ? COMBUSTIBLE_SIZE_GDE : BASIC_SIZE;
    public static final int[] IMPACTO_SIZE =  {850, 860};
    public static final int[] CONTRATO_ENERGIA_SIZE =  {885, 860};


}
