package utilClusters;

import java.util.ArrayList;
import java.util.Hashtable;

import procesosEstocasticos.ProcesoEstocastico;

/**
 * Crea clusters de observaciones con el método k-means
 */
public class Kmeans {
	
	private int cantObs;  // cantidad de observaciones
	private Hashtable<Integer, Observable> observaciones;
	
	private Hashtable<Integer, Observable> centros;
	
	/**
	 * La lista recorre las iteraciones sucesivas
	 * Para cada iteración la tabla tiene
	 *  clave: número de cluster empezando en cero
	 *  valor: el cluster de ese número
	 */
	private ArrayList<Hashtable<Integer, Cluster>> historiaIteraciones;
	
	private int k; // cantidad de clusters a hallar
	
	
	
	
	public Kmeans(Hashtable<Integer, Observable> observaciones) {
		this.observaciones = observaciones;
		cantObs = observaciones.values().size();
		Hashtable<Integer, Observable> centros = new Hashtable<Integer, Observable>();
		historiaIteraciones = new ArrayList<Hashtable<Integer, Cluster>>();
	}
	
	
	
	public Hashtable<Integer, Cluster> kmeansPlus(int ka, int maxIter){
		k = ka;
		centros = eligeCentrosIniciales();
		historiaIteraciones.add(clustersDeCentros(centros));
		Hashtable<Integer, Cluster> nuevosCentros;
		int i=0;
		boolean cambiaron = true;
		do {
			nuevosCentros = iterarKMeans();
			cambiaron = cambiaronCentros(nuevosCentros);
			historiaIteraciones.add(clustersDeCentros(centros));
			i++;
		}while(i<maxIter && cambiaron);		
		return nuevosCentros;
	}
	
	
	/**
	 * Elije k Observables de observaciones para que sean los centros iniciales
	 * 
	 * 1)Elije el primer centro al azar entre las observaciones equiprobables
	 * 2)Para cada punto x que no es centro calcula la distancia D(x) desde el punto
	 * hasta el centro ya elegido más cercano a x.
	 * 3)Sortea entre los x que no son centros un nuevo centro con probabilidad 
	 * proporcional a D(x)**2.
	 * 4)Si aún no llegó a elegir k centros vuelve a 2.
	 * 
	 * @return
	 */
	private Hashtable<Integer, Observable> eligeCentrosIniciales() {
		Hashtable<Integer, Observable> centrosIniciales = new Hashtable<Integer, Observable>();
		int cantEleg = 0;
		ArrayList<Observable> obsRestantes = new ArrayList<Observable>();
		obsRestantes.addAll(observaciones.values());
		ArrayList<Integer> indCentEleg = new ArrayList<Integer>();  // lista de índices de los centros ya elegidos en observables
		ArrayList<Observable> centEleg = new ArrayList<Observable>();  // lista de centros ya elegidos
		double aleatUnif = Math.random();
		int indEleg = ProcesoEstocastico.eligeUnOrdinalDeUnaPoblacion(k, aleatUnif);
		centEleg.add(observaciones.get(indEleg));
		while(cantEleg<k) {
			// get(i) contendrá la probabilidad acumulada hasta 
			// el elemento i-esimo, empezando en i=0
			ArrayList<Double> probSum = new ArrayList<Double>(); 
			int cantRest = cantObs - cantEleg;
			for(Observable o: obsRestantes) {
				int indCentMC = o.indiceCentroMasCercano(centEleg);
//				probSum.add(dist)
				
			}
			
			
			
		}
		// OJO TRUCHO..................................................
		return centrosIniciales;
	}	


	private Hashtable<Integer, Cluster> iterarKMeans() {
		// TODO Auto-generated method stub
		return null;
	}





	private boolean cambiaronCentros(Hashtable<Integer, Cluster> nuevosCentros) {
		// TODO Auto-generated method stub
		return false;
	}



	public Hashtable<Integer, Cluster> clustersDeCentros(Hashtable<Integer, Observable> cent){
	
		
		
		return new Hashtable<Integer, Cluster>();
	}
	
	
	
	
	
	
//    private PostizacionPaso calcularInternaClustering() {
//
//        ArrayList<Integer> numpos = new ArrayList<Integer>();
//        for (int i = 0; i < referencia.size(); i++) {
//               numpos.add(0);
//        }
//
//        referenciaIndexada = new ArrayList<ParIndexado>();
//        for (int i = 0; i < referencia.size(); i++)
//               referenciaIndexada.add(new ParIndexado(referencia.get(i), i));
//        Collections.sort(referenciaIndexada);
//
//        /**
//        * K-MEANS CLUSTERING
//        */
//        boolean converge = false;
//        int k = this.kmax;
//        Hashtable<Integer, Double> centrosIniciales = new Hashtable<Integer, Double>();
//        Hashtable<Integer, Double> centrosActuales = new Hashtable<Integer, Double>();
//        Hashtable<Integer, ArrayList<ParIndexado>> clustersActuales = new Hashtable<Integer, ArrayList<ParIndexado>>();
//        int cantInstantesMuestreo = referencia.size();
//        double tamanioTrozo = (referenciaIndexada.get(cantInstantesMuestreo - 1).first
//                      - referenciaIndexada.get(0).first) / k;
//        for (int i = 0; i < k; i++) {
//               centrosIniciales.put(i, referenciaIndexada.get(0).first + tamanioTrozo * i + tamanioTrozo / 2);
//               clustersActuales.put(i, new ArrayList<ParIndexado>());
//        }
//        centrosActuales = (Hashtable<Integer, Double>) centrosIniciales.clone();
//        double epsilon = 0.0001;
//        int j = 0;
//        while (!converge) {
//               j++;
//               for (int i = 0; i < k; i++) {
//                      clustersActuales.get(i).clear();
//               }
//               for (ParIndexado p : referenciaIndexada) {
//                      int centroMasCercano = 0;
//                      double menorDistancia = 1000000;
//                      for (int i = 0; i < k; i++) {
//                             double dist = distancia(p, centrosIniciales.get(i));
//                             if (dist < menorDistancia) {
//                                   centroMasCercano = i;
//                                   menorDistancia = dist;
//                             }
//                      }
//                      clustersActuales.get(centroMasCercano).add(p);
//               }
//
//               centrosIniciales = (Hashtable<Integer, Double>) centrosActuales.clone();
//               centrosActuales.clear();
//               for (int i = 0; i < k; i++) {
//                      double baricentro = 0;
//                      for (ParIndexado p : clustersActuales.get(i)) {
//                             baricentro += p.first;
//                      }
//                      if (clustersActuales.get(i).size() != 0)
//                             centrosActuales.put(i, baricentro / clustersActuales.get(i).size());
//                      else
//                             centrosActuales.put(i, centrosIniciales.get(i));
//               }
//               converge = chequearConvergencia(centrosActuales, centrosIniciales, epsilon);
//               centrosIniciales = (Hashtable<Integer, Double>) centrosActuales.clone();
//        }
//
//        durPos = new int[k];
//        for (int i = 0; i < k; i++) {
//               int dur = 0;
//               if (clustersActuales.get(i).size() == 0) {
//                      double menorDistancia = 100000;
//                      int centroMasCercano = 0;
//                      int indiceMasCercano = 0;
//                      
//                      for (int w = 0; w < k; w++) {
//                             int indice = 0;
//                             for (ParIndexado p : clustersActuales.get(w)) {
//                                   double dist = distancia(p, centrosIniciales.get(i));                                        
//                                   if (dist < menorDistancia) {
//                                          centroMasCercano = w;
//                                          menorDistancia = dist;
//                                          indiceMasCercano = indice;
//                                   }
//                                   indice++;
//                             }                                 
//                      }
//                      ParIndexado aCambiar = clustersActuales.get(centroMasCercano).get(indiceMasCercano);
//                      clustersActuales.get(i).add(aCambiar);
//               }
//               for (ParIndexado p : clustersActuales.get(i)) {
//                      numpos.set(p.second, i + 1);
//                      dur++;
//               }
//               durPos[i] = dur * Constantes.SEGUNDOSXHORA;
//        }
//        interPorPoste = new int[k];
//        for (int i = 0; i < k; ++i) {
//               interPorPoste[i] = durPos[i] / ptiempo.getBloque().getIntervaloMuestreo();
//        }
//        PostizacionPaso resultado = new PostizacionPaso(ptiempo,
//                      ptiempo.getDuracionPaso() / ptiempo.getIntervaloMuestreo(), numpos, interPorPoste, k, durPos);
//
//        ptiempo.getBloque().setCantPostes(k);
//        ptiempo.getBloque().setDuracionPostes(durPos);
//
//        resultado.setCantPos(k);
//        resultado.setNumpos(numpos);
//        return resultado;
//  }



}
