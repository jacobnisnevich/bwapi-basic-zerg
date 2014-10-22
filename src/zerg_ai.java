import bwapi.*;
import bwta.*;

public class TestBot1{

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;

    public void run() {
        mirror.getModule().setEventListener(new DefaultBWListener() {
            @Override
            public void onUnitCreate(Unit unit) {
                System.out.println("New unit " + unit.getType());
            }

            @Override
            public void onStart() {
                game = mirror.getGame();
                self = game.self();

                //Use BWTA to analyze map
                //This may take a few minutes if the map is processed first time!
                System.out.println("Analyzing map...");
                BWTA.readMap();
                BWTA.analyze();
                System.out.println("Map data ready");
                
            }

            @Override
            public void onFrame() {
                game.setTextSize(10);
                game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

                StringBuilder units = new StringBuilder("My units:\n");
                
                int supplyDiff = self.supplyTotal() - self.supplyUsed();
          
                boolean needOverlord = false;
                
                if (supplyDiff <= 1) {
                	needOverlord = true;
                }

                //iterate through my units
                for (Unit myUnit : self.getUnits()) {
                    units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

                    //if there's enough minerals, train a drone
                    if (myUnit.getType() == UnitType.Zerg_Larva && self.minerals() >= 50 && !needOverlord) {
                        myUnit.train(UnitType.Zerg_Drone);
                    }
                    
                    //if necessary, train an overlord
                    if (myUnit.getType() == UnitType.Zerg_Larva && self.minerals() >= 100 && needOverlord) {
                        myUnit.train(UnitType.Zerg_Overlord);
                    }

                    //if it's a drone and it's idle, send it to the closest mineral patch
                    if (myUnit.getType().isWorker() && myUnit.isIdle()) {
                        Unit closestMineral = null;

                        //find the closest mineral
                        for (Unit neutralUnit : game.neutral().getUnits()) {
                            if (neutralUnit.getType().isMineralField()) {
                                if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                                    closestMineral = neutralUnit;
                                }
                            }
                        }

                        //if a mineral patch was found, send the drone to gather it
                        if (closestMineral != null) {
                            myUnit.gather(closestMineral, false);
                        }
                    }
                }

                //draw my units on screen
                game.drawTextScreen(10, 25, units.toString());
            }
        });

        mirror.startGame();
    }

    public static void main(String... args) {
        new TestBot1().run();
    }
}
