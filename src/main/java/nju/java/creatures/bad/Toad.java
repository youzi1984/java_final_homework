package nju.java.creatures.bad;

import nju.java.Ground;
import nju.java.creatures.Creatures;

/**
 * Created by cbcwestwolf on 2017/12/28.
 */
public class Toad extends Bad{

    private int id ;
    public Toad(int x, int y, Ground ground, int id) {
        super(x, y, ground);
        power = 50;
        this.id = id+1;

    }

    @Override
    public String toString(){
        return "马仔"+id+"号";
    }

}
