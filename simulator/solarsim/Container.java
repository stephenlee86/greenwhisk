public class Container {
    public boolean coldStart = false;
    public boolean free = true;
    public int type = -1;
    public int age = 0;



    public void stageWorkload(Workload w){
        coldStart = w.type != this.type;
        if(coldStart) Statistics.coldStart();
        else Statistics.warmStart();
        age = 0;
        free = false;
        type = w.type;
    }




    public void evict(){
        free = true;
    }
}
