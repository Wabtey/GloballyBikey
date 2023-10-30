import java.util.Random;
import java.util.logging.Logger;

class SystemeEmprunt {

    /* Constantes de la simulation */

    public static final int NB_SITES = 500;
    static final int NB_CLIENTS = 100;

    private Site[] sites = new Site[NB_SITES];
    private Customer[] customers = new Customer[NB_CLIENTS];
    private SupplyTruck truck = null;

    /* Constructeur du systeme d'emprunt */
    SystemeEmprunt() {

        /* Instanciation des sites */
        for (int i = 0; i < NB_SITES; i++)
            sites[i] = new Site(i);

        /* Instanciation and Starting des clients */
        Random r = new Random();
        for (int i = 0; i < NB_CLIENTS; i++) {
            int siteDep = r.nextInt(NB_SITES);
            int siteArr = r.nextInt(NB_SITES);
            customers[i] = new Customer(i, sites[siteDep], sites[siteArr]);
            System.out.println("Customer " + i + ": site " + siteDep + " -> site " + siteArr + ".");
            // Each customer will start delayed
            customers[i].start();
        }

        /* Instanciation and Starting du camion */
        truck = new SupplyTruck(sites);
        truck.setDaemon(true);
        truck.start();

        for (int i = 0; i < NB_CLIENTS; i++) {
            // eww... this loop will freeze on the first iteration
            try {
                customers[i].join();
            } catch (InterruptedException e) {
                Logger.getGlobal().warning("Thread Customer " + i + " interrupted");
                /* Clean up whatever needs to be handled before interrupting */
                customers[i].interrupt();
            }
        }

        // There is no more customers on the road.
        // The truck being a Daemon, will stop itself being the only one left.
        System.out.println("The truck is calling it a day");
    }

    public static void main(String[] args) {
        while (true) {
            long start = System.currentTimeMillis();

            new SystemeEmprunt();

            long finish = System.currentTimeMillis();
            float timeElapsed = finish - start;
            System.out.println("Tasks completed in " + timeElapsed / 1000 + "s");
        }
    }

} // SystemeEmprunt
