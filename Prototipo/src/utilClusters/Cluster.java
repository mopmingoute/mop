package utilClusters;

import java.util.ArrayList;

public class Cluster {
	
	private int numero;  // número de cluster empezando en cero
	
	private ArrayList<Observable> observaciones;
	
	private Observable baricentro;  // baricentro del cluster aunque la observación no pertenezca al cluster
	
	private Observable baricentroObs; // observación del cluster más cercana al baricentro.

}
