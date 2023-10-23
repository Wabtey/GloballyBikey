public class Site {

    /* Constantes communes à tous les sites */

    static final int STOCK_INIT = 5;
    static final int STOCK_MAX = 10;
    static final int BORNE_SUP = 8;
    static final int BORNE_INF = 2;
    // duplicate constant...
    static final int NB_SITES = 5;

    int id;
    int currentStock;

    public Site(int id) {
        this.id = id;
        this.currentStock = STOCK_INIT;
    }

    public int distanceBetween(Site arrivalSite) {
        if (arrivalSite.id > this.id) {
            return arrivalSite.id - this.id;
        } else {
            // + 1 cause the first id is 0
            return NB_SITES - this.id + 1 + arrivalSite.id;
        }
    }

    // NOTE: In this setup, customers must wait each other in a Site and same for
    // the Truck

    /** Used by Customers */
    public synchronized void borrow() {
        if (currentStock <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        currentStock--;
        afficher();
    }

    /** Used by Customers */
    public synchronized void returnBike() {
        if (currentStock >= STOCK_MAX) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        currentStock++;
        afficher();
    }

    /**
     * Used by Truck.
     * 
     * @return the new truck's stock.
     */
    public synchronized int adjustStock(int truckStock) {
        if (currentStock < BORNE_INF) {
            int amountToRefill = BORNE_INF - currentStock;
            int amountRefilled;
            if (truckStock >= amountToRefill) {
                amountRefilled = amountToRefill;
            } else {
                amountRefilled = truckStock;
            }
            currentStock += amountRefilled;
            return truckStock - amountRefilled;
        } else if (currentStock > BORNE_SUP) {
            currentStock = BORNE_SUP;
            return truckStock + currentStock - BORNE_SUP;
        } else {
            return truckStock;
        }
    }

    /**
     * Affiche l'etat de l'objet site
     */
    public void afficher() {
        System.out.println("The site " + id + " contains " + currentStock + " bike(s).");
    }
}
