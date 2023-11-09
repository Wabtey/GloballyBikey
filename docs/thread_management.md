# Thread Management

## NotifyAll

fix Truck not waking up customers after unloading from a site

Example:
If Customer 1 waits at Site 0, to return their bike. Site 0 has 9 bike already, so Customer 1 waits. Truck arrives and unloads 1 bike from Site 0 to match the `BORNE_SUP` (8), if truck doesn't notify Customer 1 will be stuck.
A `notifyAll` is mandatory, because if truck unloads 2 bike and there is two customers waiting to return their bike. A `notify` is not enough

### Optimize Notify call

For the release version 3.
Adjust the level of granularity by creating a new special Object
within `Site` which is used only to be called as `wait()`.
With this setup, we could send notify only to a special site,
and avoid waking up others for nothing (from other sites).

## Parameters

Without changing any initial parameters, we can put some bike on the truck already.
But 3 can still leads to some freeze: All sites are in boundaries or below, the truck can't refill to load those which are below.
4 seems to be good enough (we can prove it by checking all sites and their possibles values).
But as it was missing a single bike to be unloaded, it seems logic.
But it isn't completely certain because the truck could have refilled another site before the critical one.

We could add some strategy to our refill truck, like unloads some sites with "enought" bike to loads those in critical state (customers waiting, or bike = 0).

### Much more customers than bikes

```java
public static final int NB_SITES = 100;
static final int NB_CLIENTS = 500;
```

We could compute the number of sites required to satisfy `x` customers,
but we will create a dynamic solution.

The truck must redistribute bike if there is some famine / stock with 0 bike.

### Solution: Smart Truck

The supply truck will map out the sites to keep track of all stocks.
If the current site is under the lower boundary, it will behave the same (try to restock to the `INITIAL_STOCK`).
If there is a site with 0 bike in stock,
the truck will anticipate and unload the largest stock of all the site before the first site at 0.

#### Unsolved Problems

- Site choosen outside our set boundaries (between wurrent and starving site)

  ```log
  Current Site: 2
  Site Choosen: 0 - 9/10
  Starving Site: 3 - 0/10
  ```

- The truck does not seem to move to the next site and reiter an action.
  - in `log/v2/truck_stuck.log`

  ```log
  Site Choosen: 1 - 10/10
  Current Site: 1
  Starving Site: 3 - 3/10
  Largest Sites are:
    - Site 1: 10/10
  Truck (0->5) force unloads 5 on  site 1 (new=5)
  Truck (5->10) force unloads 5 on  site 1 (new=0)
  ```

- Some customers stay asleep even if there is activity. (`log/v2/c26_is_crying.log`)
- ~~Comparable cast impossible (idk why but it's not important cause it's for debug prints)~~

## Debug Methods

- Reading :)
- Run a *** load of times our code and wait for freezes

  ```java
  public static void main(String[] args) {
      while (true) {
          long start = System.currentTimeMillis();

          new SystemeEmprunt();

          long finish = System.currentTimeMillis();
          float timeElapsed = finish - start;
          System.out.println("Tasks completed in " + timeElapsed / 1000 + "s");

          // We could add a sleep here to prevent overlapping prints.
      }
  }
  ```

### Truck unloads until SUP Boundary

## Concerning Logs

There is some initialisations logs missing (while using the *brute force* debugging method).

- `bad_exec_5.log`

  Customer 10 is infinitly blocked, the truck can't balance the sites cause all others are in boundaries.

  ```log
  The site 3 contains 3 bike(s).
  Customer 3: site 1 -> site 1.
  The site 3 contains 2 bike(s).
  Customer 4: site 4 -> site 3.
  The site 1 contains 4 bike(s).
  Customer 5: site 3 -> site 2.
  The site 4 contains 4 bike(s).
  Customer 6: site 3 -> site 2.
  The site 3 contains 1 bike(s).
  Customer 7: site 3 -> site 1.
  The site 3 contains 0 bike(s).
  Customer 8: site 3 -> site 0.
  Customer 7 waits a bike on site 3
  Customer 9: site 3 -> site 0.
  Customer 8 waits a bike on site 3
  Customer 10: site 3 -> site 0.
  Customer 9 waits a bike on site 3
  Customer 11: site 1 -> site 2.
  Customer 10 waits a bike on site 3
  Customer 12: site 2 -> site 0.
  The site 1 contains 3 bike(s).
  Customer 13: site 1 -> site 2.
  The site 2 contains 4 bike(s).
  Customer 14: site 4 -> site 1.
  The site 1 contains 2 bike(s).
  Customer 15: site 3 -> site 0.
  The site 4 contains 3 bike(s).
  Customer 16: site 3 -> site 1.
  Customer 15 waits a bike on site 3
  Customer 17: site 0 -> site 3.
  Customer 16 waits a bike on site 3
  Customer 18: site 1 -> site 1.
  The site 0 contains 4 bike(s).
  Customer 19: site 0 -> site 1.
  The site 1 contains 1 bike(s).
  The site 0 contains 3 bike(s).
  The site 4 contains 4 bike(s).
  Customer 2: Finished.
  The site 2 contains 5 bike(s).
  Customer 11: Finished.
  The site 2 contains 6 bike(s).
  Customer 13: Finished.
  The site 1 contains 2 bike(s).
  Customer 19: Finished.
  The site 0 contains 4 bike(s).
  Customer 1: Finished.
  The site 1 contains 3 bike(s).
  Customer 14: Finished.
  The site 3 contains 1 bike(s).
  Customer 17: Finished.
  The site 3 contains 0 bike(s).
  Customer 16 waits a bike on site 3
  Customer 15 waits a bike on site 3
  Customer 10 waits a bike on site 3
  Customer 9 waits a bike on site 3
  The site 1 contains 4 bike(s).
  Customer 0: Finished.
  Customer 8 waits a bike on site 3
  Truck (1) loads 2 on site 3
  The site 3 contains 2 bike(s).
  The site 3 contains 1 bike(s).
  The site 3 contains 0 bike(s).
  Customer 9 waits a bike on site 3
  Customer 10 waits a bike on site 3
  Customer 15 waits a bike on site 3
  The site 0 contains 5 bike(s).
  Customer 12: Finished.
  The site 3 contains 1 bike(s).
  Customer 4: Finished.
  The site 3 contains 0 bike(s).
  Customer 15 waits a bike on site 3
  Customer 10 waits a bike on site 3
  The site 2 contains 7 bike(s).
  Customer 5: Finished.
  The site 2 contains 8 bike(s).
  Customer 6: Finished.
  The site 1 contains 5 bike(s).
  Customer 3: Finished.
  The site 1 contains 6 bike(s).
  Customer 18: Finished.
  The site 0 contains 6 bike(s).
  Customer 8: Finished.
  The site 1 contains 7 bike(s).
  Customer 7: Finished.
  The site 1 contains 8 bike(s).
  Customer 16: Finished.
  The site 0 contains 7 bike(s).
  Customer 9: Finished.
  Truck (0) loads 1 on site 3
  The site 3 contains 1 bike(s).
  The site 3 contains 0 bike(s).
  Customer 10 waits a bike on site 3
  The site 0 contains 8 bike(s).
  Customer 15: Finished.
  ```

- `bad_exec_6.log`

  Same as `bad_exec_5.log` but with better logs.
  Customer 17 is infinitly blocked, The truck can't refill its stock with other sites (all in boundaries).

  ```log
  The site 3 contains 0 bike(s), after that Customer 5 borrowed.
  Customer 7: site 1 -> site 3.
  The site 1 contains 3 bike(s), after that Customer 6 borrowed.
  Customer 8: site 1 -> site 4.
  The site 1 contains 2 bike(s), after that Customer 7 borrowed.
  Customer 9: site 0 -> site 1.
  The site 1 contains 1 bike(s), after that Customer 8 borrowed.
  Customer 10: site 3 -> site 1.
  The site 0 contains 4 bike(s), after that Customer 9 borrowed.
  Customer 11: site 3 -> site 0.
  Customer 10 waits a bike on site 3
  Customer 12: site 3 -> site 2.
  Customer 11 waits a bike on site 3
  Customer 13: site 3 -> site 0.
  Customer 12 waits a bike on site 3
  Customer 14: site 1 -> site 4.
  Customer 13 waits a bike on site 3
  Customer 15: site 3 -> site 1.
  The site 1 contains 0 bike(s), after that Customer 14 borrowed.
  Customer 16: site 0 -> site 1.
  Customer 15 waits a bike on site 3
  Customer 17: site 3 -> site 1.
  The site 0 contains 3 bike(s), after that Customer 16 borrowed.
  Customer 18: site 3 -> site 1.
  Customer 17 waits a bike on site 3
  Customer 19: site 0 -> site 2.
  Customer 18 waits a bike on site 3
  The site 0 contains 2 bike(s), after that Customer 19 borrowed.
  The site 4 contains 6 bike(s), after that Customer 0 returned.
  Customer 0: Finished.
  The site 2 contains 6 bike(s), after that Customer 2 returned.
  Customer 2: Finished.
  The site 1 contains 1 bike(s), after that Customer 9 returned.
  Customer 9: Finished.
  The site 1 contains 2 bike(s), after that Customer 16 returned.
  Customer 16: Finished.
  The site 3 contains 1 bike(s), after that Customer 7 returned.
  Customer 7: Finished.
  The site 3 contains 0 bike(s), after that Customer 10 borrowed.
  Customer 18 waits a bike on site 3
  Customer 17 waits a bike on site 3
  Customer 15 waits a bike on site 3
  Customer 13 waits a bike on site 3
  Customer 12 waits a bike on site 3
  Customer 11 waits a bike on site 3
  The site 2 contains 7 bike(s), after that Customer 19 returned.
  Customer 19: Finished.
  The site 4 contains 7 bike(s), after that Customer 8 returned.
  Customer 8: Finished.
  The site 4 contains 8 bike(s), after that Customer 14 returned.
  Customer 14: Finished.
  The site 1 contains 3 bike(s), after that Customer 1 returned.
  Customer 1: Finished.
  Truck (3->1) loads 2 on site 3(2)
  The site 3 contains 1 bike(s), after that Customer 18 borrowed.
  The site 3 contains 0 bike(s), after that Customer 11 borrowed.
  Customer 12 waits a bike on site 3
  Customer 13 waits a bike on site 3
  Customer 15 waits a bike on site 3
  Customer 17 waits a bike on site 3
  The site 1 contains 4 bike(s), after that Customer 5 returned.
  Customer 5: Finished.
  The site 0 contains 3 bike(s), after that Customer 6 returned.
  Customer 6: Finished.
  The site 3 contains 1 bike(s), after that Customer 3 returned.
  Customer 3: Finished.
  The site 3 contains 0 bike(s), after that Customer 12 borrowed.
  Customer 17 waits a bike on site 3
  Customer 15 waits a bike on site 3
  The site 3 contains 1 bike(s), after that Customer 4 returned.
  Customer 4: Finished.
  The site 3 contains 0 bike(s), after that Customer 13 borrowed.
  Customer 15 waits a bike on site 3
  Customer 17 waits a bike on site 3
  The site 1 contains 5 bike(s), after that Customer 10 returned.
  Customer 10: Finished.
  The site 0 contains 4 bike(s), after that Customer 11 returned.
  Customer 11: Finished.
  The site 1 contains 6 bike(s), after that Customer 18 returned.
  Customer 18: Finished.
  The site 0 contains 5 bike(s), after that Customer 13 returned.
  Customer 13: Finished.
  Truck (1->0) loads 1 on site 3(1)
  The site 3 contains 0 bike(s), after that Customer 15 borrowed.
  Customer 17 waits a bike on site 3
  The site 2 contains 8 bike(s), after that Customer 12 returned.
  Customer 12: Finished.
  The site 1 contains 7 bike(s), after that Customer 15 returned.
  Customer 15: Finished.
  ```

### Truck unloads to `STOCK_INIT` whenever it has to unload a site

- `bad_exec_7.log`

  Customer 19 is famished. The truck has 3 by default in its stock and no other sites are above the superior boundary.
  Solved by putting one more in the default truck stock.

  ```log
  Customer 0: site 0 -> site 4.
  Customer 1: site 0 -> site 0.
  The truck is calling it a day
  The site 0 contains 4 bike(s), after that Customer 0 borrowed.
  Customer 2: site 3 -> site 4.
  The site 0 contains 3 bike(s), after that Customer 1 borrowed.
  Customer 3: site 4 -> site 4.
  The site 3 contains 4 bike(s), after that Customer 2 borrowed.
  Customer 4: site 4 -> site 1.
  The site 4 contains 4 bike(s), after that Customer 3 borrowed.
  Customer 5: site 4 -> site 4.
  The site 4 contains 3 bike(s), after that Customer 4 borrowed.
  Customer 6: site 4 -> site 4.
  The site 4 contains 2 bike(s), after that Customer 5 borrowed.
  Customer 7: site 3 -> site 1.
  The site 4 contains 1 bike(s), after that Customer 6 borrowed.
  Customer 8: site 3 -> site 4.
  The site 3 contains 3 bike(s), after that Customer 7 borrowed.
  Customer 9: site 0 -> site 2.
  The site 3 contains 2 bike(s), after that Customer 8 borrowed.
  Customer 10: site 4 -> site 0.
  Customer 11: site 1 -> site 0.
  The site 0 contains 2 bike(s), after that Customer 9 borrowed.
  The site 4 contains 0 bike(s), after that Customer 10 borrowed.
  Customer 12: site 2 -> site 4.
  The site 1 contains 4 bike(s), after that Customer 11 borrowed.
  Customer 13: site 3 -> site 4.
  The site 2 contains 4 bike(s), after that Customer 12 borrowed.
  Customer 14: site 1 -> site 0.
  The site 3 contains 1 bike(s), after that Customer 13 borrowed.
  Customer 15: site 0 -> site 0.
  The site 1 contains 3 bike(s), after that Customer 14 borrowed.
  Customer 16: site 3 -> site 1.
  The site 0 contains 1 bike(s), after that Customer 15 borrowed.
  Customer 17: site 0 -> site 0.
  The site 3 contains 0 bike(s), after that Customer 16 borrowed.
  Customer 18: site 3 -> site 2.
  The site 0 contains 0 bike(s), after that Customer 17 borrowed.
  Customer 19: site 3 -> site 3.
  Customer 18 waits a bike on site 3
  Customer 19 waits a bike on site 3
  Truck (3->1) loads 2 on site 0(2)
  The site 4 contains 1 bike(s), after that Customer 2 returned.
  Customer 2: Finished.
  The site 4 contains 2 bike(s), after that Customer 8 returned.
  Customer 8: Finished.
  The site 4 contains 3 bike(s), after that Customer 13 returned.
  Customer 13: Finished.
  The site 2 contains 5 bike(s), after that Customer 9 returned.
  Customer 9: Finished.
  The site 0 contains 3 bike(s), after that Customer 10 returned.
  Customer 10: Finished.
  The site 4 contains 4 bike(s), after that Customer 12 returned.
  Customer 12: Finished.
  The site 1 contains 4 bike(s), after that Customer 4 returned.
  Customer 4: Finished.
  The site 4 contains 5 bike(s), after that Customer 0 returned.
  Customer 0: Finished.
  Truck (1->0) loads 1 on site 3(1)
  The site 3 contains 0 bike(s), after that Customer 18 borrowed.
  Customer 19 waits a bike on site 3
  The site 1 contains 5 bike(s), after that Customer 7 returned.
  Customer 7: Finished.
  The site 1 contains 6 bike(s), after that Customer 16 returned.
  Customer 16: Finished.
  The site 0 contains 4 bike(s), after that Customer 11 returned.
  Customer 11: Finished.
  The site 0 contains 5 bike(s), after that Customer 14 returned.
  Customer 14: Finished.
  The site 0 contains 6 bike(s), after that Customer 1 returned.
  Customer 1: Finished.
  The site 4 contains 6 bike(s), after that Customer 3 returned.
  Customer 3: Finished.
  The site 4 contains 7 bike(s), after that Customer 5 returned.
  Customer 5: Finished.
  The site 4 contains 8 bike(s), after that Customer 6 returned.
  Customer 6: Finished.
  The site 0 contains 7 bike(s), after that Customer 15 returned.
  Customer 15: Finished.
  The site 0 contains 8 bike(s), after that Customer 17 returned.
  Customer 17: Finished.
  The site 2 contains 6 bike(s), after that Customer 18 returned.
  Customer 18: Finished.
  ```

### Truck's `STOCK_INIT` is set to 4

- `bad_exec_8.log`

  Customer 14 is famished. The truck has 4 by default in its stock and no other sites are above the superior boundary.
  Solved by ??? Truck unloads to `STOCK_INIT` when a site is below `BORNE_INF` ???

  ```log
  Customer 0: site 4 -> site 2.
  Customer 1: site 2 -> site 2.
  Customer 2: site 3 -> site 1.
  The site 4 contains 4 bike(s), after that Customer 0 borrowed.
  The site 2 contains 4 bike(s), after that Customer 1 borrowed.
  Customer 3: site 3 -> site 0.
  The site 3 contains 4 bike(s), after that Customer 2 borrowed.
  Customer 4: site 3 -> site 1.
  The site 3 contains 3 bike(s), after that Customer 3 borrowed.
  Customer 5: site 3 -> site 2.
  The site 3 contains 2 bike(s), after that Customer 4 borrowed.
  Customer 6: site 2 -> site 3.
  The site 3 contains 1 bike(s), after that Customer 5 borrowed.
  Customer 7: site 3 -> site 4.
  The site 2 contains 3 bike(s), after that Customer 6 borrowed.
  Customer 8: site 3 -> site 0.
  The site 3 contains 0 bike(s), after that Customer 7 borrowed.
  Customer 9: site 0 -> site 0.
  Customer 8 waits a bike on site 3
  Customer 10: site 2 -> site 2.
  The site 0 contains 4 bike(s), after that Customer 9 borrowed.
  Customer 11: site 4 -> site 2.
  The site 2 contains 2 bike(s), after that Customer 10 borrowed.
  Customer 12: site 3 -> site 2.
  The site 4 contains 3 bike(s), after that Customer 11 borrowed.
  Customer 13: site 2 -> site 2.
  Customer 12 waits a bike on site 3
  Customer 14: site 3 -> site 0.
  The site 2 contains 1 bike(s), after that Customer 13 borrowed.
  Customer 15: site 4 -> site 1.
  Customer 14 waits a bike on site 3
  Customer 16: site 2 -> site 1.
  The site 4 contains 2 bike(s), after that Customer 15 borrowed.
  Customer 17: site 2 -> site 4.
  The site 2 contains 0 bike(s), after that Customer 16 borrowed.
  Customer 18: site 1 -> site 4.
  Customer 17 waits a bike on site 2
  Customer 19: site 3 -> site 0.
  The site 1 contains 4 bike(s), after that Customer 18 borrowed.
  Customer 19 waits a bike on site 3
  The site 3 contains 1 bike(s), after that Customer 6 returned.
  Customer 6: Finished.
  The site 3 contains 0 bike(s), after that Customer 8 borrowed.
  Customer 19 waits a bike on site 3
  Customer 14 waits a bike on site 3
  Customer 12 waits a bike on site 3
  The site 4 contains 3 bike(s), after that Customer 7 returned.
  Customer 7: Finished.
  The site 0 contains 5 bike(s), after that Customer 3 returned.
  Customer 3: Finished.
  Truck (4->2) loads 2 on site 2(2)
  The site 2 contains 1 bike(s), after that Customer 17 borrowed.
  The site 2 contains 2 bike(s), after that Customer 0 returned.
  Customer 0: Finished.
  The site 1 contains 5 bike(s), after that Customer 2 returned.
  Customer 2: Finished.
  The site 1 contains 6 bike(s), after that Customer 15 returned.
  Customer 15: Finished.
  The site 1 contains 7 bike(s), after that Customer 4 returned.
  Customer 4: Finished.
  The site 4 contains 4 bike(s), after that Customer 18 returned.
  Customer 18: Finished.
  The site 0 contains 6 bike(s), after that Customer 8 returned.
  Customer 8: Finished.
  Truck (2->0) loads 2 on site 3(2)
  The site 3 contains 1 bike(s), after that Customer 19 borrowed.
  The site 3 contains 0 bike(s), after that Customer 12 borrowed.
  Customer 14 waits a bike on site 3
  The site 2 contains 3 bike(s), after that Customer 11 returned.
  Customer 11: Finished.
  The site 2 contains 4 bike(s), after that Customer 5 returned.
  Customer 5: Finished.
  The site 4 contains 5 bike(s), after that Customer 17 returned.
  Customer 17: Finished.
  The site 2 contains 5 bike(s), after that Customer 1 returned.
  Customer 1: Finished.
  The site 1 contains 8 bike(s), after that Customer 16 returned.
  Customer 16: Finished.
  The site 0 contains 7 bike(s), after that Customer 9 returned.
  Customer 9: Finished.
  The site 2 contains 6 bike(s), after that Customer 10 returned.
  Customer 10: Finished.
  The site 2 contains 7 bike(s), after that Customer 13 returned.
  Customer 13: Finished.
  The site 0 contains 8 bike(s), after that Customer 19 returned.
  Customer 19: Finished.
  The site 2 contains 8 bike(s), after that Customer 12 returned.
  Customer 12: Finished.
  ```

## Truck unloads to `STOCK_INIT` when a site is below `BORNE_INF`

- `bad_exec_9.log`

  Customer 12 is famished. The truck has 4 by default in its stock and no other sites are above the superior boundary.
  Solved by ???

  ```log
  Customer 0: site 3 -> site 4.
  Customer 1: site 3 -> site 4.
  Customer 2: site 3 -> site 4.
  The site 3 contains 4 bike(s), after that Customer 1 borrowed.
  The site 3 contains 3 bike(s), after that Customer 0 borrowed.
  The site 4 contains 6 bike(s), after that Customer 1 returned.
  Customer 3: site 2 -> site 0.
  Customer 1: Finished.
  The site 4 contains 7 bike(s), after that Customer 0 returned.
  Customer 0: Finished.
  The site 3 contains 2 bike(s), after that Customer 2 borrowed.
  Customer 4: site 4 -> site 1.
  Customer 5: site 1 -> site 4.
  The site 2 contains 4 bike(s), after that Customer 3 borrowed.
  The truck is calling it a day
  The site 4 contains 6 bike(s), after that Customer 4 borrowed.
  Customer 6: site 3 -> site 1.
  The site 1 contains 4 bike(s), after that Customer 5 borrowed.
  Customer 7: site 3 -> site 2.
  The site 4 contains 7 bike(s), after that Customer 2 returned.
  Customer 2: Finished.
  The site 3 contains 1 bike(s), after that Customer 6 borrowed.
  The site 1 contains 5 bike(s), after that Customer 4 returned.
  Customer 8: site 4 -> site 1.
  Customer 4: Finished.
  The site 3 contains 0 bike(s), after that Customer 7 borrowed.
  The site 4 contains 8 bike(s), after that Customer 5 returned.
  Customer 5: Finished.
  The site 0 contains 6 bike(s), after that Customer 3 returned.
  Customer 3: Finished.
  The site 4 contains 7 bike(s), after that Customer 8 borrowed.
  Customer 9: site 3 -> site 3.
  The site 1 contains 6 bike(s), after that Customer 6 returned.
  Customer 6: Finished.
  Customer 10: site 0 -> site 4.
  Customer 9 waits a bike on site 3
  Customer 11: site 2 -> site 0.
  The site 0 contains 5 bike(s), after that Customer 10 borrowed.
  Customer 12: site 3 -> site 0.
  Customer 13: site 2 -> site 3.
  Customer 14: site 2 -> site 0.
  Customer 15: site 0 -> site 4.
  Customer 16: site 1 -> site 1.
  Customer 17: site 3 -> site 1.
  The site 2 contains 3 bike(s), after that Customer 11 borrowed.
  Customer 18: site 2 -> site 2.
  Customer 12 waits a bike on site 3
  The site 2 contains 2 bike(s), after that Customer 13 borrowed.
  The site 1 contains 5 bike(s), after that Customer 16 borrowed.
  The site 2 contains 1 bike(s), after that Customer 14 borrowed.
  Customer 17 waits a bike on site 3
  The site 0 contains 4 bike(s), after that Customer 15 borrowed.
  The site 2 contains 0 bike(s), after that Customer 18 borrowed.
  Customer 19: site 4 -> site 2.
  The site 4 contains 6 bike(s), after that Customer 19 borrowed.
  The site 3 contains 1 bike(s), after that Customer 13 returned.
  Customer 13: Finished.
  The site 3 contains 0 bike(s), after that Customer 9 borrowed.
  Customer 17 waits a bike on site 3
  Customer 12 waits a bike on site 3
  The site 1 contains 6 bike(s), after that Customer 8 returned.
  Customer 8: Finished.
  Truck (4->0) loads 4 on site 2(4)
  The site 2 contains 5 bike(s), after that Customer 7 returned.
  Customer 7: Finished.
  The site 4 contains 7 bike(s), after that Customer 10 returned.
  Customer 10: Finished.
  The site 0 contains 5 bike(s), after that Customer 11 returned.
  Customer 11: Finished.
  The site 0 contains 6 bike(s), after that Customer 14 returned.
  Customer 14: Finished.
  The site 4 contains 8 bike(s), after that Customer 15 returned.
  Customer 15: Finished.
  The site 2 contains 6 bike(s), after that Customer 19 returned.
  Customer 19: Finished.
  The site 1 contains 7 bike(s), after that Customer 16 returned.
  Customer 16: Finished.
  The site 2 contains 7 bike(s), after that Customer 18 returned.
  Customer 18: Finished.
  The site 3 contains 1 bike(s), after that Customer 9 returned.
  Customer 9: Finished.
  The site 3 contains 0 bike(s), after that Customer 17 borrowed.
  Customer 12 waits a bike on site 3
  The site 1 contains 8 bike(s), after that Customer 17 returned.
  Customer 17: Finished.
  ```
