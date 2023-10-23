import java.util.logging.Logger;

public class SupplyTruck extends Thread {
    Site[] sites;
    int currentSite;
    int stock;

    public SupplyTruck(Site[] sites) {
        this.sites = sites;
        // if sites.len() == 0...
        this.currentSite = 0;
        this.stock = 3;
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("The truck is calling it a day");
                break;
            }

            // currentSite.try_refill();
            stock = sites[currentSite].adjustStock(stock);

            int nextSite;
            if (currentSite < sites.length - 1) {
                nextSite = currentSite + 1;
            } else {
                nextSite = 0;
            }

            try {
                Thread.sleep(sites[currentSite].distanceBetween(sites[nextSite]));
            } catch (InterruptedException e) {
                Logger.getGlobal().warning("Sleep Interrupted!");
                /* Clean up whatever needs to be handled before interrupting */
                Thread.currentThread().interrupt();
            }

            currentSite = nextSite;
        }
    }
}
