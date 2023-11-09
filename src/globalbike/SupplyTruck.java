package globalbike;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SupplyTruck extends Thread {

    static final int INITIAL_STOCK = 2;

    int stock;

    // /**
    // * ## Role Play
    // *
    // * This is an API used to request sites' infos from administrators.
    // */
    // Site[] sites;
    int currentSite;

    /**
     * ## Role Play
     * 
     * The truck's local map, used to monitor stocks at all sites
     * and decide which one to unload.
     * 
     * ## Note
     * 
     * kinda a duplicate of sites (perhaps just use a hashmap with key = Integer
     * value = Site)
     */
    HashMap<Integer, Site> siteMap = new HashMap<>();

    Optional<Integer> targetSite = Optional.empty();
    // Optional<Integer> largestSite = Optional.empty();

    public SupplyTruck(Site[] sites) {
        for (Site site : sites)
            siteMap.put(site.id, site);
        // if sites.len() == 0...
        this.currentSite = 0;
        this.stock = INITIAL_STOCK;
    }

    /**
     * Set the targetSite with the site having stock issue
     * 
     * OPTIMIZE: Select the closest if two sites have the same stock
     */
    private void updateStarvation() {
        // reset
        targetSite = Optional.empty();
        for (Site site : siteMap.values()) {
            if (site.currentStock <= Site.BORNE_INF &&
                    (!targetSite.isPresent() || site.currentStock <= siteMap.get(targetSite.get()).currentStock))
                targetSite = Optional.of(site.id);
        }
    }

    private Optional<Integer> closestLargestSite(int targetSite) {
        // First case: targetSite < currentSite
        // ---largest-----T---------C-------
        // Second case: currentSite < targetSite
        // -------C-----largest-----T-------

        // DOC: Draw in the markdown this loop range
        // 0 .. targetSite = 25 .. currentSite = 100 .. 500
        // 0 .. currentSite = 50 .. 70 .. targetSite = 150 .. 500

        // potentialClosestLargestSite
        Optional<Site> pCLS = siteMap
                .values()
                .stream()
                .filter(site -> (targetSite < currentSite && ((0 <= site.id && site.id < targetSite)
                        || (currentSite <= site.id && site.id < SystemeEmprunt.NB_SITES)))
                        || (currentSite < targetSite && (site.id <= currentSite && site.id < targetSite)))
                // no big deal if we don't have the closest to the current (need one more
                // calculus otherwise)
                // .max((site1, site2) -> site1.currentStock >= site2.currentStock ?
                // site1.currentStock
                // : site2.currentStock)
                .reduce((site1, site2) -> site1.currentStock >= site2.currentStock ? site1
                        : site2);
        // Integer.compare(site1.currentStock, site2.currentStock)

        // (site1, site2) -> site1.currentStock >= site2.currentStock ?
        // site1.currentStock: site2.currentStock
        int max = pCLS.isPresent() ? pCLS.get().currentStock : 0;

        if (pCLS.isPresent()) {
            System.out.println("Site Choosen: " + pCLS.get().id + " - "
                    + pCLS.get().currentStock + "/" + Site.STOCK_MAX);
        } else {
            System.out.println("There is no closest largest site");
        }

        List<Site> largestSites = siteMap.values().stream()
                .filter(site -> site.currentStock == max)
                .sorted() // is this mandatory ? (see `values()`)
                .collect(Collectors.toList());

        System.out.println("Largest Sites are:");
        for (Site site : largestSites)
            System.out.println("Site " + site.id + ": " + site.currentStock + "/" + Site.STOCK_MAX);

        return pCLS.isPresent() ? Optional.of(pCLS.get().id)
                : Optional.empty();
    }

    @Override
    public void run() {
        while (true) {
            // REMOVE: To simulate the truck maping out the sites, we're using this technic
            // (role playing).
            // Because technically we have a reference to all sites and therefore their
            // stocks.

            if (stock == 0 && !targetSite.isPresent()) {
                updateStarvation();
                if (targetSite.isPresent()) {
                    closestLargestSite(targetSite.get());
                    // // DEBUG: to visualize the largest Site
                    // break;
                    // trigger var truckNeedRefill = true;
                    // (which will be pass in the `adjust()` function)
                }
            }

            // TODO: if we are in the `nearest largest site`, unload even if in boundaries.
            // and ONLY IF our truck need to refill (stock == 0)

            // sites[currentSite].try_refill();
            stock = siteMap.get(currentSite).adjustStock(stock);

            int nextSite = currentSite < Collections.max(siteMap.keySet()) - 1 ? currentSite + 1 : 0;

            try {
                Thread.sleep(siteMap.get(currentSite).distanceBetween(siteMap.get(nextSite)));
            } catch (InterruptedException e) {
                Logger.getGlobal().warning("Sleep Interrupted!");
                /* Clean up whatever needs to be handled before interrupting */
                Thread.currentThread().interrupt();
            }

            currentSite = nextSite;
        }
    }
}
