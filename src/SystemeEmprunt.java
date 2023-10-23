import java.util.Random;

class SystemeEmprunt {

  /* Constantes de la simulation */

	static final int NB_SITES = 5;
	static final int NB_CLIENTS = 20;

	private Site[] sites = new Site[NB_SITES];
	private Client[] clients = new Client[NB_CLIENTS];
	private Camion camion = null;

	/* Constructeur du systeme d'emprunt */
	SystemeEmprunt() {

		/* Instanciation des sites */
		for(int i = 0; i < NB_SITES; i++)
			sites[i] = new Site(i);

		/* Instanciation des clients */
    Random r = new Random();
    for(int i = 0; i < NB_CLIENTS; i++) {
			int siteDep = r.nextInt(NB_SITES);
			int siteArr = r.nextInt(NB_SITES);
			clients[i] = new Client(sites[siteDep], sites[siteArr]);
		}

		/* Instanciation du camion */
		camion = new Camion(sites);

    /* Démarrage du camion et des clients */
    /* TODO */
  }

  public static void main(String[] args) {
    new SystemeEmprunt();
  }

} // SystemeEmprunt
