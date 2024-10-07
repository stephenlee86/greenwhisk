public class Battery {
    // BATT = 3.8 for server, 0.1 for pi (kwh)
    public double CAPACITY = 3.8 * 3.6E6;
    public double level = CAPACITY;


    // Used first
    // See this as using solar power and falling back on the battery
    private double solar_reserve = 0.0;

    private boolean criticalAtLastCheck = false;

    public Battery(int depth){
        CAPACITY *= depth;
        level = CAPACITY;
    }


    public void charge(double amt){ // Energy coming from solar array
        if(solar_reserve > 0.0){
            level += solar_reserve;
            if(level > CAPACITY) level = CAPACITY;
        }
        solar_reserve = amt;
    }


    public void draw(double amt) throws Exception {
        if( solar_reserve > 0.0 ){ // Use nrg from array first
            solar_reserve -= amt;
            if (solar_reserve < 0.0){
                level += solar_reserve;
                solar_reserve = 0.0;
            }
        }else{
            level -= amt;
        }

        boolean critical = isCritical();

        if(!criticalAtLastCheck && critical){
            Statistics.softShutdown();
        }

        criticalAtLastCheck = critical;

        if(level < 0) throw new Exception("Battery Depleted");
    }

    public boolean isCritical(){
        return level < CAPACITY * .2;
    }

    public String toString(){
        return String.format("%0.2d%%", (level/CAPACITY));
    }

    public double getLevel(){
        return this.level + this.solar_reserve;
    }
}
